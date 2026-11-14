package com.example.export

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import com.example.model.QuizQuestion
import com.example.model.SlideStyleConfig
import com.example.ui.theme.CorrectAnswerBorder
import com.example.ui.theme.CorrectAnswerDarkGreen
import com.example.ui.theme.CorrectAnswerTextYellow
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object SlideBitmapRenderer {

    private const val EXPORT_WIDTH = 2560
    private const val EXPORT_HEIGHT = 1440
    private const val SCALE = 2f // 2x of 1280x720 reference canvas

    fun renderSlideBitmap(
        context: Context,
        question: QuizQuestion,
        style: SlideStyleConfig,
        revealAnswer: Boolean = false,
        showLogo: Boolean = true,
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(EXPORT_WIDTH, EXPORT_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 1. Canvas Background Color & Optional Background Image
        canvas.drawColor(style.canvasBgColor.toArgb())
        style.backgroundImageUri?.let { bgUri ->
            loadScaledBitmap(context, bgUri, EXPORT_WIDTH, EXPORT_HEIGHT)?.let { bgBmp ->
                drawCenterCropBitmap(canvas, bgBmp, EXPORT_WIDTH.toFloat(), EXPORT_HEIGHT.toFloat())
                bgBmp.recycle()
            }
        }

        val outerPad = 12f * SCALE
        val innerWidth = EXPORT_WIDTH - (outerPad * 2f)
        val innerHeight = EXPORT_HEIGHT - (outerPad * 2f)
        val gapBetweenQAndOpts = 8f * SCALE

        // 2. Question Box
        val qBoxHeight = innerHeight * (style.questionBoxHeightPercent / 100f)
        val qBoxWidth = innerWidth * (style.questionBoxWidthPercent / 100f)
        val qBoxRect = RectF(
            outerPad,
            outerPad,
            outerPad + qBoxWidth,
            outerPad + qBoxHeight
        )
        val qRadius = 12f * SCALE

        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.argb(45, 0, 0, 0)
        }
        val qShadowRect = RectF(qBoxRect).apply { offset(0f, 4f * SCALE) }
        canvas.drawRoundRect(qShadowRect, qRadius, qRadius, shadowPaint)

        val qBoxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = style.questionBgColor.toArgb()
        }
        canvas.drawRoundRect(qBoxRect, qRadius, qRadius, qBoxPaint)

        val qBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            setStyle(Paint.Style.STROKE)
            strokeWidth = 1f * SCALE
            color = android.graphics.Color.argb(26, 0, 0, 0)
        }
        canvas.drawRoundRect(qBoxRect, qRadius, qRadius, qBorderPaint)

        // Question Text with Auto-Fit
        val qTypeface = loadTypeface(context, style.questionFont.fontResId)
        var fontSizePx = style.questionMaxFontSize * SCALE
        var lineHeightMult = 1.25f

        val qTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = style.questionTextColor.toArgb()
            typeface = Typeface.create(qTypeface, Typeface.BOLD)
            textSize = fontSizePx
        }

        val maxAvailHeight = (qBoxHeight - 16f * SCALE).coerceAtLeast(24f)
        val maxAvailWidth = (qBoxWidth - 20f * SCALE).coerceAtLeast(40f).toInt()

        val qString = question.question.ifBlank { " " }
        var layout = buildStaticLayout(qString, qTextPaint, maxAvailWidth, lineHeightMult, Layout.Alignment.ALIGN_CENTER)

        while (layout.height > maxAvailHeight && fontSizePx > 14f * SCALE) {
            fontSizePx -= 1f * SCALE
            val unscaled = fontSizePx / SCALE
            lineHeightMult = when {
                unscaled < 40f -> 1.12f
                unscaled < 46f -> 1.18f
                else -> 1.22f
            }
            qTextPaint.textSize = fontSizePx
            layout = buildStaticLayout(qString, qTextPaint, maxAvailWidth, lineHeightMult, Layout.Alignment.ALIGN_CENTER)
        }
        // Apply initial size if loop didn't run
        if (qTextPaint.textSize != fontSizePx) {
            qTextPaint.textSize = fontSizePx
            layout = buildStaticLayout(qString, qTextPaint, maxAvailWidth, lineHeightMult, Layout.Alignment.ALIGN_CENTER)
        }

        canvas.save()
        val textX = qBoxRect.left + (qBoxRect.width() - maxAvailWidth) / 2f
        val textY = qBoxRect.top + (qBoxRect.height() - layout.height) / 2f
        canvas.translate(textX, textY)
        layout.draw(canvas)
        canvas.restore()

        // 3. Options Container
        val optsTop = qBoxRect.bottom + gapBetweenQAndOpts + (4f * SCALE)
        val optsBottom = EXPORT_HEIGHT - outerPad - (4f * SCALE)
        val optsAvailHeight = (optsBottom - optsTop).coerceAtLeast(100f)
        val optsWidth = innerWidth * (style.optionsBlockWidthPercent / 100f)

        val singleOptHeight = (optsAvailHeight * (style.singleOptionHeightPercent / 100f))
            .coerceAtMost(optsAvailHeight * 0.245f)
        val optGap = ((optsAvailHeight - (singleOptHeight * 4f)) / 3f).coerceAtLeast(4f * SCALE)

        val optTypeface = loadTypeface(context, style.optionFont.fontResId)
        val optRadius = 8f * SCALE
        val letters = listOf("A", "B", "C", "D")

        for (i in 0 until 4) {
            val top = optsTop + i * (singleOptHeight + optGap)
            val optRect = RectF(outerPad, top, outerPad + optsWidth, top + singleOptHeight)
            val isCorrect = revealAnswer && i == question.correctIndex

            val optShadowRect = RectF(optRect).apply { offset(0f, 3f * SCALE) }
            canvas.drawRoundRect(optShadowRect, optRadius, optRadius, shadowPaint)

            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = if (isCorrect) CorrectAnswerDarkGreen.toArgb() else style.optionBgColor.toArgb()
            }
            canvas.drawRoundRect(optRect, optRadius, optRadius, bgPaint)

            val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                setStyle(Paint.Style.STROKE)
                strokeWidth = if (isCorrect) 2.5f * SCALE else 1f * SCALE
                color = if (isCorrect) CorrectAnswerBorder.toArgb() else android.graphics.Color.argb(26, 255, 255, 255)
            }
            canvas.drawRoundRect(optRect, optRadius, optRadius, borderPaint)

            val padX = 16f * SCALE
            val badgeSize = (32f * SCALE).coerceAtMost(singleOptHeight * 0.66f)
            val badgeCx = optRect.left + padX + (badgeSize / 2f)
            val badgeCy = optRect.centerY()

            val badgeBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.argb(51, 255, 255, 255)
            }
            canvas.drawCircle(badgeCx, badgeCy, badgeSize / 2f, badgeBgPaint)

            val badgeTextColor = if (isCorrect) CorrectAnswerTextYellow.toArgb() else style.optionTextColor.toArgb()
            val badgeTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = badgeTextColor
                textSize = 16f * SCALE
                typeface = Typeface.create(optTypeface, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            val badgeBaseline = badgeCy - ((badgeTextPaint.descent() + badgeTextPaint.ascent()) / 2f)
            canvas.drawText(letters[i], badgeCx, badgeBaseline, badgeTextPaint)

            val optTextStart = optRect.left + padX + badgeSize + (12f * SCALE)
            val optTextWidth = (optRect.right - padX - optTextStart).coerceAtLeast(20f).toInt()
            val optTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = badgeTextColor
                textSize = style.optionFontSize * SCALE
                typeface = Typeface.create(optTypeface, Typeface.BOLD)
            }
            val optText = question.options.getOrNull(i).orEmpty().ifBlank { " " }
            val optLayout = buildStaticLayout(optText, optTextPaint, optTextWidth, 1.15f, Layout.Alignment.ALIGN_NORMAL)

            canvas.save()
            val optY = optRect.top + (optRect.height() - optLayout.height) / 2f
            canvas.translate(optTextStart, optY)
            optLayout.draw(canvas)
            canvas.restore()
        }

        // 4. Optional Logo Overlay
        if (showLogo && style.logoImageUri != null) {
            val logoTargetSize = style.logoSizePx * SCALE
            loadScaledBitmap(context, style.logoImageUri, logoTargetSize.toInt(), logoTargetSize.toInt())?.let { logoBmp ->
                val left = (style.logoLeftFraction * EXPORT_WIDTH).coerceIn(0f, EXPORT_WIDTH - logoTargetSize)
                val top = (style.logoTopFraction * EXPORT_HEIGHT).coerceIn(0f, EXPORT_HEIGHT - logoTargetSize)
                drawFitContainBitmap(canvas, logoBmp, RectF(left, top, left + logoTargetSize, top + logoTargetSize))
                logoBmp.recycle()
            }
        }

        return bitmap
    }

    fun savePngToGalleryAndCache(
        context: Context,
        bitmap: Bitmap,
        fileName: String,
    ): Uri {
        val cacheDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val cacheFile = File(cacheDir, fileName)
        FileOutputStream(cacheFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        // Also persist to MediaStore Pictures/QuizSlideStudio on Android 10+ (zero permissions)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/QuizSlideStudio")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
            val resolver = context.contentResolver
            val itemUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            if (itemUri != null) {
                resolver.openOutputStream(itemUri)?.use { os ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, os)
                }
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(itemUri, values, null, null)
            }
        }

        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", cacheFile)
    }

    fun exportAllSlidesZip(
        context: Context,
        questions: List<QuizQuestion>,
        style: SlideStyleConfig,
        onProgress: (Int, Int) -> Unit,
    ): Uri {
        val cacheDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val zipFileName = "Quiz_Slides.zip"
        val cacheZipFile = File(cacheDir, zipFileName)

        ZipOutputStream(FileOutputStream(cacheZipFile)).use { zos ->
            val pngBuffer = ByteArrayOutputStream()
            questions.forEachIndexed { idx, q ->
                onProgress(idx + 1, questions.size)
                val bmp = renderSlideBitmap(context, q, style, revealAnswer = false, showLogo = true)
                pngBuffer.reset()
                bmp.compress(Bitmap.CompressFormat.PNG, 100, pngBuffer)
                bmp.recycle()

                zos.putNextEntry(ZipEntry("Question_${idx + 1}.png"))
                zos.write(pngBuffer.toByteArray())
                zos.closeEntry()
            }
        }

        // Also save ZIP to Downloads/QuizSlideStudio via MediaStore on API 29+ (zero permissions)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, zipFileName)
                put(MediaStore.Downloads.MIME_TYPE, "application/zip")
                put(MediaStore.Downloads.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/QuizSlideStudio")
                put(MediaStore.Downloads.IS_PENDING, 1)
            }
            val resolver = context.contentResolver
            val itemUri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            if (itemUri != null) {
                resolver.openOutputStream(itemUri)?.use { os ->
                    cacheZipFile.inputStream().use { input -> input.copyTo(os) }
                }
                values.clear()
                values.put(MediaStore.Downloads.IS_PENDING, 0)
                resolver.update(itemUri, values, null, null)
            }
        }

        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", cacheZipFile)
    }

    private fun buildStaticLayout(
        text: String,
        paint: TextPaint,
        width: Int,
        lineMult: Float,
        alignment: Layout.Alignment,
    ): StaticLayout {
        return StaticLayout.Builder.obtain(text, 0, text.length, paint, width.coerceAtLeast(1))
            .setAlignment(alignment)
            .setLineSpacing(0f, lineMult)
            .setIncludePad(false)
            .build()
    }

    private fun loadTypeface(context: Context, fontResId: Int): Typeface {
        return ResourcesCompat.getFont(context, fontResId) ?: Typeface.DEFAULT
    }

    private fun loadScaledBitmap(context: Context, uri: Uri, reqWidth: Int, reqHeight: Int): Bitmap? {
        return try {
            val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, boundsOpts)
            }
            var inSampleSize = 1
            val halfH = boundsOpts.outHeight / 2
            val halfW = boundsOpts.outWidth / 2
            while (halfH / inSampleSize >= reqHeight && halfW / inSampleSize >= reqWidth && reqHeight > 0 && reqWidth > 0) {
                inSampleSize *= 2
            }
            val decodeOpts = BitmapFactory.Options().apply { this.inSampleSize = inSampleSize }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOpts)
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun drawCenterCropBitmap(canvas: Canvas, bmp: Bitmap, targetW: Float, targetH: Float) {
        val scale = maxOf(targetW / bmp.width, targetH / bmp.height)
        val dx = (targetW - bmp.width * scale) / 2f
        val dy = (targetH - bmp.height * scale) / 2f
        val matrix = Matrix().apply {
            setScale(scale, scale)
            postTranslate(dx, dy)
        }
        canvas.drawBitmap(bmp, matrix, Paint(Paint.FILTER_BITMAP_FLAG))
    }

    private fun drawFitContainBitmap(canvas: Canvas, bmp: Bitmap, dst: RectF) {
        val scale = minOf(dst.width() / bmp.width, dst.height() / bmp.height)
        val drawW = bmp.width * scale
        val drawH = bmp.height * scale
        val left = dst.left + (dst.width() - drawW) / 2f
        val top = dst.top + (dst.height() - drawH) / 2f
        val matrix = Matrix().apply {
            setScale(scale, scale)
            postTranslate(left, top)
        }
        canvas.drawBitmap(bmp, matrix, Paint(Paint.FILTER_BITMAP_FLAG))
    }
}

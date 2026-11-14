package com.example.ui.theme

import androidx.annotation.FontRes
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val InterFontFamily = FontFamily(Font(R.font.inter, FontWeight.Normal))
val PoppinsFontFamily = FontFamily(Font(R.font.poppins, FontWeight.Normal))
val RobotoFontFamily = FontFamily(Font(R.font.roboto, FontWeight.Normal))
val MontserratFontFamily = FontFamily(Font(R.font.montserrat, FontWeight.Normal))
val NotoSerifFontFamily = FontFamily(Font(R.font.noto_serif, FontWeight.Normal))
val HindFontFamily = FontFamily(Font(R.font.hind, FontWeight.Normal))
val MuktaFontFamily = FontFamily(Font(R.font.mukta, FontWeight.Normal))
val TiroDevanagariFontFamily = FontFamily(Font(R.font.tiro_devanagari_hindi, FontWeight.Normal))
val NotoSerifDevanagariFontFamily = FontFamily(Font(R.font.noto_serif_devanagari, FontWeight.Normal))
val NotoSansDevanagariFontFamily = FontFamily(Font(R.font.noto_sans_devanagari, FontWeight.Normal))

enum class SlideFontOption(
    val displayName: String,
    val fontFamily: FontFamily,
    @FontRes val fontResId: Int,
) {
    TIRO_DEVANAGARI_HINDI("Tiro Devanagari Hindi", TiroDevanagariFontFamily, R.font.tiro_devanagari_hindi),
    NOTO_SERIF_DEVANAGARI("Noto Serif Devanagari", NotoSerifDevanagariFontFamily, R.font.noto_serif_devanagari),
    NOTO_SERIF("Noto Serif Bold", NotoSerifFontFamily, R.font.noto_serif),
    HIND("Hind", HindFontFamily, R.font.hind),
    MUKTA("Mukta", MuktaFontFamily, R.font.mukta),
    POPPINS("Poppins", PoppinsFontFamily, R.font.poppins),
    ROBOTO("Roboto", RobotoFontFamily, R.font.roboto),
    MONTSERRAT("Montserrat", MontserratFontFamily, R.font.montserrat),
    INTER("Inter", InterFontFamily, R.font.inter),
    NOTO_SANS_DEVANAGARI("Noto Sans Devanagari", NotoSansDevanagariFontFamily, R.font.noto_sans_devanagari),
}

val Typography = Typography(
    headlineSmall = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp
    ),
    titleMedium = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    labelMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelSmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp
    )
)

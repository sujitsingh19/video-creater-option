package com.example.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.PlaylistAddCheck
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun BulkUploadDialog(
    draftText: String,
    parsedCount: Int,
    onDraftChange: (String) -> Unit,
    onLoadSample: () -> Unit,
    onDismiss: () -> Unit,
    onConfirmLoad: () -> Unit,
) {
    val context = LocalContext.current
    val textFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let {
                try {
                    val content = context.contentResolver.openInputStream(it)?.bufferedReader()?.use { r -> r.readText() }
                    if (!content.isNullOrBlank()) {
                        onDraftChange(content)
                    }
                } catch (_: Exception) {
                }
            }
        }
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .widthIn(max = 640.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Manage & Bulk Upload (100+)",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("bulk_modal_close_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close bulk upload modal")
                    }
                }

                Text(
                    text = "Enter one question per line in Pipe (|) format:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "Question Text | Option A | Option B | Option C | Option D | Answer (A/B/C/D)",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onLoadSample,
                        modifier = Modifier.testTag("load_sample_questions_button")
                    ) {
                        Icon(Icons.Default.PlaylistAddCheck, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                        Text("Load 5 Sample Qs", fontSize = 12.sp)
                    }
                    OutlinedButton(
                        onClick = { textFilePicker.launch(arrayOf("text/plain", "text/csv", "text/*")) },
                        modifier = Modifier.testTag("import_txt_questions_button")
                    ) {
                        Icon(Icons.Default.FileOpen, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                        Text("Import .TXT", fontSize = 12.sp)
                    }
                }

                OutlinedTextField(
                    value = draftText,
                    onValueChange = onDraftChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .testTag("bulk_questions_input"),
                    textStyle = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    placeholder = {
                        Text(
                            "What is the capital of India? | Delhi | Mumbai | Kolkata | Chennai | A",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                    }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val countText = remember(parsedCount) {
                        if (parsedCount == 1) "1 valid question ready"
                        else "$parsedCount valid questions ready"
                    }
                    Text(
                        text = countText,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (parsedCount > 0) Color(0xFF4ADE80) else MaterialTheme.colorScheme.error
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("bulk_cancel_button")
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = onConfirmLoad,
                            enabled = parsedCount > 0,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2563EB),
                                contentColor = Color.White
                            ),
                            modifier = Modifier.testTag("bulk_load_confirm_button")
                        ) {
                            Text("Load Questions", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

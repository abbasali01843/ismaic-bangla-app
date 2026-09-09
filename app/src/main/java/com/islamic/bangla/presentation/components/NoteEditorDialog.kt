package com.islamic.bangla.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.islamic.bangla.presentation.viewmodel.ReadingViewModel
import kotlinx.coroutines.flow.first

/** Identifies the ayah/hadith/dua a note belongs to. */
data class NoteTarget(
    val type: String,
    val refId: Int,
    val title: String
)

/** Dialog for writing/editing/deleting a personal note on an item. */
@Composable
fun NoteEditorDialog(
    target: NoteTarget,
    viewModel: ReadingViewModel,
    onDismiss: () -> Unit
) {
    var text by rememberSaveable(target.type, target.refId) { mutableStateOf<String?>(null) }
    var hasExisting by rememberSaveable(target.type, target.refId) { mutableStateOf(false) }

    LaunchedEffect(target.type, target.refId) {
        val existing = viewModel.getNote(target.type, target.refId).first()
        text = existing?.noteText.orEmpty()
        hasExisting = existing != null
    }

    val current = text
    if (current == null) {
        AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = {},
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        )
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("নোট") },
            text = {
                Column {
                    Text(
                        text = target.title,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = current,
                        onValueChange = { text = it },
                        label = { Text("আপনার নোট লিখুন") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.saveNote(target.type, target.refId, target.title, current)
                        onDismiss()
                    },
                    enabled = current.isNotBlank()
                ) {
                    Text("সংরক্ষণ")
                }
            },
            dismissButton = {
                Row {
                    if (hasExisting) {
                        TextButton(
                            onClick = {
                                viewModel.deleteNote(target.type, target.refId)
                                onDismiss()
                            }
                        ) {
                            Text(
                                text = "মুছুন",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    TextButton(onClick = onDismiss) {
                        Text("বাতিল")
                    }
                }
            }
        )
    }
}

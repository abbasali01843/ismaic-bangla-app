package com.islamic.bangla.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.islamic.bangla.data.remote.audio.AudioConfig
import com.islamic.bangla.data.remote.audio.Reciter

private val banglaDigits = arrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')

private fun Int.toBanglaDigits(): String =
    toString().map { if (it in '0'..'9') banglaDigits[it - '0'] else it }.joinToString("")

/** Persistent mini-player shown above the bottom bar while recitation plays. */
@Composable
fun AudioPlayerBar(
    surahName: String,
    currentAyah: Int,
    isPlaying: Boolean,
    isLoading: Boolean,
    reciter: String,
    reciters: List<Reciter>,
    error: String?,
    onToggle: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onClose: () -> Unit,
    onReciterChange: (String) -> Unit,
    onBodyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var reciterMenuOpen by remember { mutableStateOf(false) }

    Surface(
        tonalElevation = 8.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column {
            Divider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "বন্ধ করুন")
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onBodyClick)
                        .padding(horizontal = 4.dp)
                ) {
                    Text(
                        text = if (currentAyah > 0) {
                            "$surahName • আয়াত ${currentAyah.toBanglaDigits()}"
                        } else {
                            surahName
                        },
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (error != null) {
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else {
                        Box {
                            TextButton(
                                onClick = { reciterMenuOpen = true },
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                            ) {
                                Text(
                                    text = AudioConfig.reciterName(reciter),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            DropdownMenu(
                                expanded = reciterMenuOpen,
                                onDismissRequest = { reciterMenuOpen = false }
                            ) {
                                reciters.forEach { item ->
                                    DropdownMenuItem(
                                        text = { Text(item.banglaName) },
                                        onClick = {
                                            reciterMenuOpen = false
                                            onReciterChange(item.edition)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                IconButton(onClick = onPrev) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = "আগের আয়াত")
                }
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(40.dp)
                            .padding(8.dp),
                        strokeWidth = 3.dp
                    )
                } else {
                    IconButton(onClick = onToggle) {
                        Icon(
                            imageVector = if (isPlaying) {
                                Icons.Default.Pause
                            } else {
                                Icons.Default.PlayArrow
                            },
                            contentDescription = if (isPlaying) "থামান" else "চালান",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                IconButton(onClick = onNext) {
                    Icon(Icons.Default.SkipNext, contentDescription = "পরের আয়াত")
                }
            }
        }
    }
}

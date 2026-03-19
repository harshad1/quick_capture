package com.claw.logger.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.claw.logger.data.PhotoScale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    photoScale: PhotoScale,
    photoFolderLabel: String,
    audioFolderLabel: String,
    onBack: () -> Unit,
    onScaleSelected: (PhotoScale) -> Unit,
    onPickPhotoFolder: () -> Unit,
    onPickAudioFolder: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    Button(onClick = onBack) {
                        Text("Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Card(shape = RoundedCornerShape(24.dp)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "Photo scale",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PhotoScale.entries.forEach { scale ->
                            AssistChip(
                                onClick = { onScaleSelected(scale) },
                                label = { Text(scale.label) },
                            )
                        }
                    }
                    Text(
                        text = "Current: ${photoScale.label}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            FolderCard(
                title = "Photo folder",
                folderLabel = photoFolderLabel,
                buttonLabel = "Choose photo folder",
                onPickFolder = onPickPhotoFolder,
            )

            FolderCard(
                title = "Audio folder",
                folderLabel = audioFolderLabel,
                buttonLabel = "Choose audio folder",
                onPickFolder = onPickAudioFolder,
            )
        }
    }
}

@Composable
private fun FolderCard(
    title: String,
    folderLabel: String,
    buttonLabel: String,
    onPickFolder: () -> Unit,
) {
    Card(shape = RoundedCornerShape(24.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = folderLabel,
                style = MaterialTheme.typography.bodyMedium,
            )
            Button(onClick = onPickFolder) {
                Text(buttonLabel)
            }
        }
    }
}

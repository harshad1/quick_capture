package com.claw.logger

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.documentfile.provider.DocumentFile
import com.claw.logger.data.AppPreferences
import com.claw.logger.ui.ClawLoggerTheme
import com.claw.logger.ui.SettingsScreen

class SettingsActivity : ComponentActivity() {
    private lateinit var preferences: AppPreferences

    private val pickPhotoFolder = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            persistTreeUri(uri)
            preferences.photoFolderUri = uri
            render()
        }
    }

    private val pickAudioFolder = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            persistTreeUri(uri)
            preferences.audioFolderUri = uri
            render()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = AppPreferences(this)
        render()
    }

    private fun render() {
        setContent {
            ClawLoggerTheme {
                SettingsScreen(
                    photoScale = preferences.photoScale,
                    photoFolderLabel = folderLabel(preferences.photoFolderUri),
                    audioFolderLabel = folderLabel(preferences.audioFolderUri),
                    onBack = ::finish,
                    onScaleSelected = {
                        preferences.photoScale = it
                        render()
                    },
                    onPickPhotoFolder = { pickPhotoFolder.launch(preferences.photoFolderUri) },
                    onPickAudioFolder = { pickAudioFolder.launch(preferences.audioFolderUri) },
                )
            }
        }
    }

    private fun folderLabel(uri: Uri?): String {
        if (uri == null) return "Not configured"
        return DocumentFile.fromTreeUri(this, uri)?.name ?: uri.toString()
    }

    private fun persistTreeUri(uri: Uri) {
        val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        contentResolver.takePersistableUriPermission(uri, flags)
    }
}

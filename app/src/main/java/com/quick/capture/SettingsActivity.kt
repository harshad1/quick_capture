package com.quick.capture

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import com.quick.capture.data.AppPreferences
import com.quick.capture.ui.QuickCaptureTheme
import com.quick.capture.ui.SettingsScreen

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
            QuickCaptureTheme {
                SettingsScreen(
                    photoScale = preferences.photoScale,
                    photoFolderLabel = folderPath(preferences.photoFolderUri),
                    audioFolderLabel = folderPath(preferences.audioFolderUri),
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

    private fun folderPath(uri: Uri?): String {
        if (uri == null) return "Not configured"
        return safPath(uri) ?: uri.path ?: uri.toString()
    }

    private fun safPath(uri: Uri): String? {
        val documentId = runCatching { DocumentsContract.getTreeDocumentId(uri) }.getOrNull()
            ?: return null
        return when (uri.authority) {
            "com.android.externalstorage.documents" -> externalStoragePath(documentId)
            "com.android.providers.downloads.documents" -> {
                documentId.removePrefix("raw:").takeIf { it != documentId }
            }
            else -> null
        }
    }

    private fun externalStoragePath(documentId: String): String? {
        val parts = documentId.split(":", limit = 2)
        val volume = parts.firstOrNull() ?: return null
        val relativePath = parts.getOrElse(1) { "" }
        val root = if (volume.equals("primary", ignoreCase = true)) {
            "/storage/emulated/0"
        } else {
            "/storage/$volume"
        }
        return if (relativePath.isBlank()) root else "$root/$relativePath"
    }

    private fun persistTreeUri(uri: Uri) {
        val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        contentResolver.takePersistableUriPermission(uri, flags)
    }
}

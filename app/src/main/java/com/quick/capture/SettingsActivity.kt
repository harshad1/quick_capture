package com.quick.capture

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.quick.capture.data.AppPreferences
import com.quick.capture.data.PhotoScale
import com.quick.capture.storage.SafPathFormatter
import com.quick.capture.ui.QuickCaptureTheme
import com.quick.capture.ui.SettingsScreen

class SettingsActivity : ComponentActivity() {
    private lateinit var preferences: AppPreferences
    private var photoScale by mutableStateOf(PhotoScale.FULL)
    private var photoFolderLabel by mutableStateOf("Not configured")
    private var audioFolderLabel by mutableStateOf("Not configured")

    private val pickPhotoFolder = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            persistTreeUri(uri)
            preferences.photoFolderUri = uri
            updateSettingsState()
        }
    }

    private val pickAudioFolder = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            persistTreeUri(uri)
            preferences.audioFolderUri = uri
            updateSettingsState()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = AppPreferences(this)
        updateSettingsState()

        setContent {
            QuickCaptureTheme {
                SettingsScreen(
                    photoScale = photoScale,
                    photoFolderLabel = photoFolderLabel,
                    audioFolderLabel = audioFolderLabel,
                    onScaleSelected = {
                        preferences.photoScale = it
                        photoScale = it
                    },
                    onPickPhotoFolder = { pickPhotoFolder.launch(preferences.photoFolderUri) },
                    onPickAudioFolder = { pickAudioFolder.launch(preferences.audioFolderUri) },
                )
            }
        }
    }

    private fun updateSettingsState() {
        photoScale = preferences.photoScale
        photoFolderLabel = SafPathFormatter.displayPath(preferences.photoFolderUri)
        audioFolderLabel = SafPathFormatter.displayPath(preferences.audioFolderUri)
    }

    private fun persistTreeUri(uri: Uri) {
        val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        contentResolver.takePersistableUriPermission(uri, flags)
    }
}

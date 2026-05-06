package com.quick.capture

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.quick.capture.importing.CaptureImportResult
import com.quick.capture.importing.ImportUseCase
import com.quick.capture.ui.QuickCaptureTheme
import com.quick.capture.ui.MainScreen
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : ComponentActivity() {
    private lateinit var importUseCase: ImportUseCase

    private var pendingPhotoUri: Uri? = null
    private var pendingPhotoFile: File? = null

    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val sourceUri = pendingPhotoUri
        if (!success || sourceUri == null) {
            cleanupPendingPhoto()
            return@registerForActivityResult
        }

        lifecycleScope.launch {
            try {
                when (val result = importUseCase.importCapturedPhoto(sourceUri)) {
                    CaptureImportResult.MissingSettings -> toast("Settings not configured")
                    CaptureImportResult.Failed -> toast("Photo save failed")
                    is CaptureImportResult.Saved -> toast("Saved ${result.fileName}")
                }
            } finally {
                cleanupPendingPhoto()
            }
        }
    }

    private val recordAudio = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
        val sourceUri = activityResult.data?.data
            ?: streamUriFrom(activityResult.data)
        if (sourceUri == null || activityResult.resultCode != RESULT_OK) {
            return@registerForActivityResult
        }

        lifecycleScope.launch {
            when (val importResult = importUseCase.importCapturedAudio(sourceUri)) {
                CaptureImportResult.MissingSettings -> toast("Settings not configured")
                CaptureImportResult.Failed -> toast("Audio save failed")
                is CaptureImportResult.Saved -> toast("Saved ${importResult.fileName}")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        importUseCase = ImportUseCase(this)

        setContent {
            QuickCaptureTheme {
                MainScreen(
                    orientation = resources.configuration.orientation,
                    onPhotoClick = ::launchCamera,
                    onAudioClick = ::launchAudioRecorder,
                    onSettingsClick = {
                        startActivity(Intent(this, SettingsActivity::class.java))
                    },
                )
            }
        }

        if (savedInstanceState == null) {
            handleShortcutIntent(intent)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleShortcutIntent(intent)
    }

    private fun launchCamera() {
        if (!importUseCase.hasPhotoDestination()) {
            toast("Settings not configured")
            return
        }

        val tempFile = File.createTempFile("capture_", ".jpg", cacheDir)
        val tempUri = FileProvider.getUriForFile(
            this,
            "$packageName.fileprovider",
            tempFile,
        )
        pendingPhotoUri = tempUri
        pendingPhotoFile = tempFile
        takePicture.launch(tempUri)
    }

    private fun launchAudioRecorder() {
        if (!importUseCase.hasAudioDestination()) {
            toast("Settings not configured")
            return
        }

        try {
            recordAudio.launch(Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION))
        } catch (_: ActivityNotFoundException) {
            toast("No recorder app available")
        }
    }

    private fun handleShortcutIntent(intent: Intent) {
        when (intent.action) {
            ACTION_TAKE_PHOTO_SHORTCUT -> launchCamera()
            ACTION_RECORD_AUDIO_SHORTCUT -> launchAudioRecorder()
        }
    }

    private fun cleanupPendingPhoto() {
        pendingPhotoFile?.takeIf(File::exists)?.delete()
        pendingPhotoFile = null
        pendingPhotoUri = null
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    @Suppress("DEPRECATION")
    private fun streamUriFrom(intent: Intent?): Uri? {
        return intent?.getParcelableExtra(Intent.EXTRA_STREAM)
    }

    companion object {
        const val ACTION_TAKE_PHOTO_SHORTCUT = "com.quick.capture.action.TAKE_PHOTO"
        const val ACTION_RECORD_AUDIO_SHORTCUT = "com.quick.capture.action.RECORD_AUDIO"
    }
}

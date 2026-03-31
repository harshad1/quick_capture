package com.claw.logger

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
import com.claw.logger.data.AppPreferences
import com.claw.logger.importing.ImportRepository
import com.claw.logger.storage.SafWriter
import com.claw.logger.ui.ClawLoggerTheme
import com.claw.logger.ui.MainScreen
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : ComponentActivity() {
    private lateinit var preferences: AppPreferences
    private lateinit var importRepository: ImportRepository

    private var pendingPhotoUri: Uri? = null
    private var pendingPhotoFile: File? = null

    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val sourceUri = pendingPhotoUri
        if (!success || sourceUri == null) {
            cleanupPendingPhoto()
            return@registerForActivityResult
        }

        val photoFolder = preferences.photoFolderUri
        if (!SafWriter.persistedFolderExists(this, photoFolder)) {
            toast("Settings not configured")
            cleanupPendingPhoto()
            return@registerForActivityResult
        }

        lifecycleScope.launch {
            runCatching {
                importRepository.importImage(
                    sourceUri = sourceUri,
                    destinationFolder = requireNotNull(photoFolder),
                    scale = preferences.photoScale,
                )
            }.onSuccess { fileName ->
                toast("Saved $fileName")
            }.onFailure {
                toast("Photo save failed")
            }
            cleanupPendingPhoto()
        }
    }

    private val recordAudio = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val sourceUri = result.data?.data
            ?: result.data?.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
        val audioFolder = preferences.audioFolderUri
        if (sourceUri == null || result.resultCode != RESULT_OK) {
            return@registerForActivityResult
        }
        if (!SafWriter.persistedFolderExists(this, audioFolder)) {
            toast("Settings not configured")
            return@registerForActivityResult
        }

        lifecycleScope.launch {
            runCatching {
                importRepository.importAudio(
                    sourceUri = sourceUri,
                    destinationFolder = requireNotNull(audioFolder),
                )
            }.onSuccess { fileName ->
                toast("Saved $fileName")
            }.onFailure {
                toast("Audio save failed")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = AppPreferences(this)
        importRepository = ImportRepository(this)

        setContent {
            ClawLoggerTheme {
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
        val photoFolder = preferences.photoFolderUri
        if (!SafWriter.persistedFolderExists(this, photoFolder)) {
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
        val audioFolder = preferences.audioFolderUri
        if (!SafWriter.persistedFolderExists(this, audioFolder)) {
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

    companion object {
        const val ACTION_TAKE_PHOTO_SHORTCUT = "com.claw.logger.action.TAKE_PHOTO"
        const val ACTION_RECORD_AUDIO_SHORTCUT = "com.claw.logger.action.RECORD_AUDIO"
    }
}

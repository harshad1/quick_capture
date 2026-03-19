package com.claw.logger

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.claw.logger.data.AppPreferences
import com.claw.logger.importing.ImportRepository
import com.claw.logger.storage.SafWriter
import kotlinx.coroutines.launch

class ShareImportActivity : ComponentActivity() {
    private lateinit var preferences: AppPreferences
    private lateinit var importRepository: ImportRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = AppPreferences(this)
        importRepository = ImportRepository(this)

        lifecycleScope.launch {
            val importedCount = runCatching { handleIntent(intent) }.getOrElse {
                toast("Import failed")
                finish()
                return@launch
            }

            when {
                importedCount < 0 -> toast("Settings not configured")
                importedCount == 0 -> toast("No supported files")
                importedCount == 1 -> toast("Imported 1 file")
                else -> toast("Imported $importedCount files")
            }
            finish()
        }
    }

    private suspend fun handleIntent(intent: Intent): Int {
        val uris = extractUris(intent)
        if (uris.isEmpty()) return 0

        val photoFolder = preferences.photoFolderUri
        val audioFolder = preferences.audioFolderUri

        val hasImage = uris.any { mimeTypeFor(it)?.startsWith("image/") == true }
        val hasAudio = uris.any { mimeTypeFor(it)?.startsWith("audio/") == true }

        if ((hasImage && !SafWriter.persistedFolderExists(this, photoFolder)) ||
            (hasAudio && !SafWriter.persistedFolderExists(this, audioFolder))
        ) {
            return -1
        }

        var imported = 0
        for (uri in uris) {
            when {
                mimeTypeFor(uri)?.startsWith("image/") == true -> {
                    importRepository.importImage(
                        sourceUri = uri,
                        destinationFolder = requireNotNull(photoFolder),
                        scale = preferences.photoScale,
                    )
                    imported += 1
                }

                mimeTypeFor(uri)?.startsWith("audio/") == true -> {
                    importRepository.importAudio(
                        sourceUri = uri,
                        destinationFolder = requireNotNull(audioFolder),
                    )
                    imported += 1
                }
            }
        }
        return imported
    }

    private fun extractUris(intent: Intent): List<Uri> {
        return when (intent.action) {
            Intent.ACTION_SEND -> listOfNotNull(intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java))
            Intent.ACTION_SEND_MULTIPLE -> intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM, Uri::class.java).orEmpty()
            else -> emptyList()
        }
    }

    private fun mimeTypeFor(uri: Uri): String? {
        return contentResolver.getType(uri)
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

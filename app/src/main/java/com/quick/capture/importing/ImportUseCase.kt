package com.quick.capture.importing

import android.content.Context
import android.net.Uri
import com.quick.capture.data.AppPreferences
import com.quick.capture.storage.SafWriter

class ImportUseCase(context: Context) {
    private val appContext = context.applicationContext
    private val preferences = AppPreferences(appContext)
    private val repository = ImportRepository(appContext)

    fun hasPhotoDestination(): Boolean {
        return SafWriter.persistedFolderExists(appContext, preferences.photoFolderUri)
    }

    fun hasAudioDestination(): Boolean {
        return SafWriter.persistedFolderExists(appContext, preferences.audioFolderUri)
    }

    suspend fun importCapturedPhoto(sourceUri: Uri): CaptureImportResult {
        val photoFolder = preferences.photoFolderUri
        if (!SafWriter.persistedFolderExists(appContext, photoFolder)) {
            return CaptureImportResult.MissingSettings
        }

        return runCatching {
            repository.importImage(
                sourceUri = sourceUri,
                destinationFolder = requireNotNull(photoFolder),
                scale = preferences.photoScale,
            )
        }.fold(
            onSuccess = CaptureImportResult::Saved,
            onFailure = { CaptureImportResult.Failed },
        )
    }

    suspend fun importCapturedAudio(sourceUri: Uri): CaptureImportResult {
        val audioFolder = preferences.audioFolderUri
        if (!SafWriter.persistedFolderExists(appContext, audioFolder)) {
            return CaptureImportResult.MissingSettings
        }

        return runCatching {
            repository.importAudio(
                sourceUri = sourceUri,
                destinationFolder = requireNotNull(audioFolder),
            )
        }.fold(
            onSuccess = CaptureImportResult::Saved,
            onFailure = { CaptureImportResult.Failed },
        )
    }

    suspend fun importShared(uris: List<Uri>): SharedImportResult {
        val items = uris.mapNotNull { uri ->
            val mimeType = mimeTypeFor(uri)
            when {
                mimeType?.startsWith("image/") == true -> SharedImportItem(uri, SharedImportKind.Image)
                mimeType?.startsWith("audio/") == true -> SharedImportItem(uri, SharedImportKind.Audio)
                else -> null
            }
        }
        if (items.isEmpty()) return SharedImportResult.NoSupportedFiles

        val photoFolder = preferences.photoFolderUri
        val audioFolder = preferences.audioFolderUri
        val needsPhotoFolder = items.any { it.kind == SharedImportKind.Image }
        val needsAudioFolder = items.any { it.kind == SharedImportKind.Audio }
        if ((needsPhotoFolder && !SafWriter.persistedFolderExists(appContext, photoFolder)) ||
            (needsAudioFolder && !SafWriter.persistedFolderExists(appContext, audioFolder))
        ) {
            return SharedImportResult.MissingSettings
        }

        return runCatching {
            var imported = 0
            for (item in items) {
                when (item.kind) {
                    SharedImportKind.Image -> repository.importImage(
                        sourceUri = item.uri,
                        destinationFolder = requireNotNull(photoFolder),
                        scale = preferences.photoScale,
                    )

                    SharedImportKind.Audio -> repository.importAudio(
                        sourceUri = item.uri,
                        destinationFolder = requireNotNull(audioFolder),
                    )
                }
                imported += 1
            }
            imported
        }.fold(
            onSuccess = SharedImportResult::Imported,
            onFailure = { SharedImportResult.Failed },
        )
    }

    private fun mimeTypeFor(uri: Uri): String? {
        return appContext.contentResolver.getType(uri)
    }
}

sealed interface CaptureImportResult {
    data class Saved(val fileName: String) : CaptureImportResult
    data object MissingSettings : CaptureImportResult
    data object Failed : CaptureImportResult
}

sealed interface SharedImportResult {
    data class Imported(val count: Int) : SharedImportResult
    data object MissingSettings : SharedImportResult
    data object NoSupportedFiles : SharedImportResult
    data object Failed : SharedImportResult
}

private data class SharedImportItem(
    val uri: Uri,
    val kind: SharedImportKind,
)

private enum class SharedImportKind {
    Image,
    Audio,
}

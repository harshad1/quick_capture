package com.claw.logger.data

import android.content.Context
import android.net.Uri

class AppPreferences(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var photoScale: PhotoScale
        get() = PhotoScale.fromStorageValue(prefs.getString(KEY_PHOTO_SCALE, PhotoScale.FULL.storageValue))
        set(value) {
            prefs.edit().putString(KEY_PHOTO_SCALE, value.storageValue).apply()
        }

    var photoFolderUri: Uri?
        get() = prefs.getString(KEY_PHOTO_FOLDER_URI, null)?.let(Uri::parse)
        set(value) {
            prefs.edit().putString(KEY_PHOTO_FOLDER_URI, value?.toString()).apply()
        }

    var audioFolderUri: Uri?
        get() = prefs.getString(KEY_AUDIO_FOLDER_URI, null)?.let(Uri::parse)
        set(value) {
            prefs.edit().putString(KEY_AUDIO_FOLDER_URI, value?.toString()).apply()
        }

    companion object {
        private const val PREFS_NAME = "claw_logger_prefs"
        private const val KEY_PHOTO_SCALE = "photo_scale"
        private const val KEY_PHOTO_FOLDER_URI = "photo_folder_uri"
        private const val KEY_AUDIO_FOLDER_URI = "audio_folder_uri"
    }
}

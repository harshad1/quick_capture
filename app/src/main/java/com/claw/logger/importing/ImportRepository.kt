package com.claw.logger.importing

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.claw.logger.data.PhotoScale
import com.claw.logger.storage.SafWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ImportRepository(private val context: Context) {
    suspend fun importImage(sourceUri: Uri, destinationFolder: Uri, scale: PhotoScale): String {
        return withContext(Dispatchers.IO) {
            if (scale == PhotoScale.FULL) {
                return@withContext SafWriter.importBinary(
                    context = context,
                    sourceUri = sourceUri,
                    folderUri = destinationFolder,
                    baseName = SafWriter.buildBaseName("_img"),
                    fallbackMimeType = "image/jpeg",
                )
            }

            val boundsOptions = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            SafWriter.openInputStream(context, sourceUri).use { input ->
                BitmapFactory.decodeStream(input, null, boundsOptions)
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = scale.sampleSize
            }
            val bitmap = SafWriter.openInputStream(context, sourceUri).use { input ->
                BitmapFactory.decodeStream(input, null, decodeOptions)
            } ?: error("Unable to decode image")

            val baseName = SafWriter.buildBaseName("_img")
            val destination = SafWriter.createUniqueFile(
                context = context,
                folderUri = destinationFolder,
                mimeType = "image/jpeg",
                displayName = "$baseName.jpg",
            )

            SafWriter.openOutputStream(context, destination.uri).use { output ->
                check(bitmap.compress(Bitmap.CompressFormat.JPEG, 90, output)) {
                    "Unable to save JPEG"
                }
                output.flush()
            }
            bitmap.recycle()

            destination.name ?: "$baseName.jpg"
        }
    }

    suspend fun importAudio(sourceUri: Uri, destinationFolder: Uri): String {
        return withContext(Dispatchers.IO) {
            SafWriter.importBinary(
                context = context,
                sourceUri = sourceUri,
                folderUri = destinationFolder,
                baseName = SafWriter.buildBaseName("_rec"),
                fallbackMimeType = "audio/*",
            )
        }
    }
}

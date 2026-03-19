package com.claw.logger.storage

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.documentfile.provider.DocumentFile
import java.io.FileNotFoundException
import java.io.InputStream
import java.io.OutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object SafWriter {
    private val timestampFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH-mm-ss")

    fun buildBaseName(postfix: String): String {
        return timestampFormatter.format(LocalDateTime.now()) + postfix
    }

    fun persistedFolderExists(context: Context, treeUri: Uri?): Boolean {
        if (treeUri == null) return false
        return try {
            DocumentFile.fromTreeUri(context, treeUri)?.isDirectory == true
        } catch (_: Exception) {
            false
        }
    }

    fun importBinary(
        context: Context,
        sourceUri: Uri,
        folderUri: Uri,
        baseName: String,
        fallbackMimeType: String,
    ): String {
        val mimeType = context.contentResolver.getType(sourceUri) ?: fallbackMimeType
        val extension = resolveExtension(context, sourceUri, mimeType)
        val destination = createUniqueFile(context, folderUri, mimeType, "$baseName.$extension")
        context.contentResolver.openInputStream(sourceUri).use { input ->
            context.contentResolver.openOutputStream(destination.uri, "w").use { output ->
                copyStreams(input, output)
            }
        }
        return destination.name ?: "$baseName.$extension"
    }

    fun createUniqueFile(
        context: Context,
        folderUri: Uri,
        mimeType: String,
        displayName: String,
    ): DocumentFile {
        val parent = DocumentFile.fromTreeUri(context, folderUri)
            ?: throw FileNotFoundException("Folder not found")
        require(parent.isDirectory) { "Target SAF URI is not a directory" }

        val dotIndex = displayName.lastIndexOf('.')
        val stem = if (dotIndex >= 0) displayName.substring(0, dotIndex) else displayName
        val extension = if (dotIndex >= 0) displayName.substring(dotIndex) else ""

        for (index in 0..999) {
            val candidateName = if (index == 0) displayName else "${stem}_$index$extension"
            if (parent.findFile(candidateName) == null) {
                return parent.createFile(mimeType, candidateName)
                    ?: throw FileNotFoundException("Unable to create destination file")
            }
        }

        throw IllegalStateException("Too many file name collisions for $displayName")
    }

    fun openInputStream(context: Context, uri: Uri): InputStream {
        return context.contentResolver.openInputStream(uri)
            ?: throw FileNotFoundException("Unable to open input stream")
    }

    fun openOutputStream(context: Context, uri: Uri): OutputStream {
        return context.contentResolver.openOutputStream(uri, "w")
            ?: throw FileNotFoundException("Unable to open output stream")
    }

    fun queryDisplayName(context: Context, uri: Uri): String? {
        val projection = arrayOf(OpenableColumns.DISPLAY_NAME)
        context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && cursor.moveToFirst()) {
                return cursor.getString(index)
            }
        }
        return null
    }

    private fun resolveExtension(context: Context, sourceUri: Uri, mimeType: String): String {
        MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)?.let { return it }

        val displayName = queryDisplayName(context, sourceUri)
        val dotIndex = displayName?.lastIndexOf('.') ?: -1
        if (dotIndex > 0 && displayName != null) {
            return displayName.substring(dotIndex + 1)
        }

        return when {
            mimeType.startsWith("audio/") -> "bin"
            mimeType.startsWith("image/") -> "jpg"
            else -> "bin"
        }
    }

    private fun copyStreams(input: InputStream?, output: OutputStream?) {
        requireNotNull(input) { "Missing input stream" }
        requireNotNull(output) { "Missing output stream" }
        input.copyTo(output)
        output.flush()
    }
}

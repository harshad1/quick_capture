package com.quick.capture.storage

import android.net.Uri
import android.provider.DocumentsContract

object SafPathFormatter {
    fun displayPath(uri: Uri?): String {
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
}

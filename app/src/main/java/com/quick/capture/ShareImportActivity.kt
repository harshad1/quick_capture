package com.quick.capture

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.quick.capture.importing.ImportUseCase
import com.quick.capture.importing.SharedImportResult
import kotlinx.coroutines.launch

class ShareImportActivity : ComponentActivity() {
    private lateinit var importUseCase: ImportUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        importUseCase = ImportUseCase(this)

        lifecycleScope.launch {
            when (val result = importUseCase.importShared(extractUris(intent))) {
                SharedImportResult.Failed -> toast("Import failed")
                SharedImportResult.MissingSettings -> toast("Settings not configured")
                SharedImportResult.NoSupportedFiles -> toast("No supported files")
                is SharedImportResult.Imported -> {
                    if (result.count == 1) {
                        toast("Imported 1 file")
                    } else {
                        toast("Imported ${result.count} files")
                    }
                }
            }
            finish()
        }
    }

    private fun extractUris(intent: Intent): List<Uri> {
        return when (intent.action) {
            Intent.ACTION_SEND -> listOfNotNull(streamUriFrom(intent))
            Intent.ACTION_SEND_MULTIPLE -> streamUrisFrom(intent)
            else -> emptyList()
        }
    }

    @Suppress("DEPRECATION")
    private fun streamUriFrom(intent: Intent): Uri? {
        return intent.getParcelableExtra(Intent.EXTRA_STREAM)
    }

    @Suppress("DEPRECATION")
    private fun streamUrisFrom(intent: Intent): List<Uri> {
        return intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM).orEmpty()
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

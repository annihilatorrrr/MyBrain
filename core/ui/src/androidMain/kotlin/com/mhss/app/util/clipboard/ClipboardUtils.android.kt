package com.mhss.app.util.clipboard

import android.content.ClipData
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard

actual suspend fun Clipboard.copyText(label: String, text: String) {
    setClipEntry(
        ClipEntry(ClipData.newPlainText(label, text))
    )
}

actual suspend fun Clipboard.pasteText(): String? {
    return try {
        val clipEntry = getClipEntry()
        if (clipEntry != null && clipEntry.clipData.itemCount > 0) {
            clipEntry.clipData.getItemAt(0)?.text?.toString()
        } else {
            null
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
package com.mhss.app.util.clipboard

import android.content.ClipData
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard

actual suspend fun Clipboard.copyText(label: String, text: String) {
    setClipEntry(
        ClipEntry(ClipData.newPlainText(label, text))
    )
}

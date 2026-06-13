package com.mhss.app.util.clipboard

import androidx.compose.ui.platform.Clipboard

expect suspend fun Clipboard.copyText(label: String, text: String)
expect suspend fun Clipboard.pasteText(): String?
package com.mhss.app.ui

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource

enum class AppFont(val value: Int) {
    DEFAULT(0),
    RUBIK(1),
    MONOSPACE(2),
    SANS_SERIF(3);

    companion object {
        fun fromValue(value: Int): AppFont =
            entries.firstOrNull { it.value == value } ?: DEFAULT
    }
}

fun Int.toAppFont(): AppFont = AppFont.fromValue(this)

fun AppFont.toInt(): Int = value

@Composable
fun AppFont.getName(): String {
    return when (this) {
        AppFont.DEFAULT -> stringResource(Res.string.font_system_default)
        AppFont.RUBIK -> "Rubik"
        AppFont.MONOSPACE -> "Monospace"
        AppFont.SANS_SERIF -> "Sans Serif"
    }
}

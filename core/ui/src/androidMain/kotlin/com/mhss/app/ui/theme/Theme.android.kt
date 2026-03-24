package com.mhss.app.ui.theme

import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
internal actual fun resolvePlatformColorScheme(
    darkTheme: Boolean,
    useDynamicColors: Boolean
): ColorScheme? {
    if (!useDynamicColors || Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        return null
    }

    val context = LocalContext.current
    return if (darkTheme) {
        dynamicDarkColorScheme(context)
    } else {
        dynamicLightColorScheme(context)
    }
}

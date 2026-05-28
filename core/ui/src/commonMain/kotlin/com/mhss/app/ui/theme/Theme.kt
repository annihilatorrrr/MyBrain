package com.mhss.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import com.mhss.app.ui.AppFont

private val DarkColorPalette = darkColorScheme(
    primary = PrimaryColor,
    onPrimary = OnPrimary,
    secondary = SecondaryColor,
    tertiary = TertiaryColor,
    surface = DarkGray,
    background = Color.Black,
    onSurface = Color.White,
    onBackground = Color.White,
    onSurfaceVariant = Color.White,
    surfaceTint = DarkGray,
    surfaceVariant = DarkGray,
    surfaceContainerHighest = DarkGray,
    surfaceContainerLow = DarkGray,
    surfaceContainerLowest = DarkGray,
    surfaceContainer = DarkGray,
    surfaceContainerHigh = DarkGray,
    surfaceDim = DarkGray,
    surfaceBright = DarkGray,
    error = ErrorColor
)

private val LightColorPalette = lightColorScheme(
    primary = PrimaryColor,
    onPrimary = OnPrimary,
    secondary = SecondaryColor,
    tertiary = TertiaryColor,
    background = LightBackgroundColor,
    onBackground = DarkGray,
    onSurfaceVariant = DarkGray,
    surface = LightCardColor,
    surfaceTint = LightCardColor,
    surfaceVariant = LightCardColor,
    surfaceContainerHighest = LightCardColor,
    surfaceContainerLow = LightCardColor,
    surfaceContainerLowest = LightCardColor,
    surfaceContainer = LightCardColor,
    surfaceContainerHigh = LightCardColor,
    surfaceDim = LightCardColor,
    surfaceBright = LightCardColor,
    error = ErrorColor
)

@Composable
fun MyBrainTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    useDynamicColors: Boolean = false,
    fontFamily: FontFamily = AppFont.IBM_PLEX.toFontFamily(),
    fontSizeScale: Float = 1.0f,
    content: @Composable () -> Unit
) {
    val colors = resolvePlatformColorScheme(darkTheme, useDynamicColors)
        ?: if (darkTheme) DarkColorPalette else LightColorPalette
    val typography = getTypography(fontFamily, fontSizeScale)
    MaterialTheme(
        colorScheme = colors,
        typography = typography,
        shapes = Shapes,
        content = content
    )
}

@Composable
internal expect fun resolvePlatformColorScheme(
    darkTheme: Boolean,
    useDynamicColors: Boolean
): ColorScheme?

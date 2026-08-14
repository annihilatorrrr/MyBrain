package com.mhss.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.mhss.app.ui.AppFont
import com.mhss.app.ui.Res
import com.mhss.app.ui.ibm_plex
import com.mhss.app.ui.ibm_plex_bold
import com.mhss.app.ui.rubik_bold
import com.mhss.app.ui.rubik_regular
import org.jetbrains.compose.resources.Font as ResourceFont

@Composable
fun AppFont.toFontFamily(): FontFamily {
    return when (this) {
        AppFont.DEFAULT -> FontFamily.Default
        AppFont.RUBIK -> FontFamily(
            ResourceFont(Res.font.rubik_regular),
            ResourceFont(Res.font.rubik_bold, FontWeight.Bold)
        )
        AppFont.MONOSPACE -> FontFamily.Monospace
        AppFont.SANS_SERIF -> FontFamily.SansSerif
        AppFont.IBM_PLEX -> FontFamily(
            ResourceFont(Res.font.ibm_plex),
            ResourceFont(Res.font.ibm_plex_bold, FontWeight.Bold)
        )
    }
}

fun getTypography(font: FontFamily, fontSizeScale: Float = 1.0f): Typography = Typography(
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = (16 * fontSizeScale).sp,
        fontFamily = font
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = (14 * fontSizeScale).sp,
        fontFamily = font
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = (10 * fontSizeScale).sp,
        fontFamily = font
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.W500,
        fontSize = (14 * fontSizeScale).sp,
        fontFamily = font
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.W500,
        fontSize = (12 * fontSizeScale).sp,
        fontFamily = font
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.W500,
        fontSize = (10 * fontSizeScale).sp,
        fontFamily = font
    ),
    displayLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = (96 * fontSizeScale).sp,
        fontFamily = font
    ),
    displayMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = (60 * fontSizeScale).sp,
        fontFamily = font
    ),
    displaySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = (48 * fontSizeScale).sp,
        fontFamily = font
    ),
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = (32 * fontSizeScale).sp,
        fontFamily = font
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = (28 * fontSizeScale).sp,
        fontFamily = font
    ),
    headlineSmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = (24 * fontSizeScale).sp,
        fontFamily = font
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = (20 * fontSizeScale).sp,
        fontFamily = font
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = (16 * fontSizeScale).sp,
        fontFamily = font
    ),
    titleSmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = (12 * fontSizeScale).sp,
        fontFamily = font
    )
)

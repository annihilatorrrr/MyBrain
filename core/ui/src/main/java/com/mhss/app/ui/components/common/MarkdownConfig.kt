package com.mhss.app.ui.components.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontWeight
import com.mikepenz.markdown.m3.markdownTypography

@Composable
fun defaultMarkdownTypography() = markdownTypography(
    text = MaterialTheme.typography.bodyMedium,
    bullet = MaterialTheme.typography.bodyMedium,
    ordered = MaterialTheme.typography.bodyMedium,
    list = MaterialTheme.typography.bodyMedium,
    code = MaterialTheme.typography.bodyMedium,
    h1 = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
    h2 = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
    h3 = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
    h4 = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
    h5 = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
    h6 = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
    textLink = TextLinkStyles(
        MaterialTheme.typography.bodyMedium.copy(
            fontWeight = FontWeight.Bold,
            color = Color.Blue
        ).toSpanStyle()
    )
)


@Composable
fun previewMarkdownTypography() = markdownTypography(
    text = MaterialTheme.typography.labelMedium,
    bullet = MaterialTheme.typography.labelMedium,
    ordered = MaterialTheme.typography.labelMedium,
    list = MaterialTheme.typography.labelMedium,
    code = MaterialTheme.typography.labelMedium,
    h1 = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
    h2 = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
    h3 = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
    h4 = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
    h5 = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
    h6 = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
    textLink = TextLinkStyles(
        MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.Bold,
            color = Color.Blue
        ).toSpanStyle()
    )
)
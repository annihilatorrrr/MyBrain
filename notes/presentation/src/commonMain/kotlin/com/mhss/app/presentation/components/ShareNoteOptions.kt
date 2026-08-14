package com.mhss.app.presentation.components

import androidx.compose.runtime.Composable

@Composable
expect fun ShareNoteAsPlainTextOption(
    title: String,
    content: String,
    onOptionSelected: () -> Unit,
)

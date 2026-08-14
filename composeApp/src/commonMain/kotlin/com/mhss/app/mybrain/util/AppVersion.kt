package com.mhss.app.mybrain.util

import androidx.compose.runtime.Composable

@Composable
expect fun getAppVersion(): String

expect fun supportsMaterialYouTheme(): Boolean

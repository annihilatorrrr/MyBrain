package com.mhss.app.datetime

import androidx.compose.runtime.staticCompositionLocalOf

val LocalDateTimeFormatter = staticCompositionLocalOf<DateTimeFormatter> {
    error("No DateTimeFormatter provided")
}

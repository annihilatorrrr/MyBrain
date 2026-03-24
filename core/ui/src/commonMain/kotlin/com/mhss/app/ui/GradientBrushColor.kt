package com.mhss.app.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.mhss.app.ui.theme.Blue
import com.mhss.app.ui.theme.DarkOrange
import com.mhss.app.ui.theme.Purple


fun gradientBrushColor(
    vararg colorStops: Pair<Float, Color> = arrayOf(
        0f to Blue,
        0.4f to Purple,
        1f to DarkOrange,
    )
) = Brush.linearGradient(
    colorStops = colorStops,
    start = Offset.Zero,
    end = Offset.Infinite
)
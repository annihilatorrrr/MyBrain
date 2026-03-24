package com.mhss.app.presentation

import androidx.compose.ui.graphics.Color
import com.mhss.app.domain.model.Mood
import com.mhss.app.ui.Res
import com.mhss.app.ui.awesome
import com.mhss.app.ui.bad
import com.mhss.app.ui.good
import com.mhss.app.ui.ic_happy
import com.mhss.app.ui.ic_ok_face
import com.mhss.app.ui.ic_sad
import com.mhss.app.ui.ic_very_happy
import com.mhss.app.ui.ic_very_sad
import com.mhss.app.ui.okay
import com.mhss.app.ui.terrible
import com.mhss.app.ui.theme.Blue
import com.mhss.app.ui.theme.Green
import com.mhss.app.ui.theme.Orange
import com.mhss.app.ui.theme.Purple
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

val Mood.iconRes: DrawableResource
    get() = when (this) {
        Mood.AWESOME -> Res.drawable.ic_very_happy
        Mood.GOOD -> Res.drawable.ic_happy
        Mood.OKAY -> Res.drawable.ic_ok_face
        Mood.BAD -> Res.drawable.ic_sad
        Mood.TERRIBLE -> Res.drawable.ic_very_sad
    }

val Mood.color: Color
    get() = when (this) {
        Mood.AWESOME -> Green
        Mood.GOOD -> Blue
        Mood.OKAY -> Purple
        Mood.BAD -> Orange
        Mood.TERRIBLE -> Color.Red
    }

val Mood.titleRes: StringResource
    get() = when (this) {
        Mood.AWESOME -> Res.string.awesome
        Mood.GOOD -> Res.string.good
        Mood.OKAY -> Res.string.okay
        Mood.BAD -> Res.string.bad
        Mood.TERRIBLE -> Res.string.terrible
    }
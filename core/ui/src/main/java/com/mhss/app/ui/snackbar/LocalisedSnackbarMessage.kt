package com.mhss.app.ui.snackbar

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.mhss.app.ui.R

sealed class LocalisedSnackbarMessage(
    @param:StringRes val stringRes: Int,
    @param:DrawableRes val iconRes: Int,
    val action: (() -> Unit)?,
    val color: @Composable () -> Color,
    override val message: String = "",
    override val actionLabel: String?,
    override val duration: SnackbarDuration = action?.let { SnackbarDuration.Long } ?: SnackbarDuration.Short,
    override val withDismissAction: Boolean = false,
) : SnackbarVisuals {

    class Error(
        @StringRes stringResource: Int,
        actionLabel: String? = null,
        action: (() -> Unit)? = null,
    ) : LocalisedSnackbarMessage(
        stringRes = stringResource,
        iconRes = R.drawable.ic_info,
        actionLabel = actionLabel,
        action = action,
        color = { MaterialTheme.colorScheme.errorContainer }
    )

}
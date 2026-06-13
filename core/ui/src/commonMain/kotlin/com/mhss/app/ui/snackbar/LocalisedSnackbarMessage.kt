package com.mhss.app.ui.snackbar

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.mhss.app.ui.Res
import com.mhss.app.ui.ic_check
import com.mhss.app.ui.ic_info
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

sealed class LocalisedSnackbarMessage(
    val stringRes: StringResource,
    val iconRes: DrawableResource,
    val actionLabelRes: StringResource? = null,
    val color: @Composable () -> Color,
    val contentColor: @Composable () -> Color,
    override val duration: SnackbarDuration = if (actionLabelRes != null) SnackbarDuration.Long else SnackbarDuration.Short,
    override val withDismissAction: Boolean = false,
    val formatArgs: List<Any> = emptyList()
) : SnackbarVisuals {

    override val message: String = ""
    override val actionLabel: String? = null

    class Error(
        stringResource: StringResource,
        actionLabelRes: StringResource? = null,
        formatArgs: List<Any> = emptyList()
    ) : LocalisedSnackbarMessage(
        stringRes = stringResource,
        iconRes = Res.drawable.ic_info,
        actionLabelRes = actionLabelRes,
        color = { MaterialTheme.colorScheme.errorContainer },
        contentColor = { MaterialTheme.colorScheme.onErrorContainer },
        formatArgs = formatArgs
    )

    class Success(
        stringResource: StringResource,
        actionLabelRes: StringResource? = null,
        formatArgs: List<Any> = emptyList()
    ) : LocalisedSnackbarMessage(
        stringRes = stringResource,
        iconRes = Res.drawable.ic_check,
        actionLabelRes = actionLabelRes,
        color = { com.mhss.app.ui.theme.Green },
        contentColor = { Color.White },
        formatArgs = formatArgs
    )

}

suspend fun SnackbarHostState.showSnackbar(
    stringRes: StringResource,
    actionLabelRes: StringResource? = null
): SnackbarResult {
    return showSnackbar(
        LocalisedSnackbarMessage.Error(
            stringResource = stringRes,
            actionLabelRes = actionLabelRes
        )
    )
}

suspend fun SnackbarHostState.showErrorSnackbar(
    stringRes: StringResource,
    actionLabelRes: StringResource? = null,
    formatArgs: List<Any> = emptyList()
): SnackbarResult {
    return showSnackbar(
        LocalisedSnackbarMessage.Error(
            stringResource = stringRes,
            actionLabelRes = actionLabelRes,
            formatArgs = formatArgs
        )
    )
}

suspend fun SnackbarHostState.showSuccessSnackbar(
    stringRes: StringResource,
    actionLabelRes: StringResource? = null,
    formatArgs: List<Any> = emptyList()
): SnackbarResult {
    return showSnackbar(
        LocalisedSnackbarMessage.Success(
            stringResource = stringRes,
            actionLabelRes = actionLabelRes,
            formatArgs = formatArgs
        )
    )
}
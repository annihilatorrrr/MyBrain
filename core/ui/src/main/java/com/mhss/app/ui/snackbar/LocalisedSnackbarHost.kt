package com.mhss.app.ui.snackbar

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun LocalisedSnackbarHost(
    snackbarHostState: SnackbarHostState,
) {
    SnackbarHost(
        hostState = snackbarHostState,
        snackbar = { snackbarData ->
            LocalisedSnackbar(snackbarMessage = snackbarData.visuals as LocalisedSnackbarMessage)
        },
    )
}

@Composable
private fun LocalisedSnackbar(snackbarMessage: LocalisedSnackbarMessage) {
    Snackbar(
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.imePadding(),
        containerColor = snackbarMessage.color(),
        content = {
            SnackbarContent(snackbarMessage)
        }
    )
}

@Composable
private fun SnackbarContent(snackbarMessage: LocalisedSnackbarMessage) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 12.dp),
    ) {
        Icon(
            painter = painterResource(snackbarMessage.iconRes),
            modifier = Modifier.padding(end = 8.dp),
            contentDescription = null,
        )
        Text(
            text = stringResource(snackbarMessage.stringRes),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

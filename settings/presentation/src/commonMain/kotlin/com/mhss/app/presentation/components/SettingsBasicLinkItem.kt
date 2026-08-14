package com.mhss.app.presentation.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun SettingsBasicLinkItem(
    title: StringResource,
    subtitle: String = "",
    icon: DrawableResource,
    link: String = "",
    badge: (@Composable () -> Unit)? = null,
    onClick: () -> Unit = {}
) {
    val uriHandler = LocalUriHandler.current
    SettingsItemCard(
        cornerRadius = 16.dp,
        onClick = {
            if (link.isNotBlank()) {
                uriHandler.openUri(link)
            } else onClick()
        }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(icon),
                contentDescription = stringResource(title),
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = stringResource(title),
                style = MaterialTheme.typography.bodyLarge,
            )
            if (badge != null) {
                Spacer(Modifier.width(8.dp))
                badge()
            }
        }
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

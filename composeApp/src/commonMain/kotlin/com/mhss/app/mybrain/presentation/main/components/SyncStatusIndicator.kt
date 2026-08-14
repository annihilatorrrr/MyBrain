package com.mhss.app.mybrain.presentation.main.components

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mhss.app.mybrain.sync.model.PairedDevice
import com.mhss.app.ui.Res
import com.mhss.app.ui.paired_devices_status
import com.mhss.app.ui.unknown_device
import com.mhss.app.ui.theme.Green
import com.mhss.app.ui.theme.Orange
import com.mhss.app.ui.theme.Red
import org.jetbrains.compose.resources.stringResource

@Composable
fun SyncStatusIndicator(
    pairedDevices: List<PairedDevice>,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalDevices = pairedDevices.size
    val connectedDevicesCount = pairedDevices.count { it.isConnected }

    val indicatorColor = when {
        totalDevices == 0 -> null
        connectedDevicesCount == totalDevices -> Green
        connectedDevicesCount > 0 -> Orange
        else -> Red
    }

    if (indicatorColor == null) return

    var showStatusMenu by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        SyncStatusDot(color = indicatorColor) {
            showStatusMenu = true
        }
        DropdownMenu(
            expanded = showStatusMenu,
            onDismissRequest = { showStatusMenu = false },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.widthIn(max = 250.dp)
        ) {
            Text(
                text = stringResource(Res.string.paired_devices_status),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 16.dp),
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            pairedDevices.forEach { device ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        showStatusMenu = false
                        onNavigateToSettings()
                    }.padding(vertical = 4.dp, horizontal = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = if (device.isConnected) Green else Red,
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = device.deviceName.ifBlank { stringResource(Res.string.unknown_device) },
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun SyncStatusDot(
    color: Color,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        )
    )

    IconButton(onClick = onClick) {
        Box(
            modifier = Modifier.size(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .graphicsLayer(scaleX = scale, scaleY = scale)
                    .size(10.dp)
                    .background(color.copy(alpha = 0.4f), shape = CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(color, shape = CircleShape)
            )
        }
    }
}

package com.mhss.app.ui.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import com.mhss.app.ui.Res
import com.mhss.app.ui.ic_add
import com.mhss.app.ui.theme.DarkGray
import io.github.fletchmckee.liquid.LiquidState
import org.jetbrains.compose.resources.painterResource

@Composable
fun LiquidFloatingActionButton(
    modifier: Modifier = Modifier,
    liquidState: LiquidState,
    iconPainter: Painter,
    contentDescription: String? = null,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        color = DarkGray.copy(0.01f),
        shape = FloatingActionButtonDefaults.shape,
        modifier = modifier
            .dropShadow(FloatingActionButtonDefaults.shape) {
                alpha = 0.05f
                this.spread = 7f
                radius = 36f

            }
            .iconButtonGlass(liquidState, shape = FloatingActionButtonDefaults.shape)
    ) {
        Icon(
            painter = iconPainter,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                alpha = 0.7f
            ),
            modifier = Modifier
                .padding(16.dp)
                .size(24.dp)
        )
    }
}

@Composable
fun AddLiquidFloatingActionButton(
    modifier: Modifier = Modifier,
    liquidState: LiquidState,
    contentDescription: String? = null,
    onClick: () -> Unit,
) {
    LiquidFloatingActionButton(
        modifier = modifier,
        liquidState = liquidState,
        iconPainter = painterResource(Res.drawable.ic_add),
        contentDescription = contentDescription,
        onClick = onClick,
    )
}
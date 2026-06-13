package com.mhss.app.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mhss.app.ui.Res
import com.mhss.app.ui.experimental
import com.mhss.app.ui.preview.BasePreview
import org.jetbrains.compose.resources.stringResource

@Composable
fun ExperimentalBadge(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = Color.Transparent,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.secondary,
        ),
        shape = CircleShape
    ) {
        Text(
            text = stringResource(Res.string.experimental),
            style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
        )
    }
}

@Preview
@Composable
fun ExperimentalBadgePreview() {
    BasePreview {
        ExperimentalBadge()
    }
}

@Preview
@Composable
fun ExperimentalBadgePreviewDark() {
    BasePreview(darkTheme = true) {
        ExperimentalBadge()
    }
}
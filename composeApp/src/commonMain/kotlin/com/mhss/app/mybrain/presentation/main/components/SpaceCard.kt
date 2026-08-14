package com.mhss.app.mybrain.presentation.main.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mhss.app.ui.Res
import com.mhss.app.ui.components.common.singleGradientBackground
import com.mhss.app.ui.notes_img
import com.mhss.app.ui.preview.BasePreview
import com.mhss.app.ui.theme.Blue
import org.jetbrains.compose.resources.DrawableResource
import sv.lib.squircleshape.CornerSmoothing
import sv.lib.squircleshape.SquircleShape

@Composable
fun SpaceCard(
    title: String,
    image: DrawableResource,
    modifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier,
        shape = SquircleShape(
            48.dp,
            CornerSmoothing.Medium
        ),
        elevation = CardDefaults.elevatedCardElevation(
            8.dp
        ),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    ) {
        Column(
            contentModifier
                .clickable { onClick() }

                .aspectRatio(1.0f)
                .padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
            )
            Image(
                modifier = Modifier
                    .size(70.dp)
                    .align(Alignment.End),
                painter = painterResource(image),
                contentDescription = title
            )

        }
    }
}

@Preview
@Composable
fun SpaceCardPreview() {
    BasePreview {
        Box(Modifier.size(175.dp)) {
            SpaceCard(
                title = "Notes",
                image = Res.drawable.notes_img,
                contentModifier = Modifier.singleGradientBackground(
                    gradientColor = Blue,
                    background = MaterialTheme.colorScheme.surfaceVariant,
                    backgroundAlpha = 0.35f,
                )
            )
        }
    }
}

@Preview
@Composable
fun SpaceCardPreviewDark() {
    BasePreview(darkTheme = true) {
        Box(Modifier.size(175.dp)) {
            SpaceCard(
                title = "Notes",
                image = Res.drawable.notes_img,
                contentModifier = Modifier.singleGradientBackground(
                    gradientColor = Blue,
                    background = MaterialTheme.colorScheme.surfaceVariant,
                    backgroundAlpha = 0.35f,
                )
            )
        }
    }
}
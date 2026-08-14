package com.mhss.app.presentation.integrations.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mhss.app.domain.gemininano.GeminiNanoStatus
import com.mhss.app.preferences.domain.model.AiProvider
import com.mhss.app.presentation.integrations.IntegrationsEvent
import com.mhss.app.ui.Res
import com.mhss.app.ui.download_model
import com.mhss.app.ui.downloading_model
import com.mhss.app.ui.gemini_nano_error
import com.mhss.app.ui.gemini_nano_status_check_error
import com.mhss.app.ui.gemini_nano_unsupported
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun GeminiNanoSettingsContent(
    selectedMode: String,
    statusFlow: () -> Flow<GeminiNanoStatus>,
    onEvent: (IntegrationsEvent) -> Unit
) {
    val currentStatus = statusFlow().collectAsStateWithLifecycle(GeminiNanoStatus.Checking)
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)
    ) {
        GeminiNanoModeUi.entries.forEach { modeUi ->
            val isSelected = selectedMode == modeUi.mode.value
            val borderStroke = if (isSelected) {
                BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
            } else {
                BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            }
            val containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
            } else {
                MaterialTheme.colorScheme.surface
            }

            Card(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                onClick = {
                    onEvent(
                        IntegrationsEvent.UpdateModel(
                            AiProvider.GeminiNano,
                            modeUi.mode.value
                        )
                    )
                },
                shape = RoundedCornerShape(16.dp),
                border = borderStroke,
                colors = CardDefaults.cardColors(containerColor = containerColor),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(modeUi.iconRes),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stringResource(modeUi.titleRes),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = stringResource(modeUi.descriptionRes),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
    Spacer(Modifier.height(8.dp))
    when (val status = currentStatus.value) {
        GeminiNanoStatus.Checking -> {
            Text(
                text = "Checking availability",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(Modifier.fillMaxWidth())
        }

        is GeminiNanoStatus.Downloadable -> {
            if (status.isDownloading) {
                val progressPercent = (status.progress * 100).toInt()
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(Res.string.downloading_model),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$progressPercent%",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { status.progress },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    onClick = { onEvent(IntegrationsEvent.DownloadGeminiNanoModel) }
                ) {
                    Text(stringResource(Res.string.download_model))
                }
            }
        }

        is GeminiNanoStatus.Error -> Text(
            text = stringResource(Res.string.gemini_nano_error, status.message),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        is GeminiNanoStatus.Unsupported -> Text(
            text = status.reason?.let {
                stringResource(
                    Res.string.gemini_nano_status_check_error,
                    it
                )
            } ?: stringResource(Res.string.gemini_nano_unsupported),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        GeminiNanoStatus.Ready -> Unit
    }
    Spacer(Modifier.height(8.dp))
}
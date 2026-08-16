package com.mhss.app.mybrain.presentation.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.lifecycle.lifecycleScope
import com.mhss.app.ui.R
import com.mhss.app.ui.theme.MyBrainTheme
import com.mhss.app.widget.WidgetSettings
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

abstract class WidgetConfigurationActivity : AppCompatActivity() {
    protected abstract val widget: GlanceAppWidget
    protected abstract val titleResource: Int

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private lateinit var glanceId: GlanceId

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        appWidgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )
        setResult(
            RESULT_CANCELED,
            Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        )

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        glanceId = GlanceAppWidgetManager(this).getGlanceIdBy(intent) ?: run {
            finish()
            return
        }

        setContent {
            var backgroundOpacity by remember {
                mutableFloatStateOf(WidgetSettings.DEFAULT_BACKGROUND_OPACITY)
            }
            var settingsLoaded by remember { mutableStateOf(false) }

            LaunchedEffect(glanceId) {
                backgroundOpacity = WidgetSettings.getBackgroundOpacity(
                    this@WidgetConfigurationActivity,
                    glanceId
                )
                settingsLoaded = true
            }

            MyBrainTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .statusBarsPadding()
                            .padding(24.dp)
                    ) {
                        Text(
                            text = stringResource(titleResource),
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        Text(
                            text = stringResource(R.string.widget_background_opacity),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "${(backgroundOpacity * 100).roundToInt()}%",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Slider(
                            value = backgroundOpacity,
                            onValueChange = { backgroundOpacity = it },
                            enabled = settingsLoaded,
                            valueRange = 0f..1f
                        )
                        Text(
                            text = stringResource(R.string.widget_background_opacity_description),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(48.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { finish() }) {
                                Text(stringResource(R.string.cancel))
                            }
                            Button(
                                onClick = { save(backgroundOpacity) },
                                enabled = settingsLoaded
                            ) {
                                Text(stringResource(R.string.save))
                            }
                        }
                    }
                }
            }
        }
    }

    private fun save(backgroundOpacity: Float) {
        lifecycleScope.launch {
            WidgetSettings.setBackgroundOpacity(
                this@WidgetConfigurationActivity,
                glanceId,
                backgroundOpacity
            )
            widget.update(this@WidgetConfigurationActivity, glanceId)
            setResult(
                RESULT_OK,
                Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            )
            finish()
        }
    }
}

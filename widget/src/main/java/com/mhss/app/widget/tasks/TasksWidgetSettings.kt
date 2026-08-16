package com.mhss.app.widget.tasks

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition

object TasksWidgetSettings {
    const val DEFAULT_BACKGROUND_OPACITY = 1f

    private val backgroundOpacityKey = floatPreferencesKey("background_opacity")

    fun backgroundOpacity(preferences: Preferences): Float =
        preferences[backgroundOpacityKey] ?: DEFAULT_BACKGROUND_OPACITY

    suspend fun getBackgroundOpacity(context: Context, glanceId: GlanceId): Float =
        backgroundOpacity(
            getAppWidgetState(
                context,
                PreferencesGlanceStateDefinition,
                glanceId
            )
        )

    suspend fun setBackgroundOpacity(
        context: Context,
        glanceId: GlanceId,
        opacity: Float
    ) {
        updateAppWidgetState(context, glanceId) {
            it[backgroundOpacityKey] = opacity.coerceIn(0f, 1f)
        }
    }
}

package com.mhss.app.preferences.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.mhss.app.preferences.PrefsConstants
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = PrefsConstants.SETTINGS_PREFERENCES)

@Module
actual class PreferencesPlatformModule {

    @Single
    fun dataStore(context: Context): DataStore<Preferences> = context.dataStore
}

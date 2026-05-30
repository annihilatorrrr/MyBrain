package com.mhss.app.mybrain.di

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.mhss.app.preferences.PrefsConstants
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.logging.Logger
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single


val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = PrefsConstants.SETTINGS_PREFERENCES)

@Module
class PlatformModule {
    
    @Single
    fun dataStore(context: Context): DataStore<Preferences> = context.dataStore
    
    @Single
    fun httpClientEngine(): HttpClientEngine = OkHttp.create()
    
    @Single
    fun httpLogger(): Logger = AndroidHttpLogger()
}

class AndroidHttpLogger: Logger {
    override fun log(message: String) {
        Log.i("Ktor", message)
    }
}
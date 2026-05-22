package com.mhss.app.mybrain

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appfunctions.service.AppFunctionConfiguration
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.mhss.app.alarm.di.AlarmModule
import com.mhss.app.data.NoteDataModule
import com.mhss.app.data.di.AiDataModule
import com.mhss.app.data.di.BookmarksDataModule
import com.mhss.app.data.di.CalendarDataModule
import com.mhss.app.data.di.DiaryDataModule
import com.mhss.app.data.di.SettingsDataModule
import com.mhss.app.data.di.TasksDataModule
import com.mhss.app.data.noteMarkdownModule
import com.mhss.app.data.noteRoomModule
import com.mhss.app.database.di.databaseModule
import com.mhss.app.datetime.DateTimeModule
import com.mhss.app.di.coroutinesModule
import com.mhss.app.domain.di.AiDomainModule
import com.mhss.app.domain.di.BookmarksDomainModule
import com.mhss.app.domain.di.CalendarDomainModule
import com.mhss.app.domain.di.DiaryDomainModule
import com.mhss.app.domain.di.NoteDomainModule
import com.mhss.app.domain.di.SettingsDomainModule
import com.mhss.app.domain.di.TasksDomainModule
import com.mhss.app.mybrain.appfunctions.MyBrainAppFunctions
import com.mhss.app.mybrain.appfunctions.appFunctionsModule
import com.mhss.app.mybrain.di.MainPresentationModule
import com.mhss.app.mybrain.di.platformModule
import com.mhss.app.mybrain.notification.NotificationConstants
import com.mhss.app.preferences.PrefsConstants
import com.mhss.app.preferences.di.PreferencesModule
import com.mhss.app.preferences.domain.model.booleanPreferencesKey
import com.mhss.app.preferences.domain.model.stringPreferencesKey
import com.mhss.app.preferences.domain.use_case.GetPreferenceUseCase
import com.mhss.app.presentation.di.AiPresentationModule
import com.mhss.app.presentation.di.BookmarksPresentationModule
import com.mhss.app.presentation.di.CalendarPresentationModule
import com.mhss.app.presentation.di.DiaryPresentationModule
import com.mhss.app.presentation.di.NotePresentationModule
import com.mhss.app.presentation.di.SettingsPresentationModule
import com.mhss.app.presentation.di.TasksPresentationModule
import com.mhss.app.storage.di.StorageModule
import com.mhss.app.ui.R
import com.mhss.app.widget.di.WidgetModule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.GlobalContext
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.ksp.generated.module
import kotlin.system.exitProcess

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = PrefsConstants.SETTINGS_PREFERENCES)

class MyBrainApplication : Application(), AppFunctionConfiguration.Provider {

    override val appFunctionConfiguration: AppFunctionConfiguration by lazy {
        AppFunctionConfiguration.Builder()
            .addEnclosingClassFactory(MyBrainAppFunctions::class.java) { GlobalContext.get().get<MyBrainAppFunctions>() }
            .build()
    }

    private val getPreference: GetPreferenceUseCase by inject()

    override fun onCreate() {
        super.onCreate()
        startKoin {
            allowOverride(true)
            androidContext(this@MyBrainApplication)
            androidLogger()
            modules(
                appFunctionsModule,
                platformModule,
                DateTimeModule().module,
                MainPresentationModule().module,
                AlarmModule().module,
                databaseModule,
                coroutinesModule,
                PreferencesModule().module,
                StorageModule().module,
                NotePresentationModule().module,
                NoteDataModule().module,
                NoteDomainModule().module,
                DiaryPresentationModule().module,
                DiaryDataModule().module,
                DiaryDomainModule().module,
                TasksPresentationModule().module,
                TasksDataModule().module,
                TasksDomainModule().module,
                SettingsPresentationModule().module,
                SettingsDataModule().module,
                SettingsDomainModule().module,
                CalendarPresentationModule().module,
                CalendarDataModule().module,
                CalendarDomainModule().module,
                BookmarksPresentationModule().module,
                BookmarksDataModule().module,
                BookmarksDomainModule().module,
                WidgetModule().module,
                AiDataModule().module,
                AiDomainModule().module,
                AiPresentationModule().module
            )
            workManagerFactory()
        }
        loadNotesModule()

        createRemindersNotificationChannel()
        Thread.setDefaultUncaughtExceptionHandler { _, e ->
            e.printStackTrace()
            "```\n${e.stackTraceToString()}\n```".copyToClipboard()
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(this, getString(R.string.exception_stack_trace_copied), Toast.LENGTH_LONG).show()
            }
            exitProcess(1)
        }
    }

    private fun loadNotesModule() = runBlocking {
        val isExternalNotesEnabled = getPreference(
            booleanPreferencesKey(PrefsConstants.EXTERNAL_NOTES_ENABLED),
            false
        ).first()
        val rootUri = getPreference(
            stringPreferencesKey(PrefsConstants.EXTERNAL_NOTES_FOLDER_URI),
            ""
        ).first()

        if (isExternalNotesEnabled && rootUri.isNotBlank()) {
            loadKoinModules(noteMarkdownModule(rootUri))
        } else {
            loadKoinModules(noteRoomModule)
        }
    }

    private fun createRemindersNotificationChannel() {
        val channel = NotificationChannel(
            NotificationConstants.REMINDERS_CHANNEL_ID,
            getString(R.string.reminders_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.description = getString(R.string.reminders_channel_description)
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

    }

    private fun String.copyToClipboard() {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("label", this)
        clipboard.setPrimaryClip(clip)
    }
}

package com.mhss.app.database.di

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.mhss.app.database.MyBrainDatabase
import com.mhss.app.database.helpers.DatabaseTransactionProvider
import com.mhss.app.database.helpers.RoomDatabaseTransactionProvider
import com.mhss.app.database.migrations.MIGRATION_1_2
import com.mhss.app.database.migrations.MIGRATION_2_3
import com.mhss.app.database.migrations.MIGRATION_3_4
import com.mhss.app.database.migrations.MIGRATION_4_5
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {

    single {
        val appContext = androidContext()
        val dbFile = appContext.getDatabasePath(MyBrainDatabase.DATABASE_NAME)
        Room.databaseBuilder<MyBrainDatabase>(
            context = appContext,
            name = dbFile.absolutePath
        )
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }

    single { get<MyBrainDatabase>().noteDao() }
    single { get<MyBrainDatabase>().taskDao() }
    single { get<MyBrainDatabase>().diaryDao() }
    single { get<MyBrainDatabase>().bookmarkDao() }
    single { get<MyBrainDatabase>().alarmDao() }

    single<DatabaseTransactionProvider> { RoomDatabaseTransactionProvider(get()) }

}

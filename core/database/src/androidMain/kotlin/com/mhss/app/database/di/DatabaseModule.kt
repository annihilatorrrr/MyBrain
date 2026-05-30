package com.mhss.app.database.di

import android.content.Context
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.mhss.app.database.MyBrainDatabase
import com.mhss.app.database.helpers.DatabaseTransactionProvider
import com.mhss.app.database.helpers.RoomDatabaseTransactionProvider
import com.mhss.app.database.migrations.MIGRATION_1_2
import com.mhss.app.database.migrations.MIGRATION_2_3
import com.mhss.app.database.migrations.MIGRATION_3_4
import com.mhss.app.database.migrations.MIGRATION_4_5
import com.mhss.app.database.migrations.MIGRATION_5_6
import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class DatabaseModule {

    @Single
    fun myBrainDatabase(context: Context): MyBrainDatabase {
        val dbFile = context.getDatabasePath(MyBrainDatabase.DATABASE_NAME)
        return Room.databaseBuilder<MyBrainDatabase>(
            context = context,
            name = dbFile.absolutePath
        )
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }

    @Single
    fun noteDao(database: MyBrainDatabase) = database.noteDao()

    @Single
    fun taskDao(database: MyBrainDatabase) = database.taskDao()

    @Single
    fun diaryDao(database: MyBrainDatabase) = database.diaryDao()

    @Single
    fun bookmarkDao(database: MyBrainDatabase) = database.bookmarkDao()

    @Single
    fun alarmDao(database: MyBrainDatabase) = database.alarmDao()

    @Single
    fun assistantDao(database: MyBrainDatabase) = database.assistantDao()

    @Single
    fun databaseTransactionProvider(database: MyBrainDatabase): DatabaseTransactionProvider =
        RoomDatabaseTransactionProvider(database)
}

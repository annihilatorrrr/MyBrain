package com.mhss.app.database

import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor
import androidx.room3.TypeConverters
import com.mhss.app.database.converters.DBConverters
import com.mhss.app.database.dao.AlarmDao
import com.mhss.app.database.dao.AssistantDao
import com.mhss.app.database.dao.BookmarkDao
import com.mhss.app.database.dao.DiaryDao
import com.mhss.app.database.dao.NoteDao
import com.mhss.app.database.dao.TaskDao
import com.mhss.app.database.entity.AlarmEntity
import com.mhss.app.database.entity.AssistantMessageEntity
import com.mhss.app.database.entity.AssistantThreadEntity
import com.mhss.app.database.entity.BookmarkEntity
import com.mhss.app.database.entity.DiaryEntryEntity
import com.mhss.app.database.entity.NoteEntity
import com.mhss.app.database.entity.NoteFolderEntity
import com.mhss.app.database.entity.TaskEntity

@Database(
    entities = [
        NoteEntity::class,
        TaskEntity::class,
        DiaryEntryEntity::class,
        BookmarkEntity::class,
        AlarmEntity::class,
        NoteFolderEntity::class,
        AssistantThreadEntity::class,
        AssistantMessageEntity::class
    ],
    version = 6
)
@TypeConverters(DBConverters::class)
@ConstructedBy(MyBrainDatabaseConstructor::class)
abstract class MyBrainDatabase: RoomDatabase() {

    abstract fun noteDao(): NoteDao
    abstract fun taskDao(): TaskDao
    abstract fun diaryDao(): DiaryDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun alarmDao(): AlarmDao
    abstract fun assistantDao(): AssistantDao

    companion object {
        const val DATABASE_NAME = "by_brain_db"
    }
}

@Suppress("KotlinNoActualForExpect")
expect object MyBrainDatabaseConstructor : RoomDatabaseConstructor<MyBrainDatabase>

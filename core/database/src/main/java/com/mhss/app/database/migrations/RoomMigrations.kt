@file:OptIn(ExperimentalUuidApi::class)
package com.mhss.app.database.migrations

import androidx.core.database.getIntOrNull
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mhss.app.alarm.model.Alarm
import com.mhss.app.alarm.repository.AlarmScheduler
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

// Added note folders
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE note_folders (name TEXT NOT NULL, id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL)")

        db.execSQL("CREATE TABLE IF NOT EXISTS `notes_new` (`title` TEXT NOT NULL, `content` TEXT NOT NULL, `created_date` INTEGER NOT NULL, `updated_date` INTEGER NOT NULL, `pinned` INTEGER NOT NULL, `folder_id` INTEGER, `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, FOREIGN KEY (folder_id) REFERENCES note_folders (id) ON UPDATE NO ACTION ON DELETE CASCADE)")
        db.execSQL("INSERT INTO notes_new (title, content, created_date, updated_date, pinned, id) SELECT title, content, created_date, updated_date, pinned, id FROM notes")
        db.execSQL("DROP TABLE notes")
        db.execSQL("ALTER TABLE notes_new RENAME TO notes")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE tasks ADD COLUMN recurring INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE tasks ADD COLUMN frequency INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE tasks ADD COLUMN frequency_amount INTEGER NOT NULL DEFAULT 1")
    }
}

// Migrating from using auto-incrementing integer IDs to UUIDs
class Migration4to5(private val alarmScheduler: AlarmScheduler) : Migration(4, 5) {

    override fun migrate(db: SupportSQLiteDatabase) {
        // Create a mapping of old folder IDs to new UUIDs
        val folderIdMapping = HashMap<Int, String>()

        // Migrate note_folders table
        db.execSQL("CREATE TABLE note_folders_new (name TEXT NOT NULL, id TEXT PRIMARY KEY NOT NULL)")

        val folderCursor = db.query("SELECT * FROM note_folders")
        while (folderCursor.moveToNext()) {
            val oldId = folderCursor.getInt(folderCursor.getColumnIndexOrThrow("id"))
            val name = folderCursor.getString(folderCursor.getColumnIndexOrThrow("name"))
            val newId = Uuid.random().toString()

            folderIdMapping[oldId] = newId

            db.execSQL(
                "INSERT INTO note_folders_new (id, name) VALUES (?, ?)",
                arrayOf(newId, name)
            )
        }
        folderCursor.close()

        db.execSQL("DROP TABLE note_folders")
        db.execSQL("ALTER TABLE note_folders_new RENAME TO note_folders")

        // Migrate notes table
        db.execSQL("CREATE TABLE notes_new (title TEXT NOT NULL, content TEXT NOT NULL, created_date INTEGER NOT NULL, updated_date INTEGER NOT NULL, pinned INTEGER NOT NULL, folder_id TEXT, id TEXT PRIMARY KEY NOT NULL, FOREIGN KEY (folder_id) REFERENCES note_folders (id) ON UPDATE NO ACTION ON DELETE CASCADE)")

        val notesCursor = db.query("SELECT * FROM notes")
        while (notesCursor.moveToNext()) {
            val title = notesCursor.getString(notesCursor.getColumnIndexOrThrow("title"))
            val content = notesCursor.getString(notesCursor.getColumnIndexOrThrow("content"))
            val createdDate = notesCursor.getLong(notesCursor.getColumnIndexOrThrow("created_date"))
            val updatedDate = notesCursor.getLong(notesCursor.getColumnIndexOrThrow("updated_date"))
            val pinned = notesCursor.getInt(notesCursor.getColumnIndexOrThrow("pinned"))

            val oldFolderId =
                notesCursor.getIntOrNull(notesCursor.getColumnIndexOrThrow("folder_id"))
            val newFolderId = oldFolderId?.let { folderIdMapping[it] }

            val newId = Uuid.random().toString()

            db.execSQL(
                "INSERT INTO notes_new (id, title, content, created_date, updated_date, pinned, folder_id) VALUES (?, ?, ?, ?, ?, ?, ?)",
                arrayOf(newId, title, content, createdDate, updatedDate, pinned, newFolderId)
            )
        }
        notesCursor.close()

        db.execSQL("DROP TABLE notes")
        db.execSQL("ALTER TABLE notes_new RENAME TO notes")

        // Migrate bookmarks table
        db.execSQL("CREATE TABLE bookmarks_new (url TEXT NOT NULL, title TEXT NOT NULL, description TEXT NOT NULL, created_date INTEGER NOT NULL, updated_date INTEGER NOT NULL, id TEXT PRIMARY KEY NOT NULL)")

        val bookmarksCursor = db.query("SELECT * FROM bookmarks")
        while (bookmarksCursor.moveToNext()) {
            val url = bookmarksCursor.getString(bookmarksCursor.getColumnIndexOrThrow("url"))
            val title = bookmarksCursor.getString(bookmarksCursor.getColumnIndexOrThrow("title"))
            val description =
                bookmarksCursor.getString(bookmarksCursor.getColumnIndexOrThrow("description"))
            val createdDate =
                bookmarksCursor.getLong(bookmarksCursor.getColumnIndexOrThrow("created_date"))
            val updatedDate =
                bookmarksCursor.getLong(bookmarksCursor.getColumnIndexOrThrow("updated_date"))
            val newId = Uuid.random().toString()

            db.execSQL(
                "INSERT INTO bookmarks_new (id, url, title, description, created_date, updated_date) VALUES (?, ?, ?, ?, ?, ?)",
                arrayOf(newId, url, title, description, createdDate, updatedDate)
            )
        }
        bookmarksCursor.close()

        db.execSQL("DROP TABLE bookmarks")
        db.execSQL("ALTER TABLE bookmarks_new RENAME TO bookmarks")

        // Migrate alarms table first and collect alarm ID mapping
        val alarmIdMapping = HashMap<Int, Int>()
        db.execSQL("CREATE TABLE alarms_new (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, time INTEGER NOT NULL)")

        val alarmsCursor = db.query("SELECT * FROM alarms")
        while (alarmsCursor.moveToNext()) {
            val oldAlarmId = alarmsCursor.getInt(alarmsCursor.getColumnIndexOrThrow("id"))
            val time = alarmsCursor.getLong(alarmsCursor.getColumnIndexOrThrow("time"))

            // Insert without specifying ID to get auto-generated ID
            db.execSQL(
                "INSERT INTO alarms_new (time) VALUES (?)",
                arrayOf(time)
            )

            // Get the newly generated ID
            val newIdCursor = db.query("SELECT id FROM alarms_new ORDER BY id DESC LIMIT 1")
            newIdCursor.moveToFirst()
            val newAlarmId = newIdCursor.getInt(0)
            newIdCursor.close()

            // Map old ID to new ID
            alarmIdMapping[oldAlarmId] = newAlarmId
            // Schedule the alarm using the new ID
            runCatching { alarmScheduler.scheduleAlarm(Alarm(newAlarmId, time)) }
        }
        alarmsCursor.close()

        db.execSQL("DROP TABLE alarms")
        db.execSQL("ALTER TABLE alarms_new RENAME TO alarms")

        // Migrate tasks table
        db.execSQL("CREATE TABLE tasks_new (title TEXT NOT NULL, description TEXT NOT NULL, is_completed INTEGER NOT NULL, priority INTEGER NOT NULL, created_date INTEGER NOT NULL, updated_date INTEGER NOT NULL, sub_tasks TEXT NOT NULL, dueDate INTEGER NOT NULL, recurring INTEGER NOT NULL, frequency INTEGER NOT NULL, frequency_amount INTEGER NOT NULL, alarmId INTEGER, id TEXT PRIMARY KEY NOT NULL)")

        val tasksCursor = db.query("SELECT * FROM tasks")
        while (tasksCursor.moveToNext()) {
            val oldTaskId = tasksCursor.getInt(tasksCursor.getColumnIndexOrThrow("id"))
            val title = tasksCursor.getString(tasksCursor.getColumnIndexOrThrow("title"))
            val description =
                tasksCursor.getString(tasksCursor.getColumnIndexOrThrow("description"))
            val isCompleted = tasksCursor.getInt(tasksCursor.getColumnIndexOrThrow("is_completed"))
            val priority = tasksCursor.getInt(tasksCursor.getColumnIndexOrThrow("priority"))
            val createdDate = tasksCursor.getLong(tasksCursor.getColumnIndexOrThrow("created_date"))
            val updatedDate = tasksCursor.getLong(tasksCursor.getColumnIndexOrThrow("updated_date"))
            val subTasks = tasksCursor.getString(tasksCursor.getColumnIndexOrThrow("sub_tasks"))
            val dueDate = tasksCursor.getLong(tasksCursor.getColumnIndexOrThrow("dueDate"))
            val recurring = tasksCursor.getInt(tasksCursor.getColumnIndexOrThrow("recurring"))
            val frequency = tasksCursor.getInt(tasksCursor.getColumnIndexOrThrow("frequency"))
            val frequencyAmount =
                tasksCursor.getInt(tasksCursor.getColumnIndexOrThrow("frequency_amount"))

            // old version was using the task id as the alarm id
            val alarmId = alarmIdMapping[oldTaskId]
            val newId = Uuid.random().toString()

            db.execSQL(
                "INSERT INTO tasks_new (id, title, description, is_completed, priority, created_date, updated_date, sub_tasks, dueDate, recurring, frequency, frequency_amount, alarmId) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                arrayOf(
                    newId,
                    title,
                    description,
                    isCompleted,
                    priority,
                    createdDate,
                    updatedDate,
                    subTasks,
                    dueDate,
                    recurring,
                    frequency,
                    frequencyAmount,
                    alarmId
                )
            )
        }
        tasksCursor.close()

        db.execSQL("DROP TABLE tasks")
        db.execSQL("ALTER TABLE tasks_new RENAME TO tasks")

        // Migrate diary table
        db.execSQL("CREATE TABLE diary_new (title TEXT NOT NULL, content TEXT NOT NULL, created_date INTEGER NOT NULL, updated_date INTEGER NOT NULL, mood INTEGER NOT NULL, id TEXT PRIMARY KEY NOT NULL)")

        val diaryCursor = db.query("SELECT * FROM diary")
        while (diaryCursor.moveToNext()) {
            val title = diaryCursor.getString(diaryCursor.getColumnIndexOrThrow("title"))
            val content = diaryCursor.getString(diaryCursor.getColumnIndexOrThrow("content"))
            val createdDate = diaryCursor.getLong(diaryCursor.getColumnIndexOrThrow("created_date"))
            val updatedDate = diaryCursor.getLong(diaryCursor.getColumnIndexOrThrow("updated_date"))
            val mood = diaryCursor.getInt(diaryCursor.getColumnIndexOrThrow("mood"))
            val newId = Uuid.random().toString()

            db.execSQL(
                "INSERT INTO diary_new (id, title, content, created_date, updated_date, mood) VALUES (?, ?, ?, ?, ?, ?)",
                arrayOf(newId, title, content, createdDate, updatedDate, mood)
            )
        }
        diaryCursor.close()

        db.execSQL("DROP TABLE diary")
        db.execSQL("ALTER TABLE diary_new RENAME TO diary")
    }
}

@file:OptIn(ExperimentalUuidApi::class)
package com.mhss.app.database.migrations

import androidx.room3.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

val MIGRATION_1_2 = object : Migration(1, 2) {
    override suspend fun migrate(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE note_folders (name TEXT NOT NULL, id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL)")

        connection.execSQL("CREATE TABLE IF NOT EXISTS `notes_new` (`title` TEXT NOT NULL, `content` TEXT NOT NULL, `created_date` INTEGER NOT NULL, `updated_date` INTEGER NOT NULL, `pinned` INTEGER NOT NULL, `folder_id` INTEGER, `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, FOREIGN KEY (folder_id) REFERENCES note_folders (id) ON UPDATE NO ACTION ON DELETE CASCADE)")
        connection.execSQL("INSERT INTO notes_new (title, content, created_date, updated_date, pinned, id) SELECT title, content, created_date, updated_date, pinned, id FROM notes")
        connection.execSQL("DROP TABLE notes")
        connection.execSQL("ALTER TABLE notes_new RENAME TO notes")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override suspend fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE tasks ADD COLUMN recurring INTEGER NOT NULL DEFAULT 0")
        connection.execSQL("ALTER TABLE tasks ADD COLUMN frequency INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override suspend fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE tasks ADD COLUMN frequency_amount INTEGER NOT NULL DEFAULT 1")
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {

    override suspend fun migrate(connection: SQLiteConnection) {
        val folderIdMapping = HashMap<Int, String>()

        connection.execSQL("CREATE TABLE note_folders_new (name TEXT NOT NULL, id TEXT PRIMARY KEY NOT NULL)")
        connection.execSQL("CREATE TABLE notes_new (title TEXT NOT NULL, content TEXT NOT NULL, created_date INTEGER NOT NULL, updated_date INTEGER NOT NULL, pinned INTEGER NOT NULL, folder_id TEXT, id TEXT PRIMARY KEY NOT NULL)")

        val folders = mutableListOf<Pair<Int, String>>()
        connection.prepare("SELECT id, name FROM note_folders").use { stmt ->
            while (stmt.step()) {
                folders.add(stmt.getInt(0) to stmt.getText(1))
            }
        }
        for ((oldId, name) in folders) {
            val newId = Uuid.generateV7().toString()
            folderIdMapping[oldId] = newId
            connection.prepare("INSERT INTO note_folders_new (id, name) VALUES (?, ?)").use { stmt ->
                stmt.bindText(1, newId)
                stmt.bindText(2, name)
                stmt.step()
            }
        }

        data class OldNote(
            val title: String, val content: String, val createdDate: Long,
            val updatedDate: Long, val pinned: Int, val folderId: Int?, val id: Int
        )
        val notes = mutableListOf<OldNote>()
        connection.prepare("SELECT id, title, content, created_date, updated_date, pinned, folder_id FROM notes").use { stmt ->
            while (stmt.step()) {
                notes.add(
                    OldNote(
                        title = stmt.getText(1),
                        content = stmt.getText(2),
                        createdDate = stmt.getLong(3),
                        updatedDate = stmt.getLong(4),
                        pinned = stmt.getInt(5),
                        folderId = if (stmt.isNull(6)) null else stmt.getInt(6),
                        id = stmt.getInt(0)
                    )
                )
            }
        }
        for (note in notes) {
            val newFolderId = note.folderId?.let { folderIdMapping[it] }
            val newId = Uuid.generateV7().toString()
            connection.prepare("INSERT INTO notes_new (id, title, content, created_date, updated_date, pinned, folder_id) VALUES (?, ?, ?, ?, ?, ?, ?)").use { stmt ->
                stmt.bindText(1, newId)
                stmt.bindText(2, note.title)
                stmt.bindText(3, note.content)
                stmt.bindLong(4, note.createdDate)
                stmt.bindLong(5, note.updatedDate)
                stmt.bindLong(6, note.pinned.toLong())
                if (newFolderId != null) stmt.bindText(7, newFolderId) else stmt.bindNull(7)
                stmt.step()
            }
        }

        connection.execSQL("DROP TABLE notes")
        connection.execSQL("DROP TABLE note_folders")
        connection.execSQL("ALTER TABLE note_folders_new RENAME TO note_folders")
        connection.execSQL("ALTER TABLE notes_new RENAME TO notes")

        connection.execSQL("CREATE TABLE bookmarks_new (url TEXT NOT NULL, title TEXT NOT NULL, description TEXT NOT NULL, created_date INTEGER NOT NULL, updated_date INTEGER NOT NULL, id TEXT PRIMARY KEY NOT NULL)")

        data class OldBookmark(
            val url: String, val title: String, val description: String,
            val createdDate: Long, val updatedDate: Long
        )
        val bookmarks = mutableListOf<OldBookmark>()
        connection.prepare("SELECT url, title, description, created_date, updated_date FROM bookmarks").use { stmt ->
            while (stmt.step()) {
                bookmarks.add(
                    OldBookmark(
                        url = stmt.getText(0),
                        title = stmt.getText(1),
                        description = stmt.getText(2),
                        createdDate = stmt.getLong(3),
                        updatedDate = stmt.getLong(4)
                    )
                )
            }
        }
        for (bookmark in bookmarks) {
            val newId = Uuid.generateV7().toString()
            connection.prepare("INSERT INTO bookmarks_new (id, url, title, description, created_date, updated_date) VALUES (?, ?, ?, ?, ?, ?)").use { stmt ->
                stmt.bindText(1, newId)
                stmt.bindText(2, bookmark.url)
                stmt.bindText(3, bookmark.title)
                stmt.bindText(4, bookmark.description)
                stmt.bindLong(5, bookmark.createdDate)
                stmt.bindLong(6, bookmark.updatedDate)
                stmt.step()
            }
        }

        connection.execSQL("DROP TABLE bookmarks")
        connection.execSQL("ALTER TABLE bookmarks_new RENAME TO bookmarks")

        val alarmIdSet = HashSet<Int>()
        connection.execSQL("CREATE TABLE alarms_new (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, time INTEGER NOT NULL)")

        data class OldAlarm(val id: Int, val time: Long)
        val alarms = mutableListOf<OldAlarm>()
        connection.prepare("SELECT id, time FROM alarms").use { stmt ->
            while (stmt.step()) {
                alarms.add(OldAlarm(stmt.getInt(0), stmt.getLong(1)))
            }
        }
        for (alarm in alarms) {
            connection.prepare("INSERT INTO alarms_new (id, time) VALUES (?, ?)").use { stmt ->
                stmt.bindLong(1, alarm.id.toLong())
                stmt.bindLong(2, alarm.time)
                stmt.step()
            }
            alarmIdSet.add(alarm.id)
        }

        connection.execSQL("DROP TABLE alarms")
        connection.execSQL("ALTER TABLE alarms_new RENAME TO alarms")

        connection.execSQL("CREATE TABLE tasks_new (title TEXT NOT NULL, description TEXT NOT NULL, is_completed INTEGER NOT NULL, priority INTEGER NOT NULL, created_date INTEGER NOT NULL, updated_date INTEGER NOT NULL, sub_tasks TEXT NOT NULL, dueDate INTEGER NOT NULL, recurring INTEGER NOT NULL, frequency INTEGER NOT NULL, frequency_amount INTEGER NOT NULL, alarmId INTEGER, id TEXT PRIMARY KEY NOT NULL)")

        data class OldTask(
            val id: Int, val title: String, val description: String,
            val isCompleted: Int, val priority: Int, val createdDate: Long,
            val updatedDate: Long, val subTasks: String, val dueDate: Long,
            val recurring: Int, val frequency: Int, val frequencyAmount: Int
        )
        val tasks = mutableListOf<OldTask>()
        connection.prepare("SELECT id, title, description, is_completed, priority, created_date, updated_date, sub_tasks, dueDate, recurring, frequency, frequency_amount FROM tasks").use { stmt ->
            while (stmt.step()) {
                tasks.add(
                    OldTask(
                        id = stmt.getInt(0),
                        title = stmt.getText(1),
                        description = stmt.getText(2),
                        isCompleted = stmt.getInt(3),
                        priority = stmt.getInt(4),
                        createdDate = stmt.getLong(5),
                        updatedDate = stmt.getLong(6),
                        subTasks = stmt.getText(7),
                        dueDate = stmt.getLong(8),
                        recurring = stmt.getInt(9),
                        frequency = stmt.getInt(10),
                        frequencyAmount = stmt.getInt(11)
                    )
                )
            }
        }
        for (task in tasks) {
            val alarmId = if (task.id in alarmIdSet) task.id else null
            val newId = Uuid.generateV7().toString()
            connection.prepare("INSERT INTO tasks_new (id, title, description, is_completed, priority, created_date, updated_date, sub_tasks, dueDate, recurring, frequency, frequency_amount, alarmId) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)").use { stmt ->
                stmt.bindText(1, newId)
                stmt.bindText(2, task.title)
                stmt.bindText(3, task.description)
                stmt.bindLong(4, task.isCompleted.toLong())
                stmt.bindLong(5, task.priority.toLong())
                stmt.bindLong(6, task.createdDate)
                stmt.bindLong(7, task.updatedDate)
                stmt.bindText(8, task.subTasks)
                stmt.bindLong(9, task.dueDate)
                stmt.bindLong(10, task.recurring.toLong())
                stmt.bindLong(11, task.frequency.toLong())
                stmt.bindLong(12, task.frequencyAmount.toLong())
                if (alarmId != null) stmt.bindLong(13, alarmId.toLong()) else stmt.bindNull(13)
                stmt.step()
            }
        }

        connection.execSQL("DROP TABLE tasks")
        connection.execSQL("ALTER TABLE tasks_new RENAME TO tasks")

        connection.execSQL("CREATE TABLE diary_new (title TEXT NOT NULL, content TEXT NOT NULL, created_date INTEGER NOT NULL, updated_date INTEGER NOT NULL, mood INTEGER NOT NULL, id TEXT PRIMARY KEY NOT NULL)")

        data class OldDiary(
            val title: String, val content: String, val createdDate: Long,
            val updatedDate: Long, val mood: Int
        )
        val diaryEntries = mutableListOf<OldDiary>()
        connection.prepare("SELECT title, content, created_date, updated_date, mood FROM diary").use { stmt ->
            while (stmt.step()) {
                diaryEntries.add(
                    OldDiary(
                        title = stmt.getText(0),
                        content = stmt.getText(1),
                        createdDate = stmt.getLong(2),
                        updatedDate = stmt.getLong(3),
                        mood = stmt.getInt(4)
                    )
                )
            }
        }
        for (entry in diaryEntries) {
            val newId = Uuid.generateV7().toString()
            connection.prepare("INSERT INTO diary_new (id, title, content, created_date, updated_date, mood) VALUES (?, ?, ?, ?, ?, ?)").use { stmt ->
                stmt.bindText(1, newId)
                stmt.bindText(2, entry.title)
                stmt.bindText(3, entry.content)
                stmt.bindLong(4, entry.createdDate)
                stmt.bindLong(5, entry.updatedDate)
                stmt.bindLong(6, entry.mood.toLong())
                stmt.step()
            }
        }

        connection.execSQL("DROP TABLE diary")
        connection.execSQL("ALTER TABLE diary_new RENAME TO diary")
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override suspend fun migrate(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `assistant_threads` (`id` TEXT PRIMARY KEY NOT NULL, `title` TEXT NOT NULL, `created_at` INTEGER NOT NULL, `updated_at` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `assistant_messages` (`id` TEXT PRIMARY KEY NOT NULL, `thread_id` TEXT NOT NULL, `type` INTEGER NOT NULL, `content` TEXT NOT NULL, `metadata` TEXT, `created_at` INTEGER NOT NULL)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_assistant_messages_thread_id` ON `assistant_messages` (`thread_id`)")
    }
}
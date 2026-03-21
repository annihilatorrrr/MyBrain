package com.mhss.app.data.mapper

import com.mhss.app.database.entity.BookmarkEntity
import com.mhss.app.database.entity.DiaryEntryEntity
import com.mhss.app.database.entity.NoteEntity
import com.mhss.app.database.entity.NoteFolderEntity
import com.mhss.app.database.entity.TaskEntity
import com.mhss.app.domain.model.Mood
import com.mhss.app.domain.model.Priority
import com.mhss.app.domain.model.SubTask
import com.mhss.app.domain.model.Task
import com.mhss.app.domain.model.TaskFrequency
import com.mhss.app.domain.model.backup.BackupBookmark
import com.mhss.app.domain.model.backup.BackupDiaryEntry
import com.mhss.app.domain.model.backup.BackupNote
import com.mhss.app.domain.model.backup.BackupNoteFolder
import com.mhss.app.domain.model.backup.BackupSubTask
import com.mhss.app.domain.model.backup.BackupTask
import kotlin.uuid.Uuid

fun NoteEntity.toBackupNote() = BackupNote(
    title = title,
    content = content,
    createdDate = createdDate,
    updatedDate = updatedDate,
    pinned = pinned,
    folderId = folderId,
    id = id
)

fun NoteFolderEntity.toBackupNoteFolder() = BackupNoteFolder(
    name = name,
    id = id
)

fun TaskEntity.toBackupTask() = BackupTask(
    title = title,
    description = description,
    isCompleted = isCompleted,
    priority = priority,
    createdDate = createdDate,
    updatedDate = updatedDate,
    subTasks = subTasks.map(SubTask::toBackupSubTask),
    dueDate = dueDate,
    recurring = recurring,
    frequency = frequency,
    frequencyAmount = frequencyAmount,
    alarmId = alarmId,
    id = id
)

fun DiaryEntryEntity.toBackupDiaryEntry() = BackupDiaryEntry(
    title = title,
    content = content,
    createdDate = createdDate,
    updatedDate = updatedDate,
    mood = mood.name,
    id = id
)

fun BookmarkEntity.toBackupBookmark() = BackupBookmark(
    url = url,
    title = title,
    description = description,
    createdDate = createdDate,
    updatedDate = updatedDate,
    id = id
)

fun BackupNote.toNoteEntity() = NoteEntity(
    title = title,
    content = content,
    createdDate = createdDate,
    updatedDate = updatedDate,
    pinned = pinned,
    folderId = folderId,
    id = id
)

fun BackupNoteFolder.toNoteFolderEntity() = NoteFolderEntity(
    name = name,
    id = id
)

fun BackupTask.toTask() = Task(
    title = title,
    description = description,
    isCompleted = isCompleted,
    priority = Priority.entries.firstOrNull { it.value == priority } ?: Priority.LOW,
    createdDate = createdDate,
    updatedDate = updatedDate,
    subTasks = subTasks.map(BackupSubTask::toSubTask),
    dueDate = dueDate,
    recurring = recurring,
    frequency = TaskFrequency.entries.firstOrNull { it.value == frequency } ?: TaskFrequency.DAILY,
    frequencyAmount = frequencyAmount,
    alarmId = alarmId,
    id = id
)

fun BackupDiaryEntry.toDiaryEntryEntity() = DiaryEntryEntity(
    title = title,
    content = content,
    createdDate = createdDate,
    updatedDate = updatedDate,
    mood = Mood.entries.firstOrNull { it.name == mood } ?: Mood.OKAY,
    id = id
)

fun BackupBookmark.toBookmarkEntity() = BookmarkEntity(
    url = url,
    title = title,
    description = description,
    createdDate = createdDate,
    updatedDate = updatedDate,
    id = id
)

private fun SubTask.toBackupSubTask() = BackupSubTask(
    title = title,
    isCompleted = isCompleted,
    id = id.toString()
)

private fun BackupSubTask.toSubTask() = SubTask(
    title = title,
    isCompleted = isCompleted,
    id = runCatching { Uuid.parse(id) }.getOrElse { Uuid.generateV7() }
)

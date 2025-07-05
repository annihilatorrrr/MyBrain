package com.mhss.app.data

import android.content.Context
import androidx.core.net.toUri
import androidx.core.text.isDigitsOnly
import androidx.documentfile.provider.DocumentFile
import androidx.room.withTransaction
import com.mhss.app.database.MyBrainDatabase
import com.mhss.app.database.entity.BookmarkEntity
import com.mhss.app.database.entity.DiaryEntryEntity
import com.mhss.app.database.entity.NoteEntity
import com.mhss.app.database.entity.NoteFolderEntity
import com.mhss.app.database.entity.TaskEntity
import com.mhss.app.database.entity.toTask
import com.mhss.app.domain.repository.BackupRepository
import com.mhss.app.domain.use_case.UpsertTaskUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class BackupRepositoryImpl(
    private val context: Context,
    private val database: MyBrainDatabase,
    private val upsertTaskUseCase: UpsertTaskUseCase,
    @Named("ioDispatcher") private val ioDispatcher: CoroutineDispatcher
) : BackupRepository {

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun exportDatabase(
        directoryUri: String,
        exportNotes: Boolean,
        exportTasks: Boolean,
        exportDiary: Boolean,
        exportBookmarks: Boolean,
        encrypted: Boolean, // To be added in a future version
        password: String // To be added in a future version
    ): Boolean {
        return withContext(ioDispatcher) {
            try {
                val fileName = "MyBrain_Backup_${System.currentTimeMillis()}.json"
                val pickedDir = DocumentFile.fromTreeUri(context, directoryUri.toUri())
                val destination = pickedDir!!.createFile("application/json", fileName)

                val notes = if (exportNotes) database.noteDao().getAllNotes() else emptyList()
                val noteFolders =
                    if (exportNotes) database.noteDao().getAllNoteFolders().first() else emptyList()
                val tasks =
                    if (exportTasks) database.taskDao().getAllTasks().first() else emptyList()
                val diary =
                    if (exportDiary) database.diaryDao().getAllEntries().first() else emptyList()
                val bookmarks = if (exportBookmarks) database.bookmarkDao().getAllBookmarks()
                    .first() else emptyList()

                val backupData = BackupData(notes, noteFolders, tasks, diary, bookmarks)

                val outputStream =
                    destination?.let { context.contentResolver.openOutputStream(it.uri) }
                        ?: return@withContext false

                outputStream.use {
                    Json.encodeToStream(backupData, outputStream)
                }

                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun importDatabase(
        fileUri: String,
        encrypted: Boolean, // To be added in a future version
        password: String // To be added in a future version
    ): Boolean {
        return withContext(ioDispatcher) {
            try {
                val json = Json {
                    ignoreUnknownKeys = true
                }
                val backupData = context.contentResolver.openInputStream(fileUri.toUri())?.use {
                    json.decodeFromStream<BackupData>(it)
                } ?: return@withContext false

                database.withTransaction {
                    val noteFolderIdMap = HashMap<String, String>()
                    val updatedNoteFolders = backupData.noteFolders.map { folder ->
                        val id = if (folder.id.isDigitsOnly()) {
                            Uuid.random().toString().also { noteFolderIdMap[folder.id] = it }
                        } else {
                            folder.id
                        }
                        folder.copy(id = id)
                    }
                    database.noteDao().upsertNoteFolders(updatedNoteFolders)

                    val updatedNotes = backupData.notes.map { note ->
                        val newFolderId =
                            if (note.folderId?.isDigitsOnly() == true) noteFolderIdMap[note.folderId]
                            else note.folderId.takeIfNotNull()
                        note.copy(folderId = newFolderId, id = note.id.toUuidIfNumber())
                    }
                    database.noteDao().upsertNotes(updatedNotes)

                    backupData.tasks.forEach {
                        upsertTaskUseCase(
                            task = it.toTask().copy(id = it.id.toUuidIfNumber()),
                            updateWidget = false

                        )
                    }

                    val updatedDiaryEntries = backupData.diary.map { entry ->
                        entry.copy(id = entry.id.toUuidIfNumber())
                    }
                    database.diaryDao().upsertEntries(updatedDiaryEntries)

                    val updatedBookmarks = backupData.bookmarks.map { bookmark ->
                        bookmark.copy(id = bookmark.id.toUuidIfNumber())
                    }
                    database.bookmarkDao().upsertBookmarks(updatedBookmarks)
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    private fun String?.takeIfNotNull(): String? {
        return if (this == "null") null else this
    }

    private fun String.toUuidIfNumber(): String {
        return if (this.isDigitsOnly()) {
            Uuid.random().toString()
        } else {
            this
        }
    }

    @Serializable
    private data class BackupData(
        @SerialName("notes") val notes: List<NoteEntity> = emptyList(),
        @SerialName("noteFolders") val noteFolders: List<NoteFolderEntity> = emptyList(),
        @SerialName("tasks") val tasks: List<TaskEntity> = emptyList(),
        @SerialName("diary") val diary: List<DiaryEntryEntity> = emptyList(),
        @SerialName("bookmarks") val bookmarks: List<BookmarkEntity> = emptyList()
    )
}
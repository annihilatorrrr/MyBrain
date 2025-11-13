package com.mhss.app.data.use_case

import android.content.Context
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.mhss.app.data.model.JsonBackupData
import com.mhss.app.database.MyBrainDatabase
import com.mhss.app.domain.use_case.`interface`.ExportJsonDataUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Named

@Factory
class ExportJsonDataUseCaseImpl(
    private val context: Context,
    private val database: MyBrainDatabase,
    @Named("ioDispatcher") private val ioDispatcher: CoroutineDispatcher
) : ExportJsonDataUseCase {
    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun invoke(
        directoryUri: String,
        exportNotes: Boolean,
        exportTasks: Boolean,
        exportDiary: Boolean,
        exportBookmarks: Boolean,
        encrypted: Boolean,
        password: String?
    ): Boolean {
        return withContext(ioDispatcher) {
            try {
                val fileName = "MyBrain_Backup_${System.currentTimeMillis()}.json"
                val pickedDir = DocumentFile.fromTreeUri(context, directoryUri.toUri()) ?: return@withContext false
                val destination = pickedDir.createFile("application/json", fileName)

                val notes = if (exportNotes) database.noteDao().getAllFullNotes() else emptyList()
                val noteFolders =
                    if (exportNotes) database.noteDao().getAllNoteFolders().first() else emptyList()
                val tasks =
                    if (exportTasks) database.taskDao().getAllFullTasks() else emptyList()
                val diary =
                    if (exportDiary) database.diaryDao().getAllFullEntries() else emptyList()
                val bookmarks = if (exportBookmarks) database.bookmarkDao().getAllFullBookmarks()
                    else emptyList()

                val backupData = JsonBackupData(notes, noteFolders, tasks, diary, bookmarks)

                val outputStream = destination?.let { context.contentResolver.openOutputStream(it.uri) }
                        ?: return@withContext false

                outputStream.use {
                    Json.encodeToStream(backupData, it)
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
}
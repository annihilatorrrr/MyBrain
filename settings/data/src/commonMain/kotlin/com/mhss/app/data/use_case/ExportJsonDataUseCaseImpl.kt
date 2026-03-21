package com.mhss.app.data.use_case

import com.mhss.app.domain.exception.BackupDataException
import com.mhss.app.domain.model.backup.JsonBackupData
import com.mhss.app.domain.model.backup.toBackupBookmark
import com.mhss.app.domain.model.backup.toBackupDiaryEntry
import com.mhss.app.domain.model.backup.toBackupNote
import com.mhss.app.domain.model.backup.toBackupNoteFolder
import com.mhss.app.domain.model.backup.toBackupTask
import com.mhss.app.domain.repository.BookmarkRepository
import com.mhss.app.domain.repository.DiaryRepository
import com.mhss.app.domain.repository.NoteRepository
import com.mhss.app.domain.repository.TaskRepository
import com.mhss.app.domain.use_case.`interface`.ExportJsonDataUseCase
import com.mhss.app.storage.StorageManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Named

@Factory
class ExportJsonDataUseCaseImpl(
    private val storageManager: StorageManager,
    private val noteRepository: NoteRepository,
    private val taskRepository: TaskRepository,
    private val diaryRepository: DiaryRepository,
    private val bookmarkRepository: BookmarkRepository,
    @Named("ioDispatcher") private val ioDispatcher: CoroutineDispatcher
) : ExportJsonDataUseCase {
    override suspend fun invoke(
        directoryUri: String,
        exportNotes: Boolean,
        exportTasks: Boolean,
        exportDiary: Boolean,
        exportBookmarks: Boolean,
        encrypted: Boolean,
        password: String?
    ) {
        withContext(ioDispatcher) {
            try {
                val notes = if (exportNotes) noteRepository.getAllFullNotes() else emptyList()
                val noteFolders = if (exportNotes) noteRepository.getAllNoteFolders().first() else emptyList()
                val tasks = if (exportTasks) taskRepository.getAllTasks().first() else emptyList()
                val diary = if (exportDiary) diaryRepository.getAllFullEntries() else emptyList()
                val bookmarks = if (exportBookmarks) bookmarkRepository.getAllBookmarks().first() else emptyList()

                val backupData = JsonBackupData(
                    schemaVersion = JsonBackupData.CURRENT_SCHEMA_VERSION,
                    notes = notes.map { it.toBackupNote() },
                    noteFolders = noteFolders.map { it.toBackupNoteFolder() },
                    tasks = tasks.map { it.toBackupTask() },
                    diary = diary.map { it.toBackupDiaryEntry() },
                    bookmarks = bookmarks.map { it.toBackupBookmark() }
                )


                val fileName = "MyBrain_Backup_${System.currentTimeMillis()}.json"

                storageManager.encodeJsonDataToFile(
                    directoryUri = directoryUri,
                    fileName = fileName,
                    mimeType = "application/json",
                    value = backupData,
                    serializer = JsonBackupData.serializer()
                )
            } catch (e: BackupDataException) {
                throw e
            } catch (_: Exception) {
                throw BackupDataException.GenericError()
            }
        }
    }

}

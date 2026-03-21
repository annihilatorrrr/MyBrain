package com.mhss.app.data.use_case

import androidx.core.text.isDigitsOnly
import com.mhss.app.database.DatabaseTransactionProvider
import com.mhss.app.domain.exception.BackupDataException
import com.mhss.app.domain.model.backup.JsonBackupData
import com.mhss.app.domain.model.backup.toBookmark
import com.mhss.app.domain.model.backup.toDiaryEntry
import com.mhss.app.domain.model.backup.toNote
import com.mhss.app.domain.model.backup.toNoteFolder
import com.mhss.app.domain.model.backup.toTask
import com.mhss.app.domain.repository.BookmarkRepository
import com.mhss.app.domain.repository.DiaryRepository
import com.mhss.app.domain.repository.NoteRepository
import com.mhss.app.domain.use_case.UpsertTaskUseCase
import com.mhss.app.domain.use_case.`interface`.ImportJsonDataUseCase
import com.mhss.app.storage.DecodeDataFromFileResult
import com.mhss.app.storage.StorageManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Named
import kotlin.uuid.Uuid

@Factory
class ImportJsonDataUseCaseImpl(
    private val storageManager: StorageManager,
    private val transactionProvider: DatabaseTransactionProvider,
    private val noteRepository: NoteRepository,
    private val upsertTaskUseCase: UpsertTaskUseCase,
    private val diaryRepository: DiaryRepository,
    private val bookmarkRepository: BookmarkRepository,
    @Named("ioDispatcher") private val ioDispatcher: CoroutineDispatcher
): ImportJsonDataUseCase {

    override suspend fun invoke(
        fileUri: String,
        encrypted: Boolean,
        password: String?
    ) {
        withContext(ioDispatcher) {
            try {
                val jsonBackupData = when (
                    val readResult = storageManager.decodeJsonDataFromFile(
                        fileUri = fileUri,
                        deserializer = JsonBackupData.serializer()
                    )
                ) {
                    is DecodeDataFromFileResult.Success -> readResult.value
                    DecodeDataFromFileResult.CouldNotReadFile -> throw BackupDataException.CouldNotReadFile
                }

                transactionProvider.runInTransaction {
                    val noteFolderIdMap = HashMap<String, String>()
                    val updatedNoteFolders = jsonBackupData.noteFolders.map { folder ->
                        val id = folder.id.toSafeBackupId()
                        if ( folder.id.all(Char::isDigit)) {
                            noteFolderIdMap[folder.id] = id
                        }
                        folder.copy(id = id).toNoteFolder()
                    }
                    noteRepository.upsertNoteFolders(updatedNoteFolders)

                    val updatedNotes = jsonBackupData.notes.map { note ->
                        val folderId = note.folderId
                        val newFolderId =
                            when {
                                folderId == null -> null
                                folderId.isBlank() -> null
                                folderId.isDigitsOnly() -> noteFolderIdMap[folderId]
                                else -> folderId
                            }
                        note.copy(folderId = newFolderId, id = note.id.toSafeBackupId()).toNote()
                    }
                    noteRepository.upsertNotes(updatedNotes)

                    jsonBackupData.tasks.forEach {
                        upsertTaskUseCase(
                            task = it.copy(id = it.id.toSafeBackupId()).toTask(),
                            updateWidget = false
                        )
                    }

                    val updatedDiaryEntries = jsonBackupData.diary.map { entry ->
                        entry.copy(id = entry.id.toSafeBackupId()).toDiaryEntry()
                    }
                    diaryRepository.upsertEntries(updatedDiaryEntries)

                    val updatedBookmarks = jsonBackupData.bookmarks.map { bookmark ->
                        bookmark.copy(id = bookmark.id.toSafeBackupId()).toBookmark()
                    }
                    bookmarkRepository.upsertBookmarks(updatedBookmarks)
                }
            } catch (e: BackupDataException) {
                throw e
            } catch (_: Exception) {
                throw BackupDataException.GenericError()
            }
        }
    }

    private fun String.toSafeBackupId(): String {
        return if (this.isBlank() || this == "null" || this.isDigitsOnly()) {
            Uuid.generateV7().toString()
        } else {
            this
        }
    }

}

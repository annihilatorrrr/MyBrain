package com.mhss.app.mybrain.sync.domain

import com.mhss.app.database.dao.SyncDao
import com.mhss.app.database.dao.incrementAndGet
import com.mhss.app.database.entity.DeletedEntityEntity
import com.mhss.app.database.entity.DeletedEntityType
import com.mhss.app.database.helpers.DatabaseTransactionProvider
import com.mhss.app.domain.use_case.UpsertBookmarksUseCase
import com.mhss.app.domain.use_case.UpsertDiaryEntriesUseCase
import com.mhss.app.domain.use_case.UpsertNoteFoldersUseCase
import com.mhss.app.domain.use_case.UpsertNotesUseCase
import com.mhss.app.domain.use_case.UpsertTaskUseCase
import com.mhss.app.mybrain.sync.model.SyncPayload
import com.mhss.app.mybrain.sync.repository.SyncRepository
import org.koin.core.annotation.Single

@Single
class MergePayloadUseCase(
    private val syncRepository: SyncRepository,
    private val syncDao: SyncDao,
    private val transactionProvider: DatabaseTransactionProvider,
    private val upsertTask: UpsertTaskUseCase,
    private val upsertNotes: UpsertNotesUseCase,
    private val upsertNoteFolders: UpsertNoteFoldersUseCase,
    private val upsertDiaryEntries: UpsertDiaryEntriesUseCase,
    private val upsertBookmarks: UpsertBookmarksUseCase
) {
    suspend operator fun invoke(payload: SyncPayload) {
        if (payload.folders.isNotEmpty()) {
            val tombstones = getTombstones(
                DeletedEntityType.NOTE_FOLDER,
                payload.folders.map { it.id },
                payload.deletedEntities
            )
            val localFolders = syncRepository.getNoteFoldersByIds(payload.folders.map { it.id })
                .associateBy { it.id }
            val foldersToUpsert = payload.folders.filter { folder ->
                if (folder.id in tombstones) return@filter false
                val localFolder = localFolders[folder.id]
                localFolder == null || folder.updatedDate > localFolder.updatedDate
            }
            if (foldersToUpsert.isNotEmpty()) {
                upsertNoteFolders(foldersToUpsert, notifySyncChanges = false)
            }
        }

        if (payload.notes.isNotEmpty()) {
            val tombstones = getTombstones(
                DeletedEntityType.NOTE,
                payload.notes.map { it.id },
                payload.deletedEntities
            )
            val localNotes =
                syncRepository.getNotesByIds(payload.notes.map { it.id }).associateBy { it.id }
            val notesToUpsert = payload.notes.filter { note ->
                if (note.id in tombstones) return@filter false
                val localNote = localNotes[note.id]
                localNote == null || note.updatedDate > localNote.updatedDate
            }
            if (notesToUpsert.isNotEmpty()) {
                upsertNotes(notesToUpsert, notifySyncChanges = false)
            }
        }

        if (payload.tasks.isNotEmpty()) {
            val tombstones = getTombstones(
                DeletedEntityType.TASK,
                payload.tasks.map { it.id },
                payload.deletedEntities
            )
            val localTasks =
                syncRepository.getTasksByIds(payload.tasks.map { it.id }).associateBy { it.id }
            payload.tasks.forEach { task ->
                if (task.id in tombstones) return@forEach
                val localTask = localTasks[task.id]
                if (localTask == null || task.updatedDate > localTask.updatedDate) {
                    val taskWithLocalAlarmId = task.copy(alarmId = localTask?.alarmId)
                    upsertTask(
                        taskWithLocalAlarmId,
                        previousTask = localTask,
                        notifySyncChanges = false
                    )
                }
            }
        }

        if (payload.diaryEntries.isNotEmpty()) {
            val tombstones = getTombstones(
                DeletedEntityType.DIARY,
                payload.diaryEntries.map { it.id },
                payload.deletedEntities
            )
            val localEntries =
                syncRepository.getDiaryEntriesByIds(payload.diaryEntries.map { it.id })
                    .associateBy { it.id }
            val entriesToUpsert = payload.diaryEntries.filter { entry ->
                val localEntry = localEntries[entry.id]
                entry.id !in tombstones &&
                        (localEntry == null || entry.updatedDate > localEntry.updatedDate)
            }
            if (entriesToUpsert.isNotEmpty()) {
                upsertDiaryEntries(entriesToUpsert, notifySyncChanges = false)
            }
        }

        if (payload.bookmarks.isNotEmpty()) {
            val tombstones = getTombstones(
                DeletedEntityType.BOOKMARK,
                payload.bookmarks.map { it.id },
                payload.deletedEntities
            )
            val localBookmarks = syncRepository.getBookmarksByIds(payload.bookmarks.map { it.id })
                .associateBy { it.id }
            val bookmarksToUpsert = payload.bookmarks.filter { bookmark ->
                if (bookmark.id in tombstones) return@filter false
                val localBookmark = localBookmarks[bookmark.id]
                localBookmark == null || bookmark.updatedDate > localBookmark.updatedDate
            }
            if (bookmarksToUpsert.isNotEmpty()) {
                upsertBookmarks(bookmarksToUpsert, notifySyncChanges = false)
            }
        }

        if (payload.assistantThreads.isNotEmpty()) {
            val tombstones = getTombstones(
                DeletedEntityType.THREAD,
                payload.assistantThreads.map { it.id },
                payload.deletedEntities
            )
            val localThreads =
                syncRepository.getThreadsByIds(payload.assistantThreads.map { it.id })
                    .associateBy { it.id }
            val threadsToUpsert = payload.assistantThreads.mapNotNull { thread ->
                if (thread.id in tombstones) return@mapNotNull null
                val localThread = localThreads[thread.id]
                if (localThread == null || thread.updatedAt > localThread.updatedAt) {
                    thread.copy(syncSeq = syncDao.incrementAndGet())
                } else null
            }
            if (threadsToUpsert.isNotEmpty()) {
                syncRepository.upsertThreads(threadsToUpsert)
            }
        }

        if (payload.assistantMessages.isNotEmpty()) {
            val messageTombstones = getTombstones(
                DeletedEntityType.MESSAGE,
                payload.assistantMessages.map { it.id },
                payload.deletedEntities
            )
            val threadTombstones = getTombstones(
                DeletedEntityType.THREAD,
                payload.assistantMessages.map { it.threadId }.distinct(),
                payload.deletedEntities
            )
            val localMessages =
                syncRepository.getMessagesByIds(payload.assistantMessages.map { it.id })
                    .associateBy { it.id }
            val messagesToUpsert = payload.assistantMessages.mapNotNull { message ->
                if (message.id in messageTombstones || message.threadId in threadTombstones) return@mapNotNull null
                val localMessage = localMessages[message.id]
                if (localMessage == null || message.createdAt > localMessage.createdAt) {
                    message.copy(syncSeq = syncDao.incrementAndGet())
                } else null
            }
            if (messagesToUpsert.isNotEmpty()) {
                syncRepository.upsertMessages(messagesToUpsert)
            }
        }

        if (payload.deletedEntities.isNotEmpty()) {
            for (deletion in payload.deletedEntities) {
                transactionProvider.runInTransaction {
                    if (syncRepository.deletedEntityExists(deletion.entityType, deletion.entityId)) {
                        return@runInTransaction
                    }
                    when (deletion.entityType) {
                        DeletedEntityType.NOTE.key -> syncRepository.deleteNoteById(deletion.entityId)
                        DeletedEntityType.NOTE_FOLDER.key -> syncRepository.deleteNoteFolderById(
                            deletion.entityId
                        )

                        DeletedEntityType.TASK.key -> syncRepository.deleteTaskById(deletion.entityId)
                        DeletedEntityType.DIARY.key -> syncRepository.deleteDiaryEntryById(deletion.entityId)
                        DeletedEntityType.BOOKMARK.key -> syncRepository.deleteBookmarkById(deletion.entityId)
                        DeletedEntityType.THREAD.key -> syncRepository.deleteThreadAndMessages(
                            deletion.entityId
                        )

                        DeletedEntityType.MESSAGE.key -> syncRepository.deleteMessage(deletion.entityId)
                    }
                    syncRepository.insertDeletedEntity(
                        deletion.copy(syncSeq = syncDao.incrementAndGet())
                    )
                }
            }
        }
    }

    private suspend fun getTombstones(
        type: DeletedEntityType,
        ids: List<String>,
        remoteTombstones: List<DeletedEntityEntity>
    ): Set<String> {
        val tombstoneIds = HashSet<String>()
        tombstoneIds.addAll(syncRepository.getDeletedEntityIds(type.key, ids.toHashSet().toList()))
        remoteTombstones.forEach { if (it.entityType == type.key) tombstoneIds.add(it.entityId) }
        return tombstoneIds
    }
}

package com.mhss.app.data.repository

import com.mhss.app.data.mappers.toAiMessage
import com.mhss.app.data.mappers.toAssistantMessageEntity
import com.mhss.app.database.dao.AssistantDao
import com.mhss.app.database.entity.AssistantThreadEntity
import com.mhss.app.domain.model.AiMessage
import com.mhss.app.domain.model.AssistantThread
import com.mhss.app.domain.repository.AssistantChatRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Named

@Factory
class AssistantChatRepositoryImpl(
    private val assistantDao: AssistantDao,
    private val toolExecutor: AiToolExecutor,
    @Named("ioDispatcher") private val ioDispatcher: CoroutineDispatcher,
    @Named("defaultDispatcher") private val defaultDispatcher: CoroutineDispatcher,
) : AssistantChatRepository {

    override fun getAllThreads(): Flow<List<AssistantThread>> {
        return assistantDao.getAllThreads().map { entities ->
            entities.map { it.toAssistantThread() }
        }.flowOn(ioDispatcher)
    }

    override suspend fun saveThread(thread: AssistantThread): Unit = withContext(ioDispatcher) {
        assistantDao.upsertThread(thread.toEntity())
    }

    override suspend fun deleteThread(threadId: String): Unit = withContext(ioDispatcher) {
        assistantDao.deleteThreadAndMessages(threadId)
    }

    override suspend fun deleteAllThreads(): Unit = withContext(ioDispatcher) {
        assistantDao.deleteAllThreadsAndMessages()
    }

    override fun getMessages(threadId: String): Flow<List<AiMessage>> {
        return assistantDao.getMessagesByThreadId(threadId).map { entities ->
            withContext(defaultDispatcher) {
                entities.mapNotNullTo(ArrayList(entities.size)) { it.toAiMessage(toolExecutor) }
            }
        }.flowOn(ioDispatcher)
    }

    override suspend fun saveMessage(threadId: String, message: AiMessage): Unit =
        withContext(ioDispatcher) {
            assistantDao.insertMessageAndUpdateThread(message.toAssistantMessageEntity(threadId))
        }

    override suspend fun deleteMessage(messageId: String): Unit = withContext(ioDispatcher) {
        assistantDao.deleteMessage(messageId)
    }

    private fun AssistantThreadEntity.toAssistantThread(): AssistantThread {
        return AssistantThread(
            id = id,
            title = title,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    private fun AssistantThread.toEntity(): AssistantThreadEntity {
        return AssistantThreadEntity(
            id = id,
            title = title,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

}

package com.mhss.app.data.repository

import ai.koog.agents.core.tools.Tool
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.prompt.message.Message
import ai.koog.serialization.kotlinx.KotlinxSerializer
import ai.koog.serialization.kotlinx.toKoogJSONObject
import com.mhss.app.data.json
import com.mhss.app.data.nowMillis
import com.mhss.app.data.tools.CREATE_EVENTS_TOOL
import com.mhss.app.data.tools.CREATE_EVENT_TOOL
import com.mhss.app.data.tools.CREATE_MULTIPLE_NOTES_TOOL
import com.mhss.app.data.tools.CREATE_MULTIPLE_TASKS_TOOL
import com.mhss.app.data.tools.CREATE_NOTE_TOOL
import com.mhss.app.data.tools.CREATE_TASK_TOOL
import com.mhss.app.data.tools.CalendarEventIdResult
import com.mhss.app.data.tools.CalendarEventIdsResult
import com.mhss.app.data.tools.NoteIdResult
import com.mhss.app.data.tools.NoteIdsResult
import com.mhss.app.data.tools.SEARCH_EVENTS_BY_NAME_WITHIN_RANGE_TOOL
import com.mhss.app.data.tools.SEARCH_NOTES_TOOL
import com.mhss.app.data.tools.UPDATE_TASK_COMPLETED_TOOL
import com.mhss.app.data.tools.SearchEventsResult
import com.mhss.app.data.tools.SearchNotesResult
import com.mhss.app.data.tools.TaskIdResult
import com.mhss.app.data.tools.TaskIdsResult
import com.mhss.app.data.tools.TaskResult
import com.mhss.app.domain.model.AiMessage
import com.mhss.app.domain.model.ToolCallResultObject
import com.mhss.app.domain.use_case.GetCalendarEventByIdUseCase
import com.mhss.app.domain.use_case.GetNoteUseCase
import com.mhss.app.domain.use_case.GetTaskByIdUseCase
import org.koin.core.annotation.Single
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Single
class AiToolExecutor(
    private val getNote: GetNoteUseCase,
    private val getTaskById: GetTaskByIdUseCase,
    private val getCalendarEventById: GetCalendarEventByIdUseCase,
) {

    private val koogSerializer = KotlinxSerializer(json)

    @OptIn(ExperimentalUuidApi::class)
    suspend fun executeToolCall(
        toolCall: Message.Tool.Call,
        toolRegistry: ToolRegistry,
    ): Result<AiMessage.ToolCall> = runCatching {
        val tool = toolRegistry.getTool(toolCall.tool)
        val args = tool.decodeArgs(
            toolCall.contentJson.toKoogJSONObject(),
            koogSerializer
        )
        val toolResult = (tool as Tool<Any?, Any?>).execute(args)
        val resultJson = tool.encodeResultToStringUnsafe(toolResult, koogSerializer)
        val resultObject = extractResultObject(tool.name, resultJson)
        AiMessage.ToolCall(
            uuid = Uuid.generateV7().toString(),
            id = toolCall.id,
            name = tool.name,
            rawContent = toolCall.content,
            resultRawContent = resultJson,
            time = nowMillis(),
            resultObject = resultObject
        )
    }

    internal suspend fun extractResultObject(
        toolName: String,
        resultJson: String
    ): ToolCallResultObject? = runCatching {
        when (toolName) {
            SEARCH_NOTES_TOOL ->  {
                val searchResult = json.decodeFromString<SearchNotesResult>(resultJson)
                if (searchResult.notes.size == 1) ToolCallResultObject.Notes(searchResult.notes) else null
            }

            CREATE_NOTE_TOOL -> {
                val createResult = json.decodeFromString<NoteIdResult>(resultJson)
                getNote(createResult.createdNoteId)?.let {
                    ToolCallResultObject.Notes(listOf(it))
                }
            }

            CREATE_MULTIPLE_NOTES_TOOL -> {
                val createResult = json.decodeFromString<NoteIdsResult>(resultJson)
                val notes = createResult.createdNoteIds.mapNotNull { getNote(it) }
                if (notes.isNotEmpty()) ToolCallResultObject.Notes(notes) else null
            }

            CREATE_TASK_TOOL -> {
                val createResult = json.decodeFromString<TaskIdResult>(resultJson)
                getTaskById(createResult.createdTaskId)?.let {
                    ToolCallResultObject.Tasks(listOf(it))
                }
            }

            CREATE_MULTIPLE_TASKS_TOOL -> {
                val createResult = json.decodeFromString<TaskIdsResult>(resultJson)
                val tasks = createResult.createdTaskIds.mapNotNull { getTaskById(it) }
                if (tasks.isNotEmpty()) ToolCallResultObject.Tasks(tasks) else null
            }

            UPDATE_TASK_COMPLETED_TOOL -> {
                val updateResult = json.decodeFromString<TaskResult>(resultJson)
                updateResult.task?.let {
                    ToolCallResultObject.Tasks(listOf(it))
                }
            }

            CREATE_EVENT_TOOL -> {
                val createResult = json.decodeFromString<CalendarEventIdResult>(resultJson)
                createResult.createdEventId?.let { id ->
                    getCalendarEventById(id)?.let {
                        ToolCallResultObject.CalendarEvents(listOf(it))
                    }
                }
            }

            CREATE_EVENTS_TOOL -> {
                val createResult = json.decodeFromString<CalendarEventIdsResult>(resultJson)
                val events = createResult.createdEventIds.mapNotNull { id ->
                    id?.let { getCalendarEventById(it) }
                }
                if (events.isNotEmpty()) ToolCallResultObject.CalendarEvents(events) else null
            }

            SEARCH_EVENTS_BY_NAME_WITHIN_RANGE_TOOL -> {
                val searchResult = json.decodeFromString<SearchEventsResult>(resultJson)
                if (searchResult.events.size == 1) ToolCallResultObject.CalendarEvents(searchResult.events) else null
            }

            else -> null
        }
    }.getOrNull()

    fun extractThoughtSignatures(messages: List<Message.Response>): Map<Message.Tool.Call, String?> {
        val signatures = HashMap<Message.Tool.Call, String?>()
        var lastSignature: String? = null
        for (msg in messages) {
            when (msg) {
                is Message.Reasoning -> lastSignature = msg.encrypted
                is Message.Tool.Call -> {
                    signatures[msg] = lastSignature
                    lastSignature = null
                }
                is Message.Assistant -> lastSignature = null
            }
        }
        return signatures
    }

}

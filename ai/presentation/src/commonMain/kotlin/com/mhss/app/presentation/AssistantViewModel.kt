@file:OptIn(ExperimentalUuidApi::class)

package com.mhss.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mhss.app.datetime.now
import com.mhss.app.datetime.todayPlusDays
import com.mhss.app.domain.model.AiMessage
import com.mhss.app.domain.model.AiMessageAttachment
import com.mhss.app.domain.model.AiRepositoryException
import com.mhss.app.domain.model.AssistantResult
import com.mhss.app.domain.model.CalendarEvent
import com.mhss.app.domain.model.Note
import com.mhss.app.domain.model.Task
import com.mhss.app.domain.use_case.GetAllEventsUseCase
import com.mhss.app.domain.use_case.GetNoteUseCase
import com.mhss.app.domain.use_case.GetTaskByIdUseCase
import com.mhss.app.domain.use_case.SearchNotesUseCase
import com.mhss.app.domain.use_case.SearchTasksUseCase
import com.mhss.app.domain.use_case.SendAiMessageUseCase
import com.mhss.app.preferences.PrefsConstants
import com.mhss.app.preferences.domain.model.AiProvider
import com.mhss.app.preferences.domain.model.intPreferencesKey
import com.mhss.app.preferences.domain.model.stringSetPreferencesKey
import com.mhss.app.preferences.domain.model.toAiProvider
import com.mhss.app.preferences.domain.use_case.GetPreferenceUseCase
import com.mhss.app.ui.ItemView
import com.mhss.app.ui.toIntList
import com.mhss.app.ui.toNotesView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.koin.android.annotation.KoinViewModel
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@KoinViewModel
class AssistantViewModel(
    private val sendAiMessage: SendAiMessageUseCase,
    private val getPreference: GetPreferenceUseCase,
    private val searchNotes: SearchNotesUseCase,
    private val searchTasks: SearchTasksUseCase,
    private val getCalendarEvents: GetAllEventsUseCase,
    private val getNoteById: GetNoteUseCase,
    private val getTaskById: GetTaskByIdUseCase,
) : ViewModel() {

    private val _messages = MutableStateFlow<List<AiMessage>>(emptyList())
    val messages: StateFlow<List<AiMessage>> = _messages.asStateFlow()

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var searchNotesJob: Job? = null
    private var searchTasksJob: Job? = null
    private var sendMessageJob: Job? = null

    init {
        viewModelScope.launch {
            getPreference(
                intPreferencesKey(PrefsConstants.NOTE_VIEW_KEY),
                ItemView.LIST.value
            ).onEach { value ->
                _uiState.update { it.copy(noteView = value.toNotesView()) }
            }.collect()
        }
        viewModelScope.launch {
            getPreference(intPreferencesKey(PrefsConstants.AI_PROVIDER_KEY), AiProvider.None.id)
                .map { it.toAiProvider() }
                .collect { provider ->
                    _uiState.update { it.copy(aiEnabled = provider != AiProvider.None) }
                }
        }
    }

    fun onEvent(event: AssistantEvent) {

        when (event) {
            is AssistantEvent.SendMessage -> {
                sendMessageJob?.cancel()
                sendMessageJob = viewModelScope.launch {
                    val message = AiMessage.UserMessage(
                        content = event.content,
                        attachments = event.attachments,
                        attachmentsText = getAttachmentText(event.attachments),
                        time = now(),
                        uuid = Uuid.generateV7().toString()
                    )

                    _messages.update { listOf(message) + it }
                    _uiState.update {
                        it.copy(
                            attachments = emptyList(),
                            loading = true,
                            error = null
                        )
                    }

                    sendAiMessage(_messages.value.asReversed())
                        .catch { e ->
                            delay(300)

                            val error = if (e is AiRepositoryException) {
                                e.failure
                            } else {
                                AssistantResult.OtherError(e.message)
                            }

                            if (error !is AssistantResult.ToolCallLimitExceeded) {
                                _messages.update { it.drop(1) }
                            }

                            _uiState.update {
                                it.copy(
                                    loading = false,
                                    error = error
                                )
                            }
                        }
                        .onCompletion {
                            _uiState.update { it.copy(loading = false) }
                        }
                        .collect { msg ->
                            _messages.update { listOf(msg) + it }
                        }
                }
            }

            is AssistantEvent.SearchNotes -> {
                searchNotesJob?.cancel()
                searchNotesJob = viewModelScope.launch {
                    delay(300)
                    searchNotes(event.query).let { notes ->
                        _uiState.update { it.copy(searchNotes = notes) }
                    }
                }
            }

            is AssistantEvent.SearchTasks -> {
                searchTasksJob?.cancel()
                searchTasksJob = viewModelScope.launch {
                    delay(300)
                    searchTasks(event.query).first().let { tasks ->
                        _uiState.update { it.copy(searchTasks = tasks) }
                    }
                }
            }

            AssistantEvent.AddAttachmentEvents -> {
                _uiState.update {
                    it.copy(attachments = it.attachments + AiMessageAttachment.CalenderEvents)
                }
            }

            is AssistantEvent.AddAttachmentNote -> viewModelScope.launch {
                val note = getNoteById(event.id) ?: return@launch
                _uiState.update {
                    it.copy(
                        attachments = it.attachments + AiMessageAttachment.Note(
                            note.copy(
                                title = note.title.ifBlank { "Untitled Note" }
                            )
                        )
                    )
                }
            }

            is AssistantEvent.AddAttachmentTask -> viewModelScope.launch {
                val task = getTaskById(event.id) ?: return@launch
                _uiState.update {
                    it.copy(attachments = it.attachments + AiMessageAttachment.Task(task))
                }
            }

            is AssistantEvent.RemoveAttachment -> {
                _uiState.update { s ->
                    val list = s.attachments
                    if (event.index in list.indices) {
                        s.copy(attachments = list.filterIndexed { i, _ -> i != event.index })
                    } else {
                        s
                    }
                }
            }

            AssistantEvent.CancelMessage -> {
                sendMessageJob?.cancel()
                if (_messages.value.firstOrNull() is AiMessage.UserMessage) {
                    _messages.update { it.drop(1) }
                }
                _uiState.update { it.copy(loading = false) }
            }
        }
    }

    private suspend fun getAttachmentText(attachments: List<AiMessageAttachment>): String =
        withContext(Dispatchers.Default) {
            val builder = StringBuilder()
            if (attachments.isEmpty()) return@withContext ""
            builder.appendLine()
            builder.appendLine("Attached content from the user:")
            for (attachment in attachments) {
                when (attachment) {
                    is AiMessageAttachment.Note -> {
                        builder.appendLine("Attached Note:")
                        builder.appendLine(Json.encodeToString(attachment.note))
                    }

                    is AiMessageAttachment.Task -> {
                        builder.appendLine("Attached Task:")
                        builder.appendLine(Json.encodeToString(attachment.task))
                    }

                    is AiMessageAttachment.CalenderEvents -> {
                        builder.appendLine("Next 7 days events:")
                        builder.appendLine(Json.encodeToString(getEventsForNext7Days()))
                    }
                }
            }
            return@withContext builder.toString()
        }

    private suspend fun getEventsForNext7Days(): Map<String, List<CalendarEvent>> {
        val excluded = getPreference(
            stringSetPreferencesKey(PrefsConstants.EXCLUDED_CALENDARS_KEY),
            emptySet()
        ).first()
        return getCalendarEvents(excluded.toIntList(), todayPlusDays(7))
    }


    data class UiState(
        val loading: Boolean = false,
        val error: AssistantResult.Failure? = null,
        val aiEnabled: Boolean = false,
        val noteView: ItemView = ItemView.LIST,
        val searchNotes: List<Note> = emptyList(),
        val searchTasks: List<Task> = emptyList(),
        val attachments: List<AiMessageAttachment> = emptyList(),
    )
}

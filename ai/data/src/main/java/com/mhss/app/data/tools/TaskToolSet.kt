package com.mhss.app.data.tools

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet
import com.mhss.app.data.llmDateTimeFormatUnicode
import com.mhss.app.data.nowMillis
import com.mhss.app.data.parseDateTimeFromLLM
import com.mhss.app.domain.model.Priority
import com.mhss.app.domain.model.SubTask
import com.mhss.app.domain.model.Task
import com.mhss.app.domain.model.TaskFrequency
import com.mhss.app.domain.use_case.GetTaskByIdUseCase
import com.mhss.app.domain.use_case.SearchTasksUseCase
import com.mhss.app.domain.use_case.UpdateTaskCompletedUseCase
import com.mhss.app.domain.use_case.UpsertTaskUseCase
import com.mhss.app.domain.use_case.UpsertTasksUseCase
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@OptIn(kotlin.uuid.ExperimentalUuidApi::class)
@Factory
class TaskToolSet(
    private val upsertTask: UpsertTaskUseCase,
    private val upsertTasks: UpsertTasksUseCase,
    private val searchTasksByName: SearchTasksUseCase,
    private val getTask: GetTaskByIdUseCase,
    private val updateTaskCompletedUseCase: UpdateTaskCompletedUseCase
) : ToolSet {

    @Tool(SEARCH_TASKS_TOOL)
    @LLMDescription("Search tasks by title (partial match). If the query is empty, returns all tasks. If the user asks about the due date, use $FORMAT_DATE_TOOL to get accurate dates from the result.")
    suspend fun searchTasks(
        query: String
    ): SearchTasksResult = SearchTasksResult(searchTasksByName(query).first())

    @Tool(CREATE_TASK_TOOL)
    @LLMDescription("Create a task. Returns ID.")
    suspend fun createTask(
        title: String,
        description: String = "",
        priority: Priority = Priority.LOW,
        @LLMDescription("Format: $llmDateTimeFormatUnicode") dueDate: String? = null,
        subTasks: List<SubTaskInput>? = null,
        recurring: Boolean = false,
        frequency: TaskFrequency = TaskFrequency.DAILY,
        frequencyAmount: Int = 1
    ): TaskIdResult {
        val id = Uuid.random().toString()
        val task = Task(
            title = title,
            description = description,
            priority = priority,
            dueDate = if (dueDate != null) {
                dueDate.parseDateTimeFromLLM() ?: throw IllegalArgumentException("Invalid due date format for date: $dueDate. The task was not created.")
            } else 0L,
            subTasks = subTasks?.map { SubTask(it.title, it.isCompleted) } ?: emptyList(),
            recurring = recurring,
            frequency = frequency,
            frequencyAmount = frequencyAmount,
            createdDate = nowMillis(),
            updatedDate = nowMillis(),
            id = id
        )
        upsertTask(task)
        return TaskIdResult(createdTaskId = id)
    }

    @Tool(UPDATE_TASK_COMPLETED_TOOL)
    @LLMDescription("Update task completed status.")
    suspend fun updateTaskCompleted(
        id: String,
        completed: Boolean
    ) {
        val task = getTask(id) ?: throw IllegalArgumentException("Task with id $id not found. The operation did not proceed.")
        updateTaskCompletedUseCase(task, completed)
    }

    @Tool(CREATE_MULTIPLE_TASKS_TOOL)
    @LLMDescription("Create multiple tasks. Returns IDs.")
    suspend fun createMultipleTasks(
        tasks: List<TaskInput>
    ): TaskIdsResult {
        val taskModels = tasks.map { input ->
            val id = Uuid.random().toString()
            Task(
                title = input.title,
                description = input.description,
                priority = input.priority,
                dueDate = input.dueDate?.let {
                    it.parseDateTimeFromLLM() ?: throw IllegalArgumentException("Invalid date format for task: ${input.title}. The tasks were not created.")
                } ?: 0L,
                subTasks = input.subTasks?.map { SubTask(it.title, it.isCompleted) } ?: emptyList(),
                recurring = input.recurring,
                frequency = input.frequency,
                frequencyAmount = input.frequencyAmount,
                createdDate = nowMillis(),
                updatedDate = nowMillis(),
                id = id
            )
        }
        upsertTasks(taskModels)
        return TaskIdsResult(createdTaskIds = taskModels.map { it.id })
    }
}

@Serializable
data class TaskInput(
    val title: String,
    val description: String = "",
    val priority: Priority = Priority.LOW,
    @param:LLMDescription("Format: $llmDateTimeFormatUnicode") val dueDate: String? = null,
    val subTasks: List<SubTaskInput>? = null,
    val recurring: Boolean = false,
    val frequency: TaskFrequency = TaskFrequency.DAILY,
    val frequencyAmount: Int = 1
)

@Serializable
data class SubTaskInput(
    val title: String,
    val isCompleted: Boolean = false
)

@Serializable
data class SearchTasksResult(val tasks: List<Task>)

@Serializable
data class TaskIdResult(val createdTaskId: String)

@Serializable
data class TaskResult(val task: Task?)

@Serializable
data class TaskIdsResult(val createdTaskIds: List<String>)

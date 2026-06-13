package com.mhss.app.database.dao

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Query
import androidx.room3.Update
import androidx.room3.Upsert
import com.mhss.app.database.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks")
    suspend fun getAllFullTasks(): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTask(id: String): TaskEntity?

    @Query("SELECT * FROM tasks WHERE id IN (:ids)")
    suspend fun getTasksByIds(ids: List<String>): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE updated_date > :timestamp")
    suspend fun getTasksUpdatedAfter(timestamp: Long): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE sync_seq > :seq AND sync_seq <= :maxSeq")
    suspend fun getTasksAfterSeq(seq: Long, maxSeq: Long): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE alarmId = :alarmId")
    suspend fun getTaskByAlarm(alarmId: Int): TaskEntity?

    @Query("SELECT * FROM tasks WHERE title LIKE '%' || :title || '%'")
    fun getTasksByTitle(title: String): Flow<List<TaskEntity>>

    @Upsert
    suspend fun upsertTask(task: TaskEntity)

    @Upsert
    suspend fun upsertTasks(tasks: List<TaskEntity>)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: String)

    @Query("UPDATE tasks SET is_completed = :completed, sync_seq = :syncSeq, updated_date = :updatedDate WHERE id = :id")
    suspend fun updateCompleted(id: String, completed: Boolean, syncSeq: Long, updatedDate: Long)

}

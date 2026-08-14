package com.mhss.app.database.dao

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Query
import androidx.room3.Upsert
import com.mhss.app.database.entity.AlarmEntity

@Dao
interface AlarmDao {

    @Query("SELECT * FROM alarms")
    suspend fun getAll(): List<AlarmEntity>

    @Upsert
    suspend fun upsert(alarm: AlarmEntity): Long

    @Delete
    suspend fun delete(alarm: AlarmEntity)

    @Query("DELETE FROM alarms WHERE id = :id")
    suspend fun delete(id: Int)

}

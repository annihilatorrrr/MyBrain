package com.mhss.app.database.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "sync_state")
data class SyncStateEntity(
    @PrimaryKey
    val id: Int = 1,
    @ColumnInfo(name = "last_seq")
    val lastSeq: Long = 0L
)

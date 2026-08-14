package com.mhss.app.database.converters

import androidx.room3.ColumnTypeConverter
import com.mhss.app.database.entity.AssistantMessageMetadata
import com.mhss.app.domain.model.Mood
import com.mhss.app.domain.model.SubTask
import kotlinx.serialization.json.Json

class DBConverters {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    @ColumnTypeConverter
    fun fromStringList(value: List<String>): String {
        return json.encodeToString(value)
    }

    @ColumnTypeConverter
    fun toStringList(value: String): List<String> {
        if (value.isBlank()) return emptyList()
        return try {
            json.decodeFromString(value)
        } catch (_: Exception) {
            emptyList()
        }
    }


    @ColumnTypeConverter
    fun fromSubTasksList(value: List<SubTask>): String {
        return json.encodeToString(value)
    }
    @ColumnTypeConverter
    fun toSubTasksList(value: String): List<SubTask> {
        return json.decodeFromString(value)
    }

    @ColumnTypeConverter
    fun toMood(value: Int) = enumValues<Mood>()[value]
    @ColumnTypeConverter
    fun fromMood(value: Mood) = value.ordinal

    @ColumnTypeConverter
    fun fromMetadata(value: AssistantMessageMetadata?): String? {
        return value?.let { json.encodeToString(it) }
    }

    @ColumnTypeConverter
    fun toMetadata(value: String?): AssistantMessageMetadata? {
        if (value.isNullOrBlank()) return null
        return json.decodeFromString(value)
    }
}


package com.mhss.app.database.converters

import androidx.room3.TypeConverter
import com.mhss.app.database.entity.AssistantMessageMetadata
import com.mhss.app.domain.model.Mood
import com.mhss.app.domain.model.SubTask
import kotlinx.serialization.json.Json

class DBConverters {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    @TypeConverter
    fun fromSubTasksList(value: List<SubTask>): String {
        return json.encodeToString(value)
    }
    @TypeConverter
    fun toSubTasksList(value: String): List<SubTask> {
        return json.decodeFromString(value)
    }

    @TypeConverter
    fun toMood(value: Int) = enumValues<Mood>()[value]
    @TypeConverter
    fun fromMood(value: Mood) = value.ordinal

    @TypeConverter
    fun fromMetadata(value: AssistantMessageMetadata?): String? {
        return value?.let { json.encodeToString(it) }
    }

    @TypeConverter
    fun toMetadata(value: String?): AssistantMessageMetadata? {
        if (value.isNullOrBlank()) return null
        return json.decodeFromString(value)
    }
}


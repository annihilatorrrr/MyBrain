package com.mhss.app.domain.model.backup

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class JsonBackupData(
    @SerialName("schemaVersion")
    val schemaVersion: Int = 1,
    @SerialName("notes")
    val notes: List<BackupNote> = emptyList(),
    @SerialName("noteFolders")
    val noteFolders: List<BackupNoteFolder> = emptyList(),
    @SerialName("tasks")
    val tasks: List<BackupTask> = emptyList(),
    @SerialName("diary")
    val diary: List<BackupDiaryEntry> = emptyList(),
    @SerialName("bookmarks")
    val bookmarks: List<BackupBookmark> = emptyList()
) {
    companion object {
        const val CURRENT_SCHEMA_VERSION = 2
    }
}

object BackupStringIdSerializer : KSerializer<String> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("BackupStringIdSerializer", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: String) {
        encoder.encodeString(value)
    }

    override fun deserialize(decoder: Decoder): String {
        val jsonDecoder = decoder as JsonDecoder
        val element = jsonDecoder.decodeJsonElement()
        return if (element is JsonNull) "" else element.jsonPrimitive.content
    }
}

@OptIn(ExperimentalSerializationApi::class)
object BackupNullableStringIdSerializer : KSerializer<String?> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("BackupNullableStringIdSerializer", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: String?) {
        if (value == null) encoder.encodeNull() else encoder.encodeString(value)
    }

    override fun deserialize(decoder: Decoder): String? {
        val jsonDecoder = decoder as JsonDecoder
        val element = jsonDecoder.decodeJsonElement()
        if (element is JsonNull) return null
        return element.jsonPrimitive.content.takeUnless { it == "null" }
    }
}

package com.mhss.app.data.tools

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet
import com.mhss.app.data.llmDateTimeWithDayNameFormat
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import org.koin.core.annotation.Factory
import kotlin.time.Instant

@Factory
class UtilToolSet : ToolSet {

    @Tool(FORMAT_DATE_TOOL)
    @LLMDescription("Convert a date in milliseconds to a formatted date string. Use to get a readable date from objects that contain date as milliseconds. ")
    fun formatDate(
        @LLMDescription("The date in milliseconds.") millis: Long
    ): FormattedDateResult {
        val instant = Instant.fromEpochMilliseconds(millis)
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        val formattedDate = localDateTime.format(llmDateTimeWithDayNameFormat)
        return FormattedDateResult(formattedDate)
    }
}

@Serializable
data class FormattedDateResult(val formattedDate: String)

package com.mhss.app.data.tools

import ai.koog.agents.core.tools.Tool
import ai.koog.agents.core.tools.ToolBase
import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.serialization.typeToken
import com.mhss.app.data.llmDateTimeWithDayNameFormat
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import org.koin.core.annotation.Factory
import kotlin.time.Instant

@Factory
class UtilToolSet {
    private val formatDateTool = object : Tool<FormatDateArgs, FormattedDateResult>(
        argsType = typeToken<FormatDateArgs>(),
        resultType = typeToken<FormattedDateResult>(),
        name = FORMAT_DATE_TOOL,
        description = "Convert a date in milliseconds to a formatted date string. Use to get a readable date from objects that contain date as milliseconds. "
    ) {
        override suspend fun execute(args: FormatDateArgs): FormattedDateResult {
            val instant = Instant.fromEpochMilliseconds(args.millis)
            val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
            val formattedDate = localDateTime.format(llmDateTimeWithDayNameFormat)
            return FormattedDateResult(formattedDate)
        }
    }

    val tools: List<ToolBase<*, *>> = listOf(formatDateTool)
}

@Serializable
data class FormatDateArgs(
    @property:LLMDescription("The date in milliseconds.") val millis: Long
)

@Serializable
data class FormattedDateResult(val formattedDate: String)

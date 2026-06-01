package com.mhss.app.ui.preview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mhss.app.datetime.DateTimeFormatter
import com.mhss.app.datetime.HOUR_MILLIS
import com.mhss.app.datetime.LocalDateTimeFormatter
import com.mhss.app.datetime.currentLocalDate
import com.mhss.app.datetime.isCurrentYear
import com.mhss.app.datetime.isToday
import com.mhss.app.datetime.localDateTime
import com.mhss.app.ui.theme.MyBrainTheme
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.number
import java.util.Locale

@Composable
fun BasePreview(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalDateTimeFormatter provides previewDateTimeFormatter
    ) {
        MyBrainTheme(darkTheme = darkTheme) {
            Surface(
                color = MaterialTheme.colorScheme.background
            ) {
                Box(modifier = Modifier.padding(6.dp)) {
                    content()
                }
            }
        }
    }
}


private val previewDateTimeFormatter = object : DateTimeFormatter {

    private val monthShortNames = listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )
    private val monthFullNames = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    private val dayFullNames = listOf(
        "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
    )
    private val dayShortNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    override val is24HourFormat: Boolean
        get() = false

    private fun formatTimeInternal(local: LocalDateTime): String {
        val h12 = when (val h = local.hour % 12) {
            0 -> 12
            else -> h
        }
        val amPm = if (local.hour < 12) "AM" else "PM"
        val minutePart = if (local.minute == 0) "" else ":%02d".format(local.minute)
        return "$h12$minutePart $amPm"
    }

    override fun formatEventsDayName(date: LocalDate): String {
        val dow = dayFullNames[date.dayOfWeek.ordinal]
        val mon = monthShortNames[date.month.number - 1]
        return "$dow, $mon ${date.dayOfMonth}"
    }

    override fun formatDateDependingOnDay(timestamp: Long): String {
        val localDT = timestamp.localDateTime
        val timePart = formatTimeInternal(localDT)
        return if (localDT.isToday()) {
            timePart
        } else {
            "${monthShortNames[localDT.month.number - 1]} %02d, ${localDT.year} $timePart"
                .format(localDT.day)
        }
    }

    override fun fullDate(timestamp: Long): String {
        val localDT = timestamp.localDateTime
        return "${monthShortNames[localDT.month.number - 1]} %02d, ${localDT.year} ${formatTimeInternal(localDT)}"
            .format(localDT.dayOfMonth)
    }

    override fun formatDateForMapping(timestamp: Long): String {
        val localDT = timestamp.localDateTime
        val dow = dayFullNames[localDT.dayOfWeek.ordinal]
        val mon = monthShortNames[localDT.month.number - 1]
        return "$dow ${localDT.day}, $mon ${localDT.year}"
    }

    override fun formatDateForMapping(date: LocalDate): String {
        val dow = dayFullNames[date.dayOfWeek.ordinal]
        val mon = monthShortNames[date.month.number - 1]
        return "$dow ${date.day}, $mon ${date.year}"
    }

    override fun formatTime(timestamp: Long): String {
        val localDT = timestamp.localDateTime
        val minutes = timestamp % HOUR_MILLIS
        val h12 = when (val h = localDT.hour % 12) {
            0 -> 12
            else -> h
        }
        return if (minutes == 0L) {
            val amPm = if (localDT.hour < 12) "AM" else "PM"
            "$h12 $amPm"
        } else {
            formatTimeInternal(localDT)
        }
    }

    override fun formatDate(timestamp: Long, forceShowYear: Boolean): String {
        val localDT = timestamp.localDateTime
        val dow = dayShortNames[localDT.dayOfWeek.ordinal]
        val mon = monthShortNames[localDT.month.number - 1]
        val showYear = forceShowYear || !localDT.isCurrentYear()
        return if (showYear) {
            "$dow, $mon %02d, ${localDT.year}".format(localDT.dayOfMonth)
        } else {
            "$dow, $mon %02d".format(localDT.dayOfMonth)
        }
    }

    override fun monthName(timestamp: Long): String {
        val localDT = timestamp.localDateTime
        val full = monthFullNames[localDT.month.number - 1]
        return if (localDT.isCurrentYear()) full else "$full ${localDT.year}"
    }

    override fun getDisplayName(dayOfWeek: DayOfWeek): String {
        return dayShortNames[dayOfWeek.ordinal]
    }

    override fun monthName(date: LocalDate): String {
        val full = monthFullNames[date.month.number - 1]
        return if (date.year == currentLocalDate().year) full else "$full ${date.year}"
    }

    override fun formatEventStartEnd(
        start: Long,
        end: Long,
        allDayString: String,
        eventTimeAt: String,
        eventTime: String,
        location: String?,
        allDay: Boolean
    ): String {
        return if (allDay) {
            allDayString
        } else {
            if (!location.isNullOrBlank()) {
                String.format(
                    Locale.getDefault(),
                    eventTimeAt,
                    formatTime(start),
                    formatTime(end),
                    location
                )
            } else {
                String.format(
                    Locale.getDefault(),
                    eventTime,
                    formatTime(start),
                    formatTime(end)
                )
            }
        }
    }
}

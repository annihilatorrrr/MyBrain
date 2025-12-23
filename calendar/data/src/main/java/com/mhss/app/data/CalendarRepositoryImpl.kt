package com.mhss.app.data

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import com.mhss.app.domain.model.Calendar
import com.mhss.app.domain.model.CalendarEvent
import com.mhss.app.domain.model.CalendarEventFrequency
import com.mhss.app.domain.repository.CalendarRepository
import com.mhss.app.ui.R
import com.mhss.app.util.date.at
import com.mhss.app.util.date.now
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import java.util.TimeZone
import kotlin.time.Duration.Companion.days

@Single
class CalendarRepositoryImpl(
    private val context: Context,
    @Named("ioDispatcher") private val ioDispatcher: CoroutineDispatcher
) : CalendarRepository {

    override suspend fun getEvents(excludedCalendars: List<Int>, until: Long?): List<CalendarEvent> {
        return withContext(ioDispatcher) {
            val instancesProjection = getCalendarEventsProjection()
            val contentResolver = context.contentResolver
            val startM = now().at(0, 0)
            val endM = until ?: (startM + 100.days.inWholeMilliseconds)
            val builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
            ContentUris.appendId(builder, startM)
            ContentUris.appendId(builder, endM)

            val selectionParts = mutableListOf<String>()
            selectionParts += "${CalendarContract.Instances.BEGIN} >= ?"
            if (excludedCalendars.isNotEmpty()) {
                selectionParts += "${CalendarContract.Instances.CALENDAR_ID} NOT IN (${excludedCalendars.joinToString(",")})"
            }
            val instancesSelection = selectionParts.joinToString(" AND ")
            val instancesSelectionArgs = arrayOf(startM.toString())

            val curI = contentResolver.query(
                builder.build(),
                instancesProjection,
                instancesSelection,
                instancesSelectionArgs,
                "${CalendarContract.Instances.BEGIN} ASC"
            )
            curI?.use { it.getEvents() } ?: emptyList()
        }
    }

    override suspend fun getEvents(start: Long, end: Long, excludedCalendars: List<Int>): List<CalendarEvent> {
        return withContext(ioDispatcher) {
            val instancesProjection = getCalendarEventsProjection()
            val contentResolver = context.contentResolver
            val builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
            ContentUris.appendId(builder, start)
            ContentUris.appendId(builder, end)

            val selection = if (excludedCalendars.isNotEmpty()) {
                "${CalendarContract.Instances.CALENDAR_ID} NOT IN (${excludedCalendars.joinToString(",")})"
            } else null

            val curI = contentResolver.query(
                builder.build(),
                instancesProjection,
                selection,
                null,
                null
            )

            val events = curI?.use { it.getEvents() } ?: emptyList()
            events.sortedBy { it.start }
        }
    }

    override suspend fun searchEventsByTitleWithinRange(
        start: Long,
        end: Long,
        titleQuery: String,
        excludedCalendars: List<Int>
    ): List<CalendarEvent> {
        return withContext(ioDispatcher) {
            val query = titleQuery.trim()
            if (query.isBlank()) return@withContext emptyList()

            val instancesProjection = getCalendarEventsProjection()
            val contentResolver = context.contentResolver
            val builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
            ContentUris.appendId(builder, start)
            ContentUris.appendId(builder, end)

            val selectionParts = mutableListOf<String>()
            val selectionArgs = mutableListOf<String>()

            selectionParts += "${CalendarContract.Instances.TITLE} LIKE ?"
            selectionArgs += "%$query%"

            if (excludedCalendars.isNotEmpty()) {
                selectionParts += "${CalendarContract.Instances.CALENDAR_ID} NOT IN (${excludedCalendars.joinToString(",")})"
            }

            val selection = selectionParts.joinToString(" AND ")

            val curI = contentResolver.query(
                builder.build(),
                instancesProjection,
                selection,
                selectionArgs.toTypedArray(),
                "${CalendarContract.Instances.BEGIN} ASC"
            )

            curI?.use { it.getEvents() } ?: emptyList()
        }
    }

    override suspend fun getCalendars() : List<Calendar>{
        return withContext(ioDispatcher){
            val projection = arrayOf(
                CalendarContract.Calendars._ID,                     // 0
                CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,   // 1
                CalendarContract.Calendars.ACCOUNT_NAME, // 2
                CalendarContract.Calendars.ACCOUNT_TYPE, // 3
                CalendarContract.Calendars.CALENDAR_COLOR// 4

            )
            val uri = CalendarContract.Calendars.CONTENT_URI
            val contentResolver = context.contentResolver
            val cur = contentResolver.query(
                uri,
                projection,
                null,
                null,
                null
            )
            val calendars: MutableList<Calendar> = mutableListOf()
            if (cur != null) {
                while (cur.moveToNext()) {

                    val calID: Long = cur.getLong(CALENDAR_ID_INDEX)
                    val calendarName: String = cur.getString(CALENDAR_NAME_INDEX)
                    val accountName: String = cur.getString(ACCOUNT_NAME_INDEX)
                    val color: Int = cur.getInt(CALENDAR_CALENDAR_COLOR_INDEX)

                    calendars.add(
                        Calendar(
                            calID,
                            calendarName,
                            accountName,
                            color
                        )
                    )
                }
                cur.close()
                calendars
            }else
                emptyList()
        }
    }

    override suspend fun addEvent(event: CalendarEvent) {
        withContext(ioDispatcher){
            val values = ContentValues().apply {
                put(CalendarContract.Events.CALENDAR_ID, event.calendarId)
                put(CalendarContract.Events.TITLE, event.title)
                put(CalendarContract.Events.DESCRIPTION, event.description)
                put(CalendarContract.Events.DTSTART, event.start)
                put(CalendarContract.Events.ALL_DAY, event.allDay)
                put(CalendarContract.Events.EVENT_LOCATION, event.location)
                if (event.recurring){
                    put(CalendarContract.Events.RRULE, event.getEventRRule())
                    put(CalendarContract.Events.DURATION, event.getEventDuration())
                } else {
                    put(CalendarContract.Events.DTEND, event.end)
                }
                put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
            }
            context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
        }
    }

    override suspend fun updateEvent(event: CalendarEvent) {
        withContext(ioDispatcher){
            val values = ContentValues().apply {
                put(CalendarContract.Events.CALENDAR_ID, event.calendarId)
                put(CalendarContract.Events.TITLE, event.title)
                put(CalendarContract.Events.DESCRIPTION, event.description)
                put(CalendarContract.Events.DTSTART, event.start)
                if (event.recurring && event.frequency != CalendarEventFrequency.NEVER){
                    val end: Long? = null
                    put(CalendarContract.Events.RRULE, event.getEventRRule())
                    put(CalendarContract.Events.DURATION, event.getEventDuration())
                    put(CalendarContract.Events.DTEND, end)
                }else {
                    val rule: String? = null
                    put(CalendarContract.Events.RRULE, rule)
                    put(CalendarContract.Events.DURATION, rule)
                    put(CalendarContract.Events.DTEND, event.end)
                }
                put(CalendarContract.Events.ALL_DAY, event.allDay)
                put(CalendarContract.Events.EVENT_LOCATION, event.location)
            }
            val updateUri: Uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, event.id)
            context.contentResolver.update(updateUri, values, null, null)
        }
    }

    override suspend fun createCalendar() {
        withContext(ioDispatcher){
            val uri = CalendarContract.Calendars.CONTENT_URI.asSyncAdapter(context.getString(R.string.app_name), CalendarContract.ACCOUNT_TYPE_LOCAL)
            val values = ContentValues().apply {
                put(CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL)
                put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, context.getString(R.string.app_name))
                put(CalendarContract.Calendars.NAME, context.getString(R.string.app_name))
                put(CalendarContract.Calendars.ACCOUNT_NAME, context.getString(R.string.app_name))
                put(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL, CalendarContract.Calendars.CAL_ACCESS_OWNER)
                put(CalendarContract.Calendars.CALENDAR_COLOR, 0x03DAC5)
                put(CalendarContract.Calendars.VISIBLE, 1)
                put(CalendarContract.Calendars.SYNC_EVENTS, 1)
            }
            context.contentResolver.insert(uri, values)
        }
    }

    private fun Uri.asSyncAdapter(account: String, accountType: String): Uri {
        return buildUpon()
            .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
            .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, account)
            .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, accountType).build()
    }

    override suspend fun deleteEvent(event: CalendarEvent) {
        withContext(ioDispatcher){
            val updateUri: Uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, event.id)
            context.contentResolver.delete(updateUri, null, null)
        }
    }

    private fun String.extractFrequency(): CalendarEventFrequency {
        return if (this.contains("FREQ=")) {
            val freq = this.substringAfter("FREQ=").substringBefore(";")
            when (freq) {
                "DAILY" -> CalendarEventFrequency.DAILY
                "WEEKLY" -> CalendarEventFrequency.WEEKLY
                "MONTHLY" -> CalendarEventFrequency.MONTHLY
                "YEARLY" -> CalendarEventFrequency.YEARLY
                else -> CalendarEventFrequency.NEVER
            }
        } else CalendarEventFrequency.NEVER
    }

    private fun CalendarEvent.getEventDuration(): String {
        return "P${(end - start) / 1000}S"
    }

    private fun String.extractEndFromDuration(start: Long): Long {
        return try {
            val duration = this.substring(1, this.length - 1).toLong() * 1000
            start + duration
        }catch (_: Exception) {
            start
        }
    }

    private fun CalendarEvent.getEventRRule(): String {
        return buildString {
            append("FREQ=${frequency}")
            // will be implemented later
//       if (until > 0) {
//           val formattedUntil = SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.US).format(until)
//           append(";UNTIL=${formattedUntil}")
//       }
//       if (count > 0) {
//           append(";COUNT=${count}")
//       }
//       if (interval > 0) {
//           append(";INTERVAL=${interval}")
//       }
        }
    }

    private fun getCalendarEventsProjection() = arrayOf(
        CalendarContract.Instances.EVENT_ID,
        CalendarContract.Instances.TITLE,
        CalendarContract.Instances.DESCRIPTION,
        CalendarContract.Instances.BEGIN,
        CalendarContract.Instances.END,
        CalendarContract.Instances.EVENT_LOCATION,
        CalendarContract.Instances.ALL_DAY,
        CalendarContract.Instances.EVENT_COLOR,
        CalendarContract.Instances.CALENDAR_ID,
        CalendarContract.Instances.RRULE,
        CalendarContract.Instances.DURATION,
        CalendarContract.Instances.CALENDAR_COLOR
    )

    private fun Cursor.getEvents(): List<CalendarEvent> {
        val events = mutableListOf<CalendarEvent>()
        while (moveToNext()) {
            val eventId: Long = getLong(ID_INDEX)
            val title: String = getString(TITLE_INDEX) ?: continue
            val description: String? = getString(DESC_INDEX)
            val start: Long = getLong(START_INDEX)
            val duration: String = getString(EVENT_DURATION_INDEX) ?: ""
            val end: Long = if (duration.isNotBlank()) duration.extractEndFromDuration(start) else getLong(END_INDEX)
            val location: String? = getString(LOCATION_INDEX)
            val allDay: Boolean = getInt(ALL_DAY_INDEX) == 1
            val color: Int = getInt(COLOR_INDEX)
            val calendarColor: Int = getInt(CALENDAR_COLOR_INDEX)
            val calendarId: Long = getLong(EVENT_CALENDAR_ID_INDEX)
            val rrule: String = getString(EVENT_RRULE_INDEX) ?: ""
            val recurring: Boolean = rrule.isNotBlank()
            val frequency: CalendarEventFrequency = rrule.extractFrequency()
            events.add(
                CalendarEvent(
                    id = eventId,
                    title = title,
                    description = description,
                    start = start,
                    end = end,
                    location = location,
                    allDay = allDay,
                    color = if (color != 0) color else calendarColor,
                    calendarId = calendarId,
                    frequency = frequency,
                    recurring = recurring,
                )
            )
        }
        return events
    }

    companion object {
        private const val ID_INDEX = 0
        private const val TITLE_INDEX = 1
        private const val DESC_INDEX = 2
        private const val START_INDEX = 3
        private const val END_INDEX = 4
        private const val LOCATION_INDEX = 5
        private const val ALL_DAY_INDEX = 6
        private const val COLOR_INDEX = 7
        private const val EVENT_CALENDAR_ID_INDEX = 8
        private const val EVENT_RRULE_INDEX = 9
        private const val EVENT_DURATION_INDEX = 10
        private const val CALENDAR_COLOR_INDEX = 11

        private const val CALENDAR_ID_INDEX: Int = 0
        private const val CALENDAR_NAME_INDEX: Int = 1
        private const val ACCOUNT_NAME_INDEX: Int = 2
        private const val CALENDAR_CALENDAR_COLOR_INDEX: Int = 4
    }
}
package com.mhss.app.mybrain.appfunctions

import androidx.appfunctions.AppFunctionSerializable

/**
 * A personal note
 */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class AppNote(
    /** The note's unique ID */
    val id: String,
    /** The note's title */
    val title: String,
    /** The note's body content */
    val content: String,
    /** Whether the note is pinned */
    val pinned: Boolean,
    /** The timestamp when the note was created */
    val createdDate: Long
)

/**
 * A task
 */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class AppTask(
    /** The task's ID */
    val id: String,
    /** The task's title */
    val title: String,
    /** The task's description. */
    val description: String,
    /** Whether the task is completed */
    val isCompleted: Boolean,
    /** The task's priority level (LOW, MEDIUM, or HIGH) */
    val priority: String,
    /** The task's due date timestamp */
    val dueDate: Long
)

/**
 * A diary entry.
 */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class AppDiaryEntry(
    /** The diary entry's unique ID */
    val id: String,
    /** The diary entry's title */
    val title: String,
    /** The diary entry's text content */
    val content: String,
    /** The user's mood for this entry (AWESOME, GOOD, OKAY, BAD, or TERRIBLE) */
    val mood: String,
    /** The timestamp when the diary entry was created */
    val createdDate: Long
)

/**
 * A bookmark.
 */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class AppBookmark(
    /** The bookmark's unique ID. */
    val id: String,
    /** The saved URL link */
    val url: String,
    /** The bookmark's title */
    val title: String,
    /** The description of the bookmark or site */
    val description: String,
    /** The timestamp when the bookmark was created */
    val createdDate: Long
)

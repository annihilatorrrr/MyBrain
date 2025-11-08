package com.mhss.app.domain.repository

interface NoteFileUtilsRepository {

    suspend fun takePersistablePermission(uri: String)

    suspend fun getPathFromUri(uri: String): String?
}
package com.mhss.app.domain.repository

interface FileUtilsRepository {

    suspend fun takePersistablePermission(uri: String)

    suspend fun getPathFromUri(uri: String): String
}


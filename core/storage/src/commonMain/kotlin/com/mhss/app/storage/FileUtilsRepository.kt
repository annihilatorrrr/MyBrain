package com.mhss.app.storage

interface FileUtilsRepository {

    suspend fun takePersistablePermission(uri: String)

    suspend fun getPathFromUri(uri: String): String
}

package com.mhss.app.database

interface DatabaseTransactionProvider {
    suspend fun <T> runInTransaction(block: suspend () -> T): T
}

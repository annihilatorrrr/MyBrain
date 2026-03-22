package com.mhss.app.database.helpers

interface DatabaseTransactionProvider {
    suspend fun <T> runInTransaction(block: suspend () -> T): T
}
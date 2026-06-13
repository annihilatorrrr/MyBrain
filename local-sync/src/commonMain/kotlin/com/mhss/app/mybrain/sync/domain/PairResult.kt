package com.mhss.app.mybrain.sync.domain

sealed class PairResult {
    object Success : PairResult()
    object OfflineSuccess : PairResult()
    data class Error(val message: String) : PairResult()
}
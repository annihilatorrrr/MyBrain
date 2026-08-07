package com.mhss.app.mybrain.sync.domain

sealed class PairResult {
    object Success : PairResult()
    data class Error(val message: String) : PairResult()
}

package com.mhss.app.mybrain.sync.util

import java.util.concurrent.ConcurrentHashMap

actual inline fun <K, V> concurrentMutableMap(): MutableMap<K, V> = ConcurrentHashMap()

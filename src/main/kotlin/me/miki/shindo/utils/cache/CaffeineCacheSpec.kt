package me.miki.shindo.utils.cache

import java.util.concurrent.TimeUnit

data class CaffeineCacheSpec(
    val maximumSize: Long = 512L,
    val expireAfterWriteDuration: Long = 10L,
    val expireAfterWriteUnit: TimeUnit = TimeUnit.MINUTES,
    val expireAfterAccessDuration: Long? = null,
    val expireAfterAccessUnit: TimeUnit = TimeUnit.MINUTES,
    val recordStats: Boolean = false
)

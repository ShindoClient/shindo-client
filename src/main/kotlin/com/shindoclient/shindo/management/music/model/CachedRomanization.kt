package com.shindoclient.shindo.management.music.model

internal data class CachedRomanization(
    val romanized: String,
    val timestamp: Long = System.currentTimeMillis(),
) {
    companion object {
        private const val CACHE_EXPIRATION_MS = 7 * 24 * 60 * 60 * 1000L
    }

    fun isExpired(): Boolean = System.currentTimeMillis() - timestamp > CACHE_EXPIRATION_MS
}

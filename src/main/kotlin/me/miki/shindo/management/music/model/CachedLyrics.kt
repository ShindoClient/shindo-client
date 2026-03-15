package me.miki.shindo.management.music.model

internal data class CachedLyrics(
    val lyrics: LyricsResponse,
    val timestamp: Long = System.currentTimeMillis()
) {
    companion object {
        private const val CACHE_DURATION_MS = 24 * 60 * 60 * 1000L
    }

    fun isExpired(): Boolean =
        System.currentTimeMillis() - timestamp > CACHE_DURATION_MS
}

package me.miki.shindo.management.music.model

data class LyricsResponse(
    val lines: MutableList<LyricsLine> = mutableListOf(),
    val error: Boolean = false,
    val syncType: String? = null,
) {
    fun isError(): Boolean = error
}

package com.shindoclient.shindo.management.music.model

import com.google.gson.annotations.SerializedName

class LyricsLine {
    @SerializedName("startTimeMs")
    private val startTimeMs: String? = null
    val words: String? = null

    @SerializedName("endTimeMs")
    private val endTimeMs: String? = null
    var romanizedWords: String? =
        null
    val startTime: Long
        get() =
            try {
                startTimeMs!!.toLong()
            } catch (e: NumberFormatException) {
                0
            }
    val endTime: Long
        get() {
            return try {
                endTimeMs!!.toLong()
            } catch (e: NumberFormatException) {
                0
            }
        }

    override fun toString(): String = words!!
}

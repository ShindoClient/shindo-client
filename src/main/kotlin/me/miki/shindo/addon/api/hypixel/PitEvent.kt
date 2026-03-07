package me.miki.shindo.addon.api.hypixel

/**
 * Representa um evento do The Pit conhecido pelo client.
 */
data class PitEvent(
    val id: String,
    val name: String,
    val description: String? = null,
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val inProgress: Boolean
)


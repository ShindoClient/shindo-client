package me.miki.shindo.api.websocket.presence

data class PresenceUser(
    val uuid: String,
    val name: String,
    val accountType: String,
    val lastSeen: Long,
)

package me.miki.shindo.api.chat

data class ChatMessage(
    val fromUuid: String,
    val fromName: String,
    val message: String
)

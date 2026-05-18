package me.miki.shindo.management.event.impl

import me.miki.shindo.management.event.Event

class EventSendChat(
    private val _message: String,
) : Event() {
    fun getMessage(): String = _message
}

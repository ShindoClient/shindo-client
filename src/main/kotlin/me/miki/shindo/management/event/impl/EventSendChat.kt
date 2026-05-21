package me.miki.shindo.management.event.impl

import me.miki.shindo.management.event.Event

class EventSendChat(
    private val message: String,
) : Event() {
    fun getMessage(): String = message
}

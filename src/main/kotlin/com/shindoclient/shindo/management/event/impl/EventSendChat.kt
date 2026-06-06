package com.shindoclient.shindo.management.event.impl

import com.shindoclient.shindo.management.event.Event

class EventSendChat(
    private val message: String,
) : Event() {
    fun getMessage(): String = message
}

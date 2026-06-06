package com.shindoclient.shindo.management.event.impl

import com.shindoclient.shindo.management.event.Event

class EventScrollMouse(
    private val amount: Int,
) : Event() {
    fun getAmount(): Int = amount
}

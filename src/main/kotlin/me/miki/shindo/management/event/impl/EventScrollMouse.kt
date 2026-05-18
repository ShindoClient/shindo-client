package me.miki.shindo.management.event.impl

import me.miki.shindo.management.event.Event

class EventScrollMouse(
    private val _amount: Int,
) : Event() {
    fun getAmount(): Int = _amount
}

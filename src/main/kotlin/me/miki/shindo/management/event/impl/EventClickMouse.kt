package me.miki.shindo.management.event.impl

import me.miki.shindo.management.event.Event

class EventClickMouse(
    private val button: Int,
) : Event() {
    fun getButton(): Int = button
}

package me.miki.shindo.management.event.impl

import me.miki.shindo.management.event.Event

class EventClickMouse(private val _button: Int) : Event() {
    fun getButton(): Int = _button
}


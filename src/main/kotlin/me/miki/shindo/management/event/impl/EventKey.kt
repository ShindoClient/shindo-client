package me.miki.shindo.management.event.impl

import me.miki.shindo.management.event.Event

class EventKey(
    private var keyCode: Int,
) : Event() {
    fun getKeyCode(): Int = keyCode

    fun setKeyCode(keyCode: Int) {
        this.keyCode = keyCode
    }
}

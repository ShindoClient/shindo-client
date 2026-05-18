package me.miki.shindo.management.event.impl

import me.miki.shindo.management.event.Event

class EventKey(
    keyCode: Int,
) : Event() {
    @JvmField
    var keyCode: Int = keyCode

    fun getKeyCode(): Int = keyCode

    fun setKeyCode(keyCode: Int) {
        this.keyCode = keyCode
    }
}

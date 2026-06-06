package com.shindoclient.shindo.management.event.impl

import com.shindoclient.shindo.management.event.Event

class EventKey(
    private var keyCode: Int,
) : Event() {
    fun getKeyCode(): Int = keyCode

    fun setKeyCode(keyCode: Int) {
        this.keyCode = keyCode
    }
}

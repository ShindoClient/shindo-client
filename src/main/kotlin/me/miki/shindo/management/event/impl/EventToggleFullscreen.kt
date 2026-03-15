package me.miki.shindo.management.event.impl

import me.miki.shindo.management.event.Event

class EventToggleFullscreen(state: Boolean) : Event() {
    var state: Boolean = state
    var isApplyState: Boolean = true
}


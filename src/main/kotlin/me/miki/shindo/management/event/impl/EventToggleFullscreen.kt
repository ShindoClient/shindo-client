package me.miki.shindo.management.event.impl

import me.miki.shindo.management.event.Event

class EventToggleFullscreen(
    var state: Boolean,
) : Event() {
    var isApplyState: Boolean = true
}

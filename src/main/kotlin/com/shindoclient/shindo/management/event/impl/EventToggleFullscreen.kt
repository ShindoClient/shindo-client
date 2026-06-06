package com.shindoclient.shindo.management.event.impl

import com.shindoclient.shindo.management.event.Event

class EventToggleFullscreen(
    var state: Boolean,
) : Event() {
    var isApplyState: Boolean = true
}

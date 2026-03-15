package me.miki.shindo.management.event.impl

import me.miki.shindo.management.event.Event

class EventHitOverlay(
    var red: Float,
    var green: Float,
    var blue: Float,
    var alpha: Float
) : Event()
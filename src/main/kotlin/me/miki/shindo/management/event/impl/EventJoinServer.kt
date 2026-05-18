package me.miki.shindo.management.event.impl

import me.miki.shindo.management.event.Event

class EventJoinServer(
    private val _ip: String,
) : Event() {
    fun getIp(): String = _ip
}

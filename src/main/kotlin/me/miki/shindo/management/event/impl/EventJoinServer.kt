package me.miki.shindo.management.event.impl

import me.miki.shindo.management.event.Event

class EventJoinServer(
    private val ip: String,
) : Event() {
    fun getIp(): String = ip
}

package com.shindoclient.shindo.management.event.impl

import com.shindoclient.shindo.management.event.Event

class EventJoinServer(
    private val ip: String,
) : Event() {
    fun getIp(): String = ip
}

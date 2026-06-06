package com.shindoclient.shindo.management.event.impl

import com.shindoclient.shindo.management.event.Event
import net.minecraft.client.entity.AbstractClientPlayer

class EventFovUpdate(
    private val entity: AbstractClientPlayer,
    private var fov: Float,
) : Event() {
    fun getFov(): Float = fov

    fun setFov(fov: Float) {
        this.fov = fov
    }

    fun getEntity(): AbstractClientPlayer = entity
}

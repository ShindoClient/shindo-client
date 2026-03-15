package me.miki.shindo.management.event.impl

import me.miki.shindo.management.event.Event
import net.minecraft.client.entity.AbstractClientPlayer

class EventFovUpdate(
    private val _entity: AbstractClientPlayer,
    private var _fov: Float
) : Event() {
    fun getFov(): Float = _fov
    fun setFov(fov: Float) {
        _fov = fov
    }

    fun getEntity(): AbstractClientPlayer = _entity
}


package me.miki.shindo.management.event.impl

import me.miki.shindo.management.event.Event
import net.minecraft.entity.Entity

class EventRenderHitbox(
    private val _entity: Entity,
    private val _x: Double,
    private val _y: Double,
    private val _z: Double,
    private val _entityYaw: Float,
    private val _partialTicks: Float,
) : Event() {
    fun getEntity(): Entity = _entity

    fun getX(): Double = _x

    fun getY(): Double = _y

    fun getZ(): Double = _z

    fun getEntityYaw(): Float = _entityYaw

    fun getPartialTicks(): Float = _partialTicks
}

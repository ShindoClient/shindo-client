package me.miki.shindo.management.event.impl

import me.miki.shindo.management.event.Event
import net.minecraft.entity.Entity

class EventRenderHitbox(
    private val entity: Entity,
    private val x: Double,
    private val y: Double,
    private val z: Double,
    private val entityYaw: Float,
    private val partialTicks: Float,
) : Event() {
    fun getEntity(): Entity = entity

    fun getX(): Double = x

    fun getY(): Double = y

    fun getZ(): Double = z

    fun getEntityYaw(): Float = entityYaw

    fun getPartialTicks(): Float = partialTicks
}

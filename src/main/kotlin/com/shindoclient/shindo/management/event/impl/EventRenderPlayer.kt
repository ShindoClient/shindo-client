package com.shindoclient.shindo.management.event.impl

import com.shindoclient.shindo.management.event.Event
import net.minecraft.entity.Entity

class EventRenderPlayer(
    private val entity: Entity,
    private val x: Double,
    private val y: Double,
    private val z: Double,
    private val partialTicks: Float,
) : Event() {
    fun getEntity(): Entity = entity

    fun getX(): Double = x

    fun getY(): Double = y

    fun getZ(): Double = z

    fun getPartialTicks(): Float = partialTicks
}

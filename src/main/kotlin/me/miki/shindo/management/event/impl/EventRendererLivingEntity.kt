package me.miki.shindo.management.event.impl

import me.miki.shindo.management.event.Event
import net.minecraft.client.renderer.entity.RendererLivingEntity
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase

class EventRendererLivingEntity(
    private val _renderer: RendererLivingEntity<EntityLivingBase>,
    private val _entity: Entity,
    private val _x: Double,
    private val _y: Double,
    private val _z: Double
) : Event() {
    fun getRenderer(): RendererLivingEntity<EntityLivingBase> = _renderer
    fun getEntity(): Entity = _entity
    fun getX(): Double = _x
    fun getY(): Double = _y
    fun getZ(): Double = _z
}


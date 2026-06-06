package com.shindoclient.shindo.management.event.impl

import com.shindoclient.shindo.management.event.Event
import net.minecraft.client.renderer.entity.RendererLivingEntity
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase

class EventRendererLivingEntity(
    private val renderer: RendererLivingEntity<EntityLivingBase>,
    private val entity: Entity,
    private val x: Double,
    private val y: Double,
    private val z: Double,
) : Event() {
    fun getRenderer(): RendererLivingEntity<EntityLivingBase> = renderer

    fun getEntity(): Entity = entity

    fun getX(): Double = x

    fun getY(): Double = y

    fun getZ(): Double = z
}

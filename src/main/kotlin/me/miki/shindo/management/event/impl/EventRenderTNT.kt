package me.miki.shindo.management.event.impl

import me.miki.shindo.management.event.Event
import net.minecraft.client.renderer.entity.RenderTNTPrimed
import net.minecraft.entity.item.EntityTNTPrimed

class EventRenderTNT(
    private val tntRenderer: RenderTNTPrimed,
    private val entity: EntityTNTPrimed,
    private val x: Double,
    private val y: Double,
    private val z: Double,
    private val entityYaw: Float,
    private val partialTicks: Float,
) : Event() {
    fun getTntRenderer(): RenderTNTPrimed = tntRenderer

    fun getEntity(): EntityTNTPrimed = entity

    fun getX(): Double = x

    fun getY(): Double = y

    fun getZ(): Double = z

    fun getEntityYaw(): Float = entityYaw

    fun getPartialTicks(): Float = partialTicks
}

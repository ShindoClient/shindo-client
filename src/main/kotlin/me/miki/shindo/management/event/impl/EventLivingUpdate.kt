package me.miki.shindo.management.event.impl

import me.miki.shindo.management.event.Event
import net.minecraft.entity.EntityLivingBase

class EventLivingUpdate(
    private val _entity: EntityLivingBase,
) : Event() {
    fun getEntity(): EntityLivingBase = _entity
}

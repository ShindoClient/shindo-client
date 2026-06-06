package com.shindoclient.shindo.management.event.impl

import com.shindoclient.shindo.management.event.Event
import net.minecraft.entity.EntityLivingBase

class EventLivingUpdate(
    private val entity: EntityLivingBase,
) : Event() {
    fun getEntity(): EntityLivingBase = entity
}

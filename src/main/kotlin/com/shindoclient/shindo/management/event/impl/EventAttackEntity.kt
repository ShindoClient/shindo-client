package com.shindoclient.shindo.management.event.impl

import com.shindoclient.shindo.management.event.Event
import net.minecraft.entity.Entity

class EventAttackEntity(
    private val entity: Entity,
) : Event() {
    fun getEntity(): Entity = entity
}

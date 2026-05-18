package me.miki.shindo.management.event.impl

import me.miki.shindo.management.event.Event
import net.minecraft.entity.Entity

class EventDamageEntity(
    private val _entity: Entity,
) : Event() {
    fun getEntity(): Entity = _entity
}

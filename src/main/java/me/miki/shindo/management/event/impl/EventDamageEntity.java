package me.miki.shindo.management.event.impl;

import me.miki.shindo.management.event.Event;
import net.minecraft.entity.Entity;

public class EventDamageEntity extends Event {

    private final Entity entity;

    public EventDamageEntity(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }
}

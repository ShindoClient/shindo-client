package me.miki.shindo.management.event.impl;

import lombok.Getter;
import me.miki.shindo.management.event.Event;
import net.minecraft.entity.Entity;

@Getter
public class EventDamageEntity extends Event {

    private final Entity entity;

    public EventDamageEntity(Entity entity) {
        this.entity = entity;
    }

}

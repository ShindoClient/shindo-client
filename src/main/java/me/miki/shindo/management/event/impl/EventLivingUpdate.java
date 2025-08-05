package me.miki.shindo.management.event.impl;

import me.miki.shindo.management.event.Event;
import net.minecraft.entity.EntityLivingBase;

public class EventLivingUpdate extends Event {

    private final EntityLivingBase entity;

    public EventLivingUpdate(EntityLivingBase entity) {
        this.entity = entity;
    }

    public EntityLivingBase getEntity() {
        return entity;
    }
}
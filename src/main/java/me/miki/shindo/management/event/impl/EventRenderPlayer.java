package me.miki.shindo.management.event.impl;

import me.miki.shindo.management.event.Event;
import net.minecraft.entity.Entity;

public class EventRenderPlayer extends Event {

    private final Entity entity;
    private final double x;
    private final double y;
    private final double z;
    private final float partialTicks;

    public EventRenderPlayer(Entity entity, double x, double y, double z, float partialTicks) {
        this.entity = entity;
        this.x = x;
        this.y = y;
        this.z = z;
        this.partialTicks = partialTicks;
    }

    public Entity getEntity() {
        return entity;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public float getPartialTicks() {
        return partialTicks;
    }
}

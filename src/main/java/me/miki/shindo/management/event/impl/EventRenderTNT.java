package me.miki.shindo.management.event.impl;

import me.miki.shindo.management.event.Event;
import net.minecraft.client.renderer.entity.RenderTNTPrimed;
import net.minecraft.entity.item.EntityTNTPrimed;

public class EventRenderTNT extends Event {

    private final RenderTNTPrimed tntRenderer;
    private final EntityTNTPrimed entity;
    private final double x;
    private final double y;
    private final double z;
    private final float entityYaw;
    private final float partialTicks;

    public EventRenderTNT(RenderTNTPrimed tntRenderer, EntityTNTPrimed entity, double x, double y, double z, float entityYaw, float partialTicks) {
        this.tntRenderer = tntRenderer;
        this.entity = entity;
        this.x = x;
        this.y = y;
        this.z = z;
        this.entityYaw = entityYaw;
        this.partialTicks = partialTicks;
    }

    public RenderTNTPrimed getTntRenderer() {
        return tntRenderer;
    }

    public EntityTNTPrimed getEntity() {
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

    public float getEntityYaw() {
        return entityYaw;
    }

    public float getPartialTicks() {
        return partialTicks;
    }
}
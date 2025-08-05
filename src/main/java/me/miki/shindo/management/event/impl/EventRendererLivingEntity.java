package me.miki.shindo.management.event.impl;

import me.miki.shindo.management.event.Event;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

public class EventRendererLivingEntity extends Event {

    private final RendererLivingEntity<EntityLivingBase> renderer;
    private final Entity entity;
    private final double x;
    private final double y;
    private final double z;

    public EventRendererLivingEntity(RendererLivingEntity<EntityLivingBase> renderer, Entity entity, double x, double y, double z) {
        this.renderer = renderer;
        this.entity = entity;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public RendererLivingEntity<EntityLivingBase> getRenderer() {
        return renderer;
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
}
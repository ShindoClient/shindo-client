package me.miki.shindo.management.event.impl;

import me.miki.shindo.management.event.Event;

public class EventRender3D extends Event {

    private final float partialTicks;

    public EventRender3D(float partialTicks) {
        this.partialTicks = partialTicks;
    }

    public float getPartialTicks() {
        return partialTicks;
    }
}
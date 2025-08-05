package me.miki.shindo.management.event.impl;

import me.miki.shindo.management.event.Event;

public class EventRenderVisualizer extends Event {

    private final float partialTicks;

    public EventRenderVisualizer(float partialTicks) {
        this.partialTicks = partialTicks;
    }

    public float getPartialTicks() {
        return partialTicks;
    }
}

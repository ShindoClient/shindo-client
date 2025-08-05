package me.miki.shindo.management.event.impl;

import me.miki.shindo.management.event.Event;

public class EventRenderTooltip extends Event {

    private final float partialTicks;

    public EventRenderTooltip(float partialTicks) {
        this.partialTicks = partialTicks;
    }

    public float getPartialTicks() {
        return partialTicks;
    }
}

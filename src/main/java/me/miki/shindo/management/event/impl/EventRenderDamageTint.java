package me.miki.shindo.management.event.impl;

import me.miki.shindo.management.event.Event;

public class EventRenderDamageTint extends Event {

    private final float partialTicks;

    public EventRenderDamageTint(float partialTicks) {
        this.partialTicks = partialTicks;
    }

    public float getPartialTicks() {
        return partialTicks;
    }
}

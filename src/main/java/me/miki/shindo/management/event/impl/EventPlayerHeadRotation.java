package me.miki.shindo.management.event.impl;

import me.miki.shindo.management.event.Event;

public class EventPlayerHeadRotation extends Event {

    private final float yaw;
    private final float pitch;

    public EventPlayerHeadRotation(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }
}
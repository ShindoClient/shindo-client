package me.miki.shindo.management.event.impl;

import me.miki.shindo.management.event.Event;

public class EventHurtCamera extends Event {

    private float intensity;

    public EventHurtCamera() {
        this.intensity = 1.0F;
    }

    public float getIntensity() {
        return intensity;
    }

    public void setIntensity(float intensity) {
        this.intensity = intensity;
    }
}

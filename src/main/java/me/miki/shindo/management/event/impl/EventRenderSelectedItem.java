package me.miki.shindo.management.event.impl;

import me.miki.shindo.management.event.Event;

public class EventRenderSelectedItem extends Event {

    private final int color;

    public EventRenderSelectedItem(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }
}
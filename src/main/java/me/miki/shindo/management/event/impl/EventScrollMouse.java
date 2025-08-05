package me.miki.shindo.management.event.impl;

import me.miki.shindo.management.event.Event;

public class EventScrollMouse extends Event {

    private final int amount;

    public EventScrollMouse(int amount) {
        this.amount = amount;
    }

    public int getAmount() {
        return amount;
    }
}
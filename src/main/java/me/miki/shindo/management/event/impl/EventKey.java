package me.miki.shindo.management.event.impl;

import me.miki.shindo.management.event.Event;

public class EventKey extends Event {

    private int keyCode;

    public EventKey(int keyCode) {
        this.keyCode = keyCode;
    }

    public int getKeyCode() {
        return keyCode;
    }

    public void setKeyCode(int keyCode) {
        this.keyCode = keyCode;
    }
}

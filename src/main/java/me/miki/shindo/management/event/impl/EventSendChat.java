package me.miki.shindo.management.event.impl;

import me.miki.shindo.management.event.Event;

public class EventSendChat extends Event {

    private final String message;

    public EventSendChat(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}

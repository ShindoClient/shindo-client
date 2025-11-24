package me.miki.shindo.management.event.impl;

import lombok.Getter;
import me.miki.shindo.management.event.Event;

@Getter
public class EventClickMouse extends Event {

    private final int button;

    public EventClickMouse(int button) {
        this.button = button;
    }

}

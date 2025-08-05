package me.miki.shindo.management.event.impl;

import me.miki.shindo.management.event.Event;

public class EventJoinServer extends Event {

    private final String ip;

    public EventJoinServer(String ip) {
        this.ip = ip;
    }

    public String getIp() {
        return ip;
    }
}

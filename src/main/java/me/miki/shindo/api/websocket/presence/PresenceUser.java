package me.miki.shindo.api.websocket.presence;

import lombok.Data;

@Data
public final class PresenceUser {
    public final String uuid;
    public final String name;
    public final String accountType;
    public final long lastSeen;
}

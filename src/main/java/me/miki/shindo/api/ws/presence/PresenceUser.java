package me.miki.shindo.api.ws.presence;

import lombok.Data;

@Data
public final class PresenceUser {
    public final String uuid;
    public final String name;
    public final String accountType;
    public final long lastSeen;
}

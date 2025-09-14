package me.miki.shindo.api.ws.presence;

public final class PresenceUser {
    public final String uuid;
    public final String name;
    public final String accountType;
    public volatile long lastSeen;

    public PresenceUser(String uuid, String name, String accountType, long lastSeen) {
        this.uuid = uuid;
        this.name = name;
        this.accountType = accountType;
        this.lastSeen = lastSeen;
    }
}

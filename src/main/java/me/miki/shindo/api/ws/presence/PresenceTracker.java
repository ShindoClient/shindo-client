package me.miki.shindo.api.ws.presence;

import com.google.gson.JsonObject;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class PresenceTracker {
    private static final Map<String, PresenceUser> online = new ConcurrentHashMap<>();

    public static boolean isOnline(String uuid) {
        return online.containsKey(uuid);
    }

    public void handleMessage(String type, JsonObject payload) {
        if ("user.join".equals(type)) {
            String uuid = payload.get("uuid").getAsString();
            String name = payload.has("name") ? payload.get("name").getAsString() : "Unknown";
            String acct = payload.has("accountType") ? payload.get("accountType").getAsString() : "OFFLINE";
            online.put(uuid, new PresenceUser(uuid, name, acct, System.currentTimeMillis()));
        } else if ("user.leave".equals(type)) {
            String uuid = payload.get("uuid").getAsString();
            online.remove(uuid);
        }
    }

    public Set<String> allOnlineUuids() {
        return online.keySet();
    }

    public PresenceUser get(String uuid) {
        return online.get(uuid);
    }

    public void clear() {
        online.clear();
    }
}
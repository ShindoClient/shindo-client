package me.miki.shindo.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.miki.shindo.api.endpoints.CheckStatus;
import me.miki.shindo.api.endpoints.user.GetUser;
import me.miki.shindo.api.endpoints.user.PostUser;
import me.miki.shindo.logger.ShindoLogger;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ApiManager {

    private static final long CACHE_TTL_MS = 5000;
    private final String uuid;
    private final String name;
    private final String accountType;
    private final CheckStatus checkStatus;
    private final GetUser getUser;
    private final PostUser postUser;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Set<String> watchedUUIDs = ConcurrentHashMap.newKeySet();
    // CACHES
    private final Map<String, Boolean> onlineCache = new ConcurrentHashMap<>();
    private final Map<String, String> nameCache = new ConcurrentHashMap<>();
    private final Map<String, String> accountTypeCache = new ConcurrentHashMap<>();
    private final Map<String, Long> lastUpdateTimestamps = new ConcurrentHashMap<>();
    private final Map<String, JsonArray> privilegesCache = new ConcurrentHashMap<>();

    public ApiManager(String uuid, String name, String accountType) {

        this.uuid = uuid;
        this.name = name;
        this.accountType = accountType;

        this.checkStatus = new CheckStatus();
        this.postUser = new PostUser();
        this.getUser = new GetUser();

        startAutoUpdater();
    }

    public void notifyEvent(String eventType) {
        JsonObject json = new JsonObject();
        json.addProperty("uuid", uuid);
        json.addProperty("name", name);
        json.addProperty("accountType", accountType);
        json.addProperty("eventType", eventType);

        postUser.connect(json);
    }

    private boolean shouldUpdateCache(String uuid) {
        long now = System.currentTimeMillis();
        return !lastUpdateTimestamps.containsKey(uuid) || (now - lastUpdateTimestamps.get(uuid)) > CACHE_TTL_MS;
    }

    public void updateCache(String uuid) {
        if (!shouldUpdateCache(uuid)) return;

        try {
            JsonObject json = getUser.getUserInfo(uuid);
            if (json != null) {
                // Somente atualiza o cache se os dados forem válidos
                if (json.has("online")) {
                    onlineCache.put(uuid, json.get("online").getAsBoolean());
                }

                if (json.has("name") && !json.get("name").isJsonNull()) {
                    nameCache.put(uuid, json.get("name").getAsString());
                }

                if (json.has("accountType") && !json.get("accountType").isJsonNull()) {
                    accountTypeCache.put(uuid, json.get("accountType").getAsString());
                }

                if (json.has("privileges") && json.get("privileges").isJsonArray()) {
                    privilegesCache.put(uuid, json.getAsJsonArray("privileges"));
                }

                lastUpdateTimestamps.put(uuid, System.currentTimeMillis());
            }
        } catch (Exception e) {
            ShindoLogger.warn("[API] Falha ao atualizar cache do UUID " + uuid + ": " + e.getMessage());
        }
    }

    public boolean isOnline(String uuid) {
        watchUUID(uuid);
        return onlineCache.getOrDefault(uuid, false);
    }

    public String getName(String uuid) {
        watchUUID(uuid);
        return nameCache.get(uuid);
    }

    public String getAccountType(String uuid) {
        watchUUID(uuid);
        return accountTypeCache.get(uuid);
    }

    public boolean hasRole(String uuid, String privilegeKey) {
        watchUUID(uuid); // Garante que o cache esteja populado
        JsonArray privs = privilegesCache.get(uuid);

        if (privs == null) return false;

        for (int i = 0; i < privs.size(); i++) {
            if (privs.get(i).getAsString().equalsIgnoreCase(privilegeKey)) {
                return true;
            }
        }

        return false;
    }

    private void watchUUID(String uuid) {
        watchedUUIDs.add(uuid);
    }

    private void startAutoUpdater() {
        scheduler.scheduleAtFixedRate(() -> {
            for (String uuid : watchedUUIDs) {
                updateCache(uuid);
            }
        }, 0, 5, TimeUnit.SECONDS);
    }

    public void shutdown() {
        scheduler.shutdownNow();
    }
}
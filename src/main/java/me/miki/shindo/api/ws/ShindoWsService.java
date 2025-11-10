package me.miki.shindo.api.ws;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.Setter;
import me.miki.shindo.api.roles.RoleManager;
import me.miki.shindo.api.ws.presence.PresenceTracker;
import me.miki.shindo.logger.ShindoLogger;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicReference;

/**
 * PlayerInfo includes accountType ("MICROSOFT" | "OFFLINE").
 */
public class ShindoWsService {

    private static final Set<String> ALLOWED_ROLES =
            Collections.unmodifiableSet(new HashSet<>(Arrays.asList("STAFF", "DIAMOND", "GOLD", "MEMBER")));
    private static final String DEFAULT_ROLE = "MEMBER";

    private final CopyOnWriteArrayList<Listener> listeners = new CopyOnWriteArrayList<>();
    private final URI uri;
    private final boolean ssl;
    private final AtomicReference<WsClient> clientRef = new AtomicReference<>(null);
    private final AtomicReference<List<String>> lastRolesSent = new AtomicReference<>(Collections.emptyList());
    private final AtomicReference<AuthSession> sessionRef = new AtomicReference<>(AuthSession.empty());
    private final AtomicReference<String> lastUuidSent = new AtomicReference<>("");

    @Setter
    private PlayerInfoProvider provider;
    @Setter
    private PresenceTracker presenceTracker;
    @Setter
    private RoleManager roleManager;
    @Setter
    private SessionProvider sessionProvider;
    private ScheduledFuture<?> pingTask;

    public ShindoWsService(URI uri, boolean ssl) {
        this.uri = uri;
        this.ssl = ssl;
    }

    public void addListener(Listener l) {
        if (l != null) {
            listeners.add(l);
        }
    }

    // ========= Conexão =========
    public void connect() {
        WsClient c = new WsClient(uri, ssl);
        c.addListener(new WsClient.Listener() {
            @Override
            public void onOpen() {
                authenticate();
                for (Listener l : listeners) {
                    l.onOpen(null);
                }
            }

            @Override
            public void onMessage(String type, JsonObject payload) {
                handleServerMessage(type, payload);
                for (Listener l : listeners) {
                    l.onMessage(type, payload);
                }
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                for (Listener l : listeners) {
                    l.onClose(code, reason, remote);
                }
            }

            @Override
            public void onError(Exception ex) {
                for (Listener l : listeners) {
                    l.onError(ex);
                }
            }
        });

        clientRef.set(c);
        c.connect();
    }

    public void disconnect() {
        WsClient c = clientRef.getAndSet(null);
        if (c != null) {
            try {
                c.close();
            } catch (Exception ignored) {
            }
        }
    }

    public boolean isOpen() {
        WsClient c = clientRef.get();
        return c != null && c.isOpenAtomic();
    }

    // ========= Envio =========
    public void send(String type, JsonObject payload) {
        WsClient c = clientRef.get();
        if (c == null) {
            return;
        }
        JsonObject obj = (payload != null) ? payload : new JsonObject();
        obj.addProperty("type", type);
        c.sendJson(obj);
    }

    public void reauthenticate() {
        authenticate();
    }

    public void invalidateSession() {
        sessionRef.set(AuthSession.empty());
    }

    public void pushRoles(String[] roles) {
        String[] normalized = normalizeRoles(roles);
        List<String> normalizedList = Arrays.asList(normalized);

        if (lastRolesSent.get().equals(normalizedList)) {
            return;
        }

        JsonObject payload = new JsonObject();
        JsonArray array = new JsonArray();
        for (String role : normalized) {
            array.add(role);
        }
        payload.add("roles", array);

        send("roles.update", payload);
        lastRolesSent.set(normalizedList);
    }

    private void authenticate() {
        PlayerInfo current = fetchCurrentPlayer();
        if (current == null) {
            return;
        }

        AuthSession cached = sessionRef.get();
        if (cached != null && cached.isValid()) {
            sendAuthPayload(current, cached);
            return;
        }

        CompletableFuture<AuthSession> sessionFuture = (sessionProvider != null)
                ? sessionProvider.acquireSession(current).exceptionally(error -> {
                    ShindoLogger.error("Failed to acquire session token", error);
                    return AuthSession.empty();
                })
                : CompletableFuture.completedFuture(AuthSession.empty());

        sessionFuture.thenAccept(session -> {
            sessionRef.set(session != null ? session : AuthSession.empty());
            sendAuthPayload(current, session);
        });
    }

    private PlayerInfo fetchCurrentPlayer() {
        if (provider == null) {
            return null;
        }
        PlayerInfo raw = provider.player();
        if (raw == null) {
            return null;
        }
        return sanitizePlayer(raw);
    }

    private void sendAuthPayload(PlayerInfo info, AuthSession session) {
        if (info == null) {
            return;
        }

        String[] outgoingRoles = info.roles;
        if (session != null && session.roles != null && session.roles.length > 0) {
            outgoingRoles = normalizeRoles(session.roles);
        }

        JsonObject payload = new JsonObject();
        payload.addProperty("uuid", info.uuid);
        payload.addProperty("name", info.name);
        payload.addProperty("accountType", info.accountType);

        JsonArray rolesArr = new JsonArray();
        for (String role : outgoingRoles) {
            rolesArr.add(role);
        }
        payload.add("roles", rolesArr);

        if (session != null) {
            if (session.hasToken()) {
                payload.addProperty("token", session.token);
            }
            if (session.sessionId != null && !session.sessionId.isEmpty()) {
                payload.addProperty("sessionId", session.sessionId);
            }
        }

        lastRolesSent.set(Arrays.asList(outgoingRoles));
        lastUuidSent.set(info.uuid);

        send("auth", payload);
    }

    private void handleServerMessage(String type, JsonObject payload) {
        if (type == null) {
            return;
        }
        switch (type) {
            case "auth.ok": {
                AuthSession previous = sessionRef.get();
                String sessionId = previous != null ? previous.sessionId : null;
                long expiresAt = previous != null ? previous.expiresAt : -1L;
                String[] sessionRoles = previous != null ? previous.roles : null;

                if (payload != null) {
                    if (payload.has("sessionId")) {
                        String fromPayload = payload.get("sessionId").getAsString();
                        sessionId = (fromPayload != null && !fromPayload.isEmpty())
                                ? fromPayload
                                : (previous != null ? previous.sessionId : null);
                    }
                    if (payload.has("expiresAt")) {
                        try {
                            expiresAt = payload.get("expiresAt").getAsLong();
                        } catch (Exception ignored) {
                            expiresAt = previous != null ? previous.expiresAt : -1L;
                        }
                    }
                    if (payload.has("roles") && payload.get("roles").isJsonArray()) {
                        JsonArray arr = payload.getAsJsonArray("roles");
                        String[] roles = new String[arr.size()];
                        for (int i = 0; i < arr.size(); i++) {
                            roles[i] = arr.get(i).getAsString();
                        }
                        sessionRoles = normalizeRoles(roles);
                        lastRolesSent.set(Arrays.asList(sessionRoles));
                    }
                }

                sessionRef.set(AuthSession.of(
                        previous != null ? previous.token : null,
                        sessionId,
                        expiresAt,
                        sessionRoles
                ));
            }
            case "session.invalidate": {
                ShindoLogger.warn("Session invalidated by gateway; requesting new credentials");
                invalidateSession();
                reauthenticate();
            }
            default: {}
        }

        if (presenceTracker != null) {
            presenceTracker.handleMessage(type, payload);
        }
    }

    private PlayerInfo sanitizePlayer(PlayerInfo info) {
        String uuid = safeTrim(info.uuid);
        String name = safeTrim(info.name);
        String accountType = normalizeAccountType(info.accountType);
        String[] normalizedRoles = normalizeRoles(info.roles);
        return new PlayerInfo(uuid, name, normalizedRoles, accountType);
    }

    private static String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private static String normalizeAccountType(String value) {
        String normalized = safeTrim(value).toUpperCase();
        if ("MICROSOFT".equals(normalized)) {
            return "MICROSOFT";
        }
        return "OFFLINE";
    }

    private String[] normalizeRoles(String[] roles) {
        if (roles == null || roles.length == 0) {
            return new String[]{DEFAULT_ROLE};
        }

        HashSet<String> set = new HashSet<>();
        for (String role : roles) {
            String normalized = safeTrim(role).toUpperCase();
            if (ALLOWED_ROLES.contains(normalized)) {
                set.add(normalized);
            }
        }
        if (set.isEmpty()) {
            set.add(DEFAULT_ROLE);
        }
        return set.toArray(new String[0]);
    }

    // ========= Listener =========
    public interface Listener {
        default void onOpen(ServerHandshake handshake) {
        }

        default void onClose(int code, String reason, boolean remote) {
        }

        default void onError(Exception ex) {
        }

        default void onMessage(String type, JsonObject payload) {
        }
    }

    public interface PlayerInfoProvider {
        PlayerInfo player();
    }

    public interface SessionProvider {
        CompletableFuture<AuthSession> acquireSession(PlayerInfo info);
    }

    public static class PlayerInfo {
        public final String uuid;
        public final String name;
        public final String[] roles;
        public final String accountType;

        public PlayerInfo(String uuid, String name, String[] roles, String accountType) {
            this.uuid = uuid;
            this.name = name;
            this.roles = roles;
            this.accountType = accountType;
        }
    }

    public static class AuthSession {
        public final String token;
        public final String sessionId;
        public final long expiresAt;
        public final String[] roles;

        private AuthSession(String token, String sessionId, long expiresAt, String[] roles) {
            this.token = token;
            this.sessionId = sessionId;
            this.expiresAt = expiresAt;
            this.roles = roles;
        }

        public static AuthSession of(String token, String sessionId, long expiresAt, String[] roles) {
            return new AuthSession(token, sessionId, expiresAt, roles);
        }

        public static AuthSession empty() {
            return new AuthSession(null, null, -1L, null);
        }

        public boolean hasToken() {
            return token != null && !token.isEmpty();
        }

        public boolean isValid() {
            long now = System.currentTimeMillis();
            return hasToken() && expiresAt > 0 && expiresAt - 5000 > now;
        }
    }
}

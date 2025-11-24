package me.miki.shindo.api.ws;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.Setter;
import me.miki.shindo.api.roles.RoleManager;
import me.miki.shindo.api.ws.presence.PresenceTracker;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class ShindoWsService {

    private static final Set<String> ALLOWED_ROLES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("STAFF", "DIAMOND", "GOLD", "MEMBER")));
    private static final String DEFAULT_ROLE = "MEMBER";

    private final CopyOnWriteArrayList<Listener> listeners = new CopyOnWriteArrayList<>();
    private final URI uri;
    private final boolean ssl;
    private final AtomicReference<WsClient> clientRef = new AtomicReference<>(null);
    private final AtomicReference<List<String>> lastRolesSent = new AtomicReference<>(Collections.emptyList());

    @Setter
    private IdentityProvider provider;
    @Setter
    private PresenceTracker presenceTracker;
    @Setter
    private RoleManager roleManager;

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
                notifyListeners(listener -> listener.onOpen(null));
            }

            @Override
            public void onMessage(String type, JsonObject payload) {
                handleServerMessage(type, payload);
                notifyListeners(listener -> listener.onMessage(type, payload));
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                notifyListeners(listener -> listener.onClose(code, reason, remote));
            }

            @Override
            public void onError(Exception ex) {
                notifyListeners(listener -> listener.onError(ex));
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

        send(GatewayMessageType.ROLES_UPDATE.getType(), payload);
        lastRolesSent.set(normalizedList);
    }

    private void authenticate() {
        WsIdentity current = fetchCurrentPlayer();
        if (current == null) {
            return;
        }
        sendAuthPayload(current);
    }

    private WsIdentity fetchCurrentPlayer() {
        if (provider == null) {
            return null;
        }
        WsIdentity raw = provider.player();
        if (raw == null) {
            return null;
        }
        return sanitizeIdentity(raw);
    }

    private void sendAuthPayload(WsIdentity info) {
        if (info == null) {
            return;
        }

        String[] outgoingRoles = normalizeRoles(info.getRoles());

        JsonObject payload = new JsonObject();
        payload.addProperty("uuid", info.getUuid());
        payload.addProperty("name", info.getName());
        payload.addProperty("accountType", info.getAccountType().getWireValue());

        JsonArray rolesArr = new JsonArray();
        for (String role : outgoingRoles) {
            rolesArr.add(role);
        }
        payload.add("roles", rolesArr);

        lastRolesSent.set(Arrays.asList(outgoingRoles));

        send(GatewayMessageType.AUTH.getType(), payload);
    }

    private void handleServerMessage(String type, JsonObject payload) {
        if (type == null) {
            return;
        }
        if (GatewayMessageType.AUTH_OK.matches(type) && payload != null && payload.has("roles") && payload.get("roles").isJsonArray()) {
            JsonArray arr = payload.getAsJsonArray("roles");
            String[] roles = new String[arr.size()];
            for (int i = 0; i < arr.size(); i++) {
                roles[i] = arr.get(i).getAsString();
            }
            lastRolesSent.set(Arrays.asList(normalizeRoles(roles)));
        }

        if (presenceTracker != null) {
            presenceTracker.handleMessage(type, payload);
        }
    }

    private WsIdentity sanitizeIdentity(WsIdentity info) {
        String uuid = safeTrim(info.getUuid());
        String name = safeTrim(info.getName());
        AccountType accountType = info.getAccountType() != null ? info.getAccountType() : AccountType.LOCAL;
        String[] normalizedRoles = normalizeRoles(info.getRoles());
        return new WsIdentity(uuid, name, normalizedRoles, accountType);
    }

    private static String safeTrim(String value) {
        return value == null ? "" : value.trim();
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

    private void notifyListeners(Consumer<Listener> consumer) {
        for (Listener listener : listeners) {
            try {
                consumer.accept(listener);
            } catch (Exception ignored) {
            }
        }
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

    public interface IdentityProvider {
        WsIdentity player();
    }
}

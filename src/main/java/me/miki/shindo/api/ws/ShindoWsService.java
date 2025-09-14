package me.miki.shindo.api.ws;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.Setter;
import me.miki.shindo.api.roles.RoleManager;
import me.miki.shindo.api.ws.presence.PresenceTracker;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * PlayerInfo includes accountType ("MICROSOFT" | "OFFLINE").
 */
public class ShindoWsService {

    private final CopyOnWriteArrayList<Listener> listeners = new CopyOnWriteArrayList<>();
    private final URI uri;
    private final boolean ssl;
    private final AtomicReference<WsClient> clientRef = new AtomicReference<>(null);

    @Setter
    private PlayerInfoProvider provider;
    @Setter
    private PresenceTracker presenceTracker;
    @Setter
    private RoleManager roleManager;
    private ScheduledFuture<?> pingTask;

    public ShindoWsService(URI uri, boolean ssl) {
        this.uri = uri;
        this.ssl = ssl;
    }

    public void addListener(Listener l) {
        if (l != null) listeners.add(l);
    }

    // ========= Conexão =========
    public void connect() {
        WsClient c = new WsClient(uri, ssl);
        c.addListener(new WsClient.Listener() {
            @Override
            public void onOpen() {
                sendAuth();
                for (Listener l : listeners) l.onOpen(null);
            }

            @Override
            public void onMessage(String type, JsonObject payload) {
                for (Listener l : listeners) l.onMessage(type, payload);
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                for (Listener l : listeners) l.onClose(code, reason, remote);
            }

            @Override
            public void onError(Exception ex) {
                for (Listener l : listeners) l.onError(ex);
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
        if (c == null) return;
        JsonObject obj = (payload != null) ? payload : new JsonObject();
        obj.addProperty("type", type);
        c.sendJson(obj);
    }

    // ========= Auth =========
    private void sendAuth() {
        if (provider == null) return;
        PlayerInfo p = provider.player();
        if (p == null) return;

        JsonObject auth = new JsonObject();
        auth.addProperty("type", "auth");
        auth.addProperty("uuid", p.uuid);
        auth.addProperty("name", p.name);
        auth.addProperty("accountType", p.accountType != null ? p.accountType : "OFFLINE");

        // roles com fallback MEMBER
        JsonArray arr = new JsonArray();
        String[] rs = (p.roles == null || p.roles.length == 0) ? new String[]{"MEMBER"} : p.roles;
        for (String r : rs) arr.add(r);
        auth.add("roles", arr);

        send("auth", auth);
    }

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
}
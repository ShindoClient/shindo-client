package me.miki.shindo.api.websocket;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import lombok.Setter;
import me.miki.shindo.api.roles.RoleManager;
import me.miki.shindo.api.websocket.message.MessageHandler;
import me.miki.shindo.api.websocket.message.MessageType;
import me.miki.shindo.api.websocket.presence.PresenceTracker;
import me.miki.shindo.logger.ShindoLogger;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Cliente WebSocket de alto nível para o ecossistema Shindo.
 *
 * Responsabilidades principais:
 * <ul>
 *     <li>Gerenciar conexão/reconexão com backoff exponencial.</li>
 *     <li>Enviar payloads de autenticação e atualização de roles.</li>
 *     <li>Manter heartbeats e reagir a timeouts.</li>
 *     <li>Delegar processamento de mensagens a {@link MessageHandler}.</li>
 * </ul>
 */
public class ShindoWebsocket {

    private static final Set<String> ALLOWED_ROLES =
            Collections.unmodifiableSet(new HashSet<>(Arrays.asList("STAFF", "DIAMOND", "GOLD", "MEMBER")));
    private static final String DEFAULT_ROLE = "MEMBER";

    private static final long HEARTBEAT_INTERVAL_MS = 20_000L;
    private static final long HEARTBEAT_TIMEOUT_MS = 45_000L;
    private static final long RECONNECT_BASE_MS = 2_000L;
    private static final long RECONNECT_MAX_MS = 30_000L;

    private final CopyOnWriteArrayList<Listener> listeners = new CopyOnWriteArrayList<>();
    private final URI uri;
    private final boolean ssl;
    private final AtomicReference<WsClient> clientRef = new AtomicReference<>(null);
    private final AtomicReference<List<String>> lastRolesSent = new AtomicReference<>(Collections.emptyList());
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "shindo-ws");
        t.setDaemon(true);
        return t;
    });
    private final AtomicBoolean stopRequested = new AtomicBoolean(false);
    private final AtomicInteger reconnectAttempts = new AtomicInteger(0);
    private final AtomicLong lastHeartbeatAck = new AtomicLong(0L);
    private final AtomicReference<ScheduledFuture<?>> heartbeatFuture = new AtomicReference<>(null);
    private final AtomicReference<ScheduledFuture<?>> reconnectFuture = new AtomicReference<>(null);

    @Getter
    private final MessageHandler messageHandler;

    @Setter
    private IdentityProvider provider;
    @Setter
    private PresenceTracker presenceTracker;
    @Setter
    private RoleManager roleManager;

    public ShindoWebsocket(URI uri, boolean ssl) {
        this.uri = uri;
        this.ssl = ssl;
        this.messageHandler = new MessageHandler(null);
    }

    public ShindoWebsocket(URI uri, boolean ssl, PresenceTracker presenceTracker) {
        this.uri = uri;
        this.ssl = ssl;
        this.presenceTracker = presenceTracker;
        this.messageHandler = new MessageHandler(presenceTracker);
    }

    public void addListener(Listener l) {
        if (l != null) {
            listeners.add(l);
        }
    }

    // ========= Conexão =========

    public void connect() {
        stopRequested.set(false);
        reconnectAttempts.set(0);
        cancelReconnect();
        //ShindoLogger.info("ShindoWebsocket.connect() called for URI: " + uri);
        establishClient();
    }

    public void disconnect() {
        stopRequested.set(true);
        stopHeartbeat();
        cancelReconnect();
        lastRolesSent.set(Collections.emptyList());
        lastHeartbeatAck.set(0L);
        safeClose(clientRef.getAndSet(null));
    }

    public boolean isOpen() {
        WsClient c = clientRef.get();
        return c != null && c.isOpenAtomic();
    }

    // ========= Envio =========

    public void send(MessageType type, JsonObject payload) {
        WsClient c = clientRef.get();
        if (c == null || type == null) {
            return;
        }
        JsonObject obj = (payload != null) ? payload : new JsonObject();
        obj.addProperty("type", type.getWireType());
        //ShindoLogger.info("[WS-OUT] type=" + type.getWireType() + " payload=" + obj);
        c.sendJson(obj);
    }

    public void reauthenticate() {
        if (stopRequested.get()) {
            return;
        }
        if (!isOpen()) {
            scheduleReconnect("reauth_requested");
            return;
        }
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

        send(MessageType.ROLES_UPDATE, payload);
        lastRolesSent.set(normalizedList);
    }

    private void establishClient() {
        if (stopRequested.get()) {
            return;
        }

        WsClient existing = clientRef.get();
        if (existing != null && existing.isOpenAtomic()) {
            return;
        }
        safeClose(existing);

        WsClient c = new WsClient(uri, ssl);
        c.addListener(new WsClient.Listener() {
            @Override
            public void onOpen() {
                //ShindoLogger.info("ShindoWebsocket: WebSocket opened");
                lastHeartbeatAck.set(System.currentTimeMillis());
                reconnectAttempts.set(0);
                cancelReconnect();
                authenticate();
                startHeartbeat();
                notifyListeners(listener -> listener.onOpen(null));
            }

            @Override
            public void onMessage(String type, JsonObject payload) {
                handleServerMessage(type, payload);
                notifyListeners(listener -> listener.onMessage(type, payload));
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                //ShindoLogger.warn("ShindoWebsocket: WebSocket closed code=" + code + " reason=" + reason + " remote=" + remote);
                stopHeartbeat();
                clientRef.compareAndSet(c, null);
                if (!stopRequested.get()) {
                    scheduleReconnect("close:" + code);
                }
                notifyListeners(listener -> listener.onClose(code, reason, remote));
            }

            @Override
            public void onError(Exception ex) {
                //ShindoLogger.error("ShindoWebsocket: WebSocket error", ex);
                notifyListeners(listener -> listener.onError(ex));
                if (!stopRequested.get()) {
                    scheduleReconnect("error");
                }
            }
        });

        clientRef.set(c);
        c.connect();
    }

    private void authenticate() {
        if (stopRequested.get()) {
            return;
        }
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

        //ShindoLogger.info("[WS-AUTH] sending auth payload: " + payload);
        send(MessageType.AUTH, payload);
    }

    private void handleServerMessage(String rawType, JsonObject payload) {
        MessageType type = MessageType.fromWire(rawType);
        //ShindoLogger.info("[WS-IN ] type=" + rawType + " resolved=" + type + " payload=" + payload);

        if (type != MessageType.UNKNOWN) {
            recordHeartbeat();
        }

        if (type == MessageType.PONG) {
            return;
        }

        if (type == MessageType.SERVER_KEEPALIVE) {
            // Servidor está enviando keepalive; respondemos com um ping para confirmar que estamos vivos.
            send(MessageType.PING, new JsonObject());
            return;
        }

        if (type == MessageType.SERVER_VERIFY) {
            // Servidor está pedindo uma confirmação extra; respondemos com um ping imediato.
            send(MessageType.PING, new JsonObject());
            return;
        }

        if (type == MessageType.AUTH_OK && payload != null && payload.has("roles") && payload.get("roles").isJsonArray()) {
            JsonArray arr = payload.getAsJsonArray("roles");
            String[] roles = new String[arr.size()];
            for (int i = 0; i < arr.size(); i++) {
                roles[i] = arr.get(i).getAsString();
            }
            lastRolesSent.set(Arrays.asList(normalizeRoles(roles)));
        }

        if (presenceTracker != null && messageHandler != null) {
            messageHandler.handle(rawType, payload);
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

    private void startHeartbeat() {
        stopHeartbeat();
        heartbeatFuture.set(scheduler.scheduleAtFixedRate(() -> {
            WsClient client = clientRef.get();
            if (client == null || !client.isOpenAtomic() || stopRequested.get()) {
                return;
            }

            long now = System.currentTimeMillis();
            long lastAck = lastHeartbeatAck.get();
            if (lastAck > 0 && now - lastAck > HEARTBEAT_TIMEOUT_MS) {
                safeClose(client);
                scheduleReconnect("heartbeat_timeout");
                return;
            }

            send(MessageType.PING, new JsonObject());
        }, HEARTBEAT_INTERVAL_MS, HEARTBEAT_INTERVAL_MS, TimeUnit.MILLISECONDS));
    }

    private void stopHeartbeat() {
        ScheduledFuture<?> future = heartbeatFuture.getAndSet(null);
        if (future != null) {
            future.cancel(true);
        }
    }

    private void scheduleReconnect(String reason) {
        if (stopRequested.get()) {
            return;
        }
        ScheduledFuture<?> existing = reconnectFuture.get();
        if (existing != null && !existing.isDone()) {
            return;
        }

        int attempt = Math.max(1, reconnectAttempts.incrementAndGet());
        long delay = Math.min(RECONNECT_MAX_MS, (long) (RECONNECT_BASE_MS * Math.pow(2, attempt - 1)));
        reconnectFuture.set(scheduler.schedule(this::establishClient, delay, TimeUnit.MILLISECONDS));
    }

    private void cancelReconnect() {
        ScheduledFuture<?> future = reconnectFuture.getAndSet(null);
        if (future != null) {
            future.cancel(true);
        }
    }

    private void recordHeartbeat() {
        lastHeartbeatAck.set(System.currentTimeMillis());
    }

    private void safeClose(WsClient client) {
        if (client == null) {
            return;
        }
        try {
            client.close();
        } catch (Exception ignored) {
        }
    }

    private void notifyListeners(Consumer<Listener> consumer) {
        for (Listener listener : listeners) {
            try {
                consumer.accept(listener);
            } catch (Exception ignored) {
            }
        }
    }

    // ========= Listener / Observer =========

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

    /**
     * Implementação de baixo nível do cliente WebSocket.
     * Esta classe fica encapsulada dentro de {@link ShindoWebsocket} para evitar
     * vazamento de detalhes de implementação para o restante do client.
     */
     private static final class WsClient extends WebSocketClient {

        private final List<Listener> listeners = new CopyOnWriteArrayList<>();
        private final AtomicBoolean open = new AtomicBoolean(false);
        private final Queue<JsonObject> outbox = new ConcurrentLinkedQueue<>();

        private WsClient(URI serverUri, boolean ssl) {
            super(serverUri);
            if (ssl && serverUri.toString().startsWith("wss://")) {
                try {
                    SSLContext context = SSLContext.getInstance("TLS");
                    context.init(null, null, null);
                    SSLSocketFactory factory = context.getSocketFactory();
                    setSocketFactory(factory);
                } catch (Exception ignored) {
                }
            }
            setConnectionLostTimeout(0);
        }

        private void addListener(Listener l) {
            if (l != null) listeners.add(l);
        }

        private boolean isOpenAtomic() {
            return open.get();
        }

        private void sendJson(JsonObject json) {
            if (json == null) return;
            if (isOpen()) {
                super.send(json.toString());
            } else {
                outbox.offer(json);
            }
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {
            open.set(true);
            while (!outbox.isEmpty()) {
                JsonObject o = outbox.poll();
                if (o != null) super.send(o.toString());
            }
            for (Listener l : listeners) {
                try {
                    l.onOpen();
                } catch (Exception ignored) {
                }
            }
        }

        @Override
        public void onMessage(String message) {
            if (message == null) return;
            try {
                JsonObject obj = JsonParser.parseString(message).getAsJsonObject();
                String type = obj.has("type") ? obj.get("type").getAsString() : "unknown";
                for (Listener l : listeners) {
                    try {
                        l.onMessage(type, obj);
                    } catch (Exception ignored) {
                    }
                }
            } catch (Exception ignored) {
            }
        }

        @Override
        public void onMessage(ByteBuffer bytes) {
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            open.set(false);
            for (Listener l : listeners) {
                try {
                    l.onClose(code, reason, remote);
                } catch (Exception ignored) {
                }
            }
        }

        @Override
        public void onError(Exception ex) {
            for (Listener l : listeners) {
                try {
                    l.onError(ex);
                } catch (Exception ignored) {
                }
            }
        }

        private interface Listener {
            void onOpen();

            void onMessage(String type, JsonObject payload);

            void onClose(int code, String reason, boolean remote);

            void onError(Exception ex);
        }
    }
}



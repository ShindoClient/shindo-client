package me.miki.shindo.api.ws;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class WsClient extends WebSocketClient {

    private final List<Listener> listeners = new ArrayList<>();
    private final AtomicBoolean open = new AtomicBoolean(false);
    private final Queue<JsonObject> outbox = new ConcurrentLinkedQueue<>();

    public WsClient(URI serverUri, boolean ssl) {
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
        setConnectionLostTimeout(20);
    }

    public void addListener(Listener l) {
        if (l != null) listeners.add(l);
    }

    public boolean isOpenAtomic() {
        return open.get();
    }

    public void sendJson(JsonObject json) {
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

    public interface Listener {
        void onOpen();

        void onMessage(String type, JsonObject payload);

        void onClose(int code, String reason, boolean remote);

        void onError(Exception ex);
    }
}

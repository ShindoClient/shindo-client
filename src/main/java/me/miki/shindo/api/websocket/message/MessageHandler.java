package me.miki.shindo.api.websocket.message;

import com.google.gson.JsonObject;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.miki.shindo.api.websocket.presence.PresenceTracker;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;

import static me.miki.shindo.api.websocket.message.MessageType.*;

/**
 * Orquestra o processamento de mensagens recebidas do gateway WebSocket.
 * - Normaliza o {@code type} em {@link MessageType}
 * - Aplica validação de segurança via {@link MessageValidator}
 * - Notifica observadores internos (ex.: {@link PresenceTracker}) e externos
 */
@RequiredArgsConstructor
public class MessageHandler {

    private final PresenceTracker presenceTracker;

    private final List<BiConsumer<MessageType, JsonObject>> observers = new CopyOnWriteArrayList<>();

    /**
     * Registra um observer externo que será notificado para cada mensagem
     * válida recebida do gateway.
     */
    public void addObserver(@NonNull BiConsumer<MessageType, JsonObject> observer) {
        observers.add(observer);
    }

    /**
     * Processa uma mensagem recebida do servidor.
     *
     * @param rawType tipo cru vindo do JSON
     * @param payload payload JSON (pode ser {@code null})
     */
    public void handle(String rawType, JsonObject payload) {
        MessageType type = MessageType.fromWire(rawType);

        if (!MessageValidator.isValid(type, payload)) {
            // Mensagem rejeitada por questões de segurança/estrutura.
            return;
        }

        // Primeiro, roteia para handlers internos (presença, etc.).
        routeInternal(type, payload);

        // Depois, notifica observers externos interessados.
        for (BiConsumer<MessageType, JsonObject> observer : observers) {
            try {
                observer.accept(type, payload);
            } catch (Exception ignored) {
                // Observers não devem quebrar o fluxo principal.
            }
        }
    }

    private void routeInternal(MessageType type, JsonObject payload) {
        if (presenceTracker == null) {
            return;
        }

        switch (type) {
            case USER_JOIN:
            case USER_LEAVE:
            case USER_ROLES:
                presenceTracker.handleMessage(type.getWireType(), payload);
                break;
            default:
                // Demais mensagens não impactam presença diretamente.
        }
    }
}



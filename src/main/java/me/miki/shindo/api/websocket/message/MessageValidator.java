package me.miki.shindo.api.websocket.message;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.UUID;

import static me.miki.shindo.api.websocket.message.MessageType.*;

/**
 * Validações de segurança e integridade para mensagens recebidas do gateway.
 * Mantém as regras de negócio e de segurança isoladas em um único ponto,
 * facilitando futuras auditorias e hardening.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MessageValidator {

    /**
     * Verifica se uma mensagem recebida é estruturalmente válida e segura o suficiente
     * para ser processada de acordo com o {@link MessageType}.
     *
     * @param type    tipo já normalizado
     * @param payload corpo JSON recebido (pode ser {@code null})
     * @return {@code true} se a mensagem é considerada válida
     */
    public static boolean isValid(MessageType type, JsonObject payload) {
        if (type == null) {
            return false;
        }

        // Mensagens sem payload são aceitáveis para alguns tipos.
        if (payload == null) {
            return type == PONG || type == SERVER_KEEPALIVE || type == SERVER_VERIFY || type == UNKNOWN;
        }

        switch (type) {
            case AUTH_OK:
            case AUTH_ERROR:
                // Deve conter pelo menos uuid (quando possível) e opcionalmente roles.
                return hasString(payload, "uuid");

            case USER_JOIN:
            case USER_LEAVE:
            case USER_ROLES:
                return hasNonEmptyUuid(payload);

            case PING:
            case PONG:
            case SERVER_KEEPALIVE:
            case SERVER_VERIFY:
                // Sem requisitos fortes; já foi autenticado.
                return true;

            case ROLES_UPDATE:
                // Apenas confere que o campo roles, se existir, é um array.
                JsonElement roles = payload.get("roles");
                return roles == null || roles.isJsonArray();

            case AUTH:
                // O client nunca deveria receber AUTH do servidor.
                return false;

            case UNKNOWN:
            default:
                // Por padrão bloqueamos mensagens desconhecidas.
                return false;
        }
    }

    private static boolean hasString(JsonObject obj, String key) {
        JsonElement el = obj.get(key);
        return el != null && el.isJsonPrimitive() && el.getAsJsonPrimitive().isString()
                && !el.getAsString().trim().isEmpty();
    }

    private static boolean hasNonEmptyUuid(JsonObject obj) {
        if (!hasString(obj, "uuid")) {
            return false;
        }
        try {
            UUID.fromString(obj.get("uuid").getAsString());
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }
}



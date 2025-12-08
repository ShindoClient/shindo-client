package me.miki.shindo.api.websocket.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Tipos de mensagem suportados pelo gateway WebSocket da Shindo.
 * Esta enum encapsula os {@code type} usados no protocolo JSON,
 * oferecendo uma API type-safe em vez de lidar com {@link String} soltos.
 */
@Getter
@RequiredArgsConstructor
public enum MessageType {

    AUTH("auth"),
    AUTH_OK("auth.ok"),
    AUTH_ERROR("auth.error"),

    ROLES_UPDATE("roles.update"),

    USER_JOIN("user.join"),
    USER_LEAVE("user.leave"),
    USER_ROLES("user.roles"),

    PING("ping"),
    PONG("pong"),
    SERVER_KEEPALIVE("server.keepalive"),
    SERVER_VERIFY("server.verify"),

    // Telemetria/diagnósticos específicos do client
    WARP_STATUS("warp.status"),

    UNKNOWN("unknown");

    private final String wireType;

    /**
     * Converte o valor cru do campo {@code type} para um {@link MessageType}
     * conhecido. Caso não reconheça o valor, retorna {@link #UNKNOWN}.
     */
    public static MessageType fromWire(String rawType) {
        if (rawType == null || rawType.isEmpty()) {
            return UNKNOWN;
        }
        String normalized = rawType.trim();
        for (MessageType value : values()) {
            if (value.wireType.equalsIgnoreCase(normalized)) {
                return value;
            }
        }
        return UNKNOWN;
    }
}



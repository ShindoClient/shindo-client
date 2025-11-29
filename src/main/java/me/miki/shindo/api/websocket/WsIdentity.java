package me.miki.shindo.api.websocket;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * Representa a identidade atual do jogador usada para autenticação no gateway WebSocket.
 *
 * Esta classe é imutável e preparada para ser usada em logs/debugging.
 */
@Getter
@ToString
@AllArgsConstructor
public class WsIdentity {

    /**
     * UUID do jogador. Pode ser um UUID online (Mojang/Microsoft) ou um UUID offline
     * derivado do nickname.
     */
    private final String uuid;

    /**
     * Nome do jogador exibido no gateway.
     */
    private final String name;

    /**
     * Lista de roles enviadas ao gateway para resolução de permissões.
     */
    private final String[] roles;

    /**
     * Tipo de conta (LOCAL, MICROSOFT, OFFLINE, etc).
     */
    private final AccountType accountType;
}


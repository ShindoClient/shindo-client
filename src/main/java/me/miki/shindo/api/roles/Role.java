package me.miki.shindo.api.roles;

import lombok.Getter;

/**
 * Representa os níveis de permissão de um usuário no client.
 *
 * A enum já codifica a hierarquia de acesso via {@link #priority},
 * evitando map externos para ordenação/comparação.
 */
@Getter
public enum Role {
    MEMBER(1),
    GOLD(2),
    DIAMOND(3),
    STAFF(4);

    private final int priority;

    Role(int priority) {
        this.priority = priority;
    }

    /**
     * Retorna o maior nível de acesso entre dois roles.
     */
    public static Role max(Role a, Role b) {
        if (a == null) return b != null ? b : MEMBER;
        if (b == null) return a;
        return a.priority >= b.priority ? a : b;
    }

    /**
     * Verifica se {@code have} é pelo menos tão alto quanto {@code required}.
     */
    public static boolean atLeast(Role have, Role required) {
        if (required == null) return true;
        if (have == null) return false;
        return have.priority >= required.priority;
    }
}


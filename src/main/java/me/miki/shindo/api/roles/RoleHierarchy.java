package me.miki.shindo.api.roles;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collection;

/**
 * Utilitários para trabalhar com hierarquia de {@link Role}.
 *
 * A hierarquia em si é definida dentro da própria enum ({@link Role#getPriority()}).
 * Esta classe provê helpers de alto nível para coleções de roles.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RoleHierarchy {

    public static int rank(Role r) {
        return r != null ? r.getPriority() : 0;
    }

    public static boolean atLeast(Role have, Role required) {
        return Role.atLeast(have, required);
    }

    public static Role highest(Collection<Role> roles) {
        Role best = Role.MEMBER;
        if (roles == null || roles.isEmpty()) return best;
        for (Role r : roles) best = Role.max(best, r);
        return best;
    }

    public static boolean hasAtLeast(Collection<Role> roles, Role required) {
        return atLeast(highest(roles), required);
    }
}

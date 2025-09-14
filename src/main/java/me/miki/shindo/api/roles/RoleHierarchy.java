package me.miki.shindo.api.roles;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

public final class RoleHierarchy {
    private static final Map<Role, Integer> RANK = new EnumMap<>(Role.class);

    static {
        RANK.put(Role.MEMBER, 1);
        RANK.put(Role.GOLD, 2);
        RANK.put(Role.DIAMOND, 3);
        RANK.put(Role.STAFF, 4);
    }

    private RoleHierarchy() {
    }

    public static int rank(Role r) {
        return RANK.getOrDefault(r, 0);
    }

    public static boolean atLeast(Role have, Role required) {
        return rank(have) >= rank(required);
    }

    public static Role highest(Collection<Role> roles) {
        Role best = Role.MEMBER;
        if (roles == null || roles.isEmpty()) return best;
        for (Role r : roles) if (rank(r) > rank(best)) best = r;
        return best;
    }

    public static boolean hasAtLeast(Collection<Role> roles, Role required) {
        return atLeast(highest(roles), required);
    }
}

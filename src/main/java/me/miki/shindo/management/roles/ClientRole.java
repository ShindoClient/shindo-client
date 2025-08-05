package me.miki.shindo.management.roles;

import java.util.EnumSet;
import java.util.Set;

public enum ClientRole {
    MEMBER,
    GOLD,
    DIAMOND,
    STAFF;

    public static boolean hasPermission(ClientRole actual, ClientRole required) {
        return actual.inheritedRoles().contains(required);
    }

    public Set<ClientRole> inheritedRoles() {
        Set<ClientRole> inherited = EnumSet.noneOf(ClientRole.class);
        for (ClientRole role : values()) {
            if (this.ordinal() >= role.ordinal()) {
                inherited.add(role);
            }
        }
        return inherited;
    }

    public boolean hasPermission(ClientRole required) {
        return inheritedRoles().contains(required);
    }
}
package me.miki.shindo.api.roles;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class RoleManager {
    private static final Map<UUID, Set<Role>> roles = new ConcurrentHashMap<>();
    private final List<Consumer<UUID>> listeners = new ArrayList<>();

    public static Set<Role> getDirectRoles(UUID uuid) {
        return roles.getOrDefault(uuid, Collections.singleton(Role.MEMBER));
    }

    // helpers
    public static boolean hasRole(UUID uuid, Role role) {
        return getDirectRoles(uuid).contains(role);
    }

    public static boolean hasAtLeast(UUID uuid, Role required) {
        return RoleHierarchy.hasAtLeast(getDirectRoles(uuid), required);
    }

    public void setRoles(UUID uuid, Set<Role> newRoles) {
        if (uuid == null) return;
        Set<Role> copy = EnumSet.noneOf(Role.class);
        if (newRoles != null) copy.addAll(newRoles);
        if (copy.isEmpty()) copy.add(Role.MEMBER);
        roles.put(uuid, copy);
        notifyChange(uuid);
    }

    public void addRole(UUID uuid, Role role) {
        roles.compute(uuid, (k, v) -> {
            Set<Role> s = (v == null) ? EnumSet.noneOf(Role.class) : EnumSet.copyOf(v);
            s.add(role);
            return s;
        });
        notifyChange(uuid);
    }

    public void removeRole(UUID uuid, Role role) {
        roles.computeIfPresent(uuid, (k, v) -> {
            Set<Role> s = EnumSet.copyOf(v);
            s.remove(role);
            if (s.isEmpty()) s.add(Role.MEMBER);
            return s;
        });
        notifyChange(uuid);
    }

    // listeners
    public void onChange(Consumer<UUID> l) {
        listeners.add(l);
    }

    private void notifyChange(UUID u) {
        for (Consumer<UUID> l : listeners) l.accept(u);
    }
}

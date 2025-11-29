package me.miki.shindo.api.roles;

import lombok.AccessLevel;
import lombok.Getter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Gerenciador central de roles do client.
 *
 * Responsabilidades:
 * <ul>
 *     <li>Manter um cache em memória das roles por UUID.</li>
 *     <li>Fornecer helpers estáticos para consultas rápidas (hasRole / hasAtLeast).</li>
 *     <li>Notificar listeners quando um jogador tiver suas roles alteradas.</li>
 * </ul>
 */
public class RoleManager {

    /**
     * Mapa global de roles por jogador. Mantém sempre ao menos {@link Role#MEMBER}.
     */
    @Getter(AccessLevel.PACKAGE)
    private static final Map<UUID, Set<Role>> roles = new ConcurrentHashMap<>();

    private final List<Consumer<UUID>> listeners = new ArrayList<>();

    /**
     * Retorna o conjunto de roles "diretas" para um jogador.
     * Nunca retorna {@code null} e sempre contém pelo menos {@link Role#MEMBER}.
     */
    public static Set<Role> getDirectRoles(UUID uuid) {
        if (uuid == null) return EnumSet.of(Role.MEMBER);
        Set<Role> existing = roles.get(uuid);
        if (existing == null || existing.isEmpty()) {
            return EnumSet.of(Role.MEMBER);
        }
        return EnumSet.copyOf(existing);
    }

    // helpers

    public static boolean hasRole(UUID uuid, Role role) {
        if (role == null) return false;
        return getDirectRoles(uuid).contains(role);
    }

    public static boolean hasAtLeast(UUID uuid, Role required) {
        return RoleHierarchy.hasAtLeast(getDirectRoles(uuid), required);
    }

    /**
     * Define o conjunto de roles para um jogador.
     * Garante que {@link Role#MEMBER} esteja presente se o conjunto for vazio.
     */
    public void setRoles(UUID uuid, Set<Role> newRoles) {
        if (uuid == null) return;
        EnumSet<Role> copy = EnumSet.noneOf(Role.class);
        if (newRoles != null) copy.addAll(newRoles);
        if (copy.isEmpty()) copy.add(Role.MEMBER);
        roles.put(uuid, copy);
        notifyChange(uuid);
    }

    public void addRole(UUID uuid, Role role) {
        if (uuid == null || role == null) return;
        roles.compute(uuid, (k, v) -> {
            EnumSet<Role> s = (v == null || v.isEmpty()) ? EnumSet.noneOf(Role.class) : EnumSet.copyOf(v);
            s.add(role);
            return s;
        });
        notifyChange(uuid);
    }

    public void removeRole(UUID uuid, Role role) {
        if (uuid == null || role == null) return;
        roles.computeIfPresent(uuid, (k, v) -> {
            EnumSet<Role> s = v.isEmpty() ? EnumSet.noneOf(Role.class) : EnumSet.copyOf(v);
            s.remove(role);
            if (s.isEmpty()) s.add(Role.MEMBER);
            return s;
        });
        notifyChange(uuid);
    }

    // listeners

    public void onChange(Consumer<UUID> l) {
        if (l != null) {
            listeners.add(l);
        }
    }

    private void notifyChange(UUID u) {
        for (Consumer<UUID> l : listeners) {
            try {
                l.accept(u);
            } catch (Exception ignored) {
            }
        }
    }
}
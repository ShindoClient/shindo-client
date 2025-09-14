// me/miki/shindo/api/roles/adapter/RolesAdapter.java
package me.miki.shindo.api.roles.adapter;

import me.miki.shindo.api.roles.Role;
import me.miki.shindo.api.roles.RoleManager;
import me.miki.shindo.logger.ShindoLogger;

import java.util.*;

public final class RolesAdapter {

    private RolesAdapter() {
    }

    public static String[] toWsRoles(Collection<Role> roles) {
        if (roles == null || roles.isEmpty()) return new String[]{"MEMBER"};
        LinkedHashSet<String> out = new LinkedHashSet<>();
        for (Role r : roles) {
            if (r == null) continue;
            out.add(r.name());
        }
        if (out.isEmpty()) {
            out.add("MEMBER");
        }
        return out.toArray(new String[0]);
    }

    public static Set<Role> toEnumSet(String[] roles) {
        LinkedHashSet<Role> out = new LinkedHashSet<>();
        if (roles != null) {
            for (String r : roles) {
                if (r == null) continue;
                String s = r.trim().toUpperCase(Locale.ROOT);
                try {
                    out.add(Role.valueOf(s));
                } catch (IllegalArgumentException er) {
                    ShindoLogger.error("[ROLES-ADAPTER] " + er);
                }
            }
        }
        if (out.isEmpty()) {
            out.add(Role.MEMBER);
        }
        return out;
    }

    public static String[] toWsRoles(RoleManager roleManager, UUID user) {
        if (roleManager == null || user == null) return new String[]{"MEMBER"};
        try {
            Collection<Role> set = RoleManager.getDirectRoles(user);
            return toWsRoles(set);
        } catch (Throwable t) {
            return new String[]{"MEMBER"};
        }
    }
}

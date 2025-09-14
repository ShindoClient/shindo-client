package me.miki.shindo.api.ws.integration;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.Getter;
import me.miki.shindo.api.roles.Role;
import me.miki.shindo.api.roles.RoleManager;
import me.miki.shindo.api.roles.adapter.RolesAdapter;
import me.miki.shindo.api.ws.ShindoWsService;
import me.miki.shindo.api.ws.presence.PresenceTracker;

import java.net.URI;
import java.util.EnumSet;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * Bootstrap do WebSocket para o client.
 * - Fornece uuid/name/accountType e (opcionalmente) roles;
 * - Garante MEMBER como default ao enviar;
 * - Sincroniza as roles recebidas do servidor (auth.ok.roles) para o RoleManager local;
 * - Mantém PresenceTracker, se configurado.
 */
public class ShindoApiWsBootstrap {

    private final AtomicBoolean started = new AtomicBoolean(false);

    private final String wsUrl;
    private Supplier<String> uuidSupplier;
    private Supplier<String> nameSupplier;
    private Supplier<String> accountTypeSupplier;
    private Supplier<String[]> rolesSupplier;

    private RoleManager roleManager;
    private PresenceTracker presence;

    @Getter
    private ShindoWsService service;

    public ShindoApiWsBootstrap(String wsUrl) {
        this.wsUrl = Objects.requireNonNull(wsUrl, "wsUrl");
    }

    private static String getOrDefault(Supplier<String> s) {
        try {
            return s != null ? Objects.toString(s.get(), "") : "";
        } catch (Exception e) {
            return "";
        }
    }

    private static UUID safeUUID(String s) {
        try {
            return s == null ? null : java.util.UUID.fromString(s);
        } catch (Exception e) {
            return null;
        }
    }

    private static String normalizeAccountType(String s) {
        String v = (s == null ? "" : s.trim().toUpperCase());
        if ("MICROSOFT".equals(v)) return "MICROSOFT";
        return "OFFLINE";
    }

    public ShindoApiWsBootstrap withUuid(Supplier<String> s) {
        this.uuidSupplier = s;
        return this;
    }

    public ShindoApiWsBootstrap withName(Supplier<String> s) {
        this.nameSupplier = s;
        return this;
    }

    public ShindoApiWsBootstrap withAccountType(Supplier<String> s) {
        this.accountTypeSupplier = s;
        return this;
    }

    public ShindoApiWsBootstrap withRoleManager(RoleManager rm) {
        this.roleManager = rm;
        return this;
    }

    public ShindoApiWsBootstrap withPresenceTracker(PresenceTracker pt) {
        this.presence = pt;
        return this;
    }

    public synchronized void start() {
        if (started.getAndSet(true)) return;

        URI uri = URI.create(wsUrl);
        boolean ssl = wsUrl.startsWith("wss://");

        service = new ShindoWsService(uri, ssl);

        service.addListener(new ShindoWsService.Listener() {
            @Override
            public void onMessage(String type, JsonObject payload) {
                if ("auth.ok".equals(type)) {
                    try {
                        UUID uuid = safeUUID(getOrDefault(uuidSupplier));
                        if (payload != null && payload.has("uuid")) {
                            try {
                                uuid = UUID.fromString(payload.get("uuid").getAsString());
                            } catch (Exception ignored) {
                            }
                        }
                        if (roleManager != null && uuid != null) {
                            EnumSet<Role> set = EnumSet.noneOf(Role.class);
                            if (payload != null && payload.has("roles") && payload.get("roles").isJsonArray()) {
                                JsonArray arr = payload.getAsJsonArray("roles");
                                String[] rs = new String[arr.size()];
                                for (int i = 0; i < arr.size(); i++) rs[i] = arr.get(i).getAsString();
                                set.addAll(RolesAdapter.toEnumSet(rs));
                            } else {
                                set.add(Role.MEMBER);
                            }
                            roleManager.setRoles(uuid, set);
                        }
                    } catch (Exception ignored) {
                    }
                }
                presence.handleMessage(type, payload);
            }
        });

        service.setProvider(() -> {
            String uuid = getOrDefault(uuidSupplier);
            String name = getOrDefault(nameSupplier);
            String accountType = normalizeAccountType(getOrDefault(accountTypeSupplier));

            String[] roles;
            if (rolesSupplier != null) {
                roles = rolesSupplier.get();
            } else if (roleManager != null) {
                roles = RolesAdapter.toWsRoles(RoleManager.getDirectRoles(safeUUID(uuid)));
            } else {
                roles = new String[0];
            }
            if (roles == null || roles.length == 0) roles = new String[]{"MEMBER"};

            return new ShindoWsService.PlayerInfo(uuid, name, roles, accountType);
        });

        service.connect();
    }

    public synchronized void stop() {
        if (!started.getAndSet(false)) return;
        if (service != null) {
            try {
                service.disconnect();
                presence.clear();
            } catch (Exception ignored) {

            }
        }
    }

    public void send(String type, JsonObject payload) {
        ShindoWsService s = this.service;
        if (s != null && s.isOpen()) s.send(type, payload);
    }

    public void send(String type) {
        send(type, new JsonObject());
    }
}

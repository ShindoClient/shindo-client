package me.miki.shindo.api.ws.integration;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.Getter;
import me.miki.shindo.api.roles.Role;
import me.miki.shindo.api.roles.RoleManager;
import me.miki.shindo.api.roles.adapter.RolesAdapter;
import me.miki.shindo.api.ws.ShindoWsService;
import me.miki.shindo.api.ws.presence.PresenceTracker;
import me.miki.shindo.logger.ShindoLogger;

import java.net.URI;
import java.util.EnumSet;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * Bootstrap do WebSocket para o client.
 * <p>
 * Responsável por:
 * <ul>
 *     <li>Publicar a identidade atual (uuid/nome/accountType) com fallback seguro.</li>
 *     <li>Sincronizar roles recebidas do gateway com o RoleManager local.</li>
 *     <li>Propagar mensagens relevantes ao PresenceTracker.</li>
 * </ul>
 */
public class ShindoApiWsBootstrap {

    private final AtomicBoolean started = new AtomicBoolean(false);
    private final String wsUrl;
    private final AtomicReference<String> lastUuidSent = new AtomicReference<>(null);
    private final AtomicReference<String> lastNameSent = new AtomicReference<>(null);

    private Supplier<String> uuidSupplier;
    private Supplier<String> nameSupplier;
    private Supplier<String> accountTypeSupplier;
    private Supplier<String[]> rolesSupplier;
    private RoleManager roleManager;
    private PresenceTracker presence;
    private ShindoWsService.SessionProvider sessionProvider;

    @Getter
    private ShindoWsService service;

    public ShindoApiWsBootstrap(String wsUrl) {
        this.wsUrl = Objects.requireNonNull(wsUrl, "wsUrl");
    }

    private static String getOrDefault(Supplier<String> supplier) {
        try {
            return supplier != null ? Objects.toString(supplier.get(), "") : "";
        } catch (Exception ignored) {
            return "";
        }
    }

    private static UUID safeUUID(String value) {
        try {
            return value == null ? null : java.util.UUID.fromString(value);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String normalizeAccountType(String value) {
        String normalized = (value == null ? "" : value.trim().toUpperCase());
        return "MICROSOFT".equals(normalized) ? "MICROSOFT" : "OFFLINE";
    }

    public ShindoApiWsBootstrap withUuid(Supplier<String> supplier) {
        this.uuidSupplier = supplier;
        return this;
    }

    public ShindoApiWsBootstrap withName(Supplier<String> supplier) {
        this.nameSupplier = supplier;
        return this;
    }

    public ShindoApiWsBootstrap withAccountType(Supplier<String> supplier) {
        this.accountTypeSupplier = supplier;
        return this;
    }

    public ShindoApiWsBootstrap withSessionProvider(ShindoWsService.SessionProvider provider) {
        this.sessionProvider = provider;
        return this;
    }

    public ShindoApiWsBootstrap withRoles(Supplier<String[]> supplier) {
        this.rolesSupplier = supplier;
        return this;
    }

    public ShindoApiWsBootstrap withRoleManager(RoleManager manager) {
        this.roleManager = manager;
        return this;
    }

    public ShindoApiWsBootstrap withPresenceTracker(PresenceTracker tracker) {
        this.presence = tracker;
        return this;
    }

    private ShindoWsService.PlayerInfo buildPlayerInfo() {
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
        if (roles == null || roles.length == 0) {
            roles = new String[]{"MEMBER"};
        }

        return new ShindoWsService.PlayerInfo(uuid, name, roles, accountType);
    }

    /**
     * Atualiza a identidade atual no WS.
     * <p>
     * Se a conta mudou (uuid ou nome diferentes) uma reautenticação é disparada com novo token.
     * Se apenas as roles mudaram, envia um `roles.update` com o diff.
     */
    public void updateIdentityNow() {
        ShindoWsService currentService = this.service;
        if (currentService == null) {
            return;
        }

        ShindoWsService.PlayerInfo info = buildPlayerInfo();
        String currentUuid = info.uuid;
        String currentName = info.name;

        if (!currentService.isOpen()) {
            restart();
            return;
        }

        String lastUuid = lastUuidSent.get();
        if (lastUuid != null && !lastUuid.equals(currentUuid)) {
            lastUuidSent.set(currentUuid);
            lastNameSent.set(currentName);
            currentService.invalidateSession();
            currentService.reauthenticate();
            return;
        }

        String lastName = lastNameSent.get();
        if (lastName != null && !lastName.equals(currentName)) {
            lastNameSent.set(currentName);
            currentService.invalidateSession();
            currentService.reauthenticate();
            return;
        }

        currentService.pushRoles(info.roles);
        lastUuidSent.set(currentUuid);
        lastNameSent.set(currentName);
    }

    public synchronized void start() {
        if (started.getAndSet(true)) {
            return;
        }

        URI uri = URI.create(wsUrl);
        boolean ssl = wsUrl.startsWith("wss://");

        service = new ShindoWsService(uri, ssl);
        service.setSessionProvider(sessionProvider);
        service.setPresenceTracker(presence);
        service.setRoleManager(roleManager);

        service.addListener(new ShindoWsService.Listener() {
            @Override
            public void onMessage(String type, JsonObject payload) {
                if ("auth.error".equals(type)) {
                    ShindoLogger.warn("Gateway rejected authentication payload: " + (payload != null ? payload.toString() : "unknown"));
                    if (service != null) {
                        service.invalidateSession();
                        service.reauthenticate();
                    }
                    return;
                }
                if ("auth.ok".equals(type)) {
                    syncRolesFromAuth(payload);
                }
                if (presence != null) {
                    presence.handleMessage(type, payload);
                }
            }
        });

        service.setProvider(() -> {
            ShindoWsService.PlayerInfo info = buildPlayerInfo();
            lastUuidSent.set(info.uuid);
            lastNameSent.set(info.name);
            return info;
        });

        service.connect();
    }

    public synchronized void stop() {
        if (!started.getAndSet(false)) {
            return;
        }
        if (service != null) {
            try {
                service.disconnect();
            } catch (Exception ignored) {
            } finally {
                service = null;
            }
        }
        if (presence != null) {
            try {
                presence.clear();
            } catch (Exception ignored) {
            }
        }
    }

    public void send(String type, JsonObject payload) {
        ShindoWsService current = service;
        if (current != null && current.isOpen()) {
            current.send(type, payload);
        }
    }

    public void send(String type) {
        send(type, new JsonObject());
    }

    private void restart() {
        stop();
        start();
    }

    private void syncRolesFromAuth(JsonObject payload) {
        if (roleManager == null) {
            return;
        }
        try {
            UUID uuid = safeUUID(getOrDefault(uuidSupplier));
            if (payload != null && payload.has("uuid")) {
                try {
                    uuid = UUID.fromString(payload.get("uuid").getAsString());
                } catch (Exception ignored) {
                }
            }
            if (uuid == null) {
                return;
            }

            EnumSet<Role> roles = EnumSet.noneOf(Role.class);
            if (payload != null && payload.has("roles") && payload.get("roles").isJsonArray()) {
                JsonArray arr = payload.getAsJsonArray("roles");
                String[] roleNames = new String[arr.size()];
                for (int i = 0; i < arr.size(); i++) {
                    roleNames[i] = arr.get(i).getAsString();
                }
                roles.addAll(RolesAdapter.toEnumSet(roleNames));
            } else {
                roles.add(Role.MEMBER);
            }
            roleManager.setRoles(uuid, roles);
        } catch (Exception ignored) {
        }
    }
}

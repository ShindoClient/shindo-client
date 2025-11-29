package me.miki.shindo;

import lombok.Getter;
import me.miki.shindo.api.roles.RoleManager;
import me.miki.shindo.api.websocket.AccountType;
import me.miki.shindo.api.websocket.ShindoWebsocket;
import me.miki.shindo.api.websocket.WsIdentity;
import me.miki.shindo.api.websocket.presence.PresenceTracker;
import me.miki.shindo.gui.mainmenu.GuiShindoMainMenu;
import me.miki.shindo.gui.modmenu.GuiModMenu;
import me.miki.shindo.logger.ShindoLogger;
import me.miki.shindo.management.file.FileManager;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.function.Supplier;

public class ShindoAPI {

    private final RoleManager roleManager = new RoleManager();
    private final PresenceTracker presence = new PresenceTracker();

    @Getter
    private final File firstLoginFile;

    @Getter
    private long launchTime;

    @Getter
    private GuiModMenu modMenu;

    @Getter
    private GuiShindoMainMenu mainMenu;

    @Getter
    private ShindoWebsocket ws;


    public ShindoAPI() {

        FileManager fileManager = Shindo.getInstance().getFileManager();
        firstLoginFile = new File(fileManager.getCacheDir(), "first.tmp");
    }

    public void init() {
        launchTime = System.currentTimeMillis();
        modMenu = new GuiModMenu();
        mainMenu = new GuiShindoMainMenu();
    }

    public void start() {
        // Suppliers sempre leem o estado ATUAL (session/account), então não precisamos recriar o bootstrap a cada troca
        Supplier<String> uuidSup = () -> {
            try {
                UUID id = Minecraft.getMinecraft().getSession().getProfile().getId();
                return id != null ? id.toString() : "";
            } catch (Exception e) {
                return "";
            }
        };

        Supplier<String> nameSup = () -> {
            try {
                return Minecraft.getMinecraft().getSession().getUsername();
            } catch (Exception e) {
                return "";
            }
        };

        Supplier<String> typeSup = () -> {
            try {
                Minecraft mc = Minecraft.getMinecraft();
                if (mc == null || mc.getSession() == null) {
                    return "LOCAL";
                }
                
                UUID uuid = mc.getSession().getProfile().getId();
                
                // Sem UUID válido = LOCAL
                if (uuid == null) {
                    return "LOCAL";
                }
                
                // Tenta detectar Microsoft account via reflection (compatível com diferentes versões)
                try {
                    Object session = mc.getSession();
                    Method getSessionType = session.getClass().getMethod("getSessionType");
                    String sessionType = (String) getSessionType.invoke(session);
                    if (sessionType != null && (sessionType.equals("msa") || sessionType.contains("microsoft"))) {
                        return "MICROSOFT";
                    }
                } catch (Exception e) {
                    ShindoLogger.error("An error occurred while trying to get the session type", e);
                }
                
                // Verifica se tem token (Microsoft accounts geralmente têm token)
                try {
                    Object session = mc.getSession();
                    Method getToken = session.getClass().getMethod("getToken");
                    String token = (String) getToken.invoke(session);
                    if (token != null && !token.isEmpty()) {
                        // Se tem token e UUID válido, provavelmente é Microsoft
                        return "MICROSOFT";
                    }
                } catch (Exception e) {
                    ShindoLogger.error("An error occurred while trying to get the token", e);
                }
                
                // Se tem UUID válido mas não é Microsoft = OFFLINE
                return "OFFLINE";
            } catch (Exception e) {
                return "LOCAL";
            }
        };

        ws = new ShindoWebsocket(URI.create("wss://ws.shindoclient.com/websocket"), true, presence);
        ws.setRoleManager(roleManager);
        ws.setProvider(() -> {
            String rawUuid = safeTrim(uuidSup.get());
            String rawName = safeTrim(nameSup.get());
            if (rawName.isEmpty()) {
                rawName = "Player";
            }

            UUID parsed = safeUUID(rawUuid);
            String effectiveUuid = rawUuid;
            AccountType accountType = AccountType.from(typeSup.get());

            // Se não temos UUID válido (ex.: ambiente dev sem launcher),
            // geramos um UUID offline estável baseado no nome e marcamos como LOCAL.
            if (parsed == null) {
                effectiveUuid = generateOfflineUuid(rawName);
                accountType = AccountType.LOCAL;
            }

            return new WsIdentity(
                    effectiveUuid,
                    rawName,
                    null,
                    accountType
            );
        });

        ws.connect();
    }

    public void stop() {
        if (ws != null) {
            ws.disconnect();
            ws = null;
        }
    }

    public void createFirstLoginFile() {
        Shindo.getInstance().getFileManager().createFile(firstLoginFile);
    }

    public boolean isFirstLogin() {
        return !firstLoginFile.exists();
    }

    // ===== Helpers de identidade =====

    private static String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private static UUID safeUUID(String value) {
        try {
            return (value == null || value.isEmpty()) ? null : UUID.fromString(value);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String generateOfflineUuid(String name) {
        String baseName = (name == null || name.isEmpty()) ? "Player" : name;
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + baseName).getBytes(StandardCharsets.UTF_8)).toString();
    }
}

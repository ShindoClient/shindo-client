package me.miki.shindo;

import lombok.Getter;
import me.miki.shindo.api.roles.RoleManager;
import me.miki.shindo.api.ws.integration.ShindoApiWsBootstrap;
import me.miki.shindo.api.ws.session.ShindoSessionClient;
import me.miki.shindo.api.ws.presence.PresenceTracker;
import me.miki.shindo.gui.mainmenu.GuiShindoMainMenu;
import me.miki.shindo.gui.modmenu.GuiModMenu;
import me.miki.shindo.management.file.FileManager;
import net.minecraft.client.Minecraft;

import java.io.File;
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
    private ShindoApiWsBootstrap ws;


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
                return Shindo.getInstance().getAccountManager().getCurrentAccount().getType().toString();
            } catch (Exception e) {
                return "OFFLINE";
            }
        };

        ws = new ShindoApiWsBootstrap("wss://ws.shindoclient.com/websocket")
                .withUuid(uuidSup)
                .withName(nameSup)
                .withAccountType(typeSup)
                .withSessionProvider(new ShindoSessionClient())
                .withRoleManager(roleManager)
                .withPresenceTracker(presence);

        ws.start();
    }

    public void stop() {
        if (ws != null) {
            ws.stop();
            ws = null;
        }
    }

    public void createFirstLoginFile() {
        Shindo.getInstance().getFileManager().createFile(firstLoginFile);
    }

    public boolean isFirstLogin() {
        return !firstLoginFile.exists();
    }
}

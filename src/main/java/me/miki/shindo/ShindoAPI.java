package me.miki.shindo;

import lombok.Getter;
import me.miki.shindo.api.roles.RoleManager;
import me.miki.shindo.api.ws.integration.ShindoApiWsBootstrap;
import me.miki.shindo.api.ws.presence.PresenceTracker;
import me.miki.shindo.gui.mainmenu.GuiShindoMainMenu;
import me.miki.shindo.gui.modmenu.GuiModMenu;
import me.miki.shindo.management.file.FileManager;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.util.UUID;

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
        UUID uuid = Minecraft.getMinecraft().getSession().getProfile().getId();
        ws = new ShindoApiWsBootstrap("wss://ws.shindoclient.com/websocket")
                .withUuid(uuid::toString)
                .withName(() -> Minecraft.getMinecraft().getSession().getUsername())
                .withAccountType(() -> Shindo.getInstance().getAccountManager().getCurrentAccount().getType().toString())
                .withRoleManager(roleManager)
                .withPresenceTracker(presence);

        ws.start();
    }

    public void stop() {
        if (ws != null) {
            ws.stop();
        }
    }

    public void createFirstLoginFile() {
        Shindo.getInstance().getFileManager().createFile(firstLoginFile);
    }

    public boolean isFirstLogin() {
        return !firstLoginFile.exists();
    }
}
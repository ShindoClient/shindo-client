package me.miki.shindo;

import me.miki.shindo.api.ApiManager;
import me.miki.shindo.api.utils.SSLBypass;
import me.miki.shindo.gui.mainmenu.GuiShindoMainMenu;
import me.miki.shindo.gui.modmenu.GuiModMenu;
import me.miki.shindo.management.file.FileManager;
import me.miki.shindo.management.roles.ClientRoleManager;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.util.UUID;

public class ShindoAPI {

    private final File firstLoginFile;
    private ApiManager apiManager;
    private long launchTime;
    private GuiModMenu modMenu;
    private GuiShindoMainMenu mainMenu;


    public ShindoAPI() {
        FileManager fileManager = Shindo.getInstance().getFileManager();

        firstLoginFile = new File(fileManager.getCacheDir(), "first.tmp");
    }

    public void init() {
        launchTime = System.currentTimeMillis();
        modMenu = new GuiModMenu();
        mainMenu = new GuiShindoMainMenu();
    }

    public void connect() {
        SSLBypass.disableCertificateValidation();

        String username = Minecraft.getMinecraft().getSession().getUsername();
        String uuid = Minecraft.getMinecraft().getSession().getProfile().getId().toString();
        String accountType = Shindo.getInstance()
                .getAccountManager()
                .getCurrentAccount()
                .getType()
                .name();

        // Inicializa o ApiManager com dados corretos
        this.apiManager = new ApiManager(uuid, username, accountType);
        apiManager.notifyEvent("join");
        ClientRoleManager.start();
    }

    public void disconnect() {
        if (apiManager != null) {
            apiManager.notifyEvent("leave");
            apiManager.shutdown();
            ClientRoleManager.stop();
        }
    }

    public GuiModMenu getModMenu() {
        return modMenu;
    }

    public long getLaunchTime() {
        return launchTime;
    }

    public GuiShindoMainMenu getMainMenu() {
        return mainMenu;
    }

    public void createFirstLoginFile() {
        Shindo.getInstance().getFileManager().createFile(firstLoginFile);
    }

    public boolean isFirstLogin() {
        return !firstLoginFile.exists();
    }

    public boolean isOnline(UUID uuid) {
        return apiManager.isOnline(uuid.toString());
    }

    public boolean hasRole(String uuid, String role) {
        return apiManager.hasRole(uuid, role);

    }
}
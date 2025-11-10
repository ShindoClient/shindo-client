package me.miki.shindo.management.file;

import me.miki.shindo.Shindo;
import me.miki.shindo.logger.ShindoLogger;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.IOException;

public class FileManager {

    private final File shindoDir;
    private final File externalDir;
    private final File cacheDir;

    private final File musicDir;
    private final File profileDir;
    private final File screenshotDir;
    private final File addonsDir;
    private final File gamesDir;
    private final File skinsDir;

    private final File serversDir;

    private final File customCapeDir;
    private final File capeCacheDir;

    private final File wingCacheDir;
    private final File bandannaCacheDir;
    private final File profileIconDir;


    public FileManager() {
        File soarDir = new File(Minecraft.getMinecraft().mcDataDir, "soar");

        shindoDir = new File(Minecraft.getMinecraft().mcDataDir, "shindo");
        externalDir = new File(shindoDir, "external");
        cacheDir = new File(shindoDir, "cache");

        musicDir = new File(shindoDir, "music");
        profileDir = new File(shindoDir, "profile");
        screenshotDir = new File(shindoDir, "screenshots");
        addonsDir = new File(shindoDir, "addons");
        gamesDir = new File(shindoDir, "games");
        skinsDir = new File(shindoDir, "skins");

        serversDir = new File(shindoDir, "servers");

        customCapeDir = new File(cacheDir, "custom-cape");
        capeCacheDir = new File(cacheDir, "cape");
        wingCacheDir = new File(cacheDir, "wing");
        bandannaCacheDir = new File(cacheDir, "bandanna");
        profileIconDir = new File(cacheDir, "profile-icon");


        try {

            if (!shindoDir.exists()) {
                if (soarDir.exists()) {
                    boolean migrationSuccess = soarDir.renameTo(shindoDir);
                    if (!migrationSuccess) createDir(shindoDir);
                } else {
                    createDir(shindoDir);
                }
            }

            if (!externalDir.exists()) createDir(externalDir);
            if (!cacheDir.exists()) createDir(cacheDir);

            if (!musicDir.exists()) createDir(musicDir);
            if (!profileDir.exists()) createDir(profileDir);
            if (!screenshotDir.exists()) createDir(screenshotDir);
            if (!addonsDir.exists()) createDir(addonsDir);
            if (!gamesDir.exists()) createDir(gamesDir);
            if (!skinsDir.exists()) createDir(skinsDir);

            if (!serversDir.exists()) createDir(serversDir);

            if (!customCapeDir.exists()) createDir(customCapeDir);
            if (!capeCacheDir.exists()) createDir(capeCacheDir);
            if (!wingCacheDir.exists()) createDir(wingCacheDir);
            if (!bandannaCacheDir.exists()) createDir(bandannaCacheDir);
            if (!profileIconDir.exists()) createDir(profileIconDir);


            createVersionFile();

        } catch (Exception e) {
            ShindoLogger.error("Something has gone very wrong while trying to create the shindo folder which may result in crashes later", e);
        }

    }

    private void createVersionFile() {

        File versionDir = new File(cacheDir, "version");

        createDir(versionDir);
        createFile(new File(versionDir, Shindo.getInstance().getVerIdentifier() + ".tmp"));
    }

    public void createDir(File file) {
        file.mkdir();
    }

    public void createFile(File file) {
        try {
            file.createNewFile();
        } catch (IOException e) {
            ShindoLogger.error("Failed to create file " + file.getName(), e);
        }
    }

    public File getShindoDir() {
        return shindoDir;
    }

    public File getExternalDir() {
        return externalDir;
    }

    public File getCacheDir() {
        return cacheDir;
    }

    public File getMusicDir() {
        return musicDir;
    }

    public File getProfileDir() {
        return profileDir;
    }

    public File getScreenshotDir() {
        return screenshotDir;
    }

    public File getAddonsDir() {
        return addonsDir;
    }

    public File getGamesDir() {
        return gamesDir;
    }

    public File getSkinsDir() {
        return skinsDir;
    }

    public File getServersDir() {
        return serversDir;
    }

    public File getCustomCapeDir() {
        return customCapeDir;
    }

    public File getCapeCacheDir() {
        return capeCacheDir;
    }

    public File getWingCacheDir() {
        return wingCacheDir;
    }

    public File getBandannaCacheDir() {
        return bandannaCacheDir;
    }

    public File getProfileIconDir() {
        return profileIconDir;
    }

}

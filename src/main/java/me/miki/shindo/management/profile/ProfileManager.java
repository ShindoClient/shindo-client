package me.miki.shindo.management.profile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.Getter;
import me.miki.shindo.Shindo;
import me.miki.shindo.logger.ShindoLogger;
import me.miki.shindo.management.color.ColorManager;
import me.miki.shindo.management.color.Theme;
import me.miki.shindo.management.file.FileManager;
import me.miki.shindo.management.language.Language;
import me.miki.shindo.management.mods.HUDMod;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModManager;
import me.miki.shindo.management.profile.mainmenu.BackgroundManager;
import me.miki.shindo.management.settings.Setting;
import me.miki.shindo.management.settings.impl.*;
import me.miki.shindo.management.tweaker.ConnectionTweakerManager;
import me.miki.shindo.utils.ColorUtils;
import me.miki.shindo.utils.JsonUtils;
import me.miki.shindo.utils.file.FileUtils;

import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
public class ProfileManager {

    private static final long AUTO_SAVE_INTERVAL_MS = 2500L;

    private final CopyOnWriteArrayList<Profile> profiles = new CopyOnWriteArrayList<Profile>();
    private final BackgroundManager backgroundManager;
    private final Gson gson = new Gson();
    private final Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();

    private Profile activeProfile;
    private Profile defaultProfile;
    private File pendingActiveFile;
    private String lastSavedSnapshot = "";
    private long lastAutoSaveCheck;

    public ProfileManager() {

        backgroundManager = new BackgroundManager();
        this.loadProfiles(true);
    }

    public void loadProfiles(boolean loadDefaultProfile) {

        FileManager fileManager = Shindo.getInstance().getFileManager();
        File profileDir = fileManager.getProfileDir();

        try {
            if (!profileDir.exists()) {
                fileManager.createDir(profileDir);
            }

            File defaultFile = new File(profileDir, "Default.json");
            if (!defaultFile.exists()) {
                save(defaultFile, "", ProfileType.ALL, ProfileIcon.GRASS, null);
            } else if (loadDefaultProfile) {
                load(defaultFile);
            }

            profiles.clear();

            defaultProfile = buildProfileFromFile(defaultFile, -1);
            if (defaultProfile != null) {
                profiles.add(defaultProfile);
            }

            int id = 0;
            File[] files = profileDir.listFiles();
            if (files != null) {
                for (File f : files) {
                    String extension = FileUtils.getExtension(f);
                    if (!"json".equalsIgnoreCase(extension) || f.equals(defaultFile)) {
                        continue;
                    }

                    Profile profile = buildProfileFromFile(f, id++);
                    if (profile != null) {
                        profiles.add(profile);
                    }
                }
            }
        } catch (Exception e) {
            ShindoLogger.error("Failed to load profile metadata", e);
        }

        profiles.add(new Profile(999, "", null, null, null));
        synchronizeActiveProfileReference(loadDefaultProfile);
    }

    public void save(File file, String serverIp, ProfileType type, ProfileIcon icon, File customIcon) {

        JsonObject snapshot = buildProfileSnapshot(serverIp, type, icon, customIcon);
        writeProfile(file, snapshot);

        if (activeProfile != null && activeProfile.getJsonFile() != null && activeProfile.getJsonFile().equals(file)) {
            lastSavedSnapshot = prettyGson.toJson(snapshot);
            lastAutoSaveCheck = System.currentTimeMillis();
        }
    }

    public void save() {
        saveActiveProfile();
    }

    public void saveActiveProfile() {
        Profile target = activeProfile != null ? activeProfile : defaultProfile;
        if (target == null || target.getJsonFile() == null) {
            return;
        }

        JsonObject snapshot = buildProfileSnapshot(target.getServerIp(), target.getType(), target.getIcon(), target.getCustomIcon());
        writeProfile(target.getJsonFile(), snapshot);
        lastSavedSnapshot = prettyGson.toJson(snapshot);
        lastAutoSaveCheck = System.currentTimeMillis();
    }

    public void handleAutoSave() {
        Profile target = activeProfile != null ? activeProfile : defaultProfile;
        if (target == null || target.getJsonFile() == null) {
            return;
        }

        long now = System.currentTimeMillis();
        if (now - lastAutoSaveCheck < AUTO_SAVE_INTERVAL_MS) {
            return;
        }

        JsonObject snapshot = buildProfileSnapshot(target.getServerIp(), target.getType(), target.getIcon(), target.getCustomIcon());
        String serialized = prettyGson.toJson(snapshot);

        if (!serialized.equals(lastSavedSnapshot)) {
            writeProfile(target.getJsonFile(), snapshot);
            lastSavedSnapshot = serialized;
        }

        lastAutoSaveCheck = now;
    }

    public void setActiveProfile(Profile profile) {
        if (profile == null || profile.getJsonFile() == null) {
            return;
        }

        this.activeProfile = profile;
        JsonObject snapshot = buildProfileSnapshot(profile.getServerIp(), profile.getType(), profile.getIcon(), profile.getCustomIcon());
        this.lastSavedSnapshot = prettyGson.toJson(snapshot);
        this.lastAutoSaveCheck = System.currentTimeMillis();
    }

    public void load(File file) {

        Shindo instance = Shindo.getInstance();
        ModManager modManager = instance.getModManager();
        ColorManager colorManager = instance.getColorManager();
        FileManager fileManager = instance.getFileManager();
        ConnectionTweakerManager tweakerManager = instance.getConnectionTweakerManager();

        if (file == null) {
            return;
        }

        try (FileReader reader = new FileReader(file)) {

            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
            JsonObject appJsonObject = JsonUtils.getObjectProperty(jsonObject, "Appearance");
            JsonObject modJsonObject = JsonUtils.getObjectProperty(jsonObject, "Mods");
            JsonObject tweakerJsonObject = JsonUtils.getObjectProperty(jsonObject, "Tweaker");

            colorManager.setCurrentColor(colorManager.getColorByName(JsonUtils.getStringProperty(appJsonObject, "Accent Color", "Teal Love")));
            colorManager.setTheme(Theme.getThemeById(JsonUtils.getIntProperty(appJsonObject, "Theme", Theme.LIGHT.getId())));
            backgroundManager.setCurrentBackground(backgroundManager.getBackgroundById(JsonUtils.getIntProperty(appJsonObject, "Background", 0)));
            instance.getLanguageManager().setCurrentLanguage(Language.getLanguageById(JsonUtils.getStringProperty(appJsonObject, "Language", Language.ENGLISH.getId())));

            for (Mod m : modManager.getMods()) {

                JsonObject mJsonObject = JsonUtils.getObjectProperty(modJsonObject, m.getNameKey());

                if (mJsonObject != null) {

                    m.setToggled(JsonUtils.getBooleanProperty(mJsonObject, "Toggle", false));

                    if (m instanceof HUDMod) {

                        HUDMod hMod = (HUDMod) m;

                        hMod.setX(JsonUtils.getIntProperty(mJsonObject, "X", 100));
                        hMod.setY(JsonUtils.getIntProperty(mJsonObject, "Y", 100));
                        hMod.setWidth(JsonUtils.getIntProperty(mJsonObject, "Width", 100));
                        hMod.setHeight(JsonUtils.getIntProperty(mJsonObject, "Height", 100));
                        hMod.setScale(JsonUtils.getFloatProperty(mJsonObject, "Scale", 1));
                    }

                    if (modManager.getSettingsByMod(m) != null) {

                        JsonObject sJsonObject = JsonUtils.getObjectProperty(mJsonObject, "Settings");

                        if (sJsonObject != null) {
                            for (Setting s : modManager.getSettingsByMod(m)) {

                                if (s instanceof ColorSetting) {

                                    ColorSetting cSetting = (ColorSetting) s;

                                    cSetting.setColor(ColorUtils.getColorByInt(JsonUtils.getIntProperty(sJsonObject, s.getNameKey(), Color.RED.getRGB())));
                                }

                                if (s instanceof BooleanSetting) {

                                    BooleanSetting bSetting = (BooleanSetting) s;

                                    bSetting.setToggled(JsonUtils.getBooleanProperty(sJsonObject, s.getNameKey(), false));
                                }

                                if (s instanceof ComboSetting) {

                                    ComboSetting cSetting = (ComboSetting) s;

                                    cSetting.setOption(cSetting.getOptionByNameKey(JsonUtils.getStringProperty(sJsonObject, s.getNameKey(), cSetting.getDefaultOption().getNameKey())));
                                }

                                if (s instanceof NumberSetting) {

                                    NumberSetting nSetting = (NumberSetting) s;

                                    nSetting.setValue(JsonUtils.getDoubleProperty(sJsonObject, s.getNameKey(), nSetting.getDefaultValue()));
                                }

                                if (s instanceof TextSetting) {

                                    TextSetting tSetting = (TextSetting) s;

                                    tSetting.setText(JsonUtils.getStringProperty(sJsonObject, s.getNameKey(), tSetting.getDefaultText()));
                                }

                                if (s instanceof KeybindSetting) {

                                    KeybindSetting kSetting = (KeybindSetting) s;

                                    kSetting.setKeyCode(JsonUtils.getIntProperty(sJsonObject, s.getNameKey(), kSetting.getDefaultKeyCode()));
                                }

                                if (s instanceof ImageSetting) {

                                    ImageSetting iSetting = (ImageSetting) s;

                                    File cacheDir = new File(fileManager.getCacheDir(), "custom-image");

                                    if (cacheDir.exists()) {

                                        File image = new File(cacheDir, JsonUtils.getStringProperty(sJsonObject, s.getNameKey(), null));

                                        if (image != null && image.exists()) {
                                            iSetting.setImage(image);
                                        }
                                    }
                                }

                                if (s instanceof SoundSetting) {

                                    SoundSetting sSetting = (SoundSetting) s;

                                    File cacheDir = new File(fileManager.getCacheDir(), "custom-sound");

                                    if (cacheDir.exists()) {

                                        File image = new File(cacheDir, JsonUtils.getStringProperty(sJsonObject, s.getNameKey(), null));

                                        if (image != null && image.exists()) {
                                            sSetting.setSound(image);
                                        }
                                    }
                                }

                                if (s instanceof CellGridSetting) {
                                    CellGridSetting cgSetting = (CellGridSetting) s;

                                    JsonArray outerArray = sJsonObject.getAsJsonArray(s.getNameKey());
                                    if (outerArray != null) {
                                        boolean[][] cells = new boolean[outerArray.size()][];

                                        for (int i = 0; i < outerArray.size(); i++) {
                                            JsonArray innerArray = outerArray.get(i).getAsJsonArray();
                                            cells[i] = new boolean[innerArray.size()];

                                            for (int j = 0; j < innerArray.size(); j++) {
                                                cells[i][j] = innerArray.get(j).getAsBoolean();
                                            }
                                        }
                                        cgSetting.setCells(cells);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (tweakerManager != null && tweakerJsonObject != null) {
                tweakerManager.applyProfile(tweakerJsonObject);
            }
        } catch (Exception e) {
            ShindoLogger.error("Failed to load profile", e);
        }

        pendingActiveFile = file;
        Profile located = getProfileByFile(file);
        if (located != null) {
            setActiveProfile(located);
            pendingActiveFile = null;
        }
    }

    public void delete(Profile profile) {
        if (profile == null) {
            return;
        }

        profiles.remove(profile);

        if (profile.getJsonFile() != null && profile.getJsonFile().exists()) {
            profile.getJsonFile().delete();
        }

        if (profile.getCustomIcon() != null && profile.getCustomIcon().exists()) {
            profile.getCustomIcon().delete();
        }

        if (activeProfile != null && activeProfile.equals(profile)) {
            if (defaultProfile != null && defaultProfile.getJsonFile() != null) {
                load(defaultProfile.getJsonFile());
            } else {
                activeProfile = null;
                lastSavedSnapshot = "";
            }
        }
    }

    public Profile getProfileByFile(File file) {
        if (file == null) {
            return null;
        }

        for (Profile profile : profiles) {
            if (profile.getJsonFile() != null && profile.getJsonFile().equals(file)) {
                return profile;
            }
        }

        if (defaultProfile != null && defaultProfile.getJsonFile() != null && defaultProfile.getJsonFile().equals(file)) {
            return defaultProfile;
        }

        return null;
    }

    private Profile buildProfileFromFile(File file, int id) {
        try (FileReader reader = new FileReader(file)) {
            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
            if (jsonObject == null) {
                jsonObject = new JsonObject();
            }

            JsonObject profileData = JsonUtils.getObjectProperty(jsonObject, "Profile Data");
            if (profileData == null) {
                profileData = new JsonObject();
            }

            String serverIp = JsonUtils.getStringProperty(profileData, "Server", "");
            ProfileIcon icon = ProfileIcon.getIconById(JsonUtils.getIntProperty(profileData, "Icon", ProfileIcon.GRASS.getId()));
            ProfileType type = ProfileType.getTypeById(JsonUtils.getIntProperty(profileData, "Type", ProfileType.ALL.getId()));

            File customIcon = null;
            String customIconName = JsonUtils.getStringProperty(profileData, "CustomIcon", "");
            if (customIconName != null && !customIconName.isEmpty() && !"null".equalsIgnoreCase(customIconName)) {
                File iconDir = Shindo.getInstance().getFileManager().getProfileIconDir();
                File candidate = new File(iconDir, customIconName);
                if (candidate.exists()) {
                    customIcon = candidate;
                }
            }

            Profile profile = new Profile(id, serverIp, file, icon, customIcon);
            profile.setType(type);
            return profile;
        } catch (Exception e) {
            ShindoLogger.error("Failed to load profile", e);
        }

        return null;
    }

    private JsonObject buildProfileSnapshot(String serverIp, ProfileType type, ProfileIcon icon, File customIcon) {

        Shindo instance = Shindo.getInstance();
        ModManager modManager = instance.getModManager();
        ColorManager colorManager = instance.getColorManager();
        ConnectionTweakerManager tweakerManager = instance.getConnectionTweakerManager();

        JsonObject jsonObject = new JsonObject();
        JsonObject appJsonObject = new JsonObject();
        JsonObject modJsonObject = new JsonObject();
        JsonObject profileData = new JsonObject();

        ProfileIcon resolvedIcon = icon == null ? ProfileIcon.GRASS : icon;
        ProfileType resolvedType = type == null ? ProfileType.ALL : type;

        profileData.addProperty("Icon", resolvedIcon.getId());
        profileData.addProperty("Type", resolvedType.getId());
        profileData.addProperty("Server", serverIp == null ? "" : serverIp);
        profileData.addProperty("CustomIcon", customIcon == null ? "" : customIcon.getName());

        jsonObject.add("Profile Data", profileData);

        appJsonObject.addProperty("Accent Color", colorManager.getCurrentColor().getName());
        appJsonObject.addProperty("Theme", colorManager.getTheme().getId());
        appJsonObject.addProperty("Background", backgroundManager.getCurrentBackground().getId());
        appJsonObject.addProperty("Language", instance.getLanguageManager().getCurrentLanguage().getId());

        jsonObject.add("Appearance", appJsonObject);

        for (Mod m : modManager.getMods()) {

            JsonObject mJsonObject = new JsonObject();

            mJsonObject.addProperty("Toggle", m.isToggled());

            if (m instanceof HUDMod) {

                HUDMod hMod = (HUDMod) m;

                mJsonObject.addProperty("Toggle", hMod.isToggled());
                mJsonObject.addProperty("X", hMod.getX());
                mJsonObject.addProperty("Y", hMod.getY());
                mJsonObject.addProperty("Width", hMod.getWidth());
                mJsonObject.addProperty("Height", hMod.getHeight());
                mJsonObject.addProperty("Scale", hMod.getScale());
            }

            if (modManager.getSettingsByMod(m) != null) {

                JsonObject sJsonObject = new JsonObject();

                for (Setting s : modManager.getSettingsByMod(m)) {

                    if (s instanceof ColorSetting) {

                        ColorSetting cSetting = (ColorSetting) s;

                        sJsonObject.addProperty(s.getNameKey(), cSetting.getColor().getRGB());
                    }

                    if (s instanceof BooleanSetting) {

                        BooleanSetting bSetting = (BooleanSetting) s;

                        sJsonObject.addProperty(s.getNameKey(), bSetting.isToggled());
                    }

                    if (s instanceof ComboSetting) {

                        ComboSetting cSetting = (ComboSetting) s;

                        sJsonObject.addProperty(s.getNameKey(), cSetting.getOption().getNameKey());
                    }

                    if (s instanceof NumberSetting) {

                        NumberSetting nSetting = (NumberSetting) s;

                        sJsonObject.addProperty(s.getNameKey(), nSetting.getValue());
                    }

                    if (s instanceof TextSetting) {

                        TextSetting tSetting = (TextSetting) s;

                        sJsonObject.addProperty(s.getNameKey(), tSetting.getText());
                    }

                    if (s instanceof KeybindSetting) {

                        KeybindSetting kSetting = (KeybindSetting) s;

                        sJsonObject.addProperty(s.getNameKey(), kSetting.getKeyCode());
                    }

                    if (s instanceof ImageSetting) {

                        ImageSetting iSetting = (ImageSetting) s;

                        sJsonObject.addProperty(s.getNameKey(), iSetting.getImage() == null ? "null" : iSetting.getImage().getName());
                    }

                    if (s instanceof SoundSetting) {

                        SoundSetting sSetting = (SoundSetting) s;

                        sJsonObject.addProperty(s.getNameKey(), sSetting.getSound() == null ? "null" : sSetting.getSound().getName());
                    }

                    if (s instanceof CellGridSetting) {
                        CellGridSetting cgSetting = (CellGridSetting) s;

                        JsonArray outerArray = new JsonArray();

                        boolean[][] cells = cgSetting.getCells();

                        for (boolean[] row : cells) {
                            JsonArray innerArray = new JsonArray();
                            for (boolean cell : row) {
                                innerArray.add(cell);
                            }
                            outerArray.add(innerArray);
                        }

                        sJsonObject.add(s.getNameKey(), outerArray);
                    }
                }

                if (sJsonObject.size() > 0) {
                    mJsonObject.add("Settings", sJsonObject);
                }
            }

            modJsonObject.add(m.getNameKey(), mJsonObject);
        }

        jsonObject.add("Mods", modJsonObject);

        if (tweakerManager != null) {
            jsonObject.add("Tweaker", tweakerManager.toProfileJson());
        }

        return jsonObject;
    }

    private void writeProfile(File file, JsonObject jsonObject) {
        try (FileWriter writer = new FileWriter(file)) {
            prettyGson.toJson(jsonObject, writer);
        } catch (Exception e) {
            ShindoLogger.error("Failed to save profile", e);
        }
    }

    private void synchronizeActiveProfileReference(boolean forceDefault) {
        if (pendingActiveFile != null) {
            Profile located = getProfileByFile(pendingActiveFile);
            if (located != null) {
                setActiveProfile(located);
                pendingActiveFile = null;
                return;
            }
        }

        if ((forceDefault || activeProfile == null) && defaultProfile != null) {
            setActiveProfile(defaultProfile);
            return;
        }

        if (activeProfile != null && activeProfile.getJsonFile() != null) {
            Profile refreshed = getProfileByFile(activeProfile.getJsonFile());
            if (refreshed != null) {
                setActiveProfile(refreshed);
            }
        }
    }
}

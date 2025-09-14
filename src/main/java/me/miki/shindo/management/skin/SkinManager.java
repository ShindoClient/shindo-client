package me.miki.shindo.management.skin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import me.miki.shindo.Shindo;
import me.miki.shindo.logger.ShindoLogger;
import me.miki.shindo.management.file.FileManager;
import me.miki.shindo.utils.JsonUtils;
import net.minecraft.util.ResourceLocation;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class SkinManager {
    private final CopyOnWriteArrayList<Skin> skins = new CopyOnWriteArrayList<>();

    public SkinManager() {
        // load();
    }


    public void load() {

        FileManager fileManager = Shindo.getInstance().getFileManager();
        File skinsDir = fileManager.getSkinsDir();
        File dataJson = new File(skinsDir, "skins.json");

        ArrayList<String> types = new ArrayList<String>();

        if (!dataJson.exists()) {
            fileManager.createFile(dataJson);
        }

        try (FileReader reader = new FileReader(dataJson)) {

            String name = "Default";
            ResourceLocation texture = new ResourceLocation("textures/entity/steve.png");
            SkinType type = SkinType.DEFAULT;

            Gson gson = new GsonBuilder().create();
            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);

            JsonObject skinJsonObject = JsonUtils.getObjectProperty(jsonObject, "Skins");


        } catch (Exception e) {
            ShindoLogger.error("SkinManager load error", e);
        }

    }

    public void save() {

        FileManager fileManager = Shindo.getInstance().getFileManager();
        File skinsDir = fileManager.getSkinsDir();
        if (!skinsDir.exists()) {
            skinsDir.mkdirs();
        }


    }

}
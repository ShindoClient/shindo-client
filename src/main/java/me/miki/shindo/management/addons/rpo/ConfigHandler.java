package me.miki.shindo.management.addons.rpo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class ConfigHandler {
    public final Options options;
    private final File configFile;

    public ConfigHandler(File configFile) {
        this.configFile = configFile;
        this.options = new Options();
        reload();
    }

    public void reload() {
        options.updateOptions();
    }

    public class Options {
        private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        private final List<String> enabledPacks = new ArrayList<>();

        void updateOptions() {
            try {
                if (!configFile.exists()) return;

                JsonObject json = gson.fromJson(Files.newBufferedReader(configFile.toPath()), JsonObject.class);
                JsonArray array = json.has("enabledPacks") ? json.getAsJsonArray("enabledPacks") : null;

                if (array != null) {
                    enabledPacks.clear();
                    for (int i = 0; i < array.size(); i++) {
                        enabledPacks.add(array.get(i).getAsString());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public List<String> getEnabledPacks() {
            return enabledPacks;
        }

        public void updateEnabledPacks() {
            try {
                JsonObject json = new JsonObject();
                JsonArray array = new JsonArray();
                for (String pack : Minecraft.getMinecraft().gameSettings.resourcePacks) {
                    array.add(pack);
                }
                json.add("enabledPacks", array);

                Files.write(configFile.toPath(), gson.toJson(json).getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

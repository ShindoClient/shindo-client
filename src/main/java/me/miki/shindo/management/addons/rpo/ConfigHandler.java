package me.miki.shindo.management.addons.rpo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import lombok.Getter;
import me.miki.shindo.logger.ShindoLogger;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConfigHandler {

    private static final String ENABLED_PACKS_KEY = "enabledPacks";

    @Getter
    private final Options options;
    private final File configFile;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public ConfigHandler(File configFile) {
        this.configFile = configFile;
        this.options = new Options();
        reload();
    }

    public void reload() {
        options.load();
    }

    public class Options {
        private final List<String> enabledPacks = new ArrayList<>();

        public List<String> getEnabledPacks() {
            return Collections.unmodifiableList(enabledPacks);
        }

        private void load() {
            enabledPacks.clear();
            List<String> stored = readFromDisk();

            if (stored.isEmpty()) {
                enabledPacks.addAll(Minecraft.getMinecraft().gameSettings.resourcePacks);
                writeToDisk(enabledPacks);
            } else {
                enabledPacks.addAll(stored);
            }
        }

        public void updateEnabledPacks() {
            enabledPacks.clear();
            enabledPacks.addAll(Minecraft.getMinecraft().gameSettings.resourcePacks);
            writeToDisk(enabledPacks);
        }

        private List<String> readFromDisk() {
            Path path = configFile.toPath();
            if (!Files.exists(path)) {
                return Collections.emptyList();
            }

            try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                JsonObject json = gson.fromJson(reader, JsonObject.class);
                if (json == null || !json.has(ENABLED_PACKS_KEY)) {
                    return Collections.emptyList();
                }

                JsonArray array = json.getAsJsonArray(ENABLED_PACKS_KEY);
                List<String> loaded = new ArrayList<>(array.size());
                for (int i = 0; i < array.size(); i++) {
                    loaded.add(array.get(i).getAsString());
                }
                return loaded;
            } catch (IOException | JsonParseException exception) {
                ShindoLogger.error("Failed to load RPO configuration", exception);
                return Collections.emptyList();
            }
        }

        private void writeToDisk(List<String> packs) {
            Path path = configFile.toPath();
            try {
                Path parent = path.getParent();
                if (parent != null) {
                    Files.createDirectories(parent);
                }

                JsonArray array = new JsonArray();
                for (String pack : packs) {
                    array.add(pack);
                }

                JsonObject json = new JsonObject();
                json.add(ENABLED_PACKS_KEY, array);

                try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
                    gson.toJson(json, writer);
                }
            } catch (IOException exception) {
                ShindoLogger.error("Failed to save RPO configuration", exception);
            }
        }
    }
}

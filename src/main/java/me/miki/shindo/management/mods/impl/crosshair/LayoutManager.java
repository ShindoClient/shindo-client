package me.miki.shindo.management.mods.impl.crosshair;

import com.google.gson.*;
import me.miki.shindo.Shindo;
import me.miki.shindo.management.file.FileManager;
import me.miki.shindo.utils.JsonUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class LayoutManager {

    public static final int MAX_CUSTOM_PRESETS = 8;
    private static final int DEFAULT_PRESET_COLOR = 0xFFFF0000;
    private static final String PRESET_FILE_NAME = "CrosshairPresets.json";

    private final boolean[][] preset4 = new boolean[][]{
            {false, false, false, false, false, false, false, false, false, false, false},
            {false, false, false, false, false, false, false, false, false, false, false},
            {false, false, false, false, false, false, false, false, false, false, false},
            {false, false, false, false, false, false, false, false, false, false, false},
            {false, false, false, false, false, false, false, false, false, false, false},
            {false, false, false, false, false, true, false, false, false, false, false},
            {false, false, false, false, false, false, false, false, false, false, false},
            {false, false, false, false, false, false, false, false, false, false, false},
            {false, false, false, false, false, false, false, false, false, false, false},
            {false, false, false, false, false, false, false, false, false, false, false},
            {false, false, false, false, false, false, false, false, false, false, false}
    };
    private final boolean[][] preset5 = new boolean[][]{
            {false, false, false, false, false, false, false, false, false, false, false},
            {false, false, false, false, false, false, false, false, false, false, false},
            {false, false, false, false, false, false, false, false, false, false, false},
            {false, false, false, false, true, true, true, false, false, false, false},
            {false, false, false, true, true, false, true, true, false, false, false},
            {false, false, false, true, false, false, false, true, false, false, false},
            {false, false, false, true, true, false, true, true, false, false, false},
            {false, false, false, false, true, true, true, false, false, false, false},
            {false, false, false, false, false, false, false, false, false, false, false},
            {false, false, false, false, false, false, false, false, false, false, false},
            {false, false, false, false, false, false, false, false, false, false, false}
    };
    private final boolean[][] preset7 = new boolean[][]{
            {false, false, false, false, false, false, false, false, false, false, false},
            {false, false, false, false, false, false, false, false, false, false, false},
            {false, false, false, false, false, false, false, false, false, false, false},
            {false, false, false, true, false, false, false, true, false, false, false},
            {false, false, false, false, true, false, true, false, false, false, false},
            {false, false, false, false, false, true, false, false, false, false, false},
            {false, false, false, false, true, false, true, false, false, false, false},
            {false, false, false, true, false, false, false, true, false, false, false},
            {false, false, false, false, false, false, false, false, false, false, false},
            {false, false, false, false, false, false, false, false, false, false, false},
            {false, false, false, false, false, false, false, false, false, false, false}
    };
    private final boolean[][] preset8 = new boolean[][]{
            {false, false, false, false, false, false, false, false, false, false, false},
            {false, false, false, false, false, true, false, false, false, false, false},
            {false, false, false, true, false, true, false, true, false, false, false},
            {false, false, true, false, false, true, false, false, true, false, false},
            {false, false, false, false, false, false, false, false, false, false, false},
            {false, true, true, true, false, true, false, true, true, true, false},
            {false, false, false, false, false, false, false, false, false, false, false},
            {false, false, true, false, false, true, false, false, true, false, false},
            {false, false, false, true, false, true, false, true, false, false, false},
            {false, false, false, false, false, true, false, false, false, false, false},
            {false, false, false, false, false, false, false, false, false, false, false}
    };
    private final boolean[][] preset11 = new boolean[][]{
            {false, false, false, false, false, false, false, false, false, false, false},
            {false, false, false, false, false, false, false, false, false, false, false},
            {false, false, false, false, false, true, false, false, false, false, false},
            {false, false, false, false, true, true, true, false, false, false, false},
            {false, false, false, true, true, false, true, true, false, false, false},
            {false, false, true, true, false, true, false, true, true, false, false},
            {false, false, false, true, true, false, true, true, false, false, false},
            {false, false, false, false, true, true, true, false, false, false, false},
            {false, false, false, false, false, true, false, false, false, false, false},
            {false, false, false, false, false, false, false, false, false, false, false},
            {false, false, false, false, false, false, false, false, false, false, false}
    };
    private final boolean[][] preset13 = new boolean[][]{
            {false, false, false, false, false, false, false, false, false, false, false},
            {false, false, false, false, false, false, false, false, false, false, false},
            {false, false, false, false, false, false, false, false, false, false, false},
            {false, false, true, true, false, false, false, true, true, false, false},
            {false, false, true, false, false, false, false, false, true, false, false},
            {false, false, true, false, false, true, false, false, true, false, false},
            {false, false, true, false, false, false, false, false, true, false, false},
            {false, false, true, true, false, false, false, true, true, false, false},
            {false, false, false, false, false, false, false, false, false, false, false},
            {false, false, false, false, false, false, false, false, false, false, false},
            {false, false, false, false, false, false, false, false, false, false, false}
    };
    private final boolean[][] preset14 = new boolean[][]{
            {false, false, false, false, false, false, false, false, false, false, false},
            {false, false, false, false, false, false, false, false, false, false, false},
            {false, false, false, true, true, true, true, true, false, false, false},
            {false, false, false, true, false, false, false, true, false, false, false},
            {false, false, false, false, false, false, false, false, false, false, false},
            {false, false, false, false, false, true, false, false, false, false, false},
            {false, false, false, false, false, false, false, false, false, false, false},
            {false, false, false, true, false, false, false, true, false, false, false},
            {false, false, false, true, true, true, true, true, false, false, false},
            {false, false, false, false, false, false, false, false, false, false, false},
            {false, false, false, false, false, false, false, false, false, false, false}
    };
    private final boolean[][] preset15 = new boolean[][]{
            {false, false, false, false, false, false, false, false, false, false, false},
            {false, false, false, false, false, true, false, false, false, false, false},
            {false, false, false, false, false, true, false, false, false, false, false},
            {false, false, false, false, true, false, true, false, false, false, false},
            {false, false, false, true, false, false, false, true, false, false, false},
            {false, true, true, false, false, true, false, false, true, true, false},
            {false, false, false, true, false, false, false, true, false, false, false},
            {false, false, false, false, true, false, true, false, false, false, false},
            {false, false, false, false, false, true, false, false, false, false, false},
            {false, false, false, false, false, true, false, false, false, false, false},
            {false, false, false, false, false, false, false, false, false, false, false}
    };
    private final boolean[][] preset16 = new boolean[][]{
            {false, false, false, false, false, false, false, false, false, false, false},
            {false, false, false, false, false, false, false, false, false, false, false},
            {false, false, false, false, false, false, false, false, false, false, false},
            {false, false, false, false, false, false, false, false, false, false, false},
            {false, false, false, false, false, true, false, false, false, false, false},
            {false, false, false, false, true, true, true, false, false, false, false},
            {false, false, false, false, false, true, false, false, false, false, false},
            {false, false, false, false, false, false, false, false, false, false, false},
            {false, false, false, false, false, false, false, false, false, false, false},
            {false, false, false, false, false, false, false, false, false, false, false},
            {false, false, false, false, false, false, false, false, false, false, false}
    };

    private final List<CellGridPreset> userPresets = new ArrayList<>();
    private final File presetFile;

    public LayoutManager() {
        FileManager fileManager = Shindo.getInstance().getFileManager();
        this.presetFile = new File(fileManager.getShindoDir(), PRESET_FILE_NAME);
        init();
    }

    public void init() {
        userPresets.clear();
        loadFromDisk();

        if (userPresets.isEmpty()) {
            seedDefaults();
        }
    }

    public boolean[][] getDefaultLayout() {
        return copyGrid(preset4);
    }

    public List<CellGridPreset> getCustomPresets() {
        return new ArrayList<>(userPresets);
    }

    public CellGridPreset addCustomPreset(String name, boolean[][] layout, int[][] colors) {
        return addOrUpdatePreset(null, layout, colors, name);
    }

    public CellGridPreset addOrUpdatePreset(String id, boolean[][] layout, int[][] colors, String nameOverride) {
        CellGridPreset target = id == null ? null : getPresetById(id);

        if (target != null) {
            target.update(layout, colors);
            saveToDisk();
            return target;
        }

        CellGridPreset preset = new CellGridPreset(
                id == null ? UUID.randomUUID().toString() : id,
                nameOverride == null ? "" : nameOverride,
                layout,
                colors,
                true
        );
        userPresets.removeIf(p -> p.getId().equals(preset.getId()));
        userPresets.add(preset);
        enforceCustomLimit();
        saveToDisk();
        return preset;
    }

    public CellGridPreset getPresetById(String id) {
        if (id == null) {
            return null;
        }
        for (CellGridPreset preset : userPresets) {
            if (id.equals(preset.getId())) {
                return preset;
            }
        }
        return null;
    }

    public void removePreset(CellGridPreset preset) {
        if (preset == null) {
            return;
        }
        userPresets.removeIf(entry -> entry == preset || entry.getId().equals(preset.getId()));
        saveToDisk();
    }

    private void enforceCustomLimit() {
        while (userPresets.size() > MAX_CUSTOM_PRESETS) {
            userPresets.remove(0);
        }
    }

    private void seedDefaults() {
        addOrUpdatePreset("seed-dot", preset4, null, "dot");
        addOrUpdatePreset("seed-diamond", preset5, null, "diamond");
        addOrUpdatePreset("seed-star", preset8, null, "star");
    }

    private void loadFromDisk() {
        try {
            if (!presetFile.exists()) {
                presetFile.createNewFile();
                return;
            }

            try (FileReader reader = new FileReader(presetFile)) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                JsonObject json = gson.fromJson(reader, JsonObject.class);
                if (json == null || !json.has("presets")) {
                    return;
                }

                JsonArray array = json.getAsJsonArray("presets");
                Iterator<JsonElement> iterator = array.iterator();
                while (iterator.hasNext() && userPresets.size() < MAX_CUSTOM_PRESETS) {
                    JsonObject element = iterator.next().getAsJsonObject();
                    boolean[][] layout = JsonUtils.parseBooleanGrid(element.get("layout"));
                    int[][] colors = JsonUtils.parseIntGrid(element.get("colors"));
                    String id = JsonUtils.getStringProperty(element, "id", UUID.randomUUID().toString());
                    String name = JsonUtils.getStringProperty(element, "name", "");
                    if (layout != null) {
                        userPresets.add(new CellGridPreset(id, name, layout, colors, true));
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    public void saveToDisk() {
        try (FileWriter writer = new FileWriter(presetFile)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonObject root = new JsonObject();
            JsonArray presetsArray = new JsonArray();

            for (CellGridPreset preset : userPresets) {
                JsonObject entry = new JsonObject();
                entry.addProperty("id", preset.getId());
                entry.addProperty("name", preset.getName());
                entry.add("layout", JsonUtils.toBooleanGrid(preset.getLayoutCopy()));
                entry.add("colors", JsonUtils.toIntGrid(preset.getColorCopy()));
                presetsArray.add(entry);
            }

            root.add("presets", presetsArray);
            gson.toJson(root, writer);
        } catch (Exception ignored) {
        }
    }

    private static boolean[][] copyGrid(boolean[][] source) {
        if (source == null) {
            return null;
        }
        boolean[][] copy = new boolean[source.length][];
        for (int i = 0; i < source.length; i++) {
            boolean[] row = source[i];
            copy[i] = row != null ? row.clone() : null;
        }
        return copy;
    }

    private static int[][] copyColors(int[][] source, boolean[][] layout) {
        if (layout == null) {
            return null;
        }
        int[][] copy = new int[layout.length][];
        for (int i = 0; i < layout.length; i++) {
            int length = layout[i] != null ? layout[i].length : 0;
            copy[i] = new int[length];
            if (source != null && i < source.length && source[i] != null) {
                System.arraycopy(source[i], 0, copy[i], 0, Math.min(length, source[i].length));
            }
            for (int j = 0; j < length; j++) {
                if (copy[i][j] == 0) {
                    copy[i][j] = DEFAULT_PRESET_COLOR;
                }
            }
        }
        return copy;
    }

    public static final class CellGridPreset {
        private final String id;
        private final String name;
        private boolean[][] layout;
        private int[][] colors;
        private final boolean userPreset;

        private CellGridPreset(String id, String name, boolean[][] layout, int[][] colors, boolean userPreset) {
            this.id = id;
            this.name = name;
            this.layout = copyGrid(layout);
            this.colors = copyColors(colors, layout);
            this.userPreset = userPreset;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public boolean[][] getLayoutCopy() {
            return copyGrid(layout);
        }

        public int[][] getColorCopy() {
            return copyColors(colors, layout);
        }

        public void update(boolean[][] newLayout, int[][] newColors) {
            this.layout = copyGrid(newLayout);
            this.colors = copyColors(newColors, newLayout);
        }

        public boolean isUserPreset() {
            return userPreset;
        }
    }
}

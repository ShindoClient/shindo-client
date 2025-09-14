package me.miki.shindo.management.profile.mainmenu;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.miki.shindo.Shindo;
import me.miki.shindo.management.file.FileManager;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.management.profile.mainmenu.impl.Background;
import me.miki.shindo.management.profile.mainmenu.impl.CustomBackground;
import me.miki.shindo.management.profile.mainmenu.impl.DefaultBackground;
import me.miki.shindo.management.profile.mainmenu.impl.ShaderBackground;
import me.miki.shindo.utils.JsonUtils;
import me.miki.shindo.utils.file.FileUtils;
import net.minecraft.util.ResourceLocation;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

public class BackgroundManager {

    private final CopyOnWriteArrayList<Background> backgrounds = new CopyOnWriteArrayList<Background>();
    private final CopyOnWriteArrayList<CustomBackground> removeBackgrounds = new CopyOnWriteArrayList<CustomBackground>();
    private Background currentBackground;

    public BackgroundManager() {

        FileManager fileManager = Shindo.getInstance().getFileManager();
        File bgCacheDir = new File(fileManager.getCacheDir(), "background");
        File dataJson = new File(bgCacheDir, "Data.json");

        if (!bgCacheDir.exists()) {
            fileManager.createDir(bgCacheDir);
        }

        if (!dataJson.exists()) {
            fileManager.createFile(dataJson);
        }

        backgrounds.add(new DefaultBackground(0, TranslateText.GRADIENT, new ResourceLocation("shindo/mainmenu/background.png")));
        backgrounds.add(new DefaultBackground(1, TranslateText.NIGHT, new ResourceLocation("shindo/mainmenu/background-night.png")));
        backgrounds.add(new DefaultBackground(2, TranslateText.DOLPHIN, new ResourceLocation("shindo/mainmenu/background-dolphin.png")));

        File shaderDir = new File(fileManager.getShindoDir(), "shaders");
        if (!shaderDir.exists()) {
            fileManager.createDir(shaderDir);
        }

        File menuShaderFile = new File(shaderDir, "menu.fsh");
        // The ShaderBackground will handle loading from resources with ResourceLocation
        backgrounds.add(new ShaderBackground(4, TranslateText.SHADER, menuShaderFile));
        backgrounds.add(new DefaultBackground(999, TranslateText.ADD, null));

        ArrayList<String> removeImages = load();

        for (File f : bgCacheDir.listFiles()) {
            if (FileUtils.getExtension(f).equals("png")) {
                if (!removeImages.isEmpty() && removeImages.contains(f.getName())) {
                    f.delete();
                } else {
                    addCustomBackground(f);
                }
            }
        }

        currentBackground = getBackgroundById(0);
    }

    public ArrayList<String> load() {

        FileManager fileManager = Shindo.getInstance().getFileManager();
        File bgCacheDir = new File(fileManager.getCacheDir(), "background");
        File dataJson = new File(bgCacheDir, "Data.json");
        ArrayList<String> output = new ArrayList<String>();

        try (FileReader reader = new FileReader(dataJson)) {

            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);

            if (jsonObject != null) {

                JsonArray jsonArray = JsonUtils.getArrayProperty(jsonObject, "Remove Images");

                if (jsonArray != null) {

                    Iterator<JsonElement> iterator = jsonArray.iterator();

                    while (iterator.hasNext()) {

                        JsonElement jsonElement = iterator.next();
                        JsonObject rJsonObject = gson.fromJson(jsonElement, JsonObject.class);

                        output.add(JsonUtils.getStringProperty(rJsonObject, "Image", "null"));
                    }
                }
            }
        } catch (Exception e) {
        }

        return output;
    }

    public void save() {

        FileManager fileManager = Shindo.getInstance().getFileManager();
        File bgCacheDir = new File(fileManager.getCacheDir(), "background");
        File dataJson = new File(bgCacheDir, "Data.json");

        try (FileWriter writer = new FileWriter(dataJson)) {

            JsonObject jsonObject = new JsonObject();
            JsonArray jsonArray = new JsonArray();
            Gson gson = new Gson();

            for (CustomBackground bg : removeBackgrounds) {

                JsonObject innerJsonObject = new JsonObject();

                innerJsonObject.addProperty("Image", bg.getImage().getName());

                jsonArray.add(innerJsonObject);
            }

            jsonObject.add("Remove Images", jsonArray);

            gson.toJson(jsonObject, writer);

        } catch (Exception e) {
        }
    }

    public CopyOnWriteArrayList<Background> getBackgrounds() {
        return backgrounds;
    }

    public Background getBackgroundById(int id) {

        for (Background bg : backgrounds) {
            if (bg.getId() == id) {
                return bg;
            }
        }

        return getBackgroundById(0);
    }

    public int getMaxId() {

        int maxId = 0;

        for (Background bg : backgrounds) {
            if (bg.getId() != 999 && bg.getId() > maxId) {
                maxId = bg.getId();
            }
        }

        return maxId;
    }

    public void addCustomBackground(File image) {

        int maxId = getMaxId();
        int index = backgrounds.indexOf(getBackgroundById(999));

        backgrounds.add(index, new CustomBackground(maxId + 1, image.getName().replace(".png", ""), image));
    }

    public void removeCustomBackground(CustomBackground cusBackground) {

        NanoVGManager nvg = Shindo.getInstance().getNanoVGManager();

        nvg.getAssetManager().removeImage(nvg.getContext(), cusBackground.getImage());
        backgrounds.remove(cusBackground);
        removeBackgrounds.add(cusBackground);

        save();
    }

    public Background getCurrentBackground() {
        return currentBackground;
    }

    public void setCurrentBackground(Background currentBackground) {
        this.currentBackground = currentBackground;
    }
}

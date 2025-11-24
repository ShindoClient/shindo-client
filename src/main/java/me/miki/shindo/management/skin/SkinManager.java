package me.miki.shindo.management.skin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.miki.shindo.Shindo;
import me.miki.shindo.logger.ShindoLogger;
import me.miki.shindo.management.file.FileManager;
import me.miki.shindo.utils.JsonUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class SkinManager {

    private static final String DATA_FILE = "skins.json";

    private final CopyOnWriteArrayList<Skin> skins = new CopyOnWriteArrayList<>();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Object ioLock = new Object();
    private final File skinsDir;
    private final File dataFile;

    private Skin currentSkin;

    public SkinManager() {
        FileManager fileManager = Shindo.getInstance().getFileManager();
        skinsDir = fileManager.getSkinsDir();
        if (!skinsDir.exists()) {
            fileManager.createDir(skinsDir);
        }
        dataFile = new File(skinsDir, DATA_FILE);
        if (!dataFile.exists()) {
            fileManager.createFile(dataFile);
        }
        load();
    }

    public List<Skin> getSkins() {
        return Collections.unmodifiableList(skins);
    }

    public Skin getCurrentSkin() {
        return currentSkin;
    }

    public void setCurrentSkin(Skin skin) {
        synchronized (ioLock) {
            this.currentSkin = skin;
            save();
        }
    }

    public void clearCurrentSkin() {
        synchronized (ioLock) {
            this.currentSkin = null;
            save();
        }
    }

    public Skin getSkinById(String id) {
        if (id == null) {
            return null;
        }
        for (Skin skin : skins) {
            if (id.equalsIgnoreCase(skin.getId())) {
                return skin;
            }
        }
        return null;
    }

    public Skin getSkinByName(String name) {
        if (name == null) {
            return null;
        }
        for (Skin skin : skins) {
            if (skin.getName().equalsIgnoreCase(name)) {
                return skin;
            }
        }
        return null;
    }

    public Skin addSkin(String name, SkinType type, boolean favorite, BufferedImage sourceImage, String profileUuid) throws IOException {
        synchronized (ioLock) {
            String sanitizedName = sanitizeName(name);
            validateName(sanitizedName, null);
            BufferedImage normalized = normalizeSkin(sourceImage);
            String id = UUID.randomUUID().toString().replace("-", "");
            String fileName = id + ".png";
            File target = new File(skinsDir, fileName);
            ImageIO.write(normalized, "png", target);
            ResourceLocation texture = registerTexture(target, id);
            Skin skin = new Skin(id, sanitizedName, fileName, type, favorite, texture, sanitizeUuid(profileUuid));
            skins.add(skin);
            save();
            return skin;
        }
    }

    public void updateSkin(Skin skin, String newName, SkinType newType, BufferedImage replacement, String newProfileUuid) throws IOException {
        if (skin == null) {
            return;
        }
        synchronized (ioLock) {
            String sanitizedName = sanitizeName(newName);
            validateName(sanitizedName, skin);
            skin.setName(sanitizedName);
            skin.setType(newType);
            if (newProfileUuid != null && !newProfileUuid.trim().isEmpty()) {
                skin.setProfileUuid(sanitizeUuid(newProfileUuid));
            }
            if (replacement != null) {
                BufferedImage normalized = normalizeSkin(replacement);
                File target = resolveFile(skin);
                ImageIO.write(normalized, "png", target);
                skin.setTexture(registerTexture(target, skin.getId()));
            }
            save();
        }
    }

    public void deleteSkin(Skin skin) {
        if (skin == null) {
            return;
        }
        synchronized (ioLock) {
            skins.remove(skin);
            File skinFile = resolveFile(skin);
            if (skinFile.exists() && !skinFile.delete()) {
                skinFile.deleteOnExit();
            }
            if (currentSkin != null && currentSkin.equals(skin)) {
                currentSkin = null;
            }
            save();
        }
    }

    public void setFavorite(Skin skin, boolean favorite) {
        if (skin == null) {
            return;
        }
        synchronized (ioLock) {
            skin.setFavorite(favorite);
            save();
        }
    }

    public DownloadedSkin downloadSkinByUsername(String username) throws IOException {
        if (username == null || username.trim().isEmpty()) {
            throw new IOException("Nome de usuário inválido");
        }
        String trimmed = username.trim();
        URL profileUrl = new URL("https://api.mojang.com/users/profiles/minecraft/" + trimmed);
        JsonObject profile;
        try (InputStreamReader reader = new InputStreamReader(profileUrl.openStream(), StandardCharsets.UTF_8)) {
            profile = gson.fromJson(reader, JsonObject.class);
        }
        if (profile == null || !profile.has("id")) {
            throw new IOException("Jogador não encontrado");
        }
        String uuid = profile.get("id").getAsString();
        return downloadSkinByProfileId(uuid);
    }

    public DownloadedSkin downloadSkinByUuid(String uuid) throws IOException {
        if (uuid == null || uuid.trim().isEmpty()) {
            throw new IOException("Informe um UUID válido");
        }
        return downloadSkinByProfileId(uuid);
    }

    public DownloadedSkin downloadSkinByUrl(String url) throws IOException {
        if (url == null || url.trim().isEmpty()) {
            throw new IOException("URL inválida");
        }
        String normalizedUrl = normalizeUrl(url);
        BufferedImage image = ImageIO.read(new URL(normalizedUrl));
        if (image == null) {
            throw new IOException("Não foi possível baixar a skin");
        }
        return new DownloadedSkin(normalizeSkin(image), SkinType.DEFAULT, null);
    }

    private DownloadedSkin downloadSkinByProfileId(String uuid) throws IOException {
        String normalizedUuid = requireValidUuid(uuid);
        URL sessionUrl = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + normalizedUuid);
        JsonObject sessionProfile;
        try (InputStreamReader reader = new InputStreamReader(sessionUrl.openStream(), StandardCharsets.UTF_8)) {
            sessionProfile = gson.fromJson(reader, JsonObject.class);
        }
        if (sessionProfile == null || !sessionProfile.has("properties")) {
            throw new IOException("Não foi possível carregar a skin");
        }
        JsonArray properties = sessionProfile.getAsJsonArray("properties");
        if (properties.size() == 0) {
            throw new IOException("Não foi possível carregar a skin");
        }
        String encoded = properties.get(0).getAsJsonObject().get("value").getAsString();
        JsonObject payload = gson.fromJson(new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8), JsonObject.class);
        JsonObject textures = payload.getAsJsonObject("textures");
        if (textures == null || !textures.has("SKIN")) {
            throw new IOException("Skin não encontrada para este UUID");
        }
        JsonObject skinObject = textures.getAsJsonObject("SKIN");
        boolean slim = skinObject.has("metadata")
                && skinObject.getAsJsonObject("metadata").has("model")
                && "slim".equalsIgnoreCase(skinObject.getAsJsonObject("metadata").get("model").getAsString());
        String skinUrl = skinObject.get("url").getAsString();
        BufferedImage image = ImageIO.read(new URL(skinUrl));
        if (image == null) {
            throw new IOException("Não foi possível baixar a skin");
        }
        return new DownloadedSkin(normalizeSkin(image), slim ? SkinType.SLIM : SkinType.DEFAULT, normalizedUuid);
    }

    public void load() {
        synchronized (ioLock) {
            skins.clear();
            currentSkin = null;
            if (!dataFile.exists()) {
                return;
            }

            try (FileReader reader = new FileReader(dataFile)) {
                JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
                if (jsonObject == null) {
                    jsonObject = new JsonObject();
                }
                JsonArray skinArray = JsonUtils.getArrayProperty(jsonObject, "skins");
                for (JsonElement element : skinArray) {
                    if (!element.isJsonObject()) {
                        continue;
                    }
                    JsonObject entry = element.getAsJsonObject();
                    String id = JsonUtils.getStringProperty(entry, "id", UUID.randomUUID().toString().replace("-", ""));
                    String name = JsonUtils.getStringProperty(entry, "name", "Skin");
                    String fileName = JsonUtils.getStringProperty(entry, "file", id + ".png");
                    int typeId = JsonUtils.getIntProperty(entry, "type", SkinType.DEFAULT.getId());
                    boolean favorite = JsonUtils.getBooleanProperty(entry, "favorite", false);
                    String profileUuid = sanitizeUuid(JsonUtils.getStringProperty(entry, "profileUuid", null));
                    File file = new File(skinsDir, fileName);
                    if (!file.exists()) {
                        continue;
                    }
                    try {
                        ResourceLocation texture = registerTexture(file, id);
                        Skin skin = new Skin(id, name, fileName, SkinType.getTypeById(typeId), favorite, texture, profileUuid);
                        skins.add(skin);
                    } catch (IOException io) {
                        ShindoLogger.error("Falha ao carregar a skin " + name, io);
                    }
                }
                String currentId = JsonUtils.getStringProperty(jsonObject, "currentSkin", null);
                currentSkin = getSkinById(currentId);
            } catch (Exception e) {
                ShindoLogger.error("SkinManager load error", e);
            }
        }
    }

    public void save() {
        synchronized (ioLock) {
            JsonObject jsonObject = new JsonObject();
            if (currentSkin != null) {
                jsonObject.addProperty("currentSkin", currentSkin.getId());
            }
            JsonArray skinArray = new JsonArray();
            for (Skin skin : skins) {
                JsonObject entry = new JsonObject();
                entry.addProperty("id", skin.getId());
                entry.addProperty("name", skin.getName());
                entry.addProperty("file", skin.getFileName());
                entry.addProperty("type", skin.getType().getId());
                entry.addProperty("favorite", skin.isFavorite());
                if (skin.getProfileUuid() != null && !skin.getProfileUuid().trim().isEmpty()) {
                    entry.addProperty("profileUuid", skin.getProfileUuid());
                }
                skinArray.add(entry);
            }
            jsonObject.add("skins", skinArray);

            try (FileWriter writer = new FileWriter(dataFile)) {
                gson.toJson(jsonObject, writer);
            } catch (Exception e) {
                ShindoLogger.error("SkinManager save error", e);
            }
        }
    }

    private ResourceLocation registerTexture(File file, String id) throws IOException {
        BufferedImage image = ImageIO.read(file);
        if (image == null) {
            throw new IOException("Skin inválida: " + file.getName());
        }
        return runOnRenderThread(() -> {
            DynamicTexture texture = new DynamicTexture(image);
            return Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation("skin-" + id, texture);
        });
    }

    private BufferedImage normalizeSkin(BufferedImage source) throws IOException {
        if (source == null) {
            throw new IOException("Skin inválida");
        }
        int width = source.getWidth();
        int height = source.getHeight();
        if (width != 64 || (height != 64 && height != 32)) {
            throw new IOException("A skin precisa ter 64x64 ou 64x32 pixels");
        }
        BufferedImage copy = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = copy.createGraphics();
        graphics.drawImage(source, 0, 0, null);
        graphics.dispose();
        return copy;
    }

    public File getSkinFile(Skin skin) {
        if (skin == null) {
            return null;
        }
        return resolveFile(skin);
    }

    private File resolveFile(Skin skin) {
        return new File(skinsDir, skin.getFileName());
    }

    private void validateName(String name, Skin ignore) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("O nome da skin não pode estar vazio");
        }
        for (Skin skin : skins) {
            if (skin == ignore) {
                continue;
            }
            if (skin.getName().equalsIgnoreCase(name.trim())) {
                throw new IllegalArgumentException("Já existe uma skin com esse nome");
            }
        }
    }

    private String sanitizeName(String name) {
        String trimmed = name == null ? "" : name.trim();
        return trimmed.length() > 32 ? trimmed.substring(0, 32) : trimmed;
    }

    private String normalizeUrl(String rawUrl) {
        String url = rawUrl.trim();
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }
        if (url.contains("namemc.com/skin/") && !url.endsWith(".png")) {
            url = url + ".png";
        }
        if (url.contains("namemc.com/texture/") && !url.endsWith(".png")) {
            url = url + ".png";
        }
        return url;
    }

    private String sanitizeUuid(String uuid) {
        if (uuid == null) {
            return null;
        }
        String cleaned = uuid.replace("-", "").trim();
        if (cleaned.isEmpty()) {
            return null;
        }
        return cleaned.toLowerCase(Locale.ROOT);
    }

    private String requireValidUuid(String uuid) throws IOException {
        String cleaned = sanitizeUuid(uuid);
        if (cleaned == null || cleaned.length() != 32) {
            throw new IOException("UUID inválido");
        }
        return cleaned;
    }

    private <T> T runOnRenderThread(Callable<T> task) throws IOException {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.isCallingFromMinecraftThread()) {
            try {
                return task.call();
            } catch (IOException e) {
                throw e;
            } catch (Exception e) {
                throw new IOException("Falha ao registrar textura", e);
            }
        }

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Object> ref = new AtomicReference<>();
        mc.addScheduledTask(() -> {
            try {
                ref.set(task.call());
            } catch (Exception e) {
                ref.set(e);
            } finally {
                latch.countDown();
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Thread interrompida durante o carregamento da skin", e);
        }

        Object value = ref.get();
        if (value instanceof Exception) {
            Exception exception = (Exception) value;
            if (exception instanceof IOException) {
                throw (IOException) exception;
            }
            throw new IOException("Falha ao registrar textura", exception);
        }
        @SuppressWarnings("unchecked")
        T casted = (T) value;
        return casted;
    }

    public static class DownloadedSkin {
        private final BufferedImage image;
        private final SkinType detectedType;
        private final String uuid;

        public DownloadedSkin(BufferedImage image, SkinType detectedType, String uuid) {
            this.image = image;
            this.detectedType = detectedType;
            this.uuid = uuid;
        }

        public BufferedImage getImage() {
            return image;
        }

        public SkinType getDetectedType() {
            return detectedType;
        }

        public String getUuid() {
            return uuid;
        }
    }
}

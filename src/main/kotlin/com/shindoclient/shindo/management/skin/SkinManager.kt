package com.shindoclient.shindo.management.skin

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.shindoclient.shindo.Shindo
import com.shindoclient.shindo.logger.ShindoLogger
import com.shindoclient.shindo.management.file.FileManager
import com.shindoclient.shindo.utils.JsonUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.util.ResourceLocation
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.Base64
import java.util.Collections
import java.util.Locale
import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicReference
import javax.imageio.ImageIO

class SkinManager {
    private val dataFile: File
    private val skinsDir: File
    private val gson =
        com.google.gson
            .GsonBuilder()
            .setPrettyPrinting()
            .create()
    private val ioLock = Any()
    private val skins = CopyOnWriteArrayList<Skin>()
    private var currentSkin: Skin? = null

    init {
        val fileManager: FileManager = Shindo.getInstance().getFileManager()
        skinsDir = fileManager.skinsDir
        if (!skinsDir.exists()) fileManager.createDir(skinsDir)
        dataFile = File(skinsDir, DATA_FILE)
        if (!dataFile.exists()) fileManager.createFile(dataFile)
        load()
    }

    fun getSkins(): List<Skin> = Collections.unmodifiableList(skins)

    fun getCurrentSkin(): Skin? = currentSkin

    fun setCurrentSkin(skin: Skin?) {
        synchronized(ioLock) {
            currentSkin = skin
            save()
        }
    }

    fun clearCurrentSkin() {
        synchronized(ioLock) {
            currentSkin = null
            save()
        }
    }

    fun getSkinById(id: String?): Skin? {
        if (id == null) return null
        return skins.find { it.id.equals(id, ignoreCase = true) }
    }

    fun getSkinByName(name: String?): Skin? {
        if (name == null) return null
        return skins.find { it.name.equals(name, ignoreCase = true) }
    }

    @Throws(IOException::class)
    fun addSkin(
        name: String,
        type: SkinType,
        favorite: Boolean,
        sourceImage: BufferedImage,
        profileUuid: String?,
    ): Skin =
        synchronized(ioLock) {
            val sanitizedName = sanitizeName(name)
            validateName(sanitizedName, null)
            val normalized = normalizeSkin(sourceImage)
            val id = UUID.randomUUID().toString().replace("-", "")
            val fileName = "$id.png"
            val target = File(skinsDir, fileName)
            ImageIO.write(normalized, "png", target)
            val texture = registerTexture(target, id)
            val skin = Skin(id, sanitizedName, fileName, type, favorite, texture, sanitizeUuid(profileUuid))
            skins.add(skin)
            save()
            skin
        }

    @Throws(IOException::class)
    fun updateSkin(
        skin: Skin?,
        newName: String,
        newType: SkinType,
        replacement: BufferedImage?,
        newProfileUuid: String?,
    ) {
        if (skin == null) return
        synchronized(ioLock) {
            val sanitizedName = sanitizeName(newName)
            validateName(sanitizedName, skin)
            skin.name = sanitizedName
            skin.type = newType
            if (!newProfileUuid.isNullOrBlank()) skin.profileUuid = sanitizeUuid(newProfileUuid)
            if (replacement != null) {
                val normalized = normalizeSkin(replacement)
                val target = resolveFile(skin)
                ImageIO.write(normalized, "png", target)
                skin.texture = registerTexture(target, skin.id)
            }
            save()
        }
    }

    fun deleteSkin(skin: Skin?) {
        if (skin == null) return
        synchronized(ioLock) {
            skins.remove(skin)
            val skinFile = resolveFile(skin)
            if (skinFile.exists() && !skinFile.delete()) skinFile.deleteOnExit()
            if (currentSkin == skin) currentSkin = null
            save()
        }
    }

    fun setFavorite(
        skin: Skin?,
        favorite: Boolean,
    ) {
        if (skin == null) return
        synchronized(ioLock) {
            skin.favorite = favorite
            save()
        }
    }

    @Throws(IOException::class)
    fun downloadSkinByUsername(username: String?): DownloadedSkin {
        if (username.isNullOrBlank()) throw IOException("Nome de usuário inválido")
        val trimmed = username.trim()
        val profileUrl = URL("https://api.mojang.com/users/profiles/minecraft/$trimmed")
        val profile: JsonObject? =
            profileUrl.openStream().reader(StandardCharsets.UTF_8).use {
                gson.fromJson(it, JsonObject::class.java)
            }
        if (profile == null || !profile.has("id")) throw IOException("Jogador não encontrado")
        return downloadSkinByProfileId(profile.get("id").asString)
    }

    @Throws(IOException::class)
    fun downloadSkinByUuid(uuid: String?): DownloadedSkin {
        if (uuid.isNullOrBlank()) throw IOException("Informe um UUID válido")
        return downloadSkinByProfileId(uuid!!)
    }

    @Throws(IOException::class)
    fun downloadSkinByUrl(url: String?): DownloadedSkin {
        if (url.isNullOrBlank()) throw IOException("URL inválida")
        val normalizedUrl = normalizeUrl(url!!)
        val image = ImageIO.read(URL(normalizedUrl)) ?: throw IOException("Não foi possível baixar a skin")
        return DownloadedSkin(normalizeSkin(image), SkinType.DEFAULT, null)
    }

    private fun downloadSkinByProfileId(uuid: String): DownloadedSkin {
        val normalizedUuid = requireValidUuid(uuid)
        val sessionUrl = URL("https://sessionserver.mojang.com/session/minecraft/profile/$normalizedUuid")
        val sessionProfile: JsonObject? =
            sessionUrl.openStream().reader(StandardCharsets.UTF_8).use {
                gson.fromJson(it, JsonObject::class.java)
            }
        if (sessionProfile == null ||
            !sessionProfile.has("properties")
        ) {
            throw IOException("Não foi possível carregar a skin")
        }
        val properties = sessionProfile.getAsJsonArray("properties")
        if (properties.size() == 0) throw IOException("Não foi possível carregar a skin")
        val encoded =
            properties
                .get(0)
                .asJsonObject
                .get("value")
                .asString
        val payload =
            gson.fromJson(String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8), JsonObject::class.java)
        val textures = payload.getAsJsonObject("textures") ?: throw IOException("Skin não encontrada para este UUID")
        if (!textures.has("SKIN")) throw IOException("Skin não encontrada para este UUID")
        val skinObject = textures.getAsJsonObject("SKIN")
        val slim =
            skinObject.has("metadata") &&
                skinObject.getAsJsonObject("metadata").has("model") &&
                "slim".equals(skinObject.getAsJsonObject("metadata").get("model").asString, ignoreCase = true)
        val skinUrl = skinObject.get("url").asString
        val image = ImageIO.read(URL(skinUrl)) ?: throw IOException("Não foi possível baixar a skin")
        return DownloadedSkin(normalizeSkin(image), if (slim) SkinType.SLIM else SkinType.DEFAULT, normalizedUuid)
    }

    fun load() {
        synchronized(ioLock) {
            skins.clear()
            currentSkin = null
            if (!dataFile.exists()) return
            try {
                FileReader(dataFile).use { reader ->
                    val jsonObject = gson.fromJson(reader, JsonObject::class.java) ?: JsonObject()
                    JsonUtils.getArrayProperty(jsonObject, "skins")?.let { skinArray ->
                        for (element in skinArray) {
                            if (!element.isJsonObject) continue
                            val entry = element.asJsonObject
                            val id =
                                JsonUtils.getStringProperty(entry, "id", UUID.randomUUID().toString().replace("-", ""))
                            val name = JsonUtils.getStringProperty(entry, "name", "Skin")
                            val fileName = JsonUtils.getStringProperty(entry, "file", "$id.png")
                            val typeId = JsonUtils.getIntProperty(entry, "type", SkinType.DEFAULT.id)
                            val favorite = JsonUtils.getBooleanProperty(entry, "favorite", false)
                            val profileUuid = sanitizeUuid(JsonUtils.getStringProperty(entry, "profileUuid", null))
                            val file = File(skinsDir, fileName)
                            if (!file.exists()) continue
                            try {
                                val texture = registerTexture(file, id.toString())
                                val skin =
                                    Skin(
                                        id.toString(),
                                        name.toString(),
                                        fileName.toString(),
                                        SkinType.getTypeById(typeId),
                                        favorite,
                                        texture,
                                        profileUuid,
                                    )
                                skins.add(skin)
                            } catch (io: IOException) {
                                ShindoLogger.error("Falha ao carregar a skin $name", io)
                            }
                        }
                    }
                    currentSkin = getSkinById(JsonUtils.getStringProperty(jsonObject, "currentSkin", null))
                }
            } catch (e: Exception) {
                ShindoLogger.error("SkinManager load error", e)
            }
        }
    }

    fun save() {
        synchronized(ioLock) {
            val jsonObject = JsonObject()
            currentSkin?.let { jsonObject.addProperty("currentSkin", it.id) }
            val skinArray = JsonArray()
            for (skin in skins) {
                val entry = JsonObject()
                entry.addProperty("id", skin.id)
                entry.addProperty("name", skin.name)
                entry.addProperty("file", skin.fileName)
                entry.addProperty("type", skin.type.id)
                entry.addProperty("favorite", skin.favorite)
                if (!skin.profileUuid.isNullOrBlank()) entry.addProperty("profileUuid", skin.profileUuid)
                skinArray.add(entry)
            }
            jsonObject.add("skins", skinArray)
            try {
                FileWriter(dataFile).use { gson.toJson(jsonObject, it) }
            } catch (e: Exception) {
                ShindoLogger.error("SkinManager save error", e)
            }
        }
    }

    @Throws(IOException::class)
    private fun registerTexture(
        file: File,
        id: String,
    ): ResourceLocation {
        val image = ImageIO.read(file) ?: throw IOException("Skin inválida: ${file.name}")
        return runOnRenderThread {
            val texture = DynamicTexture(image)
            Minecraft.getMinecraft().textureManager.getDynamicTextureLocation("skin-$id", texture)
        }
    }

    @Throws(IOException::class)
    private fun normalizeSkin(source: BufferedImage?): BufferedImage {
        if (source == null) throw IOException("Skin inválida")
        val width = source.width
        val height = source.height
        if (width != 64 || (height != 64 && height != 32)) throw IOException("A skin precisa ter 64x64 ou 64x32 pixels")
        val copy = BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB)
        val graphics = copy.createGraphics()
        graphics.drawImage(source, 0, 0, null)
        graphics.dispose()
        return copy
    }

    fun getSkinFile(skin: Skin?): File? = if (skin == null) null else resolveFile(skin)

    private fun resolveFile(skin: Skin): File = File(skinsDir, skin.fileName)

    private fun validateName(
        name: String?,
        ignore: Skin?,
    ) {
        if (name.isNullOrBlank()) throw IllegalArgumentException("O nome da skin não pode estar vazio")
        val trimmed = name!!.trim()
        for (skin in skins) {
            if (skin === ignore) continue
            if (skin.name.equals(
                    trimmed,
                    ignoreCase = true,
                )
            ) {
                throw IllegalArgumentException("Já existe uma skin com esse nome")
            }
        }
    }

    private fun sanitizeName(name: String?): String {
        val trimmed = name?.trim() ?: ""
        return if (trimmed.length > 32) trimmed.substring(0, 32) else trimmed
    }

    private fun normalizeUrl(rawUrl: String): String {
        var url = rawUrl.trim()
        if (!url.startsWith("http://") && !url.startsWith("https://")) url = "https://$url"
        if (url.contains("namemc.com/skin/") && !url.endsWith(".png")) url += ".png"
        if (url.contains("namemc.com/texture/") && !url.endsWith(".png")) url += ".png"
        return url
    }

    private fun sanitizeUuid(uuid: String?): String? {
        if (uuid == null) return null
        val cleaned = uuid.replace("-", "").trim()
        if (cleaned.isEmpty()) return null
        return cleaned.lowercase(Locale.ROOT)
    }

    @Throws(IOException::class)
    private fun requireValidUuid(uuid: String): String {
        val cleaned = sanitizeUuid(uuid)
        if (cleaned == null || cleaned.length != 32) throw IOException("UUID inválido")
        return cleaned
    }

    @Throws(IOException::class)
    private fun <T> runOnRenderThread(task: () -> T): T {
        val mc = Minecraft.getMinecraft()
        if (mc.isCallingFromMinecraftThread) {
            return try {
                task()
            } catch (e: IOException) {
                throw e
            } catch (e: Exception) {
                throw IOException("Falha ao registrar textura", e)
            }
        }
        val latch = CountDownLatch(1)
        val ref = AtomicReference<Any?>()
        mc.addScheduledTask {
            try {
                ref.set(task())
            } catch (e: Exception) {
                ref.set(e)
            } finally {
                latch.countDown()
            }
        }
        try {
            latch.await()
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            throw IOException("Thread interrompida durante o carregamento da skin", e)
        }
        val value = ref.get()
        if (value is Exception) {
            if (value is IOException) throw value
            throw IOException("Falha ao registrar textura", value)
        }
        @Suppress("UNCHECKED_CAST")
        return value as T
    }

    class DownloadedSkin(
        val image: BufferedImage,
        val detectedType: SkinType,
        val uuid: String?,
    )

    companion object {
        private const val DATA_FILE = "skins.json"
    }
}

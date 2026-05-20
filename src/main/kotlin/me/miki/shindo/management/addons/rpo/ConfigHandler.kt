package me.miki.shindo.management.addons.rpo

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import me.miki.shindo.logger.ShindoLogger
import net.minecraft.client.Minecraft
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.Collections

class ConfigHandler(
    private val configFile: File,
) {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val optionsInternal = Options()

    val options: Options
        get() = optionsInternal

    fun reload() {
        optionsInternal.load()
    }

    inner class Options {
        private val enabledPacks: MutableList<String> = ArrayList()

        fun getEnabledPacks(): List<String> = Collections.unmodifiableList(enabledPacks)

        fun load() {
            enabledPacks.clear()
            val stored = readFromDisk()

            if (stored.isEmpty()) {
                enabledPacks.addAll(Minecraft.getMinecraft().gameSettings.resourcePacks)
                writeToDisk(enabledPacks)
            } else {
                enabledPacks.addAll(stored)
            }
        }

        fun updateEnabledPacks() {
            enabledPacks.clear()
            enabledPacks.addAll(Minecraft.getMinecraft().gameSettings.resourcePacks)
            writeToDisk(enabledPacks)
        }

        private fun readFromDisk(): List<String> {
            val path: Path = configFile.toPath()
            if (!Files.exists(path)) {
                return emptyList()
            }

            return try {
                Files.newBufferedReader(path, StandardCharsets.UTF_8).use { reader ->
                    val json = gson.fromJson(reader, JsonObject::class.java)
                    if (json == null || !json.has(ENABLED_PACKS_KEY)) {
                        return emptyList()
                    }

                    val array: JsonArray = json.getAsJsonArray(ENABLED_PACKS_KEY)
                    val loaded: MutableList<String> = ArrayList(array.size())
                    for (element in array) {
                        loaded.add(element.asString)
                    }
                    loaded
                }
            } catch (exception: IOException) {
                ShindoLogger.error("Failed to load RPO configuration", exception)
                emptyList()
            } catch (exception: JsonParseException) {
                ShindoLogger.error("Failed to load RPO configuration", exception)
                emptyList()
            }
        }

        private fun writeToDisk(packs: List<String>) {
            val path: Path = configFile.toPath()
            try {
                val parent: Path? = path.parent
                if (parent != null) {
                    Files.createDirectories(parent)
                }

                val array = JsonArray()
                for (pack in packs) {
                    array.add(pack)
                }

                val json = JsonObject()
                json.add(ENABLED_PACKS_KEY, array)

                Files
                    .newBufferedWriter(
                        path,
                        StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING,
                        StandardOpenOption.WRITE,
                    ).use { writer ->
                        gson.toJson(json, writer)
                    }
            } catch (exception: IOException) {
                ShindoLogger.error("Failed to save RPO configuration", exception)
            }
        }
    }

    companion object {
        private const val ENABLED_PACKS_KEY = "enabledPacks"
    }
}

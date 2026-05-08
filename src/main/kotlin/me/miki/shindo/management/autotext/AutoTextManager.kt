package me.miki.shindo.management.autotext

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import me.miki.shindo.Shindo
import me.miki.shindo.logger.ShindoLogger
import me.miki.shindo.utils.JsonUtils
import org.lwjgl.input.Keyboard
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

class AutoTextManager {

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val entries = CopyOnWriteArrayList<AutoTextEntry>()
    private val file: File

    init {
        val fileManager = Shindo.getInstance().getFileManager()
        file = File(fileManager.shindoDir, FILE_NAME)
        fileManager.createFile(file)
        load()
    }

    fun getEntries(): MutableList<AutoTextEntry> = entries

    fun createEntry(): AutoTextEntry {
        val entry = AutoTextEntry(
            UUID.randomUUID().toString().replace("-", ""),
            "",
            "",
            Keyboard.KEY_NONE
        )
        entries.add(entry)
        save()
        return entry
    }

    fun removeEntry(id: String) {
        entries.removeIf { it.id == id }
        save()
    }

    fun save() {
        try {
            val root = JsonObject()
            val array = JsonArray()
            for (entry in entries) {
                val obj = JsonObject()
                obj.addProperty("id", entry.id)
                obj.addProperty("name", entry.name)
                obj.addProperty("textOrCommand", entry.textOrCommand)
                obj.addProperty("keyCode", entry.keyCode)
                array.add(obj)
            }
            root.add("entries", array)
            FileWriter(file).use { writer -> gson.toJson(root, writer) }
        } catch (e: Exception) {
            ShindoLogger.error("Failed to save AutoText entries", e)
        }
    }

    fun load() {
        entries.clear()
        if (!file.exists()) {
            return
        }

        try {
            FileReader(file).use { reader ->
                val root = gson.fromJson(reader, JsonObject::class.java) ?: return
                val array = JsonUtils.getArrayProperty(root, "entries")
                for (element in array) {
                    if (!element.isJsonObject) continue
                    val obj = element.asJsonObject
                    val id = JsonUtils.getStringProperty(obj, "id", UUID.randomUUID().toString().replace("-", ""))
                        ?: continue
                    val name = JsonUtils.getStringProperty(obj, "name", "") ?: ""
                    val text = JsonUtils.getStringProperty(obj, "textOrCommand", "") ?: ""
                    val keyCode = JsonUtils.getIntProperty(obj, "keyCode", Keyboard.KEY_NONE)
                    entries.add(AutoTextEntry(id, name, text, keyCode))
                }
            }
        } catch (e: Exception) {
            ShindoLogger.error("Failed to load AutoText entries", e)
        }
    }

    companion object {
        private const val FILE_NAME = "autotext.json"
    }
}

package me.miki.shindo.management.mods.impl.crosshair

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import me.miki.shindo.Shindo
import me.miki.shindo.utils.JsonUtils
import me.miki.shindo.utils.JsonUtils.getStringProperty
import me.miki.shindo.utils.JsonUtils.parseBooleanGrid
import me.miki.shindo.utils.JsonUtils.parseIntGrid
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.*
import kotlin.math.min

@Suppress("UNUSED")
class LayoutManager {
    private val preset4 =
        arrayOf<BooleanArray?>(
            booleanArrayOf(false, false, false, false, false, false, false, false, false, false, false),
            booleanArrayOf(false, false, false, false, false, false, false, false, false, false, false),
            booleanArrayOf(false, false, false, false, false, false, false, false, false, false, false),
            booleanArrayOf(false, false, false, false, false, false, false, false, false, false, false),
            booleanArrayOf(false, false, false, false, false, false, false, false, false, false, false),
            booleanArrayOf(false, false, false, false, false, true, false, false, false, false, false),
            booleanArrayOf(false, false, false, false, false, false, false, false, false, false, false),
            booleanArrayOf(false, false, false, false, false, false, false, false, false, false, false),
            booleanArrayOf(false, false, false, false, false, false, false, false, false, false, false),
            booleanArrayOf(false, false, false, false, false, false, false, false, false, false, false),
            booleanArrayOf(false, false, false, false, false, false, false, false, false, false, false),
        )
    private val preset5 =
        arrayOf<BooleanArray?>(
            booleanArrayOf(false, false, false, false, false, false, false, false, false, false, false),
            booleanArrayOf(false, false, false, false, false, false, false, false, false, false, false),
            booleanArrayOf(false, false, false, false, false, false, false, false, false, false, false),
            booleanArrayOf(false, false, false, false, true, true, true, false, false, false, false),
            booleanArrayOf(false, false, false, true, true, false, true, true, false, false, false),
            booleanArrayOf(false, false, false, true, false, false, false, true, false, false, false),
            booleanArrayOf(false, false, false, true, true, false, true, true, false, false, false),
            booleanArrayOf(false, false, false, false, true, true, true, false, false, false, false),
            booleanArrayOf(false, false, false, false, false, false, false, false, false, false, false),
            booleanArrayOf(false, false, false, false, false, false, false, false, false, false, false),
            booleanArrayOf(false, false, false, false, false, false, false, false, false, false, false),
        )
    private val preset7 =
        arrayOf<BooleanArray?>(
            booleanArrayOf(false, false, false, false, false, false, false, false, false, false, false),
            booleanArrayOf(false, false, false, false, false, false, false, false, false, false, false),
            booleanArrayOf(false, false, false, false, false, false, false, false, false, false, false),
            booleanArrayOf(false, false, false, true, false, false, false, true, false, false, false),
            booleanArrayOf(false, false, false, false, true, false, true, false, false, false, false),
            booleanArrayOf(false, false, false, false, false, true, false, false, false, false, false),
            booleanArrayOf(false, false, false, false, true, false, true, false, false, false, false),
            booleanArrayOf(false, false, false, true, false, false, false, true, false, false, false),
            booleanArrayOf(false, false, false, false, false, false, false, false, false, false, false),
            booleanArrayOf(false, false, false, false, false, false, false, false, false, false, false),
            booleanArrayOf(false, false, false, false, false, false, false, false, false, false, false),
        )
    private val preset8 =
        arrayOf<BooleanArray?>(
            booleanArrayOf(false, false, false, false, false, false, false, false, false, false, false),
            booleanArrayOf(false, false, false, false, false, true, false, false, false, false, false),
            booleanArrayOf(false, false, false, true, false, true, false, true, false, false, false),
            booleanArrayOf(false, false, true, false, false, true, false, false, true, false, false),
            booleanArrayOf(false, false, false, false, false, false, false, false, false, false, false),
            booleanArrayOf(false, true, true, true, false, true, false, true, true, true, false),
            booleanArrayOf(false, false, false, false, false, false, false, false, false, false, false),
            booleanArrayOf(false, false, true, false, false, true, false, false, true, false, false),
            booleanArrayOf(false, false, false, true, false, true, false, true, false, false, false),
            booleanArrayOf(false, false, false, false, false, true, false, false, false, false, false),
            booleanArrayOf(false, false, false, false, false, false, false, false, false, false, false),
        )
    private val preset11 =
        arrayOf<BooleanArray?>(
            booleanArrayOf(false, false, false, false, false, false, false, false, false, false, false),
            booleanArrayOf(false, false, false, false, false, false, false, false, false, false, false),
            booleanArrayOf(false, false, false, false, false, true, false, false, false, false, false),
            booleanArrayOf(false, false, false, false, true, true, true, false, false, false, false),
            booleanArrayOf(false, false, false, true, true, false, true, true, false, false, false),
            booleanArrayOf(false, false, true, true, false, true, false, true, true, false, false),
            booleanArrayOf(false, false, false, true, true, false, true, true, false, false, false),
            booleanArrayOf(false, false, false, false, true, true, true, false, false, false, false),
            booleanArrayOf(false, false, false, false, false, true, false, false, false, false, false),
            booleanArrayOf(false, false, false, false, false, false, false, false, false, false, false),
            booleanArrayOf(false, false, false, false, false, false, false, false, false, false, false),
        )
    private val preset13 =
        arrayOf<BooleanArray?>(
            booleanArrayOf(false, false, false, false, false, false, false, false, false, false, false),
            booleanArrayOf(false, false, false, false, false, false, false, false, false, false, false),
            booleanArrayOf(false, false, false, false, false, false, false, false, false, false, false),
            booleanArrayOf(false, false, true, true, false, false, false, true, true, false, false),
            booleanArrayOf(false, false, true, false, false, false, false, false, true, false, false),
            booleanArrayOf(false, false, true, false, false, true, false, false, true, false, false),
            booleanArrayOf(false, false, true, false, false, false, false, false, true, false, false),
            booleanArrayOf(false, false, true, true, false, false, false, true, true, false, false),
            booleanArrayOf(false, false, false, false, false, false, false, false, false, false, false),
            booleanArrayOf(false, false, false, false, false, false, false, false, false, false, false),
            booleanArrayOf(false, false, false, false, false, false, false, false, false, false, false),
        )
    private val preset14 =
        arrayOf<BooleanArray?>(
            booleanArrayOf(false, false, false, false, false, false, false, false, false, false, false),
            booleanArrayOf(false, false, false, false, false, false, false, false, false, false, false),
            booleanArrayOf(false, false, false, true, true, true, true, true, false, false, false),
            booleanArrayOf(false, false, false, true, false, false, false, true, false, false, false),
            booleanArrayOf(false, false, false, false, false, false, false, false, false, false, false),
            booleanArrayOf(false, false, false, false, false, true, false, false, false, false, false),
            booleanArrayOf(false, false, false, false, false, false, false, false, false, false, false),
            booleanArrayOf(false, false, false, true, false, false, false, true, false, false, false),
            booleanArrayOf(false, false, false, true, true, true, true, true, false, false, false),
            booleanArrayOf(false, false, false, false, false, false, false, false, false, false, false),
            booleanArrayOf(false, false, false, false, false, false, false, false, false, false, false),
        )
    private val preset15 =
        arrayOf<BooleanArray?>(
            booleanArrayOf(false, false, false, false, false, false, false, false, false, false, false),
            booleanArrayOf(false, false, false, false, false, true, false, false, false, false, false),
            booleanArrayOf(false, false, false, false, false, true, false, false, false, false, false),
            booleanArrayOf(false, false, false, false, true, false, true, false, false, false, false),
            booleanArrayOf(false, false, false, true, false, false, false, true, false, false, false),
            booleanArrayOf(false, true, true, false, false, true, false, false, true, true, false),
            booleanArrayOf(false, false, false, true, false, false, false, true, false, false, false),
            booleanArrayOf(false, false, false, false, true, false, true, false, false, false, false),
            booleanArrayOf(false, false, false, false, false, true, false, false, false, false, false),
            booleanArrayOf(false, false, false, false, false, true, false, false, false, false, false),
            booleanArrayOf(false, false, false, false, false, false, false, false, false, false, false),
        )
    private val preset16 =
        arrayOf<BooleanArray?>(
            booleanArrayOf(false, false, false, false, false, false, false, false, false, false, false),
            booleanArrayOf(false, false, false, false, false, false, false, false, false, false, false),
            booleanArrayOf(false, false, false, false, false, false, false, false, false, false, false),
            booleanArrayOf(false, false, false, false, false, false, false, false, false, false, false),
            booleanArrayOf(false, false, false, false, false, true, false, false, false, false, false),
            booleanArrayOf(false, false, false, false, true, true, true, false, false, false, false),
            booleanArrayOf(false, false, false, false, false, true, false, false, false, false, false),
            booleanArrayOf(false, false, false, false, false, false, false, false, false, false, false),
            booleanArrayOf(false, false, false, false, false, false, false, false, false, false, false),
            booleanArrayOf(false, false, false, false, false, false, false, false, false, false, false),
            booleanArrayOf(false, false, false, false, false, false, false, false, false, false, false),
        )

    private val userPresets: MutableList<CellGridPreset> = ArrayList<CellGridPreset>()
    private val presetFile: File

    init {
        val fileManager = Shindo.getInstance().getFileManager()
        this.presetFile = File(fileManager.shindoDir, PRESET_FILE_NAME)
        init()
    }

    fun init() {
        userPresets.clear()
        loadFromDisk()

        if (userPresets.isEmpty()) {
            seedDefaults()
        }
    }

    val defaultLayout: Array<BooleanArray?>
        get() = copyGrid(preset4)

    val customPresets: MutableList<CellGridPreset?>
        get() = ArrayList<CellGridPreset?>(userPresets)

    fun addCustomPreset(
        name: String?,
        layout: Array<BooleanArray?>,
        colors: Array<IntArray?>,
    ): CellGridPreset = addOrUpdatePreset(null, layout, colors, name)

    fun addOrUpdatePreset(
        id: String?,
        layout: Array<BooleanArray?>,
        colors: Array<IntArray?>,
        nameOverride: String?,
    ): CellGridPreset {
        val target = if (id == null) null else getPresetById(id)

        if (target != null) {
            target.update(layout, colors)
            saveToDisk()
            return target
        }

        val preset =
            CellGridPreset(
                id ?: UUID.randomUUID().toString(),
                nameOverride ?: "",
                layout,
                colors,
                true,
            )
        userPresets.removeIf { p: CellGridPreset? -> p!!.id == preset.id }
        userPresets.add(preset)
        enforceCustomLimit()
        saveToDisk()
        return preset
    }

    fun getPresetById(id: String?): CellGridPreset? {
        if (id == null) {
            return null
        }
        for (preset in userPresets) {
            if (id == preset.id) {
                return preset
            }
        }
        return null
    }

    fun removePreset(preset: CellGridPreset?) {
        if (preset == null) {
            return
        }
        userPresets.removeIf { entry: CellGridPreset? -> entry == preset || entry!!.id == preset.id }
        saveToDisk()
    }

    private fun enforceCustomLimit() {
        while (userPresets.size > MAX_CUSTOM_PRESETS) {
            userPresets.removeAt(0)
        }
    }

    private fun seedDefaults() {
        addOrUpdatePreset("seed-dot", preset4, arrayOfNulls(0), "dot")
        addOrUpdatePreset("seed-diamond", preset5, arrayOfNulls(0), "diamond")
        addOrUpdatePreset("seed-star", preset8, arrayOfNulls(0), "star")
    }

    private fun loadFromDisk() {
        try {
            if (!presetFile.exists()) {
                presetFile.createNewFile()
                return
            }

            FileReader(presetFile).use { reader ->
                val gson = GsonBuilder().setPrettyPrinting().create()
                val json = gson.fromJson(reader, JsonObject::class.java)
                if (json == null || !json.has("presets")) {
                    return
                }

                val array = json.getAsJsonArray("presets")
                val iterator = array.iterator()
                while (iterator.hasNext() && userPresets.size < MAX_CUSTOM_PRESETS) {
                    val element = iterator.next()!!.asJsonObject
                    val layout: Array<BooleanArray?> = parseBooleanGrid(element.get("layout"))
                    val colors: Array<IntArray?> = parseIntGrid(element.get("colors"))
                    val id = getStringProperty(element, "id", UUID.randomUUID().toString())
                    val name = getStringProperty(element, "name", "")
                    userPresets.add(CellGridPreset(id, name, layout, colors, true))
                }
            }
        } catch (ignored: Exception) {
        }
    }

    fun saveToDisk() {
        try {
            FileWriter(presetFile).use { writer ->
                val gson = GsonBuilder().setPrettyPrinting().create()
                val root = JsonObject()
                val presetsArray = JsonArray()

                for (preset in userPresets) {
                    val entry = JsonObject()
                    entry.addProperty("id", preset.id)
                    entry.addProperty("name", preset.name)
                    entry.add("layout", JsonUtils.toBooleanGrid(preset.layoutCopy))
                    entry.add("colors", JsonUtils.toIntGrid(preset.colorCopy))
                    presetsArray.add(entry)
                }

                root.add("presets", presetsArray)
                gson.toJson(root, writer)
            }
        } catch (ignored: Exception) {
        }
    }

    class CellGridPreset(
        val id: String?,
        val name: String?,
        layout: Array<BooleanArray?>,
        colors: Array<IntArray?>,
        val isUserPreset: Boolean,
    ) {
        private var layout: Array<BooleanArray?>
        private var colors: Array<IntArray?>

        init {
            this.layout = copyGrid(layout)
            this.colors = copyColors(colors, layout)
        }

        val layoutCopy: Array<BooleanArray?>
            get() = copyGrid(layout)

        val colorCopy: Array<IntArray?>
            get() = copyColors(colors, layout)

        fun update(
            newLayout: Array<BooleanArray?>,
            newColors: Array<IntArray?>,
        ) {
            this.layout = copyGrid(newLayout)
            this.colors = copyColors(newColors, newLayout)
        }
    }

    companion object {
        const val MAX_CUSTOM_PRESETS: Int = 8
        private const val DEFAULT_PRESET_COLOR = -0x10000
        private const val PRESET_FILE_NAME = "CrosshairPresets.json"

        private fun copyGrid(source: Array<BooleanArray?>): Array<BooleanArray?> {
            val copy = arrayOfNulls<BooleanArray>(source.size)
            for (i in source.indices) {
                val row = source[i]
                copy[i] = row!!.clone()
            }
            return copy
        }

        private fun copyColors(
            source: Array<IntArray?>,
            layout: Array<BooleanArray?>,
        ): Array<IntArray?> {
            val copy = arrayOfNulls<IntArray>(layout.size)
            for (i in layout.indices) {
                val length = layout[i]!!.size
                copy[i] = IntArray(length)
                if (i < source.size) {
                    System.arraycopy(source[i]!!, 0, copy[i]!!, 0, min(length, source[i]!!.size))
                }
                for (j in 0 until length) {
                    if (copy[i]!![j] == 0) {
                        copy[i]!![j] = DEFAULT_PRESET_COLOR
                    }
                }
            }
            return copy
        }
    }
}

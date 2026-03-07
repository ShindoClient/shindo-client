package me.miki.shindo.management.profile.mainmenu

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import me.miki.shindo.Shindo
import me.miki.shindo.logger.ShindoLogger
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.profile.mainmenu.impl.Background
import me.miki.shindo.management.profile.mainmenu.impl.CustomBackground
import me.miki.shindo.management.profile.mainmenu.impl.DefaultBackground
import me.miki.shindo.management.profile.mainmenu.impl.PanoramaBackground
import me.miki.shindo.utils.JsonUtils
import me.miki.shindo.utils.file.FileUtils
import net.minecraft.util.ResourceLocation
import java.io.File
import java.util.concurrent.CopyOnWriteArrayList


class BackgroundManager {

    private val backgrounds = CopyOnWriteArrayList<Background>()
    private val removeBackgrounds = CopyOnWriteArrayList<CustomBackground>()
    private var currentBackground: Background? = null

    init {
        val fileManager = Shindo.getInstance().fileManager
        val bgCacheDir = File(fileManager.cacheDir, "background")
        val dataJson = File(bgCacheDir, "Data.json")

        if (!bgCacheDir.exists()) fileManager.createDir(bgCacheDir)
        if (!dataJson.exists()) fileManager.createFile(dataJson)

        backgrounds.add(DefaultBackground(0, TranslateText.BUTTERFLY, ResourceLocation("shindo/mainmenu/background-butterfly.png")))
        backgrounds.add(DefaultBackground(1, TranslateText.NIGHT, ResourceLocation("shindo/mainmenu/background-night.png")))
        backgrounds.add(DefaultBackground(2, TranslateText.DOLPHIN, ResourceLocation("shindo/mainmenu/background-dolphin.png")))
        backgrounds.add(PanoramaBackground(3, TranslateText.PANO))
        backgrounds.add(DefaultBackground(999, TranslateText.ADD, null))

        val removeImages = load()
        bgCacheDir.listFiles()?.forEach { f ->
            if (FileUtils.getExtension(f) == "png") {
                if (removeImages.isNotEmpty() && removeImages.contains(f.name)) {
                    f.delete()
                } else {
                    addCustomBackground(f)
                }
            }
        }

        currentBackground = getBackgroundById(0)
    }

    fun load(): ArrayList<String> {
        val fileManager = Shindo.getInstance().fileManager
        val bgCacheDir = File(fileManager.cacheDir, "background")
        val dataJson = File(bgCacheDir, "Data.json")
        val output = ArrayList<String>()

        try {
            dataJson.reader().use { reader ->
                val gson = Gson()
                val jsonObject = gson.fromJson(reader, JsonObject::class.java) ?: return output
                val jsonArray = JsonUtils.getArrayProperty(jsonObject, "Remove Images")
                for (element in jsonArray) {
                    val rObj = gson.fromJson(element, JsonObject::class.java)
                    output.add(JsonUtils.getStringProperty(rObj, "Image", "null").toString())
                }
            }
        } catch (e: Exception) {
            ShindoLogger.error("An error occurred while loading backgrounds.", e)
        }
        return output
    }

    fun save() {
        val fileManager = Shindo.getInstance().fileManager
        val bgCacheDir = File(fileManager.cacheDir, "background")
        val dataJson = File(bgCacheDir, "Data.json")

        try {
            dataJson.writer().use { writer ->
                val jsonObject = JsonObject()
                val jsonArray = JsonArray()
                val gson = Gson()
                for (bg in removeBackgrounds) {
                    val inner = JsonObject()
                    inner.addProperty("Image", bg.getImage().name)
                    jsonArray.add(inner)
                }
                jsonObject.add("Remove Images", jsonArray)
                gson.toJson(jsonObject, writer)
            }
        } catch (e: Exception) {
            ShindoLogger.error("An error occurred while saving backgrounds.", e)
        }
    }

    fun getBackgroundById(id: Int): Background {
        backgrounds.firstOrNull { it.getId() == id }?.let { return it }
        return getBackgroundById(0)
    }

    private fun getMaxId(): Int {
        var maxId = 0
        for (bg in backgrounds) {
            if (bg.getId() != 999 && bg.getId() > maxId) {
                maxId = bg.getId()
            }
        }
        return maxId
    }

    fun addCustomBackground(image: File) {
        val maxId = getMaxId()
        val index = backgrounds.indexOf(getBackgroundById(999))
        backgrounds.add(index, CustomBackground(maxId + 1, image.name.replace(".png", ""), image))
    }

    fun removeCustomBackground(cusBackground: CustomBackground) {
        Shindo.getInstance().nanoVGManager?.let { nvg ->
            nvg.getAssetManager()!!.removeImage(nvg.getContext(), cusBackground.getImage())
        }
        backgrounds.remove(cusBackground)
        removeBackgrounds.add(cusBackground)
        save()
    }

    fun getCurrentBackground(): Background? = currentBackground
    fun setCurrentBackground(bg: Background?) { currentBackground = bg }
    fun getBackgrounds(): CopyOnWriteArrayList<Background> = backgrounds
}

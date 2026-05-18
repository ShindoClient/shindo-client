package me.miki.shindo.management.screenshot

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import me.miki.shindo.Shindo
import me.miki.shindo.utils.JsonUtils
import me.miki.shindo.utils.file.FileUtils
import java.io.File
import java.util.concurrent.CopyOnWriteArrayList

class ScreenshotManager {
    private val screenshots = CopyOnWriteArrayList<Screenshot>()
    private val removeScreenshots = CopyOnWriteArrayList<File>()
    private var prevSize = 0

    init {
        val fileManager = Shindo.getInstance().getFileManager()
        val screenshotCacheDir = File(fileManager.cacheDir, "screenshot")
        val dataJson = File(screenshotCacheDir, "Data.json")
        val toRemove = loadData()

        if (!screenshotCacheDir.exists()) fileManager.createDir(screenshotCacheDir)
        if (!dataJson.exists()) fileManager.createFile(dataJson)

        fileManager.screenshotDir.listFiles()?.forEach { f ->
            if (toRemove.isNotEmpty() && toRemove.contains(f.name)) {
                f.delete()
            }
        }
        loadScreenshots()
    }

    private fun loadData(): ArrayList<String> {
        val fileManager = Shindo.getInstance().getFileManager()
        val screenshotCacheDir = File(fileManager.cacheDir, "screenshot")
        val dataJson = File(screenshotCacheDir, "Data.json")
        val output = ArrayList<String>()

        try {
            dataJson.reader().use { reader ->
                val gson = Gson()
                val jsonObject = gson.fromJson(reader, JsonObject::class.java) ?: return output
                val jsonArray = JsonUtils.getArrayProperty(jsonObject, "Remove Screenshots")
                for (element in jsonArray) {
                    val rObj = gson.fromJson(element, JsonObject::class.java)
                    output.add(JsonUtils.getStringProperty(rObj, "Screenshot", "null").toString())
                }
            }
        } catch (_: Exception) {
        }
        return output
    }

    private fun saveData() {
        val fileManager = Shindo.getInstance().getFileManager()
        val screenshotCacheDir = File(fileManager.cacheDir, "screenshot")
        val dataJson = File(screenshotCacheDir, "Data.json")

        try {
            dataJson.writer().use { writer ->
                val jsonObject = JsonObject()
                val jsonArray = JsonArray()
                val gson = Gson()
                for (f in removeScreenshots) {
                    val inner = JsonObject()
                    inner.addProperty("Screenshot", f.name)
                    jsonArray.add(inner)
                }
                jsonObject.add("Remove Screenshots", jsonArray)
                gson.toJson(jsonObject, writer)
            }
        } catch (_: Exception) {
        }
    }

    fun loadScreenshots() {
        val screenshotDir = Shindo.getInstance().getFileManager().screenshotDir
        val files = screenshotDir.listFiles() ?: return
        if (prevSize != files.size) {
            prevSize = files.size
            for (f in files) {
                if (FileUtils.getExtension(f) == "png" &&
                    !removeScreenshots.contains(f) &&
                    getScreenshotByFile(f) == null
                ) {
                    screenshots.add(Screenshot(f))
                    Shindo.getInstance().nanoVGManager?.loadImage(f)
                }
            }
        }
    }

    fun getNextScreenshot(current: Screenshot): Screenshot {
        val max = screenshots.size
        var index = screenshots.indexOf(current)
        index = if (index < max - 1) index + 1 else 0
        return screenshots[index]
    }

    fun getBackScreenshot(current: Screenshot): Screenshot {
        val max = screenshots.size
        var index = screenshots.indexOf(current)
        index = if (index > 0) index - 1 else max - 1
        return screenshots[index]
    }

    fun getScreenshotByFile(file: File): Screenshot? = screenshots.firstOrNull { it.getImage() == file }

    fun delete(screenshot: Screenshot) {
        removeScreenshots.add(screenshot.getImage())
        screenshots.remove(screenshot)
        saveData()
        loadScreenshots()
    }

    fun getScreenshots(): CopyOnWriteArrayList<Screenshot> = screenshots
}

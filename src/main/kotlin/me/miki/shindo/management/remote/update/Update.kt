package me.miki.shindo.management.remote.update

import com.google.gson.JsonObject
import me.miki.shindo.Shindo
import me.miki.shindo.utils.JsonUtils
import me.miki.shindo.utils.Multithreading
import me.miki.shindo.utils.network.HttpUtils

class Update {

    var updateLink: String = "https://shindoclient.com/"
    var versionString: String = "something is broken lmao"
    var buildID: Int = 0

    fun check() {
        try {
            Multithreading.runAsync { checkUpdates() }
        } catch (_: Exception) { }
    }

    fun checkForUpdates() {
        val g = Shindo.getInstance()
        if (g.verIdentifier < buildID) g.updateNeeded = true
    }

    private fun checkUpdates() {
        val jsonObject = HttpUtils.readJson("https://cdn.shindoclient.com/data/meta/client.json", null) ?: return
        updateLink = JsonUtils.getStringProperty(jsonObject, "updatelink", "https://shindoclient.com/").toString()
        versionString = JsonUtils.getStringProperty(jsonObject, "latestversionstring", "something is broken lmao").toString()
        buildID = JsonUtils.getIntProperty(jsonObject, "latestversion", 0)
        checkForUpdates()
    }
}

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

    private fun applyLegacyMeta(jsonObject: JsonObject) {
        updateLink = JsonUtils.getStringProperty(jsonObject, "updatelink", updateLink).toString()
        versionString = JsonUtils.getStringProperty(jsonObject, "latestversionstring", versionString).toString()
        buildID = JsonUtils.getIntProperty(jsonObject, "latestversion", buildID)
    }

    private fun applyVersioningMeta(jsonObject: JsonObject): Boolean {
        val stableBuild = JsonUtils.getIntProperty(jsonObject, "channels,stable,build", 0)
        val latestBuild = JsonUtils.getIntProperty(jsonObject, "latest,build", 0)
        val resolvedBuild = if (stableBuild > 0) stableBuild else latestBuild
        if (resolvedBuild <= 0) return false

        val semver = JsonUtils.getStringProperty(jsonObject, "channels,stable,semver", null)
            ?: JsonUtils.getStringProperty(jsonObject, "latest,semver", versionString)
            ?: versionString

        val releaseUrl = JsonUtils.getStringProperty(jsonObject, "links,clientRelease", null)
            ?: JsonUtils.getStringProperty(jsonObject, "legacy,updatelink", updateLink)
            ?: updateLink

        updateLink = releaseUrl
        versionString = semver
        buildID = resolvedBuild
        return true
    }

    private fun checkUpdates() {
        val versioning = HttpUtils.readJson("https://cdn.shindoclient.com/data/meta/versioning.json", null)
        if (versioning != null && applyVersioningMeta(versioning)) {
            checkForUpdates()
            return
        }

        val legacy = HttpUtils.readJson("https://cdn.shindoclient.com/data/meta/client.json", null) ?: return
        applyLegacyMeta(legacy)
        checkForUpdates()
    }
}

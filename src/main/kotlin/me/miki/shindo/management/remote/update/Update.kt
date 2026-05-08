package me.miki.shindo.management.remote.update

import com.google.gson.JsonObject
import me.miki.shindo.Shindo
import me.miki.shindo.logger.ShindoLogger
import me.miki.shindo.utils.JsonUtils
import me.miki.shindo.utils.concurrent.TaskExecutor
import me.miki.shindo.utils.concurrent.ThreadPoolType
import me.miki.shindo.utils.network.HttpUtils

class Update {

    var updateLink: String = "https://shindoclient.com/"
    var versionString: String = "unknown"
    var buildID: Int = 0
    var buildId: String = "0.0"
    var type: String = "stable"

    fun check() {
        try {
            TaskExecutor.runAsync(ThreadPoolType.NETWORK) { checkUpdates() }
        } catch (e: Exception) {
            ShindoLogger.error("Update.check", e)
        }
    }

    fun checkForUpdates() {
        val g = Shindo.getInstance()
        val localBuild = g.getBuildInfo().build
        val localBuildId = g.getBuildInfo().buildId
        g.setUpdateNeeded(compareBuild(localBuild, localBuildId, buildID, buildId) < 0)
    }

    private fun applyLegacyMeta(jsonObject: JsonObject) {
        updateLink = JsonUtils.getStringProperty(jsonObject, "updatelink", updateLink).toString()
        versionString = JsonUtils.getStringProperty(jsonObject, "latestversionstring", versionString).toString()
        buildID = JsonUtils.getIntProperty(jsonObject, "latestversion", buildID)
        buildId = JsonUtils.getStringProperty(jsonObject, "latestbuildid", buildId) ?: "$buildID.1"
        type = JsonUtils.getStringProperty(jsonObject, "latesttype", type) ?: "stable"
    }

    private fun applyVersioningMeta(jsonObject: JsonObject): Boolean {
        val stableBuild = JsonUtils.getIntProperty(jsonObject, "channels,stable,build", 0)
        val latestBuild = JsonUtils.getIntProperty(jsonObject, "latest,build", 0)
        val resolvedBuild = if (stableBuild > 0) stableBuild else latestBuild
        if (resolvedBuild <= 0) return false

        val semver = JsonUtils.getStringProperty(jsonObject, "channels,stable,semver", null)
            ?: JsonUtils.getStringProperty(jsonObject, "latest,semver", versionString)
            ?: versionString

        val incomingBuildId = JsonUtils.getStringProperty(jsonObject, "channels,stable,buildId", null)
            ?: JsonUtils.getStringProperty(jsonObject, "latest,buildId", null)
            ?: "$resolvedBuild.1"

        val incomingType = JsonUtils.getStringProperty(jsonObject, "channels,stable,type", null)
            ?: JsonUtils.getStringProperty(jsonObject, "latest,type", null)
            ?: "stable"

        val releaseUrl = JsonUtils.getStringProperty(jsonObject, "links,clientRelease", null)
            ?: JsonUtils.getStringProperty(jsonObject, "legacy,updatelink", updateLink)
            ?: updateLink

        updateLink = releaseUrl
        versionString = semver
        buildID = resolvedBuild
        buildId = incomingBuildId
        type = incomingType
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

    private fun parseBuildId(value: String?): Pair<Int, Int>? {
        if (value == null) return null
        val regex = Regex("^(\\d+)\\.(\\d+)$")
        val match = regex.find(value.trim()) ?: return null
        val major = match.groupValues[1].toIntOrNull() ?: return null
        val minor = match.groupValues[2].toIntOrNull() ?: return null
        return Pair(major, minor)
    }

    private fun compareBuild(localBuild: Int, localBuildId: String, remoteBuild: Int, remoteBuildId: String): Int {
        if (localBuild != remoteBuild) return localBuild.compareTo(remoteBuild)
        val localParsed = parseBuildId(localBuildId)
        val remoteParsed = parseBuildId(remoteBuildId)
        if (localParsed == null || remoteParsed == null) return localBuildId.compareTo(remoteBuildId)
        if (localParsed.first != remoteParsed.first) return localParsed.first.compareTo(remoteParsed.first)
        return localParsed.second.compareTo(remoteParsed.second)
    }
}

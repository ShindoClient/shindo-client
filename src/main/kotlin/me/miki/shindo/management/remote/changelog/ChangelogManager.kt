package me.miki.shindo.management.remote.changelog

import com.google.gson.Gson
import com.google.gson.JsonObject
import me.miki.shindo.Shindo
import me.miki.shindo.utils.JsonUtils
import me.miki.shindo.utils.concurrent.TaskExecutor
import me.miki.shindo.utils.concurrent.ThreadPoolType
import me.miki.shindo.utils.network.HttpUtils
import java.util.concurrent.CopyOnWriteArrayList

class ChangelogManager {

    private val changelogs = CopyOnWriteArrayList<Changelog>()

    init {
        TaskExecutor.runAsync(ThreadPoolType.NETWORK) { loadChangelog() }
    }

    private fun loadChangelog() {
        val jsonObject = HttpUtils.readJson(
            "https://cdn.shindoclient.com/data/changelogs/versions/${Shindo.getInstance().verIdentifier}.json",
            null
        ) ?: return
        val jsonArray = JsonUtils.getArrayProperty(jsonObject, "changelogs") ?: return
        val gson = Gson()
        for (jsonElement in jsonArray) {
            val changelogJsonObject = gson.fromJson(jsonElement, JsonObject::class.java)
            changelogs.add(
                Changelog(
                    JsonUtils.getStringProperty(changelogJsonObject, "text", "null").toString(),
                    ChangelogType.getTypeById(JsonUtils.getIntProperty(changelogJsonObject, "type", 999))
                )
            )
        }
    }

    fun getChangelogs(): CopyOnWriteArrayList<Changelog> = changelogs
}

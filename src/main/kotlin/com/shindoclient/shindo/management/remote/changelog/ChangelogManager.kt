package com.shindoclient.shindo.management.remote.changelog

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.shindoclient.shindo.Shindo
import com.shindoclient.shindo.utils.JsonUtils
import com.shindoclient.shindo.utils.concurrent.TaskExecutor
import com.shindoclient.shindo.utils.concurrent.ThreadPoolType
import com.shindoclient.shindo.utils.network.HttpUtils
import java.util.concurrent.CopyOnWriteArrayList

class ChangelogManager {
    private val changelogs = CopyOnWriteArrayList<Changelog>()

    init {
        TaskExecutor.runAsync(ThreadPoolType.NETWORK) { loadChangelog() }
    }

    private fun loadChangelog() {
        val jsonObject =
            HttpUtils.readJson(
                "https://cdn.shindoclient.com/data/changelogs/versions/${Shindo.getInstance().getVerIdentifier()}.json",
                null,
            ) ?: return
        val jsonArray = JsonUtils.getArrayProperty(jsonObject, "changelogs")
        val gson = Gson()
        for (jsonElement in jsonArray) {
            val changelogJsonObject = gson.fromJson(jsonElement, JsonObject::class.java)
            changelogs.add(
                Changelog(
                    JsonUtils.getStringProperty(changelogJsonObject, "text", "null").toString(),
                    ChangelogType.getTypeById(JsonUtils.getIntProperty(changelogJsonObject, "type", 999)),
                ),
            )
        }
    }

    fun getChangelogs(): CopyOnWriteArrayList<Changelog> = changelogs
}

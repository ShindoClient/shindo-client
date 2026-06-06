package com.shindoclient.shindo.management.remote.discord

import com.google.gson.JsonObject
import com.shindoclient.shindo.Shindo
import com.shindoclient.shindo.utils.JsonUtils
import com.shindoclient.shindo.utils.concurrent.TaskExecutor
import com.shindoclient.shindo.utils.concurrent.ThreadPoolType
import com.shindoclient.shindo.utils.network.HttpUtils

class DiscordStats {
    var membersCount: Int = -1
        private set
    var membersOnline: Int = -1
        private set

    fun check() {
        TaskExecutor.runAsync(ThreadPoolType.NETWORK) { checkDiscordValues() }
    }

    fun checkDiscordValues() {
        val discordStats = Shindo.getInstance().getDiscordStats()
        val jsonObject: JsonObject? = HttpUtils.readJson("https://discord.com/api/v9/invites/uU56tvtXMU?with_counts=true", null)

        if (jsonObject != null) {
            discordStats.membersCount = JsonUtils.getIntProperty(jsonObject, "approximate_member_count", -1)
            discordStats.membersOnline = JsonUtils.getIntProperty(jsonObject, "approximate_presence_count", -1)
        }
    }
}

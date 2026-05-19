package me.miki.shindo.management.remote.discord

import com.google.gson.JsonObject
import me.miki.shindo.Shindo
import me.miki.shindo.utils.JsonUtils
import me.miki.shindo.utils.concurrent.TaskExecutor
import me.miki.shindo.utils.concurrent.ThreadPoolType
import me.miki.shindo.utils.network.HttpUtils

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

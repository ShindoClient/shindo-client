package me.miki.shindo.management.remote.blacklists

import com.google.gson.Gson
import com.google.gson.JsonObject
import me.miki.shindo.utils.JsonUtils
import me.miki.shindo.utils.concurrent.TaskExecutor
import me.miki.shindo.utils.concurrent.ThreadPoolType
import me.miki.shindo.utils.network.HttpUtils
import java.util.concurrent.CopyOnWriteArrayList

class BlacklistManager {
    private val blacklist = CopyOnWriteArrayList<Server>()

    init {
        check()
    }

    fun check() {
        TaskExecutor.runAsync(ThreadPoolType.NETWORK) { loadBlacklists() }
    }

    private fun loadBlacklists() {
        val jsonObject = HttpUtils.readJson("https://cdn.shindoclient.com/data/servers/blacklist.json", null) ?: return
        val jsonArray = JsonUtils.getArrayProperty(jsonObject, "blacklist")
        val gson = Gson()
        for (jsonElement in jsonArray) {
            val serverJsonObject = gson.fromJson(jsonElement, JsonObject::class.java)
            val serverIp = JsonUtils.getStringProperty(serverJsonObject, "serverip", "null")
            val modsArray = JsonUtils.getArrayProperty(serverJsonObject, "mods")
            val modsList = CopyOnWriteArrayList<String>()
            modsArray.forEach { modElement -> modsList.add(modElement.asString) }
            blacklist.add(Server(serverIp!!, modsList))
        }
    }

    fun getBlacklist(): CopyOnWriteArrayList<Server> = blacklist
}

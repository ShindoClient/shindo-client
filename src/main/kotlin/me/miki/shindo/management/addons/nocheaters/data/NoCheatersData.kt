package me.miki.shindo.management.addons.nocheaters.data

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import me.miki.shindo.logger.ShindoLogger
import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventTick
import java.io.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class NoCheatersData(private val configFile: File) {

    private val uuidMap: MutableMap<UUID, WDR> = ConcurrentHashMap()
    private val nickMap: MutableMap<String, WDR> = ConcurrentHashMap()
    private var dirty = false
    private var lastSaveTime = 0L
    private val autoSaveInterval = 300_000L

    init {
        loadReportedPlayers()
        Runtime.getRuntime().addShutdownHook(Thread { saveReportedPlayers() })
    }

    @EventTarget
    fun onTick(event: EventTick) {

        val currentTime = System.currentTimeMillis()
        if (dirty && currentTime - lastSaveTime > autoSaveInterval) {
            saveReportedPlayers()
        }
    }

    fun markDirty() {
        dirty = true
    }

    fun getAllWDRs(): Map<Any, WDR> {
        val mergedMap = HashMap<Any, WDR>(uuidMap.size + nickMap.size)
        mergedMap.putAll(uuidMap)
        mergedMap.putAll(nickMap)
        return mergedMap
    }

    fun getWDR(uuid: UUID?, playername: String?): WDR? {
        if (uuid != null && isRealPlayer(uuid)) {
            return uuidMap[uuid]
        }
        return playername?.let { nickMap[it] }
    }

    fun put(uuid: UUID?, playername: String?, wdr: WDR) {
        if (uuid != null && isRealPlayer(uuid)) {
            uuidMap[uuid] = wdr
        } else if (playername != null) {
            nickMap[playername] = wdr
        }
        markDirty()
    }

    fun remove(uuid: UUID?, playername: String?): WDR? {
        val removed = if (uuid != null) {
            uuidMap.remove(uuid)
        } else if (playername != null) {
            nickMap.remove(playername)
        } else {
            null
        }
        if (removed != null) {
            markDirty()
        }
        return removed
    }

    fun saveReportedPlayers() {
        try {
            val reportLines = ArrayList<String>(uuidMap.size + nickMap.size)

            for ((uuid, wdr) in uuidMap) {
                reportLines.add("$uuid ${wdr.getTimestamp()}${wdr.cheatsToString()}")
            }

            for ((playername, wdr) in nickMap) {
                reportLines.add("$playername ${wdr.getTimestamp()}${wdr.cheatsToString()}")
            }

            configFile.parentFile?.mkdirs()

            BufferedWriter(FileWriter(configFile)).use { writer ->
                val gson = GsonBuilder()
                    .setPrettyPrinting()
                    .create()
                val jsonString = gson.toJson(reportLines)
                writer.write(jsonString)
            }

            dirty = false
            lastSaveTime = System.currentTimeMillis()
            ShindoLogger.info("[NoCheaters] Saved ${reportLines.size} reported players")
        } catch (e: IOException) {
            ShindoLogger.error("[NoCheaters] Failed to save reported players", e)
        }
    }

    private fun loadReportedPlayers() {
        if (!configFile.exists()) {
            ShindoLogger.info("[NoCheaters] No existing data file found, starting fresh")
            return
        }

        try {
            val reportLines = loadReportsFromJSONFile()
            reportLines.forEach { loadReportLine(it) }
            ShindoLogger.info("[NoCheaters] Loaded ${uuidMap.size + nickMap.size} reported players")
        } catch (e: Exception) {
            ShindoLogger.error("[NoCheaters] Failed to load reported players", e)
        }
    }

    private fun loadReportsFromJSONFile(): List<String> {
        val reportLines = ArrayList<String>()
        if (!configFile.exists()) return reportLines

        try {
            BufferedReader(FileReader(configFile)).use { reader ->
                val gson = Gson()
                val type = object : TypeToken<List<String>>() {}.type
                val list: List<String>? = gson.fromJson(reader, type)
                list?.let { reportLines.addAll(it) }
            }
        } catch (e: Exception) {
            ShindoLogger.error("[NoCheaters] Failed to parse JSON file", e)
        }

        return reportLines
    }

    private fun loadReportLine(reportLine: String) {

        val split = reportLine.split(" ")
        if (split.size < 3) return

        val mapKey = split[0]
        if (mapKey.isEmpty()) return

        val timestamp = try {
            split[1].toLong()
        } catch (e: NumberFormatException) {
            return
        }

        val oldDataFormat = try {
            split[2].toLong()
            false
        } catch (e: NumberFormatException) {
            true
        }

        val startIndex = if (oldDataFormat) 2 else 3
        val hacks = split.subList(startIndex, split.size).toMutableList()

        hacks.remove("ignored")
        if (hacks.isEmpty()) return

        val now = System.currentTimeMillis()
        val maxAge = 90L * 24 * 3600 * 1000
        if (now > timestamp + maxAge) {
            return
        }

        val uuid = try {
            UUID.fromString(mapKey)
        } catch (e: IllegalArgumentException) {
            null
        }

        if (uuid != null) {
            uuidMap[uuid] = WDR(hacks, timestamp)
        } else if (mapKey.length < 17) {

            val nickMaxAge = 24L * 3600 * 1000
            if (now <= timestamp + nickMaxAge) {
                nickMap[mapKey] = WDR(hacks, timestamp)
            }
        }
    }

    private fun isRealPlayer(uuid: UUID): Boolean {


        return uuid.version() == 4
    }
}

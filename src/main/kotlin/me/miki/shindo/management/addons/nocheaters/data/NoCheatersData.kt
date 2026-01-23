package me.miki.shindo.management.addons.nocheaters.data

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import me.miki.shindo.logger.ShindoLogger
import net.minecraft.client.Minecraft
import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventTick
import java.io.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Gerencia o armazenamento de dados do NoCheaters
 * 
 * Funcionalidades:
 * - Salva/carrega jogadores reportados em JSON
 * - Suporta UUID e nicknames
 * - Auto-save periódico
 * 
 * Extensível para:
 * - Sincronização com servidor/API
 * - Backup automático
 * - Compressão de dados
 * - Migração de formatos
 */
class NoCheatersData(private val configFile: File) {

    private val uuidMap: MutableMap<UUID, WDR> = ConcurrentHashMap()
    private val nickMap: MutableMap<String, WDR> = ConcurrentHashMap()
    private var dirty = false
    private var lastSaveTime = 0L
    private val autoSaveInterval = 300_000L // 5 minutos

    init {
        loadReportedPlayers()
        Runtime.getRuntime().addShutdownHook(Thread { saveReportedPlayers() })
    }

    @EventTarget
    fun onTick(event: EventTick) {
        
        // Auto-save periódico
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
            
            // Salva por UUID
            for ((uuid, wdr) in uuidMap) {
                reportLines.add("$uuid ${wdr.getTimestamp()}${wdr.cheatsToString()}")
            }
            
            // Salva por nickname
            for ((playername, wdr) in nickMap) {
                reportLines.add("$playername ${wdr.getTimestamp()}${wdr.cheatsToString()}")
            }

            // Cria diretório se não existir
            configFile.parentFile?.mkdirs()

            // Salva em JSON
            BufferedWriter(FileWriter(configFile)).use { writer ->
                val gson = GsonBuilder()
                    .setPrettyPrinting()
                    .create()
                val jsonString = gson.toJson(reportLines)
                writer.write(jsonString)
            }

            dirty = false
            lastSaveTime = System.currentTimeMillis()
            me.miki.shindo.logger.ShindoLogger.info("[NoCheaters] Saved ${reportLines.size} reported players")
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
        // Formato: uuid/nickname timestamp cheat1 cheat2 cheat3
        val split = reportLine.split(" ")
        if (split.size < 3) return

        val mapKey = split[0]
        if (mapKey.isEmpty()) return

        val timestamp = try {
            split[1].toLong()
        } catch (e: NumberFormatException) {
            return
        }

        // Verifica se é formato antigo (com timestamp duplicado)
        val oldDataFormat = try {
            split[2].toLong()
            false
        } catch (e: NumberFormatException) {
            true
        }

        val startIndex = if (oldDataFormat) 2 else 3
        val hacks = split.subList(startIndex, split.size).toMutableList()

        // Remove "ignored" (compatibilidade com versões antigas)
        hacks.remove("ignored")
        if (hacks.isEmpty()) return

        // Verifica se deve deletar reportes antigos (opcional, pode ser configurável)
        val now = System.currentTimeMillis()
        val maxAge = 90L * 24 * 3600 * 1000 // 90 dias
        if (now > timestamp + maxAge) {
            return // Ignora reportes muito antigos
        }

        // Tenta parsear como UUID
        val uuid = try {
            UUID.fromString(mapKey)
        } catch (e: IllegalArgumentException) {
            null
        }

        if (uuid != null) {
            uuidMap[uuid] = WDR(hacks, timestamp)
        } else if (mapKey.length < 17) {
            // Nickname (limita a 24 horas para nicks)
            val nickMaxAge = 24L * 3600 * 1000
            if (now <= timestamp + nickMaxAge) {
                nickMap[mapKey] = WDR(hacks, timestamp)
            }
        }
    }

    private fun isRealPlayer(uuid: UUID): Boolean {
        // Verifica se é um UUID válido (não offline mode)
        // Pode ser expandido para verificar contra API
        return uuid.version() == 4
    }
}

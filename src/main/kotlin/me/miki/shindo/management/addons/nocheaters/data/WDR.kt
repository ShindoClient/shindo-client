package me.miki.shindo.management.addons.nocheaters.data

import java.util.*

/**
 * WDR (WatchDog Report) - Representa um reporte de cheats de um jogador
 * 
 * Armazena:
 * - Lista de cheats reportados
 * - Timestamp do reporte
 * - Ícones de severidade (vermelho/amarelo)
 * 
 * Extensível para:
 * - Adicionar mais metadados (servidor, contexto, etc.)
 * - Sistema de confiança/verificação
 * - Histórico de reportes
 */
class WDR(
    cheats: List<String>,
    timestamp: Long = System.currentTimeMillis()
) : Comparable<WDR> {

    private val cheatsList: MutableList<String> = ArrayList(cheats)
    private var redIcon: Boolean = false
    private var yellowIcon: Boolean = false
    private var timestamp: Long = timestamp

    init {
        updateIcon()
    }

    constructor(cheat: String) : this(listOf(cheat))

    /**
     * Adiciona um cheat à lista
     */
    fun addCheat(cheat: String): Boolean {
        if (!cheatsList.contains(cheat)) {
            if (cheat.endsWith("[H]")) {
                cheatsList.remove("cheating")
            }
            cheatsList.add(cheat)
            updateIcon()
            timestamp = System.currentTimeMillis()
            return true
        }
        timestamp = System.currentTimeMillis()
        return false
    }

    /**
     * Adiciona múltiplos cheats
     */
    fun addCheats(cheats: List<String>) {
        if (cheatsList.isEmpty() || cheats.size != 1 || cheats[0] != "cheating") {
            cheatsList.removeAll(cheats)
            cheatsList.addAll(cheats)
        }
        updateIcon()
        timestamp = System.currentTimeMillis()
    }

    /**
     * Atualiza os ícones baseado na severidade dos cheats
     */
    private fun updateIcon() {
        if (redIcon) return

        // Verifica cheats vermelhos (mais graves)
        for (cheat in cheatsList) {
            if (isRedCheat(cheat)) {
                redIcon = true
                yellowIcon = false
                return
            }
        }

        // Verifica cheats amarelos (menos graves)
        for (cheat in cheatsList) {
            if (!isNoIconCheat(cheat)) {
                redIcon = false
                yellowIcon = true
                return
            }
        }

        redIcon = false
        yellowIcon = false
    }

    fun hasRedIcon(): Boolean = redIcon
    fun hasYellowIcon(): Boolean = yellowIcon
    fun getTimestamp(): Long = timestamp
    fun getCheats(): List<String> = cheatsList.toList()

    /**
     * Verifica se tem cheats válidos
     */
    fun hasValidCheats(): Boolean {
        val validCheats = getValidCheatsList()
        return cheatsList.any { validCheats.contains(it) }
    }

    /**
     * Converte cheats para string
     */
    fun cheatsToString(): String {
        return cheatsList.joinToString(" ") { " $it" }
    }

    /**
     * Obtém cheats formatados para exibição (extensível)
     */
    fun getFormattedCheats(): net.minecraft.util.IChatComponent {
        val imsg = net.minecraft.util.ChatComponentText("")
        for (cheat in cheatsList) {
            val color = if (isRedCheat(cheat)) {
                net.minecraft.util.EnumChatFormatting.DARK_RED
            } else {
                net.minecraft.util.EnumChatFormatting.GOLD
            }
            imsg.appendText(" $color$cheat")
        }
        return imsg
    }

    private fun isRedCheat(cheat: String): Boolean {
        val lowerCheat = cheat.lowercase()
        val redCheats = listOf("killaura", "ka", "aimbot", "reach", "fly", "scaffold")
        return redCheats.any { lowerCheat.startsWith(it) }
    }

    override fun compareTo(other: WDR): Int {
        return timestamp.compareTo(other.timestamp)
    }

    companion object {
        /**
         * Lista de cheats válidos (pode ser expandida)
         */
        private fun getValidCheatsList(): List<String> {
            return listOf(
                "killaura", "ka", "aimbot", "reach", "autoclicker", "ac",
                "fly", "speed", "nofall", "scaffold", "blink", "noslow",
                "antikb", "velocity", "bhop", "timer", "cheating"
            )
        }

        /**
         * Cheats que devem mostrar ícone vermelho (mais graves)
         */
        private fun isRedCheat(cheat: String): Boolean {
            val lowerCheat = cheat.lowercase()
            val redCheats = listOf("killaura", "ka", "aimbot", "reach", "fly", "scaffold")
            return redCheats.any { lowerCheat.startsWith(it) }
        }

        /**
         * Cheats que não devem mostrar ícone
         */
        private fun isNoIconCheat(cheat: String): Boolean {
            val lowerCheat = cheat.lowercase()
            val noIconCheats = listOf("ignored", "unverified")
            return noIconCheats.any { lowerCheat.startsWith(it) }
        }
    }
}

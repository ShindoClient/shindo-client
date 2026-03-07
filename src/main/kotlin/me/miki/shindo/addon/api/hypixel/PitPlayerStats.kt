package me.miki.shindo.addon.api.hypixel

/**
 * Modelo de domínio simplificado para estatísticas do The Pit.
 *
 * Este modelo é estável para addons; mudanças internas de API/JSON devem ser
 * tratadas apenas dentro do Shindo Client.
 */
data class PitPlayerStats(
    val level: Int,
    val prestige: Int,
    val currentXp: Long,
    val xpForNextLevel: Long,
    val gold: Long,
    val renown: Long
)


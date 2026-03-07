package me.miki.shindo.api.scoreboard

import me.miki.shindo.addon.api.scoreboard.ScoreboardService
import net.minecraft.client.Minecraft
import net.minecraft.scoreboard.ScoreObjective

/**
 * Implementação de [ScoreboardService] que lê o scoreboard atual do Minecraft.
 */
class ScoreboardServiceImpl(
    private val mc: Minecraft = Minecraft.getMinecraft()
) : ScoreboardService {

    override fun getCurrentLines(): List<String> {
        val world = mc.theWorld ?: return emptyList()
        val scoreboard = world.scoreboard ?: return emptyList()
        val objective: ScoreObjective = mc.thePlayer?.worldScoreboard?.getObjectiveInDisplaySlot(1)
            ?: return emptyList()

        val scores = scoreboard.getSortedScores(objective)

        return scores
            .filter { it != null && it.playerName != null }
            .map { it.playerName }
    }
}


package me.miki.shindo.management.event.impl

import me.miki.shindo.management.event.Event
import net.minecraft.scoreboard.ScoreObjective

class EventRenderScoreboard(
    private val _objective: ScoreObjective,
) : Event() {
    fun getObjective(): ScoreObjective = _objective
}

package com.shindoclient.shindo.management.event.impl

import com.shindoclient.shindo.management.event.Event
import net.minecraft.scoreboard.ScoreObjective

class EventRenderScoreboard(
    private val objective: ScoreObjective,
) : Event() {
    fun getObjective(): ScoreObjective = objective
}

package me.miki.shindo.management.event.impl;

import me.miki.shindo.management.event.Event;
import net.minecraft.scoreboard.ScoreObjective;

public class EventRenderScoreboard extends Event {

    private final ScoreObjective objective;

    public EventRenderScoreboard(ScoreObjective objective) {
        this.objective = objective;
    }

    public ScoreObjective getObjective() {
        return objective;
    }
}
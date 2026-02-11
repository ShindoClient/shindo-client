package me.miki.shindo.libs.spotify.enums;

import java.util.HashMap;
import java.util.Map;

public enum Action {

    INTERRUPTING_PLAYBACK("interrupting_playback"),
    PAUSING("pausing"),
    RESUMING("resuming"),
    SEEKING("seeking"),
    SKIPPING_NEXT("skipping_next"),
    SKIPPING_PREV("skipping_prev"),
    TOGGLING_REPEAT_CONTEXT("toggling_repeat_context"),
    TOGGLING_SHUFFLE("toggling_shuffle"),
    TOGGLING_REPEAT_TRACK("toggling_repeat_track"),
    TRANSFERRING_PLAYBACK("transferring_playback");

    private static final Map<String, Action> map = new HashMap<>();

    static {
        for (Action action : Action.values()) {
            map.put(action.key, action);
        }
    }

    public final String key;

    Action(final String key) {
        this.key = key;
    }

    public static Action keyOf(String key) {
        return map.get(key);
    }

    public String getKey() {
        return key;
    }
}

package eu.shoroa.contrib.util;

import lombok.Getter;

public strictfp class Time {
    @Getter
    private static float delta;

    public static void setDelta(float delta) {
        Time.delta = delta;
    }
}

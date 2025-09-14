package me.miki.shindo.utils.helper;

import lombok.Getter;

public class ResolutionHelper {

    @Getter
    private static int currentScaleOverride = -1;
    @Getter
    private static int scaleOverride = -1;

    public static void setCurrentScaleOverride(int currentScaleOverride) {
        ResolutionHelper.currentScaleOverride = currentScaleOverride;
    }

    public static void setScaleOverride(int scaleOverride) {
        ResolutionHelper.scaleOverride = scaleOverride;
    }
}

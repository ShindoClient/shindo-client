package me.miki.shindo.utils;

import lombok.Getter;

public class OSUtils {

    @Getter
    private static final boolean windows = System.getProperty("os.name").toLowerCase().contains("windows");

    @Getter
    private static final boolean linux = System.getProperty("os.name").toLowerCase().contains("linux");

    @Getter
    private static final boolean mac = System.getProperty("os.name").toLowerCase().contains("mac");

    @Getter
    private static final boolean unix = linux || mac;

    public static String getPlatform() {
        if (windows) return "Windows";
        if (linux) return "Linux";
        if (mac) return "Mac";
        return "Unknown";
    }

}

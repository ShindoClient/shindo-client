package me.miki.shindo.utils;

import net.minecraft.util.Util;
import org.lwjgl.Sys;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;

/**
 * Utility to open system browsers in a cross-platform way.
 */
public final class BrowserUtils {

    private BrowserUtils() {
    }

    public static boolean openUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }

        URI uri;
        try {
            uri = URI.create(url.trim());
        } catch (IllegalArgumentException ex) {
            return false;
        }

        if (tryDesktopBrowse(uri)) {
            return true;
        }

        if (tryNativeOpen(uri.toString())) {
            return true;
        }

        return tryLwjglFallback(uri.toString());
    }

    private static boolean tryDesktopBrowse(URI uri) {
        if (!Desktop.isDesktopSupported()) {
            return false;
        }

        try {
            Desktop desktop = Desktop.getDesktop();
            if (!desktop.isSupported(Desktop.Action.BROWSE)) {
                return false;
            }
            desktop.browse(uri);
            return true;
        } catch (IOException | SecurityException ignored) {
            return false;
        }
    }

    private static boolean tryNativeOpen(String url) {
        try {
            Util.EnumOS os = Util.getOSType();
            if (os == Util.EnumOS.OSX) {
                Runtime.getRuntime().exec(new String[]{"/usr/bin/open", url});
                return true;
            }

            if (os == Util.EnumOS.WINDOWS) {
                Runtime.getRuntime().exec(new String[]{"cmd.exe", "/C", "start", "\"\"", url});
                return true;
            }
        } catch (IOException ignored) {
        }

        return false;
    }

    private static boolean tryLwjglFallback(String url) {
        try {
            Sys.openURL(url);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }
}

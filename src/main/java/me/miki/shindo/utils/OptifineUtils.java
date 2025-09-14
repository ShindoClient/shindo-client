package me.miki.shindo.utils;

import me.miki.shindo.injection.mixin.ShindoTweaker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;

import java.lang.reflect.Field;

public class OptifineUtils {

    private static final Minecraft mc = Minecraft.getMinecraft();
    private static Field gameSettings_ofFastRender;

    static {
        try {
            Class.forName("Config");

            gameSettings_ofFastRender = GameSettings.class.getDeclaredField("ofFastRender");
            gameSettings_ofFastRender.setAccessible(true);
        } catch (ClassNotFoundException | NoSuchFieldException ignore) {
        }
    }

    public static void disableFastRender() {

        if (ShindoTweaker.hasOptifine) {
            try {
                OptifineUtils.gameSettings_ofFastRender.set(mc.gameSettings, false);
            } catch (IllegalArgumentException | IllegalAccessException ignored) {
            }
        }

        mc.gameSettings.useVbo = true;
        mc.gameSettings.fboEnable = true;
    }
}

package com.shindoclient.shindo.utils

import com.shindoclient.shindo.injection.mixin.ShindoTweaker
import net.minecraft.client.Minecraft
import net.minecraft.client.settings.GameSettings
import java.lang.reflect.Field

object OptifineUtils {
    private val mc: Minecraft = Minecraft.getMinecraft()

    private val gameSettingsOfFastRender: Field? =
        run {
            try {
                Class.forName("Config")
                GameSettings::class.java.getDeclaredField("ofFastRender").apply { isAccessible = true }
            } catch (_: ClassNotFoundException) {
                null
            } catch (_: NoSuchFieldException) {
                null
            }
        }

    @JvmStatic
    fun disableFastRender() {
        if (ShindoTweaker.hasOptifine) {
            try {
                gameSettingsOfFastRender?.set(mc.gameSettings, false)
            } catch (_: IllegalArgumentException) {
            } catch (_: IllegalAccessException) {
            }
        }

        mc.gameSettings.useVbo = true
        mc.gameSettings.fboEnable = true
    }
}

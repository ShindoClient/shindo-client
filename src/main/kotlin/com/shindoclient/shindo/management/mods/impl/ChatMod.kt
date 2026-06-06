package com.shindoclient.shindo.management.mods.impl

import com.shindoclient.shindo.management.event.EventTarget
import com.shindoclient.shindo.management.event.impl.EventReceiveChat
import com.shindoclient.shindo.management.language.TranslateText
import com.shindoclient.shindo.management.mods.Mod
import com.shindoclient.shindo.management.mods.ModCategory
import com.shindoclient.shindo.management.nanovg.font.Shinconic
import com.shindoclient.shindo.management.settings.config.Property
import com.shindoclient.shindo.management.settings.config.PropertyType
import com.shindoclient.shindo.management.settings.impl.BooleanSetting
import com.shindoclient.shindo.management.settings.impl.NumberSetting
import com.shindoclient.shindo.management.settings.metadata.SettingRegistry.getBooleanSetting
import com.shindoclient.shindo.management.settings.metadata.SettingRegistry.getNumberSetting
import com.shindoclient.shindo.management.sound.Sound.Companion.play
import com.shindoclient.shindo.management.sound.Sounds
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.EntityPlayer
import java.util.Locale

class ChatMod : Mod(TranslateText.CHAT, TranslateText.CHAT_DESCRIPTION, ModCategory.OTHER, Shinconic.MOD_CHAT, "betterchatting") {
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.SMOOTH, category = "Animation")
    @JvmField
    var smoothSetting = false

    @Property(
        type = PropertyType.NUMBER,
        translate = TranslateText.SMOOTH_SPEED,
        category = "Animation",
        min = 1.0,
        max = 10.0,
        step = 1.0,
        current = 4.0,
    )
    @JvmField
    var smoothSpeedSetting = 4.0

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.HEAD, category = "Display")
    @JvmField
    var headSetting = false

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.INFINITY, category = "Display")
    @JvmField
    var infinitySetting = false

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.BACKGROUND, category = "Display")
    @JvmField
    var backgroundSetting = false

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.COMPACT, category = "Display")
    @JvmField
    var compactSetting = false

    @Property(type = PropertyType.BOOLEAN, name = "Highlight Mentions", category = "Display", current = 1.0)
    @JvmField
    var highlightMentionsSetting = true

    @Property(type = PropertyType.BOOLEAN, name = "Mention Ping", category = "Alerts")
    @JvmField
    var mentionPingSetting = false

    init {
        instance = this
    }

    @EventTarget
    fun onChatMessage(event: EventReceiveChat) {
        if (!isToggled() || getMentionPingSetting()?.isToggled() != true) {
            return
        }

        val mc = Minecraft.getMinecraft()
        val player: EntityPlayer = mc.thePlayer

        val component = event.getMessage()
        val name = player.name.lowercase(Locale.getDefault())
        var text = component.unformattedText.lowercase(Locale.getDefault())
        text = text.replaceFirst("<.+>".toRegex(), "")
        if (text.contains(name)) {
            play(Sounds.SHINDO_AUDIO_PLING, false)
        }
    }

    companion object {
        @JvmField
        var instance: ChatMod? = null
    }

    fun getSmoothSetting(): BooleanSetting? = getBooleanSetting(this, "smoothSetting")

    fun getSmoothSpeedSetting(): NumberSetting? = getNumberSetting(this, "smoothSpeedSetting")

    fun getHeadSetting(): BooleanSetting? = getBooleanSetting(this, "headSetting")

    fun getInfinitySetting(): BooleanSetting? = getBooleanSetting(this, "infinitySetting")

    fun getBackgroundSetting(): BooleanSetting? = getBooleanSetting(this, "backgroundSetting")

    fun getCompactSetting(): BooleanSetting? = getBooleanSetting(this, "compactSetting")

    fun getHighlightMentionsSetting(): BooleanSetting? = getBooleanSetting(this, "highlightMentionsSetting")

    fun getMentionPingSetting(): BooleanSetting? = getBooleanSetting(this, "mentionPingSetting")
}

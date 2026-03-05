package me.miki.shindo.management.mods.impl

import me.miki.client_api.event.EventTarget
import me.miki.shindo.management.event.impl.EventReceiveChat
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyType
import me.miki.shindo.management.settings.impl.BooleanSetting
import me.miki.shindo.management.settings.impl.NumberSetting
import me.miki.shindo.management.settings.metadata.SettingRegistry.getBooleanSetting
import me.miki.shindo.management.settings.metadata.SettingRegistry.getNumberSetting
import me.miki.shindo.management.sound.Sounds
import me.miki.shindo.management.sound.Sound.Companion.play
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.EntityPlayer
import java.util.*

class ChatMod :
    Mod(TranslateText.CHAT, TranslateText.CHAT_DESCRIPTION, ModCategory.OTHER, LegacyIcon.MOD_CHAT, "betterchatting") {
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
        current = 4.0
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
        val name = player.name.toLowerCase(Locale.getDefault())
        var text = component.unformattedText.toLowerCase(Locale.getDefault())
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

    fun getHighlightMentionsSetting(): BooleanSetting? =
        getBooleanSetting(this, "highlightMentionsSetting")

    fun getMentionPingSetting(): BooleanSetting? = getBooleanSetting(this, "mentionPingSetting")
}









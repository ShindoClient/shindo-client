package me.miki.shindo.management.mods.impl

import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventKey
import me.miki.shindo.management.event.impl.EventRender2D
import me.miki.shindo.management.event.impl.EventTick
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.SimpleHUDMod
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyType
import me.miki.shindo.utils.TimerUtils
import org.lwjgl.input.Keyboard
import java.text.DecimalFormat

class StopwatchMod :
    SimpleHUDMod(TranslateText.STOPWATCH, TranslateText.STOPWATCH_DESCRIPTION, LegacyIcon.MOD_STOPWATCH) {
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.ICON)
    private val iconSetting = true

    @Property(type = PropertyType.KEYBIND, translate = TranslateText.KEYBIND, keyCode = Keyboard.KEY_P)
    private val keybind = Keyboard.KEY_P

    private val timer: TimerUtils = TimerUtils()
    private val timeFormat = DecimalFormat("0.00")
    private var pressCount = 0
    private var currentTime = 0f

    @EventTarget
    fun onRender2D(event: EventRender2D?) {
        this.draw()
    }

    @EventTarget
    fun onTick(event: EventTick?) {
        when (pressCount) {
            0 -> timer!!.reset()
            1 -> currentTime = (timer!!.elapsedTime / 1000f)
            3 -> {
                timer!!.reset()
                currentTime = 0f
                pressCount = 0
            }
        }
    }

    @EventTarget
    fun onKey(event: EventKey) {
        if (event.keyCode == keybind) {
            pressCount++
        }
    }

    override fun getText(): String {
        return timeFormat.format(currentTime.toDouble()) + " s"
    }

    override fun getIcon(): String? {
        return if (iconSetting) LegacyIcon.WATCH else null
    }

    override fun onEnable() {
        super.onEnable()

        if (timer != null) {
            timer.reset()
        }

        pressCount = 0
        currentTime = 0f
    }
}



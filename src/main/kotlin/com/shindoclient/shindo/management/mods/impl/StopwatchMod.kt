package com.shindoclient.shindo.management.mods.impl

import com.shindoclient.shindo.management.event.EventTarget
import com.shindoclient.shindo.management.event.impl.EventKey
import com.shindoclient.shindo.management.event.impl.EventRender2D
import com.shindoclient.shindo.management.event.impl.EventTick
import com.shindoclient.shindo.management.language.TranslateText
import com.shindoclient.shindo.management.mods.SimpleHUDMod
import com.shindoclient.shindo.management.nanovg.font.Lucide
import com.shindoclient.shindo.management.nanovg.font.Shinconic
import com.shindoclient.shindo.management.settings.config.Property
import com.shindoclient.shindo.management.settings.config.PropertyType
import com.shindoclient.shindo.utils.TimerUtils
import org.lwjgl.input.Keyboard
import java.text.DecimalFormat

class StopwatchMod : SimpleHUDMod(TranslateText.STOPWATCH, TranslateText.STOPWATCH_DESCRIPTION, Shinconic.MOD_STOPWATCH) {
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
            0 -> {
                timer.reset()
            }

            1 -> {
                currentTime = (timer.elapsedTime / 1000f)
            }

            3 -> {
                timer.reset()
                currentTime = 0f
                pressCount = 0
            }
        }
    }

    @EventTarget
    fun onKey(event: EventKey) {
        if (event.getKeyCode() == keybind) {
            pressCount++
        }
    }

    override fun getText(): String = timeFormat.format(currentTime.toDouble()) + " s"

    override fun getIcon(): String? = if (iconSetting) Lucide.WATCH else null

    override fun onEnable() {
        super.onEnable()

        timer.reset()

        pressCount = 0
        currentTime = 0f
    }
}

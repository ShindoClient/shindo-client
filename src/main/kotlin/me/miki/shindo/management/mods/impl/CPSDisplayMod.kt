package me.miki.shindo.management.mods.impl

import me.miki.client_api.event.EventTarget
import me.miki.shindo.management.event.impl.EventClickMouse
import me.miki.shindo.management.event.impl.EventRender2D
import me.miki.shindo.management.event.impl.EventTick
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.SimpleHUDMod
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyType
import org.lwjgl.input.Mouse

class CPSDisplayMod :
    SimpleHUDMod(TranslateText.CPS_DISPLAY, TranslateText.CPS_DISPLAY_DESCRIPTION, LegacyIcon.MOD_CPS_DISPLAY) {
    private val leftPresses = ArrayList<Long?>()
    private val rightPresses = ArrayList<Long?>()

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.RIGHT_CLICK)
    private val rightClickSetting = true

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.ICON)
    private val iconSetting = true

    @EventTarget
    fun onRender2D(event: EventRender2D?) {
        this.draw()
    }

    @EventTarget
    fun onClickMouse(event: EventClickMouse) {
        if (Mouse.getEventButtonState()) {
            if (event.getButton() == 0) {
                leftPresses.add(System.currentTimeMillis())
            }

            if (event.getButton() == 1) {
                rightPresses.add(System.currentTimeMillis())
            }
        }
    }

    @EventTarget
    fun onTick(event: EventTick?) {
        leftPresses.removeIf { t: Long? -> System.currentTimeMillis() - t!! > 1000 }
        rightPresses.removeIf { t: Long? -> System.currentTimeMillis() - t!! > 1000 }
    }

    override fun getText(): String {
        return (if (rightClickSetting) leftPresses.size.toString() + " | " + rightPresses.size else leftPresses.size).toString() + " CPS"
    }

    override fun getIcon(): String? {
        return if (iconSetting) LegacyIcon.MOUSE_POINTER else null
    }
}



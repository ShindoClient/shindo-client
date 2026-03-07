package me.miki.shindo.management.mods.impl

import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventNVG
import me.miki.shindo.management.event.impl.EventPlayerHeadRotation
import me.miki.shindo.management.event.impl.EventTick
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.HUDMod
import me.miki.shindo.management.nanovg.font.LegacyIcon
import net.minecraft.util.MathHelper

class MouseStrokesMod :
    HUDMod(TranslateText.MOUSE_STROKES, TranslateText.MOUSE_STROKES_DESCRIPTION, LegacyIcon.MOD_MOUSE_STROKES) {
    private var mouseX = 0f
    private var mouseY = 0f
    private var lastMouseX = 0f
    private var lastMouseY = 0f

    @EventTarget
    fun onRender2D(event: EventNVG) {
        val calculatedMouseX = (lastMouseX + ((mouseX - lastMouseX) * event.partialTicks))
        val calculatedMouseY = (lastMouseY + ((mouseY - lastMouseY) * event.partialTicks))

        this.drawBackground(58f, 58f)
        this.drawRoundedRect(calculatedMouseX + 28 - 3.5f, calculatedMouseY + 28 - 3.5f, 9f, 9f, (9 / 2).toFloat())

        this.setWidth(58)
        this.setHeight(58)
    }

    @EventTarget
    fun onPlayerHeadRotation(event: EventPlayerHeadRotation) {
        mouseX += event.getYaw() / 40f
        mouseY -= event.getPitch() / 40f
        mouseX = MathHelper.clamp_float(mouseX, -18f, 18f)
        mouseY = MathHelper.clamp_float(mouseY, -18f, 18f)
    }

    @EventTarget
    fun onTick(event: EventTick?) {
        lastMouseX = mouseX
        lastMouseY = mouseY
        mouseX *= 0.75f
        mouseY *= 0.75f
    }
}





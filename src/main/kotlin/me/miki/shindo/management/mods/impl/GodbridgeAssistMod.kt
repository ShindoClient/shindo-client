package me.miki.shindo.management.mods.impl

import me.miki.shindo.Shindo.Companion.getInstance
import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventRender2D
import me.miki.shindo.management.event.impl.EventTick
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.HUDMod
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.LegacyIcon
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.item.ItemBlock
import kotlin.math.abs
import kotlin.math.floor

class GodbridgeAssistMod : HUDMod(
    TranslateText.GODBRIDGE_ASSIST,
    TranslateText.GODBRIDGE_ASSIST_DESCRIPTION,
    LegacyIcon.MOD_GODBRIDGE_ASSIST
) {
    private var shiftedTicks = 0

    @EventTarget
    fun onTick(event: EventTick?) {
        if (mc.inGameHasFocus) {
            if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                shiftedTicks++
            } else {
                shiftedTicks = 0
            }
        }
    }

    @EventTarget
    fun onRender2D(event: EventRender2D?) {
        val nvg = getInstance().nanoVGManager

        nvg!!.setupAndDraw(Runnable { drawNanoVG(nvg) })
    }

    private fun drawNanoVG(nvg: NanoVGManager) {
        val sr = ScaledResolution(mc)

        if (mc.inGameHasFocus) {
            if (!mc.thePlayer.isCollidedVertically) {
                return
            }

            if (!mc.gameSettings.keyBindSneak.isKeyDown()) {
                return
            }

            if (mc.thePlayer.inventory.getStackInSlot(mc.thePlayer.inventory.currentItem) == null) {
                return
            }

            if (mc.thePlayer.inventory.getStackInSlot(mc.thePlayer.inventory.currentItem).getItem() !is ItemBlock) {
                return
            }

            if (this.shiftedTicks < 5) {
                return
            }

            if (abs(abs(mc.thePlayer.rotationYaw % 90.0f) - 45.0f) >= 5.0f) {
                return
            }

            if (mc.thePlayer.rotationPitch <= 70.0f || mc.thePlayer.rotationPitch >= 80.0f) {
                return
            }

            if (Math.round(abs(mc.thePlayer.posX - floor(mc.thePlayer.posX)) * 10.0) != 3L && Math.round(
                    abs(
                        mc.thePlayer.posX - floor(
                            mc.thePlayer.posX
                        )
                    ) * 10.0
                ) != 7L && Math.round(
                    abs(mc.thePlayer.posZ - floor(mc.thePlayer.posZ)) * 10.0
                ) != 3L && Math.round(abs(mc.thePlayer.posZ - floor(mc.thePlayer.posZ)) * 10.0) != 7L
            ) {
                return
            }

            val alpha = ((abs(abs(mc.thePlayer.rotationYaw.toDouble() % 90.0) - 45.0) / 5) * 255).toInt()
            val round = ((abs(abs(mc.thePlayer.rotationYaw.toDouble() % 90.0) - 45.0) / 5) * 360).toInt()

            nvg.drawArc(
                (sr.getScaledWidth() / 2).toFloat(),
                (sr.getScaledHeight() / 2).toFloat(),
                12f,
                -90f,
                round.toFloat(),
                1.6f,
                this.getFontColor(alpha)
            )
        }
    }
}





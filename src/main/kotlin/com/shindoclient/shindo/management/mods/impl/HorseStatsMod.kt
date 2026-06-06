package com.shindoclient.shindo.management.mods.impl

import com.shindoclient.shindo.management.event.EventTarget
import com.shindoclient.shindo.management.event.impl.EventNVG
import com.shindoclient.shindo.management.language.TranslateText
import com.shindoclient.shindo.management.mods.HUDMod
import com.shindoclient.shindo.management.nanovg.font.Shinconic
import net.minecraft.entity.SharedMonsterAttributes
import net.minecraft.entity.passive.EntityHorse
import java.text.DecimalFormat

class HorseStatsMod : HUDMod(TranslateText.HORSE_STATS, TranslateText.HORSE_STATS_DESCRIPTION, Shinconic.MOD_HORSE_STATS) {
    private val df = DecimalFormat("0.0")

    @EventTarget
    fun onRender2D(event: EventNVG?) {
        var speed = "Speed: 0.0 b/s"
        var jump = "Jump: 0.0 Blocks"

        if (mc.objectMouseOver.entityHit is EntityHorse) {
            val horse = mc.objectMouseOver.entityHit as EntityHorse

            if (!mc.thePlayer.isRidingHorse) {
                speed = "Speed: " +
                    this.getHorseSpeedRounded(
                        horse.getEntityAttribute(SharedMonsterAttributes.movementSpeed).attributeValue,
                    ) + " b/s"
                jump = "Jump: " + df.format(horse.horseJumpStrength * 5.5) + " Blocks"
            }
        }

        this.drawBackground(95f, 28f)

        this.drawText(speed, 5.5f, 5.5f, 9f, getHudFont(1))
        this.drawText(jump, 5.5f, 15.5f, 9f, getHudFont(1))

        this.setWidth(95)
        this.setHeight(28)
    }

    private fun getHorseSpeedRounded(baseSpeed: Double): String {
        val factor = 43.170372f

        val speed = (baseSpeed * factor).toFloat()

        return df.format(speed.toDouble())
    }
}

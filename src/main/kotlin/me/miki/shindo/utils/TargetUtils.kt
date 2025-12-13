package me.miki.shindo.utils

import net.minecraft.client.Minecraft
import net.minecraft.client.entity.AbstractClientPlayer

object TargetUtils {

    private val mc: Minecraft = Minecraft.getMinecraft()
    private val timer = TimerUtils()

    @get:JvmStatic
    @set:JvmStatic
    var target: AbstractClientPlayer? = null

    @JvmStatic
    fun onUpdate() {
        val mouseOver = mc.objectMouseOver
        val hit = mouseOver?.entityHit

        if (hit != null && hit !== target) {
            if (hit is AbstractClientPlayer && ServerUtils.isInTabList(hit)) {
                target = hit
                timer.reset()
            }
        } else if (timer.delay(2500f, false) && mouseOver == null) {
            target = null
            timer.reset()
        }

        target?.let { current ->
            if (current.isDead || mc.thePlayer.isDead) {
                target = null
            } else if (mc.thePlayer != null) {
                if (current.isInvisible || current.getDistanceToEntity(mc.thePlayer) > 12) {
                    target = null
                }
            }
        }
    }
}

package me.miki.shindo.management.mods.impl

import me.miki.shindo.Shindo.Companion.getInstance
import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventRender3D
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyType
import me.miki.shindo.utils.ColorUtils.applyAlpha
import me.miki.shindo.utils.ColorUtils.setColor
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import org.lwjgl.opengl.GL11
import kotlin.math.cos
import kotlin.math.sin

class ReachCirclesMod : Mod(
    TranslateText.REACH_CIRCLES,
    TranslateText.REACH_CIRCLES_DESCRIPTION,
    ModCategory.PLAYER,
    LegacyIcon.MOD_REACH_CIRCLES
) {
    @Property(
        type = PropertyType.NUMBER,
        translate = TranslateText.LINE_WIDTH,
        min = 1.0,
        max = 5.0,
        current = 2.0,
        step = 1.0
    )
    private val lineWidthSetting = 2

    @EventTarget
    fun onRender3D(event: EventRender3D) {
        GL11.glPushMatrix()
        GL11.glDisable(3553)
        GL11.glEnable(3042)
        GL11.glBlendFunc(770, 771)
        GL11.glDisable(2929)
        GL11.glEnable(2848)
        GL11.glDepthMask(false)

        for (o in mc.theWorld.loadedEntityList) {
            if (o is EntityLivingBase && !o.isInvisible && !o.isSneaking && o !== mc.thePlayer && o.canEntityBeSeen(
                    mc.thePlayer
                ) && !o.isInvisible && o is EntityPlayer
            ) {
                val posX = o.lastTickPosX + (o.posX - o.lastTickPosX) * event.getPartialTicks()
                    .toDouble() - mc.renderManager.viewerPosX
                val posY = o.lastTickPosY + (o.posY - o.lastTickPosY) * event.getPartialTicks()
                    .toDouble() - mc.renderManager.viewerPosY
                val posZ = o.lastTickPosZ + (o.posZ - o.lastTickPosZ) * event.getPartialTicks()
                    .toDouble() - mc.renderManager.viewerPosZ

                this.circle(posX, posY, posZ, if (mc.playerController.isInCreativeMode) 4.7 else 3.4)
            }
        }

        GL11.glDepthMask(true)
        GL11.glDisable(2848)
        GL11.glEnable(2929)
        GL11.glDisable(3042)
        GL11.glEnable(3553)
        GL11.glPopMatrix()
    }

    private fun circle(x: Double, y: Double, z: Double, rad: Double) {
        val currentColor = getInstance().colorManager.getCurrentColor()

        GL11.glPushMatrix()
        val color = applyAlpha(currentColor.getInterpolateColor(), 120)

        GL11.glLineWidth(lineWidthSetting.toFloat())
        setColor(color.rgb)
        GL11.glBegin(1)

        for (i in 0..90) {
            setColor(color.rgb, 0.4f)
            GL11.glVertex3d(
                x + rad * cos(i.toDouble() * 6.283185307179586 / 45.0),
                y,
                z + rad * sin(i.toDouble() * 6.283185307179586 / 45.0)
            )
        }

        GL11.glEnd()
        GL11.glPopMatrix()
    }
}





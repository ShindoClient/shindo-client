package me.miki.shindo.management.mods.impl

import me.miki.shindo.Shindo
import me.miki.shindo.injection.interfaces.IMixinS14PacketEntity
import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventReceivePacket
import me.miki.shindo.management.event.impl.EventRender3D
import me.miki.shindo.management.event.impl.EventUpdate
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.utils.ColorUtils.applyAlpha
import me.miki.shindo.utils.ColorUtils.setColor
import me.miki.shindo.utils.Render3DUtils.drawBoundingBox
import me.miki.shindo.utils.ServerUtils.isHypixel
import me.miki.shindo.utils.TargetUtils
import net.minecraft.client.entity.AbstractClientPlayer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.network.play.server.S14PacketEntity
import net.minecraft.network.play.server.S18PacketEntityTeleport
import org.lwjgl.opengl.GL11

class PlayerPredicatorMod : Mod(
    TranslateText.PLAYER_PREDICATOR,
    TranslateText.PLAYER_PREDICATOR_DESCRIPTION,
    ModCategory.WORLD,
    LegacyIcon.MOD_PLAYER_PREDICATOR,
    "",
    true
) {
    private var realTargetPosition = Position(0.0, 0.0, 0.0)
    private var target: AbstractClientPlayer? = null
    private var isActive = false

    @EventTarget
    fun onUpdate(event: EventUpdate?) {
        target = TargetUtils.target

        if (target == null) {
            isActive = true
            return
        }

        if (isActive) {
            realTargetPosition = Position(target!!.posX, target!!.posY, target!!.posZ)
            isActive = false
        }
    }

    @EventTarget
    fun onReceivePacket(event: EventReceivePacket) {
        val packet = event.getPacket()

        if (target == null) {
            return
        }

        if (packet is S14PacketEntity) {
            val iS14PacketEntity = packet as IMixinS14PacketEntity

            if (iS14PacketEntity.entityId == target!!.entityId) {
                realTargetPosition.x += iS14PacketEntity.posX / 32.0
                realTargetPosition.y += iS14PacketEntity.posY / 32.0
                realTargetPosition.z += iS14PacketEntity.posZ / 32.0
            }
        } else if (packet is S18PacketEntityTeleport) {

            realTargetPosition = Position(
                packet.x / 32.0,
                packet.y / 32.0,
                packet.z / 32.0
            )
        }
    }

    @EventTarget
    fun onRender3D(event: EventRender3D?) {
        if (target == null) {
            return
        }

        if (realTargetPosition.squareDistanceTo(
                target!!.posX,
                target!!.posY,
                target!!.posZ
            ) > 0.00001 && !isHypixel()
        ) {
            GlStateManager.pushMatrix()
            GlStateManager.pushAttrib()
            GlStateManager.enableBlend()
            GlStateManager.disableTexture2D()
            GlStateManager.disableLighting()
            GL11.glDepthMask(false)

            val expand = 0.14

            setColor(applyAlpha(Shindo.getInstance().getColorManager().getCurrentColor().getInterpolateColor(0), 80).rgb)

            drawBoundingBox(
                mc.thePlayer.entityBoundingBox.offset(-mc.thePlayer.posX, -mc.thePlayer.posY, -mc.thePlayer.posZ)
                    .offset
                        (realTargetPosition.x, realTargetPosition.y, realTargetPosition.z)
                    .expand(expand, expand, expand)
            )

            GlStateManager.enableTexture2D()
            GlStateManager.enableLighting()
            GlStateManager.disableBlend()
            GL11.glDepthMask(true)
            GlStateManager.popAttrib()
            GlStateManager.popMatrix()
            GlStateManager.resetColor()
        }
    }

    private data class Position(var x: Double, var y: Double, var z: Double) {
        fun squareDistanceTo(x: Double, y: Double, z: Double): Double {
            val d0 = x - this.x
            val d1 = y - this.y
            val d2 = z - this.z

            return d0 * d0 + d1 * d1 + d2 * d2
        }
    }
}





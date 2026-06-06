package com.shindoclient.shindo.management.mods.impl

import com.shindoclient.shindo.Shindo
import com.shindoclient.shindo.gui.GuiWaypoint
import com.shindoclient.shindo.injection.interfaces.IMixinRenderManager
import com.shindoclient.shindo.management.event.EventTarget
import com.shindoclient.shindo.management.event.impl.EventKey
import com.shindoclient.shindo.management.event.impl.EventRender3D
import com.shindoclient.shindo.management.language.TranslateText
import com.shindoclient.shindo.management.mods.Mod
import com.shindoclient.shindo.management.mods.ModCategory
import com.shindoclient.shindo.management.nanovg.font.Shinconic
import com.shindoclient.shindo.management.settings.config.Property
import com.shindoclient.shindo.management.settings.config.PropertyType
import com.shindoclient.shindo.management.waypoint.Waypoint
import com.shindoclient.shindo.utils.ColorUtils.getColorByInt
import com.shindoclient.shindo.utils.render.RenderUtils.drawOutline
import com.shindoclient.shindo.utils.render.RenderUtils.drawRect
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.sqrt

class WaypointMod : Mod(TranslateText.WAYPOINT, TranslateText.WAYPOINT_DESCRIPTION, ModCategory.WORLD, Shinconic.MOD_WAYPOINT) {
    @Property(type = PropertyType.KEYBIND, translate = TranslateText.KEYBIND, keyCode = Keyboard.KEY_B)
    private val keybindSetting = Keyboard.KEY_B

    @EventTarget
    fun onRender3D(event: EventRender3D?) {
        for (wy in Shindo.getInstance().getWaypointManager().getWaypoints()) {
            if (Shindo.getInstance().getWaypointManager().getWorld() == wy.getWorld()) {
                var distance = this.getDistance(wy, mc.renderViewEntity)
                val renderDistance = (mc.gameSettings.renderDistanceChunks * 16) * 0.75

                val tagName = wy.getName() + " [" + distance.toInt() + "m]"

                var x = wy.getX() - (mc.renderManager as IMixinRenderManager).renderPosX
                var y = 2.0 + wy.getY() - (mc.renderManager as IMixinRenderManager).renderPosY
                var z = wy.getZ() - (mc.renderManager as IMixinRenderManager).renderPosZ

                if (distance > renderDistance) {
                    x = x / distance * renderDistance
                    y = y / distance * renderDistance
                    z = z / distance * renderDistance
                    distance = renderDistance
                }

                val scale = (0.016666668f * (1.0 + distance) * 0.15).toFloat()

                GL11.glPushMatrix()
                GlStateManager.translate(x, y, z)
                GlStateManager.disableDepth()

                GlStateManager.rotate(-mc.renderManager.playerViewY, 0.0f, 1.0f, 0.0f)
                GlStateManager.rotate(mc.renderManager.playerViewX, 1.0f, 0.0f, 0.0f)
                GlStateManager.scale(-scale, -scale, scale)

                val width = fr.getStringWidth(tagName)
                val height = fr.FONT_HEIGHT

                val rectWidth = width + 10
                val rectHeight = height + 6

                drawRect(
                    -rectWidth / 2f,
                    -rectHeight / 2f,
                    rectWidth.toFloat(),
                    rectHeight.toFloat(),
                    getColorByInt(Int.MIN_VALUE),
                )
                drawOutline(
                    -rectWidth / 2f,
                    -rectHeight / 2f,
                    rectWidth.toFloat(),
                    rectHeight.toFloat(),
                    2.5f,
                    wy.getColor(),
                )

                fr.drawString(tagName, -width / 2, -height / 2 + 2, Color.WHITE.rgb)

                GlStateManager.enableDepth()
                GL11.glPopMatrix()
            }
        }
    }

    @EventTarget
    fun onKey(event: EventKey) {
        if (event.getKeyCode() == keybindSetting) {
            mc.displayGuiScreen(GuiWaypoint())
        }
    }

    private fun getDistance(
        wy: Waypoint,
        entity: Entity,
    ): Double {
        val x = wy.getX() - entity.posX
        val y = wy.getY() - entity.posY
        val z = wy.getZ() - entity.posZ

        return sqrt(x * x + y * y + z * z)
    }
}

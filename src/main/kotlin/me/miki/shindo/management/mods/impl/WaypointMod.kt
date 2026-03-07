package me.miki.shindo.management.mods.impl

import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.Shindo.Companion.getInstance
import me.miki.shindo.gui.GuiWaypoint
import me.miki.shindo.injection.interfaces.IMixinRenderManager
import me.miki.shindo.management.event.impl.EventKey
import me.miki.shindo.management.event.impl.EventRender3D
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyType
import me.miki.shindo.management.waypoint.Waypoint
import me.miki.shindo.utils.ColorUtils.getColorByInt
import me.miki.shindo.utils.render.RenderUtils.drawOutline
import me.miki.shindo.utils.render.RenderUtils.drawRect
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.sqrt

class WaypointMod :
    Mod(TranslateText.WAYPOINT, TranslateText.WAYPOINT_DESCRIPTION, ModCategory.WORLD, LegacyIcon.MOD_WAYPOINT) {
    @Property(type = PropertyType.KEYBIND, translate = TranslateText.KEYBIND, keyCode = Keyboard.KEY_B)
    private val keybindSetting = Keyboard.KEY_B

    @EventTarget
    fun onRender3D(event: EventRender3D?) {
        for (wy in getInstance().waypointManager.getWaypoints()) {
            if (getInstance().waypointManager.getWorld() == wy.getWorld()) {
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
                    getColorByInt(Int.MIN_VALUE)
                )
                drawOutline(
                    -rectWidth / 2f,
                    -rectHeight / 2f,
                    rectWidth.toFloat(),
                    rectHeight.toFloat(),
                    2.5f,
                    wy.getColor()
                )

                fr.drawString(tagName, -width / 2, -height / 2 + 2, Color.WHITE.rgb)

                GlStateManager.enableDepth()
                GL11.glPopMatrix()
            }
        }
    }

    @EventTarget
    fun onKey(event: EventKey) {
        if (event.keyCode == keybindSetting) {
            mc.displayGuiScreen(GuiWaypoint())
        }
    }

    private fun getDistance(wy: Waypoint, entity: Entity): Double {
        val x = wy.getX() - entity.posX
        val y = wy.getY() - entity.posY
        val z = wy.getZ() - entity.posZ

        return sqrt(x * x + y * y + z * z)
    }
}





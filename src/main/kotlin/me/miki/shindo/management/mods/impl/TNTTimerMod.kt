package me.miki.shindo.management.mods.impl

import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventNVG
import me.miki.shindo.management.event.impl.EventRenderTNT
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.mods.SimpleHUDMod
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyEnum
import me.miki.shindo.management.settings.config.PropertyType
import me.miki.shindo.utils.ServerUtils.isHypixel
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.item.EntityTNTPrimed
import net.minecraft.util.MovingObjectPosition.MovingObjectType
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.text.DecimalFormat
import kotlin.math.min

class TNTTimerMod :
    SimpleHUDMod(TranslateText.TNT_TIMER, TranslateText.TNT_TIMER_DESCRIPTION, LegacyIcon.MOD_TNT_TIMER) {
    @Property(type = PropertyType.COMBO, translate = TranslateText.TYPE)
    private val displayMode = DisplayMode.TAG

    private val timeFormatter = DecimalFormat("0.00")
    private var currentTNT: EntityTNTPrimed? = null
    private val partialTicks = 0f

    @EventTarget
    fun onRender2D(event: EventNVG) {
        if (displayMode == DisplayMode.HUD) {
            this.draw()
            this.setCategory(ModCategory.HUD)
            this.setDraggable(true)
        } else {
            this.setDraggable(false)
            this.setCategory(ModCategory.RENDER)
        }
    }

    @EventTarget
    fun onRenderTNT(event: EventRenderTNT) {
        if (displayMode == DisplayMode.TAG) {
            val fuseTimer = if (isHypixel()) event.getEntity().fuse - 28 else event.getEntity().fuse

            if (fuseTimer >= 1) {
                val distance =
                    event.getEntity().getDistanceSqToEntity(event.getTntRenderer().getRenderManager().livingPlayer)

                if (distance <= 4096.0) {
                    val number = (fuseTimer.toFloat() - event.getPartialTicks()) / 20.0f
                    val time = timeFormatter.format(number.toDouble())
                    val fontrenderer = event.getTntRenderer().fontRendererFromRenderManager

                    GlStateManager.pushMatrix()
                    GlStateManager.translate(
                        event.getX().toFloat() + 0.0f,
                        event.getY().toFloat() + event.getEntity().height + 0.5f,
                        event.getZ().toFloat()
                    )
                    GL11.glNormal3f(0.0f, 1.0f, 0.0f)
                    GlStateManager.rotate(-event.getTntRenderer().getRenderManager().playerViewY, 0.0f, 1.0f, 0.0f)
                    var xMultiplier: Byte = 1

                    if (Minecraft.getMinecraft().gameSettings.thirdPersonView == 2) {
                        xMultiplier = -1
                    }

                    val scale = 0.02666667f

                    GlStateManager.rotate(
                        event.getTntRenderer().getRenderManager().playerViewX * xMultiplier.toFloat(),
                        1.0f,
                        0.0f,
                        0.0f
                    )
                    GlStateManager.scale(-scale, -scale, scale)
                    GlStateManager.disableLighting()
                    GlStateManager.depthMask(false)
                    GlStateManager.disableDepth()
                    GlStateManager.enableBlend()
                    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
                    val tessellator = Tessellator.getInstance()
                    val worldrenderer = tessellator.worldRenderer
                    val stringWidth = fontrenderer.getStringWidth(time) shr 1
                    val green = min(fuseTimer.toFloat() / (if (isHypixel()) 52.0f else 80.0f), 1.0f)
                    val color = Color(1.0f - green, green, 0.0f)

                    GlStateManager.enableDepth()
                    GlStateManager.depthMask(true)
                    GlStateManager.disableTexture2D()
                    worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR)
                    worldrenderer.pos((-stringWidth - 1).toDouble(), -1.0, 0.0).color(0.0f, 0.0f, 0.0f, 0.25f)
                        .endVertex()
                    worldrenderer.pos((-stringWidth - 1).toDouble(), 8.0, 0.0).color(0.0f, 0.0f, 0.0f, 0.25f)
                        .endVertex()
                    worldrenderer.pos((stringWidth + 1).toDouble(), 8.0, 0.0).color(0.0f, 0.0f, 0.0f, 0.25f).endVertex()
                    worldrenderer.pos((stringWidth + 1).toDouble(), -1.0, 0.0).color(0.0f, 0.0f, 0.0f, 0.25f)
                        .endVertex()
                    tessellator.draw()
                    GlStateManager.enableTexture2D()
                    fontrenderer.drawString(time, -fontrenderer.getStringWidth(time) shr 1, 0, color.rgb)
                    GlStateManager.enableLighting()
                    GlStateManager.disableBlend()
                    GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
                    GlStateManager.popMatrix()
                }
            }
        }
    }

    override fun getText(): String {
        if ((mc.objectMouseOver != null && mc.objectMouseOver.entityHit != null && mc.objectMouseOver.typeOfHit == MovingObjectType.ENTITY && mc.objectMouseOver.entityHit is EntityTNTPrimed)) {
            currentTNT = mc.objectMouseOver.entityHit as EntityTNTPrimed
        }

        if (currentTNT != null) {
            val fuseTimer = if (isHypixel()) currentTNT!!.fuse - 28 else currentTNT!!.fuse

            if (fuseTimer >= 1) {
                val number = (fuseTimer.toFloat() - partialTicks) / 20.0f
                val time = timeFormatter.format(number.toDouble())

                return time + "s"
            } else {
                currentTNT = null
            }
        }

        return "There is no TNT"
    }

    private enum class DisplayMode(private val translate: TranslateText) : PropertyEnum {
        TAG(TranslateText.TAG),
        HUD(TranslateText.HUD);

        override fun getTranslate(): TranslateText {
            return translate
        }
    }
}




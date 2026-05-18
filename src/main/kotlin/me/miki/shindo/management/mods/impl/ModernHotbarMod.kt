package me.miki.shindo.management.mods.impl

import me.miki.extensions.ui.animation.setAnimation
import me.miki.shindo.Shindo
import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventNVG
import me.miki.shindo.management.event.impl.EventRender2D
import me.miki.shindo.management.event.impl.EventRenderExpBar
import me.miki.shindo.management.event.impl.EventRenderTooltip
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.HUDMod
import me.miki.shindo.management.mods.impl.InternalSettingsMod.HudTheme
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyEnum
import me.miki.shindo.management.settings.config.PropertyType
import me.miki.shindo.ui.animation.v2.value.SimpleAnimation
import me.miki.shindo.utils.ColorUtils.applyAlpha
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.entity.player.EntityPlayer
import java.awt.Color
import kotlin.math.max

class ModernHotbarMod : HUDMod(TranslateText.MODERN_HOTBAR, TranslateText.MODERN_HOTBAR_DESCRIPTION, LegacyIcon.MOD_MODERN_HOTBAR) {
    private val selectorAnimation = SimpleAnimation(0.0f)

    @Property(type = PropertyType.COMBO, translate = TranslateText.DESIGN)
    private val design = Design.CLIENT

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.SMOOTH)
    private val smoothSetting = true

    @Property(type = PropertyType.COMBO, translate = TranslateText.PICKUP_ANIM)
    private val pickupAnimation = PickupAnimation.PICKUP_POP

    private var barX = 0f
    private var barY = 0f
    private var barWidth = 0f
    private var barHeight = 0f
    private var selX = 0f

    init {
        this.setDraggable(false)
    }

    @EventTarget
    fun onRenderNVG(event: EventNVG) {
        drawNanoVG(event.renderer())
    }

    @EventTarget
    fun onRender2D(event: EventRender2D) {
        val sr = ScaledResolution(mc)
        val currentDesign = design
        if (this.isEditing()) {
            return
        }

        if (mc.renderViewEntity is EntityPlayer) {
            val entityplayer = mc.renderViewEntity as EntityPlayer

            GlStateManager.enableRescaleNormal()
            GlStateManager.enableBlend()
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
            RenderHelper.enableGUIStandardItemLighting()

            for (j in 0..8) {
                val k = sr.scaledWidth / 2 - 90 + j * 20 + 2
                var l = sr.scaledHeight - 16 - 3

                if (currentDesign == Design.CHILL) {
                    l = l + 4
                }

                renderHotBarItem(j, k, l - 4, event.getPartialTicks(), entityplayer)
            }

            RenderHelper.disableStandardItemLighting()
            GlStateManager.disableRescaleNormal()
            GlStateManager.disableBlend()
        }
    }

    private fun renderHotBarItem(
        index: Int,
        xPos: Int,
        yPos: Int,
        partialTicks: Float,
        entityPlayer: EntityPlayer,
    ) {
        val animationMode = pickupAnimation
        val itemstack = entityPlayer.inventory.mainInventory[index]
        val animTreatment = animationMode == PickupAnimation.PICKUP_BREAD

        if (itemstack != null) {
            val take = if (animTreatment) partialTicks / 2 else partialTicks
            val progress = itemstack.animationsToGo.toFloat() - take
            if (progress > 0.0f) {
                GlStateManager.pushMatrix()
                GlStateManager.translate((xPos + 8).toFloat(), (yPos + 12).toFloat(), 0.0f)
                if (animationMode == PickupAnimation.PICKUP_BREAD) {
                    val scaleAmount = 1.0f + progress / 2.5f
                    GlStateManager.scale(max(1.0f, scaleAmount / (1.0f / (scaleAmount / 2))), scaleAmount, 1.0f)
                } else if (animationMode == PickupAnimation.PICKUP_POP) {
                    val scaleAmount = 1.0f + progress / 5.0f
                    GlStateManager.scale(scaleAmount, scaleAmount, 1.0f)
                } else {
                    val scaleAmount = 1.0f + progress / 5.0f
                    GlStateManager.scale(1.0f / scaleAmount, (scaleAmount + 1.0f) / 2.0f, 1.0f)
                }

                GlStateManager.translate(-(xPos + 8).toFloat(), -(yPos + 12).toFloat(), 0.0f)
            }

            mc.renderItem.renderItemAndEffectIntoGUI(itemstack, xPos, yPos)

            if (progress > 0.0f) {
                GlStateManager.popMatrix()
            }

            mc.renderItem.renderItemOverlays(mc.fontRendererObj, itemstack, xPos, yPos)
        }
    }

    private fun drawNanoVG(nvg: NanoVGManager) {
        val sr = ScaledResolution(mc)
        val currentDesign = design
        val currentColor = Shindo.getInstance().getColorManager().getCurrentColor()
        val isText = InternalSettingsMod.instance.hudTheme == HudTheme.TEXT

        if (mc.renderViewEntity is EntityPlayer) {
            if (currentDesign != Design.CHILL) {
                barX = sr.scaledWidth / 2.0f - 91
                barY = (sr.scaledHeight - 26).toFloat()
                barWidth = (91 * 2).toFloat()
                barHeight = 22f

                if (currentDesign == Design.SHINDO) {
                    nvg.drawShadow(barX, barY, barWidth, barHeight, 6f)
                    nvg.drawGradientRoundedRect(
                        barX,
                        barY,
                        barWidth,
                        barHeight,
                        6f,
                        applyAlpha(currentColor.getColor1(), 190),
                        applyAlpha(currentColor.getColor2(), 190),
                    )
                } else if (currentDesign == Design.CLIENT) {
                    if (isText) {
                        nvg.drawShadow(barX, barY, barWidth, barHeight, 6f)
                    }
                    this.setScale(1f)
                    this.setX(barX.toInt())
                    this.setY(barY.toInt())
                    drawBackground(barWidth, barHeight, 6f)
                } else {
                    nvg.drawShadow(barX, barY, barWidth, barHeight, 6f)
                    nvg.drawRoundedRect(barX, barY, barWidth, barHeight, 6f, Color(0, 0, 0, 100))
                }
            } else {
                barX = 0f
                barY = (sr.scaledHeight - 22).toFloat()
                barWidth = sr.scaledWidth.toFloat()
                barHeight = 22f

                nvg.drawShadow(barX, barY, barWidth, barHeight, 0f)
                nvg.drawRect(barX, barY, barWidth, barHeight, Color(20, 20, 20, 180))
            }

            val entityplayer = mc.renderViewEntity as EntityPlayer

            val i = sr.scaledWidth / 2

            if (smoothSetting) {
                selectorAnimation.setAnimation((i - 91 - 1 + entityplayer.inventory.currentItem * 20).toFloat(), 18)
                selX = selectorAnimation.getValue()
            } else {
                selX = (i - 91 - 1 + entityplayer.inventory.currentItem * 20).toFloat()
            }

            if (currentDesign != Design.CHILL) {
                if (currentDesign == Design.SHINDO) {
                    nvg.drawRoundedRect(
                        selX + 1,
                        (sr.scaledHeight - 22 - 4).toFloat(),
                        22f,
                        22f,
                        6f,
                        Color(255, 255, 255, 140),
                    )
                } else {
                    nvg.drawRoundedRect(
                        selX + 1,
                        (sr.scaledHeight - 22 - 4).toFloat(),
                        22f,
                        22f,
                        6f,
                        Color(0, 0, 0, 100),
                    )
                }
            } else {
                nvg.drawRect(selX + 1, (sr.scaledHeight - 22).toFloat(), 22f, 22f, Color(230, 230, 230, 180))
            }
        }
    }

    @EventTarget
    fun onRenderTooltip(event: EventRenderTooltip) {
        event.setCancelled(true)
    }

    @EventTarget
    fun onRenderExpBar(event: EventRenderExpBar) {
        event.setCancelled(design != Design.CHILL)
    }

    private enum class Design(
        private val translate: TranslateText,
    ) : PropertyEnum {
        NORMAL(TranslateText.NORMAL),
        SHINDO(TranslateText.SHINDO),
        CHILL(TranslateText.CHILL),
        CLIENT(TranslateText.CLIENT),
        ;

        override fun getTranslate(): TranslateText = translate
    }

    private enum class PickupAnimation(
        private val translate: TranslateText,
    ) : PropertyEnum {
        PICKUP_POP(TranslateText.PICKUP_POP),
        PICKUP_BREAD(TranslateText.PICKUP_BREAD),
        PICKUP_VANILLA(TranslateText.PICKUP_VANILLA),
        ;

        override fun getTranslate(): TranslateText = translate
    }
}

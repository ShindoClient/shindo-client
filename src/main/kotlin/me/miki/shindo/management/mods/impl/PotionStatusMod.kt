package me.miki.shindo.management.mods.impl

import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventNVG
import me.miki.shindo.management.event.impl.EventRender2D
import me.miki.shindo.management.event.impl.EventUpdate
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.HUDMod
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Lucide
import me.miki.shindo.management.nanovg.font.Shinconic
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyType
import me.miki.shindo.utils.GlUtils.startScale
import me.miki.shindo.utils.GlUtils.stopScale
import me.miki.shindo.utils.render.RenderUtils.drawTexturedModalRect
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.resources.I18n
import net.minecraft.potion.Potion
import net.minecraft.potion.PotionEffect
import net.minecraft.util.ResourceLocation

class PotionStatusMod : HUDMod(TranslateText.POTION_STATUS, TranslateText.POTION_STATUS_DESCRIPTION, Shinconic.MOD_POTION_STATUS) {
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.COMPACT)
    private val compact = false
    private var maxString = 0
    private var prevPotionCount = 0
    private var potions: MutableCollection<PotionEffect>? = null

    @EventTarget
    fun onUpdate(event: EventUpdate?) {
        potions =
            if (this.isEditing() || mc.thePlayer == null) {
                arrayListOf(PotionEffect(1, 0), PotionEffect(10, 0))
            } else {
                mc.thePlayer.activePotionEffects
            }
    }

    @EventTarget
    fun onRenderNVG(event: EventNVG) {
        drawNanoVG(event.renderer())
    }

    @EventTarget
    fun onRender2D(event: EventRender2D?) {
        if (!potions!!.isEmpty()) {
            val ySize = if (compact) 22 else 23
            var offsetY = 16

            for (potioneffect in potions!!) {
                val potion = Potion.potionTypes[potioneffect.potionID]
                GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
                mc.textureManager.bindTexture(ResourceLocation("textures/gui/container/inventory.png"))
                val index = potion.statusIconIndex
                GlStateManager.enableBlend()

                startScale(this.getX().toFloat(), this.getY().toFloat(), this.getScale())

                if (compact) {
                    startScale(
                        ((this.getX() + 21) - 20).toFloat(),
                        (this.getY() + offsetY) - 11 - offsetY - 2f,
                        18f,
                        18f,
                        0.72f,
                    )
                    drawTexturedModalRect(
                        (this.getX() + 21) - 20,
                        (this.getY() + offsetY) - 11,
                        index % 8 * 18,
                        198 + index / 8 * 18,
                        18,
                        18,
                    )
                    stopScale()
                } else {
                    drawTexturedModalRect(
                        (this.getX() + 21) - 17,
                        (this.getY() + offsetY) - 12,
                        index % 8 * 18,
                        198 + index / 8 * 18,
                        18,
                        18,
                    )
                }

                stopScale()

                offsetY += ySize
            }
        }
    }

    private fun drawNanoVG(nvg: NanoVGManager) {
        val ySize = if (compact) 16 else 23
        var offsetY = 16

        if (potions!!.isEmpty()) {
            maxString = 0
        }

        if (!potions!!.isEmpty()) {
            this.drawBackground((maxString + 29).toFloat(), ((ySize * potions!!.size) + 2).toFloat())

            for (potioneffect in potions!!) {
                val potion = Potion.potionTypes[potioneffect.potionID]

                var name = I18n.format(potion.name)

                when (potioneffect.amplifier) {
                    1 -> {
                        name = name + " " + I18n.format("enchantment.level.2")
                    }

                    2 -> {
                        name = name + " " + I18n.format("enchantment.level.3")
                    }

                    3 -> {
                        name = name + " " + I18n.format("enchantment.level.4")
                    }
                }

                val time = Potion.getDurationString(potioneffect)

                if (compact) {
                    this.drawText("$name | $time", 20f, offsetY - 10.5f, 9f, getHudFont(1))
                } else {
                    this.drawText(name, 25f, (offsetY - 12).toFloat(), 9f, getHudFont(1))
                    this.drawText(time, 25f, (offsetY - 1).toFloat(), 8f, getHudFont(1))
                }

                offsetY += ySize

                if (compact) {
                    val totalWidth = nvg.getTextWidth("$name | $time", 9f, getHudFont(1))

                    if (maxString < totalWidth || prevPotionCount != potions!!.size) {
                        maxString = totalWidth.toInt() - 4
                    }
                } else {
                    val levelWidth = nvg.getTextWidth(name, 9f, getHudFont(1))
                    val timeWidth = nvg.getTextWidth(time, 9f, getHudFont(1))

                    if (maxString < levelWidth || maxString < timeWidth || prevPotionCount != potions!!.size) {
                        maxString =
                            if (levelWidth > timeWidth) {
                                (levelWidth).toInt()
                            } else {
                                (timeWidth).toInt()
                            }

                        prevPotionCount = potions!!.size
                    }
                }
            }
        }

        this.setWidth(maxString + 29)
        this.setHeight((ySize * 2) + 2)
    }
}

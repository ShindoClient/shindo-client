package me.miki.shindo.management.mods.impl

import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.injection.interfaces.IMixinGuiIngame
import me.miki.shindo.management.event.impl.EventRenderPlayerStats
import me.miki.shindo.management.event.impl.EventTick
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.mods.impl.appleskin.AppleSkinHelper
import me.miki.shindo.management.nanovg.font.LegacyIcon
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.item.ItemFood
import net.minecraft.potion.Potion
import org.lwjgl.opengl.GL11
import java.util.*
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

class AppleSkinMod :
    Mod(TranslateText.APPLE_SKIN, TranslateText.APPLE_SKIN_DESCRIPTION, ModCategory.PLAYER, LegacyIcon.MOD_APPLE_SKIN) {
    private val random = Random()
    var foodBarOffsets: Vector<IntPoint?> = Vector<IntPoint?>()
    private var unclampedFlashAlpha = 0f
    private var flashAlpha = 0f
    private var alphaDir: Byte = 1

    @EventTarget
    fun onRenderPlayerStats(event: EventRenderPlayerStats?) {
        val scaledResolution = ScaledResolution(mc)
        val stats = mc.thePlayer.foodStats

        val right = scaledResolution.scaledWidth / 2 + 91
        val top = scaledResolution.scaledHeight - 39

        this.generateHungerBarOffsets(right, 0, (mc.ingameGUI as IMixinGuiIngame).updateCounter)

        this.drawSaturationOverlay(0f, stats.saturationLevel, 0, stats.foodLevel, right, top, 1.0f)

        val heldItem = mc.thePlayer.heldItem

        val holdingFood = heldItem != null && heldItem.item is ItemFood

        if (!holdingFood) {
            this.resetFlash()
            return
        }

        val foodValues = AppleSkinHelper.getFoodValues(heldItem)

        val foodHunger = foodValues.hunger
        val foodSaturationIncrement = foodValues.saturationIncrement

        val newFoodValue = stats.foodLevel + foodHunger
        val newSaturationValue = stats.saturationLevel + foodSaturationIncrement
        val saturationGained =
            if (newSaturationValue > newFoodValue) newFoodValue - stats.saturationLevel else foodSaturationIncrement

        this.drawHungerOverlay(
            foodHunger,
            stats.foodLevel,
            right,
            top,
            flashAlpha,
            AppleSkinHelper.isRottenFood(heldItem)
        )

        this.drawSaturationOverlay(
            saturationGained,
            stats.saturationLevel,
            foodHunger,
            stats.foodLevel,
            right,
            top,
            flashAlpha
        )
    }

    @EventTarget
    fun onTick(event: EventTick?) {
        unclampedFlashAlpha += alphaDir * 0.125f

        if (unclampedFlashAlpha >= 1.5f) {
            alphaDir = -1
        } else if (unclampedFlashAlpha <= -0.5f) {
            alphaDir = 1
        }

        flashAlpha = max(0.0f, min(1.0f, unclampedFlashAlpha)) * min(1.0f, 1.0f)
    }

    private fun generateHungerBarOffsets(right: Int, top: Int, ticks: Int) {
        random.setSeed(ticks * 312871L)

        val preferFoodBars = 10

        val stats = mc.thePlayer.foodStats

        val saturationLevel = stats.saturationLevel
        val foodLevel = stats.foodLevel

        val shouldAnimatedFood =
            saturationLevel <= 0.0f && (mc.ingameGUI as IMixinGuiIngame).updateCounter % (foodLevel * 3 + 1) == 0

        if (foodBarOffsets.size != preferFoodBars) {
            foodBarOffsets.setSize(preferFoodBars)
        }

        for (i in 0 until preferFoodBars) {
            val x = right - i * 8 - 9
            var y = top

            if (shouldAnimatedFood) {
                y += random.nextInt(3) - 1
            }

            var point = foodBarOffsets[i]

            if (point == null) {
                point = IntPoint()
                foodBarOffsets[i] = point
            }

            point.x = x - right
            point.y = y
        }
    }

    private fun drawSaturationOverlay(
        saturationGained: Float,
        saturationLevel: Float,
        hungerRestored: Int,
        foodLevel: Int,
        right: Int,
        top: Int,
        alpha: Float
    ) {
        if (saturationLevel + saturationGained < 0) {
            return
        }

        GlStateManager.enableBlend()
        GlStateManager.color(1.0f, 1.0f, 1.0f, alpha)
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

        val modifiedSaturation = max(0f, min(20f, saturationLevel + saturationGained))

        val modifiedFood = max(0, min(20, foodLevel + hungerRestored))

        var startSaturationBar = 0
        val endSaturationBar = ceil((modifiedSaturation / 2.0f).toDouble()).toInt()

        if (saturationGained != 0f) {
            startSaturationBar = max(saturationLevel / 2.0f, 0f).toInt()
        }

        val iconStartOffset = 16
        val iconSize = 9

        for (i in startSaturationBar until endSaturationBar) {
            val offset = foodBarOffsets[i] ?: continue

            val x = right + offset.x
            val y = top + offset.y

            val v = 3 * iconSize
            var u = iconStartOffset + 4 * iconSize
            var ub = iconStartOffset + iconSize

            for (e in mc.thePlayer.activePotionEffects) {
                if (e.potionID == Potion.hunger.getId()) {
                    u += 4 * iconSize
                    break
                }
            }

            var ubX = x
            var ubIconSize = iconSize

            if (i * 2 + 1 == modifiedSaturation.toInt()) {
                val halfIconSize = iconSize / 2

                ubX += halfIconSize
                ub += halfIconSize
                ubIconSize -= halfIconSize
            }

            if (i * 2 + 1 == modifiedFood) {
                u += iconSize
            }

            GlStateManager.color(0.75f, 0.65f, 0.0f, alpha)
            mc.ingameGUI.drawTexturedModalRect(ubX, y, ub, v, ubIconSize, iconSize)

            if (modifiedSaturation > modifiedFood) {
                continue
            }

            GlStateManager.color(1.0f, 1.0f, 1.0f, alpha)
            mc.ingameGUI.drawTexturedModalRect(x, y, u, v, iconSize, iconSize)
        }

        GlStateManager.disableBlend()
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
    }

    private fun drawHungerOverlay(
        hungerRestored: Int,
        foodLevel: Int,
        right: Int,
        top: Int,
        alpha: Float,
        useRottenTextures: Boolean
    ) {
        if (hungerRestored <= 0) {
            return
        }

        GlStateManager.enableBlend()
        GlStateManager.color(1.0f, 1.0f, 1.0f, alpha)
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

        val modifiedFood = max(0, min(20, foodLevel + hungerRestored))

        val startFoodBars = max(0, foodLevel / 2)
        val endFoodBars = ceil((modifiedFood / 2.0f).toDouble()).toInt()

        val iconStartOffset = 16
        val iconSize = 9

        for (i in startFoodBars until endFoodBars) {
            val offset = foodBarOffsets[i] ?: continue

            val x = right + offset.x
            val y = top + offset.y

            val v = 3 * iconSize
            var u = iconStartOffset + 4 * iconSize
            var ub = iconStartOffset + iconSize

            if (useRottenTextures) {
                u += 4 * iconSize
                ub += 12 * iconSize
            }

            if (i * 2 + 1 == modifiedFood) {
                u += iconSize
            }

            GlStateManager.color(1.0f, 1.0f, 1.0f, alpha * 0.25f)
            mc.ingameGUI.drawTexturedModalRect(x, y, ub, v, iconSize, iconSize)
            GlStateManager.color(1.0f, 1.0f, 1.0f, alpha)

            mc.ingameGUI.drawTexturedModalRect(x, y, u, v, iconSize, iconSize)
        }

        GlStateManager.disableBlend()
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
    }

    private fun resetFlash() {
        unclampedFlashAlpha = 0.0f
        flashAlpha = 0.0f
        alphaDir = 1
    }

    class IntPoint {
        var x: Int = 0
        var y: Int = 0
    }
}






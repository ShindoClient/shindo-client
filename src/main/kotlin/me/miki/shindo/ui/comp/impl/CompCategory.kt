package me.miki.shindo.ui.comp.impl

import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.impl.CategorySetting
import me.miki.shindo.ui.comp.Comp
import me.miki.shindo.utils.ColorUtils
import me.miki.shindo.utils.animation.simple.SimpleAnimation
import me.miki.shindo.utils.mouse.MouseUtils
import kotlin.math.max

class CompCategory : Comp {

    private val setting: CategorySetting
    private val toggleAnimation = SimpleAnimation()
    private val hoverAnimation = SimpleAnimation()

    constructor(width: Float, setting: CategorySetting): super(0f, 0f) {
        this.setting = setting
    }

    override fun draw(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val paletteColors = palette
        val accentColor = accent

        val x      = getX()
        val y      = getY()
        val width  = getWidth()
        val height = getHeight()

        val hovered = MouseUtils.isInside(mouseX, mouseY, x, y, width, height)

        toggleAnimation.setAnimation(if (setting.isCollapsed()) 0.0f else 1.0f, 12.0)
        hoverAnimation.setAnimation(if (hovered) 1.0f else 0.0f, 12.0)

        val accentPulse = max(hoverAnimation.value, 0.25f + toggleAnimation.value * 0.25f)
        val baseOverlay = ColorUtils.applyAlpha(paletteColors.getBackgroundColor(ColorType.MID), (hoverAnimation.value * 40).toInt())
        nvg.drawRoundedRect(x, y, width, height, CATEGORY_CORNER_RADIUS, baseOverlay)

        val iconSize = 11f
        val icon = if (setting.isCollapsed()) LegacyIcon.CHEVRON_RIGHT else LegacyIcon.CHEVRON_DOWN
        val iconHeight = nvg.getTextHeight(icon, iconSize, Fonts.LEGACYICON)
        val iconX = x + 4f
        val iconY = y + height / 2f - iconHeight / 2f
        val iconColor = ColorUtils.interpolateColor(
            paletteColors.getFontColor(ColorType.NORMAL),
            ColorUtils.applyAlpha(accentColor.color1, 240),
            (accentPulse * 0.35f).toDouble()
        )
        nvg.drawText(icon, iconX, iconY, iconColor, iconSize, Fonts.LEGACYICON)

        val titleSize = 11f
        val titleX = iconX + 14f
        val titleHeight = nvg.getTextHeight(setting.name, titleSize, Fonts.MEDIUM)
        val titleY = y + height / 2f - titleHeight / 2f
        val titleColor = ColorUtils.interpolateColor(
            paletteColors.getFontColor(ColorType.DARK),
            ColorUtils.applyAlpha(accentColor.color2, 230),
            (accentPulse * 0.25f).toDouble()
        )
        nvg.drawText(setting.name, titleX, titleY, titleColor, titleSize, Fonts.MEDIUM)

        val underlineAlpha = 55 + accentPulse * 85f
        nvg.drawDivider(x, y + height - 2f, width, 2f, 1.5F,  minOf(underlineAlpha, 140f))
        super.draw(mouseX, mouseY, partialTicks)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (mouseButton == 0 && MouseUtils.isInside(mouseX, mouseY, getX(), getY(), getWidth(), getHeight())) {
            setting.toggle()
        }
        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    fun getSetting(): CategorySetting = setting


    companion object {
        private const val SETTING_TEXT_MARGIN = 12F
        private const val CATEGORY_CORNER_RADIUS = 7F
    }
}

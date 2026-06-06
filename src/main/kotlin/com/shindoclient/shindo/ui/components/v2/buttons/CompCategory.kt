package com.shindoclient.shindo.ui.components.v2.buttons

import com.shindoclient.shindo.management.color.palette.ColorType
import com.shindoclient.shindo.management.nanovg.font.Fonts
import com.shindoclient.shindo.management.nanovg.font.Lucide
import com.shindoclient.shindo.management.settings.impl.CategorySetting
import com.shindoclient.shindo.ui.animation.v2.value.SimpleAnimation
import com.shindoclient.shindo.ui.components.v2.templates.CompControlTemplate
import com.shindoclient.shindo.utils.ColorUtils
import kotlin.math.max

class CompCategory : CompControlTemplate {
    private val setting: CategorySetting
    private val toggleAnimation = SimpleAnimation()
    private val hoverAnimation = SimpleAnimation()

    constructor(width: Float, setting: CategorySetting) : super(0f, 0f) {
        this.setting = setting
        setHeight(22f)
    }

    override fun drawInteractive(
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float,
        hovered: Boolean,
    ) {
        val paletteColors = palette
        val accentColor = accent

        val x = getX()
        val y = getY()
        val width = getWidth()
        val height = getHeight()

        toggleAnimation.setAnimation(if (setting.isCollapsed()) 0.0f else 1.0f, 12.0)
        hoverAnimation.setAnimation(if (hovered) 1.0f else 0.0f, 12.0)

        val accentPulse = max(hoverAnimation.getValue(), 0.25f + toggleAnimation.getValue() * 0.25f)
        val baseOverlay =
            ColorUtils.applyAlpha(
                paletteColors.getBackgroundColor(ColorType.MID),
                (hoverAnimation.getValue() * 40).toInt(),
            )
        nvg.drawRoundedRect(x, y, width, height, CATEGORY_CORNER_RADIUS, baseOverlay)

        val iconSize = 11f
        val icon = if (setting.isCollapsed()) Lucide.CHEVRON_RIGHT else Lucide.CHEVRON_DOWN
        val iconHeight = nvg.getTextHeight(icon, iconSize, Fonts.LUCIDE)
        val iconX = x + 4f
        val iconY = y + height / 2f - iconHeight / 2f
        val iconColor =
            ColorUtils.interpolateColor(
                paletteColors.getFontColor(ColorType.NORMAL),
                ColorUtils.applyAlpha(accentColor.getColor1(), 240),
                (accentPulse * 0.35f).toDouble(),
            )
        nvg.drawText(icon, iconX, iconY, iconColor, iconSize, Fonts.LUCIDE)

        val titleSize = 11f
        val titleX = iconX + 14f
        val titleHeight = nvg.getTextHeight(setting.name, titleSize, Fonts.MEDIUM)
        val titleY = y + height / 2f - titleHeight / 2f
        val titleColor =
            ColorUtils.interpolateColor(
                paletteColors.getFontColor(ColorType.DARK),
                ColorUtils.applyAlpha(accentColor.getColor2(), 230),
                (accentPulse * 0.25f).toDouble(),
            )
        nvg.drawText(setting.name, titleX, titleY, titleColor, titleSize, Fonts.MEDIUM)

        val underlineAlpha = 55 + accentPulse * 85f
        nvg.drawDivider(x, y + height - 2f, width, 2f, 1.5F, minOf(underlineAlpha, 140f))
    }

    override fun onMouseClicked(
        mouseX: Int,
        mouseY: Int,
        mouseButton: Int,
    ) {
        if (mouseButton == 0) {
            setting.toggle()
        }
    }

    fun getSetting(): CategorySetting = setting

    companion object {
        private const val SETTING_TEXT_MARGIN = 12F
        private const val CATEGORY_CORNER_RADIUS = 7F
    }
}

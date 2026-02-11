package me.miki.shindo.gui.modmenu.category.impl.setting.impl.layout

import me.miki.shindo.gui.modmenu.category.impl.SettingsCategory
import me.miki.shindo.management.color.AccentColor
import me.miki.shindo.management.color.palette.ColorPalette
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.ui.comp.feedback.CompCarouselPageIndicator
import me.miki.shindo.ui.comp.templates.CompButton
import me.miki.shindo.ui.layout.enums.UILayoutArea
import me.miki.shindo.ui.layout.enums.UILayoutType
import me.miki.shindo.utils.ColorUtils
import me.miki.shindo.utils.mouse.MouseUtils
import kotlin.math.max

abstract class LayoutCarouselScene(
        parent: SettingsCategory,
        area: UILayoutArea,
        nameTranslate: TranslateText,
        descriptionTranslate: TranslateText,
        icon: String
) : LayoutAreaScene(parent, area, nameTranslate, descriptionTranslate, icon) {

    private var previousButtonBounds: Rect? = null
    private var nextButtonBounds: Rect? = null
    private var transitionFrom: UILayoutType? = null
    private var transitionTo: UILayoutType? = null
    private var transitionDirection = 1
    private var transitionStartNs = 0L
    private var previewType: UILayoutType? = null

    private val pageIndicator = CompCarouselPageIndicator().setDotMetrics(5f, 6f)
    private val applyButton = CompButton(width = 52f, height = 14f)
            .setRadius(6f)
            .setFontSize(8.4f)

    override fun initGui() {
        super.initGui()
        previewType = getSelectedType()
        applyButton.onClick = {
            val target = transitionTo ?: previewType
            if (target != null) {
                selectType(target)
                previewType = target
                transitionFrom = null
                transitionTo = null
                transitionStartNs = 0L
            }
        }
    }

    override fun showTypeSelector(): Boolean = false

    override fun drawPreview(
            nvg: NanoVGManager,
            palette: ColorPalette,
            accent: AccentColor,
            x: Float,
            y: Float,
            width: Float,
            height: Float,
            mouseX: Int,
            mouseY: Int,
            partialTicks: Float
    ) {
        val types = getTypes()
        if (types.isEmpty()) return

        val appliedType = getSelectedType() ?: types[0]
        if (previewType == null || !types.contains(previewType!!)) {
            previewType = appliedType
        }

        val outerPaddingTop = 6f
        val outerPaddingSide = 8f
        val outerPaddingBottom = 0f
        val navButtonSize = 24f
        val navGap = 10f
        val controlsHeight = 12f
        val controlsGap = 2f
        val controlsOffsetY = 8f

        val canvasX = x + outerPaddingSide
        val canvasY = y + outerPaddingTop
        val canvasWidth = width - outerPaddingSide * 2f
        val canvasHeight = height - outerPaddingTop - outerPaddingBottom
        val previewX = canvasX + navButtonSize + navGap
        val previewY = canvasY
        val previewWidth = max(80f, canvasWidth - (navButtonSize + navGap) * 2f)
        val previewHeight = max(42f, canvasHeight - controlsHeight - controlsGap - controlsOffsetY)
        val controlsY = previewY + previewHeight + controlsGap + controlsOffsetY
        val centerY = previewY + previewHeight / 2f - navButtonSize / 2f

        previousButtonBounds = Rect(canvasX, centerY, navButtonSize, navButtonSize)
        nextButtonBounds = Rect(canvasX + canvasWidth - navButtonSize, centerY, navButtonSize, navButtonSize)

        drawNavButton(
                nvg,
                palette,
                previousButtonBounds,
                LegacyIcon.CHEVRON_LEFT,
                previousButtonBounds?.contains(mouseX, mouseY) == true
        )
        drawNavButton(
                nvg,
                palette,
                nextButtonBounds,
                LegacyIcon.CHEVRON_RIGHT,
                nextButtonBounds?.contains(mouseX, mouseY) == true
        )

        val progress = getTransitionProgress()
        val activeFrom = transitionFrom
        val activeTo = transitionTo ?: previewType ?: appliedType

        nvg.save()
        nvg.intersectScissor(previewX, previewY, previewWidth, previewHeight)

        if (activeFrom != null && progress < 1f) {
            val eased = easeOutCubic(progress)
            val shift = previewWidth * eased
            val direction = transitionDirection.toFloat()
            drawCarouselPreset(nvg, palette, accent, activeFrom, previewX - direction * shift, previewY, previewWidth, previewHeight)
            drawCarouselPreset(nvg, palette, accent, activeTo, previewX + direction * (previewWidth - shift), previewY, previewWidth, previewHeight)
        } else {
            drawCarouselPreset(nvg, palette, accent, activeTo, previewX, previewY, previewWidth, previewHeight)
        }

        nvg.restore()

        pageIndicator
                .setPageCount(types.size)
                .setSelectedIndex(types.indexOf(activeTo).coerceAtLeast(0))
                .setBounds(previewX, controlsY, previewWidth - 56f, controlsHeight)
        pageIndicator.draw(mouseX, mouseY, partialTicks)

        val isApplied = activeTo == appliedType
        applyButton.setBounds(previewX + previewWidth - 52f, controlsY - 1f, 52f, 14f)
        applyButton.setText(if (isApplied) TranslateText.LAYOUT_APPLIED.getText() else TranslateText.LAYOUT_APPLY.getText())
        applyButton.setBackgroundColor(
                if (isApplied) {
                    ColorUtils.applyAlpha(accent.getColor1(), 236)
                } else {
                    ColorUtils.applyAlpha(accent.getColor1(), 188)
                }
        )
        applyButton.setHoverColor(
                if (isApplied) {
                    ColorUtils.applyAlpha(accent.getColor1(), 208)
                } else {
                    ColorUtils.applyAlpha(accent.getColor2(), 220)
                }
        )
        applyButton.setTextColor(palette.getFontColor(ColorType.DARK))
        applyButton.setEnabled(true)
        applyButton.draw(mouseX, mouseY, partialTicks)

        if (progress >= 1f && transitionTo == null && previewType == null) {
            previewType = appliedType
        }
    }

    override fun mouseClickedExtra(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (mouseButton != 0) return

        applyButton.mouseClicked(mouseX, mouseY, mouseButton)
        if (applyButton.isHoveredInteractive(mouseX, mouseY)) {
            return
        }

        if (previousButtonBounds?.contains(mouseX, mouseY) == true) {
            navigate(-1)
            return
        }
        if (nextButtonBounds?.contains(mouseX, mouseY) == true) {
            navigate(1)
        }
    }

    private fun navigate(direction: Int) {
        val types = getTypes()
        if (types.size <= 1) return

        val current = transitionTo ?: previewType ?: getSelectedType() ?: types[0]
        val currentIndex = types.indexOf(current).coerceAtLeast(0)
        val targetIndex = ((currentIndex + direction) % types.size + types.size) % types.size
        val targetType = types[targetIndex]
        if (targetType == current) return

        transitionFrom = current
        transitionTo = targetType
        previewType = targetType
        transitionDirection = if (direction > 0) 1 else -1
        transitionStartNs = System.nanoTime()
    }

    private fun drawNavButton(
            nvg: NanoVGManager,
            palette: ColorPalette,
            bounds: Rect?,
            symbol: String,
            hovered: Boolean
    ) {
        if (bounds == null) return
        nvg.drawRoundedRect(
                bounds.x,
                bounds.y,
                bounds.width,
                bounds.height,
                8f,
                ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), if (hovered) 205 else 175)
        )
        nvg.drawCenteredText(
                symbol,
                bounds.x + bounds.width / 2f,
                bounds.y + bounds.height / 2f - 4.5f,
                palette.getFontColor(ColorType.DARK),
                12f,
                Fonts.LEGACYICON
        )
    }

    private fun getTransitionProgress(): Float {
        val target = transitionTo ?: return 1f
        if (TRANSITION_DURATION_NS <= 0L) {
            transitionFrom = null
            transitionTo = null
            previewType = target
            return 1f
        }
        if (transitionStartNs == 0L) {
            transitionStartNs = System.nanoTime()
            return 0f
        }

        val elapsed = System.nanoTime() - transitionStartNs
        val progress = (elapsed.toDouble() / TRANSITION_DURATION_NS.toDouble()).toFloat().coerceIn(0f, 1f)
        if (progress >= 1f) {
            transitionFrom = null
            transitionTo = null
            previewType = target
            transitionStartNs = 0L
        }
        return progress
    }

    private fun easeOutCubic(t: Float): Float {
        val k = 1f - t.coerceIn(0f, 1f)
        return 1f - (k * k * k)
    }

    protected abstract fun drawCarouselPreset(
            nvg: NanoVGManager,
            palette: ColorPalette,
            accent: AccentColor,
            type: UILayoutType,
            x: Float,
            y: Float,
            width: Float,
            height: Float
    )

    protected data class Rect(
            val x: Float,
            val y: Float,
            val width: Float,
            val height: Float
    ) {
        fun contains(mouseX: Int, mouseY: Int): Boolean {
            return MouseUtils.isInside(mouseX, mouseY, x, y, width, height)
        }
    }

    companion object {
        private const val TRANSITION_DURATION_NS: Long = 260_000_000L
    }
}

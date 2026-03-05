package me.miki.shindo.gui.modmenu.category.impl.setting.impl.layout

import me.miki.shindo.gui.modmenu.category.impl.SettingsCategory
import me.miki.shindo.gui.modmenu.render.ModMenuClipCoordinator
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
import org.lwjgl.input.Keyboard
import kotlin.math.max

/**
 * Shared carousel behavior for Settings/Mods/Presets scenes.
 *
 * It encapsulates:
 * - next/previous navigation controls;
 * - animated preview transitions;
 * - explicit apply button to commit the selected type.
 */
abstract class LayoutCarouselScene(
    parent: SettingsCategory,
    area: UILayoutArea,
    nameTranslate: TranslateText,
    descriptionTranslate: TranslateText,
    icon: String
) : LayoutAreaScene(parent, area, nameTranslate, descriptionTranslate, icon) {

    private var previousBounds: Rect? = null
    private var nextBounds: Rect? = null

    private var transitionFrom: UILayoutType? = null
    private var transitionTo: UILayoutType? = null
    private var transitionDirection = 1
    private var transitionStartNs = 0L

    private var previewType: UILayoutType? = null

    private val pageIndicator = CompCarouselPageIndicator().setDotMetrics(5f, 6f)
    private val applyButton = CompButton(width = 56f, height = 15f).setRadius(6f).setFontSize(8.2f)

    override fun initGui() {
        super.initGui()
        previewType = getSelectedType()
        applyButton.onClick = {
            applyPreviewSelection()
        }
    }

    override fun showTypeSelector(): Boolean {
        return false
    }

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
        if (types.isEmpty()) {
            return
        }

        val appliedType = getSelectedType() ?: types[0]
        val currentPreview = previewType
        if (currentPreview == null || !types.contains(currentPreview)) {
            previewType = appliedType
        }

        val sidePadding = 8f
        val topPadding = 6f
        val controlsHeight = 15f
        val controlsGap = 7f
        val navSize = 23f
        val navGap = 8f

        val previewX = x + sidePadding + navSize + navGap
        val previewY = y + topPadding
        val previewWidth = max(60f, width - sidePadding * 2f - (navSize + navGap) * 2f)
        val previewHeight = max(40f, height - topPadding - controlsGap - controlsHeight)
        val controlsY = previewY + previewHeight + controlsGap
        val previewLabelHeight = 34f
        val previewLabelInset = 5f
        val previewLabelPaddingX = 14f
        val previewLabelPaddingY = 8f
        val previewContentHeight = max(28f, previewHeight - previewLabelHeight - previewLabelInset * 2f - 2f)
        val previewLabelY = previewY + previewHeight - previewLabelHeight - previewLabelInset

        previousBounds = Rect(x + sidePadding, previewY + (previewHeight - navSize) / 2f, navSize, navSize)
        nextBounds = Rect(x + width - sidePadding - navSize, previewY + (previewHeight - navSize) / 2f, navSize, navSize)

        drawNavButton(nvg, palette, previousBounds, LegacyIcon.CHEVRON_LEFT, previousBounds!!.contains(mouseX, mouseY))
        drawNavButton(nvg, palette, nextBounds, LegacyIcon.CHEVRON_RIGHT, nextBounds!!.contains(mouseX, mouseY))

        LayoutSceneRenderer.drawPreviewSurface(nvg, palette, previewX, previewY, previewWidth, previewHeight, LayoutSceneStyle.PREVIEW_RADIUS)
        nvg.drawRoundedRect(
            previewX + 1f,
            previewY + 1f,
            previewWidth - 2f,
            previewHeight - 2f,
            LayoutSceneStyle.PREVIEW_RADIUS - 1f,
            ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 194)
        )

        val progress = getTransitionProgress()
        val from = transitionFrom
        val to = transitionTo ?: previewType ?: appliedType

        ModMenuClipCoordinator.withClip(
            nvg = nvg,
            x = previewX,
            y = previewY,
            width = previewWidth,
            height = previewContentHeight,
            intersect = true
        ) {
            if (from != null && progress < 1f) {
                val eased = easeOutCubic(progress)
                val shift = previewWidth * eased
                val direction = transitionDirection.toFloat()
                drawCarouselPreset(
                    nvg,
                    palette,
                    accent,
                    from,
                    previewX - direction * shift,
                    previewY,
                    previewWidth,
                    previewContentHeight
                )
                drawCarouselPreset(
                    nvg,
                    palette,
                    accent,
                    to,
                    previewX + direction * (previewWidth - shift),
                    previewY,
                    previewWidth,
                    previewContentHeight
                )
            } else {
                drawCarouselPreset(nvg, palette, accent, to, previewX, previewY, previewWidth, previewContentHeight)
            }
        }

        nvg.drawRoundedRect(
            previewX + previewLabelInset,
            previewLabelY,
            previewWidth - previewLabelInset * 2f,
            previewLabelHeight,
            6f,
            ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 232)
        )
        val labelTextWidth = previewWidth - previewLabelPaddingX * 2f - previewLabelInset * 2f
        val labelTitle = nvg.getLimitText(getPreviewTitle(to), 9.4f, Fonts.MEDIUM, labelTextWidth)
        val labelDescription = nvg.getLimitText(getPreviewDescription(to), 7.9f, Fonts.REGULAR, labelTextWidth)
        nvg.drawText(
            labelTitle,
            previewX + previewLabelInset + previewLabelPaddingX,
            previewLabelY + previewLabelPaddingY,
            palette.getFontColor(ColorType.DARK),
            9.4f,
            Fonts.MEDIUM
        )
        nvg.drawText(
            labelDescription,
            previewX + previewLabelInset + previewLabelPaddingX,
            previewLabelY + previewLabelPaddingY + 12f,
            ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 196),
            7.9f,
            Fonts.REGULAR
        )

        pageIndicator
            .setPageCount(types.size)
            .setSelectedIndex(types.indexOf(to).coerceAtLeast(0))
            .setBounds(previewX, controlsY, previewWidth - 60f, controlsHeight)
        pageIndicator.draw(mouseX, mouseY, partialTicks)

        val applied = to == appliedType
        applyButton.setBounds(previewX + previewWidth - 56f, controlsY, 56f, 15f)
        applyButton.setText(if (applied) TranslateText.LAYOUT_APPLIED.getText() else TranslateText.LAYOUT_APPLY.getText())
        val themeBase = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 214)
        val themeHover = ColorUtils.applyAlpha(
            ColorUtils.lighten(palette.getBackgroundColor(ColorType.NORMAL), 0.14f),
            236
        )
        applyButton.setBackgroundColor(
            if (applied) ColorUtils.applyAlpha(accent.getColor1(), 232) else themeBase
        )
        applyButton.setHoverColor(
            if (applied) ColorUtils.applyAlpha(accent.getColor2(), 222) else themeHover
        )
        applyButton.setTextColor(palette.getFontColor(ColorType.DARK))
        applyButton.setEnabled(true)
        applyButton.draw(mouseX, mouseY, partialTicks)
    }

    override fun mouseClickedExtra(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (mouseButton != 0) {
            return
        }

        applyButton.mouseClicked(mouseX, mouseY, mouseButton)
        if (applyButton.isHoveredInteractive(mouseX, mouseY)) {
            return
        }

        if (previousBounds != null && previousBounds!!.contains(mouseX, mouseY)) {
            navigate(-1)
            return
        }
        if (nextBounds != null && nextBounds!!.contains(mouseX, mouseY)) {
            navigate(1)
        }
    }

    /**
     * Keyboard interactions for faster layout navigation.
     *
     * - Left/Right: cycle previews
     * - Enter/Space: apply selected preview
     */
    override fun keyTyped(typedChar: Char, keyCode: Int) {
        when (keyCode) {
            Keyboard.KEY_LEFT -> navigate(-1)
            Keyboard.KEY_RIGHT -> navigate(1)
            Keyboard.KEY_RETURN, Keyboard.KEY_NUMPADENTER, Keyboard.KEY_SPACE -> applyPreviewSelection()
        }
    }

    /**
     * Applies current preview selection as active type.
     */
    private fun applyPreviewSelection() {
        val target = transitionTo ?: previewType
        if (target == null) {
            return
        }
        selectType(target)
        previewType = target
        transitionFrom = null
        transitionTo = null
        transitionStartNs = 0L
    }

    /**
     * Moves carousel preview without applying immediately.
     */
    private fun navigate(direction: Int) {
        val types = getTypes()
        if (types.size <= 1) {
            return
        }

        val current = transitionTo ?: previewType ?: getSelectedType() ?: types[0]
        val currentIndex = types.indexOf(current).coerceAtLeast(0)
        val targetIndex = ((currentIndex + direction) % types.size + types.size) % types.size
        val target = types[targetIndex]
        if (target == current) {
            return
        }

        transitionFrom = current
        transitionTo = target
        previewType = target
        transitionDirection = if (direction > 0) 1 else -1
        transitionStartNs = System.nanoTime()
    }

    /**
     * Draws one navigation button.
     */
    private fun drawNavButton(
        nvg: NanoVGManager,
        palette: ColorPalette,
        bounds: Rect?,
        symbol: String,
        hovered: Boolean
    ) {
        if (bounds == null) {
            return
        }

        nvg.drawRoundedRect(
            bounds.x,
            bounds.y,
            bounds.width,
            bounds.height,
            8f,
            ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), if (hovered) 206 else 176)
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

    /**
     * Returns transition progress in [0..1].
     */
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

    /**
     * Scene-specific preview renderer for one type.
     */
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

    /**
     * Returns title rendered in preview footer for the current type.
     */
    protected open fun getPreviewTitle(type: UILayoutType): String {
        return type.getTitle()
    }

    /**
     * Returns description rendered in preview footer for the current type.
     */
    protected open fun getPreviewDescription(type: UILayoutType): String {
        return type.getDescription()
    }

    /**
     * Immutable rectangle for nav hit-testing.
     */
    protected data class Rect(val x: Float, val y: Float, val width: Float, val height: Float) {
        fun contains(mouseX: Int, mouseY: Int): Boolean {
            return MouseUtils.isInside(mouseX, mouseY, x, y, width, height)
        }
    }

    companion object {
        private const val TRANSITION_DURATION_NS = 230_000_000L
    }
}

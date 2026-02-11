package me.miki.shindo.gui.modmenu.category.impl.setting.impl.layout

import me.miki.shindo.Shindo
import me.miki.shindo.gui.modmenu.category.impl.SettingsCategory
import me.miki.shindo.gui.modmenu.category.impl.setting.SettingScene
import me.miki.shindo.management.color.AccentColor
import me.miki.shindo.management.color.palette.ColorPalette
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.ui.layout.enums.UILayoutArea
import me.miki.shindo.ui.layout.UILayoutManager
import me.miki.shindo.ui.layout.enums.UILayoutType
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.ui.comp.selectors.CompVisualPresetSelector
import me.miki.shindo.utils.ColorUtils
import kotlin.math.max
import kotlin.math.min

abstract class LayoutAreaScene(
        parent: SettingsCategory,
        val area: UILayoutArea,
        nameTranslate: TranslateText,
        descriptionTranslate: TranslateText,
        icon: String
) : SettingScene(parent, nameTranslate, descriptionTranslate, icon) {

    private val layoutManager: UILayoutManager = Shindo.getInstance().uiLayoutManager
    private var selectedType: UILayoutType? = null
    private var hasTypeSelector: Boolean = false

    private lateinit var typeSelector: CompVisualPresetSelector

    override fun initGui() {
        selectedType = layoutManager.getSelectedType(area)
        hasTypeSelector = showTypeSelector()
        if (hasTypeSelector) {
            buildTypeSelector()
        }
        initExtraControls()
    }

    private fun buildTypeSelector() {
        val types = getTypes()
        val entries = ArrayList<CompVisualPresetSelector.Entry>(types.size)
        for (type in types) {
            entries.add(createTypeEntry(type))
        }
        val selectedIdx = types.indexOf(selectedType ?: layoutManager.getSelectedType(area)).coerceAtLeast(0)
        typeSelector = CompVisualPresetSelector()
                .setEntries(entries)
                .setSelectedIndex(selectedIdx)
                .setOnSelect { index ->
                    if (index >= 0 && index < types.size) {
                        selectType(types[index])
                    }
                }
                .setPreviewRenderer { index, _, x, y, width, height, selected, hovered, nvg, palette, accent ->
                    if (index in 0 until types.size) {
                        drawTypeCardPreview(
                                nvg,
                                palette,
                                accent,
                                types[index],
                                x,
                                y,
                                width,
                                height,
                                selected,
                                hovered
                        )
                    }
                }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val nvg = Shindo.getInstance().nanoVGManager ?: return
        val colorManager = Shindo.getInstance().colorManager
        val palette = colorManager.getPalette()
        val accent = colorManager.getCurrentColor()

        val baseX = x.toFloat()
        val baseY = contentY.toFloat()
        val baseWidth = width.toFloat()
        val baseHeight = contentHeight.toFloat()
        if (baseWidth <= 0f || baseHeight <= 0f) {
            return
        }

        val panelX = baseX + PANEL_PADDING
        val panelY = baseY + PANEL_PADDING
        val panelWidth = baseWidth - (PANEL_PADDING * 2f)
        val panelHeight = baseHeight - (PANEL_PADDING * 2f)

        drawContainer(nvg, palette, panelX, panelY, panelWidth, panelHeight)

        if (!syncSelections()) {
            return
        }

        drawPreviewPanel(
                nvg,
                palette,
                accent,
                panelX,
                panelY,
                panelWidth,
                panelHeight,
                mouseX,
                mouseY,
                partialTicks
        )
    }

    private fun syncSelections(): Boolean {
        val types = getTypes()
        if (types.isNotEmpty()) {
            val managerSelected = layoutManager.getSelectedType(area)
            if (managerSelected != selectedType) {
                selectedType = managerSelected
                if (hasTypeSelector) {
                    val idx = types.indexOf(managerSelected).coerceAtLeast(0)
                    typeSelector.setSelectedIndex(idx)
                }
            }
        }
        syncExtraSelections()
        return true
    }

    private fun drawContainer(
            nvg: NanoVGManager,
            palette: ColorPalette,
            x: Float,
            y: Float,
            width: Float,
            height: Float
    ) {
        nvg.drawShadow(x, y, width, height, CARD_RADIUS, 7)
        nvg.drawRoundedRect(
                x,
                y,
                width,
                height,
                CARD_RADIUS,
                ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 210)
        )
        nvg.drawRoundedRect(
                x + 1f,
                y + 1f,
                width - 2f,
                height - 2f,
                CARD_RADIUS - 1f,
                ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 230)
        )
    }

    private fun drawPreviewPanel(
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
        val previewX = x + 16f
        val previewY = y + 16f
        val previewWidth = width - 32f
        val extraHeight = getExtraControlsHeight()
        var selectorY = previewY

        if (extraHeight > 0f) {
            drawExtraControls(
                    nvg,
                    palette,
                    accent,
                    previewX,
                    previewY,
                    previewWidth,
                    extraHeight,
                    mouseX,
                    mouseY,
                    partialTicks
            )
            selectorY += extraHeight + CONTROL_GAP
        }

        nvg.save()
        nvg.intersectScissor(x, y, width, height)

        if (!hasTypeSelector) {
            val previewHeight = max(24f, (y + height) - selectorY - 12f)
            drawPreview(
                    nvg,
                    palette,
                    accent,
                    previewX,
                    selectorY,
                    previewWidth,
                    previewHeight,
                    mouseX,
                    mouseY,
                    partialTicks
            )
        } else if (renderLegacyPreview()) {
            var availableSelectorHeight = max(72f, (y + height) - selectorY - 12f)
            val previewHeight = min(previewMaxHeight, max(0f, availableSelectorHeight * previewHeightRatio))
            drawPreview(
                    nvg,
                    palette,
                    accent,
                    previewX,
                    previewY,
                    previewWidth,
                    previewHeight,
                    mouseX,
                    mouseY,
                    partialTicks
            )
            selectorY += previewHeight + PREVIEW_SELECTOR_GAP
            availableSelectorHeight = max(72f, (y + height) - selectorY - 12f)
            typeSelector.setBounds(previewX, selectorY, previewWidth, availableSelectorHeight)
        } else {
            val availableSelectorHeight = max(72f, (y + height) - selectorY - 12f)
            typeSelector.setBounds(previewX, selectorY, previewWidth, availableSelectorHeight)
        }

        nvg.restore()

        if (hasTypeSelector) {
            typeSelector.draw(mouseX, mouseY, partialTicks)
        }
    }

    protected abstract fun drawPreview(
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
    )

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (mouseButton != 0) {
            return
        }

        mouseClickedExtra(mouseX, mouseY, mouseButton)
        if (hasTypeSelector) {
            typeSelector.mouseClicked(mouseX, mouseY, mouseButton)
        }
    }

    protected fun getTypes(): List<UILayoutType> = layoutManager.getTypes(area)

    protected fun getSelectedType(): UILayoutType? = selectedType

    protected fun selectType(type: UILayoutType?) {
        if (type == null) return
        layoutManager.selectType(type)
        selectedType = type

        if (hasTypeSelector) {
            val types = getTypes()
            val idx = types.indexOf(type)
            if (idx >= 0) {
                typeSelector.setSelectedIndex(idx)
            }
        }
    }

    protected open fun initExtraControls() {}

    protected open fun syncExtraSelections() {}

    protected open fun getExtraControlsHeight(): Float = 0f

    protected open fun drawExtraControls(
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
    }

    protected open fun mouseClickedExtra(mouseX: Int, mouseY: Int, mouseButton: Int) {}
    protected open fun showTypeSelector(): Boolean = true

    protected open fun createTypeEntry(type: UILayoutType): CompVisualPresetSelector.Entry {
        return CompVisualPresetSelector.Entry(type.getTitle(), type.getDescription())
    }

    protected open fun drawTypeCardPreview(
            nvg: NanoVGManager,
            palette: ColorPalette,
            accent: AccentColor,
            type: UILayoutType,
            x: Float,
            y: Float,
            width: Float,
            height: Float,
            selected: Boolean,
            hovered: Boolean
    ) {
        val bg = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), if (selected) 190 else 160)
        val line = ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), if (hovered || selected) 205 else 165)
        val badge = ColorUtils.applyAlpha(accent.getColor1(), if (selected) 190 else 140)
        nvg.drawRoundedRect(x, y, width, height, 4f, bg)
        nvg.drawRoundedRect(x + 4f, y + 4f, max(8f, width * 0.28f), max(8f, height - 8f), 3f, badge)
        nvg.drawRoundedRect(x + width * 0.34f, y + 5f, max(8f, width * 0.54f), 4f, 2f, line)
        nvg.drawRoundedRect(x + width * 0.34f, y + 12f, max(8f, width * 0.44f), 3.5f, 2f, ColorUtils.applyAlpha(line, 180))
    }

    companion object {
        private const val PANEL_PADDING = 16f
        private const val CARD_RADIUS = 12f
        private const val CONTROL_GAP = 8f
        private const val PREVIEW_SELECTOR_GAP = 10f

        const val PREVIEW_RADIUS = 12f

    }

    protected open val previewMaxHeight: Float = 170f
    protected open val previewHeightRatio: Float = 0.55f

    protected open fun renderLegacyPreview(): Boolean = false
}

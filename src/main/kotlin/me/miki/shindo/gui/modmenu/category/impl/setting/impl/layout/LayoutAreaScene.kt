package me.miki.shindo.gui.modmenu.category.impl.setting.impl.layout

import me.miki.shindo.Shindo
import me.miki.shindo.gui.modmenu.category.impl.SettingsCategory
import me.miki.shindo.gui.modmenu.category.impl.setting.SettingScene
import me.miki.shindo.management.color.AccentColor
import me.miki.shindo.management.color.palette.ColorPalette
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.layout.UILayoutManager
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.settings.config.ConfigOwner
import me.miki.shindo.management.settings.impl.ComboSetting
import me.miki.shindo.management.settings.impl.combo.Option
import me.miki.shindo.ui.comp.impl.CompDropdown
import me.miki.shindo.utils.ColorUtils
import java.awt.Color
import kotlin.math.max
import kotlin.math.min

abstract class LayoutAreaScene(
    parent: SettingsCategory,
    val area: UILayoutManager.Layouts,
    nameTranslate: TranslateText,
    descriptionTranslate: TranslateText,
    icon: String
) : SettingScene(parent, nameTranslate, descriptionTranslate, icon) {

    private val layoutManager: UILayoutManager = Shindo.getInstance().uiLayoutManager
    private var selectedType: UILayoutManager.LayoutType? = null

    private lateinit var typeSetting: ComboSetting
    private lateinit var typeDropdown: CompDropdown

    private val typeOptions = ArrayList<Option>()

    override fun initGui() {
        selectedType = layoutManager.getSelectedType(area)
        buildTypeSetting()
    }

    private fun buildTypeSetting() {
        typeOptions.clear()
        val types = layoutManager.getTypes(area)
        for (type in types) {
            typeOptions.add(Option(type.title))
        }
        var defaultKey = if (typeOptions.isNotEmpty()) typeOptions[0].nameKey else "none"
        var selectedIdx = types.indexOf(selectedType ?: layoutManager.getSelectedType(area))
        if (selectedIdx < 0 && types.isNotEmpty()) {
            selectedIdx = 0
        }
        if (selectedIdx >= 0 && selectedIdx < typeOptions.size) {
            defaultKey = typeOptions[selectedIdx].nameKey
        }
        typeSetting = ComboSetting("layout-type", dummyOwner(), defaultKey, typeOptions)
        typeDropdown = CompDropdown(0f, 0f, 0f, typeSetting)
        typeDropdown.setOpenUp(true)
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val nvg = Shindo.getInstance().nanoVGManager ?: return
        val colorManager = Shindo.getInstance().colorManager
        val palette = colorManager.palette
        val accent = colorManager.currentColor

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
        val types = layoutManager.getTypes(area)
        if (types.isEmpty()) {
            return true
        }
        val typeIdx = typeSetting.getOptions().indexOf(typeSetting.getOption())
        if (typeIdx >= 0 && typeIdx < types.size) {
            val newType = types[typeIdx]
            if (newType != selectedType) {
                selectedType = newType
                layoutManager.selectType(newType)
            }
        }
        return true
    }

    private fun drawContainer(nvg: NanoVGManager, palette: ColorPalette, x: Float, y: Float, width: Float, height: Float) {
        nvg.drawShadow(x, y, width, height, CARD_RADIUS, 7)
        nvg.drawRoundedRect(x, y, width, height, CARD_RADIUS, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 210))
        nvg.drawRoundedRect(x + 1f, y + 1f, width - 2f, height - 2f, CARD_RADIUS - 1f, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 230))
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
        val previewHeight = max(0f, (y + height) - previewY - typeDropdown.controlHeight - 12f)

        nvg.save()
        nvg.intersectScissor(x, y, width, height)

        val clippedHeight = min(previewHeight, previewMaxHeight)
        drawPreview(nvg, palette, accent, previewX, previewY, previewWidth, clippedHeight)

        nvg.restore()

        val dropdownWidth = min(240f, previewWidth)
        val dropdownX = previewX
        val dropdownY = y + height - typeDropdown.controlHeight - 12f
        typeDropdown.setX(dropdownX)
        typeDropdown.setY(dropdownY)
        typeDropdown.setWidth(dropdownWidth)
        typeDropdown.draw(mouseX, mouseY, partialTicks)
    }

    protected abstract fun drawPreview(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accent: AccentColor,
        x: Float,
        y: Float,
        width: Float,
        height: Float
    )

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (mouseButton != 0) {
            return
        }

        typeDropdown.mouseClicked(mouseX, mouseY, mouseButton)
    }

    private fun dummyOwner(): ConfigOwner {
        return object : ConfigOwner {
            override fun getConfigId(): String = "layout-scene-temp"
        }
    }

    companion object {
        private const val PANEL_PADDING = 16f
        private const val CARD_RADIUS = 12f

        const val PREVIEW_RADIUS = 12f

    }

    protected open val previewMaxHeight: Float = 170f
}

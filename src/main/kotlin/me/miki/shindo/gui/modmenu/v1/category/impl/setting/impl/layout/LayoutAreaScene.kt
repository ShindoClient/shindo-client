package me.miki.shindo.gui.modmenu.v1.category.impl.setting.impl.layout

import me.miki.shindo.Shindo
import me.miki.shindo.gui.modmenu.v1.category.impl.SettingsCategory
import me.miki.shindo.gui.modmenu.v1.category.impl.setting.SettingScene
import me.miki.shindo.gui.modmenu.v1.render.ModMenuClipCoordinator
import me.miki.shindo.management.color.AccentColor
import me.miki.shindo.management.color.palette.ColorPalette
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.ui.components.v1.selectors.CompVisualPresetSelector
import me.miki.shindo.ui.layout.UILayoutManager
import me.miki.shindo.ui.layout.enums.UILayoutArea
import me.miki.shindo.ui.layout.enums.UILayoutType
import me.miki.shindo.utils.ColorUtils
import kotlin.math.max
import kotlin.math.min

/**
 * Base class for layout area scenes.
 *
 * Responsibilities:
 * - synchronize selected [UILayoutType] with [UILayoutManager];
 * - provide a consistent panel and preview canvas;
 * - optionally render a type selector grid;
 * - expose hooks for area-specific controls and previews.
 */
abstract class LayoutAreaScene(
    parent: SettingsCategory,
    val area: UILayoutArea,
    nameTranslate: TranslateText,
    descriptionTranslate: TranslateText,
    icon: String
) : SettingScene(parent, nameTranslate, descriptionTranslate, icon) {

    private val layoutManager: UILayoutManager = Shindo.getInstance().uiLayoutManager
    private var selectedType: UILayoutType? = null
    private var typeSelector: CompVisualPresetSelector? = null

    /**
     * Simple immutable preview bounds shared with specialized scenes.
     */
    protected data class PreviewRect(val x: Float, val y: Float, val width: Float, val height: Float)

    protected var lastPreviewRect: PreviewRect? = null

    override fun initGui() {
        selectedType = layoutManager.getSelectedType(area)
        buildTypeSelectorIfNeeded()
        initExtraControls()
    }

    /**
     * Rebuilds selector entries when the area uses card-based type selection.
     */
    private fun buildTypeSelectorIfNeeded() {
        if (!showTypeSelector()) {
            typeSelector = null
            return
        }

        val types = getTypes()
        if (types.isEmpty()) {
            typeSelector = null
            return
        }

        val entries = ArrayList<CompVisualPresetSelector.Entry>(types.size)
        var i = 0
        while (i < types.size) {
            entries.add(createTypeEntry(types[i]))
            i++
        }

        val selectedIndex = types.indexOf(selectedType).coerceAtLeast(0)
        typeSelector = CompVisualPresetSelector()
            .setEntries(entries)
            .setSelectedIndex(selectedIndex)
            .setOnSelect { index ->
                if (index >= 0 && index < types.size) {
                    selectType(types[index])
                }
            }
            .setPreviewRenderer { index, _, x, y, width, height, selected, hovered, nvg, palette, accent ->
                if (index >= 0 && index < types.size) {
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
        val instance = Shindo.getInstance()
        val nvg = instance.nanoVGManager ?: return
        val palette = instance.colorManager.getPalette()
        val accent = instance.colorManager.getCurrentColor()

        val baseX = x.toFloat()
        val baseY = contentY.toFloat()
        val baseWidth = width.toFloat()
        val baseHeight = contentHeight.toFloat()

        if (baseWidth <= 0f || baseHeight <= 0f) {
            return
        }

        val panelX = baseX + LayoutSceneStyle.PANEL_PADDING
        val panelY = baseY + LayoutSceneStyle.PANEL_PADDING
        val panelWidth = baseWidth - LayoutSceneStyle.PANEL_PADDING * 2f
        val panelHeight = baseHeight - LayoutSceneStyle.PANEL_PADDING * 2f

        if (panelWidth <= 0f || panelHeight <= 0f) {
            return
        }

        LayoutSceneRenderer.drawScenePanel(nvg, palette, panelX, panelY, panelWidth, panelHeight)

        syncSelectionFromManager()
        syncExtraSelections()

        val contentX = panelX + LayoutSceneStyle.CONTENT_PADDING
        val contentY = panelY + LayoutSceneStyle.CONTENT_PADDING
        val contentWidth = panelWidth - LayoutSceneStyle.CONTENT_PADDING * 2f
        val contentBottom = panelY + panelHeight - LayoutSceneStyle.CONTENT_PADDING
        if (contentWidth <= 0f || contentBottom <= contentY) {
            return
        }

        var cursorY = contentY
        val extraHeight = max(0f, getExtraControlsHeight())
        if (extraHeight > 0f) {
            drawExtraControls(
                nvg,
                palette,
                accent,
                contentX,
                cursorY,
                contentWidth,
                extraHeight,
                mouseX,
                mouseY,
                partialTicks
            )
            cursorY += extraHeight + LayoutSceneStyle.CONTROL_GAP
        }

        val selector = typeSelector
        if (selector == null) {
            val previewHeight = max(26f, contentBottom - cursorY)
            lastPreviewRect = PreviewRect(contentX, cursorY, contentWidth, previewHeight)
            ModMenuClipCoordinator.withClip(
                nvg = nvg,
                x = panelX,
                y = panelY,
                width = panelWidth,
                height = panelHeight,
                intersect = true
            ) {
                drawPreview(
                    nvg,
                    palette,
                    accent,
                    contentX,
                    cursorY,
                    contentWidth,
                    previewHeight,
                    mouseX,
                    mouseY,
                    partialTicks
                )
            }
            return
        }

        val availableHeight = max(90f, contentBottom - cursorY)
        val previewHeight = min(previewMaxHeight, max(36f, availableHeight * previewHeightRatio))
        val selectorY = cursorY + previewHeight + LayoutSceneStyle.CONTROL_GAP
        val selectorHeight = max(72f, contentBottom - selectorY)

        lastPreviewRect = PreviewRect(contentX, cursorY, contentWidth, previewHeight)

        ModMenuClipCoordinator.withClip(
            nvg = nvg,
            x = panelX,
            y = panelY,
            width = panelWidth,
            height = panelHeight,
            intersect = true
        ) {
            drawPreview(
                nvg,
                palette,
                accent,
                contentX,
                cursorY,
                contentWidth,
                previewHeight,
                mouseX,
                mouseY,
                partialTicks
            )
        }

        selector.setBounds(contentX, selectorY, contentWidth, selectorHeight)
        selector.draw(mouseX, mouseY, partialTicks)
    }

    /**
     * Updates local selection cache from manager state.
     */
    private fun syncSelectionFromManager() {
        val managerSelected = layoutManager.getSelectedType(area)
        if (managerSelected == selectedType) {
            return
        }
        selectedType = managerSelected

        val selector = typeSelector ?: return
        val idx = getTypes().indexOf(managerSelected)
        if (idx >= 0) {
            selector.setSelectedIndex(idx)
        }
    }

    /**
     * Draws the scene-specific preview canvas.
     */
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
        typeSelector?.mouseClicked(mouseX, mouseY, mouseButton)
    }

    /**
     * Returns all layout types available for this area.
     */
    protected fun getTypes(): List<UILayoutType> {
        return layoutManager.getTypes(area)
    }

    /**
     * Returns currently selected type for this area.
     */
    protected fun getSelectedType(): UILayoutType? {
        return selectedType
    }

    /**
     * Applies a new type through manager and synchronizes selector.
     */
    protected fun selectType(type: UILayoutType?) {
        if (type == null) {
            return
        }
        layoutManager.selectType(type)
        selectedType = type

        val selector = typeSelector ?: return
        val idx = getTypes().indexOf(type)
        if (idx >= 0) {
            selector.setSelectedIndex(idx)
        }
    }

    /**
     * Hook for additional controls initialization.
     */
    protected open fun initExtraControls() {
    }

    /**
     * Hook called every frame before drawing scene content.
     */
    protected open fun syncExtraSelections() {
    }

    /**
     * Returns extra controls block height.
     */
    protected open fun getExtraControlsHeight(): Float {
        return 0f
    }

    /**
     * Draws custom controls above preview/selector.
     */
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

    /**
     * Handles mouse click for scene-specific controls.
     */
    protected open fun mouseClickedExtra(mouseX: Int, mouseY: Int, mouseButton: Int) {
    }

    /**
     * Enables selector mode for scenes that use card grids instead of carousel.
     */
    protected open fun showTypeSelector(): Boolean {
        return true
    }

    /**
     * Creates one selector entry for the provided type.
     */
    protected open fun createTypeEntry(type: UILayoutType): CompVisualPresetSelector.Entry {
        return CompVisualPresetSelector.Entry(type.getTitle(), type.getDescription())
    }

    /**
     * Draws a selector card preview.
     */
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
        LayoutSceneRenderer.drawPreviewSurface(nvg, palette, x, y, width, height, 4f)
        val line = ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), if (selected || hovered) 214 else 176)
        nvg.drawRoundedRect(x + 5f, y + 5f, max(10f, width * 0.52f), 3f, 1.5f, line)
        nvg.drawRoundedRect(x + 5f, y + 10f, max(10f, width * 0.36f), 2.6f, 1.3f, ColorUtils.applyAlpha(line, 180))
    }

    /**
     * Max preview block height used when selector is visible.
     */
    protected open val previewMaxHeight: Float = 196f

    /**
     * Fraction of content area reserved for preview when selector is visible.
     */
    protected open val previewHeightRatio: Float = 0.58f
}

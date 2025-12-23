package me.miki.shindo.gui.modmenu.category.impl.shared

import me.miki.shindo.Shindo
import me.miki.shindo.management.color.AccentColor
import me.miki.shindo.management.color.palette.ColorPalette
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Font
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.settings.Setting
import me.miki.shindo.management.settings.impl.CategorySetting
import me.miki.shindo.management.settings.metadata.SettingMetadata
import me.miki.shindo.ui.comp.Comp
import me.miki.shindo.ui.comp.factory.SettingComponentFactory
import me.miki.shindo.ui.comp.impl.CompCellGrid
import me.miki.shindo.ui.comp.impl.CompColorPicker
import me.miki.shindo.ui.comp.impl.CompComboBox
import me.miki.shindo.ui.comp.impl.CompImageSelect
import me.miki.shindo.ui.comp.impl.CompKeybind
import me.miki.shindo.ui.comp.impl.CompModTextBox
import me.miki.shindo.ui.comp.impl.CompSlider
import me.miki.shindo.ui.comp.impl.CompSoundSelect
import me.miki.shindo.ui.comp.impl.CompToggleButton
import me.miki.shindo.ui.comp.impl.CompCategory
import me.miki.shindo.utils.ColorUtils
import me.miki.shindo.utils.animation.simple.SimpleAnimation
import me.miki.shindo.utils.mouse.MouseUtils
import me.miki.shindo.utils.mouse.Scroll
import java.awt.Color
import java.util.ArrayList
import java.util.HashMap
import kotlin.math.max
import kotlin.math.min

class SettingsPanel {

    enum class LayoutMode {
        SINGLE_COLUMN,
        DOUBLE_COLUMN
    }

    private val entries = ArrayList<Entry>()
    private val categoryLayouts = ArrayList<CategoryLayout>()
    private val entryStates = HashMap<Setting, EntryState>()

    var layoutMode: LayoutMode = LayoutMode.SINGLE_COLUMN
        private set

    fun clear() {
        entries.clear()
        categoryLayouts.clear()
        entryStates.clear()
    }

    fun setLayoutMode(layoutMode: LayoutMode?) {
        this.layoutMode = layoutMode ?: LayoutMode.SINGLE_COLUMN
    }

    fun buildEntries(settings: List<Setting>) {
        entries.clear()
        var currentCategory: CategorySetting? = null
        for (setting in settings) {
            if (setting is CategorySetting) {
                currentCategory = setting
                val compCategory = CompCategory(0F, currentCategory)
                compCategory.setHeight(CATEGORY_HEADER_HEIGHT)
                entries.add(Entry(setting, compCategory, currentCategory))
                getState(setting)
                continue
            }

            val component = createComponent(setting) ?: continue
            entries.add(Entry(setting, component, currentCategory))
            getState(setting)
        }
    }

    fun draw(
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float,
        contentX: Float,
        contentY: Float,
        contentWidth: Float,
        viewportHeight: Float,
        nvg: NanoVGManager,
        palette: ColorPalette,
        scroll: Scroll
    ) {
        updateLayout(contentX, contentY, contentWidth, scroll.getValue())

        var tooltip: TooltipData? = null
        var contentBottom = contentY + scroll.getValue()
        val accentColor = Shindo.getInstance().colorManager.currentColor

        for (layout in categoryLayouts) {
            contentBottom = max(contentBottom, layout.getBottom())

            val header = layout.header
            if (header != null) {
                header.setWidth(layout.headerWidth)
                header.setX(layout.headerX)
                header.setY(layout.headerY)
                header.draw(mouseX, mouseY, partialTicks)
            }

            if (layout.isCollapsed()) {
                continue
            }

            drawCategoryCard(nvg, palette, layout)

            val positionedEntries = layout.entries
            for (i in positionedEntries.indices) {
                val positioned = positionedEntries[i]
                val entry = positioned.entry
                val state = getState(entry.setting)

                val hovered = MouseUtils.isInside(mouseX, mouseY, positioned.x, positioned.y, positioned.width, positioned.height)
                state.hoverAnimation.setAnimation(if (hovered) 1f else 0f, 18.0)
                val hoverProgress = state.hoverAnimation.value

                layoutComponent(entry.comp, positioned)

                val indicatorHeight = max(14f, positioned.height - 6f)
                val indicatorY = positioned.y + (positioned.height - indicatorHeight) / 2f
                val indicatorColor = ColorUtils.applyAlpha(accentColor.color1, (hoverProgress * 120).toInt())
                nvg.drawRoundedRect(positioned.x, indicatorY, INDICATOR_WIDTH, indicatorHeight, 2f, indicatorColor)

                val textX = positioned.x + INDICATOR_WIDTH + 7f
                val textY = positioned.y + 6f
                val textWidth = resolveTextWidth(entry, positioned, textX)

                val rowTooltip = drawLabels(nvg, palette, entry, textX, textY, textWidth, hoverProgress, mouseX, mouseY)
                if (tooltip == null && rowTooltip != null) {
                    tooltip = rowTooltip
                }

                entry.comp.draw(mouseX, mouseY, partialTicks)

                if (i < positionedEntries.size - 1) {
                    val dividerAlpha = 24f + (hoverProgress * 30f)
                    nvg.drawDivider(
                        positioned.x + INDICATOR_WIDTH + 4f,
                        positioned.y + positioned.height - 2f,
                        positioned.width - INDICATOR_WIDTH - 8f,
                        1.5f,
                        1f,
                        dividerAlpha
                    )
                }
            }
        }

        if (categoryLayouts.isNotEmpty()) {
            val contentHeight = contentBottom - (contentY + scroll.getValue())
            scroll.maxScroll = max(0f, contentHeight - viewportHeight)
        } else {
            scroll.maxScroll = 0f
        }

        if (tooltip != null) {
            drawTooltip(nvg, palette, tooltip, mouseX, mouseY, contentX, contentY, contentWidth, viewportHeight)
        }
    }

    fun mouseClicked(
        mouseX: Int,
        mouseY: Int,
        mouseButton: Int,
        contentX: Float,
        contentY: Float,
        contentWidth: Float,
        viewportHeight: Float,
        scroll: Scroll
    ): Boolean {
        updateLayout(contentX, contentY, contentWidth, scroll.getValue())

        for (layout in categoryLayouts) {
            if (layout.header != null && MouseUtils.isInside(mouseX, mouseY, layout.headerX, layout.headerY, layout.headerWidth, layout.headerHeight)) {
                layout.header?.mouseClicked(mouseX, mouseY, mouseButton)
                return true
            }

            if (layout.isCollapsed()) {
                continue
            }

            for (positioned in layout.entries) {
                if (positioned.height <= 0f) {
                    continue
                }
                if (positioned.y + positioned.height < contentY || positioned.y > contentY + viewportHeight) {
                    continue
                }
                if (!MouseUtils.isInside(mouseX, mouseY, positioned.x, positioned.y, positioned.width, positioned.height)) {
                    continue
                }
                positioned.entry.comp.mouseClicked(mouseX, mouseY, mouseButton)
                return true
            }
        }
        return false
    }

    fun mouseReleased(mouseX: Int, mouseY: Int, mouseButton: Int, scroll: Scroll) {
        for (entry in entries) {
            entry.comp.mouseReleased(mouseX, mouseY, mouseButton)
        }
    }

    fun keyTyped(typedChar: Char, keyCode: Int) {
        for (entry in entries) {
            entry.comp.keyTyped(typedChar, keyCode)
        }
    }

    fun resetSettings() {
        for (entry in entries) {
            if (entry.setting !is CategorySetting) {
                entry.setting.reset()
            }
        }
    }

    private fun updateLayout(contentX: Float, contentY: Float, contentWidth: Float, scrollOffset: Float) {
        categoryLayouts.clear()

        val innerX = contentX + OUTER_MARGIN
        val innerWidth = max(0f, contentWidth - (OUTER_MARGIN * 2f))
        var yCursor = contentY + scrollOffset

        val singleWidth = innerWidth
        val doubleWidth = (innerWidth - COLUMN_GAP) / 2f

        val blocks = buildBlocks()
        val pendingRow = ArrayList<CategoryBlock>(2)

        for (block in blocks) {
            if (layoutMode == LayoutMode.DOUBLE_COLUMN && block.spansFullWidth()) {
                if (pendingRow.isNotEmpty()) {
                    yCursor = placeRow(innerX, yCursor, singleWidth, doubleWidth, pendingRow)
                    pendingRow.clear()
                }
                val layout = layoutCategory(block, innerX, yCursor, singleWidth)
                categoryLayouts.add(layout)
                yCursor = layout.getBottom() + CATEGORY_GAP
                continue
            }

            pendingRow.add(block)
            if (layoutMode == LayoutMode.SINGLE_COLUMN || pendingRow.size == 2) {
                yCursor = placeRow(innerX, yCursor, singleWidth, doubleWidth, pendingRow)
                pendingRow.clear()
            }
        }

        if (pendingRow.isNotEmpty()) {
            yCursor = placeRow(innerX, yCursor, singleWidth, doubleWidth, pendingRow)
        }
    }

    private fun placeRow(innerX: Float, yCursor: Float, singleWidth: Float, doubleWidth: Float, row: List<CategoryBlock>): Float {
        var rowHeight = 0f
        for (i in row.indices) {
            val width = if (layoutMode == LayoutMode.DOUBLE_COLUMN) doubleWidth else singleWidth
            var x = innerX
            if (layoutMode == LayoutMode.DOUBLE_COLUMN && i == 1) {
                x += doubleWidth + COLUMN_GAP
            }

            val layout = layoutCategory(row[i], x, yCursor, width)
            categoryLayouts.add(layout)
            rowHeight = max(rowHeight, layout.getTotalHeight())
        }
        return yCursor + rowHeight + CATEGORY_GAP
    }

    private fun layoutCategory(block: CategoryBlock, x: Float, y: Float, width: Float): CategoryLayout {
        val layout = CategoryLayout(block)

        val headerHeight = if (block.hasHeader()) CATEGORY_HEADER_HEIGHT else 0f
        val headerSpacing = if (block.hasHeader()) CATEGORY_HEADER_SPACING else 0f
        val cardX = x
        val cardY = y + headerHeight + headerSpacing
        val cardWidth = width
        val contentX = cardX + CARD_PADDING_X
        val contentY = cardY + CARD_PADDING_Y
        val contentWidth = max(0f, cardWidth - (CARD_PADDING_X * 2f))

        val positionedEntries = ArrayList<PositionedEntry>()

        if (block.isCollapsed()) {
            for (entry in block.settings) {
                val state = getState(entry.setting)
                val targetHeight = calculateTargetHeight(entry.comp)
                if (!state.initialized) {
                    state.heightAnimation.value = targetHeight
                    state.initialized = true
                }
                state.heightAnimation.setAnimation(0f, 18.0 )
            }
            layout.setCard(cardX, cardY, cardWidth, 0f)
            layout.setHeader(block.header, x, y, width, headerHeight)
            layout.entries = positionedEntries
            return layout
        }

        var rowCursor = contentY
        for (entry in block.settings) {
            val state = getState(entry.setting)
            val targetHeight = calculateTargetHeight(entry.comp)
            if (!state.initialized) {
                state.heightAnimation.value = targetHeight
                state.initialized = true
            }

            state.heightAnimation.setAnimation(targetHeight, 20.0)
            val rowHeight = max(MIN_ROW_HEIGHT, state.heightAnimation.value)

            val positionedEntry = PositionedEntry(entry, contentX, rowCursor, contentWidth, rowHeight)
            positionedEntries.add(positionedEntry)
            rowCursor += rowHeight + ROW_GAP
        }

        if (positionedEntries.isNotEmpty()) {
            rowCursor -= ROW_GAP
        }

        val contentHeight = max(0f, rowCursor - contentY)
        val cardHeight = max(MIN_CARD_HEIGHT, contentHeight + (CARD_PADDING_Y * 2f))

        layout.setCard(cardX, cardY, cardWidth, cardHeight)
        layout.setHeader(block.header, x, y, width, headerHeight)
        layout.entries = positionedEntries

        return layout
    }

    private fun buildBlocks(): List<CategoryBlock> {
        val blocks = ArrayList<CategoryBlock>()
        var current = CategoryBlock(null, null)

        for (entry in entries) {
            if (entry.setting is CategorySetting) {
                if (current.hasContent()) {
                    blocks.add(current)
                }
                val category = entry.setting as CategorySetting
                current = CategoryBlock(category, entry.comp as CompCategory)
                continue
            }
            current.settings.add(entry)
        }

        if (current.hasContent()) {
            blocks.add(current)
        }

        return blocks
    }

    private fun calculateTargetHeight(comp: Comp): Float {
        var base = 38f

        if (comp is CompSlider) {
            base = 50f
        }

        if (comp is CompComboBox) {
            base = 50f
        }
        if (comp is CompColorPicker) {
            val picker = comp
            if (picker.isOpen()) {
                val scale = if (picker.getScale() <= 0f) 1.0f else picker.getScale()
                val openHeight = (26f + (if (picker.isShowAlpha()) 118f else 100f)) * scale
                base = max(base, COMPONENT_VERTICAL_PADDING + openHeight + 14f)
            } else {
                val scale = if (picker.getScale() <= 0f) 1.0f else picker.getScale()
                val closedHeight = (30f * scale) + COMPONENT_VERTICAL_PADDING
                base = max(base, closedHeight)
            }
        }
        if (comp is CompCellGrid) {
            base = max(base, 340f)
        }
        return base
    }

    private fun createComponent(setting: Setting): Comp? {
        return SettingComponentFactory.create(setting)
    }

    private fun layoutComponent(comp: Comp, positioned: PositionedEntry) {
        val x = positioned.x
        val y = positioned.y
        val width = positioned.width
        val height = positioned.height
        val right = x + width

        val componentPadding = COMPONENT_VERTICAL_PADDING
        val componentY = y + componentPadding

        if (comp is CompToggleButton) {
            val toggle = comp
            toggle.setScale(0.85f)
            toggle.setX(right - 54f)
            toggle.setY(componentY - 1f)
            return
        }

        if (comp is CompSlider) {
            val slider = comp
            val compact = width < 300f
            if (compact) {
                slider.setWidth(max(0f, width - (componentPadding * 2f)))
                slider.setX(x + componentPadding)
                slider.setY(componentY + 14f)
            } else {
                slider.setWidth(max(110f, width - 200f))
                slider.setX(right - slider.getWidth() - componentPadding)
                slider.setY(componentY + 2f)
            }
            return
        }

        if (comp is CompComboBox) {
            val comboBox = comp
            val compact = width < 300f
            if (compact) {
                val comboWidth = max(0f, width - (componentPadding * 2f) - 6f)
                comboBox.setWidth(comboWidth)
                comboBox.setX(x + componentPadding)
                comboBox.setY(componentY + 14f)
            } else {
                val comboWidth = max(110f, min(width - 48f, width - 180f))
                comboBox.setWidth(comboWidth)
                comboBox.setX(max(x + componentPadding, right - comboWidth - componentPadding))
                comboBox.setY(componentY)
            }
            return
        }

        if (comp is CompKeybind) {
            val keybind = comp
            keybind.setX(right - 130f)
            keybind.setY(componentY + 2f)
            return
        }

        if (comp is CompModTextBox) {
            val textBox = comp
            textBox.setWidth(min(max(120f, width - 160f), 180f))
            textBox.setHeight(18f)
            textBox.setX(right - textBox.getWidth() - componentPadding)
            textBox.setY(componentY + 2f)
            return
        }

        if (comp is CompImageSelect) {
            val imageSelect = comp
            imageSelect.setX(right - 120f)
            imageSelect.setY(componentY + 2f)
            return
        }

        if (comp is CompSoundSelect) {
            val soundSelect = comp
            soundSelect.setX(right - 120f)
            soundSelect.setY(componentY + 2f)
            return
        }

        if (comp is CompColorPicker) {
            val picker = comp
            val scale = max(0.6f, min(1.0f, width / 180f))
            picker.setScale(scale)
            val pickerWidth = 118f * scale
            val pickerX = max(x + componentPadding, right - pickerWidth - componentPadding)
            picker.setX(pickerX)
            picker.setY(componentY)
            return
        }

        if (comp is CompCellGrid) {
            val grid = comp
            grid.setWidth(width - 24f)
            grid.setHeight(height - 40f)
            grid.setX(x + 12f)
            grid.setY(componentY + 6f)
            return
        }
    }

    private fun resolveTextWidth(entry: Entry, positioned: PositionedEntry, textX: Float): Float {
        val available = positioned.width - (textX - positioned.x)

        if (entry.comp is CompSlider || entry.comp is CompComboBox) {
            val controlLeft = entry.comp.getX()
            val spacing = TEXT_GAP
            val controlSpace = controlLeft - textX - spacing
            val baseWidth = max(90f, positioned.width - 120f)
            val resolved = if (controlSpace > 0) min(baseWidth, controlSpace) else baseWidth
            return max(80f, min(resolved, positioned.width - 32f))
        }

        if (entry.comp is CompToggleButton) {
            val toggle = entry.comp as CompToggleButton
            val controlLeft = toggle.getX()
            val spacing = TEXT_GAP
            val controlSpace = controlLeft - textX - spacing
            val baseWidth = max(90f, positioned.width - 140f)
            val resolved = if (controlSpace > 0) min(baseWidth, controlSpace) else baseWidth
            return max(80f, resolved)
        }

        if (entry.comp is CompColorPicker || entry.comp is CompCellGrid) {
            return max(120f, positioned.width - 28f)
        }

        return max(110f, available - 12f)
    }

    private fun drawLabels(
        nvg: NanoVGManager,
        palette: ColorPalette,
        entry: Entry,
        textX: Float,
        textY: Float,
        textWidth: Float,
        hoverProgress: Float,
        mouseX: Int,
        mouseY: Int
    ): TooltipData? {
        val setting = entry.setting
        val metadata = setting.getMetadata()

        val titleFull = setting.name
        val titleLimited = limitText(nvg, titleFull, TITLE_FONT_SIZE, Fonts.MEDIUM, textWidth)
        val title = titleLimited.text
        val titleTruncated = titleLimited.truncated

        val titleColor = ColorUtils.interpolateColor(
            palette.getFontColor(ColorType.DARK),
            ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 230),
            hoverProgress * 0.3
        )
        nvg.drawText(title, textX, textY, titleColor, TITLE_FONT_SIZE, Fonts.MEDIUM)

        val nextLineY = textY + TITLE_FONT_SIZE + 3f
        var descriptionTruncated = false
        var descriptionFull: String? = null
        var hasDescription = false

        if (metadata != null && !metadata.description.isNullOrEmpty()) {
            descriptionFull = metadata.description
            hasDescription = true
            val limitedDesc = limitText(nvg, descriptionFull, DESCRIPTION_FONT_SIZE, Fonts.REGULAR, textWidth)
            descriptionTruncated = limitedDesc.truncated
            val description = limitedDesc.text
            val descColor = ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 200 + (hoverProgress * 30f).toInt())
            nvg.drawText(description, textX, nextLineY, descColor, DESCRIPTION_FONT_SIZE, Fonts.REGULAR)
        }

        val labelHeight = if (metadata != null && !metadata.description.isNullOrEmpty()) {
            (nextLineY - textY) + DESCRIPTION_FONT_SIZE
        } else {
            TITLE_FONT_SIZE
        }

        val hoveringText = MouseUtils.isInside(
            mouseX,
            mouseY,
            textX,
            textY - 2f,
            max(textWidth, 60f),
            max(labelHeight + 4f, 18f)
        )
        if (!hoveringText) {
            return null
        }

        if (!titleTruncated && !descriptionTruncated && !hasDescription) {
            return null
        }

        val tooltipData = TooltipData()
        if (titleTruncated) {
            tooltipData.addLine(
                TooltipLine(
                    titleFull,
                    TITLE_FONT_SIZE,
                    Fonts.MEDIUM,
                    palette.getFontColor(ColorType.DARK)
                )
            )
        }
        if (hasDescription && descriptionFull != null) {
            tooltipData.addLine(
                TooltipLine(
                    descriptionFull,
                    DESCRIPTION_FONT_SIZE,
                    Fonts.REGULAR,
                    ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 230)
                )
            )
        }
        return tooltipData
    }

    private fun drawCategoryCard(nvg: NanoVGManager, palette: ColorPalette, layout: CategoryLayout) {
        nvg.drawContainer(layout.cardX, layout.cardY, layout.cardWidth, layout.cardHeight, CATEGORY_CARD_RADIUS, palette)
    }

    private fun drawTooltip(
        nvg: NanoVGManager,
        palette: ColorPalette,
        tooltip: TooltipData,
        mouseX: Int,
        mouseY: Int,
        contentX: Float,
        contentY: Float,
        contentWidth: Float,
        viewportHeight: Float
    ) {
        val wrappedLines = ArrayList<TooltipLine>()
        for (line in tooltip.lines) {
            wrappedLines.addAll(wrapLine(nvg, line))
        }

        if (wrappedLines.isEmpty()) {
            return
        }

        val padding = 8f
        val lineSpacing = 3f
        var width = 0f
        var height = padding * 2f

        for (i in wrappedLines.indices) {
            val line = wrappedLines[i]
            val lineWidth = nvg.getTextWidth(line.text, line.size, line.font)
            width = max(width, lineWidth)
            height += line.size
            if (i < wrappedLines.size - 1) {
                height += lineSpacing
            }
        }
        width += padding * 2f

        var tooltipX = mouseX + 12f
        var tooltipY = mouseY + 12f
        val maxX = contentX + contentWidth - 6f
        val maxY = contentY + viewportHeight - 6f

        if (tooltipX + width > maxX) {
            tooltipX = maxX - width
        }
        if (tooltipY + height > maxY) {
            tooltipY = maxY - height
        }

        tooltipX = max(contentX + 6f, tooltipX)
        tooltipY = max(contentY + 6f, tooltipY)

        val background = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 235)
        val outline = ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 90)
        nvg.drawRoundedRect(tooltipX, tooltipY, width, height, 6f, background)
        nvg.drawOutlineRoundedRect(tooltipX, tooltipY, width, height, 6f, 1f, outline)

        var textY = tooltipY + padding
        for (line in wrappedLines) {
            nvg.drawText(line.text, tooltipX + padding, textY, line.color, line.size, line.font)
            textY += line.size + lineSpacing
        }
    }

    private fun wrapLine(nvg: NanoVGManager, line: TooltipLine): List<TooltipLine> {
        val wrapped = ArrayList<TooltipLine>()
        if (line.text.isEmpty()) {
            return wrapped
        }

        val words = line.text.split(" ")
        var current = StringBuilder()
        for (word in words) {
            val candidate = if (current.isEmpty()) word else current.toString() + " " + word
            if (nvg.getTextWidth(candidate, line.size, line.font) <= TOOLTIP_MAX_WIDTH || current.isEmpty()) {
                current = StringBuilder(candidate)
            } else {
                wrapped.add(TooltipLine(current.toString(), line.size, line.font, line.color))
                current = StringBuilder(word)
            }
        }
        if (current.isNotEmpty()) {
            wrapped.add(TooltipLine(current.toString(), line.size, line.font, line.color))
        }

        val adjusted = ArrayList<TooltipLine>()
        for (tooltipLine in wrapped) {
            var text = tooltipLine.text
            while (nvg.getTextWidth(text, tooltipLine.size, tooltipLine.font) > TOOLTIP_MAX_WIDTH && text.length > 1) {
                var end = text.length - 1
                while (end > 1 && nvg.getTextWidth(text.substring(0, end), tooltipLine.size, tooltipLine.font) > TOOLTIP_MAX_WIDTH) {
                    end--
                }
                adjusted.add(TooltipLine(text.substring(0, end), tooltipLine.size, tooltipLine.font, tooltipLine.color))
                text = text.substring(end)
            }
            if (text.isNotEmpty()) {
                adjusted.add(TooltipLine(text, tooltipLine.size, tooltipLine.font, tooltipLine.color))
            }
        }

        return adjusted
    }

    private fun limitText(nvg: NanoVGManager, input: String?, size: Float, font: Font, maxWidth: Float): TruncatedText {
        if (input == null) {
            return TruncatedText("", false)
        }
        var text = input
        val fullWidth = nvg.getTextWidth(text, size, font)
        if (fullWidth <= maxWidth) {
            return TruncatedText(text, false)
        }

        val ellipsis = "..."
        val ellipsisWidth = nvg.getTextWidth(ellipsis, size, font)
        while (text!!.isNotEmpty() && nvg.getTextWidth(text, size, font) + ellipsisWidth > maxWidth) {
            text = text.substring(0, text.length - 1)
        }
        if (text.isEmpty()) {
            text = ""
        }
        return TruncatedText(text + ellipsis, true)
    }

    private data class Entry(val setting: Setting, val comp: Comp, val category: CategorySetting?)

    private data class PositionedEntry(val entry: Entry, val x: Float, val y: Float, val width: Float, val height: Float)

    private fun getState(setting: Setting): EntryState {
        return entryStates.getOrPut(setting) { EntryState() }
    }

    private class EntryState {
        val hoverAnimation = SimpleAnimation()
        val heightAnimation = SimpleAnimation()
        var initialized = false
    }

    private class CategoryBlock(val category: CategorySetting?, val header: CompCategory?) {
        val settings = ArrayList<Entry>()

        fun hasHeader(): Boolean {
            return category != null && header != null
        }

        fun spansFullWidth(): Boolean {
            return !hasHeader()
        }

        fun isCollapsed(): Boolean {
            return category != null && category.isCollapsed()
        }

        fun hasContent(): Boolean {
            return hasHeader() || settings.isNotEmpty()
        }
    }

    private class CategoryLayout(val block: CategoryBlock) {
        var header: CompCategory? = null
        var headerX = 0f
        var headerY = 0f
        var headerWidth = 0f
        var headerHeight = 0f

        var cardX = 0f
        var cardY = 0f
        var cardWidth = 0f
        var cardHeight = 0f

        var entries = ArrayList<PositionedEntry>()

        fun isCollapsed(): Boolean {
            return block.isCollapsed()
        }

        fun getTotalHeight(): Float {
            var total = 0f
            if (block.hasHeader()) {
                total += headerHeight
            }
            if (!isCollapsed()) {
                if (block.hasHeader()) {
                    total += CATEGORY_HEADER_SPACING
                }
                total += cardHeight
            }
            return total
        }

        fun getBottom(): Float {
            return (if (block.hasHeader()) headerY else cardY) + getTotalHeight()
        }

        fun setHeader(header: CompCategory?, x: Float, y: Float, width: Float, height: Float) {
            this.header = header
            this.headerX = x
            this.headerY = y
            this.headerWidth = width
            this.headerHeight = height
        }

        fun setCard(x: Float, y: Float, width: Float, height: Float) {
            this.cardX = x
            this.cardY = y
            this.cardWidth = width
            this.cardHeight = height
        }
    }

    private data class TooltipLine(val text: String, val size: Float, val font: Font, val color: Color)

    private class TooltipData {
        val lines = ArrayList<TooltipLine>()

        fun addLine(line: TooltipLine?) {
            if (line != null) {
                lines.add(line)
            }
        }
    }

    private data class TruncatedText(val text: String, val truncated: Boolean)

    companion object {
        private const val OUTER_MARGIN = 10f
        private const val CATEGORY_GAP = 14f
        private const val SETTING_TEXT_MARGIN = 12f
        private const val CATEGORY_HEADER_HEIGHT = SETTING_TEXT_MARGIN + 10f
        private const val CATEGORY_HEADER_SPACING = 6f
        private const val CATEGORY_CARD_RADIUS = 12f
        private const val CARD_PADDING_X = 16f
        private const val CARD_PADDING_Y = 12f
        private const val ROW_GAP = 8f
        private const val COLUMN_GAP = 12f
        private const val MIN_ROW_HEIGHT = 34f
        private const val MIN_CARD_HEIGHT = 36f
        private const val TITLE_FONT_SIZE = 9.0f
        private const val DESCRIPTION_FONT_SIZE = 7.6f
        private const val INDICATOR_WIDTH = 3.5f
        private const val TOOLTIP_MAX_WIDTH = 320f
        private const val COMPONENT_VERTICAL_PADDING = 12f
        private const val TEXT_GAP = 16f
    }
}

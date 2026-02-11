package me.miki.shindo.ui.comp.layout

import me.miki.shindo.Shindo
import me.miki.shindo.management.color.AccentColor
import me.miki.shindo.management.color.palette.ColorPalette
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Font
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.settings.Setting
import me.miki.shindo.management.settings.impl.CategorySetting
import me.miki.shindo.ui.animation.value.SimpleAnimation
import me.miki.shindo.ui.comp.Comp
import me.miki.shindo.ui.comp.buttons.CompCategory
import me.miki.shindo.ui.comp.factory.SettingComponentFactory
import me.miki.shindo.ui.comp.layout.settingspanel.ComponentLayoutContext
import me.miki.shindo.ui.comp.layout.settingspanel.ComponentLayoutRegistry
import me.miki.shindo.ui.comp.layout.settingspanel.ComponentPlacement
import me.miki.shindo.ui.comp.layout.settingspanel.SettingsPanelStyle
import me.miki.shindo.utils.ColorUtils
import me.miki.shindo.utils.mouse.MouseUtils
import me.miki.shindo.utils.mouse.Scroll
import java.awt.Color
import kotlin.math.max
import kotlin.math.min

class SettingsPanel {

    enum class LayoutMode {
        SINGLE_COLUMN,
        DOUBLE_COLUMN
    }

    enum class DensityMode {
        AUTO,
        COMPACT,
        COMFORTABLE
    }

    private val entries = ArrayList<Entry>()
    private val categoryLayouts = ArrayList<CategoryLayout>()
    private val entryStates = HashMap<Setting, EntryState>()
    private val layoutRegistry = ComponentLayoutRegistry.createDefault()
    private val truncatedTextCache = HashMap<TruncationCacheKey, TruncatedText>()
    private val textWidthCache = HashMap<TextWidthCacheKey, Float>()

    private var style = SettingsPanelStyle()
    private var frameIndex = 0

    private var lastContentX = 0f
    private var lastContentY = 0f
    private var lastContentWidth = 0f
    private var lastViewportHeight = 0f
    private var hasViewportContext = false

    var layoutMode: LayoutMode = LayoutMode.SINGLE_COLUMN
        private set

    var densityMode: DensityMode = DensityMode.AUTO
        private set

    fun clear() {
        entries.clear()
        categoryLayouts.clear()
        entryStates.clear()
        truncatedTextCache.clear()
        textWidthCache.clear()
    }

    fun setLayoutMode(layoutMode: LayoutMode?) {
        this.layoutMode = layoutMode ?: LayoutMode.SINGLE_COLUMN
    }

    fun setDensityMode(densityMode: DensityMode?) {
        this.densityMode = densityMode ?: DensityMode.AUTO
    }

    fun setStyle(style: SettingsPanelStyle?) {
        this.style = style ?: SettingsPanelStyle()
    }

    fun buildEntries(settings: List<Setting>) {
        entries.clear()

        for (setting in settings) {
            if (setting is CategorySetting) {
                val header = CompCategory(0f, setting)
                header.setHeight(style.categoryHeaderHeight)
                entries.add(Entry(setting, header))
                getState(setting)
                continue
            }

            val component = SettingComponentFactory.create(setting) ?: continue
            entries.add(Entry(setting, component))
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
        beginFrame()
        storeViewportContext(contentX, contentY, contentWidth, viewportHeight)
        updateLayout(contentX, contentY, contentWidth, scroll.getValue())

        var tooltip: TooltipData? = null
        var contentBottom = contentY + scroll.getValue()
        val accent = Shindo.getInstance().colorManager.getCurrentColor()
        val viewportBottom = contentY + viewportHeight

        for (layout in categoryLayouts) {
            contentBottom = max(contentBottom, layout.getBottom())

            val header = layout.header
            if (header != null) {
                header.setBounds(layout.headerX, layout.headerY, layout.headerWidth, layout.headerHeight)
                if (isRowVisible(layout.headerY, layout.headerHeight, contentY, viewportBottom)) {
                    header.draw(mouseX, mouseY, partialTicks)
                }
            }

            if (layout.isCollapsed()) {
                continue
            }

            if (isRowVisible(layout.cardY, layout.cardHeight, contentY, viewportBottom)) {
                drawCategoryCard(nvg, palette, accent, layout)
            }

            for ((index, positioned) in layout.entries.withIndex()) {
                if (!isRowVisible(positioned.y, positioned.height, contentY, viewportBottom)) {
                    continue
                }

                val entry = positioned.entry
                val state = getState(entry.setting)
                val hovered = MouseUtils.isInside(mouseX, mouseY, positioned.x, positioned.y, positioned.width, positioned.height)
                state.hoverAnimation.setAnimation(if (hovered) 1f else 0f, 18.0)
                val hoverProgress = state.hoverAnimation.value

                val placement = layoutComponent(entry.comp, positioned)

                val indicatorHeight = max(14f, positioned.height - 8f)
                val indicatorY = positioned.y + (positioned.height - indicatorHeight) / 2f
                nvg.drawRoundedRect(
                    positioned.x + 1.5f,
                    indicatorY,
                    style.indicatorWidth,
                    indicatorHeight,
                    2f,
                    ColorUtils.applyAlpha(accent.getColor1(), (hoverProgress * 110f).toInt())
                )

                val textX = positioned.x + style.indicatorWidth + 9f
                val textY = positioned.y + 6f
                val textWidth = resolveTextWidth(positioned, placement, textX)

                val rowTooltip = drawLabels(nvg, palette, entry, textX, textY, textWidth, hoverProgress, mouseX, mouseY)
                if (tooltip == null && rowTooltip != null) {
                    tooltip = rowTooltip
                }

                entry.comp.draw(mouseX, mouseY, partialTicks)

                if (index < layout.entries.size - 1) {
                    nvg.drawDivider(
                        positioned.x + style.indicatorWidth + 6f,
                        positioned.y + positioned.height - 2f,
                        positioned.width - style.indicatorWidth - 12f,
                        1f,
                        1f,
                        28f + (hoverProgress * 24f)
                    )
                }
            }
        }

        scroll.maxScroll = if (categoryLayouts.isEmpty()) {
            0f
        } else {
            val contentHeight = contentBottom - (contentY + scroll.getValue())
            max(0f, contentHeight - viewportHeight)
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
        storeViewportContext(contentX, contentY, contentWidth, viewportHeight)
        updateLayout(contentX, contentY, contentWidth, scroll.getValue())

        val viewportBottom = contentY + viewportHeight
        for (layout in categoryLayouts) {
            if (layout.header != null
                && isRowVisible(layout.headerY, layout.headerHeight, contentY, viewportBottom)
                && MouseUtils.isInside(mouseX, mouseY, layout.headerX, layout.headerY, layout.headerWidth, layout.headerHeight)
            ) {
                layout.header?.mouseClicked(mouseX, mouseY, mouseButton)
                return true
            }

            if (layout.isCollapsed()) {
                continue
            }

            for (positioned in layout.entries) {
                if (positioned.height <= 0f) continue
                if (!isRowVisible(positioned.y, positioned.height, contentY, viewportBottom)) continue
                if (!MouseUtils.isInside(mouseX, mouseY, positioned.x, positioned.y, positioned.width, positioned.height)) continue

                positioned.entry.comp.mouseClicked(mouseX, mouseY, mouseButton)
                return true
            }
        }
        return false
    }

    fun mouseReleased(mouseX: Int, mouseY: Int, mouseButton: Int, scroll: Scroll) {
        if (hasViewportContext && lastViewportHeight > 0f) {
            updateLayout(lastContentX, lastContentY, lastContentWidth, scroll.getValue())
        }
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

    private fun storeViewportContext(contentX: Float, contentY: Float, contentWidth: Float, viewportHeight: Float) {
        lastContentX = contentX
        lastContentY = contentY
        lastContentWidth = contentWidth
        lastViewportHeight = viewportHeight
        hasViewportContext = true
    }

    private fun beginFrame() {
        frameIndex++
        truncatedTextCache.clear()
        textWidthCache.clear()
    }

    private fun isCompact(contentWidth: Float): Boolean {
        return when (densityMode) {
            DensityMode.COMPACT -> true
            DensityMode.COMFORTABLE -> false
            DensityMode.AUTO -> contentWidth <= style.compactBreakpoint
        }
    }

    private fun isRowVisible(y: Float, height: Float, viewportTop: Float, viewportBottom: Float): Boolean {
        val top = viewportTop - style.virtualizationBuffer
        val bottom = viewportBottom + style.virtualizationBuffer
        return y + height >= top && y <= bottom
    }

    private fun updateLayout(contentX: Float, contentY: Float, contentWidth: Float, scrollOffset: Float) {
        categoryLayouts.clear()

        val compact = isCompact(contentWidth)
        val innerX = contentX + style.outerMargin
        val innerWidth = max(0f, contentWidth - (style.outerMargin * 2f))
        var yCursor = contentY + scrollOffset

        val singleWidth = innerWidth
        val doubleWidth = (innerWidth - style.columnGap) / 2f

        val blocks = buildBlocks()
        val pendingRow = ArrayList<CategoryBlock>(2)

        for (block in blocks) {
            if (layoutMode == LayoutMode.DOUBLE_COLUMN && block.spansFullWidth()) {
                if (pendingRow.isNotEmpty()) {
                    yCursor = placeRow(innerX, yCursor, singleWidth, doubleWidth, pendingRow, compact)
                    pendingRow.clear()
                }
                val layout = layoutCategory(block, innerX, yCursor, singleWidth, compact)
                categoryLayouts.add(layout)
                yCursor = layout.getBottom() + style.categoryGap
                continue
            }

            pendingRow.add(block)
            if (layoutMode == LayoutMode.SINGLE_COLUMN || pendingRow.size == 2) {
                yCursor = placeRow(innerX, yCursor, singleWidth, doubleWidth, pendingRow, compact)
                pendingRow.clear()
            }
        }

        if (pendingRow.isNotEmpty()) {
            placeRow(innerX, yCursor, singleWidth, doubleWidth, pendingRow, compact)
        }
    }

    private fun placeRow(
        innerX: Float,
        yCursor: Float,
        singleWidth: Float,
        doubleWidth: Float,
        row: List<CategoryBlock>,
        compact: Boolean
    ): Float {
        var rowHeight = 0f
        for (i in row.indices) {
            val width = if (layoutMode == LayoutMode.DOUBLE_COLUMN) doubleWidth else singleWidth
            val x = if (layoutMode == LayoutMode.DOUBLE_COLUMN && i == 1) innerX + doubleWidth + style.columnGap else innerX

            val layout = layoutCategory(row[i], x, yCursor, width, compact)
            categoryLayouts.add(layout)
            rowHeight = max(rowHeight, layout.getTotalHeight())
        }
        return yCursor + rowHeight + style.categoryGap
    }

    private fun layoutCategory(block: CategoryBlock, x: Float, y: Float, width: Float, compact: Boolean): CategoryLayout {
        val layout = CategoryLayout(block)

        val headerHeight = if (block.hasHeader()) style.categoryHeaderHeight else 0f
        val headerSpacing = if (block.hasHeader()) style.categoryHeaderSpacing else 0f
        val cardX = x
        val cardY = y + headerHeight + headerSpacing
        val cardWidth = width
        val contentX = cardX + style.cardPaddingX
        val contentY = cardY + style.cardPaddingY
        val contentWidth = max(0f, cardWidth - (style.cardPaddingX * 2f))

        val positionedEntries = ArrayList<PositionedEntry>()

        if (block.isCollapsed()) {
            for (entry in block.settings) {
                val state = getState(entry.setting)
                val delegate = layoutRegistry.resolve(entry.comp)
                val targetHeight = delegate.targetHeight(entry.comp, compact, style)
                if (!state.initialized) {
                    state.heightAnimation.value = targetHeight
                    state.initialized = true
                }
                state.heightAnimation.setAnimation(0f, 18.0)
            }
            layout.setCard(cardX, cardY, cardWidth, 0f)
            layout.setHeader(block.header, x, y, width, headerHeight, headerSpacing)
            layout.entries = positionedEntries
            return layout
        }

        var rowCursor = contentY
        for (entry in block.settings) {
            val state = getState(entry.setting)
            val delegate = layoutRegistry.resolve(entry.comp)
            val minHeight = if (compact) style.minRowHeightCompact else style.minRowHeightComfortable
            val targetHeight = max(minHeight, delegate.targetHeight(entry.comp, compact, style))

            if (!state.initialized) {
                state.heightAnimation.value = targetHeight
                state.initialized = true
            }

            state.heightAnimation.setAnimation(targetHeight, 20.0)
            val rowHeight = max(minHeight, state.heightAnimation.value)
            positionedEntries.add(PositionedEntry(entry, contentX, rowCursor, contentWidth, rowHeight, compact))
            rowCursor += rowHeight + style.rowGap
        }

        if (positionedEntries.isNotEmpty()) {
            rowCursor -= style.rowGap
        }

        val contentHeight = max(0f, rowCursor - contentY)
        val cardHeight = max(style.minCardHeight, contentHeight + (style.cardPaddingY * 2f))
        layout.setCard(cardX, cardY, cardWidth, cardHeight)
        layout.setHeader(block.header, x, y, width, headerHeight, headerSpacing)
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
                current = CategoryBlock(entry.setting as CategorySetting, entry.comp as CompCategory)
                continue
            }
            current.settings.add(entry)
        }

        if (current.hasContent()) {
            blocks.add(current)
        }
        return blocks
    }

    private fun layoutComponent(comp: Comp, positioned: PositionedEntry): ComponentPlacement {
        val context = ComponentLayoutContext(
            positioned.x,
            positioned.y,
            positioned.width,
            positioned.height,
            positioned.compact,
            style
        )
        return layoutRegistry.resolve(comp).place(comp, context)
    }

    private fun resolveTextWidth(positioned: PositionedEntry, placement: ComponentPlacement, textX: Float): Float {
        val available = positioned.width - (textX - positioned.x)
        if (placement.controlLeft.isNaN()) {
            return max(96f, available - 12f)
        }
        val controlSpace = placement.controlLeft - textX - style.textGap
        val limited = if (controlSpace > 0f) min(available, controlSpace) else available
        return max(80f, limited)
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
        val titleLimited = limitText(nvg, titleFull, style.titleFontSize, Fonts.MEDIUM, textWidth)
        val titleColor = ColorUtils.interpolateColor(
            palette.getFontColor(ColorType.DARK),
            ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 230),
            hoverProgress * 0.3
        )
        nvg.drawText(titleLimited.text, textX, textY, titleColor, style.titleFontSize, Fonts.MEDIUM)

        val nextLineY = textY + style.titleFontSize + 3f
        var descriptionTruncated = false
        var descriptionFull: String? = null
        var hasDescription = false

        if (!metadata?.description.isNullOrEmpty()) {
            descriptionFull = metadata!!.description
            hasDescription = true
            val limitedDesc = limitText(nvg, descriptionFull, style.descriptionFontSize, Fonts.REGULAR, textWidth)
            descriptionTruncated = limitedDesc.truncated
            nvg.drawText(
                limitedDesc.text,
                textX,
                nextLineY,
                ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 200 + (hoverProgress * 30f).toInt()),
                style.descriptionFontSize,
                Fonts.REGULAR
            )
        }

        val labelHeight = if (hasDescription) (nextLineY - textY) + style.descriptionFontSize else style.titleFontSize
        val hoveringText = MouseUtils.isInside(mouseX, mouseY, textX, textY - 2f, max(textWidth, 60f), max(labelHeight + 4f, 18f))
        if (!hoveringText) return null
        if (!titleLimited.truncated && !descriptionTruncated && !hasDescription) return null

        val tooltipData = TooltipData()
        if (titleLimited.truncated) {
            tooltipData.addLine(TooltipLine(titleFull, style.titleFontSize, Fonts.MEDIUM, palette.getFontColor(ColorType.DARK)))
        }
        if (hasDescription && descriptionFull != null) {
            tooltipData.addLine(
                TooltipLine(
                    descriptionFull,
                    style.descriptionFontSize,
                    Fonts.REGULAR,
                    ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 230)
                )
            )
        }
        return tooltipData
    }

    private fun drawCategoryCard(nvg: NanoVGManager, palette: ColorPalette, accent: AccentColor, layout: CategoryLayout) {
        nvg.drawShadow(layout.cardX, layout.cardY, layout.cardWidth, layout.cardHeight, style.categoryCardRadius, 6)
        nvg.drawRoundedRect(
            layout.cardX,
            layout.cardY,
            layout.cardWidth,
            layout.cardHeight,
            style.categoryCardRadius,
            ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 210)
        )
        nvg.drawGradientRoundedRect(
            layout.cardX,
            layout.cardY,
            layout.cardWidth,
            layout.cardHeight,
            style.categoryCardRadius,
            ColorUtils.applyAlpha(accent.getColor1(), 20),
            ColorUtils.applyAlpha(accent.getColor2(), 20)
        )
        nvg.drawOutlineRoundedRect(
            layout.cardX,
            layout.cardY,
            layout.cardWidth,
            layout.cardHeight,
            style.categoryCardRadius,
            1f,
            ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 35)
        )
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
        if (wrappedLines.isEmpty()) return

        val padding = 8f
        val lineSpacing = 3f
        var width = 0f
        var height = padding * 2f
        for (i in wrappedLines.indices) {
            val line = wrappedLines[i]
            width = max(width, textWidth(nvg, line.text, line.size, line.font))
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
        if (tooltipX + width > maxX) tooltipX = maxX - width
        if (tooltipY + height > maxY) tooltipY = maxY - height
        tooltipX = max(contentX + 6f, tooltipX)
        tooltipY = max(contentY + 6f, tooltipY)

        nvg.drawRoundedRect(tooltipX, tooltipY, width, height, 6f, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 235))
        nvg.drawOutlineRoundedRect(
            tooltipX,
            tooltipY,
            width,
            height,
            6f,
            1f,
            ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 90)
        )

        var textY = tooltipY + padding
        for (line in wrappedLines) {
            nvg.drawText(line.text, tooltipX + padding, textY, line.color, line.size, line.font)
            textY += line.size + lineSpacing
        }
    }

    private fun wrapLine(nvg: NanoVGManager, line: TooltipLine): List<TooltipLine> {
        if (line.text.isEmpty()) return emptyList()

        val wrapped = ArrayList<TooltipLine>()
        val words = line.text.split(" ")
        var current = StringBuilder()
        for (word in words) {
            val candidate = if (current.isEmpty()) word else current.toString() + " " + word
            if (textWidth(nvg, candidate, line.size, line.font) <= style.tooltipMaxWidth || current.isEmpty()) {
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
            while (textWidth(nvg, text, tooltipLine.size, tooltipLine.font) > style.tooltipMaxWidth && text.length > 1) {
                var end = text.length - 1
                while (end > 1 && textWidth(nvg, text.substring(0, end), tooltipLine.size, tooltipLine.font) > style.tooltipMaxWidth) {
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
        if (input == null) return TruncatedText("", false)
        val normalizedMaxWidth = max(1f, maxWidth)
        val key = TruncationCacheKey(frameIndex, input, (size * 10f).toInt(), font.name, (normalizedMaxWidth * 10f).toInt())
        val cached = truncatedTextCache[key]
        if (cached != null) return cached

        var text = input!!
        if (textWidth(nvg, text, size, font) <= normalizedMaxWidth) {
            val resolved = TruncatedText(text, false)
            truncatedTextCache[key] = resolved
            return resolved
        }

        val ellipsis = "..."
        val ellipsisWidth = textWidth(nvg, ellipsis, size, font)
        while (text.isNotEmpty() && textWidth(nvg, text, size, font) + ellipsisWidth > normalizedMaxWidth) {
            text = text.substring(0, text.length - 1)
        }

        val resolved = TruncatedText(text + ellipsis, true)
        truncatedTextCache[key] = resolved
        return resolved
    }

    private fun textWidth(nvg: NanoVGManager, text: String, size: Float, font: Font): Float {
        val key = TextWidthCacheKey(frameIndex, text, (size * 10f).toInt(), font.name)
        val cached = textWidthCache[key]
        if (cached != null) return cached

        val width = nvg.getTextWidth(text, size, font)
        textWidthCache[key] = width
        return width
    }

    private fun getState(setting: Setting): EntryState {
        return entryStates.getOrPut(setting) { EntryState() }
    }

    private data class Entry(val setting: Setting, val comp: Comp)
    private data class PositionedEntry(
        val entry: Entry,
        val x: Float,
        val y: Float,
        val width: Float,
        val height: Float,
        val compact: Boolean
    )
    private data class TooltipLine(val text: String, val size: Float, val font: Font, val color: Color)
    private data class TruncatedText(val text: String, val truncated: Boolean)
    private data class TruncationCacheKey(
        val frame: Int,
        val input: String,
        val size10: Int,
        val fontName: String,
        val width10: Int
    )
    private data class TextWidthCacheKey(val frame: Int, val text: String, val size10: Int, val fontName: String)

    private class EntryState {
        val hoverAnimation = SimpleAnimation()
        val heightAnimation = SimpleAnimation()
        var initialized = false
    }

    private class TooltipData {
        val lines = ArrayList<TooltipLine>()
        fun addLine(line: TooltipLine?) {
            if (line != null) {
                lines.add(line)
            }
        }
    }

    private class CategoryBlock(val category: CategorySetting?, val header: CompCategory?) {
        val settings = ArrayList<Entry>()

        fun hasHeader(): Boolean = category != null && header != null
        fun spansFullWidth(): Boolean = !hasHeader()
        fun isCollapsed(): Boolean = category != null && category.isCollapsed()
        fun hasContent(): Boolean = hasHeader() || settings.isNotEmpty()
    }

    private class CategoryLayout(private val block: CategoryBlock) {
        var header: CompCategory? = null
        var headerX = 0f
        var headerY = 0f
        var headerWidth = 0f
        var headerHeight = 0f
        var headerSpacing = 0f

        var cardX = 0f
        var cardY = 0f
        var cardWidth = 0f
        var cardHeight = 0f
        var entries = ArrayList<PositionedEntry>()

        fun isCollapsed(): Boolean = block.isCollapsed()

        fun getTotalHeight(): Float {
            var total = 0f
            if (block.hasHeader()) total += headerHeight
            if (!isCollapsed()) {
                if (block.hasHeader()) total += headerSpacing
                total += cardHeight
            }
            return total
        }

        fun getBottom(): Float {
            return (if (block.hasHeader()) headerY else cardY) + getTotalHeight()
        }

        fun setHeader(header: CompCategory?, x: Float, y: Float, width: Float, height: Float, spacing: Float) {
            this.header = header
            this.headerX = x
            this.headerY = y
            this.headerWidth = width
            this.headerHeight = height
            this.headerSpacing = spacing
        }

        fun setCard(x: Float, y: Float, width: Float, height: Float) {
            this.cardX = x
            this.cardY = y
            this.cardWidth = width
            this.cardHeight = height
        }
    }
}

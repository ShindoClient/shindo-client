package me.miki.shindo.ui.components.v2.layout

import me.miki.shindo.Shindo
import me.miki.shindo.management.color.palette.ColorPalette
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Font
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.Lucide
import me.miki.shindo.management.settings.Setting
import me.miki.shindo.management.settings.impl.CategorySetting
import me.miki.shindo.ui.animation.v2.value.SimpleAnimation
import me.miki.shindo.ui.components.v2.Component
import me.miki.shindo.ui.components.v2.buttons.CompCategory
import me.miki.shindo.ui.components.v2.factory.SettingComponentFactory
import me.miki.shindo.ui.components.v2.inputs.CompCellGrid
import me.miki.shindo.ui.components.v2.inputs.CompColorPicker
import me.miki.shindo.ui.components.v2.layout.settingspanel.ComponentLayoutContext
import me.miki.shindo.ui.components.v2.layout.settingspanel.ComponentLayoutRegistry
import me.miki.shindo.ui.components.v2.layout.settingspanel.ComponentPlacement
import me.miki.shindo.ui.components.v2.layout.settingspanel.SettingsPanelStyle
import me.miki.shindo.utils.ColorUtils
import me.miki.shindo.utils.mouse.MouseUtils
import me.miki.shindo.utils.mouse.Scroll
import kotlin.math.max
import kotlin.math.min

class SettingsPanel {
    enum class LayoutMode {
        SINGLE_COLUMN,
        DOUBLE_COLUMN,
        STAGGERED_COLUMNS,
    }

    private val entries = ArrayList<Entry>()
    private val sectionLayouts = ArrayList<SectionLayout>()
    private val entryStates = HashMap<Setting, EntryState>()
    private val sectionStates = HashMap<Any, SectionState>()
    private val layoutRegistry = ComponentLayoutRegistry.createDefault()
    private val truncatedTextCache = HashMap<TruncationCacheKey, TruncatedText>()
    private val textWidthCache = HashMap<TextWidthCacheKey, Float>()

    private val rootSectionKey = Any()

    private var style = SettingsPanelStyle()
    private var resolvedLayoutStyle = SettingsPanelStyle()
    private var frameIndex = 0

    private var lastContentX = 0f
    private var lastContentY = 0f
    private var lastContentWidth = 0f
    private var lastViewportHeight = 0f
    private var hasViewportContext = false

    var layoutMode: LayoutMode = LayoutMode.SINGLE_COLUMN
        private set

    fun clear() {
        entries.clear()
        sectionLayouts.clear()
        entryStates.clear()
        sectionStates.clear()
        truncatedTextCache.clear()
        textWidthCache.clear()
    }

    fun setLayoutMode(layoutMode: LayoutMode?) {
        this.layoutMode = layoutMode ?: LayoutMode.SINGLE_COLUMN
    }

    fun setStyle(style: SettingsPanelStyle?) {
        this.style = style ?: SettingsPanelStyle()
    }

    fun buildEntries(settings: List<Setting>) {
        entries.clear()

        for (setting in settings) {
            if (setting is CategorySetting) {
                // Kept for ordering/compatibility. Rendering is custom and header-like.
                entries.add(Entry(setting, CompCategory(0f, setting), true))
                continue
            }

            val component = SettingComponentFactory.create(setting) ?: continue
            entries.add(Entry(setting, component, false))
            getEntryState(setting)
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
        scroll: Scroll,
    ) {
        beginFrame()
        storeViewportContext(contentX, contentY, contentWidth, viewportHeight)
        updateLayout(contentX, contentY, contentWidth, scroll.getValue())

        var contentBottom = contentY + scroll.getValue()
        val viewportBottom = contentY + viewportHeight

        nvg.withState {
            nvg.scissor(contentX, contentY, contentWidth, viewportHeight)

            for (section in sectionLayouts) {
                contentBottom = max(contentBottom, section.bottom())
                if (section.hasHeader() && isVisible(section.headerY, section.headerHeight, contentY, viewportBottom)) {
                    drawCategoryHeader(nvg, palette, section)
                }

                if (section.entries.isEmpty() || section.contentProgress <= MIN_SECTION_VISUAL_PROGRESS) {
                    continue
                }

                val visibleContentHeight = section.contentFullHeight * section.contentProgress
                if (visibleContentHeight <= 0.5f) {
                    continue
                }

                nvg.withState {
                    nvg.scissor(section.headerX, section.contentStartY, section.headerWidth, visibleContentHeight + 1f)
                    nvg.intersectScissor(contentX, contentY, contentWidth, viewportHeight)
                    nvg.setAlpha(section.contentProgress.coerceIn(0f, 1f))

                    for (positioned in section.entries) {
                        if (positioned.height <= 0.5f) continue

                        val animatedY = resolveAnimatedEntryY(section, positioned)
                        if (!isVisible(animatedY, positioned.height, contentY, viewportBottom)) continue

                        val animatedEntry = positioned.copy(y = animatedY)
                        nvg.withState {
                            nvg.scissor(contentX, contentY, contentWidth, viewportHeight)
                            nvg.intersectScissor(
                                section.headerX,
                                section.contentStartY,
                                section.headerWidth,
                                visibleContentHeight + 1f,
                            )
                            val placement = layoutComponent(animatedEntry)
                            drawLabels(nvg, palette, animatedEntry, placement)
                            animatedEntry.entry.comp.draw(mouseX, mouseY, partialTicks)
                        }
                    }
                }
            }
        }

        scroll.maxScroll =
            if (sectionLayouts.isEmpty()) {
                0f
            } else {
                val contentHeight = contentBottom - (contentY + scroll.getValue())
                max(0f, contentHeight - viewportHeight)
            }

        if (scroll.maxScroll > 0f) {
            val totalContentHeight = max(0f, contentBottom - (contentY + scroll.getValue()))
            nvg.drawScrollbar(
                contentX,
                contentY,
                contentWidth,
                viewportHeight,
                totalContentHeight,
                scroll.getValue(),
                palette,
                Shindo.getInstance().getColorManager().getCurrentColor(),
                18f * PANEL_SCALE,
            )
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
        scroll: Scroll,
    ): Boolean {
        storeViewportContext(contentX, contentY, contentWidth, viewportHeight)
        updateLayout(contentX, contentY, contentWidth, scroll.getValue())

        val viewportBottom = contentY + viewportHeight
        for (section in sectionLayouts) {
            if (section.hasHeader() &&
                section.category != null &&
                isVisible(section.headerY, section.headerHeight, contentY, viewportBottom) &&
                MouseUtils.isInside(
                    mouseX,
                    mouseY,
                    section.headerX,
                    section.headerY,
                    section.headerWidth,
                    section.headerHeight,
                )
            ) {
                if (mouseButton == 0) {
                    section.category.toggle()
                }
                return true
            }

            if (section.contentProgress <= MIN_SECTION_INTERACTION_PROGRESS) {
                continue
            }

            for (positioned in section.entries) {
                if (positioned.height <= 0.5f) continue
                val animatedY = resolveAnimatedEntryY(section, positioned)
                if (!isVisible(animatedY, positioned.height, contentY, viewportBottom)) continue
                if (!MouseUtils.isInside(
                        mouseX,
                        mouseY,
                        positioned.x,
                        animatedY,
                        positioned.width,
                        positioned.height,
                    )
                ) {
                    continue
                }
                positioned.entry.comp.mouseClicked(mouseX, mouseY, mouseButton)
                return true
            }
        }
        return false
    }

    fun mouseReleased(
        mouseX: Int,
        mouseY: Int,
        mouseButton: Int,
        scroll: Scroll,
    ) {
        if (hasViewportContext && lastViewportHeight > 0f) {
            updateLayout(lastContentX, lastContentY, lastContentWidth, scroll.getValue())
        }
        for (entry in entries) {
            if (!entry.isCategoryMarker) {
                entry.comp.mouseReleased(mouseX, mouseY, mouseButton)
            }
        }
    }

    fun keyTyped(
        typedChar: Char,
        keyCode: Int,
    ) {
        for (entry in entries) {
            if (!entry.isCategoryMarker) {
                entry.comp.keyTyped(typedChar, keyCode)
            }
        }
    }

    fun resetSettings() {
        for (entry in entries) {
            if (!entry.isCategoryMarker) {
                entry.setting.reset()
            }
        }
    }

    private fun beginFrame() {
        frameIndex++
        truncatedTextCache.clear()
        textWidthCache.clear()
    }

    private fun storeViewportContext(
        contentX: Float,
        contentY: Float,
        contentWidth: Float,
        viewportHeight: Float,
    ) {
        lastContentX = contentX
        lastContentY = contentY
        lastContentWidth = contentWidth
        lastViewportHeight = viewportHeight
        hasViewportContext = true
    }

    private fun isVisible(
        y: Float,
        height: Float,
        viewportTop: Float,
        viewportBottom: Float,
    ): Boolean {
        val buffer = style.virtualizationBuffer * PANEL_SCALE
        val top = viewportTop - buffer
        val bottom = viewportBottom + buffer
        return y + height >= top && y <= bottom
    }

    private fun updateLayout(
        contentX: Float,
        contentY: Float,
        contentWidth: Float,
        scrollOffset: Float,
    ) {
        sectionLayouts.clear()

        val narrow = contentWidth <= style.narrowBreakpoint
        resolvedLayoutStyle = buildScaledLayoutStyle()

        val sidePadding = style.outerMargin * PANEL_SCALE
        val innerX = contentX + sidePadding
        val innerWidth = max(0f, contentWidth - sidePadding * 2f)
        val headerHeight = style.categoryHeaderHeight * PANEL_SCALE
        val headerSpacing = style.categoryHeaderSpacing * PANEL_SCALE
        val sectionGap = style.categoryGap * PANEL_SCALE

        var yCursor = contentY + scrollOffset + (style.outerMargin * 0.45f * PANEL_SCALE)
        val sections = buildSections()
        for (section in sections) {
            val layout = SectionLayout(section.category)
            layout.headerX = innerX
            layout.headerY = yCursor
            layout.headerWidth = innerWidth
            layout.headerHeight = if (section.category != null) headerHeight else 0f
            layout.expandProgress = resolveSectionExpand(section.category)

            if (layout.hasHeader()) {
                yCursor += layout.headerHeight + headerSpacing
            }

            layout.contentStartY = yCursor
            val fullContentBottom = layoutEntries(section, layout, innerX, yCursor, innerWidth, narrow)
            layout.contentFullHeight = max(0f, fullContentBottom - layout.contentStartY)
            layout.contentProgress = smoothProgress(layout.expandProgress)
            yCursor = layout.contentStartY + layout.contentFullHeight * layout.contentProgress

            sectionLayouts.add(layout)
            yCursor += sectionGap
        }
    }

    private fun layoutEntries(
        section: Section,
        layout: SectionLayout,
        x: Float,
        startY: Float,
        width: Float,
        narrow: Boolean,
    ): Float {
        if (section.entries.isEmpty()) {
            return startY
        }

        val multiColumn = !narrow && layoutMode != LayoutMode.SINGLE_COLUMN && width >= 280f
        return when {
            !multiColumn -> layoutSingle(section.entries, layout, x, startY, width, narrow)
            layoutMode == LayoutMode.DOUBLE_COLUMN -> layoutDouble(section.entries, layout, x, startY, width, narrow)
            else -> layoutStaggered(section.entries, layout, x, startY, width, narrow)
        }
    }

    private fun layoutSingle(
        list: List<Entry>,
        layout: SectionLayout,
        x: Float,
        startY: Float,
        width: Float,
        narrow: Boolean,
    ): Float {
        val rowGap = style.rowGap * PANEL_SCALE
        var yCursor = startY
        var placed = false

        for (entry in list) {
            val rowHeight = resolveAnimatedRowHeight(entry, narrow)
            if (rowHeight <= 0.35f) continue
            layout.entries.add(PositionedEntry(entry, x, yCursor, width, rowHeight, narrow))
            yCursor += rowHeight + rowGap
            placed = true
        }

        if (placed) {
            yCursor -= rowGap
        }
        return yCursor
    }

    private fun layoutDouble(
        list: List<Entry>,
        layout: SectionLayout,
        x: Float,
        startY: Float,
        width: Float,
        narrow: Boolean,
    ): Float {
        val rowGap = style.rowGap * PANEL_SCALE
        val columnGap = style.columnGap * PANEL_SCALE
        val columnWidth = max(0f, (width - columnGap) / 2f)
        if (columnWidth < 120f) {
            return layoutSingle(list, layout, x, startY, width, narrow)
        }

        data class Pending(
            val entry: Entry,
            val rowHeight: Float,
        )

        val pending = ArrayList<Pending>(2)
        var rowY = startY
        var placed = false

        fun flushPending() {
            if (pending.isEmpty()) return

            var maxRow = 0f
            for (item in pending) {
                maxRow = max(maxRow, item.rowHeight)
            }

            for (i in pending.indices) {
                val item = pending[i]
                val entryX = if (i == 0) x else x + columnWidth + columnGap
                layout.entries.add(PositionedEntry(item.entry, entryX, rowY, columnWidth, maxRow, narrow))
            }

            rowY += maxRow + rowGap
            placed = true
            pending.clear()
        }

        for (entry in list) {
            val delegate = layoutRegistry.resolve(entry.comp)
            val rowHeight = resolveAnimatedRowHeight(entry, narrow)
            if (rowHeight <= 0.35f) continue

            if (delegate.preferFullWidth(entry.comp)) {
                flushPending()
                layout.entries.add(PositionedEntry(entry, x, rowY, width, rowHeight, narrow))
                rowY += rowHeight + rowGap
                placed = true
                continue
            }

            pending.add(Pending(entry, rowHeight))
            if (pending.size == 2) {
                flushPending()
            }
        }

        flushPending()
        if (placed) {
            rowY -= rowGap
        }
        return rowY
    }

    private fun layoutStaggered(
        list: List<Entry>,
        layout: SectionLayout,
        x: Float,
        startY: Float,
        width: Float,
        narrow: Boolean,
    ): Float {
        val rowGap = style.rowGap * PANEL_SCALE
        val columnGap = style.columnGap * PANEL_SCALE
        val columnWidth = max(0f, (width - columnGap) / 2f)
        if (columnWidth < 120f) {
            return layoutSingle(list, layout, x, startY, width, narrow)
        }

        var leftY = startY
        var rightY = startY
        var placed = false

        for (entry in list) {
            val delegate = layoutRegistry.resolve(entry.comp)
            val rowHeight = resolveAnimatedRowHeight(entry, narrow)
            if (rowHeight <= 0.35f) continue

            if (delegate.preferFullWidth(entry.comp)) {
                val fullY = max(leftY, rightY)
                layout.entries.add(PositionedEntry(entry, x, fullY, width, rowHeight, narrow))
                val next = fullY + rowHeight + rowGap
                leftY = next
                rightY = next
                placed = true
                continue
            }

            val placeLeft = leftY <= rightY
            val entryX = if (placeLeft) x else x + columnWidth + columnGap
            val entryY = if (placeLeft) leftY else rightY
            layout.entries.add(PositionedEntry(entry, entryX, entryY, columnWidth, rowHeight, narrow))

            if (placeLeft) {
                leftY += rowHeight + rowGap
            } else {
                rightY += rowHeight + rowGap
            }
            placed = true
        }

        var endY = max(leftY, rightY)
        if (placed) {
            endY -= rowGap
        }
        return endY
    }

    private fun resolveAnimatedRowHeight(
        entry: Entry,
        narrow: Boolean,
    ): Float {
        val state = getEntryState(entry.setting)
        val delegate = layoutRegistry.resolve(entry.comp)
        val minRow = (if (narrow) style.minRowHeightNarrow else style.minRowHeightDefault) * PANEL_SCALE
        val preferred = delegate.targetHeight(entry.comp, narrow, resolvedLayoutStyle) * PANEL_SCALE
        val target = max(minRow, preferred)

        if (!state.initialized) {
            state.heightAnimation.setValue(target)
            state.initialized = true
        }

        state.heightAnimation.setAnimation(target, 16.0)
        return max(0f, state.heightAnimation.getValue())
    }

    private fun drawCategoryHeader(
        nvg: NanoVGManager,
        palette: ColorPalette,
        section: SectionLayout,
    ) {
        val category = section.category ?: return
        val titleSize = style.titleFontSize * PANEL_SCALE
        val iconSize = 9f * PANEL_SCALE
        val textColor = ColorUtils.applyAlpha(palette.getFontColor(ColorType.DARK), 232)
        val helperColor = ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 138)
        val icon = Lucide.CHEVRON_RIGHT

        val iconX = section.headerX + 1f
        val iconY = section.headerY + (section.headerHeight - iconSize) / 2f
        val titleX = iconX + 12f * PANEL_SCALE
        val titleY = section.headerY + (section.headerHeight - titleSize) / 2f

        val iconCenterX = iconX + iconSize * 0.5f
        val iconCenterY = iconY + iconSize * 0.5f
        nvg.withState {
            nvg.rotateDegreesAt(iconCenterX, iconCenterY, 90f * section.contentProgress)
            nvg.drawText(icon, iconX, iconY, helperColor, iconSize, Fonts.LUCIDE)
        }
        nvg.drawText(category.name, titleX, titleY, textColor, titleSize, Fonts.MEDIUM)

        val measured = nvg.getTextWidth(category.name, titleSize, Fonts.MEDIUM)
        val lineX = titleX + measured + 8f
        val lineW = max(0f, section.headerX + section.headerWidth - (SCROLLBAR_SAFE_INSET * PANEL_SCALE) - lineX)
        if (lineW > 1f) {
            nvg.drawDivider(lineX, section.headerY + section.headerHeight * 0.58f, lineW, 1f, 1f, 38f)
        }
    }

    private fun drawLabels(
        nvg: NanoVGManager,
        palette: ColorPalette,
        positioned: PositionedEntry,
        placement: ComponentPlacement,
    ) {
        val setting = positioned.entry.setting
        val metadata = setting.getMetadata()
        val description = metadata?.description?.takeIf { it.isNotEmpty() }
        val comp = positioned.entry.comp
        val isCellGrid = comp is CompCellGrid
        val isColorPicker = comp is CompColorPicker

        val titleSize = style.titleFontSize * PANEL_SCALE * SETTINGS_TEXT_SCALE
        val descSize = style.descriptionFontSize * PANEL_SCALE * SETTINGS_TEXT_SCALE
        val textX = positioned.x + (7f * PANEL_SCALE)
        val descriptionGap = 2f * PANEL_SCALE
        val textBlockHeight = titleSize + if (description != null) (descriptionGap + descSize) else 0f
        val textY =
            if (isCellGrid || isColorPicker) {
                positioned.y + (2f * PANEL_SCALE)
            } else {
                val centeredY = positioned.y + (positioned.height - textBlockHeight) * 0.5f
                max(positioned.y + (2f * PANEL_SCALE), centeredY)
            }
        val textWidth = resolveTextWidth(positioned, placement, textX)

        val titleLimited = limitText(nvg, setting.name, titleSize, Fonts.MEDIUM, textWidth)
        nvg.drawText(titleLimited.text, textX, textY, palette.getFontColor(ColorType.DARK), titleSize, Fonts.MEDIUM)

        description?.let {
            val descY = textY + titleSize + descriptionGap
            val descLimited = limitText(nvg, description, descSize, Fonts.REGULAR, textWidth)
            nvg.drawText(
                descLimited.text,
                textX,
                descY,
                ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 198),
                descSize,
                Fonts.REGULAR,
            )
        }
    }

    private fun layoutComponent(positioned: PositionedEntry): ComponentPlacement {
        val context =
            ComponentLayoutContext(
                positioned.x,
                positioned.y,
                positioned.width,
                positioned.height,
                positioned.narrow,
                resolvedLayoutStyle,
            )
        return layoutRegistry.resolve(positioned.entry.comp).place(positioned.entry.comp, context)
    }

    private fun resolveTextWidth(
        positioned: PositionedEntry,
        placement: ComponentPlacement,
        textX: Float,
    ): Float {
        val available = positioned.width - (textX - positioned.x)
        if (placement.controlLeft.isNaN()) {
            return max(72f, available - (8f * PANEL_SCALE))
        }
        val controlSpace = placement.controlLeft - textX - (style.textGap * PANEL_SCALE)
        val limited = if (controlSpace > 0f) min(available, controlSpace) else available
        return max(70f, limited)
    }

    private fun buildSections(): List<Section> {
        val sections = ArrayList<Section>()
        var current = Section(null)

        for (entry in entries) {
            if (entry.isCategoryMarker) {
                if (current.category != null || current.entries.isNotEmpty()) {
                    sections.add(current)
                }
                current = Section(entry.setting as CategorySetting)
                continue
            }
            current.entries.add(entry)
        }

        if (current.category != null || current.entries.isNotEmpty()) {
            sections.add(current)
        }
        return sections
    }

    private fun resolveSectionExpand(category: CategorySetting?): Float {
        if (category == null) {
            return 1f
        }
        val key: Any = category
        val state = sectionStates.getOrPut(key) { SectionState() }
        val target = if (category.isCollapsed()) 0f else 1f
        if (!state.initialized) {
            state.expandAnimation.setValue(target)
            state.initialized = true
        }
        state.expandAnimation.setAnimation(target, 9.0)
        return state.expandAnimation.getValue().coerceIn(0f, 1f)
    }

    private fun resolveAnimatedEntryY(
        section: SectionLayout,
        positioned: PositionedEntry,
    ): Float {
        if (section.contentProgress >= 0.999f) {
            return positioned.y
        }
        return section.contentStartY + (positioned.y - section.contentStartY) * section.contentProgress
    }

    private fun smoothProgress(progress: Float): Float {
        val p = progress.coerceIn(0f, 1f)
        return (p * p * (3f - (2f * p))).coerceIn(0f, 1f)
    }

    private fun buildScaledLayoutStyle(): SettingsPanelStyle =
        style.copy(
            outerMargin = style.outerMargin * PANEL_SCALE,
            categoryGap = style.categoryGap * PANEL_SCALE,
            categoryHeaderHeight = style.categoryHeaderHeight * PANEL_SCALE,
            categoryHeaderSpacing = style.categoryHeaderSpacing * PANEL_SCALE,
            categoryCardRadius = style.categoryCardRadius * PANEL_SCALE,
            cardPaddingX = style.cardPaddingX * PANEL_SCALE,
            cardPaddingY = style.cardPaddingY * PANEL_SCALE,
            rowGap = style.rowGap * PANEL_SCALE,
            columnGap = style.columnGap * PANEL_SCALE,
            minRowHeightDefault = style.minRowHeightDefault * PANEL_SCALE,
            minRowHeightNarrow = style.minRowHeightNarrow * PANEL_SCALE,
            minCardHeight = style.minCardHeight * PANEL_SCALE,
            titleFontSize = style.titleFontSize * PANEL_SCALE,
            descriptionFontSize = style.descriptionFontSize * PANEL_SCALE,
            indicatorWidth = style.indicatorWidth * PANEL_SCALE,
            tooltipMaxWidth = style.tooltipMaxWidth * PANEL_SCALE,
            componentPadding = style.componentPadding * PANEL_SCALE,
            textGap = style.textGap * PANEL_SCALE,
            narrowBreakpoint = style.narrowBreakpoint,
            virtualizationBuffer = style.virtualizationBuffer * PANEL_SCALE,
        )

    private fun getEntryState(setting: Setting): EntryState = entryStates.getOrPut(setting) { EntryState() }

    private fun limitText(
        nvg: NanoVGManager,
        input: String?,
        size: Float,
        font: Font,
        maxWidth: Float,
    ): TruncatedText {
        val inputText = input ?: return TruncatedText("", false)
        val normalizedMaxWidth = max(1f, maxWidth)
        val key =
            TruncationCacheKey(
                frameIndex,
                inputText,
                (size * 10f).toInt(),
                font.name,
                (normalizedMaxWidth * 10f).toInt(),
            )
        val cached = truncatedTextCache[key]
        if (cached != null) return cached

        var text = inputText
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

    private fun textWidth(
        nvg: NanoVGManager,
        text: String,
        size: Float,
        font: Font,
    ): Float {
        val key = TextWidthCacheKey(frameIndex, text, (size * 10f).toInt(), font.name)
        val cached = textWidthCache[key]
        if (cached != null) return cached

        val width = nvg.getTextWidth(text, size, font)
        textWidthCache[key] = width
        return width
    }

    private data class Entry(
        val setting: Setting,
        val comp: Component,
        val isCategoryMarker: Boolean,
    )

    private data class Section(
        val category: CategorySetting?,
        val entries: MutableList<Entry> = ArrayList(),
    )

    private data class PositionedEntry(
        val entry: Entry,
        val x: Float,
        val y: Float,
        val width: Float,
        val height: Float,
        val narrow: Boolean,
    )

    private class SectionLayout(
        val category: CategorySetting?,
    ) {
        var headerX = 0f
        var headerY = 0f
        var headerWidth = 0f
        var headerHeight = 0f
        var expandProgress = 1f
        var contentStartY = 0f
        var contentFullHeight = 0f
        var contentProgress = 1f
        val entries = ArrayList<PositionedEntry>()

        fun hasHeader(): Boolean = category != null && headerHeight > 0f

        fun bottom(): Float {
            val headerBottom = if (hasHeader()) headerY + headerHeight else headerY
            val contentBottom = contentStartY + contentFullHeight * contentProgress
            return max(headerBottom, contentBottom)
        }
    }

    private class EntryState {
        val heightAnimation = SimpleAnimation()
        var initialized = false
    }

    private class SectionState {
        val expandAnimation = SimpleAnimation()
        var initialized = false
    }

    private data class TruncatedText(
        val text: String,
        val truncated: Boolean,
    )

    private data class TruncationCacheKey(
        val frame: Int,
        val input: String,
        val size10: Int,
        val fontName: String,
        val width10: Int,
    )

    private data class TextWidthCacheKey(
        val frame: Int,
        val text: String,
        val size10: Int,
        val fontName: String,
    )

    companion object {
        // Global compact scaling requested for cleaner settings panel layout.
        private const val PANEL_SCALE = 0.65f
        private const val SETTINGS_TEXT_SCALE = 1.15f
        private const val SCROLLBAR_SAFE_INSET = 14f
        private const val MIN_SECTION_VISUAL_PROGRESS = 0.01f
        private const val MIN_SECTION_INTERACTION_PROGRESS = 0.08f
    }
}

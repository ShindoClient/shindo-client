package me.miki.shindo.ui.components.v1.selectors

import me.miki.shindo.management.color.AccentColor
import me.miki.shindo.management.color.palette.ColorPalette
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.ui.components.v1.templates.CompPanel
import me.miki.shindo.ui.components.v1.Comp
import me.miki.shindo.utils.ColorUtils
import me.miki.shindo.utils.mouse.MouseUtils
import kotlin.math.max
import kotlin.math.min

class CompVisualPresetSelector(
    x: Float = 0f,
    y: Float = 0f,
    width: Float = 0f,
    height: Float = 116f
) : CompPanel(x, y, width, height) {

    data class Entry(val title: String, val subtitle: String)

    private val entries = ArrayList<Entry>()
    private var selectedIndex = 0
    private var onSelect: ((index: Int) -> Unit)? = null
    private var previewRenderer: ((index: Int, entry: Entry, x: Float, y: Float, width: Float, height: Float, selected: Boolean, hovered: Boolean, nvg: NanoVGManager, palette: ColorPalette, accent: AccentColor) -> Unit)? =
        null

    private val contentPadding = 12f
    private val gap = 10f
    private val minCardWidth = 108f

    init {
        //setSurfaceVariant(CompSurfaceVariant.PANEL)
        setRadius(10f)
        //setBackgroundColor(null)
    }

    fun setEntries(entries: List<Entry>): CompVisualPresetSelector {
        this.entries.clear()
        this.entries.addAll(entries)
        selectedIndex = selectedIndex.coerceIn(0, max(0, this.entries.size - 1))
        return this
    }

    fun setSelectedIndex(index: Int): CompVisualPresetSelector {
        selectedIndex = index.coerceIn(0, max(0, entries.size - 1))
        return this
    }

    fun getSelectedIndex(): Int = selectedIndex

    fun setOnSelect(callback: ((index: Int) -> Unit)?): CompVisualPresetSelector {
        onSelect = callback
        return this
    }

    fun setPreviewRenderer(
        renderer: ((index: Int, entry: Entry, x: Float, y: Float, width: Float, height: Float, selected: Boolean, hovered: Boolean, nvg: NanoVGManager, palette: ColorPalette, accent: AccentColor) -> Unit)?
    ): CompVisualPresetSelector {
        previewRenderer = renderer
        return this
    }

    override fun drawPanelContent(mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (entries.isEmpty()) return

        val slots = computeSlots()
        for (slot in slots) {
            val entry = entries[slot.index]
            val selected = slot.index == selectedIndex
            val hovered = MouseUtils.isInside(mouseX, mouseY, slot.x, slot.y, slot.width, slot.height)

            val base = ColorUtils.applyAlpha(
                palette.getBackgroundColor(ColorType.NORMAL),
                if (selected) 215 else if (hovered) 200 else 180
            )
            nvg.drawRoundedRect(slot.x, slot.y, slot.width, slot.height, 8f, base)
            val borderColor = ColorUtils.applyAlpha(
                palette.getFontColor(ColorType.NORMAL),
                if (selected) 136 else if (hovered) 98 else 70
            )
            nvg.drawOutlineRoundedRect(slot.x, slot.y, slot.width, slot.height, 8f, 1f, borderColor)

            nvg.save()
            nvg.intersectScissor(slot.x + 1f, slot.y + 1f, slot.width - 2f, slot.height - 2f)

            val textWidth = slot.width - 12f
            val title = nvg.getLimitText(entry.title, 9.5f, Fonts.MEDIUM, textWidth)
            val subtitle = nvg.getLimitText(entry.subtitle, 8f, Fonts.REGULAR, textWidth)
            nvg.drawText(title, slot.x + 6f, slot.y + 12f, palette.getFontColor(ColorType.DARK), 9.5f, Fonts.MEDIUM)
            nvg.drawText(subtitle, slot.x + 6f, slot.y + 26f, palette.getFontColor(ColorType.NORMAL), 8f, Fonts.REGULAR)

            val previewX = slot.x + 6f
            val previewY = slot.y + 38f
            val previewWidth = slot.width - 12f
            val previewHeight = slot.height - 44f
            val customRenderer = previewRenderer
            if (customRenderer != null) {
                customRenderer.invoke(
                    slot.index,
                    entry,
                    previewX,
                    previewY,
                    previewWidth,
                    previewHeight,
                    selected,
                    hovered,
                    nvg,
                    palette,
                    accent
                )
            } else {
                drawMiniPreview(previewX, previewY, previewWidth, previewHeight, slot.index, selected)
            }

            nvg.restore()
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (!isVisible() || mouseButton != 0 || entries.isEmpty()) {
            super.mouseClicked(mouseX, mouseY, mouseButton)
            return
        }

        for (slot in computeSlots()) {
            if (!MouseUtils.isInside(mouseX, mouseY, slot.x, slot.y, slot.width, slot.height)) {
                continue
            }
            if (selectedIndex != slot.index) {
                selectedIndex = slot.index
                onSelect?.invoke(slot.index)
            }
            break
        }

        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    private fun computeSlots(): List<CardSlot> {
        if (entries.isEmpty()) return emptyList()

        val availableWidth = max(0f, getWidth() - contentPadding * 2f)
        val columns = resolveColumns(availableWidth, entries.size)
        val rows = ((entries.size + columns - 1) / columns).coerceAtLeast(1)

        val cardWidth = if (columns == 1) {
            availableWidth
        } else {
            max(minCardWidth, (availableWidth - gap * (columns - 1)) / columns)
        }
        val availableHeight = max(72f, getHeight() - contentPadding * 2f)
        val cardHeight = max(34f, (availableHeight - gap * (rows - 1)) / rows)

        val slots = ArrayList<CardSlot>(entries.size)
        var index = 0
        for (row in 0 until rows) {
            val y = getY() + contentPadding + row * (cardHeight + gap)
            val rowCount = min(columns, entries.size - index)
            val rowWidth = rowCount * cardWidth + max(0, rowCount - 1) * gap
            var x = getX() + contentPadding + (availableWidth - rowWidth) / 2f
            for (column in 0 until rowCount) {
                slots.add(CardSlot(index, x, y, cardWidth, cardHeight))
                x += cardWidth + gap
                index++
            }
        }
        return slots
    }

    private fun resolveColumns(availableWidth: Float, count: Int): Int {
        if (count <= 1 || availableWidth <= minCardWidth) return 1
        val twoColumnWidth = minCardWidth * 2f + gap
        return if (availableWidth >= twoColumnWidth) min(2, count) else 1
    }

    private fun drawMiniPreview(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        index: Int,
        selected: Boolean
    ) {
        if (width <= 8f || height <= 8f) return
        nvg.drawRoundedRect(
            x,
            y,
            width,
            height,
            4f,
            ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), if (selected) 180 else 150)
        )
        val columns = if (index % 2 == 0) 1 else 2
        val rows = if (index % 3 == 0) 3 else 2
        val cardGap = 4f
        val cardWidth = max(12f, (width - (columns + 1) * cardGap) / columns)
        val cardHeight = max(6f, (height - (rows + 1) * cardGap) / rows)

        for (row in 0 until rows) {
            for (column in 0 until columns) {
                val blockX = x + cardGap + column * (cardWidth + cardGap)
                val blockY = y + cardGap + row * (cardHeight + cardGap)
                val activeBlock = (row + column + index) % max(1, rows + columns) == 0

                nvg.drawRoundedRect(
                    blockX,
                    blockY,
                    cardWidth,
                    cardHeight,
                    3f,
                    ColorUtils.applyAlpha(
                        if (activeBlock) accent.getColor1() else palette.getBackgroundColor(ColorType.MID),
                        if (activeBlock && selected) 190 else if (activeBlock) 150 else 175
                    )
                )

                val lineWidth = max(6f, cardWidth - 8f)
                nvg.drawRoundedRect(
                    blockX + 4f,
                    blockY + 4f,
                    lineWidth,
                    3f,
                    1.5f,
                    ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), if (selected) 205 else 175)
                )
            }
        }
    }

    private data class CardSlot(
        val index: Int,
        val x: Float,
        val y: Float,
        val width: Float,
        val height: Float
    )
}

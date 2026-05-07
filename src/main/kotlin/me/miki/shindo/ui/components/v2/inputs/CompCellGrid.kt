package me.miki.shindo.ui.components.v2.inputs

import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.settings.impl.CellGridSetting
import me.miki.shindo.ui.animation.v2.value.SimpleAnimation
import me.miki.shindo.ui.components.v2.Component
import me.miki.shindo.utils.ColorUtils
import kotlin.math.max
import kotlin.math.min

class CompCellGrid(
    width: Float,
    height: Float,
    private val setting: CellGridSetting
) : Component() {

    private var hoverRow = -1
    private var hoverCol = -1
    private val hoverAnimation = SimpleAnimation()

    init {
        setWidth(width)
        setHeight(height)
    }

    fun estimatePreferredHeight(narrow: Boolean): Float {
        val cells = setting.getCells() ?: return if (narrow) 188f else 172f
        val info = resolveGridInfo(cells) ?: return if (narrow) 188f else 172f
        val preferredCell = if (narrow) 13f else 12f
        val gridHeight = preferredCell * info.rows
        val padding = if (narrow) 18f else 16f
        val minHeight = if (narrow) 160f else 148f
        val maxHeight = if (narrow) 236f else 212f
        return (gridHeight + padding).coerceIn(minHeight, maxHeight)
    }

    override fun draw(mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (!isVisible()) return
        val cells = setting.getCells() ?: return
        val layout = calculateLayout(cells) ?: return

        updateHover(mouseX, mouseY, layout, cells)
        hoverAnimation.setAnimation(if (hoverRow >= 0 && hoverCol >= 0) 1f else 0f, 12.0)

        val nvgInstance = nvg
        val paletteColors = palette
        val accentColor = accent

        val boardBackground = ColorUtils.applyAlpha(paletteColors.getBackgroundColor(ColorType.DARK), 176)
        val boardOutline = ColorUtils.applyAlpha(paletteColors.getFontColor(ColorType.NORMAL), 86)
        val cellInactive = ColorUtils.applyAlpha(paletteColors.getBackgroundColor(ColorType.NORMAL), 98)
        val hoverOverlay = (62f + (hoverAnimation.getValue() * 50f)).toInt().coerceIn(0, 150)
        val cornerRadius = min(6f, max(2.5f, layout.cellSize * 0.2f))
        val inset = layout.gap * 0.5f

        nvgInstance.withState {
            nvgInstance.scissor(getX(), getY(), getWidth(), getHeight())

            nvgInstance.drawShadow(layout.boardX, layout.boardY, layout.boardWidth, layout.boardHeight, 6f, 3)
            nvgInstance.drawRoundedRect(
                layout.boardX,
                layout.boardY,
                layout.boardWidth,
                layout.boardHeight,
                6f,
                boardBackground
            )
            nvgInstance.drawOutlineRoundedRect(
                layout.boardX,
                layout.boardY,
                layout.boardWidth,
                layout.boardHeight,
                6f,
                1f,
                boardOutline
            )

            nvgInstance.save()
            nvgInstance.scissor(layout.boardX, layout.boardY, layout.boardWidth, layout.boardHeight)

            for (row in 0 until layout.rows) {
                val rowCells = cells[row]
                for (col in rowCells.indices) {
                    val cellX = layout.gridX + (col * layout.stride) + inset
                    val cellY = layout.gridY + (row * layout.stride) + inset
                    val enabled = rowCells[col]

                    if (enabled) {
                        nvgInstance.drawGradientRoundedRect(
                            cellX,
                            cellY,
                            layout.cellSize,
                            layout.cellSize,
                            cornerRadius,
                            ColorUtils.applyAlpha(accentColor.getColor1(), 226),
                            ColorUtils.applyAlpha(accentColor.getColor2(), 214)
                        )
                    } else {
                        nvgInstance.drawRoundedRect(
                            cellX,
                            cellY,
                            layout.cellSize,
                            layout.cellSize,
                            cornerRadius,
                            cellInactive
                        )
                    }

                    if (row == hoverRow && col == hoverCol) {
                        nvgInstance.drawRoundedRect(
                            cellX,
                            cellY,
                            layout.cellSize,
                            layout.cellSize,
                            cornerRadius,
                            ColorUtils.applyAlpha(accentColor.getColor2(), hoverOverlay)
                        )
                        nvgInstance.drawOutlineRoundedRect(
                            cellX,
                            cellY,
                            layout.cellSize,
                            layout.cellSize,
                            cornerRadius,
                            1f,
                            ColorUtils.applyAlpha(accentColor.getColor1(), 172)
                        )
                    }
                }
            }

            nvgInstance.restore()
        }
        super.draw(mouseX, mouseY, partialTicks)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (!isVisible() || mouseButton != 0) {
            super.mouseClicked(mouseX, mouseY, mouseButton)
            return
        }

        val cells = setting.getCells() ?: return
        val layout = calculateLayout(cells) ?: return
        resolveCell(mouseX, mouseY, layout, cells)?.let { (row, col) ->
            setting.setCell(row, col, !cells[row][col])
        }

        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    private fun calculateLayout(cells: Array<BooleanArray>): GridLayout? {
        val info = resolveGridInfo(cells) ?: return null
        val rows = info.rows
        val cols = info.cols

        val outerPadding = (min(getWidth(), getHeight()) * 0.04f).coerceIn(3f, 8f)
        val availableWidth = max(0f, getWidth() - (outerPadding * 2f))
        val availableHeight = max(0f, getHeight() - (outerPadding * 2f))
        if (availableWidth <= 0f || availableHeight <= 0f) return null

        val stride = min(availableWidth / cols, availableHeight / rows)
        if (stride <= 0f) return null

        val gap = (stride * 0.12f).coerceIn(1.4f, 4f)
        val cellSize = max(1f, stride - gap)
        val gridWidth = stride * cols
        val gridHeight = stride * rows
        val gridX = getX() + (getWidth() - gridWidth) * 0.5f
        val gridY = getY() + (getHeight() - gridHeight) * 0.5f

        val boardPadding = (gap * 0.45f).coerceIn(1f, 2.4f)
        return GridLayout(
            rows = rows,
            stride = stride,
            cellSize = cellSize,
            gap = gap,
            gridX = gridX,
            gridY = gridY,
            boardX = gridX - boardPadding,
            boardY = gridY - boardPadding,
            boardWidth = gridWidth + (boardPadding * 2f),
            boardHeight = gridHeight + (boardPadding * 2f)
        )
    }

    private fun resolveGridInfo(cells: Array<BooleanArray>): GridInfo? {
        if (cells.isEmpty()) return null
        val rows = cells.size
        var maxCols = 0
        for (row in cells) {
            maxCols = max(maxCols, row.size)
        }
        if (maxCols == 0) return null
        return GridInfo(rows, maxCols)
    }

    private fun updateHover(mouseX: Int, mouseY: Int, layout: GridLayout, cells: Array<BooleanArray>) {
        val cell = resolveCell(mouseX, mouseY, layout, cells)
        if (cell != null) {
            hoverRow = cell.first
            hoverCol = cell.second
        } else {
            hoverRow = -1
            hoverCol = -1
        }
    }

    private fun resolveCell(
        mouseX: Int,
        mouseY: Int,
        layout: GridLayout,
        cells: Array<BooleanArray>
    ): Pair<Int, Int>? {
        val relativeX = mouseX - layout.gridX
        val relativeY = mouseY - layout.gridY
        if (relativeX < 0f || relativeY < 0f) return null

        val col = (relativeX / layout.stride).toInt()
        val row = (relativeY / layout.stride).toInt()
        if (row < 0 || row >= layout.rows) return null

        val rowCells = cells[row]
        if (col < 0 || col >= rowCells.size) return null

        val localX = relativeX - (col * layout.stride)
        val localY = relativeY - (row * layout.stride)
        val inset = layout.gap * 0.5f
        if (localX < inset || localY < inset) return null
        if (localX > inset + layout.cellSize || localY > inset + layout.cellSize) return null

        return Pair(row, col)
    }

    private data class GridInfo(val rows: Int, val cols: Int)

    private data class GridLayout(
        val rows: Int,
        val stride: Float,
        val cellSize: Float,
        val gap: Float,
        val gridX: Float,
        val gridY: Float,
        val boardX: Float,
        val boardY: Float,
        val boardWidth: Float,
        val boardHeight: Float
    )
}

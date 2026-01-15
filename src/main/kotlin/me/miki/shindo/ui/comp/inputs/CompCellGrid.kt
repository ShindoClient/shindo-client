package me.miki.shindo.ui.comp.inputs

import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.settings.impl.CellGridSetting
import me.miki.shindo.ui.comp.Comp
import java.awt.Color
import kotlin.math.max
import kotlin.math.min

class CompCellGrid(
    width: Float,
    height: Float,
    private val setting: CellGridSetting
) : Comp() {

    private val padding = 12f
    private var hoverRow = -1
    private var hoverCol = -1

    init {
        setWidth(width)
        setHeight(height)
    }

    override fun draw(mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (!isVisible()) return
        val cells = setting.getCells() ?: return
        val layout = calculateLayout(cells) ?: return

        val paletteColors = palette
        val nvgInstance = nvg

        nvgInstance.drawRoundedRect(getX(), getY(), getWidth(), getHeight(), 10f, paletteColors.getBackgroundColor(ColorType.DARK))
        nvgInstance.drawRoundedRect(getX() + 1f, getY() + 1f, getWidth() - 2f, getHeight() - 2f, 9f, paletteColors.getBackgroundColor(ColorType.MID))

        nvgInstance.save()
        nvgInstance.scissor(getX(), getY(), getWidth(), getHeight())

        val gap = min(4f, layout.cellSize * 0.08f)
        updateHover(mouseX, mouseY, layout, cells)

        for (row in 0 until layout.rows) {
            val rowCells = cells[row]
            for (col in rowCells.indices) {
                val cellX = layout.offsetX + col * layout.cellSize
                val cellY = layout.offsetY + row * layout.cellSize
                val cellSize = layout.cellSize - gap
                val fillColor = if (rowCells[col]) setting.getCellColor(row, col) else Color(0, 0, 0, 40)

                nvgInstance.drawRoundedRect(
                    cellX + gap / 2f,
                    cellY + gap / 2f,
                    cellSize,
                    cellSize,
                    3f,
                    fillColor
                )

                if (row == hoverRow && col == hoverCol) {
                    nvgInstance.drawRoundedRect(
                        cellX + gap / 2f,
                        cellY + gap / 2f,
                        cellSize,
                        cellSize,
                        3f,
                        Color(255, 255, 255, 80)
                    )
                }
            }
        }

        nvgInstance.restore()
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
            val current = cells[row][col]
            setting.setCell(row, col, !current, null)
        }

        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    private fun calculateLayout(cells: Array<BooleanArray>): GridLayout? {
        if (cells.isEmpty()) return null
        val rows = cells.size
        var maxCols = 0
        for (row in cells) {
            maxCols = max(maxCols, row.size)
        }
        if (maxCols == 0) return null

        val availableWidth = max(0f, getWidth() - padding * 2f)
        val availableHeight = max(0f, getHeight() - padding * 2f)
        val cellSize = min(availableWidth / maxCols, availableHeight / rows)
        if (cellSize <= 0f) return null

        val gridWidth = cellSize * maxCols
        val gridHeight = cellSize * rows
        val offsetX = getX() + (getWidth() - gridWidth) / 2f
        val offsetY = getY() + (getHeight() - gridHeight) / 2f

        return GridLayout(rows, maxCols, cellSize, offsetX, offsetY)
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
        val relativeX = mouseX - layout.offsetX
        val relativeY = mouseY - layout.offsetY
        if (relativeX < 0f || relativeY < 0f) return null

        val col = (relativeX / layout.cellSize).toInt()
        val row = (relativeY / layout.cellSize).toInt()

        if (row < 0 || row >= layout.rows) return null
        val rowCells = cells[row]
        if (col < 0 || col >= rowCells.size) return null

        return Pair(row, col)
    }

    private data class GridLayout(
        val rows: Int,
        val cols: Int,
        val cellSize: Float,
        val offsetX: Float,
        val offsetY: Float
    )
}

package me.miki.shindo.management.settings.impl

import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.settings.Setting
import me.miki.shindo.management.settings.config.ConfigOwner
import java.awt.Color

open class CellGridSetting : Setting {

    private val defaultCells: Array<BooleanArray>?
    private val defaultColors: Array<IntArray>?

    private var cells: Array<BooleanArray>?
    private var colors: Array<IntArray>?

    constructor(text: TranslateText, parent: ConfigOwner, cells: Array<BooleanArray>?) : super(text, parent) {
        this.cells = copyGrid(cells)
        this.defaultCells = copyGrid(cells)
        this.colors = buildColorGrid(this.cells, DEFAULT_CELL_COLOR)
        this.defaultColors = copyColorGrid(this.colors)
    }

    constructor(name: String, parent: ConfigOwner, cells: Array<BooleanArray>?) : super(name, parent) {
        this.cells = copyGrid(cells)
        this.defaultCells = copyGrid(cells)
        this.colors = buildColorGrid(this.cells, DEFAULT_CELL_COLOR)
        this.defaultColors = copyColorGrid(this.colors)
    }

    override fun reset() {
        cells = copyGrid(defaultCells)
        colors = copyColorGrid(defaultColors)
    }

    fun getCells(): Array<BooleanArray>? {
        return cells
    }

    open fun setCells(cells: Array<BooleanArray>?) {
        this.cells = copyGrid(cells)
        alignColorsWithCells()
    }

    fun getDefaultCells(): Array<BooleanArray>? {
        return defaultCells
    }

    fun getColorGrid(): Array<IntArray>? {
        return copyColorGrid(colors)
    }

    fun setColorGrid(colors: Array<IntArray>?) {
        this.colors = alignColorGrid(colors, cells)
    }

    fun setColorGrid(colors: Array<Array<Color?>>?) {
        this.colors = alignColorGrid(colors, cells)
    }

    fun getCellColor(row: Int, col: Int): Color {
        if (!isValidIndex(row, col, colors)) {
            return DEFAULT_CELL_COLOR
        }
        return Color(colors!![row][col], true)
    }

    fun getCellColorOrDefault(row: Int, col: Int, fallback: Color?): Color {
        if (!isValidIndex(row, col, colors)) {
            return fallback ?: DEFAULT_CELL_COLOR
        }
        return Color(colors!![row][col], true)
    }

    fun setCell(row: Int, col: Int, enabled: Boolean, color: Color?) {
        if (!isValidIndex(row, col, cells)) {
            return
        }
        val copy = copyGrid(cells)
        copy?.get(row)?.set(col, enabled)
        setCells(copy)
        if (color != null) {
            setCellColor(row, col, color)
        }
    }

    fun setCellColor(row: Int, col: Int, color: Color?) {
        if (!isValidIndex(row, col, cells)) {
            return
        }
        alignColorsWithCells()
        colors?.get(row)?.set(col, (color ?: DEFAULT_CELL_COLOR).rgb)
    }

    fun fillColors(color: Color?) {
        val fill = color ?: DEFAULT_CELL_COLOR
        this.colors = buildColorGrid(this.cells, fill)
    }

    private fun alignColorsWithCells() {
        this.colors = alignColorGrid(this.colors, this.cells)
    }

    companion object {
        private val DEFAULT_CELL_COLOR = Color.RED

        private fun copyGrid(source: Array<BooleanArray>?): Array<BooleanArray>? {
            if (source == null) {
                return null
            }
            val copy = Array(source.size) { BooleanArray(0) }
            for (i in source.indices) {
                val row = source[i]
                copy[i] = row.clone()
            }
            return copy
        }

        private fun copyColorGrid(source: Array<IntArray>?): Array<IntArray>? {
            if (source == null) {
                return null
            }
            val copy = Array(source.size) { IntArray(0) }
            for (i in source.indices) {
                val row = source[i]
                copy[i] = row.clone()
            }
            return copy
        }

        private fun buildColorGrid(base: Array<BooleanArray>?, fill: Color?): Array<IntArray>? {
            if (base == null) {
                return null
            }
            val result = Array(base.size) { IntArray(0) }
            val rgb = (fill ?: DEFAULT_CELL_COLOR).rgb
            for (i in base.indices) {
                val row = base[i]
                val length = row.size
                result[i] = IntArray(length)
                for (j in 0 until length) {
                    result[i][j] = rgb
                }
            }
            return result
        }

        private fun alignColorGrid(source: Array<IntArray>?, base: Array<BooleanArray>?): Array<IntArray>? {
            if (base == null) {
                return null
            }
            val target = Array(base.size) { IntArray(0) }
            for (i in base.indices) {
                val baseRow = base[i]
                val length = baseRow.size
                target[i] = IntArray(length)
                for (j in 0 until length) {
                    var rgb = DEFAULT_CELL_COLOR.rgb
                    if (source != null && i < source.size) {
                        val sourceRow = source[i]
                        if (j < sourceRow.size) {
                            rgb = sourceRow[j]
                        }
                    }
                    target[i][j] = rgb
                }
            }
            return target
        }

        private fun alignColorGrid(source: Array<Array<Color?>>?, base: Array<BooleanArray>?): Array<IntArray>? {
            if (source == null) {
                return alignColorGrid(null as Array<IntArray>?, base)
            }
            val raw = Array(source.size) { IntArray(0) }
            for (i in source.indices) {
                val row = source[i]
                val length = row.size
                raw[i] = IntArray(length)
                for (j in 0 until length) {
                    val color = row[j]
                    raw[i][j] = (color ?: DEFAULT_CELL_COLOR).rgb
                }
            }
            return alignColorGrid(raw, base)
        }
    }

    private fun isValidIndex(row: Int, col: Int, array: Array<IntArray>?): Boolean {
        return array != null
            && row >= 0 && row < array.size
            && col >= 0 && col < array[row].size
    }

    private fun isValidIndex(row: Int, col: Int, array: Array<BooleanArray>?): Boolean {
        return array != null
            && row >= 0 && row < array.size
            && col >= 0 && col < array[row].size
    }
}

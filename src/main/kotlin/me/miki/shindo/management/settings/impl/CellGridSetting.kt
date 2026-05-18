package me.miki.shindo.management.settings.impl

import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.settings.Setting
import me.miki.shindo.management.settings.config.ConfigOwner

open class CellGridSetting : Setting {
    private val defaultCells: Array<BooleanArray>?
    private var cells: Array<BooleanArray>?

    constructor(text: TranslateText, parent: ConfigOwner, cells: Array<BooleanArray>?) : super(text, parent) {
        this.cells = copyGrid(cells)
        this.defaultCells = copyGrid(cells)
    }

    constructor(name: String, parent: ConfigOwner, cells: Array<BooleanArray>?) : super(name, parent) {
        this.cells = copyGrid(cells)
        this.defaultCells = copyGrid(cells)
    }

    override fun reset() {
        cells = copyGrid(defaultCells)
    }

    fun getCells(): Array<BooleanArray>? = cells

    open fun setCells(cells: Array<BooleanArray>?) {
        this.cells = copyGrid(cells)
    }

    fun getDefaultCells(): Array<BooleanArray>? = defaultCells

    fun setCell(
        row: Int,
        col: Int,
        enabled: Boolean,
    ) {
        if (!isValidIndex(row, col, cells)) {
            return
        }
        val copy = copyGrid(cells)
        copy?.get(row)?.set(col, enabled)
        setCells(copy)
    }

    @Deprecated("Cell color is no longer stored in CellGridSetting. Use a separate ColorSetting.")
    fun setCell(
        row: Int,
        col: Int,
        enabled: Boolean,
        color: java.awt.Color?,
    ) {
        setCell(row, col, enabled)
    }

    companion object {
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
    }

    private fun isValidIndex(
        row: Int,
        col: Int,
        array: Array<BooleanArray>?,
    ): Boolean =
        array != null &&
            row >= 0 &&
            row < array.size &&
            col >= 0 &&
            col < array[row].size
}

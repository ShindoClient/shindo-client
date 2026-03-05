package me.miki.shindo.ui.layout.interfaces

import me.miki.shindo.ui.layout.enums.UILayoutType

/**
 * Encapsulates selection logic for one [UILayoutType].
 */
interface UILayoutTypeBinding {
    val type: UILayoutType
    fun applySelection()
    fun isSelected(): Boolean
}


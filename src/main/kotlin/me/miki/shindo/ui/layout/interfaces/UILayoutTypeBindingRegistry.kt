package me.miki.shindo.ui.layout.interfaces

import me.miki.shindo.ui.layout.enums.UILayoutArea
import me.miki.shindo.ui.layout.enums.UILayoutType

/**
 * Registry that resolves layout types and their selection state.
 */
interface UILayoutTypeBindingRegistry {
    fun getTypes(area: UILayoutArea): List<UILayoutType>
    fun getSelectedType(area: UILayoutArea): UILayoutType?
    fun selectType(type: UILayoutType?)
    fun isSelected(type: UILayoutType): Boolean
}


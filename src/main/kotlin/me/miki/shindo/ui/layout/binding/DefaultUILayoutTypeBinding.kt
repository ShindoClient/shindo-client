package me.miki.shindo.ui.layout.binding

import me.miki.shindo.ui.layout.enums.UILayoutType
import me.miki.shindo.ui.layout.interfaces.UILayoutTypeBinding

class DefaultUILayoutTypeBinding(
    override val type: UILayoutType,
    private val applier: () -> Unit,
    private val selectedSupplier: () -> Boolean
) : UILayoutTypeBinding {

    override fun applySelection() {
        applier.invoke()
    }

    override fun isSelected(): Boolean {
        return selectedSupplier.invoke()
    }
}


package me.miki.shindo.ui.layout.interfaces

import me.miki.shindo.ui.layout.model.UILayoutState

/**
 * Bidirectional adapter between external settings storage and [UILayoutState].
 */
interface UILayoutStateGateway {
    fun readState(): UILayoutState
    fun applyState(state: UILayoutState)
}


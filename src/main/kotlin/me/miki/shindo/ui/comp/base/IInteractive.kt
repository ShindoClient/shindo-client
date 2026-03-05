package me.miki.shindo.ui.comp.base

interface IInteractive {
    fun isHovered(mouseX: Int, mouseY: Int): Boolean
    fun isEnabled(): Boolean
    fun setEnabled(enabled: Boolean)
    var onClick: (() -> Unit)?
    var onHoverEnter: (() -> Unit)?
    var onHoverExit: (() -> Unit)?
}

package me.miki.shindo.ui.comp.base
interface IComponent {
    fun draw(mouseX: Int, mouseY: Int, partialTicks: Float)
    fun update(partialTicks: Float)
    fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int)
    fun mouseReleased(mouseX: Int, mouseY: Int, mouseButton: Int)
    fun keyTyped(typedChar: Char, keyCode: Int)
    fun isVisible(): Boolean
    fun setVisible(visible: Boolean)
}

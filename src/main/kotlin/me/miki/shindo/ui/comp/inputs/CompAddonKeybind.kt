package me.miki.shindo.ui.comp.inputs

import me.miki.shindo.api.compat.ApiCompConfigOwner
import me.miki.shindo.management.settings.impl.KeybindSetting
import me.miki.shindo.ui.comp.Comp

/**
 * Keybind para addons, sem depender de Setting no addon.
 */
class CompAddonKeybind(
    x: Float,
    y: Float,
    width: Float,
    initialKeyCode: Int,
    private val onChange: (Int) -> Unit
) : Comp(x, y) {

    private val setting = KeybindSetting("addon-keybind", ApiCompConfigOwner, initialKeyCode)
    private val delegate = CompKeybind(x, y, width, setting)

    var keyCode: Int
        get() = setting.getKeyCode()
        set(v) {
            setting.setKeyCode(v)
            onChange(v)
        }

    fun isBinding(): Boolean = delegate.isBinding

    init {
        setWidth(width)
        setHeight(16f)
        addChild(delegate)
    }

    override fun setX(x: Float) {
        super.setX(x)
        delegate.setX(x)
    }

    override fun setY(y: Float) {
        super.setY(y)
        delegate.setY(y)
    }

    override fun setWidth(w: Float) {
        super.setWidth(w)
        delegate.setWidth(w)
    }

    override fun setHeight(h: Float) {
        super.setHeight(h)
        delegate.setHeight(h)
    }

    override fun draw(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val prev = setting.getKeyCode()
        super.draw(mouseX, mouseY, partialTicks)
        val curr = setting.getKeyCode()
        if (curr != prev) onChange(curr)
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) = super.keyTyped(typedChar, keyCode)
}

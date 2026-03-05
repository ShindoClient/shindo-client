package me.miki.shindo.ui.comp.inputs

import me.miki.client_api.render.AddonColor
import me.miki.shindo.api.compat.ApiCompConfigOwner
import me.miki.shindo.management.settings.impl.ColorSetting
import me.miki.shindo.ui.comp.Comp
import java.awt.Color

/**
 * Color picker para addons, sem depender de Setting no addon.
 */
class CompAddonColorPicker(
    x: Float,
    y: Float,
    initialColor: AddonColor,
    showAlpha: Boolean,
    private val onChange: (AddonColor) -> Unit
) : Comp(x, y) {

    private val awtColor = Color(initialColor.r, initialColor.g, initialColor.b, initialColor.a)
    private val setting = ColorSetting("addon-color", ApiCompConfigOwner, awtColor, showAlpha)
    private val delegate = CompColorPicker(x, y, setting)

    var color: AddonColor
        get() {
            val c = setting.getColor()
            return AddonColor(c.red, c.green, c.blue, c.alpha)
        }
        set(v) {
            setting.setColor(Color(v.r, v.g, v.b, v.a))
        }

    fun isOpen(): Boolean = delegate.isOpen()

    init {
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

    override fun getWidth(): Float = delegate.getWidth()
    override fun getHeight(): Float = delegate.getHeight()

    override fun draw(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val prev = color
        super.draw(mouseX, mouseY, partialTicks)
        val curr = color
        if (curr != prev) onChange(curr)
    }
}

package me.miki.shindo.ui.comp.inputs

import me.miki.shindo.api.compat.ApiCompConfigOwner
import me.miki.shindo.management.settings.impl.NumberSetting
import me.miki.shindo.management.settings.metadata.SettingMetadata
import me.miki.shindo.ui.comp.Comp

/**
 * Slider para addons, sem depender de Setting no addon. Usa CompSlider interno.
 */
class CompAddonSlider(
    x: Float,
    y: Float,
    width: Float,
    min: Double,
    max: Double,
    initialValue: Double,
    step: Double,
    integer: Boolean,
    private val onChange: (Double) -> Unit
) : Comp(x, y) {

    private val setting = NumberSetting(
        "addon-slider",
        ApiCompConfigOwner,
        initialValue.coerceIn(min, max),
        min,
        max,
        integer
    ).also {
        if (step > 0) it.applyMetadata(SettingMetadata("_").apply { this.step = step })
    }

    private val delegate = CompSlider(x, y, setting, width)

    var value: Double
        get() = setting.getValue()
        set(v) {
            setting.setValue(v.coerceIn(getMin(), getMax()))
            onChange(setting.getValue())
        }

    init {
        setWidth(width)
        setHeight(4f)
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
        val prev = setting.getValue()
        super.draw(mouseX, mouseY, partialTicks)
        val curr = setting.getValue()
        if (curr != prev) onChange(curr)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) =
        super.mouseClicked(mouseX, mouseY, mouseButton)

    override fun mouseReleased(mouseX: Int, mouseY: Int, mouseButton: Int) =
        super.mouseReleased(mouseX, mouseY, mouseButton)

    fun getMin(): Double = setting.getMinValue()
    fun getMax(): Double = setting.getMaxValue()
    fun getStep(): Double = setting.getStep()
    fun isInteger(): Boolean = setting.isInteger()
}

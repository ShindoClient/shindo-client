package me.miki.shindo.api.compat

import me.miki.client_api.comp.IComp
import me.miki.client_api.comp.ICompBadge
import me.miki.client_api.comp.ICompColorPicker
import me.miki.client_api.comp.ICompDropdown
import me.miki.client_api.comp.ICompKeybind
import me.miki.client_api.comp.ICompProgressBar
import me.miki.client_api.comp.ICompSlider
import me.miki.client_api.comp.ICompTextBox
import me.miki.client_api.comp.ICompTooltip
import me.miki.client_api.comp.IScrollableComp
import me.miki.client_api.comp.IToggleComp
import me.miki.client_api.render.AddonColor
import me.miki.shindo.ui.comp.Comp
import me.miki.shindo.ui.comp.buttons.CompAddonToggleButton
import me.miki.shindo.ui.comp.display.CompBadge
import me.miki.shindo.ui.comp.display.CompProgressBar
import me.miki.shindo.ui.comp.display.CompTooltip
import me.miki.shindo.ui.comp.inputs.CompAddonColorPicker
import me.miki.shindo.ui.comp.inputs.CompAddonDropdown
import me.miki.shindo.ui.comp.inputs.CompAddonKeybind
import me.miki.shindo.ui.comp.inputs.CompAddonSlider
import me.miki.shindo.ui.comp.inputs.CompTextBox
import me.miki.shindo.ui.comp.layout.CompScrollableWithChildren

/**
 * Adaptador unificado para Comps. Usa [wrap] para criar o adaptador correto com base no tipo.
 * Um único ponto de entrada, sem classes separadas por comp.
 */
object CompAdapter {

    /**
     * Envolve qualquer Comp no adaptador apropriado (com capabilities quando aplicável).
     */
    @JvmStatic
    fun wrap(comp: Comp): IComp = when (comp) {
        is CompTextBox -> TextBox(comp)
        is CompAddonToggleButton -> Toggle(comp)
        is CompAddonSlider -> Slider(comp)
        is CompAddonKeybind -> Keybind(comp)
        is CompAddonColorPicker -> ColorPicker(comp)
        is CompAddonDropdown -> Dropdown(comp)
        is CompScrollableWithChildren -> Scrollable(comp)
        is CompProgressBar -> ProgressBar(comp)
        is CompTooltip -> Tooltip(comp)
        is CompBadge -> Badge(comp)
        else -> Base(comp)
    }

    /**
     * Base - apenas IComp.
     */
    open class Base(protected val delegate: Comp) : IComp {
        override var x: Float
            get() = delegate.getX()
            set(v) { delegate.setX(v) }
        override var y: Float
            get() = delegate.getY()
            set(v) { delegate.setY(v) }
        override var width: Float
            get() = delegate.getWidth()
            set(v) { delegate.setWidth(v) }
        override var height: Float
            get() = delegate.getHeight()
            set(v) { delegate.setHeight(v) }
        override var visible: Boolean
            get() = delegate.isVisible()
            set(v) { delegate.setVisible(v) }

        override fun draw(mouseX: Int, mouseY: Int, partialTicks: Float) = delegate.draw(mouseX, mouseY, partialTicks)
        override fun update(partialTicks: Float) = delegate.update(partialTicks)
        override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) = delegate.mouseClicked(mouseX, mouseY, mouseButton)
        override fun mouseReleased(mouseX: Int, mouseY: Int, mouseButton: Int) = delegate.mouseReleased(mouseX, mouseY, mouseButton)

        override fun addChild(child: IComp) {
            (child as? Base)?.let { delegate.addChild(it.delegate) }
        }

        fun getWrappedComp(): Comp = delegate
    }

    /** TextBox capability. */
    private class TextBox(private val box: CompTextBox) : Base(box), ICompTextBox {
        override fun getText() = box.getText()
        override fun setText(text: String) = box.setText(text)
        override fun setDefaultText(text: String?) { text?.let { box.setDefaultText(it) } }
        override fun setMaxLength(max: Int) = box.setMaxStringLength(max)
        override fun isFocused() = box.isFocused()
    }

    /** Toggle capability. */
    private class Toggle(private val toggle: CompAddonToggleButton) : Base(toggle), IToggleComp {
        override var value: Boolean
            get() = toggle.value
            set(v) { toggle.value = v }
    }

    /** Scrollable capability. */
    private class Scrollable(private val scrollable: CompScrollableWithChildren) : Base(scrollable), IScrollableComp {
        override fun setContentHeight(height: Float) = scrollable.setContentHeight(height)
        override fun getScrollY() = scrollable.getScrollY()
        override fun setScrollY(value: Float) = scrollable.setScrollY(value)
        override fun scrollBy(delta: Float) = scrollable.scrollBy(delta)
    }

    /** ProgressBar capability. */
    private class ProgressBar(private val bar: CompProgressBar) : Base(bar), ICompProgressBar {
        override var progress: Float
            get() = bar.getProgress()
            set(v) { bar.setProgress(v) }
        override var maxProgress: Float
            get() = bar.getMaxProgress()
            set(v) { bar.setMaxProgress(v) }
    }

    /** Tooltip capability. */
    private class Tooltip(private val tooltip: CompTooltip) : Base(tooltip), ICompTooltip {
        override fun show() = tooltip.show()
        override fun hide() = tooltip.hide()
        override fun getText() = tooltip.getText()
        override fun setText(text: String) { tooltip.setText(text) }
    }

    /** Badge capability. */
    private class Badge(private val badge: CompBadge) : Base(badge), ICompBadge {
        override fun getText() = badge.getText()
        override fun setText(text: String) { badge.setText(text) }
    }

    /** Slider capability. */
    private class Slider(private val slider: CompAddonSlider) : Base(slider), ICompSlider {
        override var value: Double
            get() = slider.value
            set(v) { slider.value = v }
        override fun getMin() = slider.getMin()
        override fun getMax() = slider.getMax()
        override fun getStep() = slider.getStep()
        override fun isInteger() = slider.isInteger()
    }

    /** Keybind capability. */
    private class Keybind(private val keybind: CompAddonKeybind) : Base(keybind), ICompKeybind {
        override var keyCode: Int
            get() = keybind.keyCode
            set(v) { keybind.keyCode = v }
        override fun isBinding() = keybind.isBinding()
    }

    /** ColorPicker capability. */
    private class ColorPicker(private val picker: CompAddonColorPicker) : Base(picker), ICompColorPicker {
        override var color: AddonColor
            get() = picker.color
            set(v) { picker.color = v }
        override fun isOpen() = picker.isOpen()
    }

    /** Dropdown capability. */
    private class Dropdown(private val dropdown: CompAddonDropdown) : Base(dropdown), ICompDropdown {
        override fun getOptions() = dropdown.getOptions()
        override fun getSelectedIndex() = dropdown.getSelectedIndex()
        override fun getSelected() = dropdown.getSelected()
        override fun setSelectedIndex(index: Int) = dropdown.setSelectedIndex(index)
    }
}

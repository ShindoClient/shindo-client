package me.miki.shindo.api.compat

import me.miki.client_api.comp.IComp
import me.miki.client_api.comp.ICompBadge
import me.miki.client_api.comp.ICompColorPicker
import me.miki.client_api.comp.ICompDropdown
import me.miki.client_api.comp.ICompFactory
import me.miki.client_api.comp.ICompKeybind
import me.miki.client_api.comp.ICompSlider
import me.miki.client_api.comp.ICompTooltip
import me.miki.shindo.ui.comp.buttons.CompAddonToggleButton
import me.miki.shindo.ui.comp.layout.CompCard
import me.miki.shindo.ui.comp.layout.CompScrollableWithChildren
import me.miki.shindo.ui.comp.layout.CompSeparator
import me.miki.shindo.ui.comp.templates.CompButton
import me.miki.shindo.ui.comp.templates.CompLabel
import me.miki.shindo.ui.comp.templates.CompPanel
import me.miki.shindo.ui.comp.display.CompBadge
import me.miki.shindo.ui.comp.display.CompProgressBar
import me.miki.shindo.ui.comp.display.CompTooltip
import me.miki.shindo.ui.comp.inputs.CompAddonColorPicker
import me.miki.shindo.ui.comp.inputs.CompAddonDropdown
import me.miki.shindo.ui.comp.inputs.CompAddonKeybind
import me.miki.shindo.ui.comp.inputs.CompAddonSlider
import me.miki.client_api.render.AddonColor
import me.miki.shindo.ui.comp.inputs.CompTextBox

/**
 * Implementação de ICompFactory que cria Comps do client.
 */
class CompFactoryImpl : ICompFactory {

    override fun createPanel(x: Float, y: Float, width: Float, height: Float): IComp {
        val panel = CompPanel(x, y)
        panel.setWidth(width)
        panel.setHeight(height)
        return CompAdapter.wrap(panel)
    }

    override fun createLabel(text: String, x: Float, y: Float): IComp =
        CompAdapter.wrap(CompLabel(x, y, text))

    override fun createButton(text: String, x: Float, y: Float, width: Float, height: Float, onClick: () -> Unit): IComp {
        val btn = CompButton(x, y, width, height)
        btn.setText(text)
        btn.onClick = { onClick() }
        return CompAdapter.wrap(btn)
    }

    override fun createTextBox(x: Float, y: Float, width: Float, height: Float, defaultText: String?): IComp {
        val box = CompTextBox(x, y, width, height)
        defaultText?.let { box.setDefaultText(it) }
        return CompAdapter.wrap(box)
    }

    override fun createSeparator(x: Float, y: Float, length: Float, horizontal: Boolean): IComp {
        val orientation = if (horizontal) CompSeparator.Orientation.HORIZONTAL else CompSeparator.Orientation.VERTICAL
        return CompAdapter.wrap(CompSeparator(x, y, length, orientation))
    }

    override fun createCard(x: Float, y: Float, width: Float, height: Float): IComp =
        CompAdapter.wrap(CompCard(x, y, width, height))

    override fun createToggleButton(x: Float, y: Float, scale: Float, initial: Boolean, onChange: (Boolean) -> Unit): IComp =
        CompAdapter.wrap(CompAddonToggleButton(x, y, scale, initial, onChange))

    override fun createScrollable(x: Float, y: Float, width: Float, height: Float): IComp =
        CompAdapter.wrap(CompScrollableWithChildren(x, y, width, height))

    override fun createProgressBar(x: Float, y: Float, width: Float, height: Float, maxProgress: Float): IComp =
        CompAdapter.wrap(CompProgressBar(x, y, width, height).apply { setMaxProgress(maxProgress) })

    override fun createTooltip(text: String, x: Float, y: Float): ICompTooltip =
        CompAdapter.wrap(CompTooltip(text, x, y)) as ICompTooltip

    override fun createBadge(text: String, x: Float, y: Float): ICompBadge =
        CompAdapter.wrap(CompBadge(text, x, y)) as ICompBadge

    override fun createSlider(
        x: Float,
        y: Float,
        width: Float,
        min: Double,
        max: Double,
        initialValue: Double,
        step: Double,
        integer: Boolean,
        onChange: (Double) -> Unit
    ): ICompSlider =
        CompAdapter.wrap(CompAddonSlider(x, y, width, min, max, initialValue, step, integer, onChange)) as ICompSlider

    override fun createKeybind(
        x: Float,
        y: Float,
        width: Float,
        initialKeyCode: Int,
        onChange: (Int) -> Unit
    ): ICompKeybind =
        CompAdapter.wrap(CompAddonKeybind(x, y, width, initialKeyCode, onChange)) as ICompKeybind

    override fun createColorPicker(
        x: Float,
        y: Float,
        initialColor: AddonColor,
        showAlpha: Boolean,
        onChange: (AddonColor) -> Unit
    ): ICompColorPicker =
        CompAdapter.wrap(CompAddonColorPicker(x, y, initialColor, showAlpha, onChange)) as ICompColorPicker

    override fun createDropdown(
        x: Float,
        y: Float,
        width: Float,
        options: List<String>,
        initialSelectedIndex: Int,
        onChange: (Int, String) -> Unit
    ): ICompDropdown =
        CompAdapter.wrap(CompAddonDropdown(x, y, width, options, initialSelectedIndex, onChange)) as ICompDropdown
}

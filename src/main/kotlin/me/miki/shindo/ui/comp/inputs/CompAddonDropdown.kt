package me.miki.shindo.ui.comp.inputs

import me.miki.shindo.api.compat.ApiCompConfigOwner
import me.miki.shindo.management.settings.Setting
import me.miki.shindo.management.settings.impl.ComboSetting
import me.miki.shindo.management.settings.impl.combo.Option
import me.miki.shindo.ui.comp.Comp

/**
 * Dropdown para addons, sem depender de Setting no addon.
 */
class CompAddonDropdown(
    x: Float,
    y: Float,
    width: Float,
    options: List<String>,
    initialSelectedIndex: Int,
    private val onChange: (Int, String) -> Unit
) : Comp(x, y) {

    private val optionList = options.map { Option(it) }.toMutableList()
    private val defaultKey = if (options.isNotEmpty()) {
        Setting.normalizeKey(options[initialSelectedIndex.coerceIn(0, options.size - 1)])
    } else ""
    private val setting = ComboSetting("addon-dropdown", ApiCompConfigOwner, defaultKey, optionList)
    private val delegate = CompDropdown(x, y, width, setting)

    fun getOptions(): List<String> = optionList.map { it.name }
    fun getSelectedIndex(): Int = setting.getOptions().indexOf(setting.getOption() ?: optionList.firstOrNull())
    fun getSelected(): String? = setting.getOption()?.name

    fun setSelectedIndex(index: Int) {
        val opts = setting.getOptions()
        val i = index.coerceIn(0, opts.size - 1)
        setting.setOption(opts.getOrNull(i))
        onChange(i, getSelected() ?: "")
    }

    init {
        setWidth(width)
        setHeight(delegate.controlHeight)
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

    override fun draw(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val prevIdx = getSelectedIndex()
        super.draw(mouseX, mouseY, partialTicks)
        val currIdx = getSelectedIndex()
        if (currIdx != prevIdx) onChange(currIdx, getSelected() ?: "")
    }
}

package me.miki.shindo.management.settings.impl

import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.settings.Setting
import me.miki.shindo.management.settings.config.ConfigOwner

open class BooleanSetting : Setting {

    private val defaultValue: Boolean
    private var toggled: Boolean

    constructor(text: TranslateText, parent: ConfigOwner, toggled: Boolean) : super(text, parent) {
        this.toggled = toggled
        this.defaultValue = toggled
    }

    constructor(name: String, parent: ConfigOwner, toggled: Boolean) : super(name, parent) {
        this.toggled = toggled
        this.defaultValue = toggled
    }

    override fun reset() {
        toggled = defaultValue
    }

    fun isToggled(): Boolean {
        return toggled
    }

    open fun setToggled(toggle: Boolean) {
        toggled = toggle
    }

    fun isDefaultValue(): Boolean {
        return defaultValue
    }
}

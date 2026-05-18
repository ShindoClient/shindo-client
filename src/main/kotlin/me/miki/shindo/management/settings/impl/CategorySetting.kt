package me.miki.shindo.management.settings.impl

import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.settings.Setting
import me.miki.shindo.management.settings.config.ConfigOwner

open class CategorySetting : Setting {
    private var collapsed = false

    constructor(text: TranslateText, parent: ConfigOwner) : super(text, parent)

    constructor(name: String, parent: ConfigOwner) : super(name, parent)

    fun isCollapsed(): Boolean = collapsed

    fun setCollapsed(collapsed: Boolean) {
        this.collapsed = collapsed
    }

    fun toggle() {
        collapsed = !collapsed
    }
}

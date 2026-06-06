package com.shindoclient.shindo.management.settings.impl

import com.shindoclient.shindo.management.language.TranslateText
import com.shindoclient.shindo.management.settings.Setting
import com.shindoclient.shindo.management.settings.config.ConfigOwner

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

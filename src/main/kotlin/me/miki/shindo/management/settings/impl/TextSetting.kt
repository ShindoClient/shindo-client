package me.miki.shindo.management.settings.impl

import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.settings.Setting
import me.miki.shindo.management.settings.config.ConfigOwner

open class TextSetting : Setting {
    private val defaultText: String
    private var text: String

    constructor(text: TranslateText, parent: ConfigOwner, value: String) : super(text, parent) {
        this.text = value
        this.defaultText = value
    }

    constructor(name: String, parent: ConfigOwner, value: String) : super(name, parent) {
        this.text = value
        this.defaultText = value
    }

    override fun reset() {
        text = defaultText
    }

    fun getText(): String = text

    open fun setText(text: String) {
        this.text = text
    }

    fun getDefaultText(): String = defaultText
}

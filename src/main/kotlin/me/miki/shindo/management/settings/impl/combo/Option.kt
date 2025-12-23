package me.miki.shindo.management.settings.impl.combo

import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.settings.Setting

class Option {

    private val nameTranslate: TranslateText?
    private val fallbackName: String
    val nameKey: String

    constructor(nameTranslate: TranslateText) {
        this.nameTranslate = nameTranslate
        this.fallbackName = nameTranslate.text
        this.nameKey = nameTranslate.key
    }

    constructor(name: String) {
        this.nameTranslate = null
        this.fallbackName = name
        this.nameKey = buildKey(name)
    }

    val name: String
        get() = nameTranslate?.text ?: fallbackName

    fun getTranslate(): TranslateText? {
        return nameTranslate
    }

    private fun buildKey(raw: String?): String {
        return if (raw == null) "" else Setting.normalizeKey(raw)
    }
}

package com.shindoclient.shindo.management.settings.impl.combo

import com.shindoclient.shindo.management.language.TranslateText
import com.shindoclient.shindo.management.settings.Setting

class Option {
    private val nameTranslate: TranslateText?
    private val fallbackName: String
    val nameKey: String

    constructor(nameTranslate: TranslateText) {
        this.nameTranslate = nameTranslate
        this.fallbackName = nameTranslate.getText()
        this.nameKey = nameTranslate.getKey()
    }

    constructor(name: String) {
        this.nameTranslate = null
        this.fallbackName = name
        this.nameKey = buildKey(name)
    }

    val name: String
        get() = nameTranslate?.getText() ?: fallbackName

    fun getTranslate(): TranslateText? = nameTranslate

    private fun buildKey(raw: String?): String = if (raw == null) "" else Setting.normalizeKey(raw)
}

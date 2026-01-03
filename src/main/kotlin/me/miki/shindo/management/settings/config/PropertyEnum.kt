package me.miki.shindo.management.settings.config

import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.settings.Setting
import java.util.*

interface PropertyEnum {

    fun getTranslate(): TranslateText {
        return TranslateText.NONE
    }

    fun getNameKey(): String {
        val translate = getTranslate()
        if (translate != TranslateText.NONE) {
            return translate.key
        }
        return Setting.normalizeKey((this as Enum<*>).name)
    }

    fun getDisplayName(): String {
        val translate = getTranslate()
        if (translate != TranslateText.NONE) {
            return translate.text
        }
        var raw = (this as Enum<*>)
            .name
            .lowercase(Locale.ROOT)
            .replace('_', ' ')

        if (raw.isEmpty()) {
            return ""
        }

        return raw.replaceFirstChar {
            it.titlecase(Locale.ROOT)
        }
    }
}

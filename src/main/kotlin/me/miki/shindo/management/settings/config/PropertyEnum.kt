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
            return translate.getKey()
        }
        return Setting.normalizeKey((this as Enum<*>).name)
    }

    fun getDisplayName(): String {
        val translate = getTranslate()
        if (translate != TranslateText.NONE) {
            return translate.getText()
        }
        val raw = (this as Enum<*>)
            .name
            .toLowerCase(Locale.ROOT)
            .replace('_', ' ')

        if (raw.isEmpty()) {
            return ""
        }

        return raw.substring(0, 1).toUpperCase(Locale.ROOT) + raw.substring(1)
    }
}


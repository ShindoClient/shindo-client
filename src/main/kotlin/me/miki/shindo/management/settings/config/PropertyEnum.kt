package me.miki.shindo.management.settings.config

import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.settings.Setting
import java.util.*
import kotlin.jvm.JvmDefault

interface PropertyEnum {

    @JvmDefault
    fun getTranslate(): TranslateText {
        return TranslateText.NONE
    }

    @JvmDefault
    fun getNameKey(): String {
        val translate = getTranslate()
        if (translate != TranslateText.NONE) {
            return translate.key
        }
        return Setting.normalizeKey((this as Enum<*>).name)
    }

    @JvmDefault
    fun getDisplayName(): String {
        val translate = getTranslate()
        if (translate != TranslateText.NONE) {
            return translate.text
        }
        var raw = (this as Enum<*>).name.toLowerCase(Locale.ROOT).replace('_', ' ')
        if (raw.isEmpty()) {
            return ""
        }
        raw = raw.substring(0, 1).toUpperCase(Locale.ROOT) + raw.substring(1)
        return raw
    }
}

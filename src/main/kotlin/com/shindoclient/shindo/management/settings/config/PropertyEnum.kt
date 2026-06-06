package com.shindoclient.shindo.management.settings.config

import com.shindoclient.shindo.management.language.TranslateText
import com.shindoclient.shindo.management.settings.Setting
import java.util.Locale

interface PropertyEnum {
    fun getTranslate(): TranslateText = TranslateText.NONE

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
        val raw =
            (this as Enum<*>)
                .name
                .lowercase(Locale.ROOT)
                .replace('_', ' ')

        if (raw.isEmpty()) {
            return ""
        }

        return raw.substring(0, 1).uppercase(Locale.ROOT) + raw.substring(1)
    }
}

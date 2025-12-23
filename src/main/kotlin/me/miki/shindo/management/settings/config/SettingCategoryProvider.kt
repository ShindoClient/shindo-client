package me.miki.shindo.management.settings.config

import me.miki.shindo.management.language.TranslateText
import kotlin.jvm.JvmDefault

interface SettingCategoryProvider {
    fun resolveCategoryLabel(categoryKey: String): TranslateText?

    @JvmDefault
    fun isCategoryInitiallyCollapsed(categoryKey: String): Boolean {
        return false
    }
}

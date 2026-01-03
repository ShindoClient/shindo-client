package me.miki.shindo.management.settings.config

import me.miki.shindo.management.language.TranslateText

interface SettingCategoryProvider {

    fun resolveCategoryLabel(categoryKey: String): TranslateText?

    fun isCategoryInitiallyCollapsed(categoryKey: String): Boolean {
        return false
    }
}

package com.shindoclient.shindo.management.settings.config

import com.shindoclient.shindo.management.language.TranslateText

interface SettingCategoryProvider {
    fun resolveCategoryLabel(categoryKey: String): TranslateText?

    fun isCategoryInitiallyCollapsed(categoryKey: String): Boolean = false
}

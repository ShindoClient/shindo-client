package me.miki.shindo.gui.modmenu.category.impl.network

import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.nanovg.font.LegacyIcon

enum class NetworkSection(private val label: TranslateText, val icon: String) {
    TWEAKER(TranslateText.NETWORK_CATEGORY_OVERVIEW, LegacyIcon.GLOBE),
    PROXY(TranslateText.NETWORK_PROXY_WARP, LegacyIcon.NET);

    fun getLabel(): String {
        return label.text
    }
}

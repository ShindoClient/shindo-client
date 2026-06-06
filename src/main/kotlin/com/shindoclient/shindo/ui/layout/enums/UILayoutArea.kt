package com.shindoclient.shindo.ui.layout.enums

import com.shindoclient.shindo.management.language.TranslateText
import com.shindoclient.shindo.management.nanovg.font.Lucide

enum class UILayoutArea(
    val title: TranslateText,
    val description: TranslateText,
    private val icon: String,
) {
    SETTINGS(
        TranslateText.SETTINGS,
        TranslateText.SETTINGS_LAYOUT_DESCRIPTION,
        Lucide.SETTINGS,
    ),
    MODULES(
        TranslateText.SETTINGS_LAYOUT_SECTION_MODULE,
        TranslateText.SETTINGS_LAYOUT_MODULE_SINGLE_DESCRIPTION,
        Lucide.LIST,
    ),
    NOTIFICATIONS(
        TranslateText.SETTINGS_LAYOUT_SECTION_NOTIFICATION,
        TranslateText.SETTINGS_LAYOUT_NOTIFICATION_DESCRIPTION,
        Lucide.BELL,
    ),
    VISUAL(
        TranslateText.PRESETS,
        TranslateText.APPEARANCE_DESCRIPTION,
        Lucide.PALETTE,
    ),
    ;

    fun getTitle(): String = title.getText()

    fun getDescription(): String = description.getText()

    fun getIcon(): String = icon
}

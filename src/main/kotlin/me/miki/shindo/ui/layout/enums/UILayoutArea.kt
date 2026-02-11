package me.miki.shindo.ui.layout.enums

import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.nanovg.font.LegacyIcon

enum class UILayoutArea(
        val title: TranslateText,
        val description: TranslateText,
        private val icon: String
) {
    SETTINGS(
            TranslateText.SETTINGS,
            TranslateText.SETTINGS_LAYOUT_DESCRIPTION,
            LegacyIcon.SETTINGS
    ),
    MODULES(
            TranslateText.SETTINGS_LAYOUT_SECTION_MODULE,
            TranslateText.SETTINGS_LAYOUT_MODULE_SINGLE_DESCRIPTION,
            LegacyIcon.LIST
    ),
    NOTIFICATIONS(
            TranslateText.SETTINGS_LAYOUT_SECTION_NOTIFICATION,
            TranslateText.SETTINGS_LAYOUT_NOTIFICATION_DESCRIPTION,
            LegacyIcon.BELL
    ),
    VISUAL(
            TranslateText.PRESETS,
            TranslateText.APPEARANCE_DESCRIPTION,
            LegacyIcon.COLOUR
    );

    fun getTitle(): String = title.getText()
    fun getDescription(): String = description.getText()
    fun getIcon(): String = icon
}

package me.miki.shindo.ui.layout.enums

import me.miki.shindo.management.language.TranslateText

enum class UILayoutType(
        val area: UILayoutArea,
        val title: TranslateText,
        val description: TranslateText
) {
    SETTINGS_SINGLE(
            UILayoutArea.SETTINGS,
            TranslateText.SETTINGS_LAYOUT_SINGLE_TITLE,
            TranslateText.SETTINGS_LAYOUT_SINGLE_DESCRIPTION
    ),
    SETTINGS_DOUBLE(
            UILayoutArea.SETTINGS,
            TranslateText.SETTINGS_LAYOUT_DOUBLE_TITLE,
            TranslateText.SETTINGS_LAYOUT_DOUBLE_DESCRIPTION
    ),
    MODULES_SINGLE(
            UILayoutArea.MODULES,
            TranslateText.SETTINGS_LAYOUT_MODULE_SINGLE_TITLE,
            TranslateText.SETTINGS_LAYOUT_MODULE_SINGLE_DESCRIPTION
    ),
    MODULES_DOUBLE(
            UILayoutArea.MODULES,
            TranslateText.SETTINGS_LAYOUT_MODULE_DOUBLE_TITLE,
            TranslateText.SETTINGS_LAYOUT_MODULE_DOUBLE_DESCRIPTION
    ),
    NOTIFICATION_TOP_LEFT(
            UILayoutArea.NOTIFICATIONS,
            TranslateText.SETTINGS_LAYOUT_NOTIFICATION_TOP_LEFT_TITLE,
            TranslateText.SETTINGS_LAYOUT_NOTIFICATION_TOP_LEFT_DESCRIPTION
    ),
    NOTIFICATION_TOP_RIGHT(
            UILayoutArea.NOTIFICATIONS,
            TranslateText.SETTINGS_LAYOUT_NOTIFICATION_TOP_RIGHT_TITLE,
            TranslateText.SETTINGS_LAYOUT_NOTIFICATION_TOP_RIGHT_DESCRIPTION
    ),
    NOTIFICATION_BOTTOM_LEFT(
            UILayoutArea.NOTIFICATIONS,
            TranslateText.SETTINGS_LAYOUT_NOTIFICATION_BOTTOM_LEFT_TITLE,
            TranslateText.SETTINGS_LAYOUT_NOTIFICATION_BOTTOM_LEFT_DESCRIPTION
    ),
    NOTIFICATION_BOTTOM_RIGHT(
            UILayoutArea.NOTIFICATIONS,
            TranslateText.SETTINGS_LAYOUT_NOTIFICATION_BOTTOM_RIGHT_TITLE,
            TranslateText.SETTINGS_LAYOUT_NOTIFICATION_BOTTOM_RIGHT_DESCRIPTION
    ),
    VISUAL_LIGHT(
            UILayoutArea.VISUAL,
            TranslateText.LIGHT,
            TranslateText.APPEARANCE_DESCRIPTION
    ),
    VISUAL_DARK(
            UILayoutArea.VISUAL,
            TranslateText.DARK,
            TranslateText.APPEARANCE_DESCRIPTION
    ),
    VISUAL_MODERN(
            UILayoutArea.VISUAL,
            TranslateText.MODERN,
            TranslateText.APPEARANCE_DESCRIPTION
    );

    fun getTitle(): String = title.getText()
    fun getDescription(): String = description.getText()
}

package me.miki.shindo.ui.layout

import me.miki.shindo.management.mods.impl.InternalSettingsMod
import me.miki.shindo.ui.components.v2.layout.SettingsPanel.LayoutMode
import me.miki.shindo.ui.layout.enums.UILayoutArea
import me.miki.shindo.ui.layout.enums.UILayoutType

/**
 * Direct accessor for layout settings via InternalSettingsMod.
 *
 * No preset system - just simple get/set for layout types.
 */
class UILayoutManager {
    fun getTypes(area: UILayoutArea): List<UILayoutType> =
        when (area) {
            UILayoutArea.SETTINGS -> settingsTypes
            UILayoutArea.MODULES -> modulesTypes
            UILayoutArea.NOTIFICATIONS -> notificationTypes
            UILayoutArea.VISUAL -> visualTypes
        }

    fun getSelectedType(area: UILayoutArea): UILayoutType? =
        when (area) {
            UILayoutArea.SETTINGS -> {
                when (InternalSettingsMod.instance.settingsLayoutMode) {
                    LayoutMode.SINGLE_COLUMN -> UILayoutType.SETTINGS_SINGLE
                    LayoutMode.DOUBLE_COLUMN -> UILayoutType.SETTINGS_DOUBLE
                    LayoutMode.STAGGERED_COLUMNS -> UILayoutType.SETTINGS_ADAPTIVE
                    null -> UILayoutType.SETTINGS_SINGLE
                }
            }

            UILayoutArea.MODULES -> {
                when (InternalSettingsMod.instance.getModuleLayout()) {
                    InternalSettingsMod.ModuleLayout.SINGLE_COLUMN -> UILayoutType.MODULES_SINGLE
                    InternalSettingsMod.ModuleLayout.TWO_COLUMNS -> UILayoutType.MODULES_DOUBLE
                }
            }

            UILayoutArea.NOTIFICATIONS -> {
                when (InternalSettingsMod.instance.notificationCorner) {
                    InternalSettingsMod.NotificationCorner.TOP_LEFT -> UILayoutType.NOTIFICATION_TOP_LEFT
                    InternalSettingsMod.NotificationCorner.TOP_RIGHT -> UILayoutType.NOTIFICATION_TOP_RIGHT
                    InternalSettingsMod.NotificationCorner.BOTTOM_LEFT -> UILayoutType.NOTIFICATION_BOTTOM_LEFT
                    InternalSettingsMod.NotificationCorner.BOTTOM_RIGHT -> UILayoutType.NOTIFICATION_BOTTOM_RIGHT
                }
            }

            UILayoutArea.VISUAL -> {
                when (InternalSettingsMod.instance.getVisualPreset()) {
                    InternalSettingsMod.VisualPreset.LIGHT -> UILayoutType.VISUAL_LIGHT

                    InternalSettingsMod.VisualPreset.DARK -> UILayoutType.VISUAL_DARK

                    InternalSettingsMod.VisualPreset.MODERN,
                    InternalSettingsMod.VisualPreset.CLASSIC,
                    -> UILayoutType.VISUAL_MODERN
                }
            }
        }

    fun selectType(type: UILayoutType?) {
        if (type == null) return
        when (type) {
            UILayoutType.SETTINGS_SINGLE -> {
                InternalSettingsMod.instance.settingsLayoutMode = LayoutMode.SINGLE_COLUMN
            }

            UILayoutType.SETTINGS_DOUBLE -> {
                InternalSettingsMod.instance.settingsLayoutMode = LayoutMode.DOUBLE_COLUMN
            }

            UILayoutType.SETTINGS_ADAPTIVE -> {
                InternalSettingsMod.instance.settingsLayoutMode =
                    LayoutMode.STAGGERED_COLUMNS
            }

            UILayoutType.MODULES_SINGLE -> {
                InternalSettingsMod.instance.setModuleLayout(InternalSettingsMod.ModuleLayout.SINGLE_COLUMN)
            }

            UILayoutType.MODULES_DOUBLE -> {
                InternalSettingsMod.instance.setModuleLayout(InternalSettingsMod.ModuleLayout.TWO_COLUMNS)
            }

            UILayoutType.NOTIFICATION_TOP_LEFT -> {
                InternalSettingsMod.instance.notificationCorner =
                    InternalSettingsMod.NotificationCorner.TOP_LEFT
            }

            UILayoutType.NOTIFICATION_TOP_RIGHT -> {
                InternalSettingsMod.instance.notificationCorner =
                    InternalSettingsMod.NotificationCorner.TOP_RIGHT
            }

            UILayoutType.NOTIFICATION_BOTTOM_LEFT -> {
                InternalSettingsMod.instance.notificationCorner =
                    InternalSettingsMod.NotificationCorner.BOTTOM_LEFT
            }

            UILayoutType.NOTIFICATION_BOTTOM_RIGHT -> {
                InternalSettingsMod.instance.notificationCorner =
                    InternalSettingsMod.NotificationCorner.BOTTOM_RIGHT
            }

            UILayoutType.VISUAL_LIGHT -> {
                InternalSettingsMod.instance.setVisualPreset(InternalSettingsMod.VisualPreset.LIGHT)
            }

            UILayoutType.VISUAL_DARK -> {
                InternalSettingsMod.instance.setVisualPreset(InternalSettingsMod.VisualPreset.DARK)
            }

            UILayoutType.VISUAL_MODERN -> {
                InternalSettingsMod.instance.setVisualPreset(InternalSettingsMod.VisualPreset.MODERN)
            }
        }
    }

    fun isSelected(type: UILayoutType): Boolean = getSelectedType(type.area) == type

    private val settingsTypes =
        listOf(
            UILayoutType.SETTINGS_SINGLE,
            UILayoutType.SETTINGS_DOUBLE,
            UILayoutType.SETTINGS_ADAPTIVE,
        )

    private val modulesTypes =
        listOf(
            UILayoutType.MODULES_SINGLE,
            UILayoutType.MODULES_DOUBLE,
        )

    private val notificationTypes =
        listOf(
            UILayoutType.NOTIFICATION_TOP_LEFT,
            UILayoutType.NOTIFICATION_TOP_RIGHT,
            UILayoutType.NOTIFICATION_BOTTOM_LEFT,
            UILayoutType.NOTIFICATION_BOTTOM_RIGHT,
        )

    private val visualTypes =
        listOf(
            UILayoutType.VISUAL_LIGHT,
            UILayoutType.VISUAL_DARK,
            UILayoutType.VISUAL_MODERN,
        )
}

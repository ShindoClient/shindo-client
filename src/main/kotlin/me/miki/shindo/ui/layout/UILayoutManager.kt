package me.miki.shindo.ui.layout

import me.miki.shindo.management.mods.impl.InternalSettingsMod
import me.miki.shindo.ui.layout.config.InternalSettingsLayoutConfiguration
import me.miki.shindo.ui.layout.enums.UIDensityMode
import me.miki.shindo.ui.layout.enums.UILayoutArea
import me.miki.shindo.ui.layout.enums.UILayoutType
import me.miki.shindo.ui.layout.enums.UISettingsLayoutMode
import me.miki.shindo.ui.layout.interfaces.UILayoutConfiguration

class UILayoutManager(
        private val configuration: UILayoutConfiguration = InternalSettingsLayoutConfiguration()
) {

    private val bindings = HashMap<UILayoutType, Binding>()

    init {
        bindDefaults()
    }

    fun getTypes(area: UILayoutArea): List<UILayoutType> {
        return UILayoutType.values().filter { it.area == area }
    }

    fun getSelectedType(area: UILayoutArea): UILayoutType? {
        val types = getTypes(area)
        for (type in types) {
            if (isSelected(type)) {
                return type
            }
        }
        return types.firstOrNull()
    }

    fun selectType(type: UILayoutType?) {
        type ?: return
        bindings[type]?.applier?.invoke()
    }

    fun isSelected(type: UILayoutType): Boolean {
        return bindings[type]?.selectedSupplier?.invoke() == true
    }

    fun getSettingsDensityMode(): UIDensityMode {
        return configuration.settingsDensityMode
    }

    fun setSettingsDensityMode(mode: UIDensityMode?) {
        configuration.settingsDensityMode = mode ?: UIDensityMode.AUTO
    }

    private fun bindDefaults() {
        for (spec in defaultSpecs()) {
            bind(spec.type, spec.applier, spec.selectedSupplier)
        }
    }

    private fun bind(
            type: UILayoutType,
            applier: () -> Unit,
            selectedSupplier: () -> Boolean
    ) {
        bindings[type] = Binding(applier, selectedSupplier)
    }

    private data class Binding(
            val applier: () -> Unit,
            val selectedSupplier: () -> Boolean
    )

    private data class BindingSpec(
            val type: UILayoutType,
            val applier: () -> Unit,
            val selectedSupplier: () -> Boolean
    )

    private fun defaultSpecs(): List<BindingSpec> {
        return listOf(
                BindingSpec(
                        UILayoutType.SETTINGS_SINGLE,
                        { configuration.settingsLayoutMode = UISettingsLayoutMode.SINGLE_COLUMN },
                        { configuration.settingsLayoutMode == UISettingsLayoutMode.SINGLE_COLUMN }
                ),
                BindingSpec(
                        UILayoutType.SETTINGS_DOUBLE,
                        { configuration.settingsLayoutMode = UISettingsLayoutMode.DOUBLE_COLUMN },
                        { configuration.settingsLayoutMode == UISettingsLayoutMode.DOUBLE_COLUMN }
                ),
                BindingSpec(
                        UILayoutType.MODULES_SINGLE,
                        { configuration.moduleLayout = InternalSettingsMod.ModuleLayout.SINGLE_COLUMN },
                        { configuration.moduleLayout == InternalSettingsMod.ModuleLayout.SINGLE_COLUMN }
                ),
                BindingSpec(
                        UILayoutType.MODULES_DOUBLE,
                        { configuration.moduleLayout = InternalSettingsMod.ModuleLayout.TWO_COLUMNS },
                        { configuration.moduleLayout == InternalSettingsMod.ModuleLayout.TWO_COLUMNS }
                ),
                BindingSpec(
                        UILayoutType.NOTIFICATION_TOP_LEFT,
                        { configuration.notificationCorner = InternalSettingsMod.NotificationCorner.TOP_LEFT },
                        { configuration.notificationCorner == InternalSettingsMod.NotificationCorner.TOP_LEFT }
                ),
                BindingSpec(
                        UILayoutType.NOTIFICATION_TOP_RIGHT,
                        { configuration.notificationCorner = InternalSettingsMod.NotificationCorner.TOP_RIGHT },
                        { configuration.notificationCorner == InternalSettingsMod.NotificationCorner.TOP_RIGHT }
                ),
                BindingSpec(
                        UILayoutType.NOTIFICATION_BOTTOM_LEFT,
                        { configuration.notificationCorner = InternalSettingsMod.NotificationCorner.BOTTOM_LEFT },
                        { configuration.notificationCorner == InternalSettingsMod.NotificationCorner.BOTTOM_LEFT }
                ),
                BindingSpec(
                        UILayoutType.NOTIFICATION_BOTTOM_RIGHT,
                        { configuration.notificationCorner = InternalSettingsMod.NotificationCorner.BOTTOM_RIGHT },
                        { configuration.notificationCorner == InternalSettingsMod.NotificationCorner.BOTTOM_RIGHT }
                ),
                BindingSpec(
                        UILayoutType.VISUAL_LIGHT,
                        { configuration.visualPreset = InternalSettingsMod.VisualPreset.LIGHT },
                        { configuration.visualPreset == InternalSettingsMod.VisualPreset.LIGHT }
                ),
                BindingSpec(
                        UILayoutType.VISUAL_DARK,
                        { configuration.visualPreset = InternalSettingsMod.VisualPreset.DARK },
                        { configuration.visualPreset == InternalSettingsMod.VisualPreset.DARK }
                ),
                BindingSpec(
                        UILayoutType.VISUAL_MODERN,
                        { configuration.visualPreset = InternalSettingsMod.VisualPreset.MODERN },
                        {
                            configuration.visualPreset == InternalSettingsMod.VisualPreset.MODERN ||
                                    configuration.visualPreset == InternalSettingsMod.VisualPreset.CLASSIC
                        }
                )
        )
    }
}

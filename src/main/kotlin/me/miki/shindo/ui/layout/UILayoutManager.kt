package me.miki.shindo.ui.layout

import me.miki.shindo.management.mods.impl.InternalSettingsMod
import me.miki.shindo.ui.layout.binding.DefaultUILayoutTypeBinding
import me.miki.shindo.ui.layout.binding.DefaultUILayoutTypeBindingRegistry
import me.miki.shindo.ui.layout.config.InternalSettingsLayoutConfiguration
import me.miki.shindo.ui.layout.enums.UILayoutArea
import me.miki.shindo.ui.layout.enums.UILayoutType
import me.miki.shindo.ui.layout.enums.UISettingsLayoutMode
import me.miki.shindo.ui.layout.integration.ColorManagerLayoutAppearanceIntegration
import me.miki.shindo.ui.layout.interfaces.UILayoutConfiguration
import me.miki.shindo.ui.layout.model.UILayoutPreset
import me.miki.shindo.ui.layout.model.UILayoutState
import me.miki.shindo.ui.layout.preset.DefaultUILayoutPresetResolver
import me.miki.shindo.ui.layout.preset.DefaultUILayoutPresetService
import me.miki.shindo.ui.layout.preset.DefaultUILayoutPresets
import me.miki.shindo.ui.layout.preset.InMemoryUILayoutPresetRepository
import me.miki.shindo.ui.layout.state.InternalSettingsLayoutStateGateway

/**
 * Public facade consumed by UI scenes.
 *
 * The old API is preserved while internals now delegate to modular services.
 */
class UILayoutManager(
    private val configuration: UILayoutConfiguration = InternalSettingsLayoutConfiguration()
) {

    private val stateGateway = InternalSettingsLayoutStateGateway(configuration)
    private val typeBindingRegistry = DefaultUILayoutTypeBindingRegistry(createTypeBindings())
    private val appearanceIntegration = ColorManagerLayoutAppearanceIntegration()
    private val presetRepository = InMemoryUILayoutPresetRepository(DefaultUILayoutPresets.createPresets())
    private val presetService = DefaultUILayoutPresetService(
        stateGateway = stateGateway,
        presetRepository = presetRepository,
        presetResolver = DefaultUILayoutPresetResolver(),
        appearanceIntegration = appearanceIntegration
    )

    private val typePresetMap = hashMapOf(
        UILayoutType.VISUAL_LIGHT to DefaultUILayoutPresets.PRESET_LIGHT,
        UILayoutType.VISUAL_DARK to DefaultUILayoutPresets.PRESET_DARK,
        UILayoutType.VISUAL_MODERN to DefaultUILayoutPresets.PRESET_MODERN
    )

    fun getTypes(area: UILayoutArea): List<UILayoutType> {
        return typeBindingRegistry.getTypes(area)
    }

    fun getSelectedType(area: UILayoutArea): UILayoutType? {
        return typeBindingRegistry.getSelectedType(area)
    }

    fun selectType(type: UILayoutType?) {
        if (type == null) {
            return
        }

        if (type.area == UILayoutArea.VISUAL && applyPresetForType(type)) {
            return
        }

        typeBindingRegistry.selectType(type)
    }

    fun isSelected(type: UILayoutType): Boolean {
        return typeBindingRegistry.isSelected(type)
    }

    fun getPresets(): List<UILayoutPreset> {
        return presetService.getPresets()
    }

    fun getActivePreset(): UILayoutPreset? {
        return presetService.getActivePreset()
    }

    fun getActivePresetId(): String? {
        return presetService.getActivePresetId()
    }

    fun applyPreset(id: String?): Boolean {
        if (id.isNullOrEmpty()) {
            return false
        }
        return presetService.applyPresetById(id)
    }

    fun getPresetIdForType(type: UILayoutType): String? {
        return typePresetMap[type]
    }

    fun applyPresetForType(type: UILayoutType): Boolean {
        val presetId = typePresetMap[type] ?: return false
        return applyPreset(presetId)
    }

    fun saveCurrentAsPreset(
        id: String,
        title: String,
        description: String,
        includeAppearance: Boolean = true
    ): UILayoutPreset {
        return presetService.saveCurrentAsCustomPreset(
            id = id,
            title = title,
            description = description,
            includeAppearance = includeAppearance
        )
    }

    fun removeCustomPreset(id: String): Boolean {
        return presetService.removeCustomPreset(id)
    }

    private fun createTypeBindings(): List<DefaultUILayoutTypeBinding> {
        return listOf(
            DefaultUILayoutTypeBinding(
                type = UILayoutType.SETTINGS_SINGLE,
                applier = { applyState { copy(settingsLayoutMode = UISettingsLayoutMode.SINGLE_COLUMN) } },
                selectedSupplier = { stateGateway.readState().settingsLayoutMode == UISettingsLayoutMode.SINGLE_COLUMN }
            ),
            DefaultUILayoutTypeBinding(
                type = UILayoutType.SETTINGS_DOUBLE,
                applier = { applyState { copy(settingsLayoutMode = UISettingsLayoutMode.DOUBLE_COLUMN) } },
                selectedSupplier = { stateGateway.readState().settingsLayoutMode == UISettingsLayoutMode.DOUBLE_COLUMN }
            ),
            DefaultUILayoutTypeBinding(
                type = UILayoutType.SETTINGS_ADAPTIVE,
                applier = { applyState { copy(settingsLayoutMode = UISettingsLayoutMode.STAGGERED_COLUMNS) } },
                selectedSupplier = { stateGateway.readState().settingsLayoutMode == UISettingsLayoutMode.STAGGERED_COLUMNS }
            ),
            DefaultUILayoutTypeBinding(
                type = UILayoutType.MODULES_SINGLE,
                applier = { applyState { copy(moduleLayout = InternalSettingsMod.ModuleLayout.SINGLE_COLUMN) } },
                selectedSupplier = { stateGateway.readState().moduleLayout == InternalSettingsMod.ModuleLayout.SINGLE_COLUMN }
            ),
            DefaultUILayoutTypeBinding(
                type = UILayoutType.MODULES_DOUBLE,
                applier = { applyState { copy(moduleLayout = InternalSettingsMod.ModuleLayout.TWO_COLUMNS) } },
                selectedSupplier = { stateGateway.readState().moduleLayout == InternalSettingsMod.ModuleLayout.TWO_COLUMNS }
            ),
            DefaultUILayoutTypeBinding(
                type = UILayoutType.NOTIFICATION_TOP_LEFT,
                applier = { applyState { copy(notificationCorner = InternalSettingsMod.NotificationCorner.TOP_LEFT) } },
                selectedSupplier = { stateGateway.readState().notificationCorner == InternalSettingsMod.NotificationCorner.TOP_LEFT }
            ),
            DefaultUILayoutTypeBinding(
                type = UILayoutType.NOTIFICATION_TOP_RIGHT,
                applier = { applyState { copy(notificationCorner = InternalSettingsMod.NotificationCorner.TOP_RIGHT) } },
                selectedSupplier = { stateGateway.readState().notificationCorner == InternalSettingsMod.NotificationCorner.TOP_RIGHT }
            ),
            DefaultUILayoutTypeBinding(
                type = UILayoutType.NOTIFICATION_BOTTOM_LEFT,
                applier = { applyState { copy(notificationCorner = InternalSettingsMod.NotificationCorner.BOTTOM_LEFT) } },
                selectedSupplier = { stateGateway.readState().notificationCorner == InternalSettingsMod.NotificationCorner.BOTTOM_LEFT }
            ),
            DefaultUILayoutTypeBinding(
                type = UILayoutType.NOTIFICATION_BOTTOM_RIGHT,
                applier = { applyState { copy(notificationCorner = InternalSettingsMod.NotificationCorner.BOTTOM_RIGHT) } },
                selectedSupplier = { stateGateway.readState().notificationCorner == InternalSettingsMod.NotificationCorner.BOTTOM_RIGHT }
            ),
            DefaultUILayoutTypeBinding(
                type = UILayoutType.VISUAL_LIGHT,
                applier = { applyState { copy(visualPreset = InternalSettingsMod.VisualPreset.LIGHT) } },
                selectedSupplier = { stateGateway.readState().visualPreset == InternalSettingsMod.VisualPreset.LIGHT }
            ),
            DefaultUILayoutTypeBinding(
                type = UILayoutType.VISUAL_DARK,
                applier = { applyState { copy(visualPreset = InternalSettingsMod.VisualPreset.DARK) } },
                selectedSupplier = { stateGateway.readState().visualPreset == InternalSettingsMod.VisualPreset.DARK }
            ),
            DefaultUILayoutTypeBinding(
                type = UILayoutType.VISUAL_MODERN,
                applier = { applyState { copy(visualPreset = InternalSettingsMod.VisualPreset.MODERN) } },
                selectedSupplier = {
                    val preset = stateGateway.readState().visualPreset
                    preset == InternalSettingsMod.VisualPreset.MODERN ||
                            preset == InternalSettingsMod.VisualPreset.CLASSIC
                }
            )
        )
    }

    private fun applyState(transform: UILayoutState.() -> UILayoutState) {
        val current = stateGateway.readState()
        stateGateway.applyState(current.transform())
    }
}

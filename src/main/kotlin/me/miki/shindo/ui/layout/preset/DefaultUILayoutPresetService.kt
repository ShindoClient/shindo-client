package me.miki.shindo.ui.layout.preset

import me.miki.shindo.ui.layout.interfaces.UILayoutAppearanceIntegration
import me.miki.shindo.ui.layout.interfaces.UILayoutPresetRepository
import me.miki.shindo.ui.layout.interfaces.UILayoutPresetResolver
import me.miki.shindo.ui.layout.interfaces.UILayoutPresetService
import me.miki.shindo.ui.layout.interfaces.UILayoutStateGateway
import me.miki.shindo.ui.layout.model.UILayoutPreset

class DefaultUILayoutPresetService(
    private val stateGateway: UILayoutStateGateway,
    private val presetRepository: UILayoutPresetRepository,
    private val presetResolver: UILayoutPresetResolver,
    private val appearanceIntegration: UILayoutAppearanceIntegration
) : UILayoutPresetService {

    override fun getPresets(): List<UILayoutPreset> {
        return presetRepository.getPresets()
    }

    override fun getActivePreset(): UILayoutPreset? {
        val state = stateGateway.readState()
        return presetResolver.resolveActivePreset(
            state = state,
            themeId = appearanceIntegration.getCurrentThemeId(),
            accentName = appearanceIntegration.getCurrentAccentName(),
            presets = presetRepository.getPresets()
        )
    }

    override fun getActivePresetId(): String? {
        return getActivePreset()?.id
    }

    override fun applyPresetById(id: String): Boolean {
        val preset = presetRepository.getPresetById(id) ?: return false
        stateGateway.applyState(preset.state)
        appearanceIntegration.applyAppearance(preset.themeId, preset.accentName)
        return true
    }

    override fun saveCurrentAsCustomPreset(
        id: String,
        title: String,
        description: String,
        includeAppearance: Boolean
    ): UILayoutPreset {
        val preset = UILayoutPreset(
            id = id,
            title = title,
            description = description,
            state = stateGateway.readState(),
            themeId = if (includeAppearance) appearanceIntegration.getCurrentThemeId() else null,
            accentName = if (includeAppearance) appearanceIntegration.getCurrentAccentName() else null,
            userDefined = true
        )
        presetRepository.saveCustomPreset(preset)
        return preset
    }

    override fun removeCustomPreset(id: String): Boolean {
        return presetRepository.removeCustomPreset(id)
    }
}


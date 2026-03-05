package me.miki.shindo.ui.layout.state

import me.miki.shindo.ui.layout.interfaces.UILayoutConfiguration
import me.miki.shindo.ui.layout.interfaces.UILayoutStateGateway
import me.miki.shindo.ui.layout.model.UILayoutState

/**
 * Bridges UILayoutState with InternalSettingsMod-backed configuration.
 */
class InternalSettingsLayoutStateGateway(
    private val configuration: UILayoutConfiguration
) : UILayoutStateGateway {

    override fun readState(): UILayoutState {
        return UILayoutState(
            settingsLayoutMode = configuration.settingsLayoutMode,
            moduleLayout = configuration.moduleLayout,
            notificationCorner = configuration.notificationCorner,
            visualPreset = configuration.visualPreset
        )
    }

    override fun applyState(state: UILayoutState) {
        configuration.settingsLayoutMode = state.settingsLayoutMode
        configuration.moduleLayout = state.moduleLayout
        configuration.notificationCorner = state.notificationCorner
        configuration.visualPreset = state.visualPreset
    }
}

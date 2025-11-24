package me.miki.shindo.gui.modmenu.category.impl.network.module;

import lombok.Getter;
import lombok.Setter;
import me.miki.shindo.gui.modmenu.category.impl.NetworkCategory;
import me.miki.shindo.gui.modmenu.category.impl.shared.SettingsPanel;
import me.miki.shindo.management.network.ConnectionTweakerManager;
import me.miki.shindo.management.network.ConnectionTweakerManager.ProfileSnapshot;
import me.miki.shindo.management.settings.Setting;
import me.miki.shindo.management.settings.impl.BooleanSetting;
import me.miki.shindo.ui.comp.impl.CompToggleButton;

import java.util.Collections;
import java.util.List;

@Getter
public class NetworkModuleContext {

    private final NetworkCategory category;

    private final SettingsPanel settingsPanel;

    @Setter
    private ConnectionTweakerManager manager;

    @Setter
    private ProfileSnapshot snapshot;

    @Setter
    private BooleanSetting warpSetting;

    @Setter
    private BooleanSetting optimizerSetting;

    @Setter
    private CompToggleButton optimizerToggle;

    @Setter
    private CompToggleButton warpToggle;

    @Setter
    private List<Setting> cachedSettings = Collections.emptyList();

    public NetworkModuleContext(NetworkCategory category, SettingsPanel settingsPanel) {
        this.category = category;
        this.settingsPanel = settingsPanel;
    }

}

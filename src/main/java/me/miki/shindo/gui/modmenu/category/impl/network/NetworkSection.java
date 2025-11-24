package me.miki.shindo.gui.modmenu.category.impl.network;

import lombok.Getter;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.nanovg.font.LegacyIcon;

/**
 * Sections available inside the {@link me.miki.shindo.gui.modmenu.category.impl.NetworkCategory}.
 */
public enum NetworkSection {
    TWEAKER(TranslateText.NETWORK_CATEGORY_OVERVIEW, LegacyIcon.NET),
    PROXY(TranslateText.NETWORK_PROXY_WARP, LegacyIcon.GLOBE);

    private final TranslateText label;
    @Getter
    private final String icon;

    NetworkSection(TranslateText label, String icon) {
        this.label = label;
        this.icon = icon;
    }

    public String getLabel() {
        return label.getText();
    }
}

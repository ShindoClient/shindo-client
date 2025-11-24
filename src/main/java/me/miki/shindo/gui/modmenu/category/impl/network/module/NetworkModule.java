package me.miki.shindo.gui.modmenu.category.impl.network.module;

import me.miki.shindo.gui.modmenu.category.impl.network.NetworkSection;
import me.miki.shindo.management.color.AccentColor;
import me.miki.shindo.management.color.palette.ColorPalette;
import me.miki.shindo.management.nanovg.NanoVGManager;

public interface NetworkModule {

    NetworkSection getSection();

    void init(NetworkModuleContext context);

    void draw(NetworkModuleContext context, NanoVGManager nvg, ColorPalette palette, AccentColor accent, int mouseX, int mouseY, float partialTicks, float contentTop, float contentHeight);

    default void mouseClicked(NetworkModuleContext context, int mouseX, int mouseY, int button) {
    }

    default void mouseReleased(NetworkModuleContext context, int mouseX, int mouseY, int button) {
    }

    default void keyTyped(NetworkModuleContext context, char typedChar, int keyCode) {
    }

    default void onSectionActivated(NetworkModuleContext context) {
    }
}

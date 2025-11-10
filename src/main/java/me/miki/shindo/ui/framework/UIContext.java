package me.miki.shindo.ui.framework;

import me.miki.shindo.Shindo;
import me.miki.shindo.management.color.AccentColor;
import me.miki.shindo.management.color.ColorManager;
import me.miki.shindo.management.color.palette.ColorPalette;
import me.miki.shindo.management.nanovg.NanoVGManager;

/**
 * Lightweight accessor that bundles together the rendering-related managers required by most UI components.
 * This keeps component code concise and centralises the way we fetch shared dependencies.
 */
public final class UIContext {

    private final NanoVGManager nanoVG;
    private final ColorManager colorManager;

    private UIContext(NanoVGManager nanoVG, ColorManager colorManager) {
        this.nanoVG = nanoVG;
        this.colorManager = colorManager;
    }

    public static UIContext get() {
        Shindo instance = Shindo.getInstance();
        return new UIContext(instance.getNanoVGManager(), instance.getColorManager());
    }

    public NanoVGManager nvg() {
        return nanoVG;
    }

    public ColorManager colors() {
        return colorManager;
    }

    public ColorPalette palette() {
        return colorManager.getPalette();
    }

    public AccentColor accent() {
        return colorManager.getCurrentColor();
    }
}

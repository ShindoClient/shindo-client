package me.miki.shindo.ui.theme;

import me.miki.shindo.Shindo;
import me.miki.shindo.management.color.AccentColor;
import me.miki.shindo.management.color.ColorManager;
import me.miki.shindo.management.color.palette.ColorPalette;
import me.miki.shindo.management.color.palette.ColorType;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.management.nanovg.font.Fonts;
import me.miki.shindo.utils.ColorUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

import java.awt.Color;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Shared renderer that skins vanilla {@link GuiButton} instances using the client theme.
 * Keeps the mixins lean and ensures both buttons and sliders remain visually consistent.
 */
public final class VanillaButtonRenderer {

    private VanillaButtonRenderer() {
    }

    public static boolean drawButton(GuiButton button,
                                     int x, int y, int width, int height,
                                     boolean enabled, boolean visible, boolean hovered,
                                     int packedForegroundColor,
                                     Minecraft mc, int mouseX, int mouseY,
                                     Consumer<ButtonThemeContext> afterBackground) {
        Objects.requireNonNull(button, "button");

        if (!visible) {
            return false;
        }

        Shindo instance = Shindo.getInstance();
        if (instance == null) {
            return false;
        }

        ColorManager colorManager = instance.getColorManager();
        NanoVGManager nvg = instance.getNanoVGManager();

        if (colorManager == null || nvg == null) {
            return false;
        }

        ColorPalette palette = colorManager.getPalette();
        AccentColor accent = colorManager.getCurrentColor();

        if (palette == null || accent == null) {
            return false;
        }

        float radius = Math.min(6F, height / 2F);
        float textSize = Math.max(9F, Math.min(12F, height - 6F));

        nvg.setupAndDraw(() -> {
            Color base = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID),
                    enabled ? (hovered ? 210 : 190) : 135);
            Color overlayStart = ColorUtils.applyAlpha(accent.getColor1(),
                    enabled ? (hovered ? 110 : 82) : 45);
            Color overlayEnd = ColorUtils.applyAlpha(accent.getColor2(),
                    enabled ? (hovered ? 110 : 82) : 45);

            nvg.drawRoundedRect(x, y, width, height, radius, base);
            nvg.drawGradientRoundedRect(x, y, width, height, radius, overlayStart, overlayEnd);

            if (hovered && enabled) {
                nvg.drawOutlineRoundedRect(x, y, width, height, radius, 1.1F,
                        ColorUtils.applyAlpha(accent.getColor2(), 160));
            }

            if (afterBackground != null) {
                afterBackground.accept(new ButtonThemeContext(button, nvg, palette, accent, hovered, textSize, x, y, width, height));
            }

            String label = button.displayString;
            if (label != null && !label.isEmpty()) {
                Color textColor = resolveTextColor(enabled, palette, hovered, packedForegroundColor);
                nvg.drawCenteredText(label, x + (width / 2F),
                        y + (height / 2F) - (textSize / 2F),
                        textColor, textSize, Fonts.MEDIUM);
            }
        }, true);

        return true;
    }

    private static Color resolveTextColor(boolean enabled, ColorPalette palette, boolean hovered, int packedForegroundColor) {
        if (packedForegroundColor != 0) {
            return new Color(packedForegroundColor, true);
        }

        if (!enabled) {
            return ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 150);
        }

        if (hovered) {
            return Color.WHITE;
        }

        return palette.getFontColor(ColorType.DARK);
    }

    public static final class ButtonThemeContext {
        private final GuiButton button;
        private final NanoVGManager nvg;
        private final ColorPalette palette;
        private final AccentColor accent;
        private final boolean hovered;
        private final float textSize;
        private final int x;
        private final int y;
        private final int width;
        private final int height;

        private ButtonThemeContext(GuiButton button, NanoVGManager nvg, ColorPalette palette, AccentColor accent,
                                   boolean hovered, float textSize, int x, int y, int width, int height) {
            this.button = button;
            this.nvg = nvg;
            this.palette = palette;
            this.accent = accent;
            this.hovered = hovered;
            this.textSize = textSize;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public GuiButton getButton() {
            return button;
        }

        public NanoVGManager getNvg() {
            return nvg;
        }

        public ColorPalette getPalette() {
            return palette;
        }

        public AccentColor getAccent() {
            return accent;
        }

        public boolean isHovered() {
            return hovered;
        }

        public float getTextSize() {
            return textSize;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }
    }
}

package me.miki.shindo.ui.comp.impl;

import lombok.Setter;
import me.miki.shindo.management.color.AccentColor;
import me.miki.shindo.management.color.palette.ColorPalette;
import me.miki.shindo.management.color.palette.ColorType;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.management.nanovg.font.Fonts;
import me.miki.shindo.ui.comp.Comp;
import me.miki.shindo.ui.framework.UIContext;
import me.miki.shindo.utils.ColorUtils;
import me.miki.shindo.utils.mouse.MouseUtils;

import java.awt.Color;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Lightweight icon button that uses the current accent colours. Intended for scenarios
 * like the Mod Menu side bar, small toolbar buttons and quick actions.
 */
public class CompIconButton extends Comp {

    private final Supplier<String> iconSupplier;
    private Runnable onClick;
    private Supplier<Boolean> enabledSupplier;

    @Setter
    private float radius = 6F;
    @Setter
    private float iconSize = 12F;
    @Setter
    private float fontSize = 12F;
    @Setter
    private Color overrideBackground;
    @Setter
    private Supplier<Color> iconColorSupplier;

    public CompIconButton(float x, float y, float size, Supplier<String> iconSupplier) {
        super(x, y);
        this.iconSupplier = Objects.requireNonNull(iconSupplier, "iconSupplier");
        setWidth(size);
        setHeight(size);
    }

    public CompIconButton(float size, Supplier<String> iconSupplier) {
        this(0F, 0F, size, iconSupplier);
    }

    public CompIconButton onClick(Runnable runnable) {
        this.onClick = runnable;
        return this;
    }

    public CompIconButton enabledWhen(Supplier<Boolean> enabledSupplier) {
        this.enabledSupplier = enabledSupplier;
        return this;
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {

        if (!isVisible()) {
            return;
        }

        UIContext ctx = ctx();
        NanoVGManager nvg = ctx.nvg();
        ColorPalette palette = ctx.palette();
        AccentColor accent = ctx.accent();

        boolean enabled = enabledSupplier == null || enabledSupplier.get();
        boolean hovered = enabled && MouseUtils.isInside(mouseX, mouseY, getX(), getY(), getWidth(), getHeight());

        Color baseBackground;
        if (overrideBackground != null) {
            baseBackground = overrideBackground;
        } else {
            baseBackground = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), enabled ? 190 : 120);
        }

        Color start = ColorUtils.applyAlpha(accent.getColor1(), enabled ? (hovered ? 210 : 180) : 90);
        Color end = ColorUtils.applyAlpha(accent.getColor2(), enabled ? (hovered ? 210 : 180) : 90);

        nvg.drawRoundedRect(getX(), getY(), getWidth(), getHeight(), radius, baseBackground);
        nvg.drawGradientRoundedRect(getX(), getY(), getWidth(), getHeight(), radius, start, end);

        Color iconColor = iconColorSupplier != null ? iconColorSupplier.get() : new Color(255, 255, 255, enabled ? 255 : 155);
        String icon = iconSupplier.get();
        if (icon != null) {
            float centerX = getX() + (getWidth() / 2F);
            float centerY = getY() + (getHeight() / 2F) - (nvg.getTextHeight(icon, fontSize, Fonts.LEGACYICON) / 2F);
            nvg.drawText(icon, centerX - (nvg.getTextWidth(icon, fontSize, Fonts.LEGACYICON) / 2F), centerY, iconColor, fontSize, Fonts.LEGACYICON);
        }

        super.draw(mouseX, mouseY, partialTicks);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (!isVisible()) {
            return;
        }
        boolean enabled = enabledSupplier == null || enabledSupplier.get();
        if (enabled && mouseButton == 0 && onClick != null && isHovered(mouseX, mouseY)) {
            onClick.run();
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }
}

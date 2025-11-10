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
 * Shared card-like component used across settings screens to render a title, description
 * and optional trailing control (slider, toggle, keybind etc.). This consolidates the
 * repeated NanoVG drawing code that previously lived in each scene.
 */
public class CompSettingButton extends Comp {

    private static final float DEFAULT_HEIGHT = 52F;
    private static final float DEFAULT_RADIUS = 9F;
    private static final float TEXT_TITLE_SIZE = 10.5F;
    private static final float TEXT_DESCRIPTION_SIZE = 8.5F;
    private static final float TEXT_STATUS_SIZE = 8.5F;

    private final Supplier<String> titleSupplier;
    private final Supplier<String> descriptionSupplier;

    private Supplier<String> statusSupplier;
    private Supplier<Color> statusColorSupplier;
    private Runnable onClick;
    private Comp trailingComp;

    @Setter
    private float paddingLeft = 16F;
    @Setter
    private float paddingRight = 18F;
    @Setter
    private float paddingVertical = 15F;
    @Setter
    private boolean drawShadow = true;
    @Setter
    private float shadowStrength = 6F;
    @Setter
    private float shadowRadius = 8F;

    public CompSettingButton(float x, float y, float width, Supplier<String> titleSupplier, Supplier<String> descriptionSupplier) {
        super(x, y);
        this.titleSupplier = Objects.requireNonNull(titleSupplier, "titleSupplier");
        this.descriptionSupplier = Objects.requireNonNull(descriptionSupplier, "descriptionSupplier");
        setWidth(width);
        setHeight(DEFAULT_HEIGHT);
    }

    public CompSettingButton(float width, Supplier<String> titleSupplier, Supplier<String> descriptionSupplier) {
        this(0F, 0F, width, titleSupplier, descriptionSupplier);
    }

    public CompSettingButton onClick(Runnable onClick) {
        this.onClick = onClick;
        return this;
    }

    public CompSettingButton trailing(Comp comp) {
        this.trailingComp = comp;
        return this;
    }

    public CompSettingButton status(Supplier<String> textSupplier, Supplier<Color> colorSupplier) {
        this.statusSupplier = textSupplier;
        this.statusColorSupplier = colorSupplier;
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

        float x = getX();
        float y = getY();
        float width = getWidth();
        float height = getHeight();

        boolean hovered = MouseUtils.isInside(mouseX, mouseY, x, y, width, height);

        Color base = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), hovered ? 210 : 188);
        Color overlayStart = ColorUtils.applyAlpha(accent.getColor1(), hovered ? 62 : 38);
        Color overlayEnd = ColorUtils.applyAlpha(accent.getColor2(), hovered ? 62 : 38);

        if (drawShadow) {
            nvg.drawShadow(x, y, width, height, shadowRadius, (int) shadowStrength);
        }

        nvg.drawRoundedRect(x, y, width, height, DEFAULT_RADIUS, base);
        nvg.drawGradientRoundedRect(x, y, width, height, DEFAULT_RADIUS, overlayStart, overlayEnd);

        float availableTextWidth = width - paddingLeft - paddingRight;
        if (trailingComp != null) {
            availableTextWidth -= Math.max(0F, trailingComp.getWidth());
            availableTextWidth -= 12F;
        }

        float titleY = y + paddingVertical - 4F;
        float descriptionY = titleY + 13F;

        String title = nvg.getLimitText(titleSupplier.get(), TEXT_TITLE_SIZE, Fonts.MEDIUM, Math.max(48F, availableTextWidth));
        String description = descriptionSupplier.get();
        if (description != null && !"null".equalsIgnoreCase(description)) {
            description = nvg.getLimitText(description, TEXT_DESCRIPTION_SIZE, Fonts.REGULAR, Math.max(48F, availableTextWidth));
        } else {
            description = "";
        }

        nvg.drawText(title, x + paddingLeft, titleY, palette.getFontColor(ColorType.DARK), TEXT_TITLE_SIZE, Fonts.MEDIUM);
        if (!description.isEmpty()) {
            nvg.drawText(description, x + paddingLeft, descriptionY, palette.getFontColor(ColorType.NORMAL), TEXT_DESCRIPTION_SIZE, Fonts.REGULAR);
        }

        if (statusSupplier != null && statusColorSupplier != null) {
            String status = statusSupplier.get();
            if (status != null && !status.isEmpty()) {
                float statusY = y + height - paddingVertical + 4F;
                nvg.drawText(status, x + paddingLeft, statusY, statusColorSupplier.get(), TEXT_STATUS_SIZE, Fonts.MEDIUM);
            }
        }

        if (trailingComp != null) {
            float trailingX = x + width - paddingRight - trailingComp.getWidth();
            float trailingY = y + (height - trailingComp.getHeight()) / 2F;
            trailingComp.setX(trailingX);
            trailingComp.setY(trailingY);
            trailingComp.draw(mouseX, mouseY, partialTicks);
        }

        super.draw(mouseX, mouseY, partialTicks);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (!isVisible()) {
            return;
        }
        boolean insideTrailing = false;
        if (trailingComp != null) {
            insideTrailing = MouseUtils.isInside(mouseX, mouseY, trailingComp.getX(), trailingComp.getY(), trailingComp.getWidth(), trailingComp.getHeight());
        }

        if (!insideTrailing && mouseButton == 0 && onClick != null && isHovered(mouseX, mouseY)) {
            onClick.run();
        }

        if (trailingComp != null) {
            trailingComp.mouseClicked(mouseX, mouseY, mouseButton);
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        if (!isVisible()) {
            return;
        }
        if (trailingComp != null) {
            trailingComp.mouseReleased(mouseX, mouseY, mouseButton);
        }
        super.mouseReleased(mouseX, mouseY, mouseButton);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (!isVisible()) {
            return;
        }
        if (trailingComp != null) {
            trailingComp.keyTyped(typedChar, keyCode);
        }
        super.keyTyped(typedChar, keyCode);
    }
}

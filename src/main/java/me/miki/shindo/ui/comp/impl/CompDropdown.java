package me.miki.shindo.ui.comp.impl;

import lombok.Getter;
import lombok.Setter;
import me.miki.shindo.management.color.AccentColor;
import me.miki.shindo.management.color.palette.ColorPalette;
import me.miki.shindo.management.color.palette.ColorType;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.management.nanovg.font.Fonts;
import me.miki.shindo.management.nanovg.font.LegacyIcon;
import me.miki.shindo.management.settings.impl.ComboSetting;
import me.miki.shindo.management.settings.impl.combo.Option;
import me.miki.shindo.ui.comp.Comp;
import me.miki.shindo.ui.framework.UIContext;
import me.miki.shindo.utils.ColorUtils;
import me.miki.shindo.utils.mouse.MouseUtils;

import java.awt.*;
import java.util.List;

public class CompDropdown extends Comp {

    private static final float CONTROL_HEIGHT = 20F;
    private static final float OPTION_HEIGHT = 18F;
    private static final float LIST_PADDING = 4F;

    private final ComboSetting setting;
    @Setter
    @Getter
    private float width;
    private boolean open;
    @Setter
    private boolean openUp;

    public CompDropdown(float x, float y, float width, ComboSetting setting) {
        super(x, y);
        this.setting = setting;
        this.width = width;
        super.setWidth(width);
        super.setHeight(CONTROL_HEIGHT);
    }

    public CompDropdown(float width, ComboSetting setting) {
        this(0F, 0F, width, setting);
    }

    public float getControlHeight() {
        return CONTROL_HEIGHT;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public float getDropdownHeight() {
        return open ? LIST_PADDING * 2F + Math.max(0, getOptionCount()) * OPTION_HEIGHT : 0F;
    }

    private int getOptionCount() {
        return setting != null && setting.getOptions() != null ? setting.getOptions().size() : 0;
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        UIContext ctx = ctx();
        NanoVGManager nvg = ctx.nvg();
        AccentColor accent = ctx.accent();
        ColorPalette palette = ctx.palette();

        float controlHeight = CONTROL_HEIGHT;
        float dropdownHeight = getDropdownHeight();

        super.setWidth(width);
        super.setHeight(controlHeight + dropdownHeight);

        float x = getX();
        float y = getY();

        nvg.drawGradientRoundedRect(x, y, width, controlHeight, 5F, accent.getColor1(), accent.getColor2());

        String label = setting != null && setting.getOption() != null ? setting.getOption().getName() : "None";
        nvg.drawText(label, x + 8F, y + 6F, Color.WHITE, 8.5F, Fonts.MEDIUM);

        String arrow = open ? LegacyIcon.CHEVRON_UP : LegacyIcon.CHEVRON_DOWN;
        nvg.drawText(arrow, x + width - 16F, y + 4F, Color.WHITE, 10F, Fonts.LEGACYICON);

        if (open && getOptionCount() > 0) {
            float listX = x;
            float listHeight = dropdownHeight;
            float listY = openUp ? y - listHeight - 4F : y + controlHeight + 4F;

            nvg.drawRoundedRect(listX, listY, width, listHeight, 5F,
                    ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 240));

            List<Option> options = setting.getOptions();
            for (int i = 0; i < options.size(); i++) {
                Option option = options.get(i);
                float optionY = listY + LIST_PADDING + i * OPTION_HEIGHT;
                float optionHeight = OPTION_HEIGHT - 2F;
                boolean hovered = MouseUtils.isInside(mouseX, mouseY, listX + 2F, optionY, width - 4F, optionHeight);
                if (hovered) {
                    nvg.drawRoundedRect(listX + 2F, optionY, width - 4F, optionHeight, 4F,
                            ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 220));
                }

                Color textColor = option.equals(setting.getOption())
                        ? palette.getFontColor(ColorType.DARK)
                        : palette.getFontColor(ColorType.NORMAL);
                nvg.drawText(option.getName(), listX + 8F, optionY + 5F, textColor, 8F, Fonts.REGULAR);
            }
        }

        super.draw(mouseX, mouseY, partialTicks);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton != 0) {
            return;
        }

        float controlHeight = CONTROL_HEIGHT;
        float listX = getX();
        float listHeight = getDropdownHeight();
        float listY = openUp ? getY() - listHeight - 4F : getY() + controlHeight + 4F;
        float listWidth = width;

        if (MouseUtils.isInside(mouseX, mouseY, getX(), getY(), width, controlHeight)) {
            open = !open;
            return;
        }

        if (open && MouseUtils.isInside(mouseX, mouseY, listX, listY, listWidth, listHeight)) {
            selectOptionAt(mouseX, mouseY);
            open = false;
            return;
        }

        open = false;
    }

    private void selectOptionAt(int mouseX, int mouseY) {
        if (setting == null) {
            return;
        }
        List<Option> options = setting.getOptions();
        if (options == null || options.isEmpty()) {
            return;
        }

        float optionX = getX() + 2F;
        float listHeight = getDropdownHeight();
        float optionY = (openUp ? getY() - listHeight - 4F : getY() + CONTROL_HEIGHT + 4F) + LIST_PADDING;

        for (Option option : options) {
            if (MouseUtils.isInside(mouseX, mouseY, optionX, optionY, width - 4F, OPTION_HEIGHT - 2F)) {
                setting.setOption(option);
                break;
            }
            optionY += OPTION_HEIGHT;
        }
    }
}

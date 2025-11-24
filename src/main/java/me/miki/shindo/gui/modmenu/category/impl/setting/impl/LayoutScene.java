package me.miki.shindo.gui.modmenu.category.impl.setting.impl;

import me.miki.shindo.Shindo;
import me.miki.shindo.gui.modmenu.category.impl.SettingCategory;
import me.miki.shindo.gui.modmenu.category.impl.setting.SettingScene;
import me.miki.shindo.gui.modmenu.category.impl.shared.SettingsPanel;
import me.miki.shindo.management.color.AccentColor;
import me.miki.shindo.management.color.ColorManager;
import me.miki.shindo.management.color.palette.ColorPalette;
import me.miki.shindo.management.color.palette.ColorType;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.impl.InternalSettingsMod;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.management.nanovg.font.Fonts;
import me.miki.shindo.management.nanovg.font.LegacyIcon;
import me.miki.shindo.management.screenshot.ScreenshotDisplayMode;
import me.miki.shindo.management.settings.impl.ComboSetting;
import me.miki.shindo.ui.comp.impl.CompDropdown;
import me.miki.shindo.utils.ColorUtils;
import me.miki.shindo.utils.mouse.MouseUtils;
import me.miki.shindo.utils.mouse.Scroll;

import java.awt.*;

public class LayoutScene extends SettingScene {

    private static final float VERTICAL_PADDING_TOP = 28F;
    private static final float VERTICAL_PADDING_BOTTOM = 24F;
    private static final float HORIZONTAL_PADDING = 22F;
    private static final float SECTION_SPACING = 32F;
    private static final float INFO_BLOCK_HEIGHT = 46F;
    private static final float DROPDOWN_SPACING = 16F;
    private static final float DROPDOWN_CONTROL_HEIGHT = 22F;
    private static final float PREVIEW_RADIUS = 12F;
    private static final float LAYOUT_PREVIEW_HEIGHT = 164F;
    private static final float MODULE_PREVIEW_HEIGHT = 158F;
    private static final float SCREENSHOT_PREVIEW_HEIGHT = 140F;

    private final Scroll contentScroll = new Scroll();
    private CompDropdown layoutDropdown;
    private CompDropdown moduleDropdown;
    private CompDropdown screenshotDropdown;

    public LayoutScene(SettingCategory parent) {
        super(parent, TranslateText.SETTINGS_LAYOUT_TITLE, TranslateText.SETTINGS_LAYOUT_DESCRIPTION, LegacyIcon.GRID);
    }

    @Override
    public void initGui() {
        contentScroll.resetAll();
        layoutDropdown = createDropdown(InternalSettingsMod.getInstance().getSettingsLayoutSetting());
        moduleDropdown = createDropdown(InternalSettingsMod.getInstance().getModuleLayoutSetting());
        screenshotDropdown = createDropdown(InternalSettingsMod.getInstance().getScreenshotDisplaySetting());
    }

    private CompDropdown createDropdown(ComboSetting setting) {
        return setting == null ? null : new CompDropdown(0F, 0F, 0F, setting);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        NanoVGManager nvg = Shindo.getInstance().getNanoVGManager();
        ColorManager colorManager = Shindo.getInstance().getColorManager();
        ColorPalette palette = colorManager.getPalette();
        AccentColor accent = colorManager.getCurrentColor();

        float baseX = getX();
        float baseY = getContentY();
        float baseWidth = getWidth();
        float baseHeight = getContentHeight();

        if (baseWidth <= 0F || baseHeight <= 0F) {
            return;
        }

        SettingsPanel.LayoutMode layoutMode = InternalSettingsMod.getInstance().getSettingsLayoutMode();
        int moduleColumns = InternalSettingsMod.getInstance().getModuleGridColumns();
        ScreenshotDisplayMode screenshotMode = InternalSettingsMod.getInstance().getScreenshotDisplayMode();

        float layoutSectionHeight = calculateSectionHeight(LAYOUT_PREVIEW_HEIGHT, layoutDropdown);
        float moduleSectionHeight = calculateSectionHeight(MODULE_PREVIEW_HEIGHT, moduleDropdown);
        float screenshotSectionHeight = calculateSectionHeight(SCREENSHOT_PREVIEW_HEIGHT, screenshotDropdown);
        float contentHeight = VERTICAL_PADDING_TOP + layoutSectionHeight + SECTION_SPACING + moduleSectionHeight + SECTION_SPACING + screenshotSectionHeight + VERTICAL_PADDING_BOTTOM;

        contentScroll.setMaxScroll(Math.max(0F, contentHeight - baseHeight));
        if (MouseUtils.isInside(mouseX, mouseY, baseX, baseY, baseWidth, baseHeight)) {
            contentScroll.onScroll();
        }
        contentScroll.onAnimation();

        float scrollValue = contentScroll.getValue();
        float currentY = baseY + VERTICAL_PADDING_TOP + scrollValue;
        float innerX = baseX + HORIZONTAL_PADDING;
        float innerWidth = baseWidth - (HORIZONTAL_PADDING * 2F);

        drawContainerBackground(nvg, palette, baseX, baseY, baseWidth, baseHeight);

        nvg.save();
        nvg.scissor(baseX, baseY, baseWidth, baseHeight);

        currentY += drawLayoutSection(nvg, palette, accent, innerX, currentY, innerWidth, layoutMode, mouseX, mouseY, partialTicks);
        currentY += SECTION_SPACING;
        currentY += drawModuleSection(nvg, palette, accent, innerX, currentY, innerWidth, moduleColumns, mouseX, mouseY, partialTicks);
        currentY += SECTION_SPACING;
        drawScreenshotSection(nvg, palette, accent, innerX, currentY, innerWidth, screenshotMode, mouseX, mouseY, partialTicks);

        nvg.restore();

        drawScrollbar(nvg, palette, accent, baseX, baseY, baseWidth, baseHeight, contentHeight, scrollValue);
    }

    private void drawContainerBackground(NanoVGManager nvg, ColorPalette palette, float x, float y, float width, float height) {
        float radius = 12F;
        nvg.drawShadow(x, y, width, height, radius, 6);
        nvg.drawRoundedRect(x, y, width, height, radius, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 210));
        nvg.drawRoundedRect(x + 1F, y + 1F, width - 2F, height - 2F, radius - 1F,
                ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 230));
    }

    private float drawLayoutSection(NanoVGManager nvg, ColorPalette palette, AccentColor accent, float x, float startY, float width,
                                    SettingsPanel.LayoutMode layoutMode, int mouseX, int mouseY, float partialTicks) {
        float sectionHeight = calculateSectionHeight(LAYOUT_PREVIEW_HEIGHT, layoutDropdown);
        float labelY = startY;

        nvg.drawText(TranslateText.SETTINGS_LAYOUT_SECTION_LAYOUT.getText(), x, labelY, palette.getFontColor(ColorType.DARK), 12.5F, Fonts.MEDIUM);
        TranslateText titleKey = layoutMode == SettingsPanel.LayoutMode.SINGLE_COLUMN
                ? TranslateText.SETTINGS_LAYOUT_SINGLE_TITLE
                : TranslateText.SETTINGS_LAYOUT_DOUBLE_TITLE;
        TranslateText descriptionKey = layoutMode == SettingsPanel.LayoutMode.SINGLE_COLUMN
                ? TranslateText.SETTINGS_LAYOUT_SINGLE_DESCRIPTION
                : TranslateText.SETTINGS_LAYOUT_DOUBLE_DESCRIPTION;

        nvg.drawText(titleKey.getText(), x, labelY + 16F, palette.getFontColor(ColorType.DARK), 11F, Fonts.MEDIUM);
        nvg.drawText(descriptionKey.getText(), x, labelY + 30F, ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 200), 8.5F, Fonts.REGULAR);

        float previewY = labelY + INFO_BLOCK_HEIGHT;
        drawLayoutPreview(nvg, palette, accent, x, previewY, width, LAYOUT_PREVIEW_HEIGHT, layoutMode);

        float dropdownY = previewY + LAYOUT_PREVIEW_HEIGHT + DROPDOWN_SPACING;
        if (layoutDropdown != null) {
            float dropdownWidth = Math.min(280F, width);
            setDropdownBounds(layoutDropdown, x, dropdownY, dropdownWidth);
            layoutDropdown.draw(mouseX, mouseY, partialTicks);
        }

        return sectionHeight;
    }

    private float drawModuleSection(NanoVGManager nvg, ColorPalette palette, AccentColor accent, float x, float startY, float width,
                                    int moduleColumns, int mouseX, int mouseY, float partialTicks) {
        float sectionHeight = calculateSectionHeight(MODULE_PREVIEW_HEIGHT, moduleDropdown);

        nvg.drawText(TranslateText.SETTINGS_LAYOUT_SECTION_MODULE.getText(), x, startY, palette.getFontColor(ColorType.DARK), 12.5F, Fonts.MEDIUM);

        TranslateText titleKey;
        TranslateText descriptionKey;
        if (moduleColumns >= 2) {
            titleKey = TranslateText.SETTINGS_LAYOUT_MODULE_DOUBLE_TITLE;
            descriptionKey = TranslateText.SETTINGS_LAYOUT_MODULE_DOUBLE_DESCRIPTION;
        } else {
            titleKey = TranslateText.SETTINGS_LAYOUT_MODULE_SINGLE_TITLE;
            descriptionKey = TranslateText.SETTINGS_LAYOUT_MODULE_SINGLE_DESCRIPTION;
        }

        nvg.drawText(titleKey.getText(), x, startY + 16F, palette.getFontColor(ColorType.DARK), 11F, Fonts.MEDIUM);
        nvg.drawText(descriptionKey.getText(), x, startY + 30F, ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 200), 8.5F, Fonts.REGULAR);

        float previewY = startY + INFO_BLOCK_HEIGHT;
        drawModulePreview(nvg, palette, accent, x, previewY, width, MODULE_PREVIEW_HEIGHT, moduleColumns);

        float dropdownY = previewY + MODULE_PREVIEW_HEIGHT + DROPDOWN_SPACING;
        if (moduleDropdown != null) {
            float dropdownWidth = Math.min(280F, width);
            setDropdownBounds(moduleDropdown, x, dropdownY, dropdownWidth);
            moduleDropdown.draw(mouseX, mouseY, partialTicks);
        }

        return sectionHeight;
    }

    private void drawScreenshotSection(NanoVGManager nvg, ColorPalette palette, AccentColor accent, float x, float startY, float width,
                                       ScreenshotDisplayMode mode, int mouseX, int mouseY, float partialTicks) {
        float sectionHeight = calculateSectionHeight(SCREENSHOT_PREVIEW_HEIGHT, screenshotDropdown);
        nvg.drawText(TranslateText.SETTINGS_LAYOUT_SECTION_SCREENSHOT.getText(), x, startY, palette.getFontColor(ColorType.DARK), 12.5F, Fonts.MEDIUM);

        String title = mode.getTranslate().getText();
        String description = mode.getDescription();
        nvg.drawText(title, x, startY + 16F, palette.getFontColor(ColorType.DARK), 11F, Fonts.MEDIUM);
        nvg.drawText(description, x, startY + 30F, ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 200), 8.5F, Fonts.REGULAR);

        float previewY = startY + INFO_BLOCK_HEIGHT;
        drawScreenshotPreview(nvg, palette, accent, x, previewY, width, SCREENSHOT_PREVIEW_HEIGHT, mode);

        float dropdownY = previewY + SCREENSHOT_PREVIEW_HEIGHT + DROPDOWN_SPACING;
        if (screenshotDropdown != null) {
            float dropdownWidth = Math.min(300F, width);
            setDropdownBounds(screenshotDropdown, x, dropdownY, dropdownWidth);
            screenshotDropdown.draw(mouseX, mouseY, partialTicks);
        }
    }

    private void setDropdownBounds(CompDropdown dropdown, float x, float y, float width) {
        dropdown.setX(x);
        dropdown.setY(y);
        dropdown.setWidth(width);
    }

    private float calculateSectionHeight(float previewHeight, CompDropdown dropdown) {
        float dropdownHeight = dropdown != null ? (DROPDOWN_CONTROL_HEIGHT + dropdown.getDropdownHeight()) : DROPDOWN_CONTROL_HEIGHT;
        return INFO_BLOCK_HEIGHT + previewHeight + DROPDOWN_SPACING + dropdownHeight + 12F;
    }

    private void drawLayoutPreview(NanoVGManager nvg, ColorPalette palette, AccentColor accent, float x, float y, float width, float height, SettingsPanel.LayoutMode layoutMode) {
        Color base = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 170);
        Color cardColor = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 210);
        Color detailColor = ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 220);

        nvg.drawRoundedRect(x, y, width, height, PREVIEW_RADIUS, base);
        //nvg.drawRoundedRect(x + 14F, y + 12F, width - 28F, 10F, 4F, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 140));

        int columns = layoutMode == SettingsPanel.LayoutMode.SINGLE_COLUMN ? 1 : 2;
        int rows = 3;
        float padding = 18F;
        float columnGap = 12F;
        float rowGap = 12F;
        float cardHeight = 34F;
        float columnWidth = (width - (padding * 2F) - ((columns - 1) * columnGap)) / columns;
        columnWidth = Math.max(72F, columnWidth);

        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                float cardX = x + padding + column * (columnWidth + columnGap);
                float cardY = y + padding + row * (cardHeight + rowGap);
                nvg.drawRoundedRect(cardX, cardY, columnWidth, cardHeight, 8F, cardColor);

                float headerWidth = columnWidth - 16F;
                nvg.drawRoundedRect(cardX + 8F, cardY + 7F, headerWidth, 6F, 3F, detailColor);
                nvg.drawRoundedRect(cardX + 8F, cardY + 18F, headerWidth * 0.7F, 4F, 2F, detailColor);
            }
        }

        //nvg.drawGradientRoundedRect(x + padding, y + height - 18F, width - (padding * 2F), 5F, 3F, ColorUtils.applyAlpha(accent.getColor1(), 160), ColorUtils.applyAlpha(accent.getColor2(), 160));
    }

    private void drawModulePreview(NanoVGManager nvg, ColorPalette palette, AccentColor accent, float x, float y, float width, float height, int columns) {
        columns = Math.max(1, Math.min(columns, 2));
        Color base = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 170);
        Color cardColor = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 220);
        Color pillColor = ColorUtils.applyAlpha(palette.getFontColor(ColorType.DARK), 230);

        nvg.drawRoundedRect(x, y, width, height, PREVIEW_RADIUS, base);

        int rows = 3;
        float padding = 16F;
        float columnGap = 14F;
        float rowGap = 12F;
        float cardHeight = 32F;
        float columnWidth = (width - (padding * 2F) - ((columns - 1) * columnGap)) / columns;
        columnWidth = Math.max(60F, columnWidth);

        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                float cardX = x + padding + column * (columnWidth + columnGap);
                float cardY = y + padding + row * (cardHeight + rowGap);
                nvg.drawRoundedRect(cardX, cardY, columnWidth, cardHeight, 6F, cardColor);
                nvg.drawRoundedRect(cardX + 9F, cardY + 11F, columnWidth - 34F, 6F, 3F, pillColor);
                nvg.drawRoundedRect(cardX + columnWidth - 20F, cardY + 10F, 12F, 12F, 6F, ColorUtils.applyAlpha(accent.getColor1(), 200));
            }
        }
    }

    private void drawScreenshotPreview(NanoVGManager nvg, ColorPalette palette, AccentColor accent, float x, float y, float width,
                                       float height, ScreenshotDisplayMode mode) {
        Color background = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 140);
        nvg.drawRoundedRect(x, y, width, height, PREVIEW_RADIUS, background);

        if (mode == ScreenshotDisplayMode.GRID) {
            drawGridPreview(nvg, palette, accent, x, y, width, height);
        } else {
            drawFilmstripPreview(nvg, palette, accent, x, y, width, height);
        }
    }

    private void drawGridPreview(NanoVGManager nvg, ColorPalette palette, AccentColor accent, float x, float y, float width, float height) {
        int columns = 3;
        int rows = 2;
        float padding = 16F;
        float columnGap = 10F;
        float rowGap = 10F;
        float availableWidth = width - (padding * 2F);
        float availableHeight = height - (padding * 2F);
        float cellWidth = (availableWidth - ((columns - 1) * columnGap)) / columns;
        float cellHeight = (availableHeight - ((rows - 1) * rowGap)) / rows;

        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                float cellX = x + padding + column * (cellWidth + columnGap);
                float cellY = y + padding + row * (cellHeight + rowGap);
                nvg.drawRoundedRect(cellX, cellY, cellWidth, cellHeight, 8F,
                        ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 210));
            }
        }

        //nvg.drawGradientRoundedRect(x + padding, y + height - padding - 6F, width - (padding * 2F), 6F, 3F, ColorUtils.applyAlpha(accent.getColor1(), 160), ColorUtils.applyAlpha(accent.getColor2(), 160));
    }

    private void drawFilmstripPreview(NanoVGManager nvg, ColorPalette palette, AccentColor accent, float x, float y, float width, float height) {
        float padding = 16F;
        float mainHeight = height - 42F;
        nvg.drawRoundedRect(x + padding, y + padding, width - (padding * 2F), mainHeight, 10F,
                ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 210));

        float stripY = y + height - 26F;
        nvg.drawRoundedRect(x + padding, stripY, width - (padding * 2F), 12F, 6F,
                ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 220));

        float thumbWidth = 18F;
        float thumbGap = 6F;
        float startX = x + padding + 4F;
        for (int i = 0; i < 5; i++) {
            float thumbX = startX + i * (thumbWidth + thumbGap);
            nvg.drawRoundedRect(thumbX, stripY + 2F, thumbWidth, 8F, 3F,
                    ColorUtils.applyAlpha(accent.getColor1(), i == 0 ? 180 : 90));
        }
    }

    private void drawScrollbar(NanoVGManager nvg, ColorPalette palette, AccentColor accent, float baseX, float baseY, float baseWidth,
                               float baseHeight, float contentHeight, float scrollValue) {
        if (contentHeight <= baseHeight) {
            return;
        }

        float maxScroll = Math.max(0F, contentHeight - baseHeight);
        float trackX = baseX + baseWidth - 10F;
        float trackY = baseY + 10F;
        float trackWidth = 4F;
        float trackHeight = baseHeight - 20F;

        nvg.drawRoundedRect(trackX, trackY, trackWidth, trackHeight, 2F,
                ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 120));

        float visibleRatio = Math.min(1F, baseHeight / contentHeight);
        float handleHeight = Math.max(28F, trackHeight * visibleRatio);
        float scrollOffset = -scrollValue;
        float handleY = trackY + (trackHeight - handleHeight) * (scrollOffset / maxScroll);

        nvg.drawGradientRoundedRect(trackX - 1F, handleY, trackWidth + 2F, handleHeight, 3F,
                ColorUtils.applyAlpha(accent.getColor1(), 190),
                ColorUtils.applyAlpha(accent.getColor2(), 190));
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton != 0) {
            return;
        }

        float baseX = getX();
        float baseY = getContentY();
        float baseWidth = getWidth();
        float baseHeight = getContentHeight();

        if (!MouseUtils.isInside(mouseX, mouseY, baseX, baseY, baseWidth, baseHeight)) {
            closeDropdowns();
            return;
        }

        if (layoutDropdown != null) {
            layoutDropdown.mouseClicked(mouseX, mouseY, mouseButton);
        }
        if (moduleDropdown != null) {
            moduleDropdown.mouseClicked(mouseX, mouseY, mouseButton);
        }
        if (screenshotDropdown != null) {
            screenshotDropdown.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    private void closeDropdowns() {
        if (layoutDropdown != null) {
            layoutDropdown.setOpen(false);
        }
        if (moduleDropdown != null) {
            moduleDropdown.setOpen(false);
        }
        if (screenshotDropdown != null) {
            screenshotDropdown.setOpen(false);
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        contentScroll.onKey(keyCode);
    }
}

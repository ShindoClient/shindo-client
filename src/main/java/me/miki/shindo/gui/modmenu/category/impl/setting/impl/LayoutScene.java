package me.miki.shindo.gui.modmenu.category.impl.setting.impl;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.miki.shindo.Shindo;
import me.miki.shindo.gui.modmenu.category.impl.SettingCategory;
import me.miki.shindo.gui.modmenu.category.impl.setting.SettingScene;
import me.miki.shindo.gui.modmenu.category.impl.shared.SettingsPanel;
import me.miki.shindo.management.color.AccentColor;
import me.miki.shindo.management.color.ColorManager;
import me.miki.shindo.management.color.palette.ColorPalette;
import me.miki.shindo.management.color.palette.ColorType;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.layout.UILayoutManager;
import me.miki.shindo.management.layout.UILayoutManager.LayoutType;
import me.miki.shindo.management.layout.UILayoutManager.Layouts;
import me.miki.shindo.management.mods.impl.InternalSettingsMod;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.management.nanovg.font.Fonts;
import me.miki.shindo.management.screenshot.ScreenshotDisplayMode;
import me.miki.shindo.management.settings.config.ConfigOwner;
import me.miki.shindo.management.settings.impl.ComboSetting;
import me.miki.shindo.management.settings.impl.combo.Option;
import me.miki.shindo.ui.comp.impl.CompDropdown;
import me.miki.shindo.utils.ColorUtils;

public class LayoutScene extends SettingScene {

    private static final float PANEL_PADDING = 16F;
    private static final float CARD_RADIUS = 12F;
    private static final float PREVIEW_RADIUS = 12F;
    private static final float LAYOUT_PREVIEW_HEIGHT = 170F;
    private static final float MODULE_PREVIEW_HEIGHT = 152F;
    private static final float SCREENSHOT_PREVIEW_HEIGHT = 146F;

    private final UILayoutManager layoutManager = Shindo.getInstance().getUiLayoutManager();

    private Layouts selectedArea = Layouts.SETTINGS;
    private LayoutType selectedType;

    private ComboSetting areaSetting;
    private ComboSetting typeSetting;
    private CompDropdown areaDropdown;
    private CompDropdown typeDropdown;

    private final List<Option> areaOptions = new ArrayList<Option>();
    private final List<Option> typeOptions = new ArrayList<Option>();

    public LayoutScene(SettingCategory parent) {
        super(parent, TranslateText.SETTINGS_LAYOUT_TITLE, TranslateText.SETTINGS_LAYOUT_DESCRIPTION, me.miki.shindo.management.nanovg.font.LegacyIcon.GRID);
    }

    @Override
    public void initGui() {
        selectedArea = Layouts.SETTINGS;
        selectedType = layoutManager.getSelectedType(selectedArea);
        buildAreaSetting();
        buildTypeSetting();
    }

    private void buildAreaSetting() {
        areaOptions.clear();
        for (Layouts value : Layouts.values()) {
            areaOptions.add(new Option(value.getTitle()));
        }
        areaSetting = new ComboSetting("layout-area", dummyOwner(), areaOptions.get(selectedArea.ordinal()).getNameKey(), areaOptions);
        areaDropdown = new CompDropdown(0F, 0F, 0F, areaSetting);
        areaDropdown.setOpenUp(true);
    }

    private void buildTypeSetting() {
        typeOptions.clear();
        List<LayoutType> types = layoutManager.getTypes(selectedArea);
        for (LayoutType type : types) {
            typeOptions.add(new Option(type.getTitle()));
        }
        String defaultKey = !typeOptions.isEmpty() ? typeOptions.get(0).getNameKey() : "none";
        int selectedIdx = types.indexOf(selectedType != null ? selectedType : layoutManager.getSelectedType(selectedArea));
        if (selectedIdx < 0 && !types.isEmpty()) {
            selectedIdx = 0;
        }
        if (selectedIdx >= 0 && selectedIdx < typeOptions.size()) {
            defaultKey = typeOptions.get(selectedIdx).getNameKey();
        }
        typeSetting = new ComboSetting("layout-type", dummyOwner(), defaultKey, typeOptions);
        typeDropdown = new CompDropdown(0F, 0F, 0F, typeSetting);
        typeDropdown.setOpenUp(true);
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

        float leftWidth = Math.min(120F, Math.max(100F, baseWidth * 0.18F));
        float leftX = baseX + PANEL_PADDING;
        float leftY = baseY + PANEL_PADDING;
        float leftHeight = baseHeight - (PANEL_PADDING * 2F);

        float gap = 14F;
        float rightX = leftX + leftWidth + gap;
        float rightY = leftY;
        float rightWidth = Math.max(0F, baseWidth - leftWidth - (PANEL_PADDING * 2F) - gap);
        float rightHeight = leftHeight;

        drawContainer(nvg, palette, leftX, leftY, leftWidth, leftHeight);
        drawContainer(nvg, palette, rightX, rightY, rightWidth, rightHeight);

        syncSelections();
        drawLeftCard(nvg, palette, accent, leftX, leftY, leftWidth, leftHeight, mouseX, mouseY, partialTicks);
        drawPreviewPanel(nvg, palette, accent, rightX, rightY, rightWidth, rightHeight, mouseX, mouseY, partialTicks);
    }

    private void syncSelections() {
        // Area sync
        int areaIdx = areaSetting.getOptions().indexOf(areaSetting.getOption());
        if (areaIdx >= 0 && areaIdx < Layouts.values().length) {
            Layouts newArea = Layouts.values()[areaIdx];
            if (newArea != selectedArea) {
                selectedArea = newArea;
                selectedType = layoutManager.getSelectedType(selectedArea);
                buildTypeSetting();
            }
        }

        // Type sync
        List<LayoutType> types = layoutManager.getTypes(selectedArea);
        if (types.isEmpty()) {
            return;
        }
        int typeIdx = typeSetting.getOptions().indexOf(typeSetting.getOption());
        if (typeIdx >= 0 && typeIdx < types.size()) {
            LayoutType newType = types.get(typeIdx);
            if (newType != selectedType) {
                selectedType = newType;
                layoutManager.selectType(selectedType);
            }
        }
    }

    private void drawContainer(NanoVGManager nvg, ColorPalette palette, float x, float y, float width, float height) {
        nvg.drawShadow(x, y, width, height, CARD_RADIUS, 7);
        nvg.drawRoundedRect(x, y, width, height, CARD_RADIUS, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 210));
        nvg.drawRoundedRect(x + 1F, y + 1F, width - 2F, height - 2F, CARD_RADIUS - 1F, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 230));
    }

    private void drawLeftCard(NanoVGManager nvg, ColorPalette palette, AccentColor accent, float x, float y, float width, float height, int mouseX, int mouseY, float partialTicks) {
        String areaTitle = selectedArea.getTitle();
        String areaDesc = selectedArea.getDescription();

        float headerX = x + 12F;
        float headerY = y + 14F;
        float headerWidth = width - 24F;

        nvg.drawText(areaTitle, headerX, headerY, palette.getFontColor(ColorType.DARK), 13.5F, Fonts.MEDIUM);
        nvg.drawText(nvg.getLimitText(areaDesc, 9F, Fonts.REGULAR, headerWidth),
                headerX, headerY + 16F, ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 210), 9F, Fonts.REGULAR);

        float dropdownWidth = width - 24F;
        float dropdownX = x + 12F;
        float dropdownY = y + height - typeDropdown.getHeight() - 10F;
        typeDropdown.setX(dropdownX);
        typeDropdown.setY(dropdownY);
        typeDropdown.setWidth(dropdownWidth);
        typeDropdown.draw(mouseX, mouseY, partialTicks);
    }

    private void drawPreviewPanel(NanoVGManager nvg, ColorPalette palette, AccentColor accent, float x, float y, float width, float height, int mouseX, int mouseY, float partialTicks) {
        InternalSettingsMod settings = InternalSettingsMod.getInstance();
        SettingsPanel.LayoutMode layoutMode = settings.getSettingsLayoutMode();
        int moduleColumns = settings.getModuleGridColumns();
        ScreenshotDisplayMode screenshotMode = settings.getScreenshotDisplayMode();

        float previewX = x + 14F;
        float previewY = y + 14F;
        float previewWidth = width - 28F;
        float previewHeight = Math.max(0F, height - (previewY - y) - areaDropdown.getHeight() - 12F);

        nvg.save();
        nvg.intersectScissor(x, y, width, height);

        if (selectedArea == Layouts.SETTINGS) {
            drawLayoutPreview(nvg, palette, accent, previewX, previewY, previewWidth, Math.min(previewHeight, LAYOUT_PREVIEW_HEIGHT), layoutMode);
        } else if (selectedArea == Layouts.MODULES) {
            drawModulePreview(nvg, palette, accent, previewX, previewY, previewWidth, Math.min(previewHeight, MODULE_PREVIEW_HEIGHT), moduleColumns);
        } else if (selectedArea == Layouts.SCREENSHOTS) {
            drawScreenshotPreview(nvg, palette, accent, previewX, previewY, previewWidth, Math.min(previewHeight, SCREENSHOT_PREVIEW_HEIGHT), screenshotMode);
        }

        nvg.restore();

        float dropdownWidth = Math.min(220F, previewWidth);
        float dropdownX = previewX;
        float dropdownY = y + height - areaDropdown.getHeight() - 10F;
        areaDropdown.setX(dropdownX);
        areaDropdown.setY(dropdownY);
        areaDropdown.setWidth(dropdownWidth);
        areaDropdown.draw(mouseX, mouseY, partialTicks);
    }

    private void drawLayoutPreview(NanoVGManager nvg, ColorPalette palette, AccentColor accent, float x, float y, float width, float height, SettingsPanel.LayoutMode layoutMode) {
        Color base = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 170);
        Color cardColor = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 210);
        Color detailColor = ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 220);

        nvg.drawRoundedRect(x, y, width, height, PREVIEW_RADIUS, base);

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

        float footerWidth = width - (padding * 2F);
        nvg.drawGradientRoundedRect(x + padding, y + height - padding - 6F, footerWidth, 6F, 3F,
                ColorUtils.applyAlpha(accent.getColor1(), 160), ColorUtils.applyAlpha(accent.getColor2(), 160));
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
                nvg.drawRoundedRect(cardX + columnWidth - 20F, cardY + 10F, 12F, 12F, 6F,
                        ColorUtils.applyAlpha(accent.getColor1(), 200));
            }
        }
    }

    private void drawScreenshotPreview(NanoVGManager nvg, ColorPalette palette, AccentColor accent, float x, float y, float width,
                                       float height, ScreenshotDisplayMode mode) {
        Color background = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 140);
        nvg.drawRoundedRect(x, y, width, height, PREVIEW_RADIUS, background);

        if (mode == ScreenshotDisplayMode.GRID) {
            drawGridPreview(nvg, palette, x, y, width, height);
        } else {
            drawFilmstripPreview(nvg, palette, accent, x, y, width, height);
        }
    }

    private void drawGridPreview(NanoVGManager nvg, ColorPalette palette, float x, float y, float width, float height) {
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

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton != 0) {
            return;
        }

        typeDropdown.mouseClicked(mouseX, mouseY, mouseButton);
        areaDropdown.mouseClicked(mouseX, mouseY, mouseButton);
    }

    private ConfigOwner dummyOwner() {
        return new ConfigOwner() {
            @Override
            public String getConfigId() {
                return "layout-scene-temp";
            }
        };
    }
}

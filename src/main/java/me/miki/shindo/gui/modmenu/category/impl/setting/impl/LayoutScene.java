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
import me.miki.shindo.utils.ColorUtils;
import me.miki.shindo.utils.mouse.MouseUtils;
import me.miki.shindo.utils.mouse.Scroll;

import java.awt.*;

public class LayoutScene extends SettingScene {

    private static final int PREVIEW_ROWS = 3;
    private static final int PREVIEW_COLUMNS_SINGLE = 1;
    private static final int PREVIEW_COLUMNS_DOUBLE = 2;
    private static final int[] MODULE_COLUMN_OPTIONS = new int[]{1, 2};

    private static final float VERTICAL_PADDING_TOP = 28F;
    private static final float VERTICAL_PADDING_BOTTOM = 24F;
    private static final float HORIZONTAL_PADDING = 22F;
    private static final float SECTION_SPACING = 32F;
    private static final float MIN_LAYOUT_SECTION_HEIGHT = 168F;
    private static final float MIN_MODULE_SECTION_HEIGHT = 132F;

    private final Scroll contentScroll = new Scroll();

    public LayoutScene(SettingCategory parent) {
        super(parent, TranslateText.SETTINGS_LAYOUT_TITLE, TranslateText.SETTINGS_LAYOUT_DESCRIPTION, LegacyIcon.GRID);
    }

    @Override
    public void initGui() {
        contentScroll.resetAll();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        NanoVGManager nvg = Shindo.getInstance().getNanoVGManager();
        ColorManager colorManager = Shindo.getInstance().getColorManager();
        ColorPalette palette = colorManager.getPalette();
        AccentColor accent = colorManager.getCurrentColor();

        SettingsPanel.LayoutMode current = InternalSettingsMod.getInstance().getSettingsLayoutMode();
        int storedColumns = InternalSettingsMod.getInstance().getModuleGridColumns();
        int currentModuleColumns = Math.max(1, Math.min(2, storedColumns));
        if (storedColumns != currentModuleColumns) {
            InternalSettingsMod.getInstance().setModuleGridColumns(currentModuleColumns);
        }

        float baseX = getX();
        float baseY = getContentY();
        float baseWidth = getWidth();
        float baseHeight = getContentHeight();

        if (baseWidth <= 0F || baseHeight <= 0F) {
            return;
        }

        float containerRadius = 12F;
        nvg.drawShadow(baseX, baseY, baseWidth, baseHeight, containerRadius, 6);
        nvg.drawRoundedRect(baseX, baseY, baseWidth, baseHeight, containerRadius,
                ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 210));
        nvg.drawRoundedRect(baseX + 1F, baseY + 1F, baseWidth - 2F, baseHeight - 2F, containerRadius - 1F,
                ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 228));

        float layoutSectionHeight = Math.max(MIN_LAYOUT_SECTION_HEIGHT, baseHeight * 0.60F);
        float moduleSectionHeight = Math.max(MIN_MODULE_SECTION_HEIGHT, baseHeight * 0.60F);
        float contentHeight = VERTICAL_PADDING_TOP + layoutSectionHeight + SECTION_SPACING + moduleSectionHeight + VERTICAL_PADDING_BOTTOM;
        float maxScroll = Math.max(0F, contentHeight - baseHeight);
        contentScroll.setMaxScroll(maxScroll);

        if (MouseUtils.isInside(mouseX, mouseY, baseX, baseY, baseWidth, baseHeight)) {
            contentScroll.onScroll();
        }
        contentScroll.onAnimation();

        float scrollValue = contentScroll.getValue();
        float layoutRowY = baseY + VERTICAL_PADDING_TOP + scrollValue;
        float moduleRowY = layoutRowY + layoutSectionHeight + SECTION_SPACING;

        nvg.save();
        nvg.scissor(baseX, baseY, baseWidth, baseHeight);

        nvg.drawText(TranslateText.SETTINGS_LAYOUT_SECTION_LAYOUT.getText(), baseX + HORIZONTAL_PADDING, layoutRowY - 18F,
                palette.getFontColor(ColorType.DARK), 12.5F, Fonts.MEDIUM);

        float layoutCardSpacing = 24F;
        float layoutCardWidth = (baseWidth - (HORIZONTAL_PADDING * 2) - layoutCardSpacing) / 2F;
        float leftCardX = baseX + HORIZONTAL_PADDING;
        float rightCardX = leftCardX + layoutCardWidth + layoutCardSpacing;

        drawLayoutCard(nvg, palette, accent, leftCardX, layoutRowY, layoutCardWidth, layoutSectionHeight, SettingsPanel.LayoutMode.SINGLE_COLUMN, current == SettingsPanel.LayoutMode.SINGLE_COLUMN, mouseX, mouseY);

        drawLayoutCard(nvg, palette, accent, rightCardX, layoutRowY, layoutCardWidth, layoutSectionHeight, SettingsPanel.LayoutMode.DOUBLE_COLUMN, current == SettingsPanel.LayoutMode.DOUBLE_COLUMN, mouseX, mouseY);

        nvg.drawText(TranslateText.SETTINGS_LAYOUT_SECTION_MODULE.getText(), baseX + HORIZONTAL_PADDING, moduleRowY - 18F,
                palette.getFontColor(ColorType.DARK), 12.5F, Fonts.MEDIUM);

        float moduleCardSpacing = 28F;
        float moduleCardWidth = (baseWidth - (HORIZONTAL_PADDING * 2) - moduleCardSpacing) / 2F;
        for (int i = 0; i < MODULE_COLUMN_OPTIONS.length; i++) {
            int columns = MODULE_COLUMN_OPTIONS[i];
            float cardX = baseX + HORIZONTAL_PADDING + i * (moduleCardWidth + moduleCardSpacing);
            drawModuleLayoutCard(nvg, palette, accent, cardX, moduleRowY, moduleCardWidth, moduleSectionHeight + 22, columns, currentModuleColumns == columns, mouseX, mouseY);
        }

        nvg.restore();

        if (maxScroll > 0F) {
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
            return;
        }

        float layoutSectionHeight = Math.max(MIN_LAYOUT_SECTION_HEIGHT, baseHeight * 0.60F);
        float moduleSectionHeight = Math.max(MIN_MODULE_SECTION_HEIGHT, baseHeight * 0.55F);
        float layoutRowY = baseY + VERTICAL_PADDING_TOP + contentScroll.getValue();
        float moduleRowY = layoutRowY + layoutSectionHeight + SECTION_SPACING;

        float layoutCardSpacing = 24F;
        float layoutCardWidth = (baseWidth - (HORIZONTAL_PADDING * 2) - layoutCardSpacing) / 2F;
        float leftCardX = baseX + HORIZONTAL_PADDING;
        float rightCardX = leftCardX + layoutCardWidth + layoutCardSpacing;

        if (MouseUtils.isInside(mouseX, mouseY, leftCardX, layoutRowY, layoutCardWidth, layoutSectionHeight)) {
            InternalSettingsMod.getInstance().setSettingsLayoutMode(SettingsPanel.LayoutMode.SINGLE_COLUMN);
            return;
        }
        if (MouseUtils.isInside(mouseX, mouseY, rightCardX, layoutRowY, layoutCardWidth, layoutSectionHeight)) {
            InternalSettingsMod.getInstance().setSettingsLayoutMode(SettingsPanel.LayoutMode.DOUBLE_COLUMN);
            return;
        }

        float moduleCardSpacing = 28F;
        float moduleCardWidth = (baseWidth - (HORIZONTAL_PADDING * 2) - moduleCardSpacing) / 2F;
        for (int i = 0; i < MODULE_COLUMN_OPTIONS.length; i++) {
            int columns = MODULE_COLUMN_OPTIONS[i];
            float cardX = baseX + HORIZONTAL_PADDING + i * (moduleCardWidth + moduleCardSpacing);
            if (MouseUtils.isInside(mouseX, mouseY, cardX, moduleRowY, moduleCardWidth + 22, moduleSectionHeight)) {
                InternalSettingsMod.getInstance().setModuleGridColumns(columns);
                return;
            }
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        contentScroll.onKey(keyCode);
    }

    private void drawLayoutCard(NanoVGManager nvg, ColorPalette palette, AccentColor accent, float x, float y, float width, float height, SettingsPanel.LayoutMode layout, boolean selected, int mouseX, int mouseY) {

        boolean hovered = MouseUtils.isInside(mouseX, mouseY, x, y, width, height);
        float radius = 12F;
        Color baseColor = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), selected ? 226 : 190);
        Color accentStart = ColorUtils.applyAlpha(accent.getColor1(), hovered || selected ? 78 : 36);
        Color accentEnd = ColorUtils.applyAlpha(accent.getColor2(), hovered || selected ? 78 : 36);

        nvg.drawRoundedRect(x, y, width, height, radius, baseColor);
        nvg.drawGradientRoundedRect(x, y, width, height, radius, accentStart, accentEnd);

        TranslateText titleKey = layout == SettingsPanel.LayoutMode.SINGLE_COLUMN
                ? TranslateText.SETTINGS_LAYOUT_SINGLE_TITLE
                : TranslateText.SETTINGS_LAYOUT_DOUBLE_TITLE;
        TranslateText subtitleKey = layout == SettingsPanel.LayoutMode.SINGLE_COLUMN
                ? TranslateText.SETTINGS_LAYOUT_SINGLE_DESCRIPTION
                : TranslateText.SETTINGS_LAYOUT_DOUBLE_DESCRIPTION;

        String title = titleKey.getText();
        String subtitle = subtitleKey.getText();

        float textX = x + 22F;
        float textY = y + 26F;
        float textWidth = width - 44F;

        nvg.drawText(nvg.getLimitText(title, 12.5F, Fonts.MEDIUM, textWidth), textX, textY, palette.getFontColor(ColorType.DARK), 12.5F, Fonts.MEDIUM);
        nvg.drawText(nvg.getLimitText(subtitle, 9F, Fonts.REGULAR, textWidth), textX, textY + 18F, palette.getFontColor(ColorType.NORMAL), 9F, Fonts.REGULAR);

        if (selected) {
            nvg.drawText(LegacyIcon.CHECK, x + width - 28F, y + 26F, palette.getFontColor(ColorType.DARK), 11.5F, Fonts.LEGACYICON);
        }

        float previewX = x + 22F;
        float previewY = y + 78F;
        float previewWidth = width - 44F;
        float previewHeight = Math.max(62F, height - 108F);

        nvg.drawRoundedRect(previewX, previewY, previewWidth, previewHeight, 10F, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 130));

        nvg.save();

        int columns = layout == SettingsPanel.LayoutMode.SINGLE_COLUMN ? PREVIEW_COLUMNS_SINGLE : PREVIEW_COLUMNS_DOUBLE;
        float columnGap = 10F;
        float rowGap = 8F;
        float columnWidth = (previewWidth - ((columns - 1) * columnGap)) / columns;
        float rowHeight = (previewHeight - ((PREVIEW_ROWS - 1) * rowGap)) / PREVIEW_ROWS;
        float minColumnWidth = 34F;
        float minRowHeight = 18F;
        if (columnWidth < minColumnWidth) {
            columnWidth = minColumnWidth;
        }
        if (rowHeight < minRowHeight) {
            rowHeight = minRowHeight;
        }
        float contentHeight = PREVIEW_ROWS * rowHeight + (PREVIEW_ROWS - 1) * rowGap;
        float yOffset = Math.max(0F, (previewHeight - contentHeight) / 2F);

        for (int row = 0; row < PREVIEW_ROWS; row++) {
            for (int column = 0; column < columns; column++) {
                float bx = previewX + column * (columnWidth + columnGap);
                float by = previewY + yOffset + row * (rowHeight + rowGap);
                nvg.drawRoundedRect(bx, by, columnWidth, rowHeight, 6F, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 210));
                float indicatorHeight = Math.min(8F, Math.max(4F, rowHeight - 12F));
                float indicatorY = by + Math.max(6F, (rowHeight - indicatorHeight) / 2F);
                float indicatorX = bx + 6F;
                float indicatorWidth = Math.max(22F, columnWidth - 12F);
                nvg.drawRoundedRect(indicatorX, indicatorY, indicatorWidth, indicatorHeight, 3F, palette.getFontColor(ColorType.DARK));
            }
        }

        nvg.restore();
    }




    private void drawModuleLayoutCard(NanoVGManager nvg, ColorPalette palette, AccentColor accent, float x, float y, float width, float height, int columns, boolean selected, int mouseX, int mouseY) {

        boolean hovered = MouseUtils.isInside(mouseX, mouseY, x, y, width, height);
        float radius = 10F;
        Color baseColor = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), selected ? 220 : 188);
        Color accentStart = ColorUtils.applyAlpha(accent.getColor1(), hovered || selected ? 92 : 48);
        Color accentEnd = ColorUtils.applyAlpha(accent.getColor2(), hovered || selected ? 92 : 48);

        nvg.drawRoundedRect(x, y, width, height, radius, baseColor);
        nvg.drawGradientRoundedRect(x, y, width, height, radius, accentStart, accentEnd);

        TranslateText titleKey = columns == 1
                ? TranslateText.SETTINGS_LAYOUT_MODULE_SINGLE_TITLE
                : TranslateText.SETTINGS_LAYOUT_MODULE_DOUBLE_TITLE;
        TranslateText subtitleKey = columns == 1
                ? TranslateText.SETTINGS_LAYOUT_MODULE_SINGLE_DESCRIPTION
                : TranslateText.SETTINGS_LAYOUT_MODULE_DOUBLE_DESCRIPTION;
        String title = titleKey.getText();
        String subtitle = subtitleKey.getText();

        float textX = x + 20F;
        float textY = y + 24F;
        float textWidth = width - 40F;

        nvg.drawText(nvg.getLimitText(title, 11.5F, Fonts.MEDIUM, textWidth), textX, textY, palette.getFontColor(ColorType.DARK), 11.5F, Fonts.MEDIUM);
        nvg.drawText(nvg.getLimitText(subtitle, 8.5F, Fonts.REGULAR, textWidth), textX, textY + 15F, palette.getFontColor(ColorType.NORMAL), 8.5F, Fonts.REGULAR);

        float previewX = x + 22F;
        float previewY = y + 66F;
        float previewWidth = width - 44F;
        float previewHeight = height - 88F;
        float previewRowGap = 6F;
        int previewRows = 3;

        nvg.drawRoundedRect(previewX, previewY, previewWidth, previewHeight, 8F,
                ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 122));

        float previewColumnGap = 10F;
        float previewColumnWidth = (previewWidth - (previewColumnGap * (columns - 1))) / columns;
        float previewRowHeight = (previewHeight - (previewRowGap * (previewRows - 1))) / previewRows;
        float minPreviewColumnWidth = 40F;
        float minPreviewRowHeight = 18F;
        if (previewColumnWidth < minPreviewColumnWidth) {
            previewColumnWidth = minPreviewColumnWidth;
        }
        if (previewRowHeight < minPreviewRowHeight) {
            previewRowHeight = minPreviewRowHeight;
        }
        float previewContentHeight = previewRows * previewRowHeight + (previewRows - 1) * previewRowGap;
        float previewYOffset = Math.max(0F, (previewHeight - previewContentHeight) / 2F);

        nvg.save();

        for (int row = 0; row < previewRows; row++) {
            for (int col = 0; col < columns; col++) {
                float boxX = previewX + col * (previewColumnWidth + previewColumnGap);
                float boxY = previewY + previewYOffset + row * (previewRowHeight + previewRowGap);
                nvg.drawRoundedRect(boxX, boxY, previewColumnWidth, previewRowHeight, 5F,
                        ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 200));
                float indicatorHeight = Math.min(8F, Math.max(4F, previewRowHeight - 12F));
                float indicatorY = boxY + Math.max(6F, (previewRowHeight - indicatorHeight) / 2F);
                float indicatorWidth = Math.max(22F, previewColumnWidth - 10F);
                nvg.drawRoundedRect(boxX + 5F, indicatorY, indicatorWidth, indicatorHeight, 3F,
                        palette.getFontColor(ColorType.DARK));
            }
        }

        nvg.restore();

        if (selected) {
            nvg.drawText(LegacyIcon.CHECK, x + width - 26F, y + 24F, palette.getFontColor(ColorType.DARK), 10.5F, Fonts.LEGACYICON);
        }
    }



}

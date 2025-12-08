package me.miki.shindo.gui.modmenu.category.impl.shared;

import lombok.Data;
import lombok.Getter;
import me.miki.shindo.management.color.AccentColor;
import me.miki.shindo.management.color.palette.ColorPalette;
import me.miki.shindo.management.color.palette.ColorType;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.management.nanovg.font.Font;
import me.miki.shindo.management.nanovg.font.Fonts;
import me.miki.shindo.management.settings.Setting;
import me.miki.shindo.management.settings.impl.CategorySetting;
import me.miki.shindo.management.settings.metadata.SettingMetadata;
import me.miki.shindo.ui.comp.Comp;
import me.miki.shindo.ui.comp.factory.SettingComponentFactory;
import me.miki.shindo.ui.comp.impl.CompCellGrid;
import me.miki.shindo.ui.comp.impl.CompColorPicker;
import me.miki.shindo.ui.comp.impl.CompComboBox;
import me.miki.shindo.ui.comp.impl.CompImageSelect;
import me.miki.shindo.ui.comp.impl.CompKeybind;
import me.miki.shindo.ui.comp.impl.CompModTextBox;
import me.miki.shindo.ui.comp.impl.CompSlider;
import me.miki.shindo.ui.comp.impl.CompSoundSelect;
import me.miki.shindo.ui.comp.impl.CompToggleButton;
import me.miki.shindo.ui.comp.impl.CompCategory;
import me.miki.shindo.ui.framework.UIContext;
import me.miki.shindo.ui.framework.UIRenderer;
import me.miki.shindo.ui.framework.UIStyle;
import me.miki.shindo.utils.ColorUtils;
import me.miki.shindo.utils.animation.simple.SimpleAnimation;
import me.miki.shindo.utils.mouse.MouseUtils;
import me.miki.shindo.utils.mouse.Scroll;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SettingsPanel {

    public enum LayoutMode {
        SINGLE_COLUMN,
        DOUBLE_COLUMN
    }

    private static final float OUTER_MARGIN = 10F;
    private static final float CATEGORY_GAP = 14F;
    private static final float CATEGORY_HEADER_HEIGHT = UIStyle.SETTING_TEXT_MARGIN + 10F;
    private static final float CATEGORY_HEADER_SPACING = 6F;
    private static final float CATEGORY_CARD_RADIUS = 12F;
    private static final float CARD_PADDING_X = 16F;
    private static final float CARD_PADDING_Y = 12F;
    private static final float ROW_GAP = 8F;
    private static final float COLUMN_GAP = 12F;
    private static final float MIN_ROW_HEIGHT = 34F;
    private static final float MIN_CARD_HEIGHT = 36F;
    private static final float TITLE_FONT_SIZE = 9.0F;
    private static final float DESCRIPTION_FONT_SIZE = 7.6F;
    private static final float INDICATOR_WIDTH = 3.5F;
    private static final float TOOLTIP_MAX_WIDTH = 320F;
    private static final float COMPONENT_VERTICAL_PADDING = 12F;
    private static final float TEXT_GAP = 16F;

    private final List<Entry> entries = new ArrayList<>();
    private final List<CategoryLayout> categoryLayouts = new ArrayList<>();
    private final Map<Setting, EntryState> entryStates = new HashMap<>();

    @Getter
    private LayoutMode layoutMode = LayoutMode.SINGLE_COLUMN;

    public void clear() {
        entries.clear();
        categoryLayouts.clear();
        entryStates.clear();
    }

    public void setLayoutMode(LayoutMode layoutMode) {
        this.layoutMode = layoutMode == null ? LayoutMode.SINGLE_COLUMN : layoutMode;
    }

    public void buildEntries(List<Setting> settings) {
        entries.clear();
        CategorySetting currentCategory = null;
        for (Setting setting : settings) {
            if (setting instanceof CategorySetting) {
                currentCategory = (CategorySetting) setting;
                CompCategory compCategory = new CompCategory(0, currentCategory);
                compCategory.setHeight(CATEGORY_HEADER_HEIGHT);
                entries.add(new Entry(setting, compCategory, currentCategory));
                getState(setting);
                continue;
            }

            Comp component = createComponent(setting);
            if (component == null) {
                continue;
            }
            entries.add(new Entry(setting, component, currentCategory));
            getState(setting);
        }
    }

    public void draw(int mouseX, int mouseY, float partialTicks, float contentX, float contentY, float contentWidth, float viewportHeight, NanoVGManager nvg, ColorPalette palette, Scroll scroll) {

        updateLayout(contentX, contentY, contentWidth, scroll.getValue());

        UIContext ctx = UIContext.get();
        AccentColor accentColor = ctx.accent();

        TooltipData tooltip = null;
        float contentBottom = contentY + scroll.getValue();

        for (CategoryLayout layout : categoryLayouts) {
            contentBottom = Math.max(contentBottom, layout.getBottom());

            if (layout.getHeader() != null) {
                CompCategory category = layout.getHeader();
                category.setWidth(layout.getHeaderWidth());
                category.setX(layout.getHeaderX());
                category.setY(layout.getHeaderY());
                category.draw(mouseX, mouseY, partialTicks);
            }

            if (layout.isCollapsed()) {
                continue;
            }

            drawCategoryCard(nvg, palette, layout);

            List<PositionedEntry> positionedEntries = layout.getEntries();
            for (int i = 0; i < positionedEntries.size(); i++) {
                PositionedEntry positioned = positionedEntries.get(i);
                Entry entry = positioned.entry;
                EntryState state = getState(entry.setting);

                boolean hovered = MouseUtils.isInside(mouseX, mouseY, positioned.x, positioned.y, positioned.width, positioned.height);
                state.hoverAnimation.setAnimation(hovered ? 1F : 0F, 18);
                float hoverProgress = state.hoverAnimation.getValue();

                layoutComponent(entry.comp, positioned);

                float indicatorHeight = Math.max(14F, positioned.height - 6F);
                float indicatorY = positioned.y + (positioned.height - indicatorHeight) / 2F;
                Color indicatorColor = ColorUtils.applyAlpha(accentColor.getColor1(), (int) (hoverProgress * 120));
                nvg.drawRoundedRect(positioned.x, indicatorY, INDICATOR_WIDTH, indicatorHeight, 2F, indicatorColor);

                float textX = positioned.x + INDICATOR_WIDTH + 7F;
                float textY = positioned.y + 6F;
                float textWidth = resolveTextWidth(entry, positioned, textX);

                TooltipData rowTooltip = drawLabels(nvg, palette, entry, textX, textY, textWidth, hoverProgress, mouseX, mouseY);
                if (tooltip == null && rowTooltip != null) {
                    tooltip = rowTooltip;
                }

                entry.comp.draw(mouseX, mouseY, partialTicks);

                if (i < positionedEntries.size() - 1) {
                    float dividerAlpha = 24F + (hoverProgress * 30F);
                    UIRenderer.drawDivider(ctx, positioned.x + INDICATOR_WIDTH + 4F, positioned.y + positioned.height - 2F, positioned.width - INDICATOR_WIDTH - 8F, 1.5F, 1F, dividerAlpha);
                }
            }
        }

        if (!categoryLayouts.isEmpty()) {
            float contentHeight = contentBottom - (contentY + scroll.getValue());
            scroll.setMaxScroll(Math.max(0, contentHeight - viewportHeight));
        } else {
            scroll.setMaxScroll(0);
        }

        if (tooltip != null) {
            drawTooltip(nvg, palette, tooltip, mouseX, mouseY, contentX, contentY, contentWidth, viewportHeight);
        }
    }

    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton, float contentX, float contentY, float contentWidth, float viewportHeight, Scroll scroll) {

        updateLayout(contentX, contentY, contentWidth, scroll.getValue());

        for (CategoryLayout layout : categoryLayouts) {

            if (layout.getHeader() != null && MouseUtils.isInside(mouseX, mouseY, layout.getHeaderX(), layout.getHeaderY(), layout.getHeaderWidth(), layout.getHeaderHeight())) {
                layout.getHeader().mouseClicked(mouseX, mouseY, mouseButton);
                return true;
            }

            if (layout.isCollapsed()) {
                continue;
            }

            for (PositionedEntry positioned : layout.getEntries()) {
                if (positioned.height <= 0) {
                    continue;
                }
                if (positioned.y + positioned.height < contentY || positioned.y > contentY + viewportHeight) {
                    continue;
                }
                if (!MouseUtils.isInside(mouseX, mouseY, positioned.x, positioned.y, positioned.width, positioned.height)) {
                    continue;
                }
                positioned.entry.comp.mouseClicked(mouseX, mouseY, mouseButton);
                return true;
            }
        }
        return false;
    }

    public void mouseReleased(int mouseX, int mouseY, int mouseButton, Scroll scroll) {
        for (Entry entry : entries) {
            entry.comp.mouseReleased(mouseX, mouseY, mouseButton);
        }
    }

    public void keyTyped(char typedChar, int keyCode) {
        for (Entry entry : entries) {
            entry.comp.keyTyped(typedChar, keyCode);
        }
    }

    public void resetSettings() {
        for (Entry entry : entries) {
            if (!(entry.setting instanceof CategorySetting)) {
                entry.setting.reset();
            }
        }
    }

    private void updateLayout(float contentX, float contentY, float contentWidth, float scrollOffset) {
        categoryLayouts.clear();

        float innerX = contentX + OUTER_MARGIN;
        float innerWidth = Math.max(0, contentWidth - (OUTER_MARGIN * 2));
        float yCursor = contentY + scrollOffset;

        float singleWidth = innerWidth;
        float doubleWidth = (innerWidth - COLUMN_GAP) / 2F;

        List<CategoryBlock> blocks = buildBlocks();
        List<CategoryBlock> pendingRow = new ArrayList<>(2);

        for (CategoryBlock block : blocks) {
            if (layoutMode == LayoutMode.DOUBLE_COLUMN && block.spansFullWidth()) {
                if (!pendingRow.isEmpty()) {
                    yCursor = placeRow(innerX, yCursor, singleWidth, doubleWidth, pendingRow);
                    pendingRow.clear();
                }
                CategoryLayout layout = layoutCategory(block, innerX, yCursor, singleWidth);
                categoryLayouts.add(layout);
                yCursor = layout.getBottom() + CATEGORY_GAP;
                continue;
            }

            pendingRow.add(block);
            if (layoutMode == LayoutMode.SINGLE_COLUMN || pendingRow.size() == 2) {
                yCursor = placeRow(innerX, yCursor, singleWidth, doubleWidth, pendingRow);
                pendingRow.clear();
            }
        }

        if (!pendingRow.isEmpty()) {
            yCursor = placeRow(innerX, yCursor, singleWidth, doubleWidth, pendingRow);
        }
    }

    private float placeRow(float innerX, float yCursor, float singleWidth, float doubleWidth, List<CategoryBlock> row) {
        float rowHeight = 0F;
        for (int i = 0; i < row.size(); i++) {
            float width = layoutMode == LayoutMode.DOUBLE_COLUMN ? doubleWidth : singleWidth;
            float x = innerX;
            if (layoutMode == LayoutMode.DOUBLE_COLUMN && i == 1) {
                x += doubleWidth + COLUMN_GAP;
            }

            CategoryLayout layout = layoutCategory(row.get(i), x, yCursor, width);
            categoryLayouts.add(layout);
            rowHeight = Math.max(rowHeight, layout.getTotalHeight());
        }
        return yCursor + rowHeight + CATEGORY_GAP;
    }

    private CategoryLayout layoutCategory(CategoryBlock block, float x, float y, float width) {
        CategoryLayout layout = new CategoryLayout(block);

        float headerHeight = block.hasHeader() ? CATEGORY_HEADER_HEIGHT : 0F;
        float headerSpacing = block.hasHeader() ? CATEGORY_HEADER_SPACING : 0F;
        float cardX = x;
        float cardY = y + headerHeight + headerSpacing;
        float cardWidth = width;
        float contentX = cardX + CARD_PADDING_X;
        float contentY = cardY + CARD_PADDING_Y;
        float contentWidth = Math.max(0F, cardWidth - (CARD_PADDING_X * 2F));

        List<PositionedEntry> positionedEntries = new ArrayList<>();

        if (block.isCollapsed()) {
            for (Entry entry : block.settings) {
                EntryState state = getState(entry.setting);
                float targetHeight = calculateTargetHeight(entry.comp);
                if (!state.initialized) {
                    state.heightAnimation.setValue(targetHeight);
                    state.initialized = true;
                }
                state.heightAnimation.setAnimation(0F, 18);
            }
            layout.setCard(cardX, cardY, cardWidth, 0F);
            layout.setHeader(block.header, x, y, width, headerHeight);
            layout.setEntries(positionedEntries);
            return layout;
        }

        float rowCursor = contentY;
        for (Entry entry : block.settings) {
            EntryState state = getState(entry.setting);
            float targetHeight = calculateTargetHeight(entry.comp);
            if (!state.initialized) {
                state.heightAnimation.setValue(targetHeight);
                state.initialized = true;
            }

            state.heightAnimation.setAnimation(targetHeight, 20);
            float rowHeight = Math.max(MIN_ROW_HEIGHT, state.heightAnimation.getValue());

            PositionedEntry positionedEntry = new PositionedEntry(entry, contentX, rowCursor, contentWidth, rowHeight);
            positionedEntries.add(positionedEntry);
            rowCursor += rowHeight + ROW_GAP;
        }

        if (!positionedEntries.isEmpty()) {
            rowCursor -= ROW_GAP;
        }

        float contentHeight = Math.max(0F, rowCursor - contentY);
        float cardHeight = Math.max(MIN_CARD_HEIGHT, contentHeight + (CARD_PADDING_Y * 2F));

        layout.setCard(cardX, cardY, cardWidth, cardHeight);
        layout.setHeader(block.header, x, y, width, headerHeight);
        layout.setEntries(positionedEntries);

        return layout;
    }

    private List<CategoryBlock> buildBlocks() {
        List<CategoryBlock> blocks = new ArrayList<>();
        CategoryBlock current = new CategoryBlock(null, null);

        for (Entry entry : entries) {
            if (entry.setting instanceof CategorySetting) {
                if (current.hasContent()) {
                    blocks.add(current);
                }
                CategorySetting category = (CategorySetting) entry.setting;
                current = new CategoryBlock(category, (CompCategory) entry.comp);
                continue;
            }
            current.settings.add(entry);
        }

        if (current.hasContent()) {
            blocks.add(current);
        }

        return blocks;
    }

    private float calculateTargetHeight(Comp comp) {
        float base = 38F;

        if (comp instanceof CompSlider) {
            base = 50F;
        }

        if (comp instanceof CompComboBox) {
            base = 50F;
        }
        if (comp instanceof CompColorPicker) {
            CompColorPicker picker = (CompColorPicker) comp;
            if (picker.isOpen()) {
                float scale = picker.getScale() <= 0F ? 1.0F : picker.getScale();
                float openHeight = (26F + (picker.isShowAlpha() ? 118F : 100F)) * scale;
                base = Math.max(base, COMPONENT_VERTICAL_PADDING + openHeight + 14F);
            } else {
                float scale = picker.getScale() <= 0F ? 1.0F : picker.getScale();
                float closedHeight = (30F * scale) + COMPONENT_VERTICAL_PADDING;
                base = Math.max(base, closedHeight);
            }
        }
        if (comp instanceof CompCellGrid) {
            base = Math.max(base, 340F);
        }
        return base;
    }

    private Comp createComponent(Setting setting) {
        return SettingComponentFactory.create(setting);
    }

    private void layoutComponent(Comp comp, PositionedEntry positioned) {
        float x = positioned.x;
        float y = positioned.y;
        float width = positioned.width;
        float height = positioned.height;
        float right = x + width;

        float componentPadding = COMPONENT_VERTICAL_PADDING;
        float componentY = y + componentPadding;

        if (comp instanceof CompToggleButton) {
            CompToggleButton toggle = (CompToggleButton) comp;
            toggle.setScale(0.85F);
            toggle.setX(right - 54F);
            toggle.setY(componentY - 1F);
            return;
        }

        if (comp instanceof CompSlider) {
            CompSlider slider = (CompSlider) comp;
            boolean compact = width < 300F;
            if (compact) {
                slider.setWidth(Math.max(0F, width - (componentPadding * 2)));
                slider.setX(x + componentPadding);
                slider.setY(componentY + 14F);
            } else {
                slider.setWidth(Math.max(110F, width - 200F));
                slider.setX((float) (right - slider.getWidth() - componentPadding));
                slider.setY(componentY + 2F);
            }
            return;
        }

        if (comp instanceof CompComboBox) {
            CompComboBox comboBox = (CompComboBox) comp;
            boolean compact = width < 300F;
            if (compact) {
                float comboWidth = Math.max(0F, width - (componentPadding * 2) - 6F);
                comboBox.setWidth(comboWidth);
                comboBox.setX(x + componentPadding);
                comboBox.setY(componentY + 14F);
            } else {
                float comboWidth = Math.max(110F, Math.min(width - 48F, width - 180F));
                comboBox.setWidth(comboWidth);
                comboBox.setX(Math.max(x + componentPadding, (float) (right - comboWidth - componentPadding)));
                comboBox.setY(componentY);
            }
            return;
        }

        if (comp instanceof CompKeybind) {
            CompKeybind keybind = (CompKeybind) comp;
            keybind.setX(right - 130F);
            keybind.setY(componentY + 2F);
            return;
        }

        if (comp instanceof CompModTextBox) {
            CompModTextBox textBox = (CompModTextBox) comp;
            textBox.setWidth(Math.min(Math.max(120F, width - 160F), 180F));
            textBox.setHeight(18F);
            textBox.setX(right - textBox.getWidth() - componentPadding);
            textBox.setY(componentY + 2F);
            return;
        }

        if (comp instanceof CompImageSelect) {
            CompImageSelect imageSelect = (CompImageSelect) comp;
            imageSelect.setX(right - 120F);
            imageSelect.setY(componentY + 2F);
            return;
        }

        if (comp instanceof CompSoundSelect) {
            CompSoundSelect soundSelect = (CompSoundSelect) comp;
            soundSelect.setX(right - 120F);
            soundSelect.setY(componentY + 2F);
            return;
        }

        if (comp instanceof CompColorPicker) {
            CompColorPicker picker = (CompColorPicker) comp;
            float scale = Math.max(0.6F, Math.min(1.0F, width / 180F));
            picker.setScale(scale);
            float pickerWidth = 118F * scale;
            float pickerX = Math.max(x + componentPadding, right - pickerWidth - componentPadding);
            picker.setX(pickerX);
            picker.setY(componentY);
            return;
        }

        if (comp instanceof CompCellGrid) {
            CompCellGrid grid = (CompCellGrid) comp;
            grid.setWidth(width - 24F);
            grid.setHeight(height - 40F);
            grid.setX(x + 12F);
            grid.setY(componentY + 6F);
            return;
        }
    }

    private float resolveTextWidth(Entry entry, PositionedEntry positioned, float textX) {
        float available = positioned.width - (textX - positioned.x);

        if (entry.comp instanceof CompSlider || entry.comp instanceof CompComboBox) {
            float controlLeft = entry.comp.getX();
            float spacing = TEXT_GAP;
            float controlSpace = controlLeft - textX - spacing;
            float baseWidth = Math.max(90F, positioned.width - 120F);
            float resolved = controlSpace > 0 ? Math.min(baseWidth, controlSpace) : baseWidth;
            return Math.max(80F, Math.min(resolved, positioned.width - 32F));
        }

        if (entry.comp instanceof CompToggleButton) {
            CompToggleButton toggle = (CompToggleButton) entry.comp;
            float controlLeft = toggle.getX();
            float spacing = TEXT_GAP;
            float controlSpace = controlLeft - textX - spacing;
            float baseWidth = Math.max(90F, positioned.width - 140F);
            float resolved = controlSpace > 0 ? Math.min(baseWidth, controlSpace) : baseWidth;
            return Math.max(80F, resolved);
        }

        if (entry.comp instanceof CompColorPicker || entry.comp instanceof CompCellGrid) {
            return Math.max(120F, positioned.width - 28F);
        }

        return Math.max(110F, available - 12F);
    }

    private TooltipData drawLabels(NanoVGManager nvg, ColorPalette palette, Entry entry, float textX, float textY, float textWidth, float hoverProgress, int mouseX, int mouseY) {
        Setting setting = entry.setting;
        SettingMetadata metadata = setting.getMetadata();

        String titleFull = setting.getName();
        TruncatedText titleLimited = limitText(nvg, titleFull, TITLE_FONT_SIZE, Fonts.MEDIUM, textWidth);
        String title = titleLimited.getText();
        boolean titleTruncated = titleLimited.isTruncated();

        Color titleColor = ColorUtils.interpolateColor(palette.getFontColor(ColorType.DARK),
                ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 230), hoverProgress * 0.3F);
        nvg.drawText(title, textX, textY, titleColor, TITLE_FONT_SIZE, Fonts.MEDIUM);

        float nextLineY = textY + TITLE_FONT_SIZE + 3F;
        boolean descriptionTruncated = false;
        String descriptionFull = null;
        boolean hasDescription = false;

        if (metadata != null && metadata.getDescription() != null && !metadata.getDescription().isEmpty()) {
            descriptionFull = metadata.getDescription();
            hasDescription = true;
            TruncatedText limitedDesc = limitText(nvg, descriptionFull, DESCRIPTION_FONT_SIZE, Fonts.REGULAR, textWidth);
            descriptionTruncated = limitedDesc.isTruncated();
            String description = limitedDesc.getText();
            Color descColor = ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 200 + (int) (hoverProgress * 30F));
            nvg.drawText(description, textX, nextLineY, descColor, DESCRIPTION_FONT_SIZE, Fonts.REGULAR);
        }

        float labelHeight = (metadata != null && metadata.getDescription() != null && !metadata.getDescription().isEmpty())
                ? (nextLineY - textY) + DESCRIPTION_FONT_SIZE
                : TITLE_FONT_SIZE;

        boolean hoveringText = MouseUtils.isInside(mouseX, mouseY, textX, textY - 2F, Math.max(textWidth, 60F), Math.max(labelHeight + 4F, 18F));
        if (!hoveringText) {
            return null;
        }

        if (!titleTruncated && !descriptionTruncated && !hasDescription) {
            return null;
        }

        TooltipData tooltipData = new TooltipData();
        if (titleTruncated) {
            tooltipData.addLine(new TooltipLine(titleFull, TITLE_FONT_SIZE, Fonts.MEDIUM,
                    palette.getFontColor(ColorType.DARK)));
        }
        if (hasDescription && descriptionFull != null) {
            tooltipData.addLine(new TooltipLine(descriptionFull, DESCRIPTION_FONT_SIZE, Fonts.REGULAR,
                    ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 230)));
        }
        return tooltipData;
    }

    private void drawCategoryCard(NanoVGManager nvg, ColorPalette palette, CategoryLayout layout) {
        nvg.drawContainer(layout.getCardX(), layout.getCardY(), layout.getCardWidth(), layout.getCardHeight(), CATEGORY_CARD_RADIUS, palette);
    }

    private void drawTooltip(NanoVGManager nvg, ColorPalette palette, TooltipData tooltip, int mouseX, int mouseY, float contentX, float contentY, float contentWidth, float viewportHeight) {
        List<TooltipLine> wrappedLines = new ArrayList<>();
        for (TooltipLine line : tooltip.getLines()) {
            wrappedLines.addAll(wrapLine(nvg, line));
        }

        if (wrappedLines.isEmpty()) {
            return;
        }

        float padding = 8F;
        float lineSpacing = 3F;
        float width = 0F;
        float height = padding * 2F;

        for (int i = 0; i < wrappedLines.size(); i++) {
            TooltipLine line = wrappedLines.get(i);
            float lineWidth = nvg.getTextWidth(line.getText(), line.getSize(), line.getFont());
            width = Math.max(width, lineWidth);
            height += line.getSize();
            if (i < wrappedLines.size() - 1) {
                height += lineSpacing;
            }
        }
        width += padding * 2F;

        float tooltipX = mouseX + 12F;
        float tooltipY = mouseY + 12F;
        float maxX = contentX + contentWidth - 6F;
        float maxY = contentY + viewportHeight - 6F;

        if (tooltipX + width > maxX) {
            tooltipX = maxX - width;
        }
        if (tooltipY + height > maxY) {
            tooltipY = maxY - height;
        }

        tooltipX = Math.max(contentX + 6F, tooltipX);
        tooltipY = Math.max(contentY + 6F, tooltipY);

        Color background = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 235);
        Color outline = ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 90);
        nvg.drawRoundedRect(tooltipX, tooltipY, width, height, 6F, background);
        nvg.drawOutlineRoundedRect(tooltipX, tooltipY, width, height, 6F, 1F, outline);

        float textY = tooltipY + padding;
        for (TooltipLine line : wrappedLines) {
            nvg.drawText(line.getText(), tooltipX + padding, textY, line.getColor(), line.getSize(), line.getFont());
            textY += line.getSize() + lineSpacing;
        }
    }

    private List<TooltipLine> wrapLine(NanoVGManager nvg, TooltipLine line) {
        List<TooltipLine> wrapped = new ArrayList<>();
        if (line.getText() == null || line.getText().isEmpty()) {
            return wrapped;
        }

        String[] words = line.getText().split(" ");
        StringBuilder current = new StringBuilder();
        for (String word : words) {
            String candidate = current.length() == 0 ? word : current + " " + word;
            if (nvg.getTextWidth(candidate, line.getSize(), line.getFont()) <= TOOLTIP_MAX_WIDTH || current.length() == 0) {
                current = new StringBuilder(candidate);
            } else {
                wrapped.add(new TooltipLine(current.toString(), line.getSize(), line.getFont(), line.getColor()));
                current = new StringBuilder(word);
            }
        }
        if (current.length() > 0) {
            wrapped.add(new TooltipLine(current.toString(), line.getSize(), line.getFont(), line.getColor()));
        }

        List<TooltipLine> adjusted = new ArrayList<>();
        for (TooltipLine tooltipLine : wrapped) {
            String text = tooltipLine.getText();
            while (nvg.getTextWidth(text, tooltipLine.getSize(), tooltipLine.getFont()) > TOOLTIP_MAX_WIDTH && text.length() > 1) {
                int end = text.length() - 1;
                while (end > 1 && nvg.getTextWidth(text.substring(0, end), tooltipLine.getSize(), tooltipLine.getFont()) > TOOLTIP_MAX_WIDTH) {
                    end--;
                }
                adjusted.add(new TooltipLine(text.substring(0, end), tooltipLine.getSize(), tooltipLine.getFont(), tooltipLine.getColor()));
                text = text.substring(end);
            }
            if (!text.isEmpty()) {
                adjusted.add(new TooltipLine(text, tooltipLine.getSize(), tooltipLine.getFont(), tooltipLine.getColor()));
            }
        }

        return adjusted;
    }

    private TruncatedText limitText(NanoVGManager nvg, String input, float size, Font font, float maxWidth) {
        if (input == null) {
            return new TruncatedText("", false);
        }
        String text = input;
        float fullWidth = nvg.getTextWidth(text, size, font);
        if (fullWidth <= maxWidth) {
            return new TruncatedText(text, false);
        }

        String ellipsis = "...";
        float ellipsisWidth = nvg.getTextWidth(ellipsis, size, font);
        while (!text.isEmpty() && nvg.getTextWidth(text, size, font) + ellipsisWidth > maxWidth) {
            text = text.substring(0, text.length() - 1);
        }
        if (text.isEmpty()) {
            text = "";
        }
        return new TruncatedText(text + ellipsis, true);
    }

    private static class Entry {
        final Setting setting;
        final Comp comp;
        final CategorySetting category;

        Entry(Setting setting, Comp comp, CategorySetting category) {
            this.setting = setting;
            this.comp = comp;
            this.category = category;
        }
    }

    @Data
    private static class PositionedEntry {
        final Entry entry;
        final float x;
        final float y;
        final float width;
        final float height;
    }

    private EntryState getState(Setting setting) {
        return entryStates.computeIfAbsent(setting, s -> new EntryState());
    }

    private static class EntryState {
        final SimpleAnimation hoverAnimation = new SimpleAnimation();
        final SimpleAnimation heightAnimation = new SimpleAnimation();
        boolean initialized = false;
    }

    private static class CategoryBlock {
        final CategorySetting category;
        final CompCategory header;
        final List<Entry> settings = new ArrayList<>();

        CategoryBlock(CategorySetting category, CompCategory header) {
            this.category = category;
            this.header = header;
        }

        boolean hasHeader() {
            return category != null && header != null;
        }

        boolean spansFullWidth() {
            return !hasHeader();
        }

        boolean isCollapsed() {
            return category != null && category.isCollapsed();
        }

        boolean hasContent() {
            return hasHeader() || !settings.isEmpty();
        }
    }

    @Data
    private static class CategoryLayout {
        private final CategoryBlock block;
        private CompCategory header;
        private float headerX;
        private float headerY;
        private float headerWidth;
        private float headerHeight;

        private float cardX;
        private float cardY;
        private float cardWidth;
        private float cardHeight;

        private List<PositionedEntry> entries = new ArrayList<>();

        boolean isCollapsed() {
            return block.isCollapsed();
        }

        float getTotalHeight() {
            float total = 0F;
            if (block.hasHeader()) {
                total += headerHeight;
            }
            if (!isCollapsed()) {
                if (block.hasHeader()) {
                    total += CATEGORY_HEADER_SPACING;
                }
                total += cardHeight;
            }
            return total;
        }

        float getBottom() {
            return (block.hasHeader() ? headerY : cardY) + getTotalHeight();
        }

        void setHeader(CompCategory header, float x, float y, float width, float height) {
            this.header = header;
            this.headerX = x;
            this.headerY = y;
            this.headerWidth = width;
            this.headerHeight = height;
        }

        void setCard(float x, float y, float width, float height) {
            this.cardX = x;
            this.cardY = y;
            this.cardWidth = width;
            this.cardHeight = height;
        }
    }

    @Data
    private static class TooltipLine {
        private final String text;
        private final float size;
        private final Font font;
        private final Color color;
    }

    @Data
    private static class TooltipData {
        private final List<TooltipLine> lines = new ArrayList<>();

        void addLine(TooltipLine line) {
            if (line != null) {
                lines.add(line);
            }
        }
    }

    @Data
    private static class TruncatedText {
        private final String text;
        private final boolean truncated;
    }
}

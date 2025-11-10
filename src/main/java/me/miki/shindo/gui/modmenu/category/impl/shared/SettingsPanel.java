package me.miki.shindo.gui.modmenu.category.impl.shared;

import lombok.Getter;
import me.miki.shindo.management.color.AccentColor;
import me.miki.shindo.management.color.palette.ColorPalette;
import me.miki.shindo.management.color.palette.ColorType;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.management.nanovg.font.Fonts;
import me.miki.shindo.management.settings.Setting;
import me.miki.shindo.management.settings.impl.CategorySetting;
import me.miki.shindo.management.settings.metadata.SettingMetadata;
import me.miki.shindo.ui.comp.Comp;
import me.miki.shindo.ui.comp.impl.*;
import me.miki.shindo.ui.comp.factory.SettingComponentFactory;
import me.miki.shindo.ui.framework.UIContext;
import me.miki.shindo.ui.framework.UIRenderer;
import me.miki.shindo.ui.framework.UIStyle;
import me.miki.shindo.utils.ColorUtils;
import me.miki.shindo.utils.animation.simple.SimpleAnimation;
import me.miki.shindo.utils.mouse.MouseUtils;
import me.miki.shindo.utils.mouse.Scroll;

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
    private static final float ROW_GAP = 8F;
    private static final float CATEGORY_HEIGHT = UIStyle.SETTING_TEXT_MARGIN + 18F;
    private static final float COLUMN_GAP = 10F;
    private static final float MIN_ROW_HEIGHT = 32F;

    private final List<Entry> entries = new ArrayList<>();
    private final List<PositionedEntry> positionedEntries = new ArrayList<>();
    private final Map<Setting, EntryState> entryStates = new HashMap<>();

    @Getter
    private LayoutMode layoutMode = LayoutMode.SINGLE_COLUMN;

    public void clear() {
        entries.clear();
        positionedEntries.clear();
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

        if (!positionedEntries.isEmpty()) {
            PositionedEntry last = positionedEntries.get(positionedEntries.size() - 1);
            float bottom = last.y + last.height;
            float contentHeight = bottom - (contentY + scroll.getValue());
            scroll.setMaxScroll(Math.max(0, contentHeight - viewportHeight));
        } else {
            scroll.setMaxScroll(0);
        }

        UIContext ctx = UIContext.get();
        AccentColor accentColor = ctx.accent();

        for (PositionedEntry positioned : positionedEntries) {
            Entry entry = positioned.entry;
            if (positioned.isCategory) {
                CompCategory category = (CompCategory) entry.comp;
                category.setWidth(positioned.width);
                category.setX(positioned.x);
                category.setY(positioned.y);
                category.draw(mouseX, mouseY, partialTicks);
                continue;
            }

            EntryState state = getState(entry.setting);

            if (entry.category != null && entry.category.isCollapsed()) {
                continue;
            }

            boolean hovered = MouseUtils.isInside(mouseX, mouseY, positioned.x, positioned.y, positioned.width, positioned.height);
            state.hoverAnimation.setAnimation(hovered ? 1F : 0F, 18);
            float hoverProgress = state.hoverAnimation.getValue();

            float backgroundRadius = UIStyle.SETTING_CORNER_RADIUS;
            float backgroundX = positioned.x;
            float backgroundY = positioned.y;
            float backgroundWidth = positioned.width;
            float backgroundHeight = positioned.height;

            UIRenderer.drawSettingSurface(ctx, palette, accentColor, backgroundX, backgroundY, backgroundWidth, backgroundHeight, backgroundRadius, hoverProgress);

            layoutComponent(entry.comp, positioned);

            float textX = backgroundX + UIStyle.SETTING_TEXT_MARGIN;
            float textY = backgroundY + 9;
            boolean sliderCompact = entry.comp instanceof CompSlider && positioned.width < 320F;
            float textColumnWidth;
            if (sliderCompact || entry.comp instanceof CompColorPicker || entry.comp instanceof CompCellGrid) {
                textColumnWidth = backgroundWidth - 28F;
            } else {
                float baseWidth = backgroundWidth - 110F;
                float controlLeft = entry.comp.getX();
                float available = controlLeft - textX - 18F;
                if (available > 0) {
                    textColumnWidth = Math.min(baseWidth, available);
                } else {
                    textColumnWidth = baseWidth;
                }
                textColumnWidth = Math.max(120F, Math.min(textColumnWidth, backgroundWidth - 28F));
            }

            float titleSize = 10.5F;
            String title = nvg.getLimitText(entry.setting.getName(), titleSize, Fonts.MEDIUM, textColumnWidth);
            nvg.drawText(title, textX, textY, palette.getFontColor(ColorType.DARK), titleSize, Fonts.MEDIUM);
            SettingMetadata metadata = entry.setting.getMetadata();
            if (metadata != null && !metadata.getDescription().isEmpty()) {
                float descriptionSize = 8.4F;
                String description = nvg.getLimitText(metadata.getDescription(), descriptionSize, Fonts.REGULAR, textColumnWidth);
                nvg.drawText(description, textX, textY + 12, ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 185), descriptionSize, Fonts.REGULAR);
            }

            entry.comp.draw(mouseX, mouseY, partialTicks);
        }
    }

    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton, float contentX, float contentY, float contentWidth, float viewportHeight, Scroll scroll) {

        updateLayout(contentX, contentY, contentWidth, scroll.getValue());

        for (PositionedEntry positioned : positionedEntries) {
            Entry entry = positioned.entry;

            if (positioned.y + positioned.height < contentY || positioned.y > contentY + viewportHeight) {
                continue;
            }

            if (positioned.isCategory) {
                CompCategory category = (CompCategory) entry.comp;
                if (MouseUtils.isInside(mouseX, mouseY, positioned.x, positioned.y, positioned.width, positioned.height)) {
                    category.mouseClicked(mouseX, mouseY, mouseButton);
                    return true;
                }
                continue;
            }

            if (entry.category != null && entry.category.isCollapsed()) {
                continue;
            }

            if (!MouseUtils.isInside(mouseX, mouseY, positioned.x, positioned.y, positioned.width, positioned.height)) {
                continue;
            }

            entry.comp.mouseClicked(mouseX, mouseY, mouseButton);
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
        positionedEntries.clear();

        float innerX = contentX + OUTER_MARGIN;
        float innerWidth = Math.max(0, contentWidth - (OUTER_MARGIN * 2));
        float yCursor = contentY + scrollOffset;

        CategorySetting currentCategory = null;
        List<RowEntry> pendingRow = new ArrayList<>(2);

        for (Entry entry : entries) {
            if (entry.setting instanceof CategorySetting) {
                if (!pendingRow.isEmpty()) {
                    yCursor += placeDoubleColumnRow(innerX, innerWidth, yCursor, pendingRow) + ROW_GAP;
                    pendingRow.clear();
                }

                currentCategory = (CategorySetting) entry.setting;
                float height = CATEGORY_HEIGHT;
                positionedEntries.add(new PositionedEntry(entry, innerX, yCursor, innerWidth, height, true));
                yCursor += height + ROW_GAP;
                continue;
            }

            EntryState state = getState(entry.setting);

            if (currentCategory != null && currentCategory.isCollapsed()) {
                state.heightAnimation.setAnimation(0F, 20);
                continue;
            }

            float targetHeight = calculateTargetHeight(entry.comp);
            if (!state.initialized) {
                state.heightAnimation.setValue(targetHeight);
                state.initialized = true;
            }
            state.heightAnimation.setAnimation(targetHeight, 20);
            float rowHeight = Math.max(MIN_ROW_HEIGHT, state.heightAnimation.getValue());

            if (layoutMode == LayoutMode.SINGLE_COLUMN) {
                if (!pendingRow.isEmpty()) {
                    yCursor += placeDoubleColumnRow(innerX, innerWidth, yCursor, pendingRow) + ROW_GAP;
                    pendingRow.clear();
                }
                positionedEntries.add(new PositionedEntry(entry, innerX, yCursor, innerWidth, rowHeight, false));
                yCursor += rowHeight + ROW_GAP;
                continue;
            }

            // DOUBLE COLUMN
            pendingRow.add(new RowEntry(entry, rowHeight));

            if (pendingRow.size() == 2) {
                yCursor += placeDoubleColumnRow(innerX, innerWidth, yCursor, pendingRow) + ROW_GAP;
                pendingRow.clear();
            }
        }

        if (!pendingRow.isEmpty()) {
            yCursor += placeDoubleColumnRow(innerX, innerWidth, yCursor, pendingRow);
            pendingRow.clear();
        }
    }

    private float placeDoubleColumnRow(float innerX, float innerWidth, float yCursor, List<RowEntry> rowEntries) {
        if (rowEntries.isEmpty()) {
            return 0F;
        }

        float availableWidth = innerWidth;
        float columnWidth = (availableWidth - COLUMN_GAP) / 2F;

        if (rowEntries.size() == 1) {
            RowEntry only = rowEntries.get(0);
            positionedEntries.add(new PositionedEntry(only.entry, innerX, yCursor, availableWidth, only.height, false));
            return only.height;
        }

        RowEntry left = rowEntries.get(0);
        RowEntry right = rowEntries.get(1);
        positionedEntries.add(new PositionedEntry(left.entry, innerX, yCursor, columnWidth, left.height, false));
        positionedEntries.add(new PositionedEntry(right.entry, innerX + columnWidth + COLUMN_GAP, yCursor, columnWidth, right.height, false));

        return Math.max(left.height, right.height);
    }

    private float calculateTargetHeight(Comp comp) {
        float base = 40F;

        if (comp instanceof CompSlider) {
            base = 50F;
        }

        if (comp instanceof CompComboBox) {
            base = 50F;
        }
        if (comp instanceof CompColorPicker) {
            CompColorPicker picker = (CompColorPicker) comp;
            if (picker.isOpen()) {
                base += picker.isShowAlpha() ? 112F : 92F;
            }
        }
        if (comp instanceof CompCellGrid) {
            base = Math.max(base, 200F);
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

        float componentPadding = UIStyle.COMPONENT_VERTICAL_PADDING;
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
            boolean compact = width < 320F;
            if (compact) {
                slider.setWidth(Math.max(0F, width - (componentPadding * 2)));
                slider.setX(x + componentPadding);
                slider.setY(componentY + 16F);
            } else {
                slider.setWidth(Math.max(110F, width - 200F));
                slider.setX((float) (right - slider.getWidth() - componentPadding));
                slider.setY(componentY + 2F);
            }
            return;
        }

        if (comp instanceof CompComboBox) {
            CompComboBox comboBox = (CompComboBox) comp;
            boolean compact = width < 320F;
            if (compact) {
                comboBox.setWidth(Math.max(0F, width - (componentPadding * 2)));
                comboBox.setX(x + componentPadding);
                comboBox.setY(componentY + 16F);
            } else {
                comboBox.setWidth(Math.max(110F, width - 200F));
                comboBox.setX((float) (right - comboBox.getWidth() - componentPadding));
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
            picker.setX(right - (120F * scale));
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

    private static class PositionedEntry {
        final Entry entry;
        final float x;
        final float y;
        final float width;
        final float height;
        final boolean isCategory;

        PositionedEntry(Entry entry, float x, float y, float width, float height, boolean isCategory) {
            this.entry = entry;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.isCategory = isCategory;
        }
    }

    private EntryState getState(Setting setting) {
        return entryStates.computeIfAbsent(setting, s -> new EntryState());
    }

    private static class EntryState {
        final SimpleAnimation hoverAnimation = new SimpleAnimation();
        final SimpleAnimation heightAnimation = new SimpleAnimation();
        boolean initialized = false;
    }

    private static class RowEntry {
        final Entry entry;
        final float height;

        RowEntry(Entry entry, float height) {
            this.entry = entry;
            this.height = height;
        }
    }
}

package me.miki.shindo.management.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import me.miki.shindo.gui.modmenu.category.impl.shared.SettingsPanel;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.impl.InternalSettingsMod;
import me.miki.shindo.management.nanovg.font.LegacyIcon;
import me.miki.shindo.management.screenshot.ScreenshotDisplayMode;

/**
 * Centraliza os layouts e aplica o tipo selecionado por area.
 */
public class UILayoutManager {

    public enum Layouts {
        SETTINGS(TranslateText.SETTINGS, TranslateText.SETTINGS_LAYOUT_DESCRIPTION, LegacyIcon.SETTINGS),
        MODULES(TranslateText.SETTINGS_LAYOUT_SECTION_MODULE, TranslateText.SETTINGS_LAYOUT_MODULE_SINGLE_DESCRIPTION, LegacyIcon.LIST),
        SCREENSHOTS(TranslateText.SCREENSHOT, TranslateText.SETTINGS_LAYOUT_SECTION_SCREENSHOT, LegacyIcon.CAMERA);

        private final TranslateText title;
        private final TranslateText description;
        private final String icon;

        Layouts(TranslateText title, TranslateText description, String icon) {
            this.title = title;
            this.description = description;
            this.icon = icon;
        }

        public String getTitle() {
            return title.getText();
        }

        public String getDescription() {
            return description.getText();
        }

        public String getIcon() {
            return icon;
        }
    }

    public enum LayoutType {
        SETTINGS_SINGLE(Layouts.SETTINGS, TranslateText.SETTINGS_LAYOUT_SINGLE_TITLE, TranslateText.SETTINGS_LAYOUT_SINGLE_DESCRIPTION),
        SETTINGS_DOUBLE(Layouts.SETTINGS, TranslateText.SETTINGS_LAYOUT_DOUBLE_TITLE, TranslateText.SETTINGS_LAYOUT_DOUBLE_DESCRIPTION),
        MODULES_SINGLE(Layouts.MODULES, TranslateText.SETTINGS_LAYOUT_MODULE_SINGLE_TITLE, TranslateText.SETTINGS_LAYOUT_MODULE_SINGLE_DESCRIPTION),
        MODULES_DOUBLE(Layouts.MODULES, TranslateText.SETTINGS_LAYOUT_MODULE_DOUBLE_TITLE, TranslateText.SETTINGS_LAYOUT_MODULE_DOUBLE_DESCRIPTION),
        SCREEN_FILMSTRIP(Layouts.SCREENSHOTS, ScreenshotDisplayMode.FILMSTRIP.getTranslate(), ScreenshotDisplayMode.FILMSTRIP.getTranslateDescription()),
        SCREEN_GRID(Layouts.SCREENSHOTS, ScreenshotDisplayMode.GRID.getTranslate(), ScreenshotDisplayMode.GRID.getTranslateDescription());

        private final Layouts area;
        private final TranslateText title;
        private final TranslateText description;
        private Runnable applier;
        private Supplier<Boolean> selectedSupplier;

        LayoutType(Layouts area, TranslateText title, TranslateText description) {
            this.area = area;
            this.title = title;
            this.description = description;
        }

        public Layouts getArea() {
            return area;
        }

        public String getTitle() {
            return title.getText();
        }

        public String getDescription() {
            return description.getText();
        }

        void bind(Runnable applier, Supplier<Boolean> selectedSupplier) {
            this.applier = applier;
            this.selectedSupplier = selectedSupplier;
        }

        public void apply() {
            if (applier != null) {
                applier.run();
            }
        }

        public boolean isSelected() {
            return selectedSupplier != null && selectedSupplier.get();
        }
    }

    public UILayoutManager() {
        bindDefaults();
    }

    private void bindDefaults() {
        InternalSettingsMod mod = InternalSettingsMod.getInstance();

        bind(LayoutType.SETTINGS_SINGLE,
                () -> mod.setSettingsLayoutMode(SettingsPanel.LayoutMode.SINGLE_COLUMN),
                () -> mod.getSettingsLayoutMode() == SettingsPanel.LayoutMode.SINGLE_COLUMN);
        bind(LayoutType.SETTINGS_DOUBLE,
                () -> mod.setSettingsLayoutMode(SettingsPanel.LayoutMode.DOUBLE_COLUMN),
                () -> mod.getSettingsLayoutMode() == SettingsPanel.LayoutMode.DOUBLE_COLUMN);

        bind(LayoutType.MODULES_SINGLE,
                () -> mod.setModuleGridColumns(1),
                () -> mod.getModuleGridColumns() <= 1);
        bind(LayoutType.MODULES_DOUBLE,
                () -> mod.setModuleGridColumns(2),
                () -> mod.getModuleGridColumns() >= 2);

        bind(LayoutType.SCREEN_FILMSTRIP,
                () -> mod.setScreenshotDisplayMode(ScreenshotDisplayMode.FILMSTRIP),
                () -> mod.getScreenshotDisplayMode() == ScreenshotDisplayMode.FILMSTRIP);
        bind(LayoutType.SCREEN_GRID,
                () -> mod.setScreenshotDisplayMode(ScreenshotDisplayMode.GRID),
                () -> mod.getScreenshotDisplayMode() == ScreenshotDisplayMode.GRID);
    }

    private void bind(LayoutType type, Runnable applier, Supplier<Boolean> selectedSupplier) {
        type.bind(applier, selectedSupplier);
    }

    public List<LayoutType> getTypes(Layouts area) {
        List<LayoutType> list = new ArrayList<LayoutType>();
        for (LayoutType type : LayoutType.values()) {
            if (type.getArea() == area) {
                list.add(type);
            }
        }
        return Collections.unmodifiableList(list);
    }

    public LayoutType getSelectedType(Layouts area) {
        for (LayoutType type : getTypes(area)) {
            if (type.isSelected()) {
                return type;
            }
        }
        List<LayoutType> list = getTypes(area);
        return list.isEmpty() ? null : list.get(0);
    }

    public void selectType(LayoutType type) {
        if (type != null) {
            type.apply();
        }
    }
}

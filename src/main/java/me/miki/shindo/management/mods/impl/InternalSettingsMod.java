package me.miki.shindo.management.mods.impl;

import lombok.Getter;
import me.miki.shindo.Shindo;
import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventKey;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import me.miki.shindo.management.settings.impl.*;
import me.miki.shindo.management.settings.metadata.SettingRegistry;
import me.miki.shindo.gui.modmenu.category.impl.shared.SettingsPanel;
import me.miki.shindo.management.screenshot.ScreenshotDisplayMode;
import org.lwjgl.input.Keyboard;

import me.miki.shindo.management.settings.config.Property;
import me.miki.shindo.management.settings.config.PropertyEnum;
import me.miki.shindo.management.settings.config.PropertyType;

import java.util.Objects;

public class InternalSettingsMod extends Mod {

    @Getter
    private static InternalSettingsMod instance;

    @Getter
    @Property(type = PropertyType.COMBO, translate = TranslateText.HUD_THEME)
    private HudTheme hudTheme = HudTheme.NORMAL;

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.UI_BLUR)
    private boolean blurSetting = true;

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.MC_FONT)
    private boolean mcFontSetting = false;

    @Property(type = PropertyType.NUMBER, translate = TranslateText.VOLUME, min = 0, max = 1, current = 0.8)
    private double volumeSetting = 0.8;

    @Property(type = PropertyType.KEYBIND, translate = TranslateText.KEYBIND, keyCode = Keyboard.KEY_RSHIFT)
    private int modMenuKeybindSetting = Keyboard.KEY_RSHIFT;

    @Property(type = PropertyType.TEXT, translate = TranslateText.CUSTOM_CAPE, text = "None")
    private String capeNameSetting = "None";

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.CLICK_EFFECT)
    private boolean clickEffectsSetting = true;

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.UI_SOUNDS)
    private boolean soundsUISetting = true;

    @Getter
    @Property(type = PropertyType.COMBO, name = "Settings Layout")
    private SettingsLayout settingsLayout = SettingsLayout.SINGLE_COLUMN;

    @Getter
    @Property(type = PropertyType.COMBO, name = "Module Layout")
    private ModuleLayout moduleLayout = ModuleLayout.SINGLE_COLUMN;

    @Getter
    @Property(type = PropertyType.COMBO, name = "Screenshot Display")
    private ScreenshotDisplayMode screenshotDisplayMode = ScreenshotDisplayMode.FILMSTRIP;

    public InternalSettingsMod() {
        super(TranslateText.NONE, TranslateText.NONE, ModCategory.OTHER);

        instance = this;
    }

    @Override
    public void setup() {
        this.setHide(true);
        this.setToggled(true);
    }

    @EventTarget
    public void onKey(EventKey event) {
        if (event.getKeyCode() == modMenuKeybindSetting) {
            mc.displayGuiScreen(Shindo.getInstance().getShindoAPI().getModMenu());
        }

//		Uncomment to enable the ability to change the theme of the mod menu using the down arrow key
        if (event.getKeyCode() == Keyboard.KEY_DOWN) {
            ComboSetting combo = getModThemeSetting();
            if (combo != null) {
                int max = combo.getOptions().size();
                int modeIndex = combo.getOptions().indexOf(combo.getOption());
                modeIndex = modeIndex > 0 ? modeIndex - 1 : max - 1;
                combo.setOption(combo.getOptions().get(modeIndex));
            }
        }
    }

    public BooleanSetting getClickEffectsSetting() {
        return SettingRegistry.getBooleanSetting(this, "clickEffectsSetting");
    }

    public BooleanSetting getSoundsUISetting() {
        return SettingRegistry.getBooleanSetting(this, "soundsUISetting");
    }

    public ComboSetting getSettingsLayoutSetting() {
        return SettingRegistry.getComboSetting(this, "settingsLayout");
    }

    public ComboSetting getModuleLayoutSetting() {
        return SettingRegistry.getComboSetting(this, "moduleLayout");
    }

    public ComboSetting getScreenshotDisplaySetting() {
        return SettingRegistry.getComboSetting(this, "screenshotDisplayMode");
    }

    public SettingsPanel.LayoutMode getSettingsLayoutMode() {
        return settingsLayout == SettingsLayout.COMPACT_GRID
                ? SettingsPanel.LayoutMode.DOUBLE_COLUMN
                : SettingsPanel.LayoutMode.SINGLE_COLUMN;
    }

    public void setSettingsLayoutMode(SettingsPanel.LayoutMode mode) {
        SettingsLayout target = mode == SettingsPanel.LayoutMode.DOUBLE_COLUMN
                ? SettingsLayout.COMPACT_GRID
                : SettingsLayout.SINGLE_COLUMN;
        ComboSetting combo = getSettingsLayoutSetting();
        if (combo != null && target.ordinal() < combo.getOptions().size()) {
            combo.setOption(combo.getOptions().get(target.ordinal()));
        }
    }

    public int getModuleGridColumns() {
        if (Objects.requireNonNull(moduleLayout) == ModuleLayout.TWO_COLUMNS) {
            return 2;
        }
        return 1;
    }

    public void setModuleGridColumns(int columns) {
        int normalized = Math.max(1, Math.min(columns, ModuleLayout.values().length));
        ModuleLayout target = ModuleLayout.values()[normalized - 1];
        ComboSetting combo = getModuleLayoutSetting();
        if (combo != null && target.ordinal() < combo.getOptions().size()) {
            combo.setOption(combo.getOptions().get(target.ordinal()));
        }
    }

    public void setScreenshotDisplayMode(ScreenshotDisplayMode mode) {
        ScreenshotDisplayMode target = mode == null ? ScreenshotDisplayMode.FILMSTRIP : mode;
        ComboSetting combo = getScreenshotDisplaySetting();
        if (combo != null && target.ordinal() < combo.getOptions().size()) {
            combo.setOption(combo.getOptions().get(target.ordinal()));
        }
    }

    public NumberSetting getVolumeSetting() {
        return SettingRegistry.getNumberSetting(this, "volumeSetting");
    }

    public ComboSetting getModThemeSetting() {
        return SettingRegistry.getComboSetting(this, "hudTheme");
    }

    public BooleanSetting getBlurSetting() {
        return SettingRegistry.getBooleanSetting(this, "blurSetting");
    }

    public KeybindSetting getModMenuKeybindSetting() {
        return SettingRegistry.getKeybindSetting(this, "modMenuKeybindSetting");
    }

    public BooleanSetting getMCHUDFont() {
        return SettingRegistry.getBooleanSetting(this, "mcFontSetting");
    }

    public String getCapeConfigName() {
        return capeNameSetting;
    }

    public void setCapeConfigName(String a) {
        capeNameSetting = a;
    }

    public enum HudTheme implements PropertyEnum {
        NORMAL(TranslateText.NORMAL),
        GLOW(TranslateText.GLOW),
        OUTLINE(TranslateText.OUTLINE),
        VANILLA(TranslateText.VANILLA),
        OUTLINE_GLOW(TranslateText.OUTLINE_GLOW),
        VANILLA_GLOW(TranslateText.VANILLA_GLOW),
        SHADOW(TranslateText.SHADOW),
        DARK(TranslateText.DARK),
        LIGHT(TranslateText.LIGHT),
        RECT(TranslateText.RECT),
        MODERN(TranslateText.MODERN),
        TEXT(TranslateText.TEXT),
        GRADIENT_SIMPLE(TranslateText.GRADIENT_SIMPLE);

        private final TranslateText translate;

        HudTheme(TranslateText translate) {
            this.translate = translate;
        }

        @Override
        public TranslateText getTranslate() {
            return translate;
        }
    }

    public enum SettingsLayout implements PropertyEnum {
        SINGLE_COLUMN("Single Column"),
        COMPACT_GRID("Compact Grid");

        private final String displayName;

        SettingsLayout(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String getDisplayName() {
            return displayName;
        }
    }

    public enum ModuleLayout implements PropertyEnum {
        SINGLE_COLUMN("Single Column"),
        TWO_COLUMNS("Two Columns");

        private final String displayName;

        ModuleLayout(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String getDisplayName() {
            return displayName;
        }
    }
}

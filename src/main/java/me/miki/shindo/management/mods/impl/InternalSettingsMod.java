package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.nanovg.font.LegacyIcon;
import lombok.Getter;
import me.miki.shindo.Shindo;
import me.miki.shindo.injection.interfaces.IMixinMinecraft;
import me.miki.shindo.logger.ShindoLogger;
import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventKey;
import me.miki.shindo.management.event.impl.EventPreRenderTick;
import me.miki.shindo.management.event.impl.EventToggleFullscreen;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import me.miki.shindo.management.settings.impl.*;
import me.miki.shindo.management.settings.metadata.SettingRegistry;
import me.miki.shindo.gui.modmenu.category.impl.shared.SettingsPanel;
import me.miki.shindo.management.screenshot.ScreenshotDisplayMode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

import me.miki.shindo.management.settings.config.Property;
import me.miki.shindo.management.settings.config.PropertyEnum;
import me.miki.shindo.management.settings.config.PropertyType;

import java.util.Objects;

public class InternalSettingsMod extends Mod {

    private static InternalSettingsMod instance;

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

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.BORDERLESS_FULSCREEN)
    private boolean borderlessFullscreenSetting = false;

    @Property(type = PropertyType.COMBO, name = "Settings Layout")
    private SettingsLayout settingsLayout = SettingsLayout.SINGLE_COLUMN;

    @Property(type = PropertyType.COMBO, name = "Module Layout")
    private ModuleLayout moduleLayout = ModuleLayout.SINGLE_COLUMN;

    @Property(type = PropertyType.COMBO, name = "Addon Layout")
    private AddonLayout addonLayout = AddonLayout.STANDARD;

    @Property(type = PropertyType.COMBO, name = "Screenshot Display")
    private ScreenshotDisplayMode screenshotDisplayMode = ScreenshotDisplayMode.FILMSTRIP;

    @Property(type = PropertyType.COMBO, translate = TranslateText.NOTIFICATION_POSITION)
    private NotificationCorner notificationCorner = NotificationCorner.BOTTOM_RIGHT;

    private int prevX, prevY, prevWidth, prevHeight;
    private long fullscreenTime = -1;
    private boolean lastBorderlessState = false;
    private boolean borderlessInitialized = false;

    public InternalSettingsMod() {
        super(TranslateText.NONE, TranslateText.NONE, ModCategory.OTHER, LegacyIcon.MOD_INTERNAL_SETTINGS);

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

        // Uncomment to enable the ability to change the theme of the mod menu using the down arrow key
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

    @EventTarget
    public void onRenderTick(EventPreRenderTick event) {
        if (fullscreenTime != -1 && System.currentTimeMillis() - fullscreenTime >= 100) {
            fullscreenTime = -1;

            if (mc.inGameHasFocus) {
                mc.mouseHelper.grabMouseCursor();
            }
        }

        if (borderlessInitialized && borderlessFullscreenSetting != lastBorderlessState) {
            applyBorderlessSetting(borderlessFullscreenSetting, false);
        }
    }

    @EventTarget
    public void onFullscreenToggle(EventToggleFullscreen event) {
        if (!borderlessFullscreenSetting) {
            return;
        }
        event.setApplyState(false);
        setBorderlessFullscreen(event.getState());
    }

    public static InternalSettingsMod getInstance() {
        return instance;
    }

    public HudTheme getHudTheme() {
        return hudTheme;
    }

    public ScreenshotDisplayMode getScreenshotDisplayMode() {
        return screenshotDisplayMode;
    }

    public NotificationCorner getNotificationCorner() {
        return notificationCorner;
    }

    public ModuleLayout getModuleLayout() {
        return moduleLayout;
    }

    public AddonLayout getAddonLayout() {
        return addonLayout;
    }

    public SettingsLayout getSettingsLayout() {
        return settingsLayout;
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

    public ComboSetting getAddonLayoutSetting() {
        return SettingRegistry.getComboSetting(this, "addonLayout");
    }

    public ComboSetting getScreenshotDisplaySetting() {
        return SettingRegistry.getComboSetting(this, "screenshotDisplayMode");
    }

    public ComboSetting getNotificationCornerSetting() {
        return SettingRegistry.getComboSetting(this, "notificationCorner");
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
        if (moduleLayout == ModuleLayout.ICON_CARDS) {
            return 3;
        }
        return 1;
    }

    public void setModuleGridColumns(int columns) {
        int normalized = Math.max(1, Math.min(columns, 2));
        ModuleLayout target = normalized == 2 ? ModuleLayout.TWO_COLUMNS : ModuleLayout.SINGLE_COLUMN;
        ComboSetting combo = getModuleLayoutSetting();
        if (combo != null && target.ordinal() < combo.getOptions().size()) {
            combo.setOption(combo.getOptions().get(target.ordinal()));
        }
    }

    public void setModuleLayout(ModuleLayout layout) {
        ModuleLayout target = layout == null ? ModuleLayout.SINGLE_COLUMN : layout;
        ComboSetting combo = getModuleLayoutSetting();
        if (combo != null && target.ordinal() < combo.getOptions().size()) {
            combo.setOption(combo.getOptions().get(target.ordinal()));
        }
    }

    public void setAddonLayout(AddonLayout layout) {
        AddonLayout target = layout == null ? AddonLayout.STANDARD : layout;
        ComboSetting combo = getAddonLayoutSetting();
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

    public void setNotificationCorner(NotificationCorner corner) {
        NotificationCorner target = corner == null ? NotificationCorner.BOTTOM_RIGHT : corner;
        ComboSetting combo = getNotificationCornerSetting();
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

    public BooleanSetting getBorderlessFullscreenSetting() {
        return SettingRegistry.getBooleanSetting(this, "borderlessFullscreenSetting");
    }

    public void applyBorderlessOnStartup() {
        borderlessInitialized = true;
        lastBorderlessState = borderlessFullscreenSetting;
        applyBorderlessSetting(borderlessFullscreenSetting, true);
    }

    private void applyBorderlessSetting(boolean state, boolean force) {
        if (!force && state == lastBorderlessState) {
            return;
        }

        lastBorderlessState = state;
        borderlessInitialized = true;

        if (!mc.isFullScreen()) {
            return;
        }

        if (state) {
            setBorderlessFullscreen(true);
        } else {
            setBorderlessFullscreen(false);
            mc.toggleFullscreen();
            mc.toggleFullscreen();
        }
    }

    private void setBorderlessFullscreen(boolean state) {
        try {
            System.setProperty("org.lwjgl.opengl.Window.undecorated", Boolean.toString(state));
            Display.setFullscreen(false);
            Display.setResizable(!state);

            if (state) {
                prevX = Display.getX();
                prevY = Display.getY();
                prevWidth = mc.displayWidth;
                prevHeight = mc.displayHeight;
                Display.setDisplayMode(new DisplayMode(Display.getDesktopDisplayMode().getWidth(), Display.getDesktopDisplayMode().getHeight()));
                Display.setLocation(0, 0);
                ((IMixinMinecraft) mc).resizeWindow(Display.getDesktopDisplayMode().getWidth(), Display.getDesktopDisplayMode().getHeight());
            } else {
                Display.setDisplayMode(new DisplayMode(prevWidth, prevHeight));
                Display.setLocation(prevX, prevY);
                ((IMixinMinecraft) mc).resizeWindow(prevWidth, prevHeight);

                if (mc.inGameHasFocus) {
                    mc.mouseHelper.ungrabMouseCursor();
                    fullscreenTime = System.currentTimeMillis();
                }
            }
        } catch (LWJGLException error) {
            ShindoLogger.error("Could not toggle borderless fullscreen", error);
        }
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

        @Override
        @Nullable
        public String getNameKey() {
            return PropertyEnum.super.getNameKey();
        }

        @Override
        @Nullable
        public String getDisplayName() {
            return PropertyEnum.super.getDisplayName();
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
        @Nullable
        public String getDisplayName() {
            return displayName;
        }

    }

    public enum ModuleLayout implements PropertyEnum {
        SINGLE_COLUMN("Single Column"),
        TWO_COLUMNS("Two Columns"),
        ICON_CARDS("Icon Cards");

        private final String displayName;

        ModuleLayout(String displayName) {
            this.displayName = displayName;
        }

        @Override
        @Nullable
        public String getDisplayName() {
            return displayName;
        }
    }

    public enum AddonLayout implements PropertyEnum {
        STANDARD("Standard"),
        ICON_CARDS("Icon Cards");

        private final String displayName;

        AddonLayout(String displayName) {
            this.displayName = displayName;
        }

        @Override
        @Nullable
        public String getDisplayName() {
            return displayName;
        }
    }

    public enum NotificationCorner implements PropertyEnum {
        TOP_LEFT(TranslateText.NOTIFICATION_POSITION_TOP_LEFT),
        TOP_RIGHT(TranslateText.NOTIFICATION_POSITION_TOP_RIGHT),
        BOTTOM_LEFT(TranslateText.NOTIFICATION_POSITION_BOTTOM_LEFT),
        BOTTOM_RIGHT(TranslateText.NOTIFICATION_POSITION_BOTTOM_RIGHT);

        private final TranslateText translate;

        NotificationCorner(TranslateText translate) {
            this.translate = translate;
        }

        @Override
        public TranslateText getTranslate() {
            return translate;
        }
    }
}





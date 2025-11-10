package me.miki.shindo.ui.comp.factory;

import me.miki.shindo.management.settings.Setting;
import me.miki.shindo.management.settings.impl.BooleanSetting;
import me.miki.shindo.management.settings.impl.CellGridSetting;
import me.miki.shindo.management.settings.impl.ColorSetting;
import me.miki.shindo.management.settings.impl.ComboSetting;
import me.miki.shindo.management.settings.impl.ImageSetting;
import me.miki.shindo.management.settings.impl.KeybindSetting;
import me.miki.shindo.management.settings.impl.NumberSetting;
import me.miki.shindo.management.settings.impl.SoundSetting;
import me.miki.shindo.management.settings.impl.TextSetting;
import me.miki.shindo.ui.comp.Comp;
import me.miki.shindo.ui.comp.impl.CompCellGrid;
import me.miki.shindo.ui.comp.impl.CompColorPicker;
import me.miki.shindo.ui.comp.impl.CompComboBox;
import me.miki.shindo.ui.comp.impl.CompImageSelect;
import me.miki.shindo.ui.comp.impl.CompKeybind;
import me.miki.shindo.ui.comp.impl.CompModTextBox;
import me.miki.shindo.ui.comp.impl.CompSlider;
import me.miki.shindo.ui.comp.impl.CompSoundSelect;
import me.miki.shindo.ui.comp.impl.CompToggleButton;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Central registry responsible for turning {@link Setting} instances into UI components. This makes
 * it easy to plug new component types without editing the panel code and keeps the factory logic in one place.
 */
public final class SettingComponentFactory {

    private static final Map<Class<? extends Setting>, Function<Setting, Comp>> REGISTRY = new LinkedHashMap<>();

    static {
        register(BooleanSetting.class, setting -> new CompToggleButton((BooleanSetting) setting));
        register(NumberSetting.class, setting -> new CompSlider((NumberSetting) setting));
        register(ComboSetting.class, setting -> new CompComboBox(140, (ComboSetting) setting));
        register(KeybindSetting.class, setting -> new CompKeybind(120, (KeybindSetting) setting));
        register(TextSetting.class, setting -> new CompModTextBox((TextSetting) setting));
        register(ColorSetting.class, setting -> new CompColorPicker((ColorSetting) setting));
        register(ImageSetting.class, setting -> new CompImageSelect((ImageSetting) setting));
        register(SoundSetting.class, setting -> new CompSoundSelect((SoundSetting) setting));
        register(CellGridSetting.class, setting -> new CompCellGrid(270, 160, (CellGridSetting) setting));
    }

    private SettingComponentFactory() {
    }

    public static void register(Class<? extends Setting> type, Function<? super Setting, ? extends Comp> factory) {
        REGISTRY.put(type, setting -> factory.apply(setting));
    }

    public static Comp create(Setting setting) {
        for (Map.Entry<Class<? extends Setting>, Function<Setting, Comp>> entry : REGISTRY.entrySet()) {
            if (entry.getKey().isInstance(setting)) {
                return entry.getValue().apply(setting);
            }
        }
        return null;
    }
}

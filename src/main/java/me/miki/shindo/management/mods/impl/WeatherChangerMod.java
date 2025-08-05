package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import me.miki.shindo.management.mods.settings.impl.ComboSetting;
import me.miki.shindo.management.mods.settings.impl.NumberSetting;
import me.miki.shindo.management.mods.settings.impl.combo.Option;

import java.util.ArrayList;
import java.util.Arrays;

public class WeatherChangerMod extends Mod {

    private static WeatherChangerMod instance;

    private final ComboSetting weatherSetting = new ComboSetting(TranslateText.WEATHER, this, TranslateText.CLEAR, new ArrayList<Option>(Arrays.asList(
            new Option(TranslateText.CLEAR), new Option(TranslateText.RAIN), new Option(TranslateText.STORM), new Option(TranslateText.SNOW))));

    private final NumberSetting rainStrength = new NumberSetting(TranslateText.RAIN_STRENGTH, this, 1, 0, 1, false);
    private final NumberSetting thunderStrength = new NumberSetting(TranslateText.THUNDER_STRENGTH, this, 1, 0, 1, false);

    public WeatherChangerMod() {
        super(TranslateText.WEATHER_CHANGER, TranslateText.WEATHER_CHANGER_DESCRIPTION, ModCategory.WORLD);

        instance = this;
    }

    public static WeatherChangerMod getInstance() {
        return instance;
    }

    public ComboSetting getWeatherSetting() {
        return weatherSetting;
    }

    public NumberSetting getRainStrength() {
        return rainStrength;
    }

    public NumberSetting getThunderStrength() {
        return thunderStrength;
    }
}

package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.nanovg.font.LegacyIcon;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import me.miki.shindo.management.settings.config.Property;
import me.miki.shindo.management.settings.config.PropertyEnum;
import me.miki.shindo.management.settings.config.PropertyType;
import me.miki.shindo.management.settings.impl.NumberSetting;
import me.miki.shindo.management.settings.metadata.SettingRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WeatherChangerMod extends Mod {

    private static WeatherChangerMod instance;

    @Property(type = PropertyType.COMBO, translate = TranslateText.WEATHER)
    private Weather weather = Weather.CLEAR;

    @Property(type = PropertyType.NUMBER, translate = TranslateText.RAIN_STRENGTH, min = 0, max = 1, current = 1)
    private double rainStrength = 1;

    @Property(type = PropertyType.NUMBER, translate = TranslateText.THUNDER_STRENGTH, min = 0, max = 1, current = 1)
    private double thunderStrength = 1;

    public WeatherChangerMod() {
        super(TranslateText.WEATHER_CHANGER, TranslateText.WEATHER_CHANGER_DESCRIPTION, ModCategory.WORLD, LegacyIcon.MOD_WEATHER_CHANGER);

        instance = this;
    }

    public NumberSetting getRainStrength() {
        return SettingRegistry.getNumberSetting(this, "rainStrength");
    }

    public NumberSetting getThunderStrength() {
        return SettingRegistry.getNumberSetting(this, "thunderStrength");
    }

    public enum Weather implements PropertyEnum {
        CLEAR(TranslateText.CLEAR),
        RAIN(TranslateText.RAIN),
        STORM(TranslateText.STORM),
        SNOW(TranslateText.SNOW);

        private final TranslateText translate;

        Weather(TranslateText translate) {
            this.translate = translate;
        }

        @NotNull
        @Override
        public TranslateText getTranslate() {
            return translate;
        }
    }

    public Weather getWeather() {
        return weather;
    }

    public static WeatherChangerMod getInstance() {
        return instance;
    }
}





package me.miki.shindo.management.mods.impl;

import lombok.Getter;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import me.miki.shindo.management.settings.config.Property;
import me.miki.shindo.management.settings.config.PropertyEnum;
import me.miki.shindo.management.settings.config.PropertyType;
import me.miki.shindo.management.settings.impl.NumberSetting;
import me.miki.shindo.management.settings.metadata.SettingRegistry;
public class WaveyCapesMod extends Mod {

    @Getter
    private static WaveyCapesMod instance;

    @Property(type = PropertyType.NUMBER, translate = TranslateText.GRAVITY, min = 2, max = 30, current = 15)
    private double gravitySetting = 15;

    @Getter
    @Property(type = PropertyType.COMBO, translate = TranslateText.MOVEMENT)
    private Movement movement = Movement.BASIC;

    @Getter
    @Property(type = PropertyType.COMBO, translate = TranslateText.STYLE)
    private CapeStyle style = CapeStyle.SMOOTH;

    @Getter
    @Property(type = PropertyType.COMBO, translate = TranslateText.MODE)
    private CapeMode mode = CapeMode.WAVES;

    @Property(type = PropertyType.NUMBER, translate = TranslateText.HEIGHT_MULTIPLIER, min = 2, max = 10, current = 6, step = 1)
    private int heightMultiplierSetting = 6;

    public WaveyCapesMod() {
        super(TranslateText.WAVEY_CAPES, TranslateText.WAVEY_CAPES_DESCRIPTION, ModCategory.RENDER, "clothcapesoftfabriccloak");

        instance = this;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        if (MoBendsMod.getInstance().isToggled()) {
            MoBendsMod.getInstance().setToggled(false);
        }
    }

    public NumberSetting getGravitySetting() {
        return SettingRegistry.getNumberSetting(this, "gravitySetting");
    }

    public NumberSetting getHeightMultiplierSetting() {
        return SettingRegistry.getNumberSetting(this, "heightMultiplierSetting");
    }

    public enum Movement implements PropertyEnum {
        VANILLA(TranslateText.VANILLA),
        BASIC(TranslateText.BASIC);

        private final TranslateText translate;

        Movement(TranslateText translate) {
            this.translate = translate;
        }

        @Override
        public TranslateText getTranslate() {
            return translate;
        }
    }

    public enum CapeStyle implements PropertyEnum {
        BLOCKY(TranslateText.BLOCKY),
        SMOOTH(TranslateText.SMOOTH);

        private final TranslateText translate;

        CapeStyle(TranslateText translate) {
            this.translate = translate;
        }

        @Override
        public TranslateText getTranslate() {
            return translate;
        }
    }

    public enum CapeMode implements PropertyEnum {
        NONE(TranslateText.NONE),
        WAVES(TranslateText.WAVES);

        private final TranslateText translate;

        CapeMode(TranslateText translate) {
            this.translate = translate;
        }

        @Override
        public TranslateText getTranslate() {
            return translate;
        }
    }
}

package me.miki.shindo.management.mods.impl;

import lombok.Getter;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import me.miki.shindo.management.settings.config.Property;
import me.miki.shindo.management.settings.config.PropertyEnum;
import me.miki.shindo.management.settings.config.PropertyType;

public class ClientSpooferMod extends Mod {

    @Getter
    private static ClientSpooferMod instance;

    @Getter
    @Property(type = PropertyType.COMBO, translate = TranslateText.TYPE)
    private SpoofType spoofType = SpoofType.VANILLA;

    public ClientSpooferMod() {
        super(TranslateText.CLIENT_SPOOFER, TranslateText.CLIENT_SPOOFER_DESCRIPTION, ModCategory.OTHER);

        instance = this;
    }

    public enum SpoofType implements PropertyEnum {
        VANILLA(TranslateText.VANILLA),
        FORGE(TranslateText.FORGE);

        private final TranslateText translate;

        SpoofType(TranslateText translate) {
            this.translate = translate;
        }

        @Override
        public TranslateText getTranslate() {
            return translate;
        }
    }
}

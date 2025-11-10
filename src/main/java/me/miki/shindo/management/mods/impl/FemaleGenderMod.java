package me.miki.shindo.management.mods.impl;

import eu.shoroa.contrib.cosmetic.CosmeticManager;
import eu.shoroa.contrib.impl.BoobsCosmetic;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;

import me.miki.shindo.management.settings.config.Property;
import me.miki.shindo.management.settings.config.PropertyType;
public class FemaleGenderMod extends Mod {

    private static FemaleGenderMod instance;
    @Property(type = PropertyType.NUMBER, translate = TranslateText.DAMPING, min = 0.1, max = 2.0, current = 0.95)
    private double damping = 0.95;
    @Property(type = PropertyType.NUMBER, translate = TranslateText.SPRING_STRENGTH, min = 0.1, max = 4.0, current = 0.8)
    private double springStrength = 0.8;
    @Property(type = PropertyType.NUMBER, translate = TranslateText.JIGGLE_MULTIPLY, min = 1, max = 20, current = 15)
    private double jiggleMultiplier = 15;
    @Property(type = PropertyType.NUMBER, translate = TranslateText.ACCELERATION_MULTIPLIER, min = 0.1, max = 6.0, current = 1.5)
    private double accelerationMultiplier = 1.5;

    public FemaleGenderMod() {
        super(TranslateText.FEMALE_GENDER, TranslateText.FEMALE_GENDER_DESCRIPTION, ModCategory.PLAYER, "boobs");
        instance = this;
    }

    public static FemaleGenderMod getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        CosmeticManager.getInstance().getCosmeticByClass(BoobsCosmetic.class).setEnabled(true);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        CosmeticManager.getInstance().getCosmeticByClass(BoobsCosmetic.class).setEnabled(false);
    }

    public float getDamping() {
        return (float) damping;
    }

    public float getSpringStrength() {
        return (float) springStrength;
    }

    public float getJiggleMultiplier() {
        return (float) jiggleMultiplier;
    }

    public float getAccelerationMultiplier() {
        return (float) accelerationMultiplier;
    }
}

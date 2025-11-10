package me.miki.shindo.management.addons.patcher;

import lombok.Getter;
import me.miki.shindo.management.addons.Addon;
import me.miki.shindo.management.addons.AddonType;
import me.miki.shindo.management.settings.config.Property;
import me.miki.shindo.management.settings.config.PropertyType;
import me.miki.shindo.management.settings.impl.BooleanSetting;
import me.miki.shindo.management.settings.impl.NumberSetting;
import me.miki.shindo.management.settings.metadata.SettingRegistry;

public class PatcherAddon extends Addon {

    @Getter
    private static PatcherAddon instance;

    @Property(type = PropertyType.BOOLEAN, name = "Parallax Fix", category = "Bug Fixes")
    private boolean parallaxFixSetting;

    @Property(type = PropertyType.BOOLEAN, name = "Fixed Alex Arms", category = "Bug Fixes", current = 1)
    private boolean fixedAlexArmsSetting = true;

    @Property(type = PropertyType.BOOLEAN, name = "Nausea Effect", category = "Miscellaneous")
    private boolean nauseaEffectSetting;

    @Property(type = PropertyType.BOOLEAN, name = "Remove Ground Foliage", category = "Miscellaneous")
    private boolean removeGroundFoliageSetting;

    @Property(type = PropertyType.BOOLEAN, name = "Numerical Enchantments", category = "Miscellaneous")
    private boolean numericalEnchantsSetting;

    @Property(type = PropertyType.BOOLEAN, name = "Translate Unknown Roman Numerals", category = "Miscellaneous")
    private boolean betterRomanNumeralsSetting;

    @Property(type = PropertyType.BOOLEAN, name = "Clean View", category = "Miscellaneous")
    private boolean cleanViewSetting;

    @Property(type = PropertyType.BOOLEAN, name = "Disable Breaking Particles", category = "Miscellaneous")
    private boolean disableBlockBreakParticlesSetting;

    @Property(type = PropertyType.BOOLEAN, name = "1.12 Farm Selection Boxes", category = "Miscellaneous")
    private boolean futureHitboxesSetting;

    @Property(type = PropertyType.BOOLEAN, name = "Better Camera", category = "Miscellaneous")
    private boolean betterCameraSetting;

    @Property(type = PropertyType.BOOLEAN, name = "Better F1", category = "Miscellaneous")
    private boolean betterHideGuiSetting;

    @Property(type = PropertyType.BOOLEAN, name = "Downscale Pack Images", category = "Performance")
    private boolean downscalePackImagesSetting;

    @Property(type = PropertyType.BOOLEAN, name = "Static Particle Color", category = "Performance")
    private boolean staticParticleColorSetting;

    @Property(type = PropertyType.NUMBER, name = "Max Particle Limit", category = "Performance", min = 1, max = 10000, step = 1, current = 4000)
    private double maxParticleLimitSetting = 4000D;

    @Property(type = PropertyType.BOOLEAN, name = "Disable Enchantment Glint", category = "Performance")
    private boolean disableEnchantGlintSetting;

    @Property(type = PropertyType.NUMBER, name = "Container Opacity", category = "Screens", min = 0, max = 1, step = 0.05, current = 1)
    private double containerOacitySetting = 1D;

    public PatcherAddon() {
        super("Patcher", "Minecraft QoL Mod", "null", AddonType.OTHER);
        instance = this;
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
    }

    public BooleanSetting getParallaxFixSetting() {
        return SettingRegistry.getBooleanSetting(this, "parallaxFixSetting");
    }

    public BooleanSetting getFixedAlexArmsSetting() {
        return SettingRegistry.getBooleanSetting(this, "fixedAlexArmsSetting");
    }

    public BooleanSetting getNauseaEffectSetting() {
        return SettingRegistry.getBooleanSetting(this, "nauseaEffectSetting");
    }

    public BooleanSetting getRemoveGroundFoliageSetting() {
        return SettingRegistry.getBooleanSetting(this, "removeGroundFoliageSetting");
    }

    public BooleanSetting getNumericalEnchantsSetting() {
        return SettingRegistry.getBooleanSetting(this, "numericalEnchantsSetting");
    }

    public BooleanSetting getBetterRomanNumeralsSetting() {
        return SettingRegistry.getBooleanSetting(this, "betterRomanNumeralsSetting");
    }

    public BooleanSetting getCleanViewSetting() {
        return SettingRegistry.getBooleanSetting(this, "cleanViewSetting");
    }

    public BooleanSetting getDisableBlockBreakParticlesSetting() {
        return SettingRegistry.getBooleanSetting(this, "disableBlockBreakParticlesSetting");
    }

    public BooleanSetting getFutureHitboxesSetting() {
        return SettingRegistry.getBooleanSetting(this, "futureHitboxesSetting");
    }

    public BooleanSetting getBetterCameraSetting() {
        return SettingRegistry.getBooleanSetting(this, "betterCameraSetting");
    }

    public BooleanSetting getBetterHideGuiSetting() {
        return SettingRegistry.getBooleanSetting(this, "betterHideGuiSetting");
    }

    public BooleanSetting getDownscalePackImagesSetting() {
        return SettingRegistry.getBooleanSetting(this, "downscalePackImagesSetting");
    }

    public BooleanSetting getStaticParticleColorSetting() {
        return SettingRegistry.getBooleanSetting(this, "staticParticleColorSetting");
    }

    public NumberSetting getMaxParticleLimitSetting() {
        return SettingRegistry.getNumberSetting(this, "maxParticleLimitSetting");
    }

    public BooleanSetting getDisableEnchantGlintSetting() {
        return SettingRegistry.getBooleanSetting(this, "disableEnchantGlintSetting");
    }

    public NumberSetting getContainerOacitySetting() {
        return SettingRegistry.getNumberSetting(this, "containerOacitySetting");
    }
}

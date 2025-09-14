package me.miki.shindo.management.addons.patcher;

import lombok.Getter;
import me.miki.shindo.management.addons.Addon;
import me.miki.shindo.management.addons.AddonType;
import me.miki.shindo.management.addons.settings.impl.BooleanSetting;
import me.miki.shindo.management.addons.settings.impl.CategorySetting;
import me.miki.shindo.management.addons.settings.impl.NumberSetting;

public class PatcherAddon extends Addon {


    @Getter
    private static PatcherAddon instance;

    // BUGFIXES
    @Getter
    private final CategorySetting bugFix = new CategorySetting("BugFix", this);
    @Getter
    private final BooleanSetting parallaxFixSetting = new BooleanSetting("Parallax Fix", this, false);
    @Getter
    private final BooleanSetting fixedAlexArmsSetting = new BooleanSetting("Fixed Alex Arms", this, true);


    // MISC
    @Getter
    private final CategorySetting miscellaneous = new CategorySetting("Miscellaneous", this);
    @Getter
    private final BooleanSetting nauseaEffectSetting = new BooleanSetting("Nausea Effect", this, false);
    @Getter
    private final BooleanSetting removeGroundFoliageSetting = new BooleanSetting("Remove Ground Foliage", this, false);
    @Getter
    private final BooleanSetting numericalEnchantsSetting = new BooleanSetting("Numerical Enchantments", this, false);
    @Getter
    private final BooleanSetting betterRomanNumeralsSetting = new BooleanSetting("Translate Unknown Roman Numerals", this, false);
    @Getter
    private final BooleanSetting cleanViewSetting = new BooleanSetting("Clean View", this, false);
    @Getter
    private final BooleanSetting disableBlockBreakParticlesSetting = new BooleanSetting("Disable Breaking Particles", this, false);
    @Getter
    private final BooleanSetting futureHitboxesSetting = new BooleanSetting("1.12 Farm Selection Boxes", this, false);
    @Getter
    private final BooleanSetting betterCameraSetting = new BooleanSetting("Better Camera", this, false);
    @Getter
    private final BooleanSetting betterHideGuiSetting = new BooleanSetting("Better F1", this, false);


    // PERFORMANCE
    @Getter
    private final CategorySetting performance = new CategorySetting("Performance", this);
    @Getter
    private final BooleanSetting downscalePackImagesSetting = new BooleanSetting("Downscale Pack Images", this, false);
    @Getter
    private final BooleanSetting staticParticleColorSetting = new BooleanSetting("Static Particle Color", this, false);
    @Getter
    private final NumberSetting maxParticleLimitSetting = new NumberSetting("Max Particle Limit", this, 4000D, 1D, 10000D, true);
    @Getter
    private final BooleanSetting disableEnchantGlintSetting = new BooleanSetting("Disable Enchantment Glint", this, false);

    // SCREENS
    @Getter
    private final CategorySetting features = new CategorySetting("Screens", this);
    @Getter
    private final NumberSetting containerOacitySetting = new NumberSetting("Container Opacity", this, 1D, 0D, 1D, false);


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


}

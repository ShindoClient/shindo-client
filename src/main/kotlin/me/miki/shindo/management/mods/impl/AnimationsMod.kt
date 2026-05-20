package me.miki.shindo.management.mods.impl

import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.nanovg.font.Shinconic
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyType
import me.miki.shindo.management.settings.impl.BooleanSetting
import me.miki.shindo.management.settings.impl.NumberSetting
import me.miki.shindo.management.settings.metadata.SettingRegistry.getBooleanSetting
import me.miki.shindo.management.settings.metadata.SettingRegistry.getNumberSetting

class AnimationsMod :
    Mod(
        TranslateText.OLD_ANIMATION,
        TranslateText.OLD_ANIMATION_DESCRIPTION,
        ModCategory.RENDER,
        Shinconic.MOD_ANIMATIONS,
        "oldoam1.7smoothsneak",
    ) {
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.BLOCK_HIT)
    @JvmField
    var blockHitSetting = false

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.PUSHING)
    @JvmField
    var pushingSetting = false

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.PUSHING_PARTICLES)
    @JvmField
    var pushingParticleSetting = false

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.SNEAK)
    @JvmField
    var sneakSetting = false

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.SNEAKSMOOTH)
    @JvmField
    var smoothSneakSetting = false

    @Property(type = PropertyType.NUMBER, translate = TranslateText.SMOOTH_SPEED, min = 0.5, max = 20.0, step = 0.5)
    private val smoothSneakSpeedSetting = 6.0

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.HEALTH)
    @JvmField
    var healthSetting = false

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.ARMOR_DAMAGE)
    @JvmField
    var armorDamageSetting = false

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.ITEM_SWITCH)
    @JvmField
    var itemSwitchSetting = false

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.ROD)
    @JvmField
    var rodSetting = false

    init {
        instance = this
    }

    fun getSmoothSneakSpeedSetting(): Float = smoothSneakSpeedSetting.toFloat()

    val smoothSneakSpeedSettingProperty: NumberSetting?
        get() = getNumberSetting(this, "smoothSneakSpeedSetting")

    companion object {
        @JvmField
        var instance: AnimationsMod? = null
    }

    fun getBlockHitSetting(): BooleanSetting? = getBooleanSetting(this, "blockHitSetting")

    fun getPushingSetting(): BooleanSetting? = getBooleanSetting(this, "pushingSetting")

    fun getPushingParticleSetting(): BooleanSetting? = getBooleanSetting(this, "pushingParticleSetting")

    fun getSneakSetting(): BooleanSetting? = getBooleanSetting(this, "sneakSetting")

    fun getSmoothSneakSetting(): BooleanSetting? = getBooleanSetting(this, "smoothSneakSetting")

    fun getHealthSetting(): BooleanSetting? = getBooleanSetting(this, "healthSetting")

    fun getArmorDamageSetting(): BooleanSetting? = getBooleanSetting(this, "armorDamageSetting")

    fun getItemSwitchSetting(): BooleanSetting? = getBooleanSetting(this, "itemSwitchSetting")

    fun getRodSetting(): BooleanSetting? = getBooleanSetting(this, "rodSetting")
}

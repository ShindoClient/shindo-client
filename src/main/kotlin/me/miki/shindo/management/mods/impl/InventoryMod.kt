package me.miki.shindo.management.mods.impl

import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyEnum
import me.miki.shindo.management.settings.config.PropertyType
import me.miki.shindo.management.settings.impl.BooleanSetting
import me.miki.shindo.management.settings.metadata.SettingRegistry.getBooleanSetting

class InventoryMod :
    Mod(TranslateText.INVENTORY, TranslateText.INVENTORY_DESCRIPTION, ModCategory.OTHER, LegacyIcon.MOD_INVENTORY) {
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.ANIMATION)
    @JvmField
    var animationSetting = false

    @Property(type = PropertyType.COMBO, translate = TranslateText.ANIMATION_TYPE)
    val animationType: AnimationType = AnimationType.NORMAL

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.BACKGROUND)
    @JvmField
    var backgroundSetting = false

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.PREVENT_POTION_SHIFT)
    @JvmField
    var preventPotionShiftSetting = false

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.PARTICLE)
    @JvmField
    var particleSetting = false

    init {
        instance = this
    }

    enum class AnimationType(private val translate: TranslateText) : PropertyEnum {
        NORMAL(TranslateText.NORMAL),
        BACKIN(TranslateText.BACKIN);

        override fun getTranslate(): TranslateText {
            return translate
        }
    }

    companion object {
        @JvmField
        var instance: InventoryMod? = null
    }

    fun getAnimationSetting(): BooleanSetting? = getBooleanSetting(this, "animationSetting")

    fun getBackgroundSetting(): BooleanSetting? = getBooleanSetting(this, "backgroundSetting")

    fun getPreventPotionShiftSetting(): BooleanSetting? =
        getBooleanSetting(this, "preventPotionShiftSetting")

    fun getParticleSetting(): BooleanSetting? = getBooleanSetting(this, "particleSetting")
}





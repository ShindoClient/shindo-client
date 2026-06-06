package com.shindoclient.shindo.management.mods.impl

import com.shindoclient.shindo.management.language.TranslateText
import com.shindoclient.shindo.management.mods.Mod
import com.shindoclient.shindo.management.mods.ModCategory
import com.shindoclient.shindo.management.nanovg.font.Shinconic
import com.shindoclient.shindo.management.settings.config.Property
import com.shindoclient.shindo.management.settings.config.PropertyEnum
import com.shindoclient.shindo.management.settings.config.PropertyType
import com.shindoclient.shindo.management.settings.impl.BooleanSetting
import com.shindoclient.shindo.management.settings.metadata.SettingRegistry.getBooleanSetting

class InventoryMod : Mod(TranslateText.INVENTORY, TranslateText.INVENTORY_DESCRIPTION, ModCategory.OTHER, Shinconic.MOD_INVENTORY) {
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.ANIMATION)
    @JvmField
    var animationSetting = false

    @Property(type = PropertyType.COMBO, translate = TranslateText.ANIMATION_TYPE)
    @JvmField
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

    enum class AnimationType(
        private val translate: TranslateText,
    ) : PropertyEnum {
        NORMAL(TranslateText.NORMAL),
        BACKIN(TranslateText.BACKIN),
        ;

        override fun getTranslate(): TranslateText = translate
    }

    companion object {
        @JvmField
        var instance: InventoryMod? = null
    }

    fun getAnimationSetting(): BooleanSetting? = getBooleanSetting(this, "animationSetting")

    fun getBackgroundSetting(): BooleanSetting? = getBooleanSetting(this, "backgroundSetting")

    fun getPreventPotionShiftSetting(): BooleanSetting? = getBooleanSetting(this, "preventPotionShiftSetting")

    fun getParticleSetting(): BooleanSetting? = getBooleanSetting(this, "particleSetting")
}

package me.miki.shindo.management.mods.impl

import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventRender2D
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.SimpleHUDMod
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyType
import me.miki.shindo.utils.PlayerUtils.getPotionsFromInventory
import net.minecraft.potion.Potion

class PotionCounterMod : SimpleHUDMod(
    TranslateText.POTION_COUNTER,
    TranslateText.POTION_COUNTER_DESCRIPTION,
    LegacyIcon.MOD_POTION_COUNTER
) {
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.ICON)
    private val showIcon = true

    @EventTarget
    fun onRender2D(event: EventRender2D?) {
        this.draw()
    }

    public override fun getText(): String? {
        val amount = getPotionsFromInventory(Potion.heal)

        return amount.toString() + " " + (if (amount <= 1) "pot" else "pots")
    }

    public override fun getIcon(): String? {
        return if (showIcon) LegacyIcon.ARCHIVE else null
    }
}



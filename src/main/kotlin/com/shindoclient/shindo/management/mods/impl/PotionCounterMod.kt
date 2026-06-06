package com.shindoclient.shindo.management.mods.impl

import com.shindoclient.shindo.management.event.EventTarget
import com.shindoclient.shindo.management.event.impl.EventNVG
import com.shindoclient.shindo.management.language.TranslateText
import com.shindoclient.shindo.management.mods.SimpleHUDMod
import com.shindoclient.shindo.management.nanovg.font.Lucide
import com.shindoclient.shindo.management.nanovg.font.Shinconic
import com.shindoclient.shindo.management.settings.config.Property
import com.shindoclient.shindo.management.settings.config.PropertyType
import com.shindoclient.shindo.utils.PlayerUtils.getPotionsFromInventory
import net.minecraft.potion.Potion

class PotionCounterMod :
    SimpleHUDMod(
        TranslateText.POTION_COUNTER,
        TranslateText.POTION_COUNTER_DESCRIPTION,
        Shinconic.MOD_POTION_COUNTER,
    ) {
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.ICON)
    private val showIcon = true

    @EventTarget
    fun onRender2D(event: EventNVG) {
        this.draw()
    }

    override fun getText(): String {
        val amount = getPotionsFromInventory(Potion.heal)

        return amount.toString() + " " + (if (amount <= 1) "pot" else "pots")
    }

    override fun getIcon(): String? = if (showIcon) Lucide.ARCHIVE else null
}

package me.miki.shindo.management.mods.impl

import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventAttackEntity
import me.miki.shindo.management.event.impl.EventDamageEntity
import me.miki.shindo.management.event.impl.EventNVG
import me.miki.shindo.management.event.impl.EventTick
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.SimpleHUDMod
import me.miki.shindo.management.nanovg.font.Lucide
import me.miki.shindo.management.nanovg.font.Shinconic
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyType

class ComboCounterMod : SimpleHUDMod(TranslateText.COMBO_COUNTER, TranslateText.COMBO_COUNTER_DESCRIPTION, Shinconic.MOD_COMBO_COUNTER) {
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.ICON)
    private val iconSetting = true

    private var hitTime: Long = -1
    private var combo = 0
    private var possibleTarget = 0

    @EventTarget
    fun onRender2D(event: EventNVG?) {
        this.draw()
    }

    @EventTarget
    fun onTick(event: EventTick?) {
        if ((System.currentTimeMillis() - hitTime) > 2000) {
            combo = 0
        }
    }

    @EventTarget
    fun onAttackEntity(event: EventAttackEntity) {
        possibleTarget = event.getEntity().entityId
    }

    @EventTarget
    fun onDamageEntity(event: EventDamageEntity) {
        if (event.getEntity().entityId == possibleTarget) {
            combo++
            possibleTarget = -1
            hitTime = System.currentTimeMillis()
        } else if (event.getEntity() === mc.thePlayer) {
            combo = 0
        }
    }

    override fun getText(): String =
        if (combo == 0) {
            "No Combo"
        } else {
            "$combo Combo"
        }

    override fun getIcon(): String? = if (iconSetting) Lucide.BAR_CHART else null
}

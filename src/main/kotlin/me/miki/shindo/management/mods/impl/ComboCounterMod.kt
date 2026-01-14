package me.miki.shindo.management.mods.impl

import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventAttackEntity
import me.miki.shindo.management.event.impl.EventDamageEntity
import me.miki.shindo.management.event.impl.EventRender2D
import me.miki.shindo.management.event.impl.EventTick
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.SimpleHUDMod
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyType

class ComboCounterMod :
    SimpleHUDMod(TranslateText.COMBO_COUNTER, TranslateText.COMBO_COUNTER_DESCRIPTION, LegacyIcon.MOD_COMBO_COUNTER) {
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.ICON)
    private val iconSetting = true

    private var hitTime: Long = -1
    private var combo = 0
    private var possibleTarget = 0

    @EventTarget
    fun onRender2D(event: EventRender2D?) {
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
        possibleTarget = event.entity.entityId
    }

    @EventTarget
    fun onDamageEntity(event: EventDamageEntity) {
        if (event.entity.entityId == possibleTarget) {
            combo++
            possibleTarget = -1
            hitTime = System.currentTimeMillis()
        } else if (event.entity === mc.thePlayer) {
            combo = 0
        }
    }

    override fun getText(): String {
        if (combo == 0) {
            return "No Combo"
        } else {
            return combo.toString() + " Combo"
        }
    }

    override fun getIcon(): String? {
        return if (iconSetting) LegacyIcon.BAR_CHERT else null
    }
}



package me.miki.shindo.management.mods.impl

import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventDamageEntity
import me.miki.shindo.management.event.impl.EventNVG
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.SimpleHUDMod
import me.miki.shindo.management.nanovg.font.Lucide
import me.miki.shindo.management.nanovg.font.Shinconic
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyType
import net.minecraft.util.MovingObjectPosition.MovingObjectType
import java.text.DecimalFormat

class ReachDisplayMod : SimpleHUDMod(TranslateText.REACH_DISPLAY, TranslateText.REACH_DISPLAY_DESCRIPTION, Shinconic.MOD_REACH_DISPLAY) {
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.ICON)
    private val iconEnabled = true

    private val df = DecimalFormat("0.##")

    private var distance = 0.0
    private var hitTime: Long = -1

    @EventTarget
    fun onRender2D(event: EventNVG) {
        this.draw()
    }

    @EventTarget
    fun onDamageEntity(event: EventDamageEntity?) {
        if (mc.objectMouseOver != null &&
            mc.objectMouseOver.hitVec != null &&
            mc.objectMouseOver.typeOfHit == MovingObjectType.ENTITY
        ) {
            distance = mc.objectMouseOver.hitVec.distanceTo(mc.thePlayer.getPositionEyes(1.0f))
            hitTime = System.currentTimeMillis()
        }
    }

    override fun getText(): String {
        if ((System.currentTimeMillis() - hitTime) > 5000) {
            distance = 0.0
        }

        return if (distance == 0.0) {
            "Hasn't attacked"
        } else {
            df.format(distance) + " blocks"
        }
    }

    override fun getIcon(): String? = if (iconEnabled) Lucide.ACTIVITY else null
}

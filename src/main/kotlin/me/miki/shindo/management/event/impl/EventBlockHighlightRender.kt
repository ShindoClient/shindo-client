package me.miki.shindo.management.event.impl

import me.miki.shindo.management.event.Event
import net.minecraft.util.MovingObjectPosition

class EventBlockHighlightRender(
    val objectMouseOver: MovingObjectPosition,
    @JvmField val partialTicks: Float,
) : Event() {
    fun getPartialTicks(): Float = partialTicks
}

package me.miki.shindo.management.event.impl;

import lombok.Getter;
import me.miki.shindo.management.event.Event;
import net.minecraft.util.MovingObjectPosition;

@Getter
public class EventBlockHighlightRender extends Event {

    private final MovingObjectPosition objectMouseOver;
    private final float partialTicks;

    public EventBlockHighlightRender(MovingObjectPosition objectMouseOver, float partialTicks) {
        this.objectMouseOver = objectMouseOver;
        this.partialTicks = partialTicks;
    }

}

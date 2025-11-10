package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.settings.config.Property;
import me.miki.shindo.management.settings.config.PropertyType;
import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventDamageEntity;
import me.miki.shindo.management.event.impl.EventRender2D;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.SimpleHUDMod;
import me.miki.shindo.management.nanovg.font.LegacyIcon;
import net.minecraft.util.MovingObjectPosition;

import java.text.DecimalFormat;

public class ReachDisplayMod extends SimpleHUDMod {

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.ICON)
    private boolean iconEnabled = true;

    private final DecimalFormat df = new DecimalFormat("0.##");

    private double distance = 0;
    private long hitTime = -1;

    public ReachDisplayMod() {
        super(TranslateText.REACH_DISPLAY, TranslateText.REACH_DISPLAY_DESCRIPTION);
    }

    @EventTarget
    public void onRender2D(EventRender2D event) {
        this.draw();
    }

    @EventTarget
    public void onDamageEntity(EventDamageEntity event) {
        if (mc.objectMouseOver != null && mc.objectMouseOver.hitVec != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
            distance = mc.objectMouseOver.hitVec.distanceTo(mc.thePlayer.getPositionEyes(1.0F));
            hitTime = System.currentTimeMillis();
        }
    }

    @Override
    public String getText() {

        if ((System.currentTimeMillis() - hitTime) > 5000) {
            distance = 0;
        }

        if (distance == 0) {
            return "Hasn't attacked";
        } else {
            return df.format(distance) + " blocks";
        }
    }

    @Override
    public String getIcon() {
        return iconEnabled ? LegacyIcon.ACTIVITY : null;
    }
}

package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventAttackEntity;
import me.miki.shindo.management.event.impl.EventDamageEntity;
import me.miki.shindo.management.event.impl.EventRender2D;
import me.miki.shindo.management.event.impl.EventTick;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.settings.config.Property;
import me.miki.shindo.management.settings.config.PropertyType;
import me.miki.shindo.management.mods.SimpleHUDMod;
import me.miki.shindo.management.nanovg.font.LegacyIcon;

public class ComboCounterMod extends SimpleHUDMod {

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.ICON, category = "Display")
    private boolean iconSetting = true;

    private long hitTime = -1;
    private int combo, possibleTarget;

    public ComboCounterMod() {
        super(TranslateText.COMBO_COUNTER, TranslateText.COMBO_COUNTER_DESCRIPTION);
    }

    @EventTarget
    public void onRender2D(EventRender2D event) {
        this.draw();
    }

    @EventTarget
    public void onTick(EventTick event) {
        if ((System.currentTimeMillis() - hitTime) > 2000) {
            combo = 0;
        }
    }

    @EventTarget
    public void onAttackEntity(EventAttackEntity event) {
        possibleTarget = event.getEntity().getEntityId();
    }

    @EventTarget
    public void onDamageEntity(EventDamageEntity event) {
        if (event.getEntity().getEntityId() == possibleTarget) {
            combo++;
            possibleTarget = -1;
            hitTime = System.currentTimeMillis();
        } else if (event.getEntity() == mc.thePlayer) {
            combo = 0;
        }
    }

    @Override
    public String getText() {
        if (combo == 0) {
            return "No Combo";
        } else {
            return combo + " Combo";
        }
    }

    @Override
    public String getIcon() {
        return iconSetting ? LegacyIcon.BAR_CHERT : null;
    }
}

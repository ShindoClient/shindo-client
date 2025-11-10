package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventRender2D;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.settings.config.Property;
import me.miki.shindo.management.settings.config.PropertyType;
import me.miki.shindo.management.mods.SimpleHUDMod;
import me.miki.shindo.management.nanovg.font.LegacyIcon;
import me.miki.shindo.utils.PlayerUtils;
import net.minecraft.potion.Potion;

public class PotionCounterMod extends SimpleHUDMod {

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.ICON)
    private boolean showIcon = true;

    public PotionCounterMod() {
        super(TranslateText.POTION_COUNTER, TranslateText.POTION_COUNTER_DESCRIPTION);
    }

    @EventTarget
    public void onRender2D(EventRender2D event) {
        this.draw();
    }

    @Override
    public String getText() {

        int amount = PlayerUtils.getPotionsFromInventory(Potion.heal);

        return amount + " " + (amount <= 1 ? "pot" : "pots");
    }

    @Override
    public String getIcon() {
        return showIcon ? LegacyIcon.ARCHIVE : null;
    }
}

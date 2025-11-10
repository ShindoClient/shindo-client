package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventRenderItemInFirstPerson;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.settings.config.Property;
import me.miki.shindo.management.settings.config.PropertyType;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import net.minecraft.client.renderer.GlStateManager;

public class CustomHeldItemsMod extends Mod {

    @Property(type = PropertyType.NUMBER, translate = TranslateText.X, category = "Offset", min = -1, max = 1, current = 0.75)
    private double xSetting = 0.75;
    @Property(type = PropertyType.NUMBER, translate = TranslateText.Y, category = "Offset", min = -1, max = 1, current = -0.15)
    private double ySetting = -0.15;
    @Property(type = PropertyType.NUMBER, translate = TranslateText.Z, category = "Offset", min = -1, max = 1, current = -1)
    private double zSetting = -1;

    @Property(type = PropertyType.NUMBER, translate = TranslateText.X_SCALE, category = "Scale", min = 0, max = 1, current = 1)
    private double xScaleSetting = 1;
    @Property(type = PropertyType.NUMBER, translate = TranslateText.Y_SCALE, category = "Scale", min = 0, max = 1, current = 1)
    private double yScaleSetting = 1;
    @Property(type = PropertyType.NUMBER, translate = TranslateText.Z_SCALE, category = "Scale", min = 0, max = 1, current = 1)
    private double zScaleSetting = 1;

    public CustomHeldItemsMod() {
        super(TranslateText.CUSTOM_HELD_ITEMS, TranslateText.CUSTOM_HELD_ITEMS_DESCRIPTION, ModCategory.RENDER);
    }

    @EventTarget
    public void onRenderItemInFirstPerson(EventRenderItemInFirstPerson event) {
        GlStateManager.translate((float) xSetting, (float) ySetting, (float) zSetting);
        GlStateManager.scale((float) xScaleSetting, (float) yScaleSetting, (float) zScaleSetting);
    }
}

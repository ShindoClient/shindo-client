package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventRenderItemInFirstPerson;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import me.miki.shindo.management.mods.settings.impl.NumberSetting;
import net.minecraft.client.renderer.GlStateManager;

public class CustomHeldItemsMod extends Mod {

    private final NumberSetting xSetting = new NumberSetting(TranslateText.X, this, 0.75, -1, 1, false);
    private final NumberSetting ySetting = new NumberSetting(TranslateText.Y, this, -0.15, -1, 1, false);
    private final NumberSetting zSetting = new NumberSetting(TranslateText.Z, this, -1, -1, 1, false);

    private final NumberSetting xScaleSetting = new NumberSetting(TranslateText.X_SCALE, this, 1, 0, 1, false);
    private final NumberSetting yScaleSetting = new NumberSetting(TranslateText.Y_SCALE, this, 1, 0, 1, false);
    private final NumberSetting zScaleSetting = new NumberSetting(TranslateText.Z_SCALE, this, 1, 0, 1, false);

    public CustomHeldItemsMod() {
        super(TranslateText.CUSTOM_HELD_ITEMS, TranslateText.CUSTOM_HELD_ITEMS_DESCRIPTION, ModCategory.RENDER);
    }

    @EventTarget
    public void onRenderItemInFirstPerson(EventRenderItemInFirstPerson event) {
        GlStateManager.translate(xSetting.getValueFloat(), ySetting.getValueFloat(), zSetting.getValueFloat());
        GlStateManager.scale(xScaleSetting.getValueFloat(), yScaleSetting.getValueFloat(), zScaleSetting.getValueFloat());
    }
}

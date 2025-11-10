package me.miki.shindo.management.mods.impl;

import me.miki.shindo.Shindo;
import me.miki.shindo.management.color.AccentColor;
import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventRender3D;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import me.miki.shindo.management.settings.config.Property;
import me.miki.shindo.management.settings.config.PropertyType;
import me.miki.shindo.utils.ColorUtils;
import me.miki.shindo.utils.Render3DUtils;
import me.miki.shindo.utils.TargetUtils;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;

public class TargetIndicatorMod extends Mod {

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.CUSTOM_COLOR, category = "Display")
    private boolean customColorSetting;

    @Property(type = PropertyType.COLOR, translate = TranslateText.COLOR, category = "Display")
    private Color colorSetting = Color.RED;

    public TargetIndicatorMod() {
        super(TranslateText.TARGET_INDICATOR, TranslateText.TARGET_INDICATOR_DESCRIPTION, ModCategory.RENDER);
    }

    @EventTarget
    public void onRender3D(EventRender3D event) {

        AccentColor currentColor = Shindo.getInstance().getColorManager().getCurrentColor();

        if (TargetUtils.getTarget() != null && !TargetUtils.getTarget().equals(mc.thePlayer)) {
            Render3DUtils.drawTargetIndicator(TargetUtils.getTarget(), 0.67, customColorSetting ? ColorUtils.applyAlpha(colorSetting, 255) : currentColor.getInterpolateColor());
            GlStateManager.enableBlend();
        }
    }
}

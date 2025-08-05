package me.miki.shindo.management.mods.impl;

import me.miki.shindo.Shindo;
import me.miki.shindo.management.color.AccentColor;
import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventMotionUpdate;
import me.miki.shindo.management.event.impl.EventRender3D;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import me.miki.shindo.management.mods.settings.impl.BooleanSetting;
import me.miki.shindo.management.mods.settings.impl.ColorSetting;
import me.miki.shindo.management.mods.settings.impl.NumberSetting;
import me.miki.shindo.utils.ColorUtils;
import me.miki.shindo.utils.Render3DUtils;
import net.minecraft.util.Vec3;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BreadcrumbsMod extends Mod {

    private final List<Vec3> path = new ArrayList<>();

    private final BooleanSetting customColorSetting = new BooleanSetting(TranslateText.CUSTOM_COLOR, this, false);
    private final ColorSetting colorSetting = new ColorSetting(TranslateText.COLOR, this, Color.RED, false);

    private final BooleanSetting timeoutSetting = new BooleanSetting(TranslateText.TIMEOUT, this, true);
    private final NumberSetting timeSetting = new NumberSetting(TranslateText.TIME, this, 15, 1, 150, true);

    public BreadcrumbsMod() {
        super(TranslateText.BREADCRUMBS, TranslateText.BREADCRUMBS_DESCRIPTION, ModCategory.RENDER, "playertrails");
    }

    @EventTarget
    public void onRender3D(EventRender3D event) {

        AccentColor currentColor = Shindo.getInstance().getColorManager().getCurrentColor();

        Render3DUtils.renderBreadCrumbs(path, customColorSetting.isToggled() ? ColorUtils.applyAlpha(colorSetting.getColor(), 255) : currentColor.getInterpolateColor());
    }

    @EventTarget
    public void onMotionUpdate(EventMotionUpdate event) {

        if (mc.thePlayer.lastTickPosX != mc.thePlayer.posX || mc.thePlayer.lastTickPosY != mc.thePlayer.posY || mc.thePlayer.lastTickPosZ != mc.thePlayer.posZ) {
            path.add(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ));
        }

        if (timeoutSetting.isToggled()) {
            while (path.size() > timeSetting.getValueInt()) {
                path.remove(0);
            }
        }
    }
}

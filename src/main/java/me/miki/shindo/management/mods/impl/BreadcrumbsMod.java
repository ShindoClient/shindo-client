package me.miki.shindo.management.mods.impl;

import me.miki.shindo.Shindo;
import me.miki.shindo.management.color.AccentColor;
import me.miki.shindo.management.settings.config.Property;
import me.miki.shindo.management.settings.config.PropertyType;
import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventMotionUpdate;
import me.miki.shindo.management.event.impl.EventRender3D;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import me.miki.shindo.utils.ColorUtils;
import me.miki.shindo.utils.Render3DUtils;
import net.minecraft.util.Vec3;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BreadcrumbsMod extends Mod {

    private final List<Vec3> path = new ArrayList<>();

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.CUSTOM_COLOR)
    private boolean customColor = false;

    @Property(type = PropertyType.COLOR, translate = TranslateText.COLOR)
    private Color trailColor = Color.RED;

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.TIMEOUT)
    private boolean timeoutEnabled = true;

    @Property(type = PropertyType.NUMBER, translate = TranslateText.TIME, min = 1, max = 150, step = 1)
    private double timeoutTicks = 15;

    public BreadcrumbsMod() {
        super(TranslateText.BREADCRUMBS, TranslateText.BREADCRUMBS_DESCRIPTION, ModCategory.RENDER, "playertrails");
    }

    @EventTarget
    public void onRender3D(EventRender3D event) {

        AccentColor currentColor = Shindo.getInstance().getColorManager().getCurrentColor();

        Render3DUtils.renderBreadCrumbs(path, customColor ? ColorUtils.applyAlpha(trailColor, 255) : currentColor.getInterpolateColor());
    }

    @EventTarget
    public void onMotionUpdate(EventMotionUpdate event) {

        if (mc.thePlayer.lastTickPosX != mc.thePlayer.posX || mc.thePlayer.lastTickPosY != mc.thePlayer.posY || mc.thePlayer.lastTickPosZ != mc.thePlayer.posZ) {
            path.add(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ));
        }

        if (timeoutEnabled) {
            int limit = (int) timeoutTicks;
            while (path.size() > limit) {
                path.remove(0);
            }
        }
    }
}

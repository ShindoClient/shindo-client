package me.miki.shindo.management.mods.impl;

import me.miki.shindo.Shindo;
import me.miki.shindo.gui.GuiWaypoint;
import me.miki.shindo.injection.interfaces.IMixinRenderManager;
import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventKey;
import me.miki.shindo.management.event.impl.EventRender3D;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import me.miki.shindo.management.waypoint.Waypoint;
import me.miki.shindo.utils.ColorUtils;
import me.miki.shindo.utils.render.RenderUtils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.awt.*;

import me.miki.shindo.management.settings.config.Property;
import me.miki.shindo.management.settings.config.PropertyType;
public class WaypointMod extends Mod {

    @Property(type = PropertyType.KEYBIND, translate = TranslateText.KEYBIND, keyCode = Keyboard.KEY_B)
    private int keybindSetting = Keyboard.KEY_B;

    public WaypointMod() {
        super(TranslateText.WAYPOINT, TranslateText.WAYPOINT_DESCRIPTION, ModCategory.WORLD);
    }

    @EventTarget
    public void onRender3D(EventRender3D event) {

        for (Waypoint wy : Shindo.getInstance().getWaypointManager().getWaypoints()) {
            if (Shindo.getInstance().getWaypointManager().getWorld().equals(wy.getWorld())) {

                double distance = this.getDistance(wy, mc.getRenderViewEntity());
                double renderDistance = (mc.gameSettings.renderDistanceChunks * 16) * 0.75;

                String tagName = wy.getName() + " [" + (int) distance + "m]";

                double x = wy.getX() - ((IMixinRenderManager) mc.getRenderManager()).getRenderPosX();
                double y = 2.0 + wy.getY() - ((IMixinRenderManager) mc.getRenderManager()).getRenderPosY();
                double z = wy.getZ() - ((IMixinRenderManager) mc.getRenderManager()).getRenderPosZ();

                if (distance > renderDistance) {
                    x = x / distance * renderDistance;
                    y = y / distance * renderDistance;
                    z = z / distance * renderDistance;
                    distance = renderDistance;
                }

                float scale = (float) (0.016666668f * (1.0 + distance) * 0.15);

                GL11.glPushMatrix();
                GlStateManager.translate(x, y, z);
                GlStateManager.disableDepth();

                GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
                GlStateManager.rotate(mc.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
                GlStateManager.scale(-scale, -scale, scale);

                int width = fr.getStringWidth(tagName);
                int height = fr.FONT_HEIGHT;

                int rectWidth = width + 10;
                int rectHeight = height + 6;

                RenderUtils.drawRect(-rectWidth / 2, -rectHeight / 2, rectWidth, rectHeight, ColorUtils.getColorByInt(Integer.MIN_VALUE));
                RenderUtils.drawOutline(-rectWidth / 2, -rectHeight / 2, rectWidth, rectHeight, 2.5F, wy.getColor());

                fr.drawString(tagName, -width / 2, -height / 2 + 2, Color.WHITE.getRGB());

                GlStateManager.enableDepth();
                GL11.glPopMatrix();
            }
        }
    }

    @EventTarget
    public void onKey(EventKey event) {

        if (event.getKeyCode() == keybindSetting) {
            mc.displayGuiScreen(new GuiWaypoint());
        }
    }

    private double getDistance(Waypoint wy, Entity entity) {

        double x = wy.getX() - entity.posX;
        double y = wy.getY() - entity.posY;
        double z = wy.getZ() - entity.posZ;

        return Math.sqrt(x * x + y * y + z * z);
    }
}

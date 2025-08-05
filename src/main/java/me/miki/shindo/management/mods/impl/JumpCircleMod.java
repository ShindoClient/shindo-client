package me.miki.shindo.management.mods.impl;

import me.miki.shindo.Shindo;
import me.miki.shindo.injection.interfaces.IMixinMinecraft;
import me.miki.shindo.injection.interfaces.IMixinRenderManager;
import me.miki.shindo.management.color.AccentColor;
import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventJump;
import me.miki.shindo.management.event.impl.EventRender3D;
import me.miki.shindo.management.event.impl.EventUpdate;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class JumpCircleMod extends Mod {

    private final List<JumpCircle> circles = new ArrayList<JumpCircle>();
    private boolean jumping;

    public JumpCircleMod() {
        super(TranslateText.JUMP_CIRCLE, TranslateText.JUMP_CIRCLE_DESCRIPTION, ModCategory.RENDER);
    }

    private static double createAnimation(double value) {
        return Math.sqrt(1.0 - Math.pow(value - 1.0, 2.0));
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {

        if (jumping && mc.thePlayer.onGround) {
            jumping = false;
            circles.add(new JumpCircle(mc.thePlayer.getPositionVector()));
        }

        circles.removeIf(JumpCircle::update);
    }

    @EventTarget
    public void onJump(EventJump event) {
        jumping = true;
    }

    @EventTarget
    public void onRender3D(EventRender3D event) {

        AccentColor currentColor = Shindo.getInstance().getColorManager().getCurrentColor();

        GL11.glPushMatrix();
        GL11.glEnable(3042);
        GL11.glDisable(3008);
        GL11.glDisable(2884);
        GL11.glDisable(3553);
        GL11.glShadeModel(7425);

        for (JumpCircle circle : circles) {
            GL11.glBegin(8);
            for (int i = 0; i <= 360; i += 5) {

                float red = (float) (currentColor.getInterpolateColor().getRGB() >> 16 & 255) / 255.0F;
                float green = (float) (currentColor.getInterpolateColor().getRGB() >> 8 & 255) / 255.0F;
                float blue = (float) (currentColor.getInterpolateColor().getRGB() & 255) / 255.0F;

                Vec3 pos = circle.pos();
                double x = Math.cos(Math.toRadians(i)) * createAnimation(1.0 - circle.getAnimation(((IMixinMinecraft) mc).getTimer().renderPartialTicks)) * 0.7;
                double z = Math.sin(Math.toRadians(i)) * createAnimation(1.0 - circle.getAnimation(((IMixinMinecraft) mc).getTimer().renderPartialTicks)) * 0.7;
                GL11.glColor4d(red, green, blue, 0.6 * circle.getAnimation(((IMixinMinecraft) mc).getTimer().renderPartialTicks));
                GL11.glVertex3d(pos.xCoord + x, pos.yCoord + (double) 0.2f, pos.zCoord + z);
                GL11.glColor4d(red, green, blue, 0.2 * circle.getAnimation(((IMixinMinecraft) mc).getTimer().renderPartialTicks));
                GL11.glVertex3d(pos.xCoord + x * 1.4, pos.yCoord + (double) 0.2f, pos.zCoord + z * 1.4);
            }
            GL11.glEnd();
        }

        GL11.glEnable(3553);
        GL11.glDisable(3042);
        GL11.glEnable(3008);
        GL11.glShadeModel(7424);
        GL11.glEnable(2884);
        GL11.glPopMatrix();
        GlStateManager.resetColor();
    }

    private class JumpCircle {

        private final Minecraft mc = Minecraft.getMinecraft();

        private final Vec3 vector;
        private int tick;
        private int prevTick;

        public JumpCircle(Vec3 vector) {
            this.vector = vector;
            this.prevTick = 20;
            this.prevTick = this.tick = 20;
        }

        public double getAnimation(float pt) {
            return ((float) this.prevTick + (float) (this.tick - this.prevTick) * pt) / 20.0f;
        }

        public boolean update() {
            this.prevTick = this.tick;
            return this.tick-- <= 0;
        }

        public Vec3 pos() {
            return new Vec3(this.vector.xCoord - ((IMixinRenderManager) mc.getRenderManager()).getRenderPosX(), this.vector.yCoord - ((IMixinRenderManager) mc.getRenderManager()).getRenderPosY(), this.vector.zCoord - ((IMixinRenderManager) mc.getRenderManager()).getRenderPosZ());
        }
    }
}
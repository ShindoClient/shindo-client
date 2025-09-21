package me.miki.shindo.injection.mixin.mixins.client.renderer;

import me.miki.shindo.api.roles.Role;
import me.miki.shindo.api.roles.RoleManager;
import me.miki.shindo.api.ws.presence.PresenceTracker;
import me.miki.shindo.management.mods.impl.FreelookMod;
import me.miki.shindo.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.UUID;

@Mixin(Render.class)
public abstract class MixinRender<T extends Entity> {

    @Final
    @Shadow
    protected RenderManager renderManager;

    @Shadow
    public abstract FontRenderer getFontRendererFromRenderManager();

    /**
     * @author MikiDevAHM
     * @reason Client Logo Rendering
     */
    @Overwrite
    protected void renderLivingLabel(T entityIn, String str, double x, double y, double z, int maxDistance) {

        double d0 = entityIn.getDistanceSqToEntity(this.renderManager.livingPlayer);

        if (d0 <= (double) (maxDistance * maxDistance)) {
            float viewYaw = this.renderManager.playerViewY;
            float viewPitch = this.renderManager.playerViewX;

            FreelookMod freelook = FreelookMod.getInstance();
            if (freelook.isToggled() && freelook.isActive()) {
                viewYaw = freelook.getCameraYaw();
                viewPitch = freelook.getCameraPitch();

                if (Minecraft.getMinecraft().gameSettings.thirdPersonView == 2) viewYaw += 180.0F;
            }

            FontRenderer fontrenderer = this.getFontRendererFromRenderManager();
            float f = 1.6F;
            float f1 = 0.016666668F * f;
            GlStateManager.pushMatrix();
            GlStateManager.translate((float) x + 0.0F, (float) y + entityIn.height + 0.5F, (float) z);
            GL11.glNormal3f(0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(-viewYaw, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(viewPitch, 1.0F, 0.0F, 0.0F);
            GlStateManager.scale(-f1, -f1, f1);
            GlStateManager.disableLighting();
            GlStateManager.depthMask(false);
            GlStateManager.enableBlend();

            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldrenderer = tessellator.getWorldRenderer();
            int i = 0;

            int j = fontrenderer.getStringWidth(str) / 2;
            if (str.equals("deadmau5")) {
                i = -10;
            }

            if (entityIn instanceof AbstractClientPlayer) {
                AbstractClientPlayer player = (AbstractClientPlayer) entityIn;
                UUID uuid = player.getGameProfile().getId();

                if (PresenceTracker.isOnline(uuid.toString())) {
                    // fundo estendido
                    GlStateManager.disableTexture2D();
                    worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
                    worldrenderer.pos(-j - 11, -1 + i, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
                    worldrenderer.pos(-j - 11, 8 + i, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
                    worldrenderer.pos(j + 1, 8 + i, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
                    worldrenderer.pos(j + 1, -1 + i, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
                    tessellator.draw();
                    GlStateManager.enableTexture2D();
                    GlStateManager.depthMask(true);

                    String texture;
                    if (RoleManager.hasAtLeast(uuid, Role.STAFF)) texture = "logo_red";
                    else if (RoleManager.hasAtLeast(uuid, Role.DIAMOND)) texture = "logo_blue";
                    else if (RoleManager.hasAtLeast(uuid, Role.GOLD)) texture = "logo_yellow";
                    else texture = "logo";

                    Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("shindo/logos/" + texture + ".png"));
                    RenderUtils.drawModalRectWithCustomSizedTexture(-fontrenderer.getStringWidth(str) / 2F - 10, -1, 0, 0, 9, 9, 9, 9);
                } else {
                    // fundo sem logo
                    GlStateManager.disableTexture2D();
                    worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
                    worldrenderer.pos(-j - 1, -1 + i, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
                    worldrenderer.pos(-j - 1, 8 + i, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
                    worldrenderer.pos(j + 1, 8 + i, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
                    worldrenderer.pos(j + 1, -1 + i, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
                    tessellator.draw();
                    GlStateManager.enableTexture2D();
                    GlStateManager.depthMask(true);
                }
            } else {

                GlStateManager.disableTexture2D();
                worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
                worldrenderer.pos(-j - 1, -1 + i, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
                worldrenderer.pos(-j - 1, 8 + i, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
                worldrenderer.pos(j + 1, 8 + i, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
                worldrenderer.pos(j + 1, -1 + i, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
                tessellator.draw();
                GlStateManager.enableTexture2D();
                GlStateManager.depthMask(true);
            }

            fontrenderer.drawString(str, -fontrenderer.getStringWidth(str) / 2, i, 553648127);
            fontrenderer.drawString(str, -fontrenderer.getStringWidth(str) / 2, i, -1);
            GlStateManager.enableLighting();
            GlStateManager.disableBlend();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.popMatrix();
        }
    }
}

package me.miki.shindo.injection.mixin.mixins.client.renderer;

import me.miki.shindo.api.roles.Role;
import me.miki.shindo.api.roles.RoleManager;
import me.miki.shindo.api.ws.presence.PresenceTracker;
import me.miki.shindo.injection.interfaces.IMixinRenderPlayer;
import me.miki.shindo.management.event.impl.EventHitOverlay;
import me.miki.shindo.management.event.impl.EventRendererLivingEntity;
import me.miki.shindo.management.mods.impl.NametagMod;
import me.miki.shindo.management.mods.impl.Skin3DMod;
import me.miki.shindo.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(RendererLivingEntity.class)
public abstract class MixinRendererLivingEntity<T extends EntityLivingBase> extends Render<T> {

    @Unique
    private float red;
    @Unique
    private float green;
    @Unique
    private float blue;
    @Unique
    private float alpha;

    protected MixinRendererLivingEntity(RenderManager renderManager) {
        super(renderManager);
    }

    @Inject(method = "renderName", at = @At("HEAD"), cancellable = true)
    public void preRenderName(T entity, double x, double y, double z, CallbackInfo ci) {

        if (this.canRenderName(entity)) {

            String str = entity.getDisplayName().getFormattedText();

            double d0 = entity.getDistanceSqToEntity(Minecraft.getMinecraft().getRenderManager().livingPlayer);
            float f = entity.isSneaking() ? 32.0F : 64.0F;

            if (d0 < (double) (f * f)) {
                String s = entity.getDisplayName().getFormattedText();
                GlStateManager.alphaFunc(516, 0.1F);

                if (entity.isSneaking()) {
                    FontRenderer fontrenderer = this.getFontRendererFromRenderManager();
                    GlStateManager.pushMatrix();
                    GlStateManager.translate((float) x, (float) y + entity.height + 0.5F - (entity.isChild() ? entity.height / 2.0F : 0.0F), (float) z);
                    GL11.glNormal3f(0.0F, 1.0F, 0.0F);
                    GlStateManager.rotate(-this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
                    GlStateManager.rotate(this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
                    GlStateManager.scale(-0.02666667F, -0.02666667F, 0.02666667F);
                    GlStateManager.translate(0.0F, 9.374999F, 0.0F);
                    GlStateManager.disableLighting();
                    GlStateManager.depthMask(false);
                    GlStateManager.enableBlend();

                    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                    Tessellator tessellator = Tessellator.getInstance();
                    WorldRenderer worldrenderer = tessellator.getWorldRenderer();
                    int i = fontrenderer.getStringWidth(s) / 2;

                    if (entity instanceof AbstractClientPlayer) {
                        AbstractClientPlayer player = (AbstractClientPlayer) entity;
                        UUID uuid = player.getGameProfile().getId();

                        if (PresenceTracker.isOnline(String.valueOf(uuid))) {
                            GlStateManager.disableTexture2D();
                            worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
                            worldrenderer.pos(-i - 11, -1.0D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
                            worldrenderer.pos(-i - 11, 8.0D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
                            worldrenderer.pos(i + 1, 8.0D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
                            worldrenderer.pos(i + 1, -1.0D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
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
                            GlStateManager.disableTexture2D();
                            worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
                            worldrenderer.pos(-i - 1, -1.0D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
                            worldrenderer.pos(-i - 1, 8.0D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
                            worldrenderer.pos(i + 1, 8.0D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
                            worldrenderer.pos(i + 1, -1.0D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
                            tessellator.draw();
                            GlStateManager.enableTexture2D();
                            GlStateManager.depthMask(true);
                        }
                    } else {
                        GlStateManager.disableTexture2D();
                        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
                        worldrenderer.pos(-i - 1, -1.0D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
                        worldrenderer.pos(-i - 1, 8.0D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
                        worldrenderer.pos(i + 1, 8.0D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
                        worldrenderer.pos(i + 1, -1.0D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
                        tessellator.draw();
                        GlStateManager.enableTexture2D();
                        GlStateManager.depthMask(true);
                    }


                    fontrenderer.drawString(s, -fontrenderer.getStringWidth(s) / 2, 0, 553648127);
                    GlStateManager.enableLighting();
                    GlStateManager.disableBlend();
                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                    GlStateManager.popMatrix();
                } else {
                    renderOffsetLivingLabel(entity, x, y - (entity.isChild() ? (double) (entity.height / 2.0F) : 0.0D), z, s, 0.02666667F, d0);
                }
            }
        }
        ci.cancel();
    }

    @SuppressWarnings("unchecked")
    @Inject(method = "doRender*", at = @At("HEAD"), cancellable = true)
    public void preDoRender(T entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo ci) {

        EventRendererLivingEntity event = new EventRendererLivingEntity((RendererLivingEntity<EntityLivingBase>) (Object) this, entity, x, y, z);
        event.call();

        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Redirect(method = "canRenderName*", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/entity/RenderManager;livingPlayer:Lnet/minecraft/entity/Entity;"))
    public Entity renderOwnName(RenderManager manager) {

        if (NametagMod.getInstance().isToggled()) {
            return null;
        }

        return manager.livingPlayer;
    }

    @Inject(method = "renderModel", at = @At("TAIL"))
    private void renderModelLayers(T p_renderModel_1_, float p_renderModel_2_, float p_renderModel_3_, float p_renderModel_4_, float p_renderModel_5_, float p_renderModel_6_, float p_renderModel_7_, CallbackInfo info) {

        if (Skin3DMod.getInstance().isToggled()) {

            if (!(this instanceof IMixinRenderPlayer)) {
                return;
            }

            boolean flag = !p_renderModel_1_.isInvisible();
            boolean flag1 = (!flag && !p_renderModel_1_.isInvisibleToPlayer((Minecraft.getMinecraft()).thePlayer));

            if (flag || flag1) {

                IMixinRenderPlayer playerRenderer = (IMixinRenderPlayer) this;

                if (flag1) {
                    GlStateManager.pushMatrix();
                    GlStateManager.color(1.0F, 1.0F, 1.0F, 0.15F);
                    GlStateManager.depthMask(false);
                    GlStateManager.enableBlend();
                    GlStateManager.blendFunc(770, 771);
                    GlStateManager.alphaFunc(516, 0.003921569F);
                }
                playerRenderer.getHeadLayer().doRenderLayer((AbstractClientPlayer) p_renderModel_1_, p_renderModel_2_, 0f, p_renderModel_3_, p_renderModel_4_, p_renderModel_5_, p_renderModel_6_, p_renderModel_7_);
                playerRenderer.getBodyLayer().doRenderLayer((AbstractClientPlayer) p_renderModel_1_, p_renderModel_2_, 0f, p_renderModel_3_, p_renderModel_4_, p_renderModel_5_, p_renderModel_6_, p_renderModel_7_);
                if (flag1) {
                    GlStateManager.disableBlend();
                    GlStateManager.alphaFunc(516, 0.1F);
                    GlStateManager.popMatrix();
                    GlStateManager.depthMask(true);
                }
            }
        }
    }

    @Inject(method = "setBrightness", at = @At("HEAD"))
    public void hitColor(T entitylivingbaseIn, float partialTicks, boolean combineTextures, CallbackInfoReturnable<Boolean> cir) {

        EventHitOverlay event = new EventHitOverlay(1, 0, 0, 0.3F);
        event.call();

        red = event.getRed();
        green = event.getGreen();
        blue = event.getBlue();
        alpha = event.getAlpha();
    }

    @ModifyConstant(method = "setBrightness", constant = @Constant(floatValue = 1, ordinal = 0))
    public float setBrightnessRed(float original) {
        return red;
    }

    @ModifyConstant(method = "setBrightness", constant = @Constant(floatValue = 0, ordinal = 0))
    public float setBrightnessGreen(float original) {
        return green;
    }

    @ModifyConstant(method = "setBrightness", constant = @Constant(floatValue = 0, ordinal = 1))
    public float setBrightnessBlue(float original) {
        return blue;
    }

    @ModifyConstant(method = "setBrightness", constant = @Constant(floatValue = 0.3F, ordinal = 0))
    public float setBrightnessAlpha(float original) {
        return alpha;
    }
}

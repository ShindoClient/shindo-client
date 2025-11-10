package me.miki.shindo.management.cosmetic.wing.layer;

import me.miki.shindo.Shindo;
import me.miki.shindo.injection.interfaces.IMixinModelBase;
import me.miki.shindo.management.cosmetic.wing.WingManager;
import me.miki.shindo.management.cosmetic.wing.impl.Wing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;

public class WingRenderLayer implements LayerRenderer<AbstractClientPlayer> {

    private static ModelRenderer wing;
    private static ModelRenderer wingTip;
    private final WingRenderLayer.ModelDragonWings modelDragonWings;
    private final Minecraft mc = Minecraft.getMinecraft();
    private boolean flying = false;

    public WingRenderLayer(RenderPlayer renderPlayer) {
        this.modelDragonWings = new ModelDragonWings();

        // 🔄 Agora usando o cast para a interface
        IMixinModelBase mixinModel = (IMixinModelBase) this.modelDragonWings;
        mixinModel.setTextureOffset("wingTip.bone", 112, 136);
        mixinModel.setTextureOffset("wing.skin", -56, 88);
        mixinModel.setTextureOffset("wing.bone", 112, 88);
        mixinModel.setTextureOffset("wingTip.skin", -56, 144);

        int bw = this.modelDragonWings.textureWidth;
        int bh = this.modelDragonWings.textureHeight;
        this.modelDragonWings.textureWidth = 256;
        this.modelDragonWings.textureHeight = 256;

        wing = new ModelRenderer(this.modelDragonWings, "wing");
        wing.setRotationPoint(-12.0F, 5.0F, 2.0F);
        wing.addBox("bone", -56.0F, -4.0F, -4.0F, 56, 8, 8);
        wing.addBox("skin", -56.0F, 0.0F, 2.0F, 56, 0, 56);
        wing.isHidden = true;

        wingTip = new ModelRenderer(this.modelDragonWings, "wingTip");
        wingTip.setRotationPoint(-56.0F, 0.0F, 0.0F);
        wingTip.isHidden = true;
        wingTip.addBox("bone", -56.0F, -2.0F, -2.0F, 56, 4, 4);
        wingTip.addBox("skin", -56.0F, 0.0F, 2.0F, 56, 0, 56);
        wing.addChild(wingTip);

        this.modelDragonWings.textureWidth = bw;
        this.modelDragonWings.textureHeight = bh;
    }

    @Override
    public void doRenderLayer(AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float HeadYaw, float headPitch, float scale) {

        if (mc.thePlayer == null || !player.getUniqueID().equals(mc.thePlayer.getUniqueID())) {
            return;
        }

        WingManager wingManager = Shindo.getInstance().getWingManager();
        Wing activeWing = wingManager != null ? wingManager.getCurrentWing() : null;
        if (activeWing == null || activeWing.getWing() == null) {
            return;
        }

        mc.getTextureManager().bindTexture(activeWing.getWing());

        if (player.isSneaking()) {
            GL11.glTranslated(0.0D, 0.225D, 0.0D);
        }

        GlStateManager.pushMatrix();
        this.modelDragonWings.render(player, limbSwing, limbSwingAmount, ageInTicks, HeadYaw, headPitch, scale);
        this.modelDragonWings.setRotationAngles(scale, limbSwing, limbSwingAmount, ageInTicks, HeadYaw, headPitch, player);
        GL11.glPopMatrix();
        if (!player.isSneaking()) {
            GL11.glTranslated(0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }

    class ModelDragonWings extends ModelBase {
        public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
            super.render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
            GlStateManager.pushMatrix();
            float f1 = 0.0F;
            if (Minecraft.getMinecraft().thePlayer.capabilities.isFlying) {
                f1 = ageInTicks / 200.0F;
            } else {
                f1 = ageInTicks / 80.0F;
            }

            float anSpeed = 100.0F;
            if (!entityIn.onGround || WingRenderLayer.this.flying) {
                anSpeed = 50.0F;
                WingRenderLayer.this.flying = true;
            }

            GlStateManager.scale(0.15D, 0.15D, 0.15D);
            GlStateManager.translate(0.0D, -0.3D, 1.1D);
            GlStateManager.rotate(50.0F, -50.0F, 0.0F, 0.0F);
            boolean x = false;
            boolean index = false;

            for (int i = 0; i < 2; ++i) {
                float f6 = f1 * 9.141593F * 2.0F;
                WingRenderLayer.wing.rotateAngleX = 0.125F - (float) Math.cos(f6) * 0.2F;
                WingRenderLayer.wing.rotateAngleY = 0.25F;
                WingRenderLayer.wing.rotateAngleZ = (float) (Math.sin(f6) + 1.225D) * 0.3F;
                WingRenderLayer.wingTip.rotateAngleZ = -((float) (Math.sin(f6 + 2.0F) + 0.5D)) * 0.75F;
                WingRenderLayer.wing.isHidden = false;
                WingRenderLayer.wingTip.isHidden = false;
                if (!entityIn.isInvisible()) {
                    GlStateManager.pushMatrix();
                    GlStateManager.disableLighting();
                    WingRenderLayer.wing.render(scale);
                    GlStateManager.blendFunc(1, 1);
                    GlStateManager.enableLighting();
                    GlStateManager.popMatrix();
                }

                WingRenderLayer.wing.isHidden = false;
                WingRenderLayer.wingTip.isHidden = false;
                if (i == 0) {
                    GlStateManager.scale(-1.0F, 1.0F, 1.0F);
                }
            }

            GlStateManager.popMatrix();
            WingRenderLayer.this.flying = !entityIn.onGround;
        }
    }
}

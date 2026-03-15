package me.miki.shindo.hooks;

import me.miki.shindo.management.mods.impl.ItemPhysicsMod;
import me.miki.shindo.management.mods.impl.Items2DMod;
import me.miki.shindo.management.mods.impl.UHCOverlayMod;
import me.miki.shindo.management.settings.impl.NumberSetting;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;

public enum RenderEntityItemHook {
    ;

    public static int func_177077_a(EntityItem itemIn, double p_177077_2_, double p_177077_4_, double p_177077_6_, float p_177077_8_, IBakedModel p_177077_9_, int func_177078_a) {

        ItemStack itemstack = itemIn.getEntityItem();
        Item item = itemstack.getItem();
        Block block = Block.getBlockFromItem(item);

        ItemPhysicsMod mod = ItemPhysicsMod.instance;
        NumberSetting speedSetting = mod.getSpeedSetting();
        float speed = speedSetting != null ? speedSetting.getValueFloat() : 1.0F;

        if (item == null) {
            return 0;
        } else {
            boolean flag = p_177077_9_.isGui3d();
            int i = func_177078_a;

            if (mod.isToggled()) {
                if (block != null) {
                    GlStateManager.translate((float) p_177077_2_, (float) p_177077_4_ + 0.15F, (float) p_177077_6_);
                } else {
                    GlStateManager.translate((float) p_177077_2_, (float) p_177077_4_ + 0.02F, (float) p_177077_6_);
                    GlStateManager.rotate(-90F, 1F, 0F, 0F);
                }
            } else {
                float f1 = MathHelper.sin(((float) itemIn.getAge() + p_177077_8_) / 10.0F + itemIn.hoverStart) * 0.1F + 0.1F;
                float f2 = p_177077_9_.getItemCameraTransforms().getTransform(ItemCameraTransforms.TransformType.GROUND).scale.y;

                GlStateManager.translate((float) p_177077_2_, (float) p_177077_4_ + f1 + 0.25F * f2, (float) p_177077_6_);
            }

            if (!mod.isToggled()) {
                if (flag || Minecraft.getMinecraft().getRenderManager().options != null) {
                    if (p_177077_9_.isGui3d() || !Items2DMod.instance.isToggled()) {
                        float f3 = (((float) itemIn.getAge() + p_177077_8_) / 20.0F + itemIn.hoverStart) * (180F / (float) Math.PI);
                        GlStateManager.rotate(f3, 0.0F, 1.0F, 0.0F);
                    } else {
                        GlStateManager.rotate(180.0F - (Minecraft.getMinecraft().getRenderManager()).playerViewY, 0.0F, 1.0F, 0.0F);
                        GlStateManager.rotate(((Minecraft.getMinecraft()).gameSettings.thirdPersonView == 2) ? (Minecraft.getMinecraft().getRenderManager()).playerViewX : -(Minecraft.getMinecraft().getRenderManager()).playerViewX, 1.0F, 0.0F, 0.0F);
                    }
                }
            }

            if (!flag) {
                float f6 = -0.0F * (float) (i - 1) * 0.5F;
                float f4 = -0.0F * (float) (i - 1) * 0.5F;
                float f5 = -0.046875F * (float) (i - 1) * 0.5F;
                GlStateManager.translate(f6, f4, f5);
            }

            if (mod.isToggled() && !itemIn.onGround) {
                float angle = System.currentTimeMillis() % (360 * 20) / (float) (4.5 - (speed));
                GlStateManager.rotate(angle, 1F, 1F, 1F);
            }

            UHCOverlayMod uhcMod = UHCOverlayMod.instance;
            NumberSetting ingotScaleSetting = uhcMod.getGoldIngotScaleSetting();
            NumberSetting nuggetScaleSetting = uhcMod.getGoldNuggetScaleSetting();
            NumberSetting appleScaleSetting = uhcMod.getGoldAppleScaleSetting();
            NumberSetting oreScaleSetting = uhcMod.getGoldOreScaleSetting();
            NumberSetting skullScaleSetting = uhcMod.getSkullScaleSetting();
            float ingotScale = ingotScaleSetting != null ? ingotScaleSetting.getValueFloat() : 1.0F;
            float nuggetScale = nuggetScaleSetting != null ? nuggetScaleSetting.getValueFloat() : 1.0F;
            float appleScale = appleScaleSetting != null ? appleScaleSetting.getValueFloat() : 1.0F;
            float oreScale = oreScaleSetting != null ? oreScaleSetting.getValueFloat() : 1.0F;
            float skullScale = skullScaleSetting != null ? skullScaleSetting.getValueFloat() : 1.0F;

            float f6 = -0.0F * (float) (i - 1) * 0.5F;
            float f4 = -0.0F * (float) (i - 1) * 0.5F;
            float f5 = -0.046875F * (float) (i - 1) * 0.5F;

            if (uhcMod.isToggled()) {
                if (item == Items.gold_ingot) {

                    if (!mod.isToggled()) {
                        GlStateManager.translate(f6, f4 + (ingotScale / 8), f5);
                    }

                    GlStateManager.scale(ingotScale, ingotScale, ingotScale);
                }
                if (item == Items.gold_nugget) {
                    if (!mod.isToggled()) {
                        GlStateManager.translate(f6, f4 + (nuggetScale / 8), f5);
                    }
                    GlStateManager.scale(nuggetScale, nuggetScale, nuggetScale);
                }
                if (item == Items.golden_apple) {
                    if (!mod.isToggled()) {
                        GlStateManager.translate(f6, f4 + (appleScale / 8), f5);
                    }
                    GlStateManager.scale(appleScale, appleScale, appleScale);
                }
                if (block == Blocks.gold_ore) {
                    if (!mod.isToggled()) {
                        GlStateManager.translate(f6, f4 + (oreScale / 8), f5);
                    }
                    GlStateManager.scale(oreScale, oreScale, oreScale);
                }
                if (item == Items.skull) {
                    if (!mod.isToggled()) {
                        GlStateManager.translate(f6, f4 + (skullScale / 8), f5);
                    }
                    GlStateManager.scale(skullScale, skullScale, skullScale);
                }
            }

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            return i;
        }
    }

    public static void oldItemRender(RenderItem instance, IBakedModel model, ItemStack stack) {
        GlStateManager.pushMatrix();
        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
        Minecraft.getMinecraft().getTextureManager().getTexture(TextureMap.locationBlocksTexture).setBlurMipmap(false, false);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableBlend();
        GlStateManager.disableCull();
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.blendFunc(770, 771);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        instance.renderItem(stack, model);
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableLighting();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.popMatrix();
        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
        Minecraft.getMinecraft().getTextureManager().getTexture(TextureMap.locationBlocksTexture).restoreLastBlurMipmap();
    }
}

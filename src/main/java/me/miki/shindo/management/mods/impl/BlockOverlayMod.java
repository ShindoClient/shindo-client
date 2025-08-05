package me.miki.shindo.management.mods.impl;

import me.miki.shindo.Shindo;
import me.miki.shindo.management.color.AccentColor;
import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventBlockHighlightRender;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import me.miki.shindo.management.mods.settings.impl.BooleanSetting;
import me.miki.shindo.management.mods.settings.impl.ColorSetting;
import me.miki.shindo.management.mods.settings.impl.NumberSetting;
import me.miki.shindo.utils.ColorUtils;
import me.miki.shindo.utils.Render3DUtils;
import me.miki.shindo.utils.TimerUtils;
import me.miki.shindo.utils.animation.simple.SimpleAnimation;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.WorldSettings;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class BlockOverlayMod extends Mod {

    protected AxisAlignedBB currentBB;
    protected AxisAlignedBB slideBB;
    protected TimerUtils timer = new TimerUtils();

    private final SimpleAnimation[] simpleAnimation = {new SimpleAnimation(0.0F), new SimpleAnimation(0.0F), new SimpleAnimation(0.0F), new SimpleAnimation(0.0F), new SimpleAnimation(0.0F), new SimpleAnimation(0.0F)};

    private final BooleanSetting animationSetting = new BooleanSetting(TranslateText.ANIMATION, this, false);
    private final BooleanSetting fillSetting = new BooleanSetting(TranslateText.FILL, this, true);
    private final BooleanSetting outlineSetting = new BooleanSetting(TranslateText.OUTLINE, this, true);

    private final NumberSetting fillAlphaSetting = new NumberSetting(TranslateText.FILL_ALPHA, this, 0.15, 0, 1.0, false);
    private final NumberSetting outlineAlphaSetting = new NumberSetting(TranslateText.OUTLINE_ALPHA, this, 0.15, 0, 1.0, false);

    private final NumberSetting outlineWidthSetting = new NumberSetting(TranslateText.OUTLINE_WIDTH, this, 4, 1, 10, false);

    private final BooleanSetting depthSetting = new BooleanSetting(TranslateText.DEPTH, this, false);
    private final BooleanSetting customColorSetting = new BooleanSetting(TranslateText.CUSTOM_COLOR, this, false);
    private final ColorSetting fillColorSetting = new ColorSetting(TranslateText.FILL_COLOR, this, Color.RED, false);
    private final ColorSetting outlineColorSetting = new ColorSetting(TranslateText.OUTLINE_COLOR, this, Color.RED, false);

    public BlockOverlayMod() {
        super(TranslateText.BLOCK_OVERLAY, TranslateText.BLOCK_OVERLAY_DESCRIPTION, ModCategory.RENDER, "blockoutline");
    }

    @EventTarget
    public void onBlockHighlightRender(EventBlockHighlightRender event) {

        AccentColor currentColor = Shindo.getInstance().getColorManager().getCurrentColor();

        event.setCancelled(true);

        if (!canRender(event.getObjectMouseOver())) {
            return;
        }

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        if (depthSetting.isToggled()) {
            GlStateManager.disableDepth();
        }

        GlStateManager.disableTexture2D();

        GlStateManager.depthMask(false);
        BlockPos blockpos = event.getObjectMouseOver().getBlockPos();
        Block block = mc.theWorld.getBlockState(blockpos).getBlock();

        if (block.getMaterial() != Material.air && mc.theWorld.getWorldBorder().contains(blockpos)) {

            block.setBlockBoundsBasedOnState(mc.theWorld, blockpos);

            double x = mc.getRenderViewEntity().lastTickPosX
                    + (mc.getRenderViewEntity().posX - mc.getRenderViewEntity().lastTickPosX) * (double) event.getPartialTicks();
            double y = mc.getRenderViewEntity().lastTickPosY
                    + (mc.getRenderViewEntity().posY - mc.getRenderViewEntity().lastTickPosY) * (double) event.getPartialTicks();
            double z = mc.getRenderViewEntity().lastTickPosZ
                    + (mc.getRenderViewEntity().posZ - mc.getRenderViewEntity().lastTickPosZ) * (double) event.getPartialTicks();

            AxisAlignedBB selectedBox = block.getSelectedBoundingBox(mc.theWorld, blockpos);

            if (animationSetting.isToggled()) {

                if (!selectedBox.equals(currentBB)) {
                    slideBB = currentBB;
                    currentBB = selectedBox;
                }

                AxisAlignedBB slide;

                if ((slide = slideBB) != null) {

                    simpleAnimation[0].setAnimation((float) (slide.minX + (selectedBox.minX - slide.minX)), 24);
                    simpleAnimation[1].setAnimation((float) (slide.minY + (selectedBox.minY - slide.minY)), 24);
                    simpleAnimation[2].setAnimation((float) (slide.minZ + (selectedBox.minZ - slide.minZ)), 24);
                    simpleAnimation[3].setAnimation((float) (slide.maxX + (selectedBox.maxX - slide.maxX)), 24);
                    simpleAnimation[4].setAnimation((float) (slide.maxY + (selectedBox.maxY - slide.maxY)), 24);
                    simpleAnimation[5].setAnimation((float) (slide.maxZ + (selectedBox.maxZ - slide.maxZ)), 24);

                    AxisAlignedBB renderBB = new AxisAlignedBB(
                            simpleAnimation[0].getValue() - 0.01,
                            simpleAnimation[1].getValue() - 0.01,
                            simpleAnimation[2].getValue() - 0.01,
                            simpleAnimation[3].getValue() + 0.01,
                            simpleAnimation[4].getValue() + 0.01,
                            simpleAnimation[5].getValue() + 0.01
                    );

                    if (fillSetting.isToggled()) {
                        ColorUtils.setColor(customColorSetting.isToggled() ? fillColorSetting.getColor().getRGB() : currentColor.getInterpolateColor().getRGB(), fillAlphaSetting.getValueFloat());
                        Render3DUtils.drawFillBox(interpolateAxis(renderBB));
                    }

                    if (outlineSetting.isToggled()) {
                        ColorUtils.setColor(customColorSetting.isToggled() ? outlineColorSetting.getColor().getRGB() : currentColor.getInterpolateColor().getRGB(), outlineAlphaSetting.getValueFloat());
                        GL11.glLineWidth(outlineWidthSetting.getValueFloat());
                        RenderGlobal.drawSelectionBoundingBox(interpolateAxis(renderBB));
                    }
                }
            } else {

                selectedBox = selectedBox.expand(0.0020000000949949026D, 0.0020000000949949026D, 0.0020000000949949026D).offset(-x, -y, -z);

                if (fillSetting.isToggled()) {
                    ColorUtils.setColor(customColorSetting.isToggled() ? fillColorSetting.getColor().getRGB() : currentColor.getInterpolateColor().getRGB(), fillAlphaSetting.getValueFloat());
                    Render3DUtils.drawFillBox(selectedBox);
                }

                if (outlineSetting.isToggled()) {
                    ColorUtils.setColor(customColorSetting.isToggled() ? outlineColorSetting.getColor().getRGB() : currentColor.getInterpolateColor().getRGB(), outlineAlphaSetting.getValueFloat());
                    GL11.glLineWidth(outlineWidthSetting.getValueFloat());
                    RenderGlobal.drawSelectionBoundingBox(selectedBox);
                }
            }
        }

        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();

        GlStateManager.disableBlend();

        if (depthSetting.isToggled()) {
            GlStateManager.enableDepth();
        }

        GL11.glLineWidth(2);
    }

    private boolean canRender(MovingObjectPosition movingObjectPositionIn) {

        Entity entity = mc.getRenderViewEntity();
        boolean result = entity instanceof EntityPlayer && !mc.gameSettings.hideGUI;

        if (result && !((EntityPlayer) entity).capabilities.allowEdit) {
            ItemStack itemstack = ((EntityPlayer) entity).getCurrentEquippedItem();

            if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                BlockPos selectedBlock = mc.objectMouseOver.getBlockPos();
                Block block = mc.theWorld.getBlockState(selectedBlock).getBlock();

                if (mc.playerController.getCurrentGameType() == WorldSettings.GameType.SPECTATOR) {
                    result = block.hasTileEntity() && mc.theWorld.getTileEntity(selectedBlock) instanceof IInventory;
                } else {
                    result = itemstack != null && (itemstack.canDestroy(block) || itemstack.canPlaceOn(block));
                }
            }
        }

        result = result && movingObjectPositionIn.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK;

        return result;
    }

    public AxisAlignedBB interpolateAxis(AxisAlignedBB bb) {
        return new AxisAlignedBB(
                bb.minX - mc.getRenderManager().viewerPosX,
                bb.minY - mc.getRenderManager().viewerPosY,
                bb.minZ - mc.getRenderManager().viewerPosZ,
                bb.maxX - mc.getRenderManager().viewerPosX,
                bb.maxY - mc.getRenderManager().viewerPosY,
                bb.maxZ - mc.getRenderManager().viewerPosZ);
    }
}

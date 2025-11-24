package me.miki.shindo.management.mods.impl;

import me.miki.shindo.Shindo;
import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventRender2D;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.SimpleHUDMod;
import me.miki.shindo.management.nanovg.NanoVGManager;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class ArmorStatusMod extends SimpleHUDMod {

    public ArmorStatusMod() {
        super(TranslateText.ARMOR_STATUS, TranslateText.ARMOR_STATUS_DESCRIPTION);
    }

    @EventTarget
    public void onRender2D(EventRender2D event) {


        ItemStack[] fakeStack = new ItemStack[4];

        fakeStack[3] = new ItemStack(Items.diamond_helmet);
        fakeStack[2] = new ItemStack(Items.diamond_chestplate);
        fakeStack[1] = new ItemStack(Items.diamond_leggings);
        fakeStack[0] = new ItemStack(Items.diamond_boots);

        NanoVGManager nvg = Shindo.getInstance().getNanoVGManager();
        ItemStack[] stacks = this.isEditing() || mc.thePlayer == null ? fakeStack : mc.thePlayer.inventory.armorInventory;
        nvg.setupAndDraw(() -> this.drawNanoVG(nvg, stacks));
        renderArmorItems(stacks);
    }

    private void drawNanoVG(NanoVGManager nvg, ItemStack[] items) {

        this.drawBackground(72, 64);

        for (int i = 0; i < 4; i++) {
            ItemStack item = items[Math.abs(3 - i)];
            int addY = 16 * i;
            if (item != null) {
                int remaining = item.getMaxDamage() - item.getItemDamage();
                drawText(String.valueOf(remaining), 28, addY + 4, 9F, getHudFont(1));
            }
        }

        this.setWidth(72);
        this.setHeight(16 * 4);
    }

    private void renderArmorItems(ItemStack[] items) {
        GlStateManager.pushMatrix();
        RenderHelper.enableGUIStandardItemLighting();
        for (int i = 0; i < 4; i++) {
            ItemStack item = items[Math.abs(3 - i)];
            if (item == null) continue;
            int addY = 16 * i;
            int iconX = getX() + 6;
            int iconY = getY() + addY;
            mc.getRenderItem().renderItemAndEffectIntoGUI(item, iconX, iconY);
            mc.getRenderItem().renderItemOverlays(mc.fontRendererObj, item, iconX, iconY);
        }
        RenderHelper.disableStandardItemLighting();
        GlStateManager.popMatrix();
    }
}

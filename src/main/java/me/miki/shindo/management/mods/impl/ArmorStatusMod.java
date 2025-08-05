package me.miki.shindo.management.mods.impl;

import me.miki.shindo.Shindo;
import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventRender2D;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.SimpleHUDMod;
import me.miki.shindo.management.nanovg.NanoVGManager;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

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
        nvg.setupAndDraw(() -> this.drawNanoVG(nvg, this.isEditing() ? fakeStack : mc.thePlayer.inventory.armorInventory));
    }

    private void drawNanoVG(NanoVGManager nvg, ItemStack[] items) {

        this.drawBackground(48, 64);

        for (int i = 0; i < 4; i++) {
            ItemStack item = items[Math.abs(3 - i)];
            int addY = 16 * i;
            if (item != null) {
                drawImage(new ResourceLocation("shindo/armor/" + item.getItem().getUnlocalizedName().replace("item.", "") + ".png"), getX() + 30, getY() + addY, 16, 16);
                drawText(String.valueOf(item.getMaxDamage() - item.getItemDamage()), 5, addY + 4, 9F, getHudFont(1));
            }
        }

        this.setWidth(48);
        this.setHeight(16 * 4);
    }

}

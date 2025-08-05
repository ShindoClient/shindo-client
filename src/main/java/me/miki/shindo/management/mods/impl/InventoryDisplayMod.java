package me.miki.shindo.management.mods.impl;

import me.miki.shindo.Shindo;
import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventRender2D;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.HUDMod;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.utils.GlUtils;
import me.miki.shindo.utils.render.RenderUtils;
import net.minecraft.item.ItemStack;

public class InventoryDisplayMod extends HUDMod {

    public InventoryDisplayMod() {
        super(TranslateText.INVENTORY_DISPLAY, TranslateText.INVENTORY_DISPLAY_DESCRIPTION);
    }

    @EventTarget
    public void onRender2D(EventRender2D event) {

        int startX = this.getX() + 6;
        int startY = this.getY() + 22;
        int index = 0;

        NanoVGManager nvg = Shindo.getInstance().getNanoVGManager();

        nvg.setupAndDraw(() -> drawNanoVG());

        GlUtils.startScale(this.getX(), this.getY(), this.getScale());

        for (int i = 9; i < 36; i++) {

            ItemStack slot = mc.thePlayer.inventory.mainInventory[i];

            if (slot == null) {
                startX += 20;
                index += 1;

                if (index > 8) {
                    index = 0;
                    startY += 20;
                    startX = this.getX() + 4;
                }

                continue;
            }

            RenderUtils.drawItemStack(slot, startX, startY);

            startX += 20;
            index += 1;
            if (index > 8) {
                index = 0;
                startY += 20;
                startX = this.getX() + 6;
            }
        }

        GlUtils.stopScale();
    }

    private void drawNanoVG() {

        this.drawBackground(188, 82);
        this.drawText("Inventory", 5.5F, 6F, 10.5F, getHudFont(1));
        this.drawRect(0, 17.5F, 188, 1);

        this.setWidth(188);
        this.setHeight(82);
    }
}

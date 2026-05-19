package me.miki.shindo.management.mods.impl

import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventNVG
import me.miki.shindo.management.event.impl.EventRender2D
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.HUDMod
import me.miki.shindo.management.nanovg.font.Lucide
import me.miki.shindo.management.nanovg.font.Shinconic
import me.miki.shindo.utils.GlUtils.startScale
import me.miki.shindo.utils.GlUtils.stopScale
import me.miki.shindo.utils.render.RenderUtils.drawItemStack

class InventoryDisplayMod :
    HUDMod(
        TranslateText.INVENTORY_DISPLAY,
        TranslateText.INVENTORY_DISPLAY_DESCRIPTION,
        Shinconic.MOD_INVENTORY_DISPLAY,
    ) {
    @EventTarget
    fun onRender2D(event: EventRender2D?) {
        var startX = this.getX() + 6
        var startY = this.getY() + 22
        var index = 0

        startScale(this.getX().toFloat(), this.getY().toFloat(), this.getScale())

        for (i in 9..35) {
            val slot = mc.thePlayer.inventory.mainInventory[i]

            if (slot == null) {
                startX += 20
                index += 1

                if (index > 8) {
                    index = 0
                    startY += 20
                    startX = this.getX() + 4
                }

                continue
            }

            drawItemStack(slot, startX, startY)

            startX += 20
            index += 1
            if (index > 8) {
                index = 0
                startY += 20
                startX = this.getX() + 6
            }
        }

        stopScale()
    }

    @EventTarget
    fun drawNanoVG(event: EventNVG?) {
        this.drawBackground(188f, 82f)
        this.drawText("Inventory", 5.5f, 6f, 10.5f, getHudFont(1))
        this.drawRect(0f, 17.5f, 188f, 1f)

        this.setWidth(188)
        this.setHeight(82)
    }
}

package me.miki.shindo.management.mods.impl

import me.miki.shindo.Shindo.Companion.getInstance
import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventRender2D
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.SimpleHUDMod
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Lucide
import me.miki.shindo.management.nanovg.font.Shinconic
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import kotlin.math.abs

class ArmorStatusMod : SimpleHUDMod(TranslateText.ARMOR_STATUS, TranslateText.ARMOR_STATUS_DESCRIPTION, Shinconic.MOD_ARMOR_STATUS) {
    @EventTarget
    fun onRender2D(event: EventRender2D?) {
        val fakeStack = arrayOfNulls<ItemStack>(4)

        fakeStack[3] = ItemStack(Items.diamond_helmet)
        fakeStack[2] = ItemStack(Items.diamond_chestplate)
        fakeStack[1] = ItemStack(Items.diamond_leggings)
        fakeStack[0] = ItemStack(Items.diamond_boots)

        val nvg = getInstance().nanoVGManager
        val stacks = if (this.isEditing() || mc.thePlayer == null) fakeStack else mc.thePlayer.inventory.armorInventory
        nvg!!.setupAndDraw(Runnable { this.drawNanoVG(nvg, stacks) })
        renderArmorItems(stacks)
    }

    private fun drawNanoVG(
        nvg: NanoVGManager?,
        items: Array<ItemStack?>,
    ) {
        this.drawBackground(52f, 64f)

        for (i in 0..3) {
            val item = items[abs(3 - i)]
            val addY = 16 * i
            if (item != null) {
                val remaining = item.maxDamage - item.itemDamage
                drawText(remaining.toString(), 28f, (addY + 4).toFloat(), 9f, getHudFont(1))
            }
        }

        this.setWidth(52)
        this.setHeight(16 * 4)
    }

    private fun renderArmorItems(items: Array<ItemStack?>) {
        GlStateManager.pushMatrix()
        RenderHelper.enableGUIStandardItemLighting()
        for (i in 0..3) {
            val item = items[abs(3 - i)]
            if (item == null) continue
            val addY = 16 * i
            val iconX = getX() + 6
            val iconY = getY() + addY
            mc.renderItem.renderItemAndEffectIntoGUI(item, iconX, iconY)
            mc.renderItem.renderItemOverlays(mc.fontRendererObj, item, iconX, iconY)
        }
        RenderHelper.disableStandardItemLighting()
        GlStateManager.popMatrix()
    }
}

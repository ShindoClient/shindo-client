package me.miki.shindo.management.mods.impl

import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventRenderSelectedItem
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyType
import me.miki.shindo.utils.PlayerUtils.isCreative
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.item.ItemPotion
import net.minecraft.item.ItemStack
import net.minecraft.potion.Potion
import net.minecraft.potion.PotionEffect
import net.minecraft.util.EnumChatFormatting
import net.minecraft.util.StatCollector

class ItemInfoMod :
    Mod(TranslateText.ITEM_INFO, TranslateText.ITEM_INFO_DESCRIPTION, ModCategory.RENDER, LegacyIcon.MOD_ITEM_INFO) {
    private val ENCHANTMENT_SHORT_NAME: MutableMap<*, *> = object : HashMap<Any?, Any?>() {
        init {
            this.put(0, "P")
            this.put(1, "FP")
            this.put(2, "FF")
            this.put(3, "BP")
            this.put(4, "PP")
            this.put(5, "R")
            this.put(6, "AA")
            this.put(7, "T")
            this.put(8, "DS")
            this.put(9, "FW")
            this.put(16, "SH")
            this.put(17, "SM")
            this.put(18, "BoA")
            this.put(19, "KB")
            this.put(20, "FA")
            this.put(21, "L")
            this.put(32, "EFF")
            this.put(33, "ST")
            this.put(34, "UNB")
            this.put(35, "F")
            this.put(48, "POW")
            this.put(49, "PUN")
            this.put(50, "FLA")
            this.put(51, "INF")
            this.put(61, "LoS")
            this.put(62, "LU")
            this.put(70, "MEN")
        }
    }

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.POTION_COLOR)
    private val potionColorSetting = false

    @EventTarget
    fun onRenderTooltip(event: EventRenderSelectedItem) {
        val heldItemStack = mc.thePlayer.inventory.getCurrentItem()

        val sr = ScaledResolution(Minecraft.getMinecraft())

        var addY = 70

        if (heldItemStack != null) {
            var s1 = ""

            if (heldItemStack.item is ItemPotion) {
                s1 = this.getPotionEffectString(heldItemStack)
            } else {
                s1 = this.getEnchantmentString(heldItemStack)
            }

            if (isCreative()) {
                addY = 55
            }

            fr.drawStringWithShadow(
                s1,
                (sr.scaledWidth / 2f) - (mc.fontRendererObj.getStringWidth(s1) / 2f),
                (sr.scaledHeight - addY - 1).toFloat(),
                event.color
            )
        }
    }

    private fun getPotionEffectString(heldItemStack: ItemStack): String {
        val potion = heldItemStack.item as ItemPotion
        val effects: MutableList<*>? = potion.getEffects(heldItemStack)

        if (effects == null) {
            return ""
        } else {
            val potionBuilder = StringBuilder()

            for (effect in effects) {
                val entry = effect as PotionEffect
                val duration = entry.duration / 20

                if (potionColorSetting) {
                    if (entry.potionID == Potion.moveSpeed.getId()) {
                        potionBuilder.append(EnumChatFormatting.AQUA)
                    }
                    if (entry.potionID == Potion.regeneration.getId()) {
                        potionBuilder.append(EnumChatFormatting.LIGHT_PURPLE)
                    }
                    if (entry.potionID == Potion.poison.getId()) {
                        potionBuilder.append(EnumChatFormatting.DARK_GREEN)
                    }
                    if (entry.potionID == Potion.jump.getId()) {
                        potionBuilder.append(EnumChatFormatting.GREEN)
                    }
                    if (entry.potionID == Potion.fireResistance.getId()) {
                        potionBuilder.append(EnumChatFormatting.GOLD)
                    }
                    if (entry.potionID == Potion.heal.getId()) {
                        potionBuilder.append(EnumChatFormatting.RED)
                    }
                    if (entry.potionID == Potion.moveSlowdown.getId()) {
                        potionBuilder.append(EnumChatFormatting.GRAY)
                    }
                    if (entry.potionID == Potion.nightVision.getId()) {
                        potionBuilder.append(EnumChatFormatting.DARK_BLUE)
                    }
                    if (entry.potionID == Potion.damageBoost.getId()) {
                        potionBuilder.append(EnumChatFormatting.DARK_PURPLE)
                    }
                }

                potionBuilder.append(StatCollector.translateToLocal(entry.effectName))
                potionBuilder.append(EnumChatFormatting.WHITE)
                potionBuilder.append(" ")
                potionBuilder.append(entry.amplifier + 1)
                potionBuilder.append(" ")
                potionBuilder.append("(")
                potionBuilder.append(duration / 60).append(String.format(":%02d", duration % 60))
                potionBuilder.append(") ")
            }

            return potionBuilder.toString().trim { it <= ' ' }
        }
    }

    private fun getEnchantmentString(heldItemStack: ItemStack): String {
        val enchantBuilder = StringBuilder()
        val en: MutableMap<*, *> = EnchantmentHelper.getEnchantments(heldItemStack)

        for (o in en.entries) {
            val entry = o

            enchantBuilder.append(this.ENCHANTMENT_SHORT_NAME.get(entry.key) as String?)
            enchantBuilder.append(" ")
            enchantBuilder.append(entry.value)
            enchantBuilder.append(" ")
        }

        return enchantBuilder.toString().trim { it <= ' ' }
    }
}





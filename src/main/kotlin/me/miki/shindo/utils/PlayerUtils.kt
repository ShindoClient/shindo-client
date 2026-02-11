package me.miki.shindo.utils

import net.minecraft.block.Block
import net.minecraft.client.Minecraft
import net.minecraft.client.network.NetworkPlayerInfo
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.Entity
import net.minecraft.init.Blocks
import net.minecraft.item.*
import net.minecraft.potion.Potion
import net.minecraft.util.MathHelper
import net.minecraft.world.WorldSettings

object PlayerUtils {

    private val mc: Minecraft = Minecraft.getMinecraft()

    @JvmField
    val MODIFIER_BY_TICK: MutableMap<Int, Float> = mutableMapOf(
        0 to 0.0f,
        1 to 0.00037497282f,
        2 to 0.0015000105f,
        3 to 0.0033749938f,
        4 to 0.0059999824f,
        5 to 0.009374976f,
        6 to 0.013499975f,
        7 to 0.01837498f,
        8 to 0.023999989f,
        9 to 0.030375004f,
        10 to 0.037500024f,
        11 to 0.04537499f,
        12 to 0.05400002f,
        13 to 0.063374996f,
        14 to 0.07349998f,
        15 to 0.084375024f,
        16 to 0.096000016f,
        17 to 0.10837501f,
        18 to 0.121500015f,
        19 to 0.13537502f,
        20 to 0.14999998f
    )

    @JvmStatic
    fun hasItem(item: Item): Boolean {
        for (i in 0 until 9) {
            val itemStack = mc.thePlayer.inventory.getStackInSlot(i)
            if (itemStack != null && itemStack.item == item) {
                return true
            }
        }
        return false
    }

    @JvmStatic
    fun getSpeed(): Float {
        val distTraveledLastTickX = mc.thePlayer.posX - mc.thePlayer.prevPosX
        val distTraveledLastTickZ = mc.thePlayer.posZ - mc.thePlayer.prevPosZ
        val currentSpeed =
            MathHelper.sqrt_double(distTraveledLastTickX * distTraveledLastTickX + distTraveledLastTickZ * distTraveledLastTickZ)
        return (currentSpeed / 0.05).toFloat()
    }

    @JvmStatic
    fun getItemSlot(item: Item): Int {
        var slot = -1
        for (i in 0 until 9) {
            val itemStack = mc.thePlayer.inventory.getStackInSlot(i)
            if (itemStack != null && itemStack.item == item) {
                slot = i
                break
            }
        }
        if (slot == -1) {
            return mc.thePlayer.inventory.currentItem
        }
        return slot
    }

    @JvmStatic
    fun getBestBow(entity: Entity): Int {
        var slot = -1
        var bestBow: ItemStack? = null

        for (i in 0 until 9) {
            val itemStack = mc.thePlayer.inventory.getStackInSlot(i)
            if (itemStack != null && itemStack.item is ItemBow) {
                if (bestBow == null || getBowStrength(itemStack) > getBowStrength(bestBow)) {
                    bestBow = itemStack
                    slot = i
                }
            }
        }

        if (slot == -1 || bestBow == null) {
            return mc.thePlayer.inventory.currentItem
        }
        return slot
    }

    @JvmStatic
    fun getBestSword(entity: Entity): Int {
        var slot = -1
        var bestSword: ItemStack? = null

        for (i in 0 until 9) {
            val itemStack = mc.thePlayer.inventory.getStackInSlot(i)
            if (itemStack != null && itemStack.item is ItemSword) {
                if (bestSword == null || getSwordStrength(itemStack) > getSwordStrength(bestSword)) {
                    bestSword = itemStack
                    slot = i
                }
            }
        }

        if (slot == -1 || bestSword == null) {
            return mc.thePlayer.inventory.currentItem
        }
        return slot
    }

    @JvmStatic
    fun getBestAxe(entity: Entity): Int {
        var slot = -1
        var bestAxe: ItemStack? = null

        for (i in 0 until 9) {
            val itemStack = mc.thePlayer.inventory.getStackInSlot(i)
            if (itemStack != null && itemStack.item is ItemAxe) {
                if (bestAxe == null || getToolEfficiency(itemStack) > getToolEfficiency(bestAxe)) {
                    bestAxe = itemStack
                    slot = i
                }
            }
        }

        if (slot == -1 || bestAxe == null) {
            return mc.thePlayer.inventory.currentItem
        }
        return slot
    }

    @JvmStatic
    fun getBestPickaxe(entity: Entity): Int {
        var slot = -1
        var bestPickaxe: ItemStack? = null

        for (i in 0 until 9) {
            val itemStack = mc.thePlayer.inventory.getStackInSlot(i)
            if (itemStack != null && itemStack.item is ItemPickaxe) {
                if (bestPickaxe == null || getToolEfficiency(itemStack) > getToolEfficiency(bestPickaxe)) {
                    bestPickaxe = itemStack
                    slot = i
                }
            }
        }

        if (slot == -1 || bestPickaxe == null) {
            return mc.thePlayer.inventory.currentItem
        }
        return slot
    }

    @JvmStatic
    fun getBestBlock(entity: Entity): Int {
        var slot = -1
        var bestBlock: ItemBlock? = null

        var wool = false
        var planks = false
        var cobblestone = false

        for (i in 0 until 9) {
            val itemStack = mc.thePlayer.inventory.getStackInSlot(i)
            if (itemStack != null && itemStack.item is ItemBlock) {
                val itemBlock = itemStack.item as ItemBlock
                val block: Block = itemBlock.block

                if (bestBlock == null) {
                    bestBlock = itemBlock
                    slot = i
                }

                if (bestBlock != null) {
                    if (!wool && block == Blocks.wool) {
                        wool = true
                        bestBlock = itemBlock
                        slot = i
                        continue
                    }

                    if (!wool && !planks && block == Blocks.planks) {
                        planks = true
                        bestBlock = itemBlock
                        slot = i
                        continue
                    }

                    if (!wool && !planks && !cobblestone && block == Blocks.cobblestone) {
                        cobblestone = false
                        bestBlock = itemBlock
                        slot = i
                        continue
                    }
                }
            }
        }

        if (slot == -1 || bestBlock == null) {
            return mc.thePlayer.inventory.currentItem
        }
        return slot
    }

    private fun getBowStrength(stack: ItemStack): Float {
        if (stack.item is ItemBow) {
            val bow = stack.item as ItemBow
            val power = EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, stack) * 1.5f
            val flame = EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, stack) * 1.2f
            return bow.maxDamage + power + flame
        }
        return 0f
    }

    private fun getSwordStrength(stack: ItemStack): Float {
        if (stack.item is ItemSword) {
            val sword = stack.item as ItemSword
            val sharpness = EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, stack) * 1.25f
            val fireAspect = EnchantmentHelper.getEnchantmentLevel(Enchantment.fireAspect.effectId, stack) * 1.5f
            return sword.damageVsEntity + sharpness + fireAspect
        }
        return 0f
    }

    private fun getToolEfficiency(stack: ItemStack): Float {
        if (stack.item is ItemAxe) {
            val axe = stack.item as ItemAxe
            val efficiency = EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, stack) * 1.25f
            return axe.getStrVsBlock(stack, Blocks.planks) + efficiency
        }

        if (stack.item is ItemPickaxe) {
            val pickaxe = stack.item as ItemPickaxe
            val efficiency = EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, stack) * 1.25f
            return pickaxe.getStrVsBlock(stack, Blocks.stone) + efficiency
        }
        return 0f
    }

    @JvmStatic
    fun getPotionsFromInventory(inputPotion: Potion): Int {
        var count = 0
        for (i in 1 until 45) {
            if (mc.thePlayer.inventoryContainer.getSlot(i).hasStack) {
                val isStack = mc.thePlayer.inventoryContainer.getSlot(i).stack
                val item = isStack.item
                if (item is ItemPotion) {
                    val potion = item
                    val effects = potion.getEffects(isStack)
                    if (effects != null) {
                        for (effect in effects) {
                            if (effect.potionID == inputPotion.id) {
                                ++count
                            }
                        }
                    }
                }
            }
        }
        return count
    }

    @JvmStatic
    fun isSpectator(): Boolean {
        val networkplayerinfo =
            mc.netHandler.getPlayerInfo(mc.thePlayer.gameProfile.id)
        return networkplayerinfo != null && networkplayerinfo.gameType == WorldSettings.GameType.SPECTATOR
    }

    @JvmStatic
    fun isCreative(): Boolean {
        val networkplayerinfo: NetworkPlayerInfo? =
            mc.netHandler.getPlayerInfo(mc.thePlayer.gameProfile.id)
        return networkplayerinfo != null && networkplayerinfo.gameType == WorldSettings.GameType.CREATIVE
    }

    @JvmStatic
    fun isSurvival(): Boolean {
        val networkplayerinfo: NetworkPlayerInfo? =
            mc.netHandler.getPlayerInfo(mc.thePlayer.gameProfile.id)
        return networkplayerinfo != null && networkplayerinfo.gameType == WorldSettings.GameType.SURVIVAL
    }
}

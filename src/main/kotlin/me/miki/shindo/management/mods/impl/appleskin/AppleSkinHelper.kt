package me.miki.shindo.management.mods.impl.appleskin

import me.miki.shindo.injection.interfaces.IMixinItemFood
import net.minecraft.item.ItemFood
import net.minecraft.item.ItemStack
import net.minecraft.potion.Potion

object AppleSkinHelper {

    fun getFoodValues(stack: ItemStack): FoodValues {
        val food = stack.item as ItemFood?
        val hunger = food?.getHealAmount(stack) ?: 0
        val saturationModifier = food?.getSaturationModifier(stack) ?: 0f

        return FoodValues(hunger, saturationModifier)
    }

    fun isRottenFood(stack: ItemStack): Boolean {
        if (stack.item !is ItemFood) {
            return false
        }

        val food = stack.item as ItemFood

        if (food.getPotionEffect(stack) != null) {
            return Potion.potionTypes[(food as IMixinItemFood).potionID].isBadEffect
        }

        return false
    }
}

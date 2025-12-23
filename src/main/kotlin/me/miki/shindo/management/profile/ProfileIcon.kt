package me.miki.shindo.management.profile

import me.miki.shindo.utils.animation.simple.SimpleAnimation
import net.minecraft.util.ResourceLocation

enum class ProfileIcon(val id: Int, val icon: ResourceLocation) {
    COMMAND         (0, ResourceLocation("shindo/icons/command.png")),
    CRAFTING_TABLE  (1, ResourceLocation("shindo/icons/crafting_table.png")),
    FURNACE         (2, ResourceLocation("shindo/icons/furnace.png")),
    GRASS           (3, ResourceLocation("shindo/icons/grass.png")),
    HAY             (4, ResourceLocation("shindo/icons/hay.png")),
    PUMPKIN         (5, ResourceLocation("shindo/icons/pumpkin.png")),
    TNT             (6, ResourceLocation("shindo/icons/tnt.png"));

    val animation: SimpleAnimation = SimpleAnimation()

    companion object {
        @JvmStatic
        fun getIconById(id: Int): ProfileIcon = values().firstOrNull { it.id == id } ?: GRASS
    }
}

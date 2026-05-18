package me.miki.shindo.management.cosmetic.wing.impl

import me.miki.shindo.api.roles.Role
import me.miki.shindo.management.cosmetic.wing.WingCategory
import net.minecraft.util.ResourceLocation

class NormalWing(
    name: String,
    sample: ResourceLocation?,
    texture: ResourceLocation?,
    category: WingCategory,
    requiredRole: Role,
) : Wing(name, sample, texture, category, requiredRole)

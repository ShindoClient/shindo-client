package me.miki.shindo.management.cosmetic.cape.impl

import me.miki.shindo.api.roles.Role
import me.miki.shindo.management.cosmetic.cape.CapeCategory
import net.minecraft.util.ResourceLocation

class NormalCape(
    name: String,
    private val sample: ResourceLocation?,
    cape: ResourceLocation?,
    category: CapeCategory,
    requiredRole: Role
) : Cape(name, cape, category, requiredRole) {

    fun getSample(): ResourceLocation? = sample
}

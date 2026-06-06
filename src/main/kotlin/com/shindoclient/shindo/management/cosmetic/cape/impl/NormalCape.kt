package com.shindoclient.shindo.management.cosmetic.cape.impl

import com.shindoclient.shindo.api.roles.Role
import com.shindoclient.shindo.management.cosmetic.cape.CapeCategory
import net.minecraft.util.ResourceLocation

class NormalCape(
    name: String,
    private val sample: ResourceLocation?,
    cape: ResourceLocation?,
    category: CapeCategory,
    requiredRole: Role,
) : Cape(name, cape, category, requiredRole) {
    fun getSample(): ResourceLocation? = sample
}

package com.shindoclient.shindo.management.cosmetic.bandana.impl

import com.shindoclient.shindo.api.roles.Role
import com.shindoclient.shindo.management.cosmetic.bandana.BandanaCategory
import net.minecraft.util.ResourceLocation

class NormalBandana(
    name: String,
    sample: ResourceLocation?,
    texture: ResourceLocation?,
    category: BandanaCategory,
    requiredRole: Role,
) : Bandana(name, sample, texture, category, requiredRole)

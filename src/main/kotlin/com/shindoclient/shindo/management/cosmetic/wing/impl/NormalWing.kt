package com.shindoclient.shindo.management.cosmetic.wing.impl

import com.shindoclient.shindo.api.roles.Role
import com.shindoclient.shindo.management.cosmetic.wing.WingCategory
import net.minecraft.util.ResourceLocation

class NormalWing(
    name: String,
    sample: ResourceLocation?,
    texture: ResourceLocation?,
    category: WingCategory,
    requiredRole: Role,
) : Wing(name, sample, texture, category, requiredRole)

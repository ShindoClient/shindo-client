package me.miki.shindo.management.cosmetic.cape.impl

import me.miki.shindo.api.roles.Role
import me.miki.shindo.management.cosmetic.cape.CapeCategory
import net.minecraft.util.ResourceLocation
import java.io.File

class CustomCape(
    name: String,
    private val sample: File,
    cape: ResourceLocation,
    category: CapeCategory,
    requiredRole: Role
) : Cape(name, cape, category, requiredRole) {

    fun getSample(): File = sample
}

package com.shindoclient.shindo.management.cosmetic.cape.impl

import com.shindoclient.shindo.api.roles.Role
import com.shindoclient.shindo.management.cosmetic.cape.CapeCategory
import com.shindoclient.shindo.ui.animation.v2.value.SimpleAnimation
import net.minecraft.util.ResourceLocation

open class Cape(
    private val name: String,
    private val cape: ResourceLocation?,
    private val category: CapeCategory,
    private val requiredRole: Role,
) {
    private val animation = SimpleAnimation()

    fun getName(): String = name

    fun getCape(): ResourceLocation? = cape

    fun getCategory(): CapeCategory = category

    fun getRequiredRole(): Role = requiredRole

    fun getAnimation(): SimpleAnimation = animation
}

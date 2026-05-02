package me.miki.shindo.management.cosmetic.bandana.impl

import me.miki.shindo.api.roles.Role
import me.miki.shindo.management.cosmetic.bandana.BandanaCategory
import me.miki.shindo.ui.animation.v1.value.SimpleAnimation
import net.minecraft.util.ResourceLocation

open class Bandana(
    private val name: String,
    private val sample: ResourceLocation?,
    private val texture: ResourceLocation?,
    private val category: BandanaCategory,
    private val requiredRole: Role
) {
    private val animation = SimpleAnimation()

    fun getName(): String = name
    fun getSample(): ResourceLocation? = sample
    fun getTexture(): ResourceLocation? = texture
    fun getCategory(): BandanaCategory = category
    fun getRequiredRole(): Role = requiredRole
    fun getAnimation(): SimpleAnimation = animation
}

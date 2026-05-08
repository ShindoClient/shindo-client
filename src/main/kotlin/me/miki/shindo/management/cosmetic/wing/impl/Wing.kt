package me.miki.shindo.management.cosmetic.wing.impl

import me.miki.shindo.api.roles.Role
import me.miki.shindo.management.cosmetic.wing.WingCategory
import me.miki.shindo.ui.animation.v2.value.SimpleAnimation
import net.minecraft.util.ResourceLocation

open class Wing(
    private val name: String,
    private val sample: ResourceLocation?,
    private val texture: ResourceLocation?,
    private val category: WingCategory,
    private val requiredRole: Role
) {
    private val animation = SimpleAnimation()

    fun getName(): String = name
    fun getSample(): ResourceLocation? = sample
    fun getTexture(): ResourceLocation? = texture
    fun getCategory(): WingCategory = category
    fun getRequiredRole(): Role = requiredRole
    fun getAnimation(): SimpleAnimation = animation
}

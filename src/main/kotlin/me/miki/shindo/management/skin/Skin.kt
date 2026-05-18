package me.miki.shindo.management.skin

import me.miki.shindo.ui.animation.v2.value.SimpleAnimation
import net.minecraft.util.ResourceLocation

class Skin(
    val id: String,
    var name: String,
    val fileName: String,
    var type: SkinType,
    var favorite: Boolean,
    var texture: ResourceLocation?,
    var profileUuid: String?,
) {
    val animation: SimpleAnimation = SimpleAnimation()

    override fun equals(other: Any?): Boolean = this === other || (other is Skin && id == other.id)

    override fun hashCode(): Int = id.hashCode()
}

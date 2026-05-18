package me.miki.shindo.management.skin

import me.miki.shindo.ui.animation.v2.value.ColorAnimation
import me.miki.shindo.ui.animation.v2.value.SimpleAnimation

enum class SkinType(
    val id: Int,
    val names: String,
) {
    DEFAULT(0, "Default"),
    SLIM(1, "Slim"),
    ;

    val textColorAnimation: ColorAnimation = ColorAnimation()
    val backgroundAnimation: SimpleAnimation = SimpleAnimation()

    companion object {
        @JvmStatic
        fun getTypeById(id: Int): SkinType = values().find { it.id == id } ?: DEFAULT
    }
}

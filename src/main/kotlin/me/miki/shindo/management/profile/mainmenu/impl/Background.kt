package me.miki.shindo.management.profile.mainmenu.impl

import me.miki.shindo.ui.animation.v2.value.SimpleAnimation

open class Background(
    private val id: Int,
    private val name: String?,
) {
    val focusAnimation = SimpleAnimation()

    open fun getName(): String? = name ?: "null"

    open fun getId(): Int = id
}

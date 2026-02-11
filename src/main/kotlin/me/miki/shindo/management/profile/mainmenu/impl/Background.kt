package me.miki.shindo.management.profile.mainmenu.impl

import me.miki.shindo.ui.animation.value.SimpleAnimation

open class Background(private val id: Int, private val name: String?) {

    val focusAnimation = SimpleAnimation()

    open fun getName(): String? {
        return name ?: "null"
    }

    open fun getId(): Int {
        return id
    }
}

package me.miki.shindo.management.cosmetic.bandana

import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.ui.animation.v2.value.ColorAnimation
import me.miki.shindo.ui.animation.v2.value.SimpleAnimation

enum class BandanaCategory(
    private val names: String,
) {
    ALL(TranslateText.ALL.getText()),
    BASIC("Basic"),
    SPORTS("Sports"),
    ELITE("Elite"),
    ;

    private val backgroundAnimation = SimpleAnimation()
    private val textColorAnimation = ColorAnimation()

    fun getName(): String = names

    fun getBackgroundAnimation(): SimpleAnimation = backgroundAnimation

    fun getTextColorAnimation(): ColorAnimation = textColorAnimation
}

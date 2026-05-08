package me.miki.shindo.management.cosmetic.wing

import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.ui.animation.v2.value.ColorAnimation
import me.miki.shindo.ui.animation.v2.value.SimpleAnimation

enum class WingCategory(private val names: String) {
    ALL(TranslateText.ALL.getText()),
    CLASSIC("Classic"),
    FANTASY("Fantasy"),
    TECH("Tech");

    private val backgroundAnimation = SimpleAnimation()
    private val textColorAnimation = ColorAnimation()

    fun getName(): String = names
    fun getBackgroundAnimation(): SimpleAnimation = backgroundAnimation
    fun getTextColorAnimation(): ColorAnimation = textColorAnimation
}

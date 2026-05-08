package me.miki.shindo.management.addons

import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.ui.animation.v2.value.ColorAnimation
import me.miki.shindo.ui.animation.v2.value.SimpleAnimation

enum class AddonType(
    private val nameTranslate: TranslateText
) {
    ALL(TranslateText.ALL),
    RENDER(TranslateText.RENDER),
    QOL(TranslateText.QOL),
    OTHER(TranslateText.OTHER);

    val textColorAnimation: ColorAnimation = ColorAnimation()
    val backgroundAnimation: SimpleAnimation = SimpleAnimation()

    fun getName(): String = nameTranslate.getText()
}


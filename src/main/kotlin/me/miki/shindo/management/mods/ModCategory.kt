package me.miki.shindo.management.mods

import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.ui.animation.v1.value.ColorAnimation
import me.miki.shindo.ui.animation.v1.value.SimpleAnimation

enum class ModCategory(private val nameTranslate: TranslateText) {
    ALL(TranslateText.ALL),
    PLAYER(TranslateText.PLAYER),
    RENDER(TranslateText.RENDER),
    HUD(TranslateText.HUD),
    WORLD(TranslateText.WORLD),
    OTHER(TranslateText.OTHER);

    val textColorAnimation = ColorAnimation()
    val backgroundAnimation = SimpleAnimation()

    fun getName(): String {
        return nameTranslate.getText()
    }
}

package me.miki.shindo.management.profile

import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.ui.animation.value.ColorAnimation
import me.miki.shindo.ui.animation.value.SimpleAnimation

enum class ProfileType(val id: Int, private val nameTranslate: TranslateText) {
    ALL(0, TranslateText.ALL),
    FAVORITE(1, TranslateText.FAVORITE);

    val textColorAnimation: ColorAnimation = ColorAnimation()
    val backgroundAnimation: SimpleAnimation = SimpleAnimation()

    fun getName(): String = nameTranslate.getText()
    fun getKey(): String = nameTranslate.getKey()

    companion object {
        @JvmStatic
        fun getTypeById(id: Int): ProfileType = values().firstOrNull { it.id == id } ?: ALL
    }
}

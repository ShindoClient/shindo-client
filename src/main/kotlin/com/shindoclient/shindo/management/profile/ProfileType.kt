package com.shindoclient.shindo.management.profile

import com.shindoclient.shindo.management.language.TranslateText
import com.shindoclient.shindo.ui.animation.v2.value.ColorAnimation
import com.shindoclient.shindo.ui.animation.v2.value.SimpleAnimation

enum class ProfileType(
    val id: Int,
    private val nameTranslate: TranslateText,
) {
    ALL(0, TranslateText.ALL),
    FAVORITE(1, TranslateText.FAVORITE),
    ;

    val textColorAnimation: ColorAnimation = ColorAnimation()
    val backgroundAnimation: SimpleAnimation = SimpleAnimation()

    fun getName(): String = nameTranslate.getText()

    fun getKey(): String = nameTranslate.getKey()

    companion object {
        @JvmStatic
        fun getTypeById(id: Int): ProfileType = values().firstOrNull { it.id == id } ?: ALL
    }
}

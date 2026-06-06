package com.shindoclient.shindo.management.mods

import com.shindoclient.shindo.management.language.TranslateText
import com.shindoclient.shindo.ui.animation.v2.value.ColorAnimation
import com.shindoclient.shindo.ui.animation.v2.value.SimpleAnimation

@Suppress("UNUSED")
enum class ModCategory(
    private val nameTranslate: TranslateText,
) {
    ALL(TranslateText.ALL),
    PLAYER(TranslateText.PLAYER),
    RENDER(TranslateText.RENDER),
    HUD(TranslateText.HUD),
    WORLD(TranslateText.WORLD),
    OTHER(TranslateText.OTHER),
    ;

    val textColorAnimation = ColorAnimation()
    val backgroundAnimation = SimpleAnimation()

    fun getName(): String = nameTranslate.getText()
}

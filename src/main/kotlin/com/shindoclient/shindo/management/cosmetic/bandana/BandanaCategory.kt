package com.shindoclient.shindo.management.cosmetic.bandana

import com.shindoclient.shindo.management.language.TranslateText
import com.shindoclient.shindo.ui.animation.v2.value.ColorAnimation
import com.shindoclient.shindo.ui.animation.v2.value.SimpleAnimation

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

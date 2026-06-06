package com.shindoclient.shindo.management.cosmetic.cape

import com.shindoclient.shindo.management.language.TranslateText
import com.shindoclient.shindo.ui.animation.v2.value.ColorAnimation
import com.shindoclient.shindo.ui.animation.v2.value.SimpleAnimation

enum class CapeCategory(
    private val names: String,
) {
    ALL(TranslateText.ALL.getText()),
    MINECON("Minecon"),
    FLAG("Flags"),
    CARTOON("Cartoon"),
    CUSTOM("Custom"),
    ;

    private val backgroundAnimation = SimpleAnimation()
    private val textColorAnimation = ColorAnimation()

    fun getName(): String = names

    fun getBackgroundAnimation(): SimpleAnimation = backgroundAnimation

    fun getTextColorAnimation(): ColorAnimation = textColorAnimation
}

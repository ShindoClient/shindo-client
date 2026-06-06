package com.shindoclient.shindo.management.addons

import com.shindoclient.shindo.management.language.TranslateText
import com.shindoclient.shindo.ui.animation.v2.value.ColorAnimation
import com.shindoclient.shindo.ui.animation.v2.value.SimpleAnimation

enum class AddonType(
    private val nameTranslate: TranslateText,
) {
    ALL(TranslateText.ALL),
    RENDER(TranslateText.RENDER),
    QOL(TranslateText.QOL),
    OTHER(TranslateText.OTHER),
    ;

    val textColorAnimation: ColorAnimation = ColorAnimation()
    val backgroundAnimation: SimpleAnimation = SimpleAnimation()

    fun getName(): String = nameTranslate.getText()
}

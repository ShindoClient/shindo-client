package com.shindoclient.shindo.management.profile.mainmenu.impl

import com.shindoclient.shindo.management.language.TranslateText

class PanoramaBackground(
    id: Int,
    private val nameTranslate: TranslateText,
) : Background(id, nameTranslate.getText()) {
    override fun getName(): String = nameTranslate.getText()

    fun getNameKey(): String = nameTranslate.getKey()
}

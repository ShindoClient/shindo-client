package me.miki.shindo.management.profile.mainmenu.impl

import me.miki.shindo.management.language.TranslateText


class PanoramaBackground(id: Int, private val nameTranslate: TranslateText) : Background(id, nameTranslate.getText()) {

    override fun getName(): String {
        return nameTranslate.getText()
    }

    fun getNameKey(): String {
        return nameTranslate.getKey()
    }

}
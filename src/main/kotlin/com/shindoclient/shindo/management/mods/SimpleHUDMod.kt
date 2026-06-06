package com.shindoclient.shindo.management.mods

import com.shindoclient.shindo.Shindo
import com.shindoclient.shindo.management.language.TranslateText
import com.shindoclient.shindo.management.nanovg.font.Fonts

open class SimpleHUDMod : HUDMod {
    constructor(nameTranslate: TranslateText, descriptionText: TranslateText, icon: String) : super(
        nameTranslate,
        descriptionText,
        icon,
    )

    constructor(nameTranslate: TranslateText, descriptionText: TranslateText, icon: String, alias: String) : super(
        nameTranslate,
        descriptionText,
        icon,
        alias,
    )

    fun draw() {
        val instance = Shindo.getInstance()
        instance.nanoVGManager
        val icon = getIcon()
        val hasIcon = icon != null
        val addX = if (hasIcon) (getTextWidth(icon, 9.5f, Fonts.LUCIDE) + 4) else 0f

        val text = getText()
        if (text != null) {
            val bgWidth = getTextWidth(getText()!!, 9f, getHudFont(1)) + 10 + addX

            this.drawBackground(bgWidth, 18f)
            this.drawText(getText()!!, 5.5f + addX, 5.5f, 9f, getHudFont(1))

            if (hasIcon) {
                this.drawText(getIcon()!!, 5.5f, 4f, 10.4f, Fonts.LUCIDE)
            }

            setWidth(bgWidth.toInt())
            setHeight(18)
        }
    }

    open fun getText(): String? = null
}

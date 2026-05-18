package me.miki.shindo.management.profile.mainmenu.impl

import me.miki.shindo.management.language.TranslateText
import net.minecraft.util.ResourceLocation

class DefaultBackground(
    id: Int,
    private val nameTranslate: TranslateText,
    private val image: ResourceLocation?,
) : Background(id, nameTranslate.getText()) {
    override fun getName(): String = nameTranslate.getText()

    fun getNameKey(): String = nameTranslate.getKey()

    fun getImage(): ResourceLocation? = image
}

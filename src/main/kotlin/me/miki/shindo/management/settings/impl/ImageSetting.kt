package me.miki.shindo.management.settings.impl

import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.settings.Setting
import me.miki.shindo.management.settings.config.ConfigOwner
import java.io.File

open class ImageSetting : Setting {
    private var image: File? = null

    constructor(nameTranslate: TranslateText, parent: ConfigOwner) : super(nameTranslate, parent)

    constructor(name: String, parent: ConfigOwner) : super(name, parent)

    override fun reset() {
        image = null
    }

    fun getImage(): File? = image

    open fun setImage(image: File?) {
        this.image = image
    }
}

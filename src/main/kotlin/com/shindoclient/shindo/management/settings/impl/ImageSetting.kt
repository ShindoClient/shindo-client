package com.shindoclient.shindo.management.settings.impl

import com.shindoclient.shindo.management.language.TranslateText
import com.shindoclient.shindo.management.settings.Setting
import com.shindoclient.shindo.management.settings.config.ConfigOwner
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

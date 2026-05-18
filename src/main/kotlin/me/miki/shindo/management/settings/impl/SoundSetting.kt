package me.miki.shindo.management.settings.impl

import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.settings.Setting
import me.miki.shindo.management.settings.config.ConfigOwner
import java.io.File

open class SoundSetting : Setting {
    private var sound: File? = null

    constructor(nameTranslate: TranslateText, parent: ConfigOwner) : super(nameTranslate, parent)

    constructor(name: String, parent: ConfigOwner) : super(name, parent)

    override fun reset() {
        sound = null
    }

    fun getSound(): File? = sound

    open fun setSound(sound: File?) {
        this.sound = sound
    }
}

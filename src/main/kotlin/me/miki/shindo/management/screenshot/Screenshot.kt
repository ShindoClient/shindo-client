package me.miki.shindo.management.screenshot

import me.miki.shindo.ui.animation.v1.value.SimpleAnimation
import java.io.File

class Screenshot(image: File) {

    private val selectAnimation = SimpleAnimation()
    private val name: String = image.name.replace(".png", "")
    private val image: File = image

    fun getSelectAnimation(): SimpleAnimation = selectAnimation
    fun getName(): String = name
    fun getImage(): File = image
}

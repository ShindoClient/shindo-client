package com.shindoclient.shindo.management.profile.mainmenu.impl

import com.shindoclient.shindo.ui.animation.v2.value.SimpleAnimation
import java.io.File

class CustomBackground(
    id: Int,
    name: String,
    private val image: File,
) : Background(id, name) {
    private val trashAnimation = SimpleAnimation()

    fun getImage(): File = image

    fun getTrashAnimation(): SimpleAnimation = trashAnimation
}

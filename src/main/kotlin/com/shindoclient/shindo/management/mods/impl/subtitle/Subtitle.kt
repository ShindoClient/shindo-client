package com.shindoclient.shindo.management.mods.impl.subtitle

import com.shindoclient.shindo.ui.animation.v2.value.SimpleAnimation
import net.minecraft.client.Minecraft
import net.minecraft.util.Vec3

class Subtitle(
    val string: String?,
    var location: Vec3?,
) {
    var animation: SimpleAnimation = SimpleAnimation(0.0f)
    var startTime: Long
        private set
    var isRemove: Boolean = false
    var isDone: Boolean = false

    init {
        this.startTime = Minecraft.getSystemTime()
    }

    fun refresh(locationIn: Vec3?) {
        this.location = locationIn
        this.startTime = Minecraft.getSystemTime()
    }
}

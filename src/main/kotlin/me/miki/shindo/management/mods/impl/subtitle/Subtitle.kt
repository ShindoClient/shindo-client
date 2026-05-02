package me.miki.shindo.management.mods.impl.subtitle

import me.miki.shindo.ui.animation.v1.value.SimpleAnimation
import net.minecraft.client.Minecraft
import net.minecraft.util.Vec3

class Subtitle(val string: String?, var location: Vec3?) {
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
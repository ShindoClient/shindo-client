package me.miki.shindo.management.event.impl

import me.miki.shindo.management.event.Event
import net.minecraft.client.shader.ShaderGroup

class EventShader : Event() {
    val groups: MutableList<ShaderGroup> = ArrayList()
}


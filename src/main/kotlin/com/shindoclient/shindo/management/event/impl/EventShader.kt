package com.shindoclient.shindo.management.event.impl

import com.shindoclient.shindo.management.event.Event
import net.minecraft.client.shader.ShaderGroup

class EventShader : Event() {
    val groups: MutableList<ShaderGroup> = ArrayList()
}

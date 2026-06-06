package com.shindoclient.shindo.management.event.impl

import com.shindoclient.shindo.management.event.Event
import net.minecraft.client.renderer.chunk.RenderChunk

class EventPreRenderChunk(
    private val renderChunk: RenderChunk,
) : Event() {
    fun getRenderChunk(): RenderChunk = renderChunk
}

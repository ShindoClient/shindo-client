package me.miki.shindo.management.event.impl

import me.miki.shindo.management.event.Event
import net.minecraft.client.renderer.chunk.RenderChunk

class EventPreRenderChunk(
    private val _renderChunk: RenderChunk,
) : Event() {
    fun getRenderChunk(): RenderChunk = _renderChunk
}

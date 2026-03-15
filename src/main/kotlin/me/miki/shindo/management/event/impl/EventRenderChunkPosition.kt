package me.miki.shindo.management.event.impl

import me.miki.shindo.management.event.Event
import net.minecraft.client.renderer.chunk.RenderChunk
import net.minecraft.util.BlockPos

class EventRenderChunkPosition(
    private val _renderChunk: RenderChunk,
    private var _blockPos: BlockPos
) : Event() {
    fun getBlockPos(): BlockPos = _blockPos
    fun setBlockPos(blockPos: Any) {
        _blockPos = blockPos as BlockPos
    }

    fun getRenderChunk(): RenderChunk = _renderChunk
}


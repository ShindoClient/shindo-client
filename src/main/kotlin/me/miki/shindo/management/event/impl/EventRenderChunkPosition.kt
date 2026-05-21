package me.miki.shindo.management.event.impl

import me.miki.shindo.management.event.Event
import net.minecraft.client.renderer.chunk.RenderChunk
import net.minecraft.util.BlockPos

class EventRenderChunkPosition(
    private val renderChunk: RenderChunk,
    private var blockPos: BlockPos,
) : Event() {
    fun getBlockPos(): BlockPos = blockPos

    fun setBlockPos(blockPos: BlockPos) {
        this.blockPos = blockPos
    }

    fun getRenderChunk(): RenderChunk = renderChunk
}

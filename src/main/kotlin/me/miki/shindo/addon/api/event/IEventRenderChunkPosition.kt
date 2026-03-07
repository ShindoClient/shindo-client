package me.miki.shindo.addon.api.event

/** Pos chunk. */
interface IEventRenderChunkPosition : IEvent {
    fun getRenderChunk(): Any
    fun getBlockPos(): Any
    fun setBlockPos(blockPos: Any)
}

package me.miki.shindo.management.event.impl;

import me.miki.shindo.management.event.Event;
import net.minecraft.client.renderer.chunk.RenderChunk;

public class EventPreRenderChunk extends Event {

    private final RenderChunk renderChunk;

    public EventPreRenderChunk(RenderChunk renderChunk) {
        this.renderChunk = renderChunk;
    }

    public RenderChunk getRenderChunk() {
        return renderChunk;
    }
}
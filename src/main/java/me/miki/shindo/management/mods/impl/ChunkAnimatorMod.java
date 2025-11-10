package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventPreRenderChunk;
import me.miki.shindo.management.event.impl.EventRenderChunkPosition;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.chunk.RenderChunk;

import java.util.Map;
import java.util.WeakHashMap;

import me.miki.shindo.management.settings.config.Property;
import me.miki.shindo.management.settings.config.PropertyType;
public class ChunkAnimatorMod extends Mod {

    private final Map<RenderChunk, Long> chunks = new WeakHashMap<>();

    @Property(type = PropertyType.NUMBER, translate = TranslateText.DURATION, min = 0, max = 5, current = 1, step = 1)
    private int duration = 1;

    public ChunkAnimatorMod() {
        super(TranslateText.CHUNK_ANIMATOR, TranslateText.CHUNK_ANIMATOR_DESCRIPTION, ModCategory.RENDER);
    }

    @EventTarget
    public void preRenderChunk(EventPreRenderChunk event) {
        if (chunks.containsKey(event.getRenderChunk())) {

            long time = chunks.get(event.getRenderChunk());
            long now = System.currentTimeMillis();

            if (time == -1L) {
                chunks.put(event.getRenderChunk(), now);
                time = now;
            }

            long passedTime = now - time;

            if (passedTime < (int) (duration * 1000)) {
                int chunkY = event.getRenderChunk().getPosition().getY();
                GlStateManager.translate(0, -chunkY + this.easeOut(passedTime, 0, chunkY, (int) (duration * 1000)), 0);
            }
        }
    }

    @EventTarget
    public void setPosition(EventRenderChunkPosition event) {
        if (mc.thePlayer != null) {
            chunks.put(event.getRenderChunk(), -1L);
        }
    }

    private float easeOut(float t, float b, float c, float d) {
        return c * (float) Math.sin(t / d * (Math.PI / 2)) + b;
    }
}

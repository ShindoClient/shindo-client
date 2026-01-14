package me.miki.shindo.management.mods.impl

import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventPreRenderChunk
import me.miki.shindo.management.event.impl.EventRenderChunkPosition
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyType
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.chunk.RenderChunk
import java.util.*
import kotlin.math.sin

class ChunkAnimatorMod : Mod(
    TranslateText.CHUNK_ANIMATOR,
    TranslateText.CHUNK_ANIMATOR_DESCRIPTION,
    ModCategory.RENDER,
    LegacyIcon.MOD_CHUNK_ANIMATOR
) {
    private val chunks: MutableMap<RenderChunk?, Long?> = WeakHashMap<RenderChunk?, Long?>()

    @Property(type = PropertyType.NUMBER, translate = TranslateText.DURATION, min = 0, max = 5, current = 1, step = 1)
    private val duration = 1

    @EventTarget
    fun preRenderChunk(event: EventPreRenderChunk) {
        if (chunks.containsKey(event.getRenderChunk())) {
            var time: Long = chunks.get(event.getRenderChunk())!!
            val now = System.currentTimeMillis()

            if (time == -1L) {
                chunks.put(event.getRenderChunk(), now)
                time = now
            }

            val passedTime = now - time

            if (passedTime < (duration * 1000)) {
                val chunkY = event.getRenderChunk().getPosition().getY()
                GlStateManager.translate(
                    0f,
                    -chunkY + this.easeOut(passedTime.toFloat(), 0f, chunkY.toFloat(), (duration * 1000).toFloat()),
                    0f
                )
            }
        }
    }

    @EventTarget
    fun setPosition(event: EventRenderChunkPosition) {
        if (mc.thePlayer != null) {
            chunks.put(event.getRenderChunk(), -1L)
        }
    }

    private fun easeOut(t: Float, b: Float, c: Float, d: Float): Float {
        return c * sin(t / d * (Math.PI / 2)).toFloat() + b
    }
}

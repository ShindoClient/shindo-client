package me.miki.shindo.management.event.impl

import me.miki.client_api.event.*
import me.miki.shindo.management.event.Event
import net.minecraft.client.entity.AbstractClientPlayer
import net.minecraft.client.network.NetworkPlayerInfo
import net.minecraft.client.renderer.chunk.RenderChunk
import net.minecraft.client.renderer.entity.RenderTNTPrimed
import net.minecraft.client.renderer.entity.RendererLivingEntity
import net.minecraft.client.shader.ShaderGroup
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityTNTPrimed
import net.minecraft.network.Packet
import net.minecraft.scoreboard.ScoreObjective
import net.minecraft.util.BlockPos
import net.minecraft.util.IChatComponent
import net.minecraft.util.MovingObjectPosition
import net.minecraft.util.ResourceLocation
import java.util.ArrayList

class EventText(private val _text: String) : Event(), IEventText {
    private var _outputText: String = _text

    override fun getText(): String = _text
    override fun getOutputText(): String = _outputText
    override fun setOutputText(text: String) {
        _outputText = text
    }

    fun replace(src: String, target: String): String {
        _outputText = _text.replace(src, target)
        return _outputText
    }
}


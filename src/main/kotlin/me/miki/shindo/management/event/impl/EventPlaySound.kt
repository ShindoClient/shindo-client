package me.miki.shindo.management.event.impl

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

class EventPlaySound(
    private val _soundName: String,
    private var _volume: Float,
    private var _pitch: Float,
    private var _originalVolume: Float,
    private var _originalPitch: Float
) : Event(){
    fun getVolume(): Float = _volume
    fun setVolume(volume: Float) {
        _volume = volume
    }

    fun getPitch(): Float = _pitch
    fun setPitch(pitch: Float) {
        _pitch = pitch
    }

    fun getOriginalVolume(): Float = _originalVolume
    fun setOriginalVolume(originalVolume: Float) {
        _originalVolume = originalVolume
    }

    fun getOriginalPitch(): Float = _originalPitch
    fun setOriginalPitch(originalPitch: Float) {
        _originalPitch = originalPitch
    }

    fun getSoundName(): String = _soundName
}


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

class EventCameraRotation(
    @JvmField var yaw: Float,
    @JvmField var pitch: Float,
    @JvmField var roll: Float,
    @JvmField var thirdPersonDistance: Float
) : Event(){
    fun getYaw(): Float = yaw
    fun setYaw(yaw: Float) {
        this.yaw = yaw
    }

    fun getPitch(): Float = pitch
    fun setPitch(pitch: Float) {
        this.pitch = pitch
    }

    fun getRoll(): Float = roll
    fun setRoll(roll: Float) {
        this.roll = roll
    }

    fun getThirdPersonDistance(): Float = thirdPersonDistance
    fun setThirdPersonDistance(thirdPersonDistance: Float) {
        this.thirdPersonDistance = thirdPersonDistance
    }
}


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

class EventRenderHitbox(
    private val _entity: Entity,
    private val _x: Double,
    private val _y: Double,
    private val _z: Double,
    private val _entityYaw: Float,
    private val _partialTicks: Float
) : Event(){
    fun getEntity(): Entity = _entity
    fun getX(): Double = _x
    fun getY(): Double = _y
    fun getZ(): Double = _z
    fun getEntityYaw(): Float = _entityYaw
    fun getPartialTicks(): Float = _partialTicks
}


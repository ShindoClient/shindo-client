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

class EventRenderTNT(
    private val _tntRenderer: RenderTNTPrimed,
    private val _entity: EntityTNTPrimed,
    private val _x: Double,
    private val _y: Double,
    private val _z: Double,
    private val _entityYaw: Float,
    private val _partialTicks: Float
) : Event(), IEventRenderTNT {
    override fun getTntRenderer(): RenderTNTPrimed = _tntRenderer
    override fun getEntity(): EntityTNTPrimed = _entity
    override fun getX(): Double = _x
    override fun getY(): Double = _y
    override fun getZ(): Double = _z
    override fun getEntityYaw(): Float = _entityYaw
    override fun getPartialTicks(): Float = _partialTicks
}


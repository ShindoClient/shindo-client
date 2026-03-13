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

class EventDamageEntity(private val _entity: Entity) : Event(){
    fun getEntity(): Entity = _entity
}


package me.miki.shindo.management.mods.impl

import me.miki.shindo.injection.interfaces.IMixinRenderManager
import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventRenderTick
import me.miki.shindo.management.event.impl.EventRendererLivingEntity
import me.miki.shindo.management.event.impl.EventTick
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyType
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.entity.RenderManager
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.scoreboard.Team.EnumVisible
import net.minecraft.util.AxisAlignedBB
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL33
import org.lwjgl.opengl.GLContext
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class EntityCullingMod :
    Mod(
        TranslateText.ENTITY_CULLING,
        TranslateText.ENTITY_CULLING_DESCRIPTIONN,
        ModCategory.OTHER,
        LegacyIcon.MOD_ENTITY_CULLING,
    ) {
    private val renderManager: RenderManager = mc.renderManager
    private val queries: ConcurrentHashMap<UUID?, OcclusionQuery> = ConcurrentHashMap<UUID?, OcclusionQuery>()
    private val SUPPORT_NEW_GL = GLContext.getCapabilities().OpenGL33

    @Property(
        type = PropertyType.NUMBER,
        translate = TranslateText.DELAY,
        min = 1.0,
        max = 3.0,
        current = 2.0,
        step = 1.0,
    )
    private val delaySetting = 2

    @Property(
        type = PropertyType.NUMBER,
        translate = TranslateText.DISTANCE,
        min = 10.0,
        max = 150.0,
        current = 45.0,
        step = 1.0,
    )
    private val distanceSetting = 45

    private var destroyTimer = 0

    @EventTarget
    fun onRendererLivingEntity(event: EventRendererLivingEntity) {
        if (!shouldPerformCulling) {
            return
        }

        val entity = event.getEntity() as EntityLivingBase

        val armorstand = entity is EntityArmorStand

        if (entity === mc.thePlayer ||
            entity.worldObj !== mc.thePlayer.worldObj ||
            (armorstand && (entity as EntityArmorStand).hasMarker()) ||
            (
                entity.isInvisibleToPlayer(
                    mc.thePlayer,
                )
            )
        ) {
            return
        }

        if (checkEntity(entity)) {
            event.setCancelled(true)

            if (!canRenderName(entity)) {
                return
            }

            val x = event.getX()
            val y = event.getY()
            val z = event.getZ()
            val renderer = event.getRenderer()

            renderer.renderName(entity, x, y, z)
        }

        if ((entity is EntityArmorStand) || (entity.isInvisible && entity is EntityPlayer)) {
            event.setCancelled(true)
        }

        if (shouldPerformCulling) {
            val entityDistance = entity.getDistanceToEntity(mc.thePlayer)

            if (entityDistance > distanceSetting) {
                event.setCancelled(true)
            }
        }
    }

    @EventTarget
    fun onRenderTick(event: EventRenderTick?) {
        mc.addScheduledTask(Runnable { this.check() })
    }

    @EventTarget
    fun onTick(event: EventTick?) {
        if (this.destroyTimer++ < 120) {
            return
        }

        this.destroyTimer = 0
        val theWorld = mc.theWorld ?: return

        val remove: MutableList<UUID> = ArrayList<UUID>()
        val loaded: MutableSet<UUID?> = HashSet<UUID?>()
        for (entity in theWorld.loadedEntityList) {
            loaded.add(entity.uniqueID)
        }

        for (value in queries.values) {
            if (loaded.contains(value.uuid)) {
                continue
            }

            remove.add(value.uuid!!)
            if (value.nextQuery != 0) {
                GL15.glDeleteQueries(value.nextQuery)
            }
        }

        for (uuid in remove) {
            queries.remove(uuid)
        }
    }

    fun canRenderName(entity: EntityLivingBase): Boolean {
        val player = mc.thePlayer
        if (entity is EntityPlayer && entity !== player) {
            val otherEntityTeam = entity.team
            val playerTeam = player.team

            if (otherEntityTeam != null) {
                val teamVisibilityRule = otherEntityTeam.nameTagVisibility

                when (teamVisibilityRule) {
                    EnumVisible.NEVER -> {
                        return false
                    }

                    EnumVisible.HIDE_FOR_OTHER_TEAMS -> {
                        return playerTeam == null ||
                            otherEntityTeam.isSameTeam(
                                playerTeam,
                            )
                    }

                    EnumVisible.HIDE_FOR_OWN_TEAM -> {
                        return playerTeam == null || !otherEntityTeam.isSameTeam(playerTeam)
                    }

                    EnumVisible.ALWAYS -> {
                        return true
                    }

                    else -> {
                        return true
                    }
                }
            }
        }

        return Minecraft.isGuiEnabled() &&
            entity !== mc.renderManager.livingPlayer &&
            (
                (entity is EntityArmorStand) ||
                    !entity.isInvisibleToPlayer(
                        player,
                    )
            ) &&
            entity.riddenByEntity == null
    }

    fun renderItem(stack: Entity): Boolean = shouldPerformCulling && stack.worldObj === mc.thePlayer.worldObj && checkEntity(stack)

    private fun check() {
        var delay: Long = 0

        when (delaySetting - 1) {
            0 -> {
                delay = 10
            }

            1 -> {
                delay = 25
            }

            2 -> {
                delay = 50
            }

            else -> {}
        }
        val nanoTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime())
        for (query in queries.values) {
            if (query.nextQuery != 0) {
                val queryObject = GL15.glGetQueryObjecti(query.nextQuery, GL15.GL_QUERY_RESULT_AVAILABLE).toLong()
                if (queryObject != 0L) {
                    query.occluded = GL15.glGetQueryObjecti(query.nextQuery, GL15.GL_QUERY_RESULT) == 0
                    GL15.glDeleteQueries(query.nextQuery)
                    query.nextQuery = 0
                }
            }
            if (query.nextQuery == 0 && nanoTime - query.executionTime > delay) {
                query.executionTime = nanoTime
                query.refresh = true
            }
        }
    }

    private fun checkEntity(entity: Entity): Boolean {
        val query = queries.computeIfAbsent(entity.uniqueID) { uuid: UUID? -> OcclusionQuery(uuid) }

        if (query.refresh) {
            query.nextQuery = Companion.query
            query.refresh = false
            val mode = if (SUPPORT_NEW_GL) GL33.GL_ANY_SAMPLES_PASSED else GL15.GL_SAMPLES_PASSED
            GL15.glBeginQuery(mode, query.nextQuery)
            drawSelectionBoundingBox(
                entity.entityBoundingBox
                    .expand(.2, .2, .2)
                    .offset(
                        -(renderManager as IMixinRenderManager).getRenderPosX(),
                        -(renderManager as IMixinRenderManager).getRenderPosY(),
                        -(renderManager as IMixinRenderManager).getRenderPosZ(),
                    ),
            )
            GL15.glEndQuery(mode)
        }

        return query.occluded
    }

    private class OcclusionQuery(
        val uuid: UUID?,
    ) {
        var nextQuery: Int = 0
        var refresh: Boolean = true
        var occluded: Boolean = false
        var executionTime: Long = 0
    }

    companion object {
        @JvmField
        var shouldPerformCulling: Boolean = false

        fun drawSelectionBoundingBox(b: AxisAlignedBB) {
            GlStateManager.disableAlpha()
            GlStateManager.disableCull()
            GlStateManager.depthMask(false)
            GlStateManager.colorMask(false, false, false, false)
            val tessellator = Tessellator.getInstance()
            val worldrenderer = tessellator.worldRenderer
            worldrenderer.begin(GL11.GL_QUAD_STRIP, DefaultVertexFormats.POSITION)
            worldrenderer.pos(b.maxX, b.maxY, b.maxZ).endVertex()
            worldrenderer.pos(b.maxX, b.maxY, b.minZ).endVertex()
            worldrenderer.pos(b.minX, b.maxY, b.maxZ).endVertex()
            worldrenderer.pos(b.minX, b.maxY, b.minZ).endVertex()
            worldrenderer.pos(b.minX, b.minY, b.maxZ).endVertex()
            worldrenderer.pos(b.minX, b.minY, b.minZ).endVertex()
            worldrenderer.pos(b.minX, b.maxY, b.minZ).endVertex()
            worldrenderer.pos(b.minX, b.minY, b.minZ).endVertex()
            worldrenderer.pos(b.maxX, b.maxY, b.minZ).endVertex()
            worldrenderer.pos(b.maxX, b.minY, b.minZ).endVertex()
            worldrenderer.pos(b.maxX, b.maxY, b.maxZ).endVertex()
            worldrenderer.pos(b.maxX, b.minY, b.maxZ).endVertex()
            worldrenderer.pos(b.minX, b.maxY, b.maxZ).endVertex()
            worldrenderer.pos(b.minX, b.minY, b.maxZ).endVertex()
            worldrenderer.pos(b.minX, b.minY, b.maxZ).endVertex()
            worldrenderer.pos(b.maxX, b.minY, b.maxZ).endVertex()
            worldrenderer.pos(b.minX, b.minY, b.minZ).endVertex()
            worldrenderer.pos(b.maxX, b.minY, b.minZ).endVertex()
            tessellator.draw()
            GlStateManager.depthMask(true)
            GlStateManager.colorMask(true, true, true, true)
            GlStateManager.enableAlpha()
        }

        private val query: Int
            get() {
                try {
                    return GL15.glGenQueries()
                } catch (throwable: Throwable) {
                    return 0
                }
            }
    }
}

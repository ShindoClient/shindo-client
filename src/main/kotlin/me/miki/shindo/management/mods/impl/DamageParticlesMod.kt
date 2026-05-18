package me.miki.shindo.management.mods.impl

import me.miki.shindo.injection.interfaces.IMixinRenderManager
import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventLivingUpdate
import me.miki.shindo.management.event.impl.EventLoadWorld
import me.miki.shindo.management.event.impl.EventRender3D
import me.miki.shindo.management.event.impl.EventTick
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.utils.LocationUtils
import me.miki.shindo.utils.MathUtils.roundToPlace
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.EnumChatFormatting
import org.lwjgl.opengl.GL11
import java.util.*
import java.util.function.Consumer

class DamageParticlesMod :
    Mod(
        TranslateText.DAMAGE_PARTICLES,
        TranslateText.DAMAGE_PARTICLES_DESCRIPTION,
        ModCategory.RENDER,
        LegacyIcon.MOD_DAMAGE_PARTICLES,
    ) {
    private val healthMap = HashMap<EntityLivingBase?, Float?>()
    private val particles = ArrayList<Particle>()
    private var canRemove = false
    private var removeParticle: Particle? = null

    @EventTarget
    fun onTick(event: EventTick?) {
        if (canRemove) {
            particles.remove(removeParticle)
        }

        particles.forEach(
            Consumer { particle: Particle? ->
                particle!!.ticks++
                if (particle.ticks <= 10) {
                    particle.location.setY(particle.location.y + particle.ticks * 0.005)
                }
                if (particle.ticks > 20) {
                    canRemove = true
                    removeParticle = particle
                }
            },
        )
    }

    @EventTarget
    fun onLivingUpdate(event: EventLivingUpdate) {
        val entity = event.getEntity()

        if (entity === this.mc.thePlayer) {
            return
        }

        if (!healthMap.containsKey(entity)) {
            healthMap[entity] = entity.health
        }

        val before: Float = healthMap[entity]!!
        val after = entity.health

        if (before != after) {
            val text: String =
                if ((before - after) < 0) {
                    EnumChatFormatting.GREEN.toString() + "" + roundToPlace((before - after) * -1, 1)
                } else {
                    EnumChatFormatting.YELLOW.toString() + "" + roundToPlace((before - after), 1)
                }

            val location = LocationUtils(entity)

            location.setY(
                entity.entityBoundingBox.minY + ((entity.entityBoundingBox.maxY - entity.entityBoundingBox.minY) / 2),
            )

            location.setX((location.x - 0.5) + (Random(System.currentTimeMillis()).nextInt(5) * 0.1))
            location.setZ((location.z - 0.5) + (Random(System.currentTimeMillis() + 1).nextInt(5) * 0.1))

            particles.add(Particle(location, text))

            healthMap.remove(entity)
            healthMap[entity] = entity.health
        }
    }

    @EventTarget
    fun onRender3D(event: EventRender3D?) {
        for (particle in this.particles) {
            val x = particle.location.x - (mc.renderManager as IMixinRenderManager).renderPosX
            val y = particle.location.y - (mc.renderManager as IMixinRenderManager).renderPosY
            val z = particle.location.z - (mc.renderManager as IMixinRenderManager).renderPosZ

            GlStateManager.pushMatrix()

            GlStateManager.enablePolygonOffset()
            GlStateManager.doPolygonOffset(1.0f, -1500000.0f)

            GlStateManager.translate(x.toFloat(), y.toFloat(), z.toFloat())
            GlStateManager.rotate(-mc.renderManager.playerViewY, 0.0f, 1.0f, 0.0f)
            val var10001 = if (mc.gameSettings.thirdPersonView == 2) -1.0f else 1.0f
            GlStateManager.rotate(mc.renderManager.playerViewX, var10001, 0.0f, 0.0f)
            val scale = 0.03
            GlStateManager.scale(-scale, -scale, scale)

            GL11.glDisable(2929)
            GL11.glEnable(3042)
            GL11.glDisable(3553)
            GL11.glBlendFunc(770, 771)
            GL11.glDepthMask(true)
            GL11.glEnable(2848)
            GL11.glHint(3154, 4354)
            GL11.glHint(3155, 4354)
            GL11.glEnable(3553)
            GL11.glDisable(3042)
            GL11.glEnable(2929)
            GL11.glDisable(2848)
            GL11.glHint(3154, 4352)
            GL11.glHint(3155, 4352)

            GL11.glDepthMask(false)
            fr.drawStringWithShadow(
                particle.text,
                -(mc.fontRendererObj.getStringWidth(particle.text) / 2).toFloat(),
                -(mc.fontRendererObj.FONT_HEIGHT - 1).toFloat(),
                0,
            )
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
            GL11.glDepthMask(true)

            GlStateManager.doPolygonOffset(1.0f, 1500000.0f)
            GlStateManager.disablePolygonOffset()

            GlStateManager.popMatrix()
        }
    }

    @EventTarget
    fun onLoadWorld(event: EventLoadWorld?) {
        this.particles.clear()
        this.healthMap.clear()
    }

    private class Particle(
        var location: LocationUtils,
        var text: String?,
    ) {
        var ticks: Int = 0
    }
}

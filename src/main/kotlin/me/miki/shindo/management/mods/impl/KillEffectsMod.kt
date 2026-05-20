package me.miki.shindo.management.mods.impl

import me.miki.shindo.Shindo
import me.miki.shindo.injection.interfaces.IMixinRenderManager
import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventLoadWorld
import me.miki.shindo.management.event.impl.EventRender3D
import me.miki.shindo.management.event.impl.EventTick
import me.miki.shindo.management.event.impl.EventUpdate
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.nanovg.font.Shinconic
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyEnum
import me.miki.shindo.management.settings.config.PropertyType
import net.minecraft.block.Block
import net.minecraft.client.Minecraft
import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.effect.EntityLightningBolt
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumParticleTypes
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.util.Random
import kotlin.math.abs

@Suppress("unused", "UNUSED_PARAMETER")
class KillEffectsMod :
    Mod(
        TranslateText.KILL_EFFECTS,
        TranslateText.KILL_EFFECTS_DESCRIPTION,
        ModCategory.RENDER,
        Shinconic.MOD_KILL_EFFECTS,
    ) {
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.SOUND)
    private val soundSetting = true

    @Property(type = PropertyType.COMBO, translate = TranslateText.EFFECT)
    private val effectType = EffectType.BLOOD

    @Property(
        type = PropertyType.NUMBER,
        translate = TranslateText.MULTIPLIER,
        min = 1.0,
        max = 10.0,
        step = 1.0,
        current = 1.0,
    )
    private val multiplierSetting = 1

    private val trackedEntities = ArrayList<EntityLivingBase>()
    private var entityID = 0
    private val physicsParticles: ArrayList<PhysicsParticle> = ArrayList()

    @EventTarget
    fun onUpdate(event: EventUpdate?) {
        for (obj in mc.theWorld.loadedEntityList) {
            if (obj is EntityLivingBase && obj !== mc.thePlayer) {
                if (!trackedEntities.contains(obj)) {
                    trackedEntities.add(obj)
                }
            }
        }

        val toRemove = ArrayList<EntityLivingBase>()
        for (entity in trackedEntities) {
            if (entity.deathTime == 1) {
                spawnKillEffect(entity)
                toRemove.add(entity)
            }
        }
        trackedEntities.removeAll(toRemove.toSet())
    }

    private fun spawnKillEffect(target: EntityLivingBase) {
        if (mc.thePlayer.ticksExisted > 10) {
            if (effectType == EffectType.LIGHTNING) {
                val entityLightningBolt = EntityLightningBolt(mc.theWorld, target.posX, target.posY, target.posZ)
                mc.theWorld.addEntityToWorld(entityID--, entityLightningBolt)
                if (soundSetting) {
                    mc.soundHandler.playSound(
                        PositionedSoundRecord.create(
                            ResourceLocation("ambient.weather.thunder"),
                            target.posX.toFloat(),
                            target.posY.toFloat(),
                            target.posZ.toFloat(),
                        ),
                    )
                }
            } else if (effectType == EffectType.FLAMES) {
                for (i in 0 until multiplierSetting) {
                    mc.effectRenderer.emitParticleAtEntity(target, EnumParticleTypes.FLAME)
                }
                if (soundSetting) {
                    mc.soundHandler.playSound(
                        PositionedSoundRecord.create(
                            ResourceLocation("item.fireCharge.use"),
                            target.posX.toFloat(),
                            target.posY.toFloat(),
                            target.posZ.toFloat(),
                        ),
                    )
                }
            } else if (effectType == EffectType.CLOUD) {
                for (i in 0 until multiplierSetting) {
                    mc.effectRenderer.emitParticleAtEntity(target, EnumParticleTypes.CLOUD)
                }
                if (soundSetting) {
                    mc.soundHandler.playSound(
                        PositionedSoundRecord.create(
                            ResourceLocation("fireworks.twinkle"),
                            target.posX.toFloat(),
                            target.posY.toFloat(),
                            target.posZ.toFloat(),
                        ),
                    )
                }
            } else if (effectType == EffectType.BLOOD) {
                for (i in 0..49) {
                    mc.theWorld.spawnParticle(
                        EnumParticleTypes.BLOCK_CRACK,
                        target.posX,
                        target.posY + target.height - 0.75,
                        target.posZ,
                        0.0,
                        0.0,
                        0.0,
                        Block.getStateId(Blocks.redstone_block.defaultState),
                    )
                }
                if (soundSetting) {
                    mc.soundHandler.playSound(
                        PositionedSoundRecord(
                            ResourceLocation("dig.stone"),
                            4.0f,
                            1.2f,
                            target.posX.toFloat(),
                            target.posY.toFloat(),
                            target.posZ.toFloat(),
                        ),
                    )
                }
            } else if (effectType == EffectType.PHYSICS) {
                val random = Random()
                val particleCount: Int = multiplierSetting * 10
                for (i in 0 until particleCount) {
                    val offsetX = (random.nextDouble() - 0.5) * 0.5
                    val offsetY = (random.nextDouble() - 0.5) * 0.5 + target.height / 2
                    val offsetZ = (random.nextDouble() - 0.5) * 0.5
                    val velocityX = (random.nextDouble() - 0.5) * 0.3
                    val velocityY = random.nextDouble() * 0.4 + 0.1
                    val velocityZ = (random.nextDouble() - 0.5) * 0.3
                    physicsParticles.add(
                        PhysicsParticle(
                            target.posX + offsetX,
                            target.posY + offsetY,
                            target.posZ + offsetZ,
                            velocityX,
                            velocityY,
                            velocityZ,
                        ),
                    )
                }
                if (soundSetting) {
                    mc.soundHandler.playSound(
                        PositionedSoundRecord(
                            ResourceLocation("dig.stone"),
                            2.0f,
                            0.8f,
                            target.posX.toFloat(),
                            target.posY.toFloat(),
                            target.posZ.toFloat(),
                        ),
                    )
                }
            }
        }
    }

    @EventTarget
    fun onLoadWorld(event: EventLoadWorld?) {
        entityID = 0
        physicsParticles.clear()
        trackedEntities.clear()
    }

    @EventTarget
    fun onTick(event: EventTick?) {
        for (particle in ArrayList(physicsParticles)) {
            particle.update()
            if (particle.age > 100 || particle.posY < 0) {
                physicsParticles.remove(particle)
            }
        }
    }

    @EventTarget
    fun onRender3D(event: EventRender3D) {
        for (particle in physicsParticles) {
            particle.render(event.getPartialTicks())
        }
    }

    private enum class EffectType(
        private val translate: TranslateText,
    ) : PropertyEnum {
        LIGHTNING(TranslateText.LIGHTING),
        FLAMES(TranslateText.FLAMES),
        CLOUD(TranslateText.CLOUD),
        BLOOD(TranslateText.BLOOD),
        PHYSICS(TranslateText.PHYSICS),
        ;

        override fun getTranslate(): TranslateText = translate
    }

    private class PhysicsParticle(
        private var posX: Double,
        var posY: Double,
        private var posZ: Double,
        vx: Double,
        vy: Double,
        vz: Double,
    ) {
        private var prevPosX: Double
        private var prevPosY: Double
        private var prevPosZ: Double
        private var velocityX: Double
        private var velocityY: Double
        private var velocityZ: Double
        var age: Int
        private val size: Double = 0.05
        private var mc: Minecraft = Minecraft.getMinecraft()

        fun update() {
            age++
            prevPosX = posX
            prevPosY = posY
            prevPosZ = posZ
            velocityY -= GRAVITY
            posX += velocityX
            posY += velocityY
            posZ += velocityZ
            velocityX *= FRICTION
            velocityZ *= FRICTION
            val blockPos = BlockPos(posX, posY - size, posZ)
            val blockBelow: Block = mc.theWorld.getBlockState(blockPos).block
            if (blockBelow !== Blocks.air && posY - posY.toInt() < size) {
                posY = posY.toInt() + size
                velocityY = -velocityY * BOUNCE_DAMPING
                if (abs(velocityY) < 0.01) {
                    velocityY = 0.0
                    velocityX *= 0.8
                    velocityZ *= 0.8
                }
            }
            val blockPosX = BlockPos(posX, posY, posZ)
            val blockAtX: Block = mc.theWorld.getBlockState(blockPosX).block
            if (blockAtX !== Blocks.air) {
                velocityX = -velocityX * BOUNCE_DAMPING
                posX -= velocityX * 2
            }
            val blockPosZ = BlockPos(posX, posY, posZ)
            val blockAtZ: Block = mc.theWorld.getBlockState(blockPosZ).block
            if (blockAtZ !== Blocks.air) {
                velocityZ = -velocityZ * BOUNCE_DAMPING
                posZ -= velocityZ * 2
            }
        }

        fun render(partialTicks: Float) {
            val interpX = prevPosX + (posX - prevPosX) * partialTicks
            val interpY = prevPosY + (posY - prevPosY) * partialTicks
            val interpZ = prevPosZ + (posZ - prevPosZ) * partialTicks
            val renderX = interpX - (mc.renderManager as IMixinRenderManager).renderPosX
            val renderY = interpY - (mc.renderManager as IMixinRenderManager).renderPosY
            val renderZ = interpZ - (mc.renderManager as IMixinRenderManager).renderPosZ
            val color: Color =
                Shindo
                    .getInstance()
                    .getColorManager()
                    .getCurrentColor()
                    .getColor1()
            GlStateManager.pushMatrix()
            GlStateManager.translate(renderX, renderY, renderZ)
            GlStateManager.rotate(-mc.renderManager.playerViewY, 0.0f, 1.0f, 0.0f)
            GlStateManager.rotate(mc.renderManager.playerViewX, 1.0f, 0.0f, 0.0f)
            GlStateManager.disableLighting()
            GlStateManager.enableBlend()
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
            GlStateManager.disableCull()
            val alpha = 0f.coerceAtLeast(1 - age / 50f)
            val tessellator = Tessellator.getInstance()
            val worldrenderer = tessellator.worldRenderer
            GlStateManager.color(color.red / 255f, color.green / 255f, color.blue / 255f, alpha)
            val size = (size * alpha).toFloat()
            mc.textureManager.bindTexture(ResourceLocation("shindo/circle.png"))
            worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX)
            worldrenderer.pos(-size.toDouble(), -size.toDouble(), 0.0).tex(0.0, 0.0).endVertex()
            worldrenderer.pos(-size.toDouble(), size.toDouble(), 0.0).tex(0.0, 1.0).endVertex()
            worldrenderer.pos(size.toDouble(), size.toDouble(), 0.0).tex(1.0, 1.0).endVertex()
            worldrenderer.pos(size.toDouble(), -size.toDouble(), 0.0).tex(1.0, 0.0).endVertex()
            tessellator.draw()
            GlStateManager.enableLighting()
            GlStateManager.disableBlend()
            GlStateManager.enableCull()
            GlStateManager.popMatrix()
        }

        companion object {
            private const val GRAVITY = 0.03
            private const val BOUNCE_DAMPING = 0.6
            private const val FRICTION = 0.98
        }

        init {
            prevPosX = posX
            prevPosY = posY
            prevPosZ = posZ
            velocityX = vx
            velocityY = vy
            velocityZ = vz
            age = 0
        }
    }
}

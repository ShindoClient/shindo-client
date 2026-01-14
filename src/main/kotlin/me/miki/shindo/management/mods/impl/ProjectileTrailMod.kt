package me.miki.shindo.management.mods.impl

import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventUpdate
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.mods.impl.projectiletrail.ProjectileTrailType
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyType
import net.minecraft.entity.Entity
import net.minecraft.entity.projectile.EntityArrow
import net.minecraft.entity.projectile.EntityFishHook
import net.minecraft.entity.projectile.EntityThrowable
import net.minecraft.util.Vec3
import java.util.*

class ProjectileTrailMod : Mod(
    TranslateText.PROJECTILE_TRAIL,
    TranslateText.PROJECTILE_TRAIL_DESCRIPTION,
    ModCategory.PLAYER,
    LegacyIcon.MOD_PROJECTILE_TRAIL
) {
    @Property(type = PropertyType.COMBO, translate = TranslateText.TYPE)
    private val type = ProjectileTrailType.HEARTS

    private val throwables = ArrayList<Any?>()
    private var ticks = 0

    @EventTarget
    fun onUpdate(event: EventUpdate?) {
        val currentType = type
        ticks = if (ticks >= 20) 0 else ticks + 2

        updateThrowables()

        for (entity in mc.theWorld.getLoadedEntityList()) {
            if (entity != null && (isValidEntity(entity) || throwables.contains(entity)) && entity.getDistanceToEntity(
                    mc.thePlayer
                ) > 3.0f
            ) {
                spawnParticle(currentType, entity.getPositionVector())
            }
        }
    }

    fun spawnParticle(trail: ProjectileTrailType, vector: Vec3) {
        if (trail != ProjectileTrailType.GREEN_STAR && trail != ProjectileTrailType.HEARTS || ticks % 4 == 0) {
            if (trail != ProjectileTrailType.MUSIC_NOTES || ticks % 2 == 0) {
                val translate = trail.translate
                val velocity = trail.velocity

                for (i in 0..<trail.count) {
                    val random = Random()
                    val x = random.nextFloat() * translate * 2.0f - translate
                    val y = random.nextFloat() * translate * 2.0f - translate
                    val z = random.nextFloat() * translate * 2.0f - translate
                    val xVel = random.nextFloat() * velocity * 2.0f - velocity
                    val yVel = random.nextFloat() * velocity * 2.0f - velocity
                    val zVel = random.nextFloat() * velocity * 2.0f - velocity
                    val d0 = x.toDouble() + vector.xCoord
                    val d1 = y.toDouble() + vector.yCoord
                    val d2 = z.toDouble() + vector.zCoord

                    mc.theWorld.spawnParticle(
                        trail.particle,
                        true,
                        d0,
                        d1,
                        d2,
                        xVel.toDouble(),
                        yVel.toDouble(),
                        zVel.toDouble()
                    )
                }
            }
        }
    }

    fun isValidEntity(entity: Entity): Boolean {
        if (entity.posX == entity.prevPosX && entity.posY == entity.prevPosY && entity.posZ == entity.prevPosZ) {
            return false
        } else {
            if (entity is EntityArrow) {
                return entity.shootingEntity != null && entity.shootingEntity == mc.thePlayer
            } else if (entity is EntityFishHook) {
                return entity.angler != null && entity.angler == mc.thePlayer
            } else if (entity is EntityThrowable && entity.ticksExisted == 1 && entity.getDistanceSqToEntity(mc.thePlayer) <= 11.0 && !throwables.contains(
                    entity
                )
            ) {
                throwables.add(entity)
                return true
            }

            return false
        }
    }

    fun updateThrowables() {
        val iterator = throwables.iterator()

        while (iterator.hasNext()) {
            val throwable = iterator.next() as EntityThrowable?

            if (throwable == null || throwable.isDead) {
                iterator.remove()
            }
        }
    }
}





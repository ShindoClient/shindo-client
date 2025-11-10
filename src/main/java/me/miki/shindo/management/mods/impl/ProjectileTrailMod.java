package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventUpdate;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import me.miki.shindo.management.settings.config.Property;
import me.miki.shindo.management.settings.config.PropertyType;
import me.miki.shindo.management.mods.impl.projectiletrail.ProjectileTrailType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.Vec3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class ProjectileTrailMod extends Mod {

    private final ArrayList<Object> throwables = new ArrayList<>();
    @Property(type = PropertyType.COMBO, translate = TranslateText.TYPE)
    private ProjectileTrailType type = ProjectileTrailType.HEARTS;
    private int ticks;

    public ProjectileTrailMod() {
        super(TranslateText.PROJECTILE_TRAIL, TranslateText.PROJECTILE_TRAIL_DESCRIPTION, ModCategory.PLAYER);
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {

        ProjectileTrailType currentType = type;
        ticks = ticks >= 20 ? 0 : ticks + 2;

        updateThrowables();
        Iterator<Entity> iterator = mc.theWorld.getLoadedEntityList().iterator();

        while (iterator.hasNext()) {
            Entity entity = iterator.next();

            if (entity != null && (isValidEntity(entity) || throwables.contains(entity)) && entity.getDistanceToEntity(mc.thePlayer) > 3.0F) {
                spawnParticle(currentType, entity.getPositionVector());
            }
        }
    }

    public void spawnParticle(ProjectileTrailType trail, Vec3 vector) {
        if (trail != ProjectileTrailType.GREEN_STAR && trail != ProjectileTrailType.HEARTS || ticks % 4 == 0) {
            if (trail != ProjectileTrailType.MUSIC_NOTES || ticks % 2 == 0) {
                float translate = trail.translate;
                float velocity = trail.velocity;

                for (int i = 0; i < trail.count; ++i) {
                    Random random = new Random();
                    float x = random.nextFloat() * translate * 2.0F - translate;
                    float y = random.nextFloat() * translate * 2.0F - translate;
                    float z = random.nextFloat() * translate * 2.0F - translate;
                    float xVel = random.nextFloat() * velocity * 2.0F - velocity;
                    float yVel = random.nextFloat() * velocity * 2.0F - velocity;
                    float zVel = random.nextFloat() * velocity * 2.0F - velocity;
                    double d0 = (double) x + vector.xCoord;
                    double d1 = (double) y + vector.yCoord;
                    double d2 = (double) z + vector.zCoord;

                    mc.theWorld.spawnParticle(trail.particle, true, d0, d1, d2, xVel, yVel, zVel);
                }
            }
        }
    }

    public boolean isValidEntity(Entity entity) {
        if (entity.posX == entity.prevPosX && entity.posY == entity.prevPosY && entity.posZ == entity.prevPosZ) {
            return false;
        } else {
            if (entity instanceof EntityArrow) {
                return ((EntityArrow) entity).shootingEntity != null && ((EntityArrow) entity).shootingEntity.equals(mc.thePlayer);
            } else if (entity instanceof EntityFishHook) {
                return ((EntityFishHook) entity).angler != null && ((EntityFishHook) entity).angler.equals(mc.thePlayer);
            } else if (entity instanceof EntityThrowable && entity.ticksExisted == 1 && entity.getDistanceSqToEntity(mc.thePlayer) <= 11.0D && !throwables.contains(entity)) {
                throwables.add(entity);
                return true;
            }

            return false;
        }
    }

    public void updateThrowables() {

        Iterator<?> iterator = throwables.iterator();

        while (iterator.hasNext()) {
            EntityThrowable throwable = (EntityThrowable) iterator.next();

            if (throwable == null || throwable.isDead) {
                iterator.remove();
            }
        }
    }
}

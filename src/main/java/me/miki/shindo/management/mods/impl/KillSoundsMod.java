package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventMotionUpdate;
import me.miki.shindo.management.event.impl.EventTick;
import me.miki.shindo.management.event.impl.EventUpdate;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import me.miki.shindo.management.settings.config.Property;
import me.miki.shindo.management.settings.config.PropertyType;
import me.miki.shindo.utils.Sound;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

import java.io.File;
public class KillSoundsMod extends Mod {

    private final Sound oofSound = new Sound();
    private final Sound customSound = new Sound();
    @Property(type = PropertyType.NUMBER, translate = TranslateText.VOLUME, min = 0.0, max = 1.0, current = 0.5)
    private double volumeSetting = 0.5;

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.CUSTOM_SOUND)
    private boolean customSoundSetting;

    @Property(type = PropertyType.SOUND, translate = TranslateText.SOUND)
    private File soundFile;
    private EntityLivingBase target;
    private File prevCustomSound;

    public KillSoundsMod() {
        super(TranslateText.KILL_SOUNDS, TranslateText.KILL_SOUNDS_DESCRIPTION, ModCategory.OTHER);
    }

    @EventTarget
    public void onTick(EventTick event) {

        if (customSoundSetting) {
            if (soundFile != null) {
                if (!soundFile.equals(prevCustomSound)) {
                    prevCustomSound = soundFile;
                    try {
                        customSound.loadClip(soundFile);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                customSound.setVolume((float) volumeSetting);
            }
        } else {
            oofSound.setVolume((float) volumeSetting);
        }
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        if (mc.objectMouseOver != null & mc.objectMouseOver.entityHit != null) {
            if (mc.objectMouseOver.entityHit instanceof EntityLivingBase) {
                target = (EntityLivingBase) mc.objectMouseOver.entityHit;
            }
        }
    }

    @EventTarget
    public void onPreMotionUpdate(EventMotionUpdate event) {

        if (target != null && !mc.theWorld.loadedEntityList.contains(target) && mc.thePlayer.getDistanceSq(target.posX, mc.thePlayer.posY, target.posZ) < 100) {

            if (mc.thePlayer.ticksExisted > 3) {

                if (customSoundSetting) {
                    customSound.play();
                } else {
                    oofSound.play();
                }
            }

            target = null;
        }
    }

    @Override
    public void onEnable() {
        super.onEnable();
        try {
            oofSound.loadClip(new ResourceLocation("shindo/audio/oof.wav"));
        } catch (Exception e) {
        }
    }
}

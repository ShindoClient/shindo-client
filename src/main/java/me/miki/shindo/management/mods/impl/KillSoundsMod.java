package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventMotionUpdate;
import me.miki.shindo.management.event.impl.EventTick;
import me.miki.shindo.management.event.impl.EventUpdate;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import me.miki.shindo.management.mods.settings.impl.BooleanSetting;
import me.miki.shindo.management.mods.settings.impl.NumberSetting;
import me.miki.shindo.management.mods.settings.impl.SoundSetting;
import me.miki.shindo.utils.Sound;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

import java.io.File;

public class KillSoundsMod extends Mod {

    private EntityLivingBase target;

    private File prevCustomSound;

    private final Sound oofSound = new Sound();
    private final Sound customSound = new Sound();

    private final NumberSetting volumeSetting = new NumberSetting(TranslateText.VOLUME, this, 0.5, 0.0, 1.0, false);
    private final BooleanSetting customSoundSetting = new BooleanSetting(TranslateText.CUSTOM_SOUND, this, false);
    private final SoundSetting soundSetting = new SoundSetting(TranslateText.SOUND, this);

    public KillSoundsMod() {
        super(TranslateText.KILL_SOUNDS, TranslateText.KILL_SOUNDS_DESCRIPTION, ModCategory.OTHER);
    }

    @EventTarget
    public void onTick(EventTick event) {

        if (customSoundSetting.isToggled()) {

            if (soundSetting.getSound() != null) {

                if (prevCustomSound != soundSetting.getSound()) {

                    prevCustomSound = soundSetting.getSound();

                    try {
                        customSound.loadClip(soundSetting.getSound());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                customSound.setVolume(volumeSetting.getValueFloat());
            }
        } else {
            oofSound.setVolume(volumeSetting.getValueFloat());
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

                if (customSoundSetting.isToggled()) {
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

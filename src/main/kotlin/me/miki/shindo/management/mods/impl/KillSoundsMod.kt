package me.miki.shindo.management.mods.impl

import me.miki.shindo.logger.ShindoLogger
import me.miki.client_api.event.EventTarget
import me.miki.shindo.management.event.impl.EventMotionUpdate
import me.miki.shindo.management.event.impl.EventTick
import me.miki.shindo.management.event.impl.EventUpdate
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyType
import me.miki.shindo.management.sound.Sound
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.ResourceLocation
import java.io.File

class KillSoundsMod : Mod(
    TranslateText.KILL_SOUNDS,
    TranslateText.KILL_SOUNDS_DESCRIPTION,
    ModCategory.OTHER,
    LegacyIcon.MOD_KILL_SOUNDS
) {
    private val oofSound = Sound()
    private val customSound = Sound()

    @Property(type = PropertyType.NUMBER, translate = TranslateText.VOLUME, min = 0.0, max = 1.0, current = 0.5)
    private val volumeSetting = 0.5

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.CUSTOM_SOUND)
    private val customSoundSetting = false

    @Property(type = PropertyType.SOUND, translate = TranslateText.SOUND)
    private val soundFile: File? = null
    private var target: EntityLivingBase? = null
    private var prevCustomSound: File? = null

    @EventTarget
    fun onTick(event: EventTick?) {
        if (customSoundSetting) {
            if (soundFile != null) {
                if (soundFile != prevCustomSound) {
                    prevCustomSound = soundFile
                    try {
                        customSound.loadClip(soundFile)
                    } catch (e: Exception) {
                        ShindoLogger.error(
                            "An error occurred while loading the custom sound file: " + soundFile.absolutePath,
                            e
                        )
                    }
                }
                customSound.setVolume(volumeSetting.toFloat())
            }
        } else {
            oofSound.setVolume(volumeSetting.toFloat())
        }
    }

    @EventTarget
    fun onUpdate(event: EventUpdate?) {
        if (mc.objectMouseOver != null && mc.objectMouseOver.entityHit != null) {
            if (mc.objectMouseOver.entityHit is EntityLivingBase) {
                target = mc.objectMouseOver.entityHit as EntityLivingBase
            }
        }
    }

    @EventTarget
    fun onPreMotionUpdate(event: EventMotionUpdate?) {
        if (target != null && !mc.theWorld.loadedEntityList.contains(target) && mc.thePlayer.getDistanceSq(
                target!!.posX,
                mc.thePlayer.posY,
                target!!.posZ
            ) < 100
        ) {
            if (mc.thePlayer.ticksExisted > 3) {
                if (customSoundSetting) {
                    customSound.play()
                } else {
                    oofSound.play()
                }
            }

            target = null
        }
    }

    override fun onEnable() {
        super.onEnable()
        try {
            oofSound.loadClip(ResourceLocation("shindo/audio/oof.wav"))
        } catch (e: Exception) {
            ShindoLogger.error(
                "An error occurred while loading the custom sound file: " + soundFile!!.absolutePath,
                e
            )
        }
    }
}






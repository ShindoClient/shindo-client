package me.miki.shindo.management.mods.impl

import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventPlaySound
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyType

class SoundModifierMod : Mod(
    TranslateText.SOUND_MODIFIER,
    TranslateText.SOUND_MODIFIER_DESCRIPTION,
    ModCategory.OTHER,
    LegacyIcon.MOD_SOUND_MODIFIER
) {
    @Property(type = PropertyType.NUMBER, translate = TranslateText.NOTE, min = 0, max = 100, current = 100, step = 1)
    private val noteSetting = 100

    @Property(type = PropertyType.NUMBER, translate = TranslateText.TNT, min = 0, max = 100, current = 100, step = 1)
    private val tntSetting = 100

    @Property(type = PropertyType.NUMBER, translate = TranslateText.PORTAL, min = 0, max = 100, current = 100, step = 1)
    private val portalSetting = 100

    @Property(type = PropertyType.NUMBER, translate = TranslateText.STEP, min = 0, max = 100, current = 100, step = 1)
    private val stepSetting = 100

    @Property(type = PropertyType.NUMBER, translate = TranslateText.MOBS, min = 0, max = 100, current = 100, step = 1)
    private val mobsSetting = 100

    @Property(
        type = PropertyType.NUMBER,
        translate = TranslateText.RECORDS,
        min = 0,
        max = 100,
        current = 100,
        step = 1
    )
    private val recordsSetting = 100

    @Property(
        type = PropertyType.NUMBER,
        translate = TranslateText.FIREWORKS,
        min = 0,
        max = 100,
        current = 100,
        step = 1
    )
    private val fireworksSetting = 100

    @EventTarget
    fun onPlaySound(event: EventPlaySound) {
        if (event.getSoundName().startsWith("fireworks")) {
            event.setVolume(fireworksSetting / 100f)
        }

        if (event.getSoundName().startsWith("records")) {
            event.setVolume(recordsSetting / 100f)
        }

        if (event.getSoundName().startsWith("step")) {
            event.setVolume(stepSetting / 100f)
        }

        if (event.getSoundName().contains("mob")) {
            event.setVolume(mobsSetting / 100f)
        }

        if (event.getSoundName().startsWith("note")) {
            event.setVolume(noteSetting / 100f)
        }

        if (event.getSoundName() == "game.tnt.primed" || event.getSoundName() == "random.explode" || event.getSoundName() == "creeper.primed") {
            event.setVolume(tntSetting / 100f)
        }

        if (event.getSoundName().startsWith("portal")) {
            event.setVolume(portalSetting / 100f)
        }
    }
}





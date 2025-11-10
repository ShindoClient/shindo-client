package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventPlaySound;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;

import me.miki.shindo.management.settings.config.Property;
import me.miki.shindo.management.settings.config.PropertyType;
public class SoundModifierMod extends Mod {

    @Property(type = PropertyType.NUMBER, translate = TranslateText.NOTE, min = 0, max = 100, current = 100, step = 1)
    private int noteSetting = 100;
    @Property(type = PropertyType.NUMBER, translate = TranslateText.TNT, min = 0, max = 100, current = 100, step = 1)
    private int tntSetting = 100;
    @Property(type = PropertyType.NUMBER, translate = TranslateText.PORTAL, min = 0, max = 100, current = 100, step = 1)
    private int portalSetting = 100;
    @Property(type = PropertyType.NUMBER, translate = TranslateText.STEP, min = 0, max = 100, current = 100, step = 1)
    private int stepSetting = 100;
    @Property(type = PropertyType.NUMBER, translate = TranslateText.MOBS, min = 0, max = 100, current = 100, step = 1)
    private int mobsSetting = 100;
    @Property(type = PropertyType.NUMBER, translate = TranslateText.RECORDS, min = 0, max = 100, current = 100, step = 1)
    private int recordsSetting = 100;
    @Property(type = PropertyType.NUMBER, translate = TranslateText.FIREWORKS, min = 0, max = 100, current = 100, step = 1)
    private int fireworksSetting = 100;

    public SoundModifierMod() {
        super(TranslateText.SOUND_MODIFIER, TranslateText.SOUND_MODIFIER_DESCRIPTION, ModCategory.OTHER);
    }

    @EventTarget
    public void onPlaySound(EventPlaySound event) {

        if (event.getSoundName().startsWith("fireworks")) {
            event.setVolume(fireworksSetting / 100F);
        }

        if (event.getSoundName().startsWith("records")) {
            event.setVolume(recordsSetting / 100F);
        }

        if (event.getSoundName().startsWith("step")) {
            event.setVolume(stepSetting / 100F);
        }

        if (event.getSoundName().contains("mob")) {
            event.setVolume(mobsSetting / 100F);
        }

        if (event.getSoundName().startsWith("note")) {
            event.setVolume(noteSetting / 100F);
        }

        if (event.getSoundName().equals("game.tnt.primed") || event.getSoundName().equals("random.explode") || event.getSoundName().equals("creeper.primed")) {
            event.setVolume(tntSetting / 100F);
        }

        if (event.getSoundName().startsWith("portal")) {
            event.setVolume(portalSetting / 100F);
        }
    }
}

package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventPlaySound;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import me.miki.shindo.management.mods.settings.impl.NumberSetting;

public class SoundModifierMod extends Mod {

    private final NumberSetting noteSetting = new NumberSetting(TranslateText.NOTE, this, 100, 0, 100, true);
    private final NumberSetting tntSetting = new NumberSetting(TranslateText.TNT, this, 100, 0, 100, true);
    private final NumberSetting portalSetting = new NumberSetting(TranslateText.PORTAL, this, 100, 0, 100, true);
    private final NumberSetting stepSetting = new NumberSetting(TranslateText.STEP, this, 100, 0, 100, true);
    private final NumberSetting mobsSetting = new NumberSetting(TranslateText.MOBS, this, 100, 0, 100, true);
    private final NumberSetting recordsSetting = new NumberSetting(TranslateText.RECORDS, this, 100, 0, 100, true);
    private final NumberSetting fireworksSetting = new NumberSetting(TranslateText.FIREWORKS, this, 100, 0, 100, true);

    public SoundModifierMod() {
        super(TranslateText.SOUND_MODIFIER, TranslateText.SOUND_MODIFIER_DESCRIPTION, ModCategory.OTHER);
    }

    @EventTarget
    public void onPlaySound(EventPlaySound event) {

        if (event.getSoundName().startsWith("fireworks")) {
            event.setVolume(fireworksSetting.getValueInt() / 100F);
        }

        if (event.getSoundName().startsWith("records")) {
            event.setVolume(recordsSetting.getValueInt() / 100F);
        }

        if (event.getSoundName().startsWith("step")) {
            event.setVolume(stepSetting.getValueInt() / 100F);
        }

        if (event.getSoundName().contains("mob")) {
            event.setVolume(mobsSetting.getValueInt() / 100F);
        }

        if (event.getSoundName().startsWith("note")) {
            event.setVolume(noteSetting.getValueInt() / 100F);
        }

        if (event.getSoundName().equals("game.tnt.primed") || event.getSoundName().equals("random.explode") || event.getSoundName().equals("creeper.primed")) {
            event.setVolume(tntSetting.getValueInt() / 100F);
        }

        if (event.getSoundName().startsWith("portal")) {
            event.setVolume(portalSetting.getValueInt() / 100F);
        }
    }
}

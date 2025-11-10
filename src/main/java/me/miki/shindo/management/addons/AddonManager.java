package me.miki.shindo.management.addons;

import lombok.Getter;
import me.miki.shindo.management.addons.patcher.PatcherAddon;
import me.miki.shindo.management.addons.rpo.RPOAddon;
import me.miki.shindo.management.settings.Setting;
import me.miki.shindo.management.settings.metadata.SettingRegistry;
import me.miki.shindo.utils.Sound;

import java.util.ArrayList;
import java.util.Arrays;

@Getter
public class AddonManager {

    private final ArrayList<Addon> addons = new ArrayList<Addon>();
    private final ArrayList<Setting> settings = new ArrayList<Setting>();


    public void init() {
        registerAddon(new RPOAddon());
        registerAddon(new PatcherAddon());
    }

    public Addon getAddonByName(String name) {

        for (Addon a : addons) {
            if (a.getName().equals(name)) {
                return a;
            }
        }

        return null;
    }

    public ArrayList<Setting> getSettingByAddon(Addon a) {

        ArrayList<Setting> result = new ArrayList<Setting>();

        for (Setting s : settings) {
            if (s.getParent().equals(a)) {
                result.add(s);
            }
        }

        if (result.isEmpty()) {
            return null;
        }

        return result;
    }

    public String getWords(Addon addon) {

        StringBuilder result = new StringBuilder();

        for (Addon a : addons) {
            if (a.equals(addon)) {
                result.append(a.getName()).append(" ");
            }
        }
        return result.toString();
    }

    public void addSettings(Setting... settingsList) {
        settings.addAll(Arrays.asList(settingsList));
    }

    private void registerAddon(Addon addon) {
        addons.add(addon);
        SettingRegistry.applyMetadata(addon);
    }


    public void playToggleSound(boolean toggled) {
        if (toggled) {
            Sound.play("shindo/audio/positive.wav", true);
        } else {
            Sound.play("shindo/audio/negative.wav", true);
        }

    }
}

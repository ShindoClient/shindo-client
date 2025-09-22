package me.miki.shindo.management.addons;

import lombok.Getter;
import me.miki.shindo.management.addons.patcher.PatcherAddon;
import me.miki.shindo.management.addons.rpo.RPOAddon;
import me.miki.shindo.management.addons.settings.AddonSetting;
import me.miki.shindo.utils.Sound;

import java.util.ArrayList;
import java.util.Arrays;

@Getter
public class AddonManager {

    private final ArrayList<Addon> addons = new ArrayList<Addon>();
    private final ArrayList<AddonSetting> settings = new ArrayList<AddonSetting>();


    public void init() {
        addons.add(new RPOAddon());
        addons.add(new PatcherAddon());
    }

    public Addon getAddonByName(String name) {

        for (Addon a : addons) {
            if (a.getName().equals(name)) {
                return a;
            }
        }

        return null;
    }

    public ArrayList<AddonSetting> getSettingByAddon(Addon a) {

        ArrayList<AddonSetting> result = new ArrayList<AddonSetting>();

        for (AddonSetting s : settings) {
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

    public void addSettings(AddonSetting... settingsList) {
        settings.addAll(Arrays.asList(settingsList));
    }


    public void playToggleSound(boolean toggled) {
        if (toggled) {
            Sound.play("shindo/audio/positive.wav", true);
        } else {
            Sound.play("shindo/audio/negative.wav", true);
        }

    }
}

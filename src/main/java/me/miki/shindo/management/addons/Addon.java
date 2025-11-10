package me.miki.shindo.management.addons;

import lombok.Getter;
import me.miki.shindo.Shindo;
import me.miki.shindo.logger.ShindoLogger;
import me.miki.shindo.management.settings.config.ConfigOwner;
import me.miki.shindo.utils.animation.simple.SimpleAnimation;

@Getter
public class Addon implements ConfigOwner {

    private final SimpleAnimation animation = new SimpleAnimation();

    private final String name;
    private final String description;
    private final String icon;
    private final AddonType type;

    private boolean toggled;

    public Addon(String name, String description, String icon, AddonType type) {
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.type = type;

        this.setup();
    }

    public void setup() {
    }


    public void onEnable() {
        Shindo.getInstance().getEventManager().register(this);
        ShindoLogger.info("[ADDON] " + getName() + " was enabled");
    }

    public void onDisable() {
        Shindo.getInstance().getEventManager().unregister(this);
        ShindoLogger.info("[ADDON] " + getName() + " was disabled");
    }

    public void toggle() {
        setToggled(!toggled, true);
    }

    public void setToggled(boolean toggled, boolean sound) {

        this.toggled = toggled;

        if (toggled) {
            onEnable();
            if (sound) Shindo.getInstance().getAddonManager().playToggleSound(true);
        } else {
            onDisable();
            if (sound) Shindo.getInstance().getAddonManager().playToggleSound(false);
        }
    }

    @Override
    public String getConfigId() {
        return getName().toLowerCase().replace(' ', '_');
    }

    @Override
    public String getDisplayName() {
        return getName();
    }

}

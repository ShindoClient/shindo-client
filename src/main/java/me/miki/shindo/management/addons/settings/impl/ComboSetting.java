package me.miki.shindo.management.addons.settings.impl;

import lombok.Getter;
import lombok.Setter;
import me.miki.shindo.Shindo;
import me.miki.shindo.management.addons.Addon;
import me.miki.shindo.management.addons.settings.AddonSetting;
import me.miki.shindo.management.addons.settings.impl.combo.Option;
import me.miki.shindo.management.language.TranslateText;

import java.util.ArrayList;

@Getter
public class ComboSetting extends AddonSetting {

    private final ArrayList<Option> options;

    private final Option defaultOption;
    @Setter
    private Option option;

    public ComboSetting(String text, Addon parent, TranslateText defaultOption, ArrayList<Option> options) {
        super(text, parent);

        this.options = options;
        this.option = getOptionByName(defaultOption.getKey());
        this.defaultOption = getOptionByName(defaultOption.getKey());

        Shindo.getInstance().getAddonManager().addSettings(this);
    }

    @Override
    public void reset() {
        this.option = defaultOption;
    }

    public Option getOptionByName(String nameKey) {

        for (Option op : options) {
            if (op.getName().equals(nameKey)) {
                return op;
            }
        }

        return option;
    }

}

package me.miki.shindo.management.settings.impl;

import me.miki.shindo.management.settings.config.ConfigOwner;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.settings.Setting;
import me.miki.shindo.management.settings.impl.combo.Option;

import java.util.ArrayList;
import java.util.List;

public class ComboSetting extends Setting {

    private final List<Option> options;
    private final Option defaultOption;
    private Option option;

    public ComboSetting(TranslateText text, ConfigOwner parent, TranslateText defaultOption, List<Option> options) {
        super(text, parent);
        this.options = new ArrayList<>(options);
        this.option = getOptionByKey(defaultOption.getKey());
        this.defaultOption = this.option;
    }

    public ComboSetting(TranslateText text, ConfigOwner parent, String defaultOptionKey, List<Option> options) {
        super(text, parent);
        this.options = new ArrayList<>(options);
        this.option = getOptionByKey(defaultOptionKey);
        if (this.option == null && !this.options.isEmpty()) {
            this.option = this.options.get(0);
        }
        this.defaultOption = this.option;
    }

    public ComboSetting(String name, ConfigOwner parent, String defaultOptionKey, List<Option> options) {
        super(name, parent);
        this.options = new ArrayList<>(options);
        this.option = getOptionByKey(defaultOptionKey);
        if (this.option == null && !this.options.isEmpty()) {
            this.option = this.options.get(0);
        }
        this.defaultOption = this.option;
    }

    @Override
    public void reset() {
        this.option = defaultOption;
    }

    public Option getOption() {
        return option;
    }

    public void setOption(Option option) {
        this.option = option;
    }

    public List<Option> getOptions() {
        return options;
    }

    public Option getDefaultOption() {
        return defaultOption;
    }

    @Deprecated
    public Option getOptionByNameKey(String nameKey) {
        return getOptionByKey(nameKey);
    }

    public Option getOptionByKey(String key) {
        for (Option op : options) {
            if (op.getNameKey().equalsIgnoreCase(key)) {
                return op;
            }
        }
        return options.isEmpty() ? null : options.get(0);
    }
}

package me.miki.shindo.management.settings.impl;

import me.miki.shindo.management.settings.config.ConfigOwner;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.settings.Setting;

public class CategorySetting extends Setting {

    public CategorySetting(TranslateText text, ConfigOwner parent) {
        super(text, parent);
    }

    public CategorySetting(String name, ConfigOwner parent) {
        super(name, parent);
    }

    private boolean collapsed;

    public boolean isCollapsed() {
        return collapsed;
    }

    public void setCollapsed(boolean collapsed) {
        this.collapsed = collapsed;
    }

    public void toggle() {
        this.collapsed = !this.collapsed;
    }
}

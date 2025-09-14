package me.miki.shindo.gui.modmenu.category.impl;

import me.miki.shindo.gui.modmenu.GuiModMenu;
import me.miki.shindo.gui.modmenu.category.Category;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.nanovg.font.LegacyIcon;

public class TweakerCategory extends Category {

    public TweakerCategory(GuiModMenu parent) {
        super(parent, TranslateText.TWEAKER, LegacyIcon.TWEAKER, false, true);
    }
}

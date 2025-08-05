package me.miki.shindo.management.mods.settings.impl;

import me.miki.shindo.Shindo;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.settings.Setting;

public class CellGridSetting extends Setting {
    private boolean[][] cells;
    private final boolean[][] defaultCells;

    public CellGridSetting(TranslateText text, Mod parent, boolean[][] cells) {
        super(text, parent);
        this.cells = cells;
        this.defaultCells = cells;

        Shindo.getInstance().getModManager().addSettings(this);
    }

    @Override
    public void reset() {
        this.cells = defaultCells;
    }

    public boolean[][] getCells() {
        return cells;
    }

    public void setCells(boolean[][] cells) {
        this.cells = cells;
    }

    public boolean[][] getDefaultCells() {
        return defaultCells;
    }
}

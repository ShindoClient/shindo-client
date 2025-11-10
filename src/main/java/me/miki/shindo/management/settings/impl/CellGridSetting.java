package me.miki.shindo.management.settings.impl;

import me.miki.shindo.management.settings.config.ConfigOwner;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.settings.Setting;

public class CellGridSetting extends Setting {
    private final boolean[][] defaultCells;
    private boolean[][] cells;

    public CellGridSetting(TranslateText text, ConfigOwner parent, boolean[][] cells) {
        super(text, parent);
        this.cells = cells;
        this.defaultCells = cells;
    }

    public CellGridSetting(String name, ConfigOwner parent, boolean[][] cells) {
        super(name, parent);
        this.cells = cells;
        this.defaultCells = cells;
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

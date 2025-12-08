package me.miki.shindo.management.settings.impl;

import me.miki.shindo.management.settings.config.ConfigOwner;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.settings.Setting;

import java.awt.Color;

public class CellGridSetting extends Setting {
    private static final Color DEFAULT_CELL_COLOR = Color.RED;

    private final boolean[][] defaultCells;
    private final int[][] defaultColors;

    private boolean[][] cells;
    private int[][] colors;

    public CellGridSetting(TranslateText text, ConfigOwner parent, boolean[][] cells) {
        super(text, parent);
        this.cells = copyGrid(cells);
        this.defaultCells = copyGrid(cells);
        this.colors = buildColorGrid(this.cells, DEFAULT_CELL_COLOR);
        this.defaultColors = copyColorGrid(this.colors);
    }

    public CellGridSetting(String name, ConfigOwner parent, boolean[][] cells) {
        super(name, parent);
        this.cells = copyGrid(cells);
        this.defaultCells = copyGrid(cells);
        this.colors = buildColorGrid(this.cells, DEFAULT_CELL_COLOR);
        this.defaultColors = copyColorGrid(this.colors);
    }

    @Override
    public void reset() {
        this.cells = copyGrid(defaultCells);
        this.colors = copyColorGrid(defaultColors);
    }

    public boolean[][] getCells() {
        return cells;
    }

    public void setCells(boolean[][] cells) {
        this.cells = copyGrid(cells);
        alignColorsWithCells();
    }

    public boolean[][] getDefaultCells() {
        return defaultCells;
    }

    public int[][] getColorGrid() {
        return copyColorGrid(colors);
    }

    public void setColorGrid(int[][] colors) {
        this.colors = alignColorGrid(colors, cells);
    }

    public void setColorGrid(Color[][] colors) {
        this.colors = alignColorGrid(colors, cells);
    }

    public Color getCellColor(int row, int col) {
        if (!isValidIndex(row, col, colors)) {
            return DEFAULT_CELL_COLOR;
        }
        return new Color(colors[row][col], true);
    }

    public Color getCellColorOrDefault(int row, int col, Color fallback) {
        if (!isValidIndex(row, col, colors)) {
            return fallback == null ? DEFAULT_CELL_COLOR : fallback;
        }
        return new Color(colors[row][col], true);
    }

    public void setCell(int row, int col, boolean enabled, Color color) {
        if (!isValidIndex(row, col, cells)) {
            return;
        }
        boolean[][] copy = copyGrid(cells);
        copy[row][col] = enabled;
        setCells(copy);
        if (color != null) {
            setCellColor(row, col, color);
        }
    }

    public void setCellColor(int row, int col, Color color) {
        if (!isValidIndex(row, col, cells)) {
            return;
        }
        alignColorsWithCells();
        colors[row][col] = color == null ? DEFAULT_CELL_COLOR.getRGB() : color.getRGB();
    }

    public void fillColors(Color color) {
        Color fill = color == null ? DEFAULT_CELL_COLOR : color;
        this.colors = buildColorGrid(this.cells, fill);
    }

    private void alignColorsWithCells() {
        this.colors = alignColorGrid(this.colors, this.cells);
    }

    private static boolean[][] copyGrid(boolean[][] source) {
        if (source == null) {
            return null;
        }
        boolean[][] copy = new boolean[source.length][];
        for (int i = 0; i < source.length; i++) {
            boolean[] row = source[i];
            copy[i] = row != null ? row.clone() : null;
        }
        return copy;
    }

    private static int[][] copyColorGrid(int[][] source) {
        if (source == null) {
            return null;
        }
        int[][] copy = new int[source.length][];
        for (int i = 0; i < source.length; i++) {
            int[] row = source[i];
            copy[i] = row != null ? row.clone() : null;
        }
        return copy;
    }

    private static int[][] buildColorGrid(boolean[][] base, Color fill) {
        if (base == null) {
            return null;
        }
        int[][] result = new int[base.length][];
        int rgb = (fill == null ? DEFAULT_CELL_COLOR : fill).getRGB();
        for (int i = 0; i < base.length; i++) {
            boolean[] row = base[i];
            int length = row != null ? row.length : 0;
            result[i] = new int[length];
            for (int j = 0; j < length; j++) {
                result[i][j] = rgb;
            }
        }
        return result;
    }

    private static int[][] alignColorGrid(int[][] source, boolean[][] base) {
        int[][] target = new int[base.length][];
        for (int i = 0; i < base.length; i++) {
            boolean[] baseRow = base[i];
            int length = baseRow != null ? baseRow.length : 0;
            target[i] = new int[length];
            for (int j = 0; j < length; j++) {
                int rgb = DEFAULT_CELL_COLOR.getRGB();
                if (source != null && i < source.length && source[i] != null && j < source[i].length) {
                    rgb = source[i][j];
                }
                target[i][j] = rgb;
            }
        }
        return target;
    }

    private static int[][] alignColorGrid(Color[][] source, boolean[][] base) {
        if (source == null) {
            return alignColorGrid((int[][]) null, base);
        }
        int[][] raw = new int[source.length][];
        for (int i = 0; i < source.length; i++) {
            Color[] row = source[i];
            raw[i] = new int[row == null ? 0 : row.length];
            for (int j = 0; row != null && j < row.length; j++) {
                raw[i][j] = (row[j] == null ? DEFAULT_CELL_COLOR : row[j]).getRGB();
            }
        }
        return alignColorGrid(raw, base);
    }

    private boolean isValidIndex(int row, int col, int[][] array) {
        return array != null
                && row >= 0 && row < array.length
                && array[row] != null
                && col >= 0 && col < array[row].length;
    }

    private boolean isValidIndex(int row, int col, boolean[][] array) {
        return array != null
                && row >= 0 && row < array.length
                && array[row] != null
                && col >= 0 && col < array[row].length;
    }
}

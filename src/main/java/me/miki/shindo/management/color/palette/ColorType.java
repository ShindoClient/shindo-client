package me.miki.shindo.management.color.palette;

public enum ColorType {
    DARK(0), NORMAL(1);

    private final int index;

    ColorType(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}

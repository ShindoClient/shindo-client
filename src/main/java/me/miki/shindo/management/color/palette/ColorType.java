package me.miki.shindo.management.color.palette;

public enum ColorType {
    DARK(0),
    MID(1),
    NORMAL(2);

    private final int index;

    ColorType(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}

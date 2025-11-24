package me.miki.shindo.gui.modmenu.category.impl.shared;

import me.miki.shindo.utils.mouse.MouseUtils;

public class FilterChip {

    private float x;
    private float y;
    private float width;
    private float height;
    private final Runnable onClick;

    public FilterChip(Runnable onClick) {
        this.onClick = onClick;
    }

    public void setBounds(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public boolean contains(int mx, int my) {
        return MouseUtils.isInside(mx, my, x, y, width, height);
    }

    public void click() {
        if (onClick != null) {
            onClick.run();
        }
    }
}

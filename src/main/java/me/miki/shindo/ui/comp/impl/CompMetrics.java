package me.miki.shindo.ui.comp.impl;

import lombok.Getter;
import lombok.Setter;
import me.miki.shindo.ui.comp.Comp;
import me.miki.shindo.utils.mouse.MouseUtils;

/**
 * Lightweight helper component that only carries sizing/bounds information.
 * Useful for chips, cards or layout calculations without duplicating bounding logic.
 */
public class CompMetrics extends Comp {

    @Getter
    @Setter
    private float padding = 0F;

    public CompMetrics() {
        super(0, 0);
    }

    public CompMetrics from(float x, float y, float width, float height) {
        setBounds(x, y, width, height);
        return this;
    }

    public CompMetrics clamp(float viewportWidth, float viewportHeight) {
        float clampedX = Math.max(0F, Math.min(getX(), viewportWidth));
        float clampedY = Math.max(0F, Math.min(getY(), viewportHeight));
        float clampedWidth = Math.max(0F, Math.min(getWidth(), viewportWidth - clampedX));
        float clampedHeight = Math.max(0F, Math.min(getHeight(), viewportHeight - clampedY));
        setBounds(clampedX, clampedY, clampedWidth, clampedHeight);
        return this;
    }

    public CompMetrics withPadding(float padding) {
        this.padding = padding;
        setBounds(getX() + padding, getY() + padding, Math.max(0F, getWidth() - (padding * 2F)), Math.max(0F, getHeight() - (padding * 2F)));
        return this;
    }

    public boolean isInside(int mouseX, int mouseY) {
        return MouseUtils.isInside(mouseX, mouseY, getX(), getY(), getWidth(), getHeight());
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        // Metrics component does not render; it simply holds bounds.
    }
}

package me.miki.shindo.ui.comp;

import lombok.Getter;
import lombok.Setter;
import me.miki.shindo.ui.framework.UIContext;
import me.miki.shindo.utils.mouse.MouseUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class Comp {


    @Setter
    @Getter
    private float x, y;
    @Setter
    @Getter
    private float width, height;
    @Setter
    @Getter
    private boolean visible = true;
    private final List<Comp> children = new ArrayList<>();

    public Comp(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void draw(int mouseX, int mouseY, float partialTicks) {
        if (!visible) {
            return;
        }
        drawChildren(mouseX, mouseY, partialTicks);
    }

    public void update(float partialTicks) {
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (!visible) {
            return;
        }
        forEachChild(child -> child.mouseClicked(mouseX, mouseY, mouseButton));
    }

    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        if (!visible) {
            return;
        }
        forEachChild(child -> child.mouseReleased(mouseX, mouseY, mouseButton));
    }

    public void keyTyped(char typedChar, int keyCode) {
        if (!visible) {
            return;
        }
        forEachChild(child -> child.keyTyped(typedChar, keyCode));
    }

    protected void drawChildren(int mouseX, int mouseY, float partialTicks) {
        forEachChild(child -> child.draw(mouseX, mouseY, partialTicks));
    }

    public void addChild(Comp comp) {
        if (comp != null && !children.contains(comp)) {
            children.add(comp);
        }
    }

    public void removeChild(Comp comp) {
        children.remove(comp);
    }

    public void clearChildren() {
        children.clear();
    }

    public List<Comp> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }

    public void setBounds(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public boolean isHovered(int mouseX, int mouseY) {
        return MouseUtils.isInside(mouseX, mouseY, x, y, width, height);
    }

    protected UIContext ctx() {
        return UIContext.get();
    }

    private void forEachChild(Consumer<Comp> consumer) {
        if (consumer == null) {
            return;
        }
        for (Comp child : children) {
            consumer.accept(child);
        }
    }
}

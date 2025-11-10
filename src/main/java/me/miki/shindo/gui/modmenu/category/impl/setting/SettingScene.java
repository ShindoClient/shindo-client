package me.miki.shindo.gui.modmenu.category.impl.setting;

import lombok.Getter;
import me.miki.shindo.gui.modmenu.category.impl.SettingCategory;
import me.miki.shindo.management.language.TranslateText;

public class SettingScene {

    private static final int HEADER_OFFSET = 56;

    private final SettingCategory parent;
    @Getter
    private final String icon;
    private final TranslateText nameTranslate;
    private final TranslateText descriptionTranslate;

    public SettingScene(SettingCategory parent, TranslateText nameTranslate, TranslateText descriptionTranslate, String icon) {
        this.parent = parent;
        this.nameTranslate = nameTranslate;
        this.descriptionTranslate = descriptionTranslate;
        this.icon = icon;
    }

    public void initGui() {
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
    }

    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
    }

    public void keyTyped(char typedChar, int keyCode) {
    }

    public String getName() {
        return nameTranslate.getText();
    }

    public String getDescription() {
        return descriptionTranslate.getText();
    }

    public int getX() {
        return parent.getSceneX();
    }

    public int getY() {
        return parent.getSceneY();
    }

    public int getWidth() {
        return parent.getSceneWidth();
    }

    public int getHeight() {
        return parent.getSceneHeight();
    }

    public int getContentY() {
        return parent.getSceneY() + HEADER_OFFSET;
    }

    public int getContentHeight() {
        return Math.max(0, parent.getSceneHeight() - HEADER_OFFSET);
    }

    public int getHeaderOffset() {
        return HEADER_OFFSET;
    }
}

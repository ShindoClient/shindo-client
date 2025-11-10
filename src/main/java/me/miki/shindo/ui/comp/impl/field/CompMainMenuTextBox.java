package me.miki.shindo.ui.comp.impl.field;

import lombok.Getter;
import lombok.Setter;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.management.nanovg.font.Fonts;
import me.miki.shindo.utils.ColorUtils;
import me.miki.shindo.utils.TimerUtils;
import me.miki.shindo.utils.animation.simple.SimpleAnimation;
import me.miki.shindo.ui.framework.UIContext;

import java.awt.*;

public class CompMainMenuTextBox extends CompTextBoxBase {

    private final TimerUtils timer = new TimerUtils();
    private final SimpleAnimation animation = new SimpleAnimation();
    @Setter
    @Getter
    private Color backgroundColor, fontColor;
    private String title = null;
    private String icon = null;


    @Setter
    private boolean passwordMode = false; // ✅ Adicionado

    public CompMainMenuTextBox(float x, float y, float width, float height) {
        super(x, y, width, height);
        this.backgroundColor = Color.WHITE;
        this.fontColor = Color.WHITE;
    }

    public CompMainMenuTextBox() {
        super(0, 0, 0, 0);
        this.backgroundColor = Color.WHITE;
        this.fontColor = Color.WHITE;
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {

        NanoVGManager nvg = UIContext.get().nvg();

        float height = this.getHeight();
        int selectionEnd = this.getSelectionEnd();
        int cursorPosition = this.getCursorPosition();
        String rawText = this.getText(); // ✅ Texto real
        String drawText = passwordMode ? repeat(rawText.length()) : rawText;
        boolean focused = this.isFocused();

        float addX = 0;
        float halfHeight = height / 2F;

        int outTextSize = 0;
        String resultText = "";

        for (char c : drawText.toCharArray()) {

            resultText = resultText + c;

            if (nvg.getTextWidth(resultText, halfHeight, Fonts.REGULAR) + halfHeight + 5 > this.getWidth()) {
                outTextSize++;

                addX = this.getWidth() - nvg.getTextWidth(resultText, halfHeight, Fonts.REGULAR) - halfHeight - 5;
            }
        }

        if (selectionEnd < outTextSize) {

            StringBuilder reversedText = new StringBuilder(drawText).reverse();

            addX = this.getWidth() - nvg.getTextWidth(reversedText.substring(outTextSize - selectionEnd), halfHeight, Fonts.REGULAR) - halfHeight - 5;
        }

        animation.setAnimation(!focused && rawText.isEmpty() ? 1.0F : 0.0F, 16);

        if (icon != null && title != null) {

            nvg.drawText(icon, this.getX() + 5, this.getY() + (this.getHeight() / 2) - (nvg.getTextHeight(drawText, halfHeight, Fonts.REGULAR) / 2), fontColor, halfHeight, Fonts.LEGACYICON);

            if (rawText.isEmpty()) {
                nvg.save();
                nvg.translate((animation.getValue() * 8) - 8, 0);
                nvg.drawText(title, this.getX() + 16, this.getY() + (this.getHeight() / 2) - (nvg.getTextHeight(drawText, halfHeight, Fonts.REGULAR) / 2) + 1, ColorUtils.applyAlpha(fontColor, (int) (animation.getValue() * 255)), halfHeight, Fonts.REGULAR);
                nvg.restore();
            }
        }

        nvg.drawRoundedRect(this.getX(), this.getY(), this.getWidth(), this.getHeight(), 4, backgroundColor);

        nvg.save();
        nvg.scissor(this.getX() + 1, this.getY(), this.getWidth() - 2, this.getHeight());

        addX = addX + ((title != null && icon != null) ? 16 : 5);

        if (cursorPosition != selectionEnd) {

            int start = Math.min(selectionEnd, cursorPosition);
            int end = Math.max(selectionEnd, cursorPosition);

            float selectionWidth = nvg.getTextWidth(drawText.substring(start, end), halfHeight, Fonts.REGULAR);
            float offset = nvg.getTextWidth(drawText.substring(0, start), halfHeight, Fonts.REGULAR);

            if (selectionWidth != 0) {
                nvg.drawRect(this.getX() + offset + addX - 1, this.getY() + (this.getHeight() / 2) - (nvg.getTextHeight(drawText, halfHeight, Fonts.REGULAR) / 2), selectionWidth, nvg.getTextHeight(drawText, halfHeight, Fonts.REGULAR), new Color(0, 135, 247));
            }
        }

        nvg.drawText(drawText, this.getX() + addX, this.getY() + (this.getHeight() / 2) - (nvg.getTextHeight(drawText, halfHeight, Fonts.REGULAR) / 2) + 1, fontColor, halfHeight, Fonts.REGULAR);

        if (timer.delay(600)) {

            float position = nvg.getTextWidth(drawText.substring(0, cursorPosition), halfHeight, Fonts.REGULAR);

            if (focused && cursorPosition == selectionEnd) {
                nvg.drawRect(this.getX() + addX + position,
                        this.getY() + (this.getHeight() / 2) - (nvg.getTextHeight(drawText, halfHeight, Fonts.REGULAR) / 2),
                        0.7F, 10, fontColor);
            }

            if (timer.delay(1200)) {
                timer.reset();
            }
        }

        nvg.restore();
        super.draw(mouseX, mouseY, partialTicks);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        super.keyTyped(typedChar, keyCode);
    }

    private String repeat(int count) {
        if (count <= 0) return "";
        StringBuilder builder = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            builder.append('*');
        }
        return builder.toString();
    }

    public void setEmptyText(String icon, String title) {
        this.icon = icon;
        this.title = title;
    }

}

package me.miki.shindo.ui.comp.impl;

import me.miki.shindo.management.color.palette.ColorPalette;
import me.miki.shindo.management.color.palette.ColorType;
import me.miki.shindo.management.settings.impl.TextSetting;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.management.nanovg.font.Fonts;
import me.miki.shindo.ui.comp.impl.field.CompTextBoxBase;
import me.miki.shindo.ui.framework.UIContext;
import me.miki.shindo.utils.TimerUtils;

import java.awt.*;

public class CompModTextBox extends CompTextBoxBase {

    private final TextSetting setting;
    private final TimerUtils timer = new TimerUtils();

    public CompModTextBox(float x, float y, float width, float height, TextSetting setting) {
        super(x, y, width, height);
        this.setting = setting;
        this.setText(setting.getText());
    }

    public CompModTextBox(TextSetting setting) {
        super(0, 0, 0, 0);
        this.setting = setting;
        this.setText(setting.getText());
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {

        UIContext ctx = ctx();
        NanoVGManager nvg = ctx.nvg();
        ColorPalette palette = ctx.palette();

        float height = this.getHeight();
        int selectionEnd = this.getSelectionEnd();
        int cursorPosition = this.getCursorPosition();
        String text = this.getText();
        boolean focused = this.isFocused();

        float addX = 0;
        float halfHeight = height / 2F;

        int outTextSize = 0;
        String resultText = "";

        for (char c : this.getText().toCharArray()) {

            resultText = resultText + c;

            if (nvg.getTextWidth(resultText, halfHeight, Fonts.REGULAR) + halfHeight + 5 > this.getWidth()) {
                outTextSize++;

                addX = this.getWidth() - nvg.getTextWidth(resultText, halfHeight, Fonts.REGULAR) - halfHeight - 5;
            }
        }

        if (selectionEnd < outTextSize) {

            StringBuilder reversedText = new StringBuilder(this.getText()).reverse();

            addX = this.getWidth() - nvg.getTextWidth(reversedText.substring(outTextSize - selectionEnd), halfHeight, Fonts.REGULAR) - halfHeight - 5;
        }

        nvg.drawRoundedRect(this.getX(), this.getY(), this.getWidth(), this.getHeight(), 4, palette.getBackgroundColor(ColorType.NORMAL));

        nvg.save();
        nvg.scissor(this.getX() + 1, this.getY(), this.getWidth() - 2, this.getHeight());

        if (cursorPosition != selectionEnd) {

            int start = Math.min(selectionEnd, cursorPosition);
            int end = Math.max(selectionEnd, cursorPosition);

            float selectionWidth = nvg.getTextWidth(this.getText().substring(start, end), halfHeight, Fonts.REGULAR);
            float offset = nvg.getTextWidth(this.getText().substring(0, start), halfHeight, Fonts.REGULAR);

            if (selectionWidth != 0) {
                nvg.drawRect(this.getX() + 4 + offset + addX, this.getY() + (this.getHeight() / 2) - (nvg.getTextHeight(text, halfHeight, Fonts.REGULAR) / 2), selectionWidth, nvg.getTextHeight(text, halfHeight, Fonts.REGULAR), new Color(0, 135, 247));
            }
        }

        nvg.drawText(this.getText(), this.getX() + 5 + addX, this.getY() + (this.getHeight() / 2) - (nvg.getTextHeight(text, halfHeight, Fonts.REGULAR) / 2) + 1, palette.getFontColor(ColorType.DARK), halfHeight, Fonts.REGULAR);

        if (timer.delay(600)) {

            float position = nvg.getTextWidth(this.getText(), halfHeight, Fonts.REGULAR) - nvg.getTextWidth(this.getText().substring(cursorPosition), halfHeight, Fonts.REGULAR);

            if (focused && cursorPosition == selectionEnd) {
                nvg.drawRect(this.getX() + 5 + addX + position,
                        this.getY() + (this.getHeight() / 2) - (nvg.getTextHeight(text, halfHeight, Fonts.REGULAR) / 2),
                        0.7F, 10, palette.getFontColor(ColorType.DARK));
            }

            if (timer.delay(1200)) {
                timer.reset();
            }
        }

        if (!focused) {
            setting.setText(this.getText());
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
}

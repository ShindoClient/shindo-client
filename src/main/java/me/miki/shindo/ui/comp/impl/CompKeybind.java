package me.miki.shindo.ui.comp.impl;

import lombok.Getter;
import me.miki.shindo.management.color.AccentColor;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.management.settings.impl.KeybindSetting;
import me.miki.shindo.management.nanovg.font.Fonts;
import me.miki.shindo.ui.comp.Comp;
import me.miki.shindo.ui.framework.UIContext;
import me.miki.shindo.utils.mouse.MouseUtils;
import org.lwjgl.input.Keyboard;

import java.awt.*;

public class CompKeybind extends Comp {

    private final KeybindSetting setting;
    private final float width;
    @Getter
    private boolean binding;

    public CompKeybind(float x, float y, float width, KeybindSetting setting) {
        super(x, y);
        this.setting = setting;
        this.width = width;
        setWidth(width);
        setHeight(16F);
    }

    public CompKeybind(float width, KeybindSetting setting) {
        super(0, 0);
        this.width = width;
        this.setting = setting;
        setWidth(width);
        setHeight(16F);
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {

        UIContext ctx = ctx();
        NanoVGManager nvg = ctx.nvg();
        AccentColor accentColor = ctx.accent();

        String info = binding ? "Binding..." : Keyboard.getKeyName(setting.getKeyCode());

        nvg.drawGradientRoundedRect(this.getX(), this.getY(), width, 16, 4, accentColor.getColor1(), accentColor.getColor2());

        nvg.drawCenteredText(info, this.getX() + (width / 2), this.getY() + 5F, new Color(255, 255, 255), 8, Fonts.REGULAR);

        super.draw(mouseX, mouseY, partialTicks);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (MouseUtils.isInside(mouseX, mouseY, this.getX(), this.getY(), width, 16) && mouseButton == 0) {
            binding = !binding;
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {

        if (binding) {

            if (keyCode == Keyboard.KEY_ESCAPE) {
                setting.setKeyCode(Keyboard.KEY_NONE);
                binding = false;
                return;
            }

            setting.setKeyCode(keyCode);
            binding = false;
        }

        super.keyTyped(typedChar, keyCode);
    }

}

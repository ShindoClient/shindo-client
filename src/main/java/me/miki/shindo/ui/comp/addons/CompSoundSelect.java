package me.miki.shindo.ui.comp.addons;

import me.miki.shindo.Shindo;
import me.miki.shindo.management.addons.settings.impl.SoundSetting;
import me.miki.shindo.management.color.AccentColor;
import me.miki.shindo.management.color.ColorManager;
import me.miki.shindo.management.color.palette.ColorPalette;
import me.miki.shindo.management.color.palette.ColorType;
import me.miki.shindo.management.file.FileManager;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.management.nanovg.font.Fonts;
import me.miki.shindo.management.nanovg.font.LegacyIcon;
import me.miki.shindo.ui.comp.Comp;
import me.miki.shindo.utils.Multithreading;
import me.miki.shindo.utils.file.FileUtils;
import me.miki.shindo.utils.mouse.MouseUtils;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class CompSoundSelect extends Comp {

    private final SoundSetting soundSetting;

    public CompSoundSelect(float x, float y, SoundSetting soundSetting) {
        super(x, y);
        this.soundSetting = soundSetting;
    }

    public CompSoundSelect(SoundSetting soundSetting) {
        super(0, 0);
        this.soundSetting = soundSetting;
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {

        Shindo instance = Shindo.getInstance();
        NanoVGManager nvg = instance.getNanoVGManager();
        ColorManager colorManager = instance.getColorManager();
        AccentColor accentColor = colorManager.getCurrentColor();
        ColorPalette palette = colorManager.getPalette();

        String name = soundSetting.getSound() == null ? TranslateText.NONE.getText() : soundSetting.getSound().getName();
        float nameWidth = nvg.getTextWidth(name, 9, Fonts.REGULAR);

        nvg.drawGradientRoundedRect(this.getX(), this.getY(), 16, 16, 4, accentColor.getColor1(), accentColor.getColor2());
        nvg.drawText(name, this.getX() - nameWidth - 5, this.getY() + 4, palette.getFontColor(ColorType.DARK), 9, Fonts.REGULAR);
        nvg.drawCenteredText(LegacyIcon.FOLDER, this.getX() + 8, this.getY() + 2.5F, Color.WHITE, 10, Fonts.LEGACYICON);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {

        if (MouseUtils.isInside(mouseX, mouseY, this.getX(), this.getY(), 16, 16) && mouseButton == 0) {

            Multithreading.runAsync(() -> {

                File sound = FileUtils.selectSoundFile();

                if (sound != null) {

                    FileManager fileManager = Shindo.getInstance().getFileManager();
                    File cacheDir = new File(fileManager.getCacheDir(), "custom-sound");

                    fileManager.createDir(cacheDir);

                    File newImage = new File(cacheDir, sound.getName());

                    try {
                        FileUtils.copyFile(sound, newImage);
                    } catch (IOException ignored) {
                    }

                    soundSetting.setSound(newImage);
                }
            });
        }
    }
}
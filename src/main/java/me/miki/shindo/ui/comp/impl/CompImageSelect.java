package me.miki.shindo.ui.comp.impl;

import me.miki.shindo.Shindo;
import me.miki.shindo.management.color.AccentColor;
import me.miki.shindo.management.color.ColorManager;
import me.miki.shindo.management.color.palette.ColorPalette;
import me.miki.shindo.management.color.palette.ColorType;
import me.miki.shindo.management.file.FileManager;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.settings.impl.ImageSetting;
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

public class CompImageSelect extends Comp {

    private final ImageSetting imageSetting;

    public CompImageSelect(float x, float y, ImageSetting imageSetting) {
        super(x, y);
        this.imageSetting = imageSetting;
    }

    public CompImageSelect(ImageSetting imageSetting) {
        super(0, 0);
        this.imageSetting = imageSetting;
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {

        Shindo instance = Shindo.getInstance();
        NanoVGManager nvg = instance.getNanoVGManager();
        ColorManager colorManager = instance.getColorManager();
        AccentColor accentColor = colorManager.getCurrentColor();
        ColorPalette palette = colorManager.getPalette();

        String name = imageSetting.getImage() == null ? TranslateText.NONE.getText() : imageSetting.getImage().getName();
        float nameWidth = nvg.getTextWidth(name, 9, Fonts.REGULAR);

        nvg.drawGradientRoundedRect(this.getX(), this.getY(), 16, 16, 4, accentColor.getColor1(), accentColor.getColor2());
        nvg.drawText(name, this.getX() - nameWidth - 5, this.getY() + 4, palette.getFontColor(ColorType.DARK), 9, Fonts.REGULAR);
        nvg.drawCenteredText(LegacyIcon.FOLDER, this.getX() + 8, this.getY() + 2.5F, Color.WHITE, 10, Fonts.LEGACYICON);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {

        if (MouseUtils.isInside(mouseX, mouseY, this.getX(), this.getY(), 16, 16) && mouseButton == 0) {

            Multithreading.runAsync(() -> {

                File image = FileUtils.selectImageFile();

                if (image != null) {

                    FileManager fileManager = Shindo.getInstance().getFileManager();
                    File cacheDir = new File(fileManager.getCacheDir(), "custom-image");

                    fileManager.createDir(cacheDir);

                    File newImage = new File(cacheDir, image.getName());

                    try {
                        FileUtils.copyFile(image, newImage);
                    } catch (IOException e) {
                    }

                    imageSetting.setImage(newImage);
                }
            });
        }
    }
}

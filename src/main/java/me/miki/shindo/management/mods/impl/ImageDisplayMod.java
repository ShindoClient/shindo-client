package me.miki.shindo.management.mods.impl;

import me.miki.shindo.Shindo;
import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventRender2D;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.HUDMod;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.management.settings.config.Property;
import me.miki.shindo.management.settings.config.PropertyType;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
public class ImageDisplayMod extends HUDMod {

    @Property(type = PropertyType.NUMBER, translate = TranslateText.RADIUS, min = 2, max = 64, current = 6, step = 1)
    private int radiusSetting = 6;

    @Property(type = PropertyType.NUMBER, translate = TranslateText.ALPHA, min = 0.0F, max = 1.0F, current = 1.0F)
    private double alphaSetting = 1.0F;

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.SHADOW)
    private boolean shadowSetting;

    @Property(type = PropertyType.IMAGE, translate = TranslateText.IMAGE)
    private File imageFile;

    private BufferedImage image;
    private File prevImage;

    public ImageDisplayMod() {
        super(TranslateText.IMAGE_DISPLAY, TranslateText.IMAGE_DISPLAY_DESCRIPTION);
    }

    @EventTarget
    public void onRender2D(EventRender2D event) {

        NanoVGManager nvg = Shindo.getInstance().getNanoVGManager();

        nvg.setupAndDraw(() -> drawNanoVG());
    }

    private void drawNanoVG() {

        if (imageFile != null && !imageFile.equals(prevImage)) {
            prevImage = imageFile;
            try {
                image = ImageIO.read(imageFile);
            } catch (IOException e) {
            }
        }

        if (image != null) {

            int width = image.getWidth();
            int height = image.getHeight();

            if (width > 500 || height > 500) {

                if ((width < 1000 || height < 1000)) {
                    width = width / 2;
                    height = height / 2;
                }

                if ((width > 1000 || height > 1000)) {
                    width = width / 3;
                    height = height / 3;
                }
            }

            if (shadowSetting) {
                this.drawShadow(0, 0, width, height, radiusSetting);
            }

            this.drawRoundedImage(imageFile, 0, 0, width, height, radiusSetting, (float) alphaSetting);

            this.setWidth(width);
            this.setHeight(height);
        }
    }
}

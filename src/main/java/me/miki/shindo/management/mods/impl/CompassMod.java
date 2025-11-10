package me.miki.shindo.management.mods.impl;

import me.miki.shindo.Shindo;
import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventRender2D;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.settings.config.Property;
import me.miki.shindo.management.settings.config.PropertyEnum;
import me.miki.shindo.management.settings.config.PropertyType;
import me.miki.shindo.management.mods.SimpleHUDMod;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.management.nanovg.font.LegacyIcon;
import me.miki.shindo.utils.buffer.ScreenStencil;
import net.minecraft.util.MathHelper;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NanoVG;

import java.awt.*;

public class CompassMod extends SimpleHUDMod {

    private final ScreenStencil stencil = new ScreenStencil();

    @Property(type = PropertyType.COMBO, translate = TranslateText.DESIGN)
    private Design design = Design.SIMPLE;

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.ICON, category = "Display")
    private boolean iconSetting = true;

    @Property(type = PropertyType.NUMBER, translate = TranslateText.WIDTH, category = "Layout", min = 50, max = 450, step = 1, current = 180)
    private int widthSetting = 180;

    public CompassMod() {
        super(TranslateText.COMPASS, TranslateText.COMPASS_DESCRIPTION);
    }

    @EventTarget
    public void onRender2D(EventRender2D event) {

        NanoVGManager nvg = Shindo.getInstance().getNanoVGManager();

        if (design == Design.SIMPLE) {
            this.draw();
        } else {
            nvg.setupAndDraw(() -> {
                this.drawBackground(widthSetting, 29);
            });
            stencil.wrap(() -> drawNanoVG(), this.getX(), this.getY(), this.getWidth(), this.getHeight(), 6 * this.getScale());
        }
    }

    private void drawNanoVG() {

        int angle = (int) MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw) * -1 - 360;
        int angle2 = (int) MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw) * -1 - 360;
        int angle3 = (int) MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw) * -1 - 360;
        int angle4 = (int) MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw) * -1 - 360;
        int angle5 = (int) MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw) * -1 - 360;
        int angle6 = (int) MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw) * -1 - 360;
        int width = widthSetting;

        this.renderMarker(this.getX() + ((width / 2) * this.getScale()), this.getY() + (2.5F * this.getScale()), this.getFontColor());

        for (int i = 0; i <= 2; i++) {

            for (double d = 0.0D; d <= 1.5D; d += 0.5D) {

                String s = "W";

                if (d == 0.0D) {
                    s = "S";
                }

                if (d == 1.0D) {
                    s = "N";
                }

                if (d == 1.5D) {
                    s = "E";
                }

                this.drawRect((width / 2) + angle - 2F, 8, 0.8F, 9);

                this.drawRect((width / 2) + angle + 12F, 8, 0.8F, 6);
                this.drawRect((width / 2) + angle + 26F, 8, 0.8F, 6);

                this.drawRect((width / 2) + angle - 16F, 8, 0.8F, 6);
                this.drawRect((width / 2) + angle - 30F, 8, 0.8F, 6);

                this.drawCenteredText(s, (width / 2) + angle - 1.5F, 19, 8.5F, getHudFont(2));

                angle += 90;
            }

            for (double d = 0.0D; d <= 1.5D; d += 0.5D) {

                String s = "NW";

                if (d == 0.0D) {
                    s = "SW";
                }

                if (d == 1.0D) {
                    s = "NE";
                }

                if (d == 1.5D) {
                    s = "SE";
                }


                this.drawCenteredText(s, (width / 2) + angle2 + 43F, 8.5F, 6.8F, getHudFont(1));

                angle2 += 90;
            }

            for (double d = 0.0D; d <= 1.5D; d += 0.5D) {

                String s = "105";

                if (d == 0.0D) {
                    s = "15";
                }

                if (d == 1.0D) {
                    s = "195";
                }

                if (d == 1.5D) {
                    s = "285";
                }

                this.drawCenteredText(s, (width / 2) + angle3 + 13F, 17, 5.4F, getHudFont(1));

                angle3 += 90;
            }

            for (double d = 0.0D; d <= 1.5D; d += 0.5D) {

                String s = "120";

                if (d == 0.0D) {
                    s = "30";
                }

                if (d == 1.0D) {
                    s = "210";
                }

                if (d == 1.5D) {
                    s = "300";
                }

                this.drawCenteredText(s, (width / 2) + angle4 + 27F, 17, 5.4F, getHudFont(1));

                angle4 += 90;
            }

            for (double d = 0.0D; d <= 1.5D; d += 0.5D) {

                String s = "150";

                if (d == 0.0D) {
                    s = "60";
                }

                if (d == 1.0D) {
                    s = "240";
                }

                if (d == 1.5D) {
                    s = "300";
                }

                this.drawCenteredText(s, (width / 2) + angle5 + 60.5F, 17, 5.4F, getHudFont(1));

                angle5 += 90;
            }

            for (double d = 0.0D; d <= 1.5D; d += 0.5D) {

                String s = "165";

                if (d == 0.0D) {
                    s = "70";
                }

                if (d == 1.0D) {
                    s = "255";
                }

                if (d == 1.5D) {
                    s = "345";
                }

                this.drawCenteredText(s, (width / 2) + angle6 + 74.5F, 17, 5.4F, getHudFont(1));

                angle6 += 90;
            }
        }

        this.setWidth(width);
        this.setHeight(29);
    }

    private void renderMarker(float x, float y, Color color) {

        NanoVGManager nvg = Shindo.getInstance().getNanoVGManager();
        long vg = nvg.getContext();
        NVGColor nvgColor = nvg.getColor(color);
        float scale = this.getScale();

        NanoVG.nvgBeginPath(vg);
        NanoVG.nvgMoveTo(vg, x, y + (4 * scale));
        NanoVG.nvgLineTo(vg, x + (4 * scale), y);
        NanoVG.nvgLineTo(vg, x - (4 * scale), y);
        NanoVG.nvgClosePath(vg);

        NanoVG.nvgFillColor(vg, nvgColor);
        NanoVG.nvgFill(vg);
    }

    @Override
    public String getText() {

        String s = "Direction: ";
        double rotation = (mc.thePlayer.rotationYawHead - 90) % 360;

        if (rotation < 0) {
            rotation += 360.0;
        }

        if (0 <= rotation && rotation < 22.5) {
            return s + "W";
        } else if (22.5 <= rotation && rotation < 67.5) {
            return s + "NW";
        } else if (67.5 <= rotation && rotation < 112.5) {
            return s + "N";
        } else if (112.5 <= rotation && rotation < 157.5) {
            return s + "NE";
        } else if (157.5 <= rotation && rotation < 202.5) {
            return s + "E";
        } else if (202.5 <= rotation && rotation < 247.5) {
            return s + "SE";
        } else if (247.5 <= rotation && rotation < 292.5) {
            return s + "S";
        } else if (292.5 <= rotation && rotation < 337.5) {
            return s + "SW";
        } else if (337.5 <= rotation && rotation < 360.0) {
            return s + "W";
        }

        return s + "Error";
    }

    @Override
    public String getIcon() {
        return iconSetting ? LegacyIcon.COMPASS : null;
    }

    private enum Design implements PropertyEnum {
        SIMPLE(TranslateText.SIMPLE),
        FANCY(TranslateText.FANCY);

        private final TranslateText translate;

        Design(TranslateText translate) {
            this.translate = translate;
        }

        @Override
        public TranslateText getTranslate() {
            return translate;
        }
    }
}

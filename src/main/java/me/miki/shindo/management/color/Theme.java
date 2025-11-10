package me.miki.shindo.management.color;

import me.miki.shindo.utils.ColorUtils;
import me.miki.shindo.utils.animation.simple.SimpleAnimation;

import java.awt.*;

public enum Theme {
    DARK(0, "Dark", new Color(19, 19, 20), new Color(34, 35, 39), new Color(255, 255, 255), new Color(235, 235, 235)),
    LIGHT(1, "Light", new Color(254, 254, 254), new Color(238, 238, 238), new Color(54, 54, 54), new Color(107, 117, 129)),
    DARK_BLUE(2, "Dark Blue", new Color(22, 28, 41), new Color(27, 36, 52), new Color(157, 175, 211), new Color(116, 131, 164)),
    MIDNIGHT(3, "Midnight", new Color(47, 54, 61), new Color(36, 41, 46), new Color(255, 255, 255), new Color(235, 235, 235)),
    DARK_PURPLE(4, "Dark Purple", new Color(44, 14, 72), new Color(53, 24, 90), new Color(234, 226, 252), new Color(194, 186, 212)),
    SEA(5, "Sea", new Color(203, 224, 255), new Color(190, 216, 238), new Color(32, 32, 32), new Color(106, 106, 106)),
    SAKURA(6, "Sakura", new Color(255, 191, 178), new Color(255, 223, 226), new Color(35, 35, 35), new Color(80, 80, 80)),
    CATPPUCCIN_MOCHA(7, "Catppuccin Mocha", new Color(49, 50, 68), new Color(30, 30, 46), new Color(205, 214, 244), new Color(245, 194, 231)),
    CATPPUCCIN_LATTE(8, "Catppuccin Latte", new Color(230, 233, 239), new Color(239, 241, 245), new Color(76, 79, 105), new Color(140, 143, 161)),
    BIRD(9, "Twoot twoot", new Color(25, 40, 52), new Color(20, 32, 43), new Color(255, 255, 255), new Color(136, 153, 171)),
    CALIFORNIA(10, "California", new Color(22, 22, 25), new Color(0, 0, 0), new Color(230, 230, 230), new Color(130, 130, 130)),
    LAVENDER(11, "Lavender", new Color(228, 229, 241), new Color(250, 250, 250), new Color(72, 75, 105), new Color(147, 148, 165)),
    CAMELLIA(12, "Camellia", new Color(30, 31, 36), new Color(23, 24, 28), new Color(228, 229, 231), new Color(250, 56, 103)),
    TERMINAL(13, "Terminal", new Color(7, 7, 7), new Color(12, 12, 12), new Color(33, 96, 7), new Color(54, 73, 0)),
    NORD(14, "Nord", new Color(59, 66, 82), new Color(46, 52, 64), new Color(236, 239, 244), new Color(216, 222, 233)),
    GRUVBOX(15, "Gruvbox Dark Med", new Color(0x3C3836), new Color(0x282828), new Color(0xEBDBB2), new Color(0xA89984));

    private final SimpleAnimation animation = new SimpleAnimation();

    private final String name;
    private final int id;
    private final Color darkBackgroundColor;
    private final Color midBackgroundColor;
    private final Color normalBackgroundColor;
    private final Color darkFontColor;
    private final Color normalFontColor;
    private final Color midFontColor;

    Theme(int id, String name, Color darkBackgroundColor, Color normalBackgroundColor,
          Color darkFontColor, Color normalFontColor) {
        this.name = name;
        this.id = id;
        Color adjustedDark = ColorUtils.darken(darkBackgroundColor, 0.12F);
        Color adjustedNormal = ColorUtils.lighten(normalBackgroundColor, 0.10F);
        this.darkBackgroundColor = adjustedDark;
        this.normalBackgroundColor = adjustedNormal;
        this.midBackgroundColor = ColorUtils.interpolateColor(adjustedDark, adjustedNormal, 0.5D);
        this.darkFontColor = darkFontColor;
        this.normalFontColor = normalFontColor;
        this.midFontColor = ColorUtils.interpolateColor(darkFontColor, normalFontColor, 0.5D);
    }

    public static Theme getThemeById(int id) {

        for (Theme t : Theme.values()) {
            if (t.getId() == id) {
                return t;
            }
        }

        return LIGHT;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public Color getDarkBackgroundColor() {
        return darkBackgroundColor;
    }

    public Color getMidBackgroundColor() {
        return midBackgroundColor;
    }

    public Color getNormalBackgroundColor() {
        return normalBackgroundColor;
    }

    public Color getDarkFontColor() {
        return darkFontColor;
    }

    public Color getMidFontColor() {
        return midFontColor;
    }

    public Color getNormalFontColor() {
        return normalFontColor;
    }

    public Color getDarkBackgroundColor(int alpha) {
        return ColorUtils.applyAlpha(darkBackgroundColor, alpha);
    }

    public Color getMidBackgroundColor(int alpha) {
        return ColorUtils.applyAlpha(midBackgroundColor, alpha);
    }

    public Color getNormalBackgroundColor(int alpha) {
        return ColorUtils.applyAlpha(normalBackgroundColor, alpha);
    }

    public Color getDarkFontColor(int alpha) {
        return ColorUtils.applyAlpha(darkFontColor, alpha);
    }

    public Color getMidFontColor(int alpha) {
        return ColorUtils.applyAlpha(midFontColor, alpha);
    }

    public Color getNormalFontColor(int alpha) {
        return ColorUtils.applyAlpha(normalFontColor, alpha);
    }

    public SimpleAnimation getAnimation() {
        return animation;
    }
}

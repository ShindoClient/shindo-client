package me.miki.shindo.management.mods;

import eu.shoroa.contrib.render.ShBlur;
import lombok.Getter;
import lombok.Setter;
import me.miki.shindo.Shindo;
import me.miki.shindo.gui.GuiEditHUD;
import me.miki.shindo.management.color.AccentColor;
import me.miki.shindo.management.color.ColorManager;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.impl.InternalSettingsMod;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.management.nanovg.font.Font;
import me.miki.shindo.management.nanovg.font.Fonts;
import me.miki.shindo.utils.ColorUtils;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.io.File;

@Getter
public class HUDMod extends Mod {



    @Setter
    private int x, y, draggingX, draggingY, width, height;

    private float scale;

    @Setter
    private boolean dragging, draggable;

    public HUDMod(TranslateText nameTranslate, TranslateText descriptionText) {
        super(nameTranslate, descriptionText, ModCategory.HUD);

        this.x = 100;
        this.y = 100;
        this.width = 100;
        this.height = 100;
        this.scale = 1.0F;
        this.draggable = true;
    }

    public HUDMod(TranslateText nameTranslate, TranslateText descriptionText, String alias) {
        super(nameTranslate, descriptionText, ModCategory.HUD, alias);

        this.x = 100;
        this.y = 100;
        this.width = 100;
        this.height = 100;
        this.scale = 1.0F;
        this.draggable = true;
    }

    public HUDMod(TranslateText nameTranslate, TranslateText descriptionText, String alias, boolean restricted) {
        super(nameTranslate, descriptionText, ModCategory.HUD, alias, restricted);

        this.x = 100;
        this.y = 100;
        this.width = 100;
        this.height = 100;
        this.scale = 1.0F;
        this.draggable = true;
    }

    public void save() {

        Shindo instance = Shindo.getInstance();
        NanoVGManager nvg = instance.getNanoVGManager();

        nvg.save();
    }

    public void restore() {

        Shindo instance = Shindo.getInstance();
        NanoVGManager nvg = instance.getNanoVGManager();

        nvg.restore();
    }

    public void scissor(float addX, float addY, float width, float height) {

        Shindo instance = Shindo.getInstance();
        NanoVGManager nvg = instance.getNanoVGManager();

        nvg.scissor(x + (addX * scale), y + (addY * scale), width * scale, height * scale);
    }

    public void drawPlayerHead(ResourceLocation location, float addX, float addY, float width, float height, float radius) {

        Shindo instance = Shindo.getInstance();
        NanoVGManager nvg = instance.getNanoVGManager();

        nvg.drawPlayerHead(location, x + (addX * scale), y + (addY * scale), width * scale, height * scale, radius * scale);
    }

    public void drawImage(ResourceLocation location, float addX, float addY, float width, float height) {
        Shindo instance = Shindo.getInstance();
        NanoVGManager nvg = instance.getNanoVGManager();

        nvg.drawImage(location, addX, addY, width, height);
    }

    public void drawRoundedImage(int texture, float addX, float addY, float width, float height, float radius) {

        Shindo instance = Shindo.getInstance();
        NanoVGManager nvg = instance.getNanoVGManager();

        nvg.drawRoundedImage(texture, x + (addX * scale), y + (addY * scale), width * scale, height * scale, radius * scale);
    }

    public void drawRoundedImage(File file, float addX, float addY, float width, float height, float radius, float alpha) {

        Shindo instance = Shindo.getInstance();
        NanoVGManager nvg = instance.getNanoVGManager();

        nvg.drawRoundedImage(file, x + (addX * scale), y + (addY * scale), width * scale, height * scale, radius * scale, alpha);
    }

    public void drawRoundedImage(File file, float addX, float addY, float width, float height, float radius) {
        drawRoundedImage(file, addX, addY, width, height, radius, 1.0F);
    }

    public void drawRoundedImage(ResourceLocation location, float addX, float addY, float width, float height, float radius) {

        Shindo instance = Shindo.getInstance();
        NanoVGManager nvg = instance.getNanoVGManager();

        nvg.drawRoundedImage(location, x + (addX * scale), y + (addY * scale), width * scale, height * scale, radius * scale);
    }

    public void drawArc(float addX, float addY, float radius, float startAngle, float endAngle, float strokeWidth, Color color) {

        Shindo instance = Shindo.getInstance();
        NanoVGManager nvg = instance.getNanoVGManager();

        nvg.drawArc(x + (addX * scale), y + (addY * scale), radius * scale, startAngle, endAngle, strokeWidth * scale, color);
    }

    public void drawArc(float addX, float addY, float radius, float startAngle, float endAngle, float strokeWidth) {
        drawArc(addX, addY, radius, startAngle, endAngle, strokeWidth, this.getFontColor());
    }

    public void drawShadow(float addX, float addY, float width, float height, float radius) {

        Shindo instance = Shindo.getInstance();
        NanoVGManager nvg = instance.getNanoVGManager();

        nvg.drawShadow(x + (addX * scale), y + (addY * scale), width * scale, height * scale, radius * scale);
    }

    public void drawRect(float addX, float addY, float width, float height, Color color) {

        Shindo instance = Shindo.getInstance();
        NanoVGManager nvg = instance.getNanoVGManager();

        nvg.drawRect(x + (addX * scale), y + (addY * scale), width * scale, height * scale, color);
    }

    public void drawRect(float addX, float addY, float width, float height) {
        drawRect(addX, addY, width, height, this.getFontColor());
    }

    public void drawRoundedRect(float addX, float addY, float width, float height, float radius, Color color) {

        Shindo instance = Shindo.getInstance();
        NanoVGManager nvg = instance.getNanoVGManager();

        if (width < 0) {
            return;
        }

        if (height < 0) {
            return;
        }

        nvg.drawRoundedRect(x + (addX * scale), y + (addY * scale), width * scale, height * scale, radius * scale, color);
    }

    public void drawRoundedRect(float addX, float addY, float width, float height, float radius) {
        drawRoundedRect(addX, addY, width, height, radius, this.getFontColor());
    }

    public void drawBackground(float addX, float addY, float width, float height, float radius) {

        Shindo instance = Shindo.getInstance();
        NanoVGManager nvg = instance.getNanoVGManager();
        ColorManager colorManager = instance.getColorManager();
        AccentColor currentColor = colorManager.getCurrentColor();
        InternalSettingsMod.HudTheme theme = InternalSettingsMod.getInstance().getHudTheme();

        boolean isNormal = theme == InternalSettingsMod.HudTheme.NORMAL;
        boolean isVanilla = theme == InternalSettingsMod.HudTheme.VANILLA;
        boolean isGlow = theme == InternalSettingsMod.HudTheme.GLOW;
        boolean isVanillaGlow = theme == InternalSettingsMod.HudTheme.VANILLA_GLOW;
        boolean isOutline = theme == InternalSettingsMod.HudTheme.OUTLINE;
        boolean isOutlineGlow = theme == InternalSettingsMod.HudTheme.OUTLINE_GLOW;
        boolean isShadow = theme == InternalSettingsMod.HudTheme.SHADOW;
        boolean isDark = theme == InternalSettingsMod.HudTheme.DARK;
        boolean isLight = theme == InternalSettingsMod.HudTheme.LIGHT;
        boolean isRect = theme == InternalSettingsMod.HudTheme.RECT;
        boolean isModern = theme == InternalSettingsMod.HudTheme.MODERN;
        boolean isSimpGrad = theme == InternalSettingsMod.HudTheme.GRADIENT_SIMPLE;
        boolean isBlur = InternalSettingsMod.getInstance().getBlurSetting().isToggled();

        float lastWidth = width * scale;
        float lastHeight = height * scale;
        float x = this.x + (addX * scale);
        float y = this.y + (addY * scale);

        if (isBlur) ShBlur.getInstance().drawBlur(x, y, lastWidth, lastHeight, radius);

        if (isNormal || isVanilla || isShadow || isDark || isLight || isModern) {
            nvg.drawShadow(x, y, lastWidth, lastHeight, radius - 0.75F);
        } else if (isGlow || isVanillaGlow) {
            nvg.drawGradientShadow(x, y, lastWidth, lastHeight, radius, currentColor.getColor1(), currentColor.getColor2());
        } else if (isOutline || isOutlineGlow) {
            if (isOutline) {
                nvg.drawShadow(x - 2, y - 2, lastWidth + 4, lastHeight + 4, radius + 2);
            } else if (isOutlineGlow) {
                nvg.drawGradientShadow(x - 2, y - 2, lastWidth + 4, lastHeight + 4, radius + 2, currentColor.getColor1(), currentColor.getColor2());
            }
        }

        if (isOutline || isOutlineGlow) {
            nvg.drawGradientOutlineRoundedRect(x - 1, y - 1, lastWidth + 2, lastHeight + 2, radius + 1, 1.5F, currentColor.getColor1(), currentColor.getColor2());
        }

        if (isVanilla || isVanillaGlow || isOutline || isOutlineGlow) {
            nvg.drawRoundedRect(x, y, lastWidth, lastHeight, radius, new Color(0, 0, 0, 100));
        } else if (isNormal || isGlow) {
            nvg.drawGradientRoundedRect(x, y, lastWidth, lastHeight, radius, ColorUtils.applyAlpha(currentColor.getColor1(), 220), ColorUtils.applyAlpha(currentColor.getColor2(), 220));
        } else if (isLight) {
            nvg.drawRoundedRect(x, y, lastWidth, lastHeight, radius, new Color(240, 240, 240, 220));
        } else if (isDark) {
            nvg.drawRoundedRect(x, y, lastWidth, lastHeight, radius, new Color(20, 20, 20, 220));
        }
        if (isRect || isSimpGrad) {
            nvg.drawRect(x, y, lastWidth, lastHeight, new Color(20, 20, 20, 165));
        }
        if (isSimpGrad) {
            nvg.drawHorizontalGradientRect(x, y - (2 * scale), lastWidth, (2 * scale), ColorUtils.interpolateColors(8, 0, currentColor.getColor1(), currentColor.getColor2()), ColorUtils.interpolateColors(10, 20, currentColor.getColor1(), currentColor.getColor2()));
        }
        if (isModern) {
            nvg.drawRoundedRect(x, y, lastWidth, lastHeight, radius, new Color(0, 0, 0, 110));
            nvg.drawOutlineRoundedRect(x - 0.5F, y - 0.5F, lastWidth + 1, lastHeight + 1, radius + 0.5F, 0.7F, new Color(255, 255, 255, 110));
        }

    }

    public void drawBackground(float width, float height) {
        drawBackground(0, 0, width, height, 6 * scale);
    }

    public void drawBackground(float addX, float addY, float width, float height) {
        drawBackground(addX, addY, width, height, 6 * scale);
    }

    public void drawBackground(float width, float height, float radius) {
        drawBackground(0, 0, width, height, radius);
    }

    public void drawText(String text, float addX, float addY, float size, Font font, Color color) {

        if (font == Fonts.MOJANGLES) {
            addX = addX - 0.5F;
            addY = addY - 1.3F;
        }

        NanoVGManager nvg = Shindo.getInstance().getNanoVGManager();
        float lastSize = size * scale;
        InternalSettingsMod.HudTheme theme = InternalSettingsMod.getInstance().getHudTheme();
        boolean isText = theme == InternalSettingsMod.HudTheme.TEXT;

        if (isText) {
            nvg.save();
            nvg.fontBlur(20);
            nvg.drawText(text, x + (addX * scale), y + (addY * scale), new Color(0, 0, 0, 150), lastSize, font);
            nvg.restore();
        }

        nvg.drawText(text, x + (addX * scale), y + (addY * scale), new Color(color.getRed(), color.getGreen(), color.getBlue(), 180), lastSize, font);
    }

    public void scale(float addX, float addY, float width, float height, float nvgScale) {

        NanoVGManager nvg = Shindo.getInstance().getNanoVGManager();

        nvg.scale(x + (addX * scale), y + (addY * scale), width * scale, height * scale, nvgScale);
    }

    public void drawText(String text, float addX, float addY, float size, Font font) {
        drawText(text, addX, addY, size, font, getFontColor());
    }

    public void drawCenteredText(String text, float addX, float addY, float size, Font font, Color color) {

        NanoVGManager nvg = Shindo.getInstance().getNanoVGManager();
        float lastSize = size * scale;

        nvg.drawCenteredText(text, x + (addX * scale), y + (addY * scale), color, lastSize, font);
    }

    public void drawCenteredText(String text, float addX, float addY, float size, Font font) {
        drawCenteredText(text, addX, addY, size, font, getFontColor());
    }

    public float getTextWidth(String text, float size, Font font) {

        NanoVGManager nvg = Shindo.getInstance().getNanoVGManager();

        return nvg.getTextWidth(text, size, font);
    }

    public String getLimitText(String text, float size, Font font, float maxWidth) {

        NanoVGManager nvg = Shindo.getInstance().getNanoVGManager();

        return nvg.getLimitText(text, size, font, maxWidth);
    }

    public Color getFontColor(int alpha) {

        InternalSettingsMod.HudTheme theme = InternalSettingsMod.getInstance().getHudTheme();

        boolean isDark = theme == InternalSettingsMod.HudTheme.DARK;
        boolean isLight = theme == InternalSettingsMod.HudTheme.LIGHT;

        if (isDark || isLight) {
            return Shindo.getInstance().getColorManager().getCurrentColor().getInterpolateColor(alpha);
        }

        return new Color(255, 255, 255, alpha);
    }

    public Color getFontColor() {
        return getFontColor(255);
    }

    public boolean isEditing() {
        return mc.currentScreen instanceof GuiEditHUD;
    }

    public int getWidth() {
        return (int) (width * scale);
    }

    public int getHeight() {
        return (int) (height * scale);
    }

    public void setScale(float scale) {

        if (scale > 5.0 || scale < 0.2) {

            if (scale > 5.0) {
                this.scale = 5.0F;
            }

            if (scale < 0.2) {
                this.scale = 0.2F;
            }

            return;
        }

        this.scale = scale;
    }

    public Font getHudFont(int in) {
        if (InternalSettingsMod.getInstance().getMCHUDFont().isToggled()) return Fonts.MOJANGLES;
        switch (in) {
            case 1:
                return Fonts.REGULAR;
            case 2:
                return Fonts.MEDIUM;
            case 3:
                return Fonts.SEMIBOLD;
            default:
                return Fonts.REGULAR;
        }
    }

}

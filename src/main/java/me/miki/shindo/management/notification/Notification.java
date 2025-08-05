package me.miki.shindo.management.notification;

import me.miki.shindo.Shindo;
import me.miki.shindo.management.color.AccentColor;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.management.nanovg.font.Fonts;
import me.miki.shindo.utils.ColorUtils;
import me.miki.shindo.utils.TimerUtils;
import me.miki.shindo.utils.animation.normal.Animation;
import me.miki.shindo.utils.animation.normal.Direction;
import me.miki.shindo.utils.animation.normal.easing.EaseBackIn;
import me.miki.shindo.utils.buffer.ScreenAlpha;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;

public class Notification {

    private final String title;
    private final String message;
    private final NotificationType type;
    private final TimerUtils timer;
    private final ScreenAlpha screenAlpha = new ScreenAlpha();
    private Animation animation;

    public Notification(TranslateText title, TranslateText message, NotificationType type) {
        this.title = title.getText();
        this.message = message.getText();
        this.type = type;
        this.timer = new TimerUtils();
    }

    public Notification(String title, String message, NotificationType type) {
        this.title = title;
        this.message = message;
        this.type = type;
        this.timer = new TimerUtils();
    }

    public Notification(TranslateText title, String message, NotificationType type) {
        this.title = title.getText();
        this.message = message;
        this.type = type;
        this.timer = new TimerUtils();
    }


    public void draw() {

        NanoVGManager nvg = Shindo.getInstance().getNanoVGManager();

        screenAlpha.wrap(() -> drawNanoVG(nvg), animation.getValueFloat());
    }

    private void drawNanoVG(NanoVGManager nvg) {

        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        Shindo instance = Shindo.getInstance();
        AccentColor currentColor = instance.getColorManager().getCurrentColor();

        float maxWidth;
        float titleWidth = nvg.getTextWidth(title, 9.6F, Fonts.MEDIUM);
        float messageWidth = nvg.getTextWidth(message, 7.6F, Fonts.REGULAR);

        maxWidth = Math.max(titleWidth, messageWidth);

        maxWidth = maxWidth + 31;

        int x = (int) (sr.getScaledWidth() - maxWidth) - 8;
        int y = sr.getScaledHeight() - 29 - 8;

        if (timer.delay(3000)) {
            animation.setDirection(Direction.BACKWARDS);
        }

        nvg.save();
        nvg.translate(160 - (animation.getValueFloat() * 160), 0);

        nvg.drawShadow(x, y, maxWidth, 29, 6);
        nvg.drawGradientRoundedRect(x, y, maxWidth, 29, 6, ColorUtils.applyAlpha(currentColor.getColor1(), 220), ColorUtils.applyAlpha(currentColor.getColor2(), 220));
        nvg.drawText(type.getIcon(), x + 5, y + 6F, Color.WHITE, 17, Fonts.LEGACYICON);
        nvg.drawText(title, x + 26, y + 6F, Color.white, 9.6F, Fonts.MEDIUM);
        nvg.drawText(message, x + 26, y + 17.5F, Color.WHITE, 7.5F, Fonts.REGULAR);

        nvg.restore();
    }

    public void show() {
        animation = new EaseBackIn(300, 1, 0);
        animation.setDirection(Direction.FORWARDS);
        animation.reset();
        timer.reset();
    }

    public boolean isShown() {
        return !animation.isDone(Direction.BACKWARDS);
    }

    public Animation getAnimation() {
        return animation;
    }

    public TimerUtils getTimer() {
        return timer;
    }
}

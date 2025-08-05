package me.miki.shindo.management.mods.impl;

import me.miki.shindo.Shindo;
import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventRender2D;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.SimpleHUDMod;
import me.miki.shindo.management.mods.settings.impl.BooleanSetting;
import me.miki.shindo.management.music.MusicManager;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.management.nanovg.font.Fonts;
import me.miki.shindo.management.nanovg.font.LegacyIcon;
import me.miki.shindo.utils.TimerUtils;

import java.awt.*;

public class MusicInfoMod extends SimpleHUDMod {

    private static MusicInfoMod instance;

    private final TimerUtils timer = new TimerUtils();
    private final TimerUtils timer2 = new TimerUtils();

    private float addX;
    private boolean back;

    private final BooleanSetting iconSetting = new BooleanSetting(TranslateText.ICON, this, true);

    public MusicInfoMod() {
        super(TranslateText.MUSIC_INFO, TranslateText.MUSIC_INFO_DESCRIPTION);

        instance = this;
    }

    public static int calculateProgressWidth(float currentTime, float endTime, int maxWidth) {
        if (endTime <= 0f || currentTime < 0f) return 0;

        float progress = Math.min(currentTime / endTime, 1.0f);
        return Math.round(progress * maxWidth);
    }

    public static MusicInfoMod getInstance() {
        return instance;
    }

    @EventTarget
    public void onRender2D(EventRender2D event) {

        NanoVGManager nvg = Shindo.getInstance().getNanoVGManager();
        nvg.setupAndDraw(() -> drawNanoVG(nvg));
    }

    private void drawNanoVG(NanoVGManager nvg) {

        MusicManager musicManager = Shindo.getInstance().getMusicManager();

        float current = musicManager.getCurrentTime();
        float end = musicManager.getEndTime();

        boolean hasIcon = getIcon() != null;

        float addX = hasIcon ? this.getTextWidth(getIcon(), 9.5F, Fonts.LEGACYICON) + 4 : 0;

        if (getText() != null) {

            float bgWidth = (this.getTextWidth(getLimitText(getText(), 9, getHudFont(1), 120), 9, getHudFont(1)) + 10) + addX;

            this.drawBackground(bgWidth, 28);
            this.drawText(getLimitText(getText(), 9, getHudFont(1), 120), 5.5F + addX, 5.5F, 9, getHudFont(1));

            if (hasIcon) {
                this.drawText(getIcon(), 5.5F, 4F, 10.4F, Fonts.LEGACYICON);
            }

            this.drawRoundedRect(2F, 18F, bgWidth - 48F, 6F, 2F);
            this.drawRoundedRect(3F, 19F, calculateProgressWidth(current, end, (int) (bgWidth - 50F)), 4F, 2F, Color.BLACK);

            String formattedCurrent = formatTime((int) musicManager.getCurrentTime());
            String formattedEnd = formatTime((int) musicManager.getEndTime());
            this.drawCenteredText(formattedCurrent + " / " + formattedEnd, bgWidth - 22F, 18F, 6, getHudFont(1));

            this.setWidth((int) bgWidth);
            this.setHeight(28);
        }
    }

    private String formatTime(int totalSeconds) {
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        if (hours > 0)
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        else
            return String.format("%02d:%02d", minutes, seconds);
    }

    @Override
    public String getText() {

        MusicManager musicManager = Shindo.getInstance().getMusicManager();

        if (musicManager.isPlaying()) {
            return "Playing: " + musicManager.getCurrentMusic().getName();
        } else {
            return "Nothing is Playing";
        }
    }

    @Override
    public String getIcon() {
        return iconSetting.isToggled() ? LegacyIcon.MUSIC : null;
    }
}

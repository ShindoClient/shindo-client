package me.miki.shindo.gui.modmenu.category.impl;

import me.miki.shindo.Shindo;
import me.miki.shindo.gui.modmenu.GuiModMenu;
import me.miki.shindo.gui.modmenu.category.Category;
import me.miki.shindo.management.color.AccentColor;
import me.miki.shindo.management.color.ColorManager;
import me.miki.shindo.management.color.palette.ColorPalette;
import me.miki.shindo.management.color.palette.ColorType;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.impl.InternalSettingsMod;
import me.miki.shindo.management.music.MusicData;
import me.miki.shindo.management.music.MusicManager;
import me.miki.shindo.management.music.MusicType;
import me.miki.shindo.management.music.ytdlp.YouTubeUrlCleaner;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.management.nanovg.font.Fonts;
import me.miki.shindo.management.nanovg.font.LegacyIcon;
import me.miki.shindo.management.notification.NotificationType;
import me.miki.shindo.ui.comp.impl.CompSlider;
import me.miki.shindo.ui.comp.impl.field.CompTextBox;
import me.miki.shindo.utils.ColorUtils;
import me.miki.shindo.utils.Multithreading;
import me.miki.shindo.utils.SearchUtils;
import me.miki.shindo.utils.animation.simple.SimpleAnimation;
import me.miki.shindo.utils.mouse.MouseUtils;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

public class MusicCategory extends Category {

    private final SimpleAnimation downloaderAnimation = new SimpleAnimation();
    private final CompTextBox textBox = new CompTextBox();
    private CompSlider volumeSlider;
    private MusicType currentType;
    private boolean openDownloader;

    public MusicCategory(GuiModMenu parent) {
        super(parent, TranslateText.MUSIC, LegacyIcon.MUSIC, true, true);
    }

    @Override
    public void initGui() {
        currentType = MusicType.ALL;
        openDownloader = false;

        if (volumeSlider == null) {
            volumeSlider = new CompSlider(InternalSettingsMod.getInstance().getVolumeSetting());
        }
    }

    @Override
    public void initCategory() {
        scroll.resetAll();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        Shindo instance = Shindo.getInstance();
        NanoVGManager nvg = instance.getNanoVGManager();
        ColorManager colorManager = instance.getColorManager();
        ColorPalette palette = colorManager.getPalette();
        AccentColor accentColor = colorManager.getCurrentColor();
        MusicManager musicManager = instance.getMusicManager();
        MusicData currentMusic = musicManager.getCurrentMusic();

        int offsetX = 0;
        float offsetY = 13;
        int index = 1;

        nvg.save();
        nvg.translate(0, scroll.getValue());

        for (MusicType t : MusicType.values()) {

            float textWidth = nvg.getTextWidth(t.getName(), 9, Fonts.MEDIUM);
            boolean isCurrentCategory = t.equals(currentType);

            t.getBackgroundAnimation().setAnimation(isCurrentCategory ? 1.0F : 0.0F, 16);

            Color defaultColor = palette.getBackgroundColor(ColorType.DARK);
            Color color1 = ColorUtils.applyAlpha(accentColor.getColor1(), (int) (t.getBackgroundAnimation().getValue() * 255));
            Color color2 = ColorUtils.applyAlpha(accentColor.getColor2(), (int) (t.getBackgroundAnimation().getValue() * 255));
            Color textColor = t.getTextColorAnimation().getColor(isCurrentCategory ? Color.WHITE : palette.getFontColor(ColorType.DARK), 20);

            nvg.drawRoundedRect(this.getX() + 15 + offsetX, this.getY() + offsetY - 3, textWidth + 20, 16, 6, defaultColor);
            nvg.drawGradientRoundedRect(this.getX() + 15 + offsetX, this.getY() + offsetY - 3, textWidth + 20, 16, 6, color1, color2);

            nvg.drawText(t.getName(), this.getX() + 15 + offsetX + ((textWidth + 20) - textWidth) / 2, this.getY() + offsetY + 1.5F, textColor, 9, Fonts.MEDIUM);

            offsetX += (int) (textWidth + 28);
        }

        offsetY = offsetY + 23;

        for (MusicData m : musicManager.getMusics()) {

            if (filter(m)) {
                continue;
            }

            nvg.drawRoundedRect(this.getX() + 15, this.getY() + offsetY, this.getWidth() - 30, 46, 8, palette.getBackgroundColor(ColorType.DARK));

            if (m.getIcon() == null) {
                nvg.drawRoundedImage(new ResourceLocation("shindo/music.png"), this.getX() + 21, this.getY() + offsetY + 6, 34, 34, 6);
            } else {
                nvg.drawRoundedImage(m.getIcon(), this.getX() + 21, this.getY() + offsetY + 6, 34, 34, 6);
            }

            m.getFavoriteAnimation().setAnimation(m.getType().equals(MusicType.FAVORITE) ? 1.0F : 0.0F, 16);

            nvg.drawText(nvg.getLimitText(m.getName(), 11, Fonts.MEDIUM, 280), this.getX() + 63, this.getY() + offsetY + 9, palette.getFontColor(ColorType.DARK), 11, Fonts.MEDIUM);
            nvg.drawText(LegacyIcon.STAR, this.getX() + this.getWidth() - 56.5F, this.getY() + offsetY + 16, palette.getMaterialYellow(), 13, Fonts.LEGACYICON);
            nvg.drawText(LegacyIcon.STAR_FILL, this.getX() + this.getWidth() - 56.5F, this.getY() + offsetY + 16, palette.getMaterialYellow((int) (m.getFavoriteAnimation().getValue() * 255)), 13, Fonts.LEGACYICON);
            nvg.drawText(LegacyIcon.TRASH, this.getX() + this.getWidth() - 39, this.getY() + offsetY + 16, palette.getMaterialRed(), 13, Fonts.LEGACYICON);

            index++;
            offsetY += 56;
        }

        nvg.restore();

        downloaderAnimation.setAnimation(openDownloader ? 1.0F : 0.0F, 16);

        nvg.save();
        nvg.translate(0, 60 - (downloaderAnimation.getValue() * 60));

        nvg.drawRoundedRect(this.getX() + this.getWidth() - 175, this.getY() + this.getHeight() - 86, 165, 30, 6, palette.getBackgroundColor(ColorType.DARK));
        textBox.setPosition(this.getX() + this.getWidth() - 169, this.getY() + this.getHeight() - 80, 129, 18);
        textBox.draw(mouseX, mouseY, partialTicks);

        nvg.drawRoundedRect(this.getX() + this.getWidth() - 34, this.getY() + this.getHeight() - 80, 18, 18, 4, palette.getBackgroundColor(ColorType.NORMAL));
        nvg.drawText(LegacyIcon.DOWNLOAD, this.getX() + this.getWidth() - 30.5F, this.getY() + this.getHeight() - 76.5F, palette.getFontColor(ColorType.DARK), 11, Fonts.LEGACYICON);

        nvg.restore();

        nvg.drawRoundedRectVarying(this.getX(), this.getY() + this.getHeight() - 46F, this.getWidth(), 46, 0, 0, 0, 12, palette.getBackgroundColor(ColorType.DARK));

        if (currentMusic == null || currentMusic.getIcon() == null) {
            nvg.drawRoundedImage(new ResourceLocation("shindo/music.png"), this.getX() + 15, this.getY() + this.getHeight() - 40F, 34, 34, 6);
        } else {
            nvg.drawRoundedImage(musicManager.getCurrentMusic().getIcon(), this.getX() + 15, this.getY() + this.getHeight() - 40F, 34, 34, 6);
        }


        if (musicManager.getCurrentMusic() != null) {
            String current = formatTime((int) musicManager.getCurrentTime());
            String total = formatTime((int) musicManager.getEndTime());

            nvg.drawText(current + " / " + total, getX() + 55, this.getY() + this.getHeight() - 15F, palette.getFontColor(ColorType.DARK), 9, Fonts.MEDIUM);
        }

        nvg.drawText(currentMusic == null ? TranslateText.NOTHING_IS_PLAYING.getText() : nvg.getLimitText(currentMusic.getName(), 9, Fonts.MEDIUM, 290), this.getX() + 56, this.getY() + this.getHeight() - 37,
                palette.getFontColor(ColorType.DARK), 9, Fonts.MEDIUM);

        nvg.drawText(currentMusic == null || !musicManager.isPlaying() ? LegacyIcon.PLAY : LegacyIcon.PAUSE, this.getX() + (this.getWidth() / 2F) - 8, this.getY() + this.getHeight() - 21.5F,
                palette.getFontColor(ColorType.NORMAL), 16, Fonts.LEGACYICON);

        nvg.drawText(LegacyIcon.BACK, this.getX() + (this.getWidth() / 2F) - 33, this.getY() + this.getHeight() - 21.5F,
                palette.getFontColor(ColorType.NORMAL), 16, Fonts.LEGACYICON);

        nvg.drawText(LegacyIcon.FORWARD, this.getX() + (this.getWidth() / 2F) + 17, this.getY() + this.getHeight() - 21.5F,
                palette.getFontColor(ColorType.NORMAL), 16, Fonts.LEGACYICON);

        nvg.drawText("+" + TranslateText.ADD_SONG.getText(), this.getX() + this.getWidth() - (nvg.getTextWidth("+" + TranslateText.ADD_SONG.getText(), 9, Fonts.MEDIUM)) - 10, this.getY() + this.getHeight() - 37,
                palette.getFontColor(ColorType.DARK), 9, Fonts.MEDIUM);

        volumeSlider.setX(this.getX() + this.getWidth() - 62 - 10);
        volumeSlider.setY(this.getY() + this.getHeight() - 20);
        volumeSlider.setWidth(62);
        volumeSlider.setHeight(4.5);
        volumeSlider.setCircle(false);
        volumeSlider.setShowValue(false);
        volumeSlider.draw(mouseX, mouseY, partialTicks);
        musicManager.setVolume();

        float volume = volumeSlider.getSetting().getValueFloat();

        String volumeIcon = LegacyIcon.VOLUME;

        if (volume == 0) {
            volumeIcon = LegacyIcon.VOLUME_X;
        }
        if (volume > 0.1F) {
            volumeIcon = LegacyIcon.VOLUME;
        }
        if (volume > 0.4F) {
            volumeIcon = LegacyIcon.VOLUME_1;
        }
        if (volume > 0.8F) {
            volumeIcon = LegacyIcon.VOLUME_2;
        }

        nvg.drawText(volumeIcon, this.getX() + this.getWidth() - 94, this.getY() + this.getHeight() - 26,
                palette.getFontColor(ColorType.NORMAL), 16, Fonts.LEGACYICON);

        scroll.setMaxScroll((index - (index > 3 ? 3.91F : index)) * 56);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {

        Shindo instance = Shindo.getInstance();
        NanoVGManager nvg = instance.getNanoVGManager();
        MusicManager musicManager = instance.getMusicManager();

        int offsetX = 0;
        float offsetY = 13 + scroll.getValue();

        if (openDownloader) {

            textBox.mouseClicked(mouseX, mouseY, mouseButton);

            if (MouseUtils.isInside(mouseX, mouseY, this.getX() + this.getWidth() - 34, this.getY() + this.getHeight() - 80, 18, 18) && mouseButton == 0) {

                openDownloader = false;

                Multithreading.runAsync(() -> {

                    instance.getNotificationManager().post(TranslateText.MUSIC, TranslateText.ADDED_MUSIC_QUEUE, NotificationType.INFO);

                    if (musicManager.getYtdlp().download(YouTubeUrlCleaner.cleanUrl(textBox.getText()))) {
                        instance.getNotificationManager().post(TranslateText.MUSIC, TranslateText.MUSIC_DOWNLOAD_COMPLETE, NotificationType.SUCCESS);
                        musicManager.load();
                    } else {
                        instance.getNotificationManager().post(TranslateText.MUSIC, TranslateText.MUSIC_DOWNLOAD_FAILED, NotificationType.ERROR);
                    }
                });

                return;
            }

            if (!MouseUtils.isInside(mouseX, mouseY, this.getX() + this.getWidth() - 175, this.getY() + this.getHeight() - 86, 165, 30)) {
                openDownloader = false;
                return;
            }
        }

        for (MusicType t : MusicType.values()) {

            float textWidth = nvg.getTextWidth(t.getName(), 9, Fonts.MEDIUM);

            if (MouseUtils.isInside(mouseX, mouseY, this.getX(), this.getY(), this.getWidth(), this.getHeight())) {

                if (MouseUtils.isInside(mouseX, mouseY, this.getX() + 15 + offsetX, this.getY() + offsetY - 3, textWidth + 20, 16) && mouseButton == 0) {
                    currentType = t;
                }
            }

            offsetX += (int) (textWidth + 28);
        }

        offsetY = offsetY + 23;

        for (MusicData m : musicManager.getMusics()) {

            if (filter(m)) {
                continue;
            }

            if (mouseButton == 0 && !openDownloader && MouseUtils.isInside(mouseX, mouseY, this.getX(), this.getY(), this.getWidth(), this.getHeight() - 46)) {

                boolean inside = MouseUtils.isInside(mouseX, mouseY, this.getX() + 15, this.getY() + offsetY, this.getWidth() - 30, 46);
                boolean favorite = MouseUtils.isInside(mouseX, mouseY, this.getX() + this.getWidth() - 59, this.getY() + offsetY + 14, 18, 18);
                boolean trash = MouseUtils.isInside(mouseX, mouseY, this.getX() + this.getWidth() - 41, this.getY() + offsetY + 14, 18, 18);

                if (inside && !favorite && !trash) {
                    musicManager.stop();
                    musicManager.setCurrentMusic(m);
                    musicManager.play();
                }

                if (favorite) {
                    if (m.getType().equals(MusicType.ALL)) {
                        m.setType(MusicType.FAVORITE);
                    } else {
                        m.setType(MusicType.ALL);
                    }
                    musicManager.saveData();
                }

                if (trash) {
                    musicManager.delete(m);
                }
            }

            offsetY += 56;
        }

        if (mouseButton == 0) {

            float textWidth = nvg.getTextWidth("+" + TranslateText.ADD_SONG.getText(), 9, Fonts.MEDIUM);

            if (MouseUtils.isInside(mouseX, mouseY, this.getX() + (this.getWidth() / 2F) - 9, this.getY() + this.getHeight() - 21.5F, 17, 17)) {
                musicManager.switchPlayBack();
            }

            if (MouseUtils.isInside(mouseX, mouseY, this.getX() + (this.getWidth() / 2F) - 34, this.getY() + this.getHeight() - 22.5F, 18, 18)) {
                musicManager.back();
            }

            if (MouseUtils.isInside(mouseX, mouseY, this.getX() + (this.getWidth() / 2F) + 16, this.getY() + this.getHeight() - 22.5F, 18, 18)) {
                musicManager.next();
            }

            if (MouseUtils.isInside(mouseX, mouseY, this.getX() + this.getWidth() - textWidth - 10, this.getY() + this.getHeight() - 39, textWidth, 12)) {
                if (openDownloader) {
                    openDownloader = false;
                } else {
                    textBox.setText("");
                    openDownloader = true;
                }
            }
        }

        volumeSlider.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        volumeSlider.mouseReleased(mouseX, mouseY, mouseButton);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (openDownloader) {
            textBox.keyTyped(typedChar, keyCode);
        }
    }

    private boolean filter(MusicData m) {

        if (!currentType.equals(MusicType.ALL) && !m.getType().equals(currentType)) {
            return true;
        }


        return !this.getSearchBox().getText().isEmpty() && !SearchUtils.isSimillar(m.getName(), this.getSearchBox().getText());
    }

    private String formatTime(int seconds) {
        int mins = seconds / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d", mins, secs);
    }
}

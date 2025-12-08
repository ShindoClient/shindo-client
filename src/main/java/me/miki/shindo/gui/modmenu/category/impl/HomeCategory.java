package me.miki.shindo.gui.modmenu.category.impl;

import me.miki.shindo.Shindo;
import me.miki.shindo.gui.modmenu.GuiModMenu;
import me.miki.shindo.gui.modmenu.category.Category;
import me.miki.shindo.logger.ShindoLogger;
import me.miki.shindo.management.color.AccentColor;
import me.miki.shindo.management.color.ColorManager;
import me.miki.shindo.management.color.palette.ColorPalette;
import me.miki.shindo.management.color.palette.ColorType;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.music.MusicManager;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.management.nanovg.font.Fonts;
import me.miki.shindo.management.nanovg.font.LegacyIcon;
import me.miki.shindo.management.remote.changelog.Changelog;
import me.miki.shindo.management.remote.changelog.ChangelogManager;
import me.miki.shindo.management.remote.discord.DiscordStats;
import me.miki.shindo.management.remote.news.News;
import me.miki.shindo.management.remote.news.NewsManager;
import me.miki.shindo.libs.spotify.model_objects.specification.Track;
import me.miki.shindo.utils.ColorUtils;
import me.miki.shindo.utils.TimerUtils;
import me.miki.shindo.utils.mouse.MouseUtils;
import me.miki.shindo.utils.mouse.Scroll;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.net.URL;
import java.util.List;

public class HomeCategory extends Category {

    private final Scroll changelogScroll = new Scroll();
    private final Scroll newsScroll = new Scroll();
    private final TimerUtils newsRotationTimer = new TimerUtils();
    private int currentNewsIndex = 0;
    Color onlineColour = new Color(85, 155, 89, 255);
    Color noColour = new Color(0, 0, 0, 0);

    public HomeCategory(GuiModMenu parent) {
        super(parent, TranslateText.HOME, LegacyIcon.HOME, false, false);
    }

    @Override
    public void initGui() {
        changelogScroll.resetAll();
        newsScroll.resetAll();
        newsRotationTimer.reset();
        currentNewsIndex = 0;
        Shindo.getInstance().getDiscordStats().check();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        Shindo instance = Shindo.getInstance();
        NanoVGManager nvg = instance.getNanoVGManager();
        ColorManager colorManager = instance.getColorManager();
        ColorPalette palette = colorManager.getPalette();
        AccentColor currentColor = colorManager.getCurrentColor();
        ChangelogManager changelogManager = instance.getChangelogManager();
        NewsManager newsManager = instance.getNewsManager();
        DiscordStats discStat = instance.getDiscordStats();
        int standardPadding = 8;
        int outerPadding = 15;

        // News - altura igual ao changelog (151)
        List<News> newsList = newsManager.getNews();
        if (!newsList.isEmpty()) {
            // Rotação automática a cada 15 segundos
            if (newsRotationTimer.delay(15000)) {
                currentNewsIndex = (currentNewsIndex + 1) % newsList.size();
                newsRotationTimer.reset();
            }

            News currentNews = newsList.get(currentNewsIndex);
            float newsHeight = 151F; // Mesma altura do changelog
            nvg.drawRoundedRect(this.getX() + outerPadding, this.getY() + outerPadding, 200, newsHeight, 8, palette.getBackgroundColor(ColorType.DARK));
            nvg.drawText(TranslateText.NEWS.getText(), this.getX() + outerPadding + 8, this.getY() + 15 + 8, palette.getFontColor(ColorType.DARK), 11F, Fonts.SEMIBOLD);

            nvg.save();
            nvg.scissor(this.getX() + outerPadding, this.getY() + outerPadding + 20, 200, newsHeight - 20);
            nvg.translate(0, newsScroll.getValue());

            float newsY = this.getY() + 43F;
            float titleSize = nvg.getTextBoxHeight(currentNews.getTitle(), 10, Fonts.SEMIBOLD, 180);
            nvg.drawTextBox(currentNews.getTitle(), this.getX() + outerPadding + 8, newsY, 180, palette.getFontColor(ColorType.DARK), 10, Fonts.SEMIBOLD);
            newsY += titleSize + 2;
            float subTitleSize = nvg.getTextBoxHeight(currentNews.getSubTitle(), 8.5F, Fonts.MEDIUM, 180);
            nvg.drawTextBox(currentNews.getSubTitle(), this.getX() + outerPadding + 8, newsY, 180, palette.getFontColor(ColorType.DARK), 8.5F, Fonts.MEDIUM);
            newsY += subTitleSize + 3;
            float bodySize = nvg.getTextBoxHeight(currentNews.getBody(), 8, Fonts.REGULAR, 180);
            nvg.drawTextBox(currentNews.getBody(), this.getX() + outerPadding + 8, newsY, 180, palette.getFontColor(ColorType.DARK), 8, Fonts.REGULAR);

            nvg.restore();

            if (MouseUtils.isInside(mouseX, mouseY, this.getX() + outerPadding, this.getY() + outerPadding, 200, newsHeight)) {
                newsScroll.onScroll();
            }
            newsScroll.onAnimation();
            newsScroll.setMaxScroll(Math.max((int) (titleSize + subTitleSize + bodySize + 10) - (int) (newsHeight - 40), 0));

            // Shadow
            nvg.drawVerticalGradientRect(this.getX() + outerPadding + 8, this.getY() + outerPadding + 20, 200 - 16, 8, palette.getBackgroundColor(ColorType.DARK), noColour);
            nvg.drawVerticalGradientRect(this.getX() + outerPadding + 8, this.getY() + outerPadding + newsHeight - 8, 200 - 16, 8, noColour, palette.getBackgroundColor(ColorType.DARK));
        }


        // Changelog com progressbar
        int offsetChangelogY = 0;
        float changelogHeight = 151F;

        nvg.drawRoundedRect(this.getX() + 230, this.getY() + outerPadding, 174, changelogHeight, 8, palette.getBackgroundColor(ColorType.DARK));
        nvg.drawText(TranslateText.CHANGELOG.getText(), this.getX() + 230 + 8, this.getY() + 15 + 8, palette.getFontColor(ColorType.DARK), 11F, Fonts.SEMIBOLD);

        // Progressbar de 15 segundos
        float progressBarX = this.getX() + outerPadding + 8;
        float progressBarY = this.getY() + outerPadding + changelogHeight - 12;
        float progressBarWidth = 200 - 16;
        float progressBarHeight = 2F;
        long elapsed = newsRotationTimer.getElapsedTime();
        float progress = Math.min(1.0F, elapsed / 15000.0F);

        nvg.drawRoundedRect(progressBarX, progressBarY, progressBarWidth, progressBarHeight, 1F, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 150));
        nvg.drawRoundedRect(progressBarX, progressBarY, progressBarWidth * progress, progressBarHeight, 1F, currentColor.getColor1());

        nvg.save();
        nvg.scissor(this.getX() + 230, this.getY() + outerPadding + 20, 174, changelogHeight - 32);
        nvg.translate(0, changelogScroll.getValue());

        for (Changelog c : changelogManager.getChangelogs()) {
            float tbSize = nvg.getTextBoxHeight(c.getText(), 8, Fonts.MEDIUM, 174 - 33);
            nvg.drawRoundedRect(this.getX() + 230 + 8, this.getY() + 40 + offsetChangelogY + ((tbSize / 2) - 4), 13, 13, 7F, c.getType().getColor());
            nvg.drawCenteredText(c.getType().getText(), this.getX() + 230 + 8 + (13 / 2F), this.getY() + 42F + offsetChangelogY + ((tbSize / 2) - 3), Color.WHITE, 7, Fonts.LEGACYICON);
            nvg.drawTextBox(c.getText(), this.getX() + 230 + 25, this.getY() + 43F + offsetChangelogY, 174 - 33, palette.getFontColor(ColorType.DARK), 8, Fonts.MEDIUM);
            offsetChangelogY += (int) (tbSize + 9);
        }
        nvg.restore();
        if (offsetChangelogY > (changelogHeight - 40) && MouseUtils.isInside(mouseX, mouseY, this.getX() + 230, this.getY() + outerPadding, 174, changelogHeight)) {
            changelogScroll.onScroll();
        }
        changelogScroll.onAnimation();
        changelogScroll.setMaxScroll(Math.max(offsetChangelogY - (int) (changelogHeight - 40), 0));


        // Player & Spotify Card (no espaço que sobrou)
        float playerCardY = this.getY() + outerPadding + 151 + 12; // Abaixo do changelog
        float playerCardHeight = 99F; // Altura do card (86 do discord + 13 de espaço)
        float playerCardX = this.getX() + outerPadding;
        float playerCardWidth = 200F;

        nvg.drawRoundedRect(playerCardX, playerCardY, playerCardWidth, playerCardHeight, 8, palette.getBackgroundColor(ColorType.DARK));

        MusicManager musicManager = instance.getMusicManager();
        Minecraft mc = Minecraft.getMinecraft();
        boolean spotifyLinked = musicManager != null && musicManager.isAuthorized();

        // Player head e nome
        float headSize = 32F;
        float headX = playerCardX + 8;
        float headY = playerCardY + 8;
        String playerName = mc.getSession() != null ? mc.getSession().getUsername() : TranslateText.PLAYER.getText();
        ResourceLocation playerHead = mc.thePlayer != null ? mc.thePlayer.getLocationSkin() : new ResourceLocation("textures/entity/steve.png");

        nvg.drawPlayerHead(playerHead, headX, headY, headSize, headSize, 4F);
        nvg.drawText(playerName, headX + headSize + 8, headY + 10, palette.getFontColor(ColorType.DARK), 10F, Fonts.MEDIUM);

        // Spotify controls
        float spotifyY = headY + headSize + 8;
        if (spotifyLinked && musicManager != null) {
            Track currentTrack = musicManager.getCurrentTrack();
            boolean isPlaying = musicManager.isPlaying();
            long trackPosition = musicManager.getCurrentPosition();
            long trackDuration = (long) (musicManager.getEndTime() * 1000); // getEndTime retorna em segundos

            if (currentTrack != null) {
                // Ícone da música (se couber)
                float iconSize = 16F;
                float iconX = headX;
                float iconY = spotifyY;
                nvg.drawText(LegacyIcon.MUSIC, iconX, iconY, palette.getFontColor(ColorType.NORMAL), iconSize, Fonts.LEGACYICON);

                // Nome da música
                String trackName = currentTrack.getName();
                String artistName = currentTrack.getArtists() != null && currentTrack.getArtists().length > 0 ? currentTrack.getArtists()[0].getName() : TranslateText.UNKNOWN.getText();
                float trackNameX = iconX + iconSize + 4;
                float trackNameWidth = playerCardWidth - 8;
                nvg.drawText(nvg.getLimitText(trackName, 9F, Fonts.MEDIUM, trackNameWidth), trackNameX, iconY, palette.getFontColor(ColorType.DARK), 9F, Fonts.MEDIUM);
                nvg.drawText(nvg.getLimitText(artistName, 7.5F, Fonts.REGULAR, trackNameWidth), trackNameX, iconY + 12, ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 200), 7.5F, Fonts.REGULAR);

                // Controles
                float controlsY = spotifyY + 24;
                float controlsCenterX = playerCardX + playerCardWidth / 2F;
                float controlSize = 14F;
                float controlSpacing = 24F;

                // Previous
                boolean prevHovered = MouseUtils.isInside(mouseX, mouseY, controlsCenterX - controlSpacing - controlSize / 2F, controlsY, controlSize, controlSize);
                nvg.drawCenteredText(LegacyIcon.BACK, controlsCenterX - controlSpacing, controlsY + 2, ColorUtils.applyAlpha(palette.getFontColor(ColorType.DARK), prevHovered ? 255 : 200), controlSize, Fonts.LEGACYICON);

                // Play/Pause
                boolean playHovered = MouseUtils.isInside(mouseX, mouseY, controlsCenterX - controlSize / 2F, controlsY, controlSize, controlSize);
                String playIcon = isPlaying ? LegacyIcon.PAUSE : LegacyIcon.PLAY;
                nvg.drawCenteredText(playIcon, controlsCenterX, controlsY + 2, ColorUtils.applyAlpha(palette.getFontColor(ColorType.DARK), playHovered ? 255 : 200), controlSize, Fonts.LEGACYICON);

                // Next
                boolean nextHovered = MouseUtils.isInside(mouseX, mouseY, controlsCenterX + controlSpacing - controlSize / 2F, controlsY, controlSize, controlSize);
                nvg.drawCenteredText(LegacyIcon.FORWARD, controlsCenterX + controlSpacing, controlsY + 2, ColorUtils.applyAlpha(palette.getFontColor(ColorType.DARK), nextHovered ? 255 : 200), controlSize, Fonts.LEGACYICON);

                // Progress bar
                float progressBarY2 = controlsY + controlSize + 6;
                float progressBarWidth2 = playerCardWidth - 16;
                float progressBarHeight2 = 2F;
                float progress2 = trackDuration > 0 ? (float) trackPosition / (float) trackDuration : 0F;

                nvg.drawRoundedRect(headX, progressBarY2, progressBarWidth2, progressBarHeight2, 1F, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 150));
                nvg.drawRoundedRect(headX, progressBarY2, progressBarWidth2 * progress2, progressBarHeight2, 1F, currentColor.getColor1());

                // Tempo
                String currentTime = formatTime(trackPosition / 1000);
                String totalTime = formatTime(trackDuration / 1000);
                float timeWidth = nvg.getTextWidth(currentTime + " / " + totalTime, 7F, Fonts.REGULAR);

                nvg.drawText( currentTime + " / " + totalTime, headX + progressBarWidth2 - timeWidth, progressBarY2 - 9, ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 180), 7F, Fonts.REGULAR);
            } else {
                // Nada tocando
                nvg.drawText(TranslateText.NOTHING_IS_PLAYING.getText(), headX, spotifyY, ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 200), 8F, Fonts.REGULAR);
            }
        } else {
            // Spotify não linkado
                nvg.drawText(TranslateText.SPOTIFY_NOT_LINKED.getText(), headX, spotifyY, ColorUtils.applyAlpha(new Color(255, 180, 90), 220), 8F, Fonts.REGULAR);
                nvg.drawTextBox(TranslateText.SPOTIFY_LINK_DESCRIPTION.getText(), headX, spotifyY + 12, playerCardWidth - 16, ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 180), 7F, Fonts.REGULAR);
        }

        // Discord
        int discordStartX = this.getX() + 230;
        int discordStartY = (int) (playerCardY);
        int discordWidth = 174;

        //bg
        nvg.drawRoundedRect(discordStartX, discordStartY, discordWidth, 86, 8, palette.getBackgroundColor(ColorType.DARK));
        // Discord branding
        nvg.drawRoundedRectVarying(discordStartX + discordWidth - 22, discordStartY, 22, 22, 0, 8, 8, 0, new Color(114, 137, 214));
        nvg.drawCenteredText(LegacyIcon.DISCORD, discordStartX + discordWidth - 11, discordStartY + 4, Color.WHITE, 14F, Fonts.LEGACYICON);
        // txt
        nvg.drawText(TranslateText.JOIN_OUR_DISCORD_SERVER.getText(), discordStartX + standardPadding, discordStartY + standardPadding, palette.getFontColor(ColorType.DARK), 11F, Fonts.SEMIBOLD);
        nvg.drawTextBox(TranslateText.DISCORD_DESCRIPTION.getText(), discordStartX + standardPadding, discordStartY + 26, discordWidth - 16, palette.getFontColor(ColorType.DARK), 8, Fonts.REGULAR);
        // stats
        if (discStat.getMemberCount() != -1) {
            nvg.drawRoundedRect(discordStartX + 10, discordStartY + 66, 6, 6, 3, onlineColour);
            nvg.drawRoundedGlow(discordStartX + 10, discordStartY + 66, 6, 6, 3, onlineColour, 7);
            nvg.drawTextGlowing(discStat.getMemberCount() + " " + TranslateText.MEMBERS.getText(), discordStartX + 20, discordStartY + 62, onlineColour, 4, 8, Fonts.REGULAR);
            nvg.drawTextGlowing(discStat.getMemberOnline() + " " + TranslateText.ONLINE.getText(), discordStartX + 20, discordStartY + 70, onlineColour, 4, 8, Fonts.REGULAR);
        }
        // join button
        nvg.drawRoundedRect(discordStartX + discordWidth - 60, discordStartY + 60, 52, 18, 9, new Color(114, 137, 214));
        nvg.drawCenteredText(TranslateText.JOIN.getText() + " >", discordStartX + discordWidth - 60 + (52 / 2F), discordStartY + 66, Color.WHITE, 7, Fonts.REGULAR);

    }

    private String formatTime(long seconds) {
        long minutes = seconds / 60L;
        long remainingSeconds = seconds % 60L;
        return String.format("%02d:%02d", minutes, remainingSeconds);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        int outerPadding = 15;
        int discordStartX = this.getX() + 230;
        int discordStartY = (this.getY() + outerPadding + 151 + 12); // Abaixo do player card
        if (MouseUtils.isInside(mouseX, mouseY, discordStartX + 174 - 60, discordStartY + 60, 52, 18)) {
            try {
                Desktop.getDesktop().browse(new URL("https://shindoclient.com/discord").toURI());
            } catch (Exception e) {
                ShindoLogger.error(TranslateText.DISCORD_LINK_ERROR.getText(), e);
            }
        }

        // Spotify controls
        MusicManager musicManager = Shindo.getInstance().getMusicManager();
        if (musicManager != null && musicManager.isAuthorized()) {
            float playerCardY = this.getY() + outerPadding + 151 + 12; // Abaixo do changelog
            float playerCardHeight = 99F; // Altura do card (86 do discord + 13 de espaço)
            float playerCardX = this.getX() + outerPadding;
            float playerCardWidth = 200F;


            // Player head e nome
            float headSize = 32F;
            float headX = playerCardX + 8;
            float headY = playerCardY + 8;

            // Spotify controls
            float spotifyY = headY + headSize + 8;

            float controlsY = spotifyY + 24;
            float controlsCenterX = playerCardX + playerCardWidth / 2F;
            float controlSize = 14F;
            float controlSpacing = 24F;

            if (mouseButton == 0) {
                // Previous
                if (MouseUtils.isInside(mouseX, mouseY, controlsCenterX - controlSpacing - controlSize / 2F, controlsY, controlSize, controlSize)) {
                    musicManager.previousTrack();
                    return;
                }

                // Play/Pause
                if (MouseUtils.isInside(mouseX, mouseY, controlsCenterX - controlSize / 2F, controlsY, controlSize, controlSize)) {
                    if (musicManager.isPlaying()) {
                        musicManager.pause();
                    } else {
                        musicManager.resume();
                    }
                    return;
                }

                // Next
                if (MouseUtils.isInside(mouseX, mouseY, controlsCenterX + controlSpacing - controlSize / 2F, controlsY, controlSize, controlSize)) {
                    musicManager.nextTrack();
                    return;
                }

                // Progress bar click (seek)
                float progressBarY = controlsY + controlSize + 6;
                float progressBarWidth = 200 - 16;
                if (MouseUtils.isInside(mouseX, mouseY, this.getX() + outerPadding + 8, progressBarY, progressBarWidth, 2F)) {
                    Track currentTrack = musicManager.getCurrentTrack();
                    if (currentTrack != null) {
                        long trackDuration = (long) (musicManager.getEndTime() * 1000);
                        if (trackDuration > 0) {
                            float relativeX = mouseX - (this.getX() + outerPadding + 8);
                            float progress = Math.max(0F, Math.min(1F, relativeX / progressBarWidth));
                            long seekPosition = (long) (trackDuration * progress);
                            musicManager.seekToPosition(seekPosition);
                        }
                    }
                }
            }
        }
    }
}

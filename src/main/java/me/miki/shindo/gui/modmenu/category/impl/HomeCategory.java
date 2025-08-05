package me.miki.shindo.gui.modmenu.category.impl;

import me.miki.shindo.Shindo;
import me.miki.shindo.gui.modmenu.GuiModMenu;
import me.miki.shindo.gui.modmenu.category.Category;
import me.miki.shindo.management.color.AccentColor;
import me.miki.shindo.management.color.ColorManager;
import me.miki.shindo.management.color.palette.ColorPalette;
import me.miki.shindo.management.color.palette.ColorType;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.management.nanovg.font.Fonts;
import me.miki.shindo.management.nanovg.font.LegacyIcon;
import me.miki.shindo.management.remote.changelog.Changelog;
import me.miki.shindo.management.remote.changelog.ChangelogManager;
import me.miki.shindo.management.remote.discord.DiscordStats;
import me.miki.shindo.management.remote.news.News;
import me.miki.shindo.management.remote.news.NewsManager;
import me.miki.shindo.utils.mouse.MouseUtils;
import me.miki.shindo.utils.mouse.Scroll;

import java.awt.*;
import java.net.URL;

public class HomeCategory extends Category {

    Color onlineColour = new Color(85, 155, 89, 255);
    Color noColour = new Color(0, 0, 0, 0);
    private final Scroll changelogScroll = new Scroll();
    private final Scroll newsScroll = new Scroll();

    public HomeCategory(GuiModMenu parent) {
        super(parent, TranslateText.HOME, LegacyIcon.HOME, false, false);
    }

    @Override
    public void initGui() {
        changelogScroll.resetAll();
        newsScroll.resetAll();
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

        // news
        int offsetNewsY = 0;
        nvg.drawRoundedRect(this.getX() + outerPadding, this.getY() + outerPadding, 200, 250, 8, palette.getBackgroundColor(ColorType.DARK));
        nvg.drawText(TranslateText.NEWS.getText(), this.getX() + outerPadding + 8, this.getY() + 15 + 8, palette.getFontColor(ColorType.DARK), 11F, Fonts.SEMIBOLD);

        nvg.save();
        nvg.scissor(this.getX() + outerPadding, this.getY() + outerPadding + 20, 200, 230);
        nvg.translate(0, newsScroll.getValue());

        for (News n : newsManager.getNews()) {
            float titleSize = nvg.getTextBoxHeight(n.getTitle(), 10, Fonts.SEMIBOLD, 180);
            nvg.drawTextBox(n.getTitle(), this.getX() + outerPadding + 8, this.getY() + 43F + offsetNewsY, 180, palette.getFontColor(ColorType.DARK), 10, Fonts.SEMIBOLD);
            offsetNewsY += (int) (titleSize);
            float subTitleSize = nvg.getTextBoxHeight(n.getSubTitle(), 8.5F, Fonts.MEDIUM, 180);
            nvg.drawTextBox(n.getSubTitle(), this.getX() + outerPadding + 8, this.getY() + 43F + offsetNewsY, 180, palette.getFontColor(ColorType.DARK), 8.5F, Fonts.MEDIUM);
            offsetNewsY += (int) (subTitleSize + 1);
            float bodySize = nvg.getTextBoxHeight(n.getBody(), 8, Fonts.REGULAR, 180);
            nvg.drawTextBox(n.getBody(), this.getX() + outerPadding + 8, this.getY() + 43F + offsetNewsY, 180, palette.getFontColor(ColorType.DARK), 8, Fonts.REGULAR);
            offsetNewsY += (int) (bodySize + 9);
        }
        nvg.restore();

        if (MouseUtils.isInside(mouseX, mouseY, this.getX() + outerPadding, this.getY() + outerPadding, 200, 250)) {
            newsScroll.onScroll();
        }
        newsScroll.onAnimation();
        newsScroll.setMaxScroll(Math.max(offsetNewsY - 225, 0));

        // shadow
        nvg.drawVerticalGradientRect(this.getX() + outerPadding + 8, this.getY() + outerPadding + 20, 200 - 16, 8, palette.getBackgroundColor(ColorType.DARK), noColour);
        nvg.drawVerticalGradientRect(this.getX() + outerPadding + 8, this.getY() + outerPadding + 250 - 8, 200 - 16, 8, noColour, palette.getBackgroundColor(ColorType.DARK));


        // Changelog

        int offsetChangelogY = 0;

        nvg.drawRoundedRect(this.getX() + 230, this.getY() + outerPadding, 174, 151, 8, palette.getBackgroundColor(ColorType.DARK));
        nvg.drawText(TranslateText.CHANGELOG.getText(), this.getX() + 230 + 8, this.getY() + 15 + 8, palette.getFontColor(ColorType.DARK), 11F, Fonts.SEMIBOLD);

        nvg.save();
        nvg.scissor(this.getX() + 230, this.getY() + outerPadding + 20, 174, 131);
        nvg.translate(0, changelogScroll.getValue());

        for (Changelog c : changelogManager.getChangelogs()) {
            float tbSize = nvg.getTextBoxHeight(c.getText(), 8, Fonts.MEDIUM, 174 - 33);
            nvg.drawRoundedRect(this.getX() + 230 + 8, this.getY() + 40 + offsetChangelogY + ((tbSize / 2) - 4), 13, 13, 7F, c.getType().getColor());
            nvg.drawCenteredText(c.getType().getText(), this.getX() + 230 + 8 + (13 / 2), this.getY() + 42F + offsetChangelogY + ((tbSize / 2) - 3), Color.WHITE, 7, Fonts.LEGACYICON);
            nvg.drawTextBox(c.getText(), this.getX() + 230 + 25, this.getY() + 43F + offsetChangelogY, 174 - 33, palette.getFontColor(ColorType.DARK), 8, Fonts.MEDIUM);
            offsetChangelogY += (int) (tbSize + 9);
        }
        nvg.restore();
        if (offsetChangelogY > 130 && MouseUtils.isInside(mouseX, mouseY, this.getX() + 230, this.getY() + outerPadding, 174, 151)) {
            changelogScroll.onScroll();
        }
        changelogScroll.onAnimation();
        changelogScroll.setMaxScroll(Math.max(offsetChangelogY - 120, 0));

        nvg.drawVerticalGradientRect(this.getX() + 230 + 8, this.getY() + outerPadding + 20, 174 - 16, 8, palette.getBackgroundColor(ColorType.DARK), noColour);
        nvg.drawVerticalGradientRect(this.getX() + 230 + 8, this.getY() + outerPadding + 151 - 8, 174 - 16, 8, noColour, palette.getBackgroundColor(ColorType.DARK));


        // Discord
        int discordStartX = this.getX() + 230;
        int discordStartY = this.getY() + 179;
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
            nvg.drawTextGlowing(discStat.getMemberCount() + " Members", discordStartX + 20, discordStartY + 62, onlineColour, 4, 8, Fonts.REGULAR);
            nvg.drawTextGlowing(discStat.getMemberOnline() + " Online", discordStartX + 20, discordStartY + 70, onlineColour, 4, 8, Fonts.REGULAR);
        }
        // join button
        nvg.drawRoundedRect(discordStartX + discordWidth - 60, discordStartY + 60, 52, 18, 9, new Color(114, 137, 214));
        nvg.drawCenteredText(TranslateText.JOIN.getText() + " >", discordStartX + discordWidth - 60 + (52 / 2), discordStartY + 66, Color.WHITE, 7, Fonts.REGULAR);

    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        int discordStartX = this.getX() + 230;
        int discordStartY = this.getY() + 179;
        if (MouseUtils.isInside(mouseX, mouseY, discordStartX + 174 - 60, discordStartY + 60, 52, 18)) {
            try {
                Desktop.getDesktop().browse(new URL("https://shindoclient.github.io/discord").toURI());
            } catch (Exception e) {
            }
        }
    }
}

package me.miki.shindo.gui.modmenu.category.impl

import me.miki.shindo.Shindo
import me.miki.shindo.gui.modmenu.GuiModMenu
import me.miki.shindo.gui.modmenu.category.Category
import me.miki.shindo.logger.ShindoLogger
import me.miki.shindo.management.color.AccentColor
import me.miki.shindo.management.color.ColorManager
import me.miki.shindo.management.color.palette.ColorPalette
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.music.MusicManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.remote.changelog.Changelog
import me.miki.shindo.management.remote.changelog.ChangelogManager
import me.miki.shindo.management.remote.discord.DiscordStats
import me.miki.shindo.management.remote.news.News
import me.miki.shindo.management.remote.news.NewsManager
import me.miki.shindo.utils.ColorUtils
import me.miki.shindo.utils.TimerUtils
import me.miki.shindo.utils.mouse.MouseUtils
import me.miki.shindo.utils.mouse.Scroll
import net.minecraft.client.Minecraft
import net.minecraft.util.ResourceLocation
import java.awt.Color
import java.awt.Desktop
import java.net.URL
import kotlin.math.max
import kotlin.math.min

class HomeCategory(parent: GuiModMenu) : Category(parent, TranslateText.HOME, LegacyIcon.HOME, false, false) {

    private val newsScroll = Scroll()
    private val newsRotationTimer = TimerUtils()
    private val changelogRotationTimer = TimerUtils()
    private var currentNewsIndex = 0
    private var currentChangelogPage = 0
    private val onlineColour = Color(85, 155, 89, 255)
    private val noColour = Color(0, 0, 0, 0)

    override fun initGui() {
        newsScroll.resetAll()
        newsRotationTimer.reset()
        changelogRotationTimer.reset()
        currentNewsIndex = 0
        currentChangelogPage = 0
        Shindo.getInstance().discordStats.check()
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val instance = Shindo.getInstance()
        val nvg = instance.nanoVGManager ?: return
        val colorManager: ColorManager = instance.colorManager
        val palette: ColorPalette = colorManager.getPalette()
        val currentColor: AccentColor = colorManager.getCurrentColor()
        val changelogManager: ChangelogManager = instance.changelogManager
        val newsManager: NewsManager = instance.newsManager
        val discStat: DiscordStats = instance.discordStats
        val standardPadding = 8
        val layout = computeLayout()
        val leftX = layout.leftX
        val rightX = layout.rightX
        val topY = layout.topY
        val bottomY = layout.bottomY
        val columnWidth = layout.columnWidth

        val newsList: List<News> = newsManager.getNews()
        if (newsList.isNotEmpty()) {
            if (newsRotationTimer.delay(PAGE_ROTATION_MS.toFloat())) {
                currentNewsIndex = (currentNewsIndex + 1) % newsList.size
                newsRotationTimer.reset()
            }

            val currentNews = newsList[currentNewsIndex]
            val newsHeight = TOP_CARD_HEIGHT
            nvg.drawRoundedRect(leftX, topY, columnWidth, newsHeight, 8f, palette.getBackgroundColor(ColorType.DARK))
            nvg.drawText(
                TranslateText.NEWS.getText(),
                leftX + INNER_PADDING,
                topY + 15 + INNER_PADDING,
                palette.getFontColor(ColorType.DARK),
                11f,
                Fonts.SEMIBOLD
            )
            if (newsList.size > 1) {
                val newsPageLabel = (currentNewsIndex + 1).toString() + "/" + newsList.size
                val newsPageWidth = nvg.getTextWidth(newsPageLabel, 8f, Fonts.REGULAR)
                val newsPageY = topY + newsHeight - 20f
                nvg.drawText(
                    newsPageLabel,
                    leftX + columnWidth - newsPageWidth - INNER_PADDING,
                    newsPageY,
                    ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 200),
                    8f,
                    Fonts.REGULAR
                )
            }

            nvg.save()
            nvg.scissor(leftX, topY + 20, columnWidth, newsHeight - 20)
            nvg.translate(0f, newsScroll.getValue())

            val textWidth = max(0f, columnWidth - (INNER_PADDING * 2f))
            var newsY = topY + 43f
            val titleSize = nvg.getTextBoxHeight(currentNews.title, 10f, Fonts.SEMIBOLD, textWidth)
            nvg.drawTextBox(
                currentNews.title,
                leftX + INNER_PADDING,
                newsY,
                textWidth,
                palette.getFontColor(ColorType.DARK),
                10f,
                Fonts.SEMIBOLD
            )
            newsY += titleSize + 2
            val subTitleSize = nvg.getTextBoxHeight(currentNews.subTitle, 8.5f, Fonts.MEDIUM, textWidth)
            nvg.drawTextBox(
                currentNews.subTitle,
                leftX + INNER_PADDING,
                newsY,
                textWidth,
                palette.getFontColor(ColorType.DARK),
                8.5f,
                Fonts.MEDIUM
            )
            newsY += subTitleSize + 3
            val bodySize = nvg.getTextBoxHeight(currentNews.body, 8f, Fonts.REGULAR, textWidth)
            nvg.drawTextBox(
                currentNews.body,
                leftX + INNER_PADDING,
                newsY,
                textWidth,
                palette.getFontColor(ColorType.DARK),
                8f,
                Fonts.REGULAR
            )

            nvg.restore()

            if (MouseUtils.isInside(mouseX, mouseY, leftX, topY, columnWidth, newsHeight)) {
                newsScroll.onScroll()
            }
            newsScroll.onAnimation()
            newsScroll.maxScroll = max((titleSize + subTitleSize + bodySize + 10F) - (newsHeight - 40F), 0F)

            nvg.drawVerticalGradientRect(
                leftX + INNER_PADDING,
                topY + 20,
                columnWidth - (INNER_PADDING * 2f),
                8f,
                palette.getBackgroundColor(ColorType.DARK),
                noColour
            )
            nvg.drawVerticalGradientRect(
                leftX + INNER_PADDING,
                topY + newsHeight - 8,
                columnWidth - (INNER_PADDING * 2f),
                8f,
                noColour,
                palette.getBackgroundColor(ColorType.DARK)
            )
        }

        var offsetChangelogY = 0
        val changelogHeight = TOP_CARD_HEIGHT

        nvg.drawRoundedRect(rightX, topY, columnWidth, changelogHeight, 8f, palette.getBackgroundColor(ColorType.DARK))
        nvg.drawText(
            TranslateText.CHANGELOG.getText(),
            rightX + INNER_PADDING,
            topY + 15 + INNER_PADDING,
            palette.getFontColor(ColorType.DARK),
            11f,
            Fonts.SEMIBOLD
        )

        val progressBarX = leftX + INNER_PADDING
        val progressBarY = topY + changelogHeight - 12
        val progressBarWidth = columnWidth - (INNER_PADDING * 2f)
        val progressBarHeight = 2f
        val elapsed = newsRotationTimer.elapsedTime
        val progress = min(1.0f, elapsed / PAGE_ROTATION_MS.toFloat())

        nvg.drawRoundedRect(
            progressBarX,
            progressBarY,
            progressBarWidth,
            progressBarHeight,
            1f,
            ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 150)
        )
        nvg.drawRoundedRect(
            progressBarX,
            progressBarY,
            progressBarWidth * progress,
            progressBarHeight,
            1f,
            currentColor.getColor1()
        )

        val contentTop = topY + 40f
        val contentHeight = changelogHeight - 48f
        val iconSize = 13f
        val iconGap = 4f
        val textWidth = max(0f, columnWidth * 0.68f)
        iconSize + iconGap + textWidth
        val iconX = rightX + INNER_PADDING
        val textX = iconX + iconSize + iconGap

        val pages = ArrayList<ArrayList<Changelog>>()
        var currentPage = ArrayList<Changelog>()
        var currentHeight = 0f
        for (c in changelogManager.getChangelogs()) {
            val tbSize = nvg.getTextBoxHeight(c.text, 8f, Fonts.MEDIUM, textWidth)
            val entryHeight = tbSize + 9f
            if (currentPage.isNotEmpty() && currentHeight + entryHeight > contentHeight) {
                pages.add(currentPage)
                currentPage = ArrayList()
                currentHeight = 0f
            }
            currentPage.add(c)
            currentHeight += entryHeight
        }
        if (currentPage.isNotEmpty()) {
            pages.add(currentPage)
        }

        val totalPages = max(1, pages.size)
        if (currentChangelogPage >= totalPages) {
            currentChangelogPage = 0
            changelogRotationTimer.reset()
        }
        if (totalPages > 1 && changelogRotationTimer.delay(PAGE_ROTATION_MS.toFloat())) {
            currentChangelogPage = (currentChangelogPage + 1) % totalPages
            changelogRotationTimer.reset()
        }

        if (totalPages > 1) {
            val changelogProgressX = rightX + INNER_PADDING
            val changelogProgressY = topY + changelogHeight - 12
            val changelogProgressWidth = columnWidth - (INNER_PADDING * 2f)
            val changelogProgressHeight = 2f
            val changelogElapsed = changelogRotationTimer.elapsedTime
            val changelogProgress = min(1.0f, changelogElapsed / PAGE_ROTATION_MS.toFloat())

            nvg.drawRoundedRect(
                changelogProgressX,
                changelogProgressY,
                changelogProgressWidth,
                changelogProgressHeight,
                1f,
                ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 150)
            )
            nvg.drawRoundedRect(
                changelogProgressX,
                changelogProgressY,
                changelogProgressWidth * changelogProgress,
                changelogProgressHeight,
                1f,
                currentColor.getColor1()
            )

            val changelogPageLabel = (currentChangelogPage + 1).toString() + "/" + totalPages
            val changelogPageWidth = nvg.getTextWidth(changelogPageLabel, 8f, Fonts.REGULAR)
            val changelogPageY = changelogProgressY - 8f
            nvg.drawText(
                changelogPageLabel,
                rightX + columnWidth - changelogPageWidth - INNER_PADDING,
                changelogPageY,
                ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 200),
                8f,
                Fonts.REGULAR
            )
        }

        nvg.save()
        nvg.scissor(rightX, topY + 20, columnWidth, changelogHeight - 32)

        if (pages.isNotEmpty()) {
            val page = pages[min(currentChangelogPage, pages.size - 1)]
            for (c in page) {
                val tbSize = nvg.getTextBoxHeight(c.text, 8f, Fonts.MEDIUM, textWidth)
                val lineY = contentTop + offsetChangelogY
                nvg.drawRoundedRect(iconX, lineY + ((tbSize / 2f) - 4f), iconSize, iconSize, 7f, c.type.color)
                nvg.drawCenteredText(
                    c.type.text,
                    iconX + (iconSize / 2f),
                    lineY + ((tbSize / 2f) - 1f),
                    Color.WHITE,
                    7f,
                    Fonts.LEGACYICON
                )
                nvg.drawTextBox(
                    c.text,
                    textX,
                    lineY + 3f,
                    textWidth,
                    palette.getFontColor(ColorType.DARK),
                    8f,
                    Fonts.MEDIUM
                )
                offsetChangelogY += (tbSize + 9f).toInt()
            }
        }

        nvg.restore()

        val playerCardHeight = BOTTOM_CARD_HEIGHT

        nvg.drawRoundedRect(
            leftX,
            bottomY,
            columnWidth,
            playerCardHeight,
            8f,
            palette.getBackgroundColor(ColorType.DARK)
        )

        val musicManager: MusicManager = instance.musicManager
        val mc = Minecraft.getMinecraft()
        val spotifyLinked = musicManager.isAuthorized()

        val headSize = 32f
        val headX = leftX + INNER_PADDING
        val headY = bottomY + INNER_PADDING
        val playerName = if (mc.session != null) mc.session.username else TranslateText.PLAYER.getText()
        val playerHead =
            if (mc.thePlayer != null) mc.thePlayer.locationSkin else ResourceLocation("textures/entity/steve.png")

        nvg.drawPlayerHead(playerHead, headX, headY, headSize, headSize, 4f)
        nvg.drawText(
            playerName,
            headX + headSize + 8,
            headY + 10,
            palette.getFontColor(ColorType.DARK),
            10f,
            Fonts.MEDIUM
        )

        val spotifyY = headY + headSize + 8
        if (spotifyLinked) {
            val currentTrack = musicManager.getCurrentTrack()
            val isPlaying = musicManager.isPlaying()
            val trackPosition = musicManager.getCurrentPosition()
            val trackDuration = (musicManager.getEndTime() * 1000).toLong()

            if (currentTrack != null) {
                val iconSize1 = 16f
                nvg.drawText(
                    LegacyIcon.MUSIC,
                    headX,
                    spotifyY,
                    palette.getFontColor(ColorType.NORMAL),
                    iconSize,
                    Fonts.LEGACYICON
                )

                val trackName = currentTrack.name
                val artistName =
                    if (currentTrack.artists != null && currentTrack.artists.isNotEmpty()) currentTrack.artists[0].name else TranslateText.UNKNOWN.getText()
                val trackNameX = headX + iconSize1 + 4
                val trackNameWidth = columnWidth - (INNER_PADDING * 2f)
                nvg.drawText(
                    nvg.getLimitText(trackName, 9f, Fonts.MEDIUM, trackNameWidth),
                    trackNameX,
                    spotifyY,
                    palette.getFontColor(ColorType.DARK),
                    9f,
                    Fonts.MEDIUM
                )
                nvg.drawText(
                    nvg.getLimitText(artistName, 7.5f, Fonts.REGULAR, trackNameWidth),
                    trackNameX,
                    spotifyY + 12,
                    ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 200),
                    7.5f,
                    Fonts.REGULAR
                )

                val controlsY = spotifyY + 24
                val controlsCenterX = leftX + columnWidth / 2f
                val controlSize = 14f
                val controlSpacing = 24f

                val prevHovered = MouseUtils.isInside(
                    mouseX,
                    mouseY,
                    controlsCenterX - controlSpacing - controlSize / 2f,
                    controlsY,
                    controlSize,
                    controlSize
                )
                nvg.drawCenteredText(
                    LegacyIcon.BACK,
                    controlsCenterX - controlSpacing,
                    controlsY + 2,
                    ColorUtils.applyAlpha(palette.getFontColor(ColorType.DARK), if (prevHovered) 255 else 200),
                    controlSize,
                    Fonts.LEGACYICON
                )

                val playHovered = MouseUtils.isInside(
                    mouseX,
                    mouseY,
                    controlsCenterX - controlSize / 2f,
                    controlsY,
                    controlSize,
                    controlSize
                )
                val playIcon = if (isPlaying) LegacyIcon.PAUSE else LegacyIcon.PLAY
                nvg.drawCenteredText(
                    playIcon,
                    controlsCenterX,
                    controlsY + 2,
                    ColorUtils.applyAlpha(palette.getFontColor(ColorType.DARK), if (playHovered) 255 else 200),
                    controlSize,
                    Fonts.LEGACYICON
                )

                val nextHovered = MouseUtils.isInside(
                    mouseX,
                    mouseY,
                    controlsCenterX + controlSpacing - controlSize / 2f,
                    controlsY,
                    controlSize,
                    controlSize
                )
                nvg.drawCenteredText(
                    LegacyIcon.FORWARD,
                    controlsCenterX + controlSpacing,
                    controlsY + 2,
                    ColorUtils.applyAlpha(palette.getFontColor(ColorType.DARK), if (nextHovered) 255 else 200),
                    controlSize,
                    Fonts.LEGACYICON
                )

                val progressBarY2 = controlsY + controlSize + 6
                val progressBarWidth2 = columnWidth - (INNER_PADDING * 2f)
                val progressBarHeight2 = 2f
                val progress2 = if (trackDuration > 0) trackPosition.toFloat() / trackDuration.toFloat() else 0f

                nvg.drawRoundedRect(
                    headX,
                    progressBarY2,
                    progressBarWidth2,
                    progressBarHeight2,
                    1f,
                    ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 150)
                )
                nvg.drawRoundedRect(
                    headX,
                    progressBarY2,
                    progressBarWidth2 * progress2,
                    progressBarHeight2,
                    1f,
                    currentColor.getColor1()
                )

                val currentTime = formatTime(trackPosition / 1000)
                val totalTime = formatTime(trackDuration / 1000)
                val timeWidth = nvg.getTextWidth("$currentTime / $totalTime", 7f, Fonts.REGULAR)

                nvg.drawText(
                    "$currentTime / $totalTime",
                    headX + progressBarWidth2 - timeWidth,
                    progressBarY2 - 9,
                    ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 180),
                    7f,
                    Fonts.REGULAR
                )
            } else {
                nvg.drawText(
                    TranslateText.NOTHING_IS_PLAYING.getText(),
                    headX,
                    spotifyY,
                    ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 200),
                    8f,
                    Fonts.REGULAR
                )
            }
        } else {
            nvg.drawText(
                TranslateText.SPOTIFY_NOT_LINKED.getText(),
                headX,
                spotifyY,
                ColorUtils.applyAlpha(Color(255, 180, 90), 220),
                8f,
                Fonts.REGULAR
            )
            nvg.drawTextBox(
                TranslateText.SPOTIFY_LINK_DESCRIPTION.getText(),
                headX,
                spotifyY + 12,
                columnWidth - (INNER_PADDING * 2f),
                ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 180),
                7f,
                Fonts.REGULAR
            )
        }

        val discordStartX = rightX.toInt()
        val discordStartY = bottomY.toInt()
        val discordWidth = columnWidth.toInt()

        nvg.drawRoundedRect(
            discordStartX.toFloat(),
            discordStartY.toFloat(),
            discordWidth.toFloat(),
            BOTTOM_CARD_HEIGHT,
            8f,
            palette.getBackgroundColor(ColorType.DARK)
        )
        nvg.drawRoundedRectVarying(
            discordStartX + discordWidth - 22F,
            discordStartY.toFloat(),
            22F,
            22F,
            0F,
            8F,
            8F,
            0F,
            Color(114, 137, 214)
        )
        nvg.drawCenteredText(
            LegacyIcon.DISCORD,
            discordStartX + discordWidth - 11f,
            discordStartY + 4f,
            Color.WHITE,
            14f,
            Fonts.LEGACYICON
        )

        nvg.drawText(
            TranslateText.JOIN_OUR_DISCORD_SERVER.getText(),
            discordStartX + standardPadding.toFloat(),
            discordStartY + standardPadding.toFloat(),
            palette.getFontColor(ColorType.DARK),
            11f,
            Fonts.SEMIBOLD
        )
        nvg.drawTextBox(
            TranslateText.DISCORD_DESCRIPTION.getText(),
            discordStartX + standardPadding.toFloat(),
            discordStartY + 26f,
            discordWidth - 16f,
            palette.getFontColor(ColorType.DARK),
            8f,
            Fonts.REGULAR
        )
        if (discStat.membersCount != -1) {
            nvg.drawRoundedRect(discordStartX + 10f, discordStartY + BOTTOM_CARD_HEIGHT - 20, 6f, 6f, 3f, onlineColour)
            nvg.drawRoundedGlow(
                discordStartX + 10f,
                discordStartY + BOTTOM_CARD_HEIGHT - 20,
                6f,
                6f,
                3f,
                onlineColour,
                7
            )
            nvg.drawTextGlowing(
                discStat.membersCount.toString() + " " + TranslateText.MEMBERS.getText(),
                discordStartX + 20f,
                discordStartY + BOTTOM_CARD_HEIGHT - 24,
                onlineColour,
                4F,
                8F,
                Fonts.REGULAR
            )
            nvg.drawTextGlowing(
                discStat.membersOnline.toString() + " " + TranslateText.ONLINE.getText(),
                discordStartX + 20f,
                discordStartY + BOTTOM_CARD_HEIGHT - 16,
                onlineColour,
                4F,
                8F,
                Fonts.REGULAR
            )
        }
        nvg.drawRoundedRect(
            discordStartX + discordWidth - 60f,
            discordStartY + BOTTOM_CARD_HEIGHT - 28,
            52f,
            18f,
            9f,
            Color(114, 137, 214)
        )
        nvg.drawCenteredText(
            TranslateText.JOIN.getText() + " >",
            discordStartX + discordWidth - 60f + (52 / 2f),
            discordStartY + BOTTOM_CARD_HEIGHT - 22,
            Color.WHITE,
            7f,
            Fonts.REGULAR
        )
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        val layout = computeLayout()
        val discordStartX = layout.rightX.toInt()
        val discordStartY = layout.bottomY.toInt()
        val discordWidth = layout.columnWidth.toInt()
        val discordHeight = BOTTOM_CARD_HEIGHT.toInt()
        val joinButtonX = discordStartX + discordWidth - 60
        val joinButtonY = discordStartY + discordHeight - 28
        if (MouseUtils.isInside(mouseX, mouseY, joinButtonX.toFloat(), joinButtonY.toFloat(), 52f, 18f)) {
            try {
                Desktop.getDesktop().browse(URL("https://shindoclient.com/discord").toURI())
            } catch (e: Exception) {
                ShindoLogger.error(TranslateText.DISCORD_LINK_ERROR.getText(), e)
            }
        }

        val musicManager: MusicManager = Shindo.getInstance().musicManager
        if (musicManager.isAuthorized()) {
            val playerCardY = layout.bottomY
            val playerCardX = layout.leftX
            val playerCardWidth = layout.columnWidth

            val headSize = 32f
            val headX = playerCardX + INNER_PADDING
            val headY = playerCardY + INNER_PADDING

            val spotifyY = headY + headSize + 8

            val controlsY = spotifyY + 24
            val controlsCenterX = playerCardX + playerCardWidth / 2f
            val controlSize = 14f
            val controlSpacing = 24f

            if (mouseButton == 0) {
                if (MouseUtils.isInside(
                        mouseX,
                        mouseY,
                        controlsCenterX - controlSpacing - controlSize / 2f,
                        controlsY,
                        controlSize,
                        controlSize
                    )
                ) {
                    musicManager.previousTrack()
                    return
                }

                if (MouseUtils.isInside(
                        mouseX,
                        mouseY,
                        controlsCenterX - controlSize / 2f,
                        controlsY,
                        controlSize,
                        controlSize
                    )
                ) {
                    if (musicManager.isPlaying()) {
                        musicManager.pause()
                    } else {
                        musicManager.resume()
                    }
                    return
                }

                if (MouseUtils.isInside(
                        mouseX,
                        mouseY,
                        controlsCenterX + controlSpacing - controlSize / 2f,
                        controlsY,
                        controlSize,
                        controlSize
                    )
                ) {
                    musicManager.nextTrack()
                    return
                }

                val progressBarY = controlsY + controlSize + 6
                val progressBarWidth = playerCardWidth - (INNER_PADDING * 2f)
                if (MouseUtils.isInside(mouseX, mouseY, headX, progressBarY, progressBarWidth, 2f)) {
                    val currentTrack = musicManager.getCurrentTrack()
                    if (currentTrack != null) {
                        val trackDuration = (musicManager.getEndTime() * 1000).toLong()
                        if (trackDuration > 0) {
                            val relativeX = mouseX - headX
                            val progress = max(0f, min(1f, relativeX / progressBarWidth))
                            val seekPosition = (trackDuration * progress).toLong()
                            musicManager.seekToPosition(seekPosition)
                        }
                    }
                }
            }
        }
    }

    private fun formatTime(seconds: Long): String {
        val minutes = seconds / 60L
        val remainingSeconds = seconds % 60L
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }

    private fun computeLayout(): HomeLayout {
        val viewportX = getX().toFloat()
        val viewportY = getY().toFloat()
        val viewportWidth = getWidth().toFloat()

        val contentX = viewportX + OUTER_PADDING
        val contentY = viewportY + OUTER_PADDING
        val contentWidth = viewportWidth - (OUTER_PADDING * 2f)
        val columnWidth = max(0f, (contentWidth - COLUMN_GAP) / 2f)

        val leftX = contentX
        val rightX = contentX + columnWidth + COLUMN_GAP
        val topY = contentY
        val bottomY = topY + TOP_CARD_HEIGHT + ROW_GAP

        return HomeLayout(leftX, rightX, topY, bottomY, columnWidth)
    }

    private data class HomeLayout(
        val leftX: Float,
        val rightX: Float,
        val topY: Float,
        val bottomY: Float,
        val columnWidth: Float
    )

    private companion object {
        const val OUTER_PADDING = 16
        const val COLUMN_GAP = 16
        const val ROW_GAP = 12
        const val TOP_CARD_HEIGHT = 155f
        const val BOTTOM_CARD_HEIGHT = 100f
        const val INNER_PADDING = 8f
        const val PAGE_ROTATION_MS = 15000
    }
}

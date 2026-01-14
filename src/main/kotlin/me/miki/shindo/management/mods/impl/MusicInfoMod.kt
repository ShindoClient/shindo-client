package me.miki.shindo.management.mods.impl

import me.miki.shindo.Shindo.Companion.getInstance
import me.miki.shindo.libs.spotify.model_objects.specification.ArtistSimplified
import me.miki.shindo.libs.spotify.model_objects.specification.Track
import me.miki.shindo.logger.ShindoLogger.info
import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventKey
import me.miki.shindo.management.event.impl.EventRender2D
import me.miki.shindo.management.event.impl.EventUpdate
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.SimpleHUDMod
import me.miki.shindo.management.music.LyricsManager
import me.miki.shindo.management.music.LyricsManager.LyricsResponse
import me.miki.shindo.management.music.MusicManager.TrackInfoCallback
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyEnum
import me.miki.shindo.management.settings.config.PropertyType
import me.miki.shindo.management.settings.impl.BooleanSetting
import me.miki.shindo.management.settings.metadata.SettingRegistry.getBooleanSetting
import me.miki.shindo.management.settings.metadata.SettingRegistry.getTextSetting
import net.minecraft.util.ResourceLocation
import org.lwjgl.input.Keyboard
import java.awt.Color
import java.io.File
import java.lang.String
import java.util.*
import java.util.function.Consumer
import kotlin.Float
import kotlin.Int
import kotlin.Long
import kotlin.also
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

class MusicInfoMod :
    SimpleHUDMod(TranslateText.MUSIC_INFO, TranslateText.MUSIC_INFO_DESCRIPTION, LegacyIcon.MOD_MUSIC_INFO),
    TrackInfoCallback {
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.ICON)
    private val iconSetting = true

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.SHOW_LYRICS)
    private val showLyricsSetting = true

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.ROMANIZE_JAPANESE)
    private val romanizeJapaneseSetting = false

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.ENABLE_HOTKEYS)
    private val enableHotkeysSetting = true

    @Property(type = PropertyType.COMBO, translate = TranslateText.DESIGN)
    val design: Design = Design.SIMPLE

    @Property(
        type = PropertyType.TEXT,
        translate = TranslateText.LYRICS_API_URL,
        text = "https://spotify.mopigames.gay/"
    )
    val lyricsApiUrlSetting: String
        get() = getTextSetting(this, "lyricsApiUrlSetting")
    private val visibleLyrics = 5
    private val addX = 0f
    private val back = false
    private var trackDuration = 0L
    private var currentTrackId: String? = ""
    private var lyricsScrollOffset = 0.0f
    private var prevLyricsLineIndex = 0
    private var lastLyricsScrollTime = 0L
    private var cachedHeight = 85

    init {
        instance = this
    }

    @EventTarget
    fun onRender2D(event: EventRender2D?) {
        val nvg = getInstance().nanoVGManager
        this.updateDynamicHeight()
        if (design == Design.SIMPLE) {
            this.draw()
        } else if (design == Design.ADVANCED) {
            nvg!!.setupAndDraw(Runnable { this.drawAdvancedNanoVG() })
        }
    }

    @EventTarget
    fun onUpdate(event: EventUpdate?) {
        this.setDraggable(true)
        val musicManager = getInstance().musicManager
        if (musicManager != null && musicManager.isPlaying() && musicManager.getCurrentTrack() != null) {
            this.updateLyrics(musicManager.getCurrentTrack(), musicManager.getTrackPosition())
        }
    }

    @EventTarget
    fun onKey(event: EventKey) {
        if (!this.isToggled() || !this.enableHotkeysSetting) {
            return
        }

        val keyCode = event.getKeyCode()
        val musicManager = getInstance().musicManager

        if (musicManager == null || !musicManager.isPlaying()) {
            return
        }

        var lastVolumeChangeTime = 0L
        if (keyCode == Keyboard.KEY_UP) {
            val currentVolume = musicManager.getVolume()
            val newVolume = min(100, currentVolume + 5) // Increase by 5%, capped at 100%
            musicManager.setVolume(newVolume)
            lastVolumeChangeTime = System.currentTimeMillis()
        } else if (keyCode == Keyboard.KEY_DOWN) {
            val currentVolume = musicManager.getVolume()
            val newVolume = max(0, currentVolume - 5) // Decrease by 5%, minimum 0%
            musicManager.setVolume(newVolume)
            lastVolumeChangeTime = System.currentTimeMillis()
        }

        // Fixed seeking implementation
        if (keyCode == Keyboard.KEY_RIGHT) {
            // Get current position directly from MusicManager to ensure it's up-to-date
            val currentPosition = musicManager.getTrackPosition()
            val duration = if (trackDuration > 0) trackDuration else Long.Companion.MAX_VALUE
            // Skip forward 10 seconds (10,000 ms)
            val newPosition = min(currentPosition + 10000, duration)
            info("Seeking from " + currentPosition + "ms to " + newPosition + "ms")
            musicManager.seekToPosition(newPosition)
        } else if (keyCode == Keyboard.KEY_LEFT) {
            // Get current position directly from MusicManager to ensure it's up-to-date
            val currentPosition = musicManager.getTrackPosition()
            // Skip backward 10 seconds (10,000 ms)
            val newPosition = max(currentPosition - 10000, 0)
            info("Seeking from " + currentPosition + "ms to " + newPosition + "ms")
            musicManager.seekToPosition(newPosition)
        }
    }

    private fun updateDynamicHeight() {
        val lyricsManager: LyricsManager?
        val lyrics: LyricsResponse?
        val musicManager = getInstance().musicManager
        var baseHeight = 85
        if (musicManager == null || !musicManager.isPlaying() || musicManager.getCurrentTrack() == null) {
            baseHeight = 75
        } else if (this.showLyricsSetting && musicManager.getLyricsManager() != null && ((musicManager.getLyricsManager()
                .also { lyricsManager = it }).getCurrentLyrics()
                .also { lyrics = it }) != null && !lyrics!!.isError() && !lyrics.getLines().isEmpty()
        ) {
            baseHeight = 110 + this.visibleLyrics * 12
        }
        this.cachedHeight = baseHeight
        this.setHeight(baseHeight)
    }

    private fun updateLyrics(currentTrack: Track?, position: Long) {
        if (!this.showLyricsSetting || currentTrack == null) {
            return
        }
        val musicManager = getInstance().musicManager
        if (musicManager == null || musicManager.getLyricsManager() == null) {
            return
        }
        val lyricsManager = musicManager.getLyricsManager()
        if (currentTrack.getId() != this.currentTrackId) {
            this.currentTrackId = currentTrack.getId()
            lyricsManager.reset()
            lyricsManager.fetchLyrics(currentTrack).thenAcceptAsync(Consumer { lyrics: LyricsResponse? ->
                if (lyrics != null && !lyrics.isError() && !lyrics.getLines().isEmpty()) {
                    if (this.romanizeJapaneseSetting) {
                        lyricsManager.processLyricsRomanization(lyrics)
                    }
                }
            })
        }
        lyricsManager.updateCurrentLineIndex(position)
    }

    private fun drawAdvancedNanoVG() {
        val lyricsManager: LyricsManager?
        val lyrics: LyricsResponse?
        val musicManager = getInstance().musicManager
        var hasLyrics = false
        val baseHeight = this.cachedHeight
        if (this.showLyricsSetting && musicManager != null && musicManager.isPlaying() && musicManager.getCurrentTrack() != null && musicManager.getLyricsManager() != null && ((musicManager.getLyricsManager()
                .also { lyricsManager = it }).getCurrentLyrics().also { lyrics = it }) != null && !lyrics!!.getLines()
                .isEmpty()
        ) {
            hasLyrics = true
        }
        this.drawBackground(155.0f, baseHeight.toFloat())
        if (musicManager.isPlaying() && musicManager.getCurrentTrack() != null) {
            val currentTrack = musicManager.getCurrentTrack()
            val albumArtUrl = musicManager.getAlbumArtUrl(currentTrack)
            if (albumArtUrl != null && !albumArtUrl.isEmpty()) {
                val albumArtFile = File(albumArtUrl)
                if (albumArtFile.exists()) {
                    this.drawRoundedImage(albumArtFile, 5.5f, 25.0f, 37.0f, 37.0f, 6.0f)
                } else {
                    this.drawRoundedImage(PLACEHOLDER_IMAGE, 5.5f, 25.0f, 37.0f, 37.0f, 6.0f)
                }
            } else {
                this.drawRoundedImage(PLACEHOLDER_IMAGE, 5.5f, 25.0f, 37.0f, 37.0f, 6.0f)
            }
            this.save()
            this.scissor(0.0f, 0.0f, 155.0f, baseHeight.toFloat())
            this.drawText(
                TranslateText.NOW_PLAYING.getText(),
                5.5f,
                6.0f,
                10.5f,
                this.getHudFont(3),
                Color(255, 255, 255, 80)
            )
            val trackName = currentTrack.getName()
            val artistNames = String.join(
                ", ",
                *Arrays.stream<ArtistSimplified?>(currentTrack.getArtists())
                    .map<kotlin.String?> { obj: ArtistSimplified? -> obj!!.getName() }
                    .toArray<kotlin.String?> { _Dummy_.__Array__() })
            val trackNameLines = this.breakTextIntoLines(trackName, 95.0f)
            var trackNameY = 25.0f
            for (line in trackNameLines) {
                this.drawText(line, 47.0f, trackNameY, 10.5f, this.getHudFont(2), Color(255, 255, 255, 80))
                trackNameY += 12.0f
            }
            val artistY = trackNameY + 2.0f
            this.drawText(artistNames, 47.0f, artistY, 9.5f, this.getHudFont(1), Color(255, 255, 255, 80))
            this.restore()
            val current = musicManager.getCurrentTime()
            val end = musicManager.getEndTime()
            val currentTime = this.formatTime(current.toLong())
            val totalTime = this.formatTime(end.toLong())
            val progressBarY = 70.5f
            val progressFactor = current / end

            // Use improved progress bar design from main menu
            this.drawRoundedRect(
                6.0f, progressBarY, 142.5f, 2.5f, 1.3f,
                Color(255, 255, 255, 80)
            )
            this.drawRoundedRect(
                6.0f, progressBarY, progressFactor * 142.5f, 2.5f, 1.3f,
                Color(255, 255, 255, 180)
            )

            val timeY = progressBarY + 6.0f
            this.drawText(currentTime, 6.0f, timeY, 6.0f, this.getHudFont(1))
            val totalTimeWidth: Float = this.getTextWidth(totalTime, 9.0f, this.getHudFont(1))!!
            this.drawText(totalTime, 163.0f - totalTimeWidth - 5.5f, timeY, 6.0f, this.getHudFont(1))

            if (hasLyrics && this.showLyricsSetting) {
                val lyricsHeaderY = timeY + 15.0f
                val lyricsManager2 = musicManager.getLyricsManager()
                val visibleLines = lyricsManager2.getVisibleLines(this.visibleLyrics)

                if (visibleLines != null && !visibleLines.isEmpty()) {
                    // Begin scissoring (clipping) for lyrics section
                    this.save()
                    val lyricsAreaHeight = baseHeight - lyricsHeaderY - 5.0f // 5px padding at bottom
                    this.scissor(0f, lyricsHeaderY, 145.0f, lyricsAreaHeight + 4.0f)

                    val currentLineIndex = lyricsManager2.getCurrentLineIndex()
                    this.updateLyricsScrollAnimation(currentLineIndex)
                    var lyricsY = lyricsHeaderY
                    val lineHeight = 16.0f
                    val yOffset = this.lyricsScrollOffset * lineHeight

                    for (i in visibleLines.indices) {
                        val line = visibleLines.get(i)
                        if (line == null) continue
                        val actualIndex = max(0, currentLineIndex - visibleLines.size / 2) + i
                        val isCurrentLine = actualIndex == currentLineIndex
                        var text = line.getWords()

                        // Use romanized text if available and the setting is enabled
                        if (this.romanizeJapaneseSetting && line.getRomanizedWords() != null) {
                            text = line.getRomanizedWords()
                        }

                        if (text != null && !text.isEmpty()) {
                            val limitedText =
                                getInstance().nanoVGManager!!.getLimitText(text, 9.0f, this.getHudFont(1), 140.0f)
                            val xPos = 5.0f
                            if (isCurrentLine) {
                                this.drawText(
                                    limitedText,
                                    xPos,
                                    lyricsY + yOffset,
                                    9.0f,
                                    this.getHudFont(2),
                                    Color(255, 255, 255, 180)
                                )
                            } else {
                                this.drawText(
                                    limitedText,
                                    xPos,
                                    lyricsY + yOffset,
                                    9.0f,
                                    this.getHudFont(1),
                                    Color(255, 255, 255, 80)
                                )
                            }
                        }
                        lyricsY += lineHeight
                    }

                    // End scissoring
                    this.restore()
                } else {
                    val noLyricsText = "No lyrics available"
                    val textWidth: Float = this.getTextWidth(noLyricsText, 10.0f, this.getHudFont(1))!!
                    val centerX = 77.5f
                    this.drawText(
                        noLyricsText,
                        centerX - textWidth / 2.0f,
                        lyricsHeaderY + 20.0f,
                        10.0f,
                        this.getHudFont(1),
                        Color(200, 200, 200)
                    )
                }
            }
        } else {
            this.drawText(
                TranslateText.NOTHING_IS_PLAYING.getText(),
                5.5f,
                6.0f,
                10.5f,
                this.getHudFont(3),
                Color(255, 255, 255, 80)
            )
            this.drawRoundedImage(PLACEHOLDER_IMAGE, 5.5f, 25.0f, 37.0f, 37.0f, 6.0f)
            val progressBarY = 67.5f
            // Use consistent progress bar design even when nothing is playing
            this.drawRoundedRect(
                6.0f, progressBarY, 142.5f, 2.5f, 1.3f,
                Color(255, 255, 255, 80)
            )
        }
        this.setWidth(155)
    }

    private fun formatTime(seconds: Long): kotlin.String {
        val minutes = seconds / 60L
        val remainingSeconds = seconds % 60L
        return kotlin.String.format("%02d:%02d", minutes, remainingSeconds)
    }

    private fun updateLyricsScrollAnimation(currentLineIndex: Int) {
        if (currentLineIndex != this.prevLyricsLineIndex) {
            this.lyricsScrollOffset = (currentLineIndex - this.prevLyricsLineIndex).toFloat()
            this.lastLyricsScrollTime = System.currentTimeMillis()
            this.prevLyricsLineIndex = currentLineIndex
        }

        if (this.lyricsScrollOffset != 0.0f) {
            val currentTime = System.currentTimeMillis()
            val timeSinceScroll = currentTime - this.lastLyricsScrollTime
            if (timeSinceScroll >= LYRICS_SCROLL_DURATION) {
                this.lyricsScrollOffset = 0.0f
            } else {
                var progress: Float = timeSinceScroll.toFloat() / LYRICS_SCROLL_DURATION
                progress = this.easeOutCubic(progress)
                this.lyricsScrollOffset = (1.0f - progress) * this.lyricsScrollOffset
            }
        }
    }

    private fun easeOutCubic(t: Float): Float {
        return 1.0f - (1.0f - t).toDouble().pow(3.0).toFloat()
    }

    private fun breakTextIntoLines(text: kotlin.String, maxWidth: Float): MutableList<kotlin.String> {
        val lines = ArrayList<kotlin.String>()
        val nvgManager = getInstance().nanoVGManager
        if (this.getTextWidth(text, 10.5f, this.getHudFont(1))!! <= maxWidth) {
            lines.add(text)
            return lines
        }
        val words = text.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        var currentLine = StringBuilder()
        for (word in words) {
            val testLine: kotlin.String
            testLine = if (currentLine.length > 0) currentLine.toString() + " " + word else word
            val string: kotlin.String? = testLine
            if (this.getTextWidth(testLine, 10.5f, this.getHudFont(1))!! <= maxWidth) {
                currentLine = StringBuilder(testLine)
                continue
            }
            if (currentLine.length > 0) {
                lines.add(currentLine.toString())
            }
            currentLine = StringBuilder(word)
        }
        if (currentLine.length > 0) {
            lines.add(currentLine.toString())
        }
        if (lines.size > 2) {
            val lastLine = lines.get(1)
            if (lastLine.length > 3) {
                lines.set(1, lastLine.substring(0, lastLine.length - 3) + "...")
            }
            return lines.subList(0, 2)
        }
        return lines
    }

    public override fun getText(): kotlin.String? {
        val musicManager = getInstance().musicManager
        if (musicManager.isPlaying()) {
            val currentTrack = musicManager.getCurrentTrack()
            return if (currentTrack != null) "Now Playing: " + currentTrack.getName() else "Nothing is Playing"
        }
        return "Nothing is Playing"
    }

    public override fun getIcon(): kotlin.String? {
        return if (this.iconSetting) "9" else null
    }

    override fun onTrackInfoUpdated(position: Long, duration: Long) {
        this.trackDuration = duration
        val musicManager = getInstance().musicManager
        if (musicManager != null && musicManager.getLyricsManager() != null) {
            musicManager.getLyricsManager().updateCurrentLineIndex(position)
            val newLineIndex = musicManager.getLyricsManager().getCurrentLineIndex()
            if (newLineIndex != this.prevLyricsLineIndex) {
                this.lyricsScrollOffset = (newLineIndex - this.prevLyricsLineIndex).toFloat()
                this.lastLyricsScrollTime = System.currentTimeMillis()
                this.prevLyricsLineIndex = newLineIndex
            }
        }
    }

    fun getShowLyricsSetting(): BooleanSetting? {
        return getBooleanSetting(this, "showLyricsSetting")
    }

    fun getRomanizeJapaneseSetting(): BooleanSetting? {
        return getBooleanSetting(this, "romanizeJapaneseSetting")
    }

    fun getEnableHotkeysSetting(): BooleanSetting? {
        return getBooleanSetting(this, "enableHotkeysSetting")
    }

    enum class Design(private val translate: TranslateText) : PropertyEnum {
        SIMPLE(TranslateText.SIMPLE),
        ADVANCED(TranslateText.ADVANCED);

        override fun getTranslate(): TranslateText {
            return translate
        }
    }

    companion object {
        private val PLACEHOLDER_IMAGE = ResourceLocation("shindo/music.png")
        private const val LYRICS_SCROLL_DURATION = 500L
        @JvmField
        var instance: MusicInfoMod? = null
    }
}




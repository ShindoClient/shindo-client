package me.miki.shindo.management.mods.impl;

import lombok.Getter;
import me.miki.shindo.Shindo;
import me.miki.shindo.libs.spotify.model_objects.specification.ArtistSimplified;
import me.miki.shindo.libs.spotify.model_objects.specification.Track;
import me.miki.shindo.logger.ShindoLogger;
import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventKey;
import me.miki.shindo.management.event.impl.EventRender2D;
import me.miki.shindo.management.event.impl.EventUpdate;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.SimpleHUDMod;
import me.miki.shindo.management.music.LyricsManager;
import me.miki.shindo.management.music.MusicManager;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.management.settings.impl.BooleanSetting;
import me.miki.shindo.management.settings.impl.TextSetting;
import me.miki.shindo.management.settings.metadata.SettingRegistry;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.miki.shindo.management.settings.config.Property;
import me.miki.shindo.management.settings.config.PropertyEnum;
import me.miki.shindo.management.settings.config.PropertyType;
public class MusicInfoMod extends SimpleHUDMod implements MusicManager.TrackInfoCallback {
    private static final ResourceLocation PLACEHOLDER_IMAGE = new ResourceLocation("soar/music.png");
    private static final long LYRICS_SCROLL_DURATION = 500L;
    @Getter
    private static MusicInfoMod instance;
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.ICON)
    private boolean iconSetting = true;
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.SHOW_LYRICS)
    private boolean showLyricsSetting = true;
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.ROMANIZE_JAPANESE)
    private boolean romanizeJapaneseSetting = false;
    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.ENABLE_HOTKEYS)
    private boolean enableHotkeysSetting = true;
    @Getter
    @Property(type = PropertyType.COMBO, translate = TranslateText.DESIGN)
    private Design design = Design.SIMPLE;
    @Property(type = PropertyType.TEXT, translate = TranslateText.LYRICS_API_URL, text = "https://spotify.mopigames.gay/")
    private String lyricsApiUrlSetting = "https://spotify.mopigames.gay/";
    private final int visibleLyrics = 5;
    private float addX;
    private boolean back;
    private long trackDuration = 0L;
    private String currentTrackId = "";
    private float lyricsScrollOffset = 0.0f;
    private int prevLyricsLineIndex = 0;
    private long lastLyricsScrollTime = 0L;
    private int cachedHeight = 85;

    public MusicInfoMod() {
        super(TranslateText.MUSIC_INFO, TranslateText.MUSIC_INFO_DESCRIPTION);
        instance = this;
    }

    @EventTarget
    public void onRender2D(EventRender2D event) {
        NanoVGManager nvg = Shindo.getInstance().getNanoVGManager();
        this.updateDynamicHeight();
        if (design == Design.SIMPLE) {
            this.draw();
        } else if (design == Design.ADVANCED) {
            nvg.setupAndDraw(this::drawAdvancedNanoVG);
        }
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        this.setDraggable(true);
        MusicManager musicManager = Shindo.getInstance().getMusicManager();
        if (musicManager != null && musicManager.isPlaying() && musicManager.getCurrentTrack() != null) {
            this.updateLyrics(musicManager.getCurrentTrack(), musicManager.getTrackPosition());
        }
    }

    @EventTarget
    public void onKey(EventKey event) {
        if (!this.isToggled() || !this.enableHotkeysSetting) {
            return;
        }

        int keyCode = event.getKeyCode();
        MusicManager musicManager = Shindo.getInstance().getMusicManager();

        if (musicManager == null || !musicManager.isPlaying()) {
            return;
        }

        long lastVolumeChangeTime = 0L;
        if (keyCode == Keyboard.KEY_UP) {
            int currentVolume = musicManager.getVolume();
            int newVolume = Math.min(100, currentVolume + 5); // Increase by 5%, capped at 100%
            musicManager.setVolume(newVolume);
            lastVolumeChangeTime = System.currentTimeMillis();
        } else if (keyCode == Keyboard.KEY_DOWN) {
            int currentVolume = musicManager.getVolume();
            int newVolume = Math.max(0, currentVolume - 5); // Decrease by 5%, minimum 0%
            musicManager.setVolume(newVolume);
            lastVolumeChangeTime = System.currentTimeMillis();
        }

        // Fixed seeking implementation
        if (keyCode == Keyboard.KEY_RIGHT) {
            // Get current position directly from MusicManager to ensure it's up-to-date
            long currentPosition = musicManager.getTrackPosition();
            long duration = trackDuration > 0 ? trackDuration : Long.MAX_VALUE;
            // Skip forward 10 seconds (10,000 ms)
            long newPosition = Math.min(currentPosition + 10000, duration);
            ShindoLogger.info("Seeking from " + currentPosition + "ms to " + newPosition + "ms");
            musicManager.seekToPosition(newPosition);
        } else if (keyCode == Keyboard.KEY_LEFT) {
            // Get current position directly from MusicManager to ensure it's up-to-date
            long currentPosition = musicManager.getTrackPosition();
            // Skip backward 10 seconds (10,000 ms)
            long newPosition = Math.max(currentPosition - 10000, 0);
            ShindoLogger.info("Seeking from " + currentPosition + "ms to " + newPosition + "ms");
            musicManager.seekToPosition(newPosition);
        }
    }

    private void updateDynamicHeight() {
        LyricsManager lyricsManager;
        LyricsManager.LyricsResponse lyrics;
        MusicManager musicManager = Shindo.getInstance().getMusicManager();
        int baseHeight = 85;
        if (musicManager == null || !musicManager.isPlaying() || musicManager.getCurrentTrack() == null) {
            baseHeight = 75;
        } else if (this.showLyricsSetting && musicManager.getLyricsManager() != null && (lyrics = (lyricsManager = musicManager.getLyricsManager()).getCurrentLyrics()) != null && !lyrics.isError() && !lyrics.getLines().isEmpty()) {
            baseHeight = 110 + this.visibleLyrics * 12;
        }
        this.cachedHeight = baseHeight;
        this.setHeight(baseHeight);
    }

    private void updateLyrics(Track currentTrack, long position) {
        if (!this.showLyricsSetting || currentTrack == null) {
            return;
        }
        MusicManager musicManager = Shindo.getInstance().getMusicManager();
        if (musicManager == null || musicManager.getLyricsManager() == null) {
            return;
        }
        LyricsManager lyricsManager = musicManager.getLyricsManager();
        if (!currentTrack.getId().equals(this.currentTrackId)) {
            this.currentTrackId = currentTrack.getId();
            lyricsManager.reset();
            lyricsManager.fetchLyrics(currentTrack).thenAcceptAsync(lyrics -> {
                if (lyrics != null && !lyrics.isError() && !lyrics.getLines().isEmpty()) {
                    if (this.romanizeJapaneseSetting) {
                        lyricsManager.processLyricsRomanization(lyrics);
                    }
                }
            });
        }
        lyricsManager.updateCurrentLineIndex(position);
    }

    private void drawAdvancedNanoVG() {
        LyricsManager lyricsManager;
        LyricsManager.LyricsResponse lyrics;
        MusicManager musicManager = Shindo.getInstance().getMusicManager();
        boolean hasLyrics = false;
        int baseHeight = this.cachedHeight;
        if (this.showLyricsSetting && musicManager != null && musicManager.isPlaying() && musicManager.getCurrentTrack() != null && musicManager.getLyricsManager() != null && (lyrics = (lyricsManager = musicManager.getLyricsManager()).getCurrentLyrics()) != null && !lyrics.getLines().isEmpty()) {
            hasLyrics = true;
        }
        this.drawBackground(155.0f, baseHeight);
        if (musicManager.isPlaying() && musicManager.getCurrentTrack() != null) {
            Track currentTrack = musicManager.getCurrentTrack();
            String albumArtUrl = musicManager.getAlbumArtUrl(currentTrack);
            if (albumArtUrl != null && !albumArtUrl.isEmpty()) {
                File albumArtFile = new File(albumArtUrl);
                if (albumArtFile.exists()) {
                    this.drawRoundedImage(albumArtFile, 5.5f, 25.0f, 37.0f, 37.0f, 6.0f);
                } else {
                    this.drawRoundedImage(PLACEHOLDER_IMAGE, 5.5f, 25.0f, 37.0f, 37.0f, 6.0f);
                }
            } else {
                this.drawRoundedImage(PLACEHOLDER_IMAGE, 5.5f, 25.0f, 37.0f, 37.0f, 6.0f);
            }
            this.save();
            this.scissor(0.0f, 0.0f, 155.0f, baseHeight);
            this.drawText(TranslateText.NOW_PLAYING.getText(), 5.5f, 6.0f, 10.5f, this.getHudFont(3), new Color(255, 255, 255, 80));
            String trackName = currentTrack.getName();
            String artistNames = String.join(", ", Arrays.stream(currentTrack.getArtists()).map(ArtistSimplified::getName).toArray(String[]::new));
            List<String> trackNameLines = this.breakTextIntoLines(trackName, 95.0f);
            float trackNameY = 25.0f;
            for (String line : trackNameLines) {
                this.drawText(line, 47.0f, trackNameY, 10.5f, this.getHudFont(2), new Color(255, 255, 255, 80));
                trackNameY += 12.0f;
            }
            float artistY = trackNameY + 2.0f;
            this.drawText(artistNames, 47.0f, artistY, 9.5f, this.getHudFont(1), new Color(255, 255, 255, 80));
            this.restore();
            float current = musicManager.getCurrentTime();
            float end = musicManager.getEndTime();
            String currentTime = this.formatTime((long) current);
            String totalTime = this.formatTime((long) end);
            float progressBarY = 70.5f;
            float progressFactor = current / end;

            // Use improved progress bar design from main menu
            this.drawRoundedRect(6.0f, progressBarY, 142.5f, 2.5f, 1.3f,
                    new Color(255, 255, 255, 80));
            this.drawRoundedRect(6.0f, progressBarY, progressFactor * 142.5f, 2.5f, 1.3f,
                    new Color(255, 255, 255, 180));

            float timeY = progressBarY + 6.0f;
            this.drawText(currentTime, 6.0f, timeY, 6.0f, this.getHudFont(1));
            float totalTimeWidth = this.getTextWidth(totalTime, 9.0f, this.getHudFont(1));
            this.drawText(totalTime, 163.0f - totalTimeWidth - 5.5f, timeY, 6.0f, this.getHudFont(1));

            if (hasLyrics && this.showLyricsSetting) {
                float lyricsHeaderY = timeY + 15.0f;
                LyricsManager lyricsManager2 = musicManager.getLyricsManager();
                List<LyricsManager.LyricsLine> visibleLines = lyricsManager2.getVisibleLines(this.visibleLyrics);

                if (visibleLines != null && !visibleLines.isEmpty()) {
                    // Begin scissoring (clipping) for lyrics section
                    this.save();
                    float lyricsAreaHeight = baseHeight - lyricsHeaderY - 5.0f; // 5px padding at bottom
                    this.scissor(0, lyricsHeaderY, 145.0f, lyricsAreaHeight + 4.0f);

                    int currentLineIndex = lyricsManager2.getCurrentLineIndex();
                    this.updateLyricsScrollAnimation(currentLineIndex);
                    float lyricsY = lyricsHeaderY;
                    float lineHeight = 16.0f;
                    float yOffset = this.lyricsScrollOffset * lineHeight;

                    for (int i = 0; i < visibleLines.size(); ++i) {
                        LyricsManager.LyricsLine line = visibleLines.get(i);
                        if (line == null) continue;
                        int actualIndex = Math.max(0, currentLineIndex - visibleLines.size() / 2) + i;
                        boolean isCurrentLine = actualIndex == currentLineIndex;
                        String text = line.getWords();

                        // Use romanized text if available and the setting is enabled
                        if (this.romanizeJapaneseSetting && line.getRomanizedWords() != null) {
                            text = line.getRomanizedWords();
                        }

                        if (text != null && !text.isEmpty()) {
                            String limitedText = Shindo.getInstance().getNanoVGManager().getLimitText(text, 9.0f, this.getHudFont(1), 140.0f);
                            float xPos = 5.0f;
                            if (isCurrentLine) {
                                this.drawText(limitedText, xPos, lyricsY + yOffset, 9.0f, this.getHudFont(2), new Color(255, 255, 255, 180));
                            } else {
                                this.drawText(limitedText, xPos, lyricsY + yOffset, 9.0f, this.getHudFont(1), new Color(255, 255, 255, 80));
                            }
                        }
                        lyricsY += lineHeight;
                    }

                    // End scissoring
                    this.restore();
                } else {
                    String noLyricsText = "No lyrics available";
                    float textWidth = this.getTextWidth(noLyricsText, 10.0f, this.getHudFont(1));
                    float centerX = 77.5f;
                    this.drawText(noLyricsText, centerX - textWidth / 2.0f, lyricsHeaderY + 20.0f, 10.0f, this.getHudFont(1), new Color(200, 200, 200));
                }
            }
        } else {
            this.drawText(TranslateText.NOTHING_IS_PLAYING.getText(), 5.5f, 6.0f, 10.5f, this.getHudFont(3), new Color(255, 255, 255, 80));
            this.drawRoundedImage(PLACEHOLDER_IMAGE, 5.5f, 25.0f, 37.0f, 37.0f, 6.0f);
            float progressBarY = 67.5f;
            // Use consistent progress bar design even when nothing is playing
            this.drawRoundedRect(6.0f, progressBarY, 142.5f, 2.5f, 1.3f,
                    new Color(255, 255, 255, 80));
        }
        this.setWidth(155);
    }

    private String formatTime(long seconds) {
        long minutes = seconds / 60L;
        long remainingSeconds = seconds % 60L;
        return String.format("%02d:%02d", minutes, remainingSeconds);
    }

    private void updateLyricsScrollAnimation(int currentLineIndex) {
        if (currentLineIndex != this.prevLyricsLineIndex) {
            this.lyricsScrollOffset = currentLineIndex - this.prevLyricsLineIndex;
            this.lastLyricsScrollTime = System.currentTimeMillis();
            this.prevLyricsLineIndex = currentLineIndex;
        }

        if (this.lyricsScrollOffset != 0.0f) {
            long currentTime = System.currentTimeMillis();
            long timeSinceScroll = currentTime - this.lastLyricsScrollTime;
            if (timeSinceScroll >= LYRICS_SCROLL_DURATION) {
                this.lyricsScrollOffset = 0.0f;
            } else {
                float progress = (float) timeSinceScroll / LYRICS_SCROLL_DURATION;
                progress = this.easeOutCubic(progress);
                this.lyricsScrollOffset = (1.0f - progress) * this.lyricsScrollOffset;
            }
        }
    }

    private float easeOutCubic(float t) {
        return 1.0f - (float) Math.pow(1.0f - t, 3.0);
    }

    private List<String> breakTextIntoLines(String text, float maxWidth) {
        ArrayList<String> lines = new ArrayList<String>();
        NanoVGManager nvgManager = Shindo.getInstance().getNanoVGManager();
        if (this.getTextWidth(text, 10.5f, this.getHudFont(1)) <= maxWidth) {
            lines.add(text);
            return lines;
        }
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();
        for (String word : words) {
            String testLine;
            String string = testLine = currentLine.length() > 0 ? currentLine + " " + word : word;
            if (this.getTextWidth(testLine, 10.5f, this.getHudFont(1)) <= maxWidth) {
                currentLine = new StringBuilder(testLine);
                continue;
            }
            if (currentLine.length() > 0) {
                lines.add(currentLine.toString());
            }
            currentLine = new StringBuilder(word);
        }
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }
        if (lines.size() > 2) {
            String lastLine = lines.get(1);
            if (lastLine.length() > 3) {
                lines.set(1, lastLine.substring(0, lastLine.length() - 3) + "...");
            }
            return lines.subList(0, 2);
        }
        return lines;
    }

    @Override
    public String getText() {
        MusicManager musicManager = Shindo.getInstance().getMusicManager();
        if (musicManager.isPlaying()) {
            Track currentTrack = musicManager.getCurrentTrack();
            return currentTrack != null ? "Now Playing: " + currentTrack.getName() : "Nothing is Playing";
        }
        return "Nothing is Playing";
    }

    @Override
    public String getIcon() {
        return this.iconSetting ? "9" : null;
    }

    @Override
    public void onTrackInfoUpdated(long position, long duration) {
        this.trackDuration = duration;
        MusicManager musicManager = Shindo.getInstance().getMusicManager();
        if (musicManager != null && musicManager.getLyricsManager() != null) {
            musicManager.getLyricsManager().updateCurrentLineIndex(position);
            int newLineIndex = musicManager.getLyricsManager().getCurrentLineIndex();
            if (newLineIndex != this.prevLyricsLineIndex) {
                this.lyricsScrollOffset = newLineIndex - this.prevLyricsLineIndex;
                this.lastLyricsScrollTime = System.currentTimeMillis();
                this.prevLyricsLineIndex = newLineIndex;
            }
        }
    }

    public BooleanSetting getShowLyricsSetting() {
        return SettingRegistry.getBooleanSetting(this, "showLyricsSetting");
    }

    public TextSetting getLyricsApiUrlSetting() {
        return SettingRegistry.getTextSetting(this, "lyricsApiUrlSetting");
    }

    public BooleanSetting getRomanizeJapaneseSetting() {
        return SettingRegistry.getBooleanSetting(this, "romanizeJapaneseSetting");
    }

    public BooleanSetting getEnableHotkeysSetting() {
        return SettingRegistry.getBooleanSetting(this, "enableHotkeysSetting");
    }

    public enum Design implements PropertyEnum {
        SIMPLE(TranslateText.SIMPLE),
        ADVANCED(TranslateText.ADVANCED);

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

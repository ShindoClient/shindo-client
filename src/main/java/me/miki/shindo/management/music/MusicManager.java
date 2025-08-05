package me.miki.shindo.management.music;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javafx.embed.swing.JFXPanel;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import me.miki.mp3agic.Mp3File;
import me.miki.mp3agic.interfaces.ID3v2;
import me.miki.shindo.Shindo;
import me.miki.shindo.logger.ShindoLogger;
import me.miki.shindo.management.file.FileManager;
import me.miki.shindo.management.mods.impl.InternalSettingsMod;
import me.miki.shindo.management.music.ytdlp.YTDLP;
import me.miki.shindo.utils.ImageUtils;
import me.miki.shindo.utils.JsonUtils;
import me.miki.shindo.utils.Multithreading;
import me.miki.shindo.utils.RandomUtils;
import me.miki.shindo.utils.file.FileUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;




public class MusicManager {

    private final CopyOnWriteArrayList<MusicData> musics = new CopyOnWriteArrayList<MusicData>();
    private final YTDLP ytdlp = new YTDLP();

    private MusicData currentMusic;
    private Media media;
    private MediaPlayer mediaPlayer;

    public MusicManager() {
        load();
        loadData();
    }

    public void loadData() {

        FileManager fileManager = Shindo.getInstance().getFileManager();
        File cacheDir = new File(fileManager.getCacheDir(), "music");
        File dataJson = new File(cacheDir, "Data.json");

        ArrayList<String> favorites = new ArrayList<String>();

        if (!dataJson.exists()) {
            fileManager.createFile(dataJson);
        }

        try (FileReader reader = new FileReader(dataJson)) {

            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);

            if (jsonObject != null) {

                JsonArray jsonArray = JsonUtils.getArrayProperty(jsonObject, "Favorite Musics");

                if (jsonArray != null) {

                    for (JsonElement jsonElement : jsonArray) {

                        JsonObject rJsonObject = gson.fromJson(jsonElement, JsonObject.class);

                        favorites.add(JsonUtils.getStringProperty(rJsonObject, "Favorite", "null"));
                    }
                }
            }
        } catch (Exception e) {
            ShindoLogger.error("Failed to load music data", e);
        }

        for (MusicData m : musics) {
            if (favorites.contains(m.getName())) {
                m.setType(MusicType.FAVORITE);
            }
        }
    }

    public void saveData() {

        FileManager fileManager = Shindo.getInstance().getFileManager();
        File cacheDir = new File(fileManager.getCacheDir(), "music");
        File dataJson = new File(cacheDir, "Data.json");

        if (!dataJson.exists()) {
            fileManager.createFile(dataJson);
        }

        try (FileWriter writer = new FileWriter(dataJson)) {

            JsonObject jsonObject = new JsonObject();
            JsonArray jsonArray = new JsonArray();
            Gson gson = new Gson();

            for (MusicData m : musics) {

                if (m.getType().equals(MusicType.FAVORITE)) {

                    JsonObject innerJsonObject = new JsonObject();

                    innerJsonObject.addProperty("Favorite", m.getName());

                    jsonArray.add(innerJsonObject);
                }
            }

            jsonObject.add("Favorite Musics", jsonArray);

            gson.toJson(jsonObject, writer);

        } catch (Exception e) {
            ShindoLogger.error("Failed to save music", e);
        }
    }

    public void play() {

        if(currentMusic == null) {
            return;
        }

        if(mediaPlayer != null) {
            mediaPlayer.dispose();
        }

        if(media == null || mediaPlayer == null) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    new JFXPanel();
                }
            });
        }

        media = new Media(currentMusic.getAudio().toURI().toString());
        mediaPlayer = new MediaPlayer(media);

        mediaPlayer.setOnEndOfMedia(new Runnable() {
            @Override
            public void run() {
                stop();
                currentMusic = musics.get(RandomUtils.getRandomInt(0, musics.size() - 1));
                play();
            }
        });

        mediaPlayer.play();
        setVolume();
    }

    public void setVolume() {
        if(mediaPlayer != null) {
            mediaPlayer.setVolume(InternalSettingsMod.getInstance().getVolumeSetting().getValue());
        }
    }

    public void next() {

        if(currentMusic == null) {
            return;
        }

        int max = musics.size();
        int index = musics.indexOf(currentMusic);

        if(index < max - 1) {
            index++;
        }else {
            index = 0;
        }

        currentMusic = musics.get(index);
        play();
    }

    public void back() {

        if(currentMusic == null) {
            return;
        }

        int max = musics.size();
        int index = musics.indexOf(currentMusic);

        if(index > 0) {
            index--;
        }else {
            index = max - 1;
        }

        currentMusic = musics.get(index);
        play();
    }

    public void switchPlayBack() {
        if(mediaPlayer != null) {
            if(mediaPlayer.getStatus().equals(Status.PAUSED)) {
                mediaPlayer.play();
            }else if(mediaPlayer.getStatus().equals(Status.PLAYING)) {
                mediaPlayer.pause();
            }
        }
    }

    public void stop() {
        if(mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }

    public boolean isPlaying() {

        if(mediaPlayer == null) {
            return false;
        }

        return mediaPlayer.getStatus().equals(Status.PLAYING);
    }

    public float getCurrentTime() {

        if(mediaPlayer == null) {
            return 0;
        }

        return (float) mediaPlayer.getCurrentTime().toSeconds();
    }

    public float getEndTime() {

        if(mediaPlayer == null) {
            return 0;
        }

        return (float) mediaPlayer.getMedia().getDuration().toSeconds();
    }

    public void load() {

        FileManager fileManager = Shindo.getInstance().getFileManager();
        File musicDir = fileManager.getMusicDir();
        File cacheDir = new File(fileManager.getCacheDir(), "music");

        if (!cacheDir.exists()) {
            fileManager.createDir(cacheDir);
        }

        for (File f : Objects.requireNonNull(musicDir.listFiles())) {

            if (FileUtils.getExtension(f).equals("mp3")) {

                File imageFile = new File(cacheDir, f.getName().replace(".mp3", ""));

                if (!imageFile.exists()) {

                    try {

                        Mp3File mp3File = new Mp3File(f);

                        if (mp3File.hasId3v2Tag()) {

                            ID3v2 id3v2tag = mp3File.getId3v2Tag();

                            if (id3v2tag.getAlbumImage() != null) {

                                byte[] imageData = id3v2tag.getAlbumImage();

                                FileOutputStream fos = new FileOutputStream(imageFile);

                                fos.write(imageData);
                                fos.close();

                                BufferedImage original = ImageIO.read(imageFile);
                                BufferedImage cropped = ImageUtils.cropCenterHorizontal(original, 256);
                                ImageIO.write(cropped, "png", imageFile);
                            }
                        }

                    } catch (Exception e) {
                        ShindoLogger.error("An error occurred while processing the music file: " + f.getName(), e);
                    }
                }
            }
        }

        for (File f : Objects.requireNonNull(musicDir.listFiles())) {
            if (FileUtils.isAudioFile(f)) {

                if (getMusicByAudioFile(f) != null) {
                    continue;
                }

                if (FileUtils.getExtension(f).equals("mp3")) {

                    File imageFile = new File(cacheDir, f.getName().replace(".mp3", ""));

                    if (imageFile.exists()) {
                        musics.add(new MusicData(f, imageFile, MusicType.ALL));
                    } else {
                        musics.add(new MusicData(f, null, MusicType.ALL));
                    }
                } else {
                    musics.add(new MusicData(f, null, MusicType.ALL));
                }
            }
        }
    }


    public void loadAsync() {
        Multithreading.runAsync(this::load);
    }

    public MusicData getMusicByName(String name) {

        for (MusicData m : musics) {
            if (m.getName().equals(name)) {
                return m;
            }
        }

        return null;
    }

    public MusicData getMusicByAudioFile(File file) {

        for (MusicData m : musics) {
            if (m.getAudio().equals(file)) {
                return m;
            }
        }

        return null;
    }

    public void delete(MusicData m) {
        musics.remove(m);
        m.getAudio().delete();
        load();
    }

    public CopyOnWriteArrayList<MusicData> getMusics() {
        return musics;
    }

    public MusicData getCurrentMusic() {
        return currentMusic;
    }

    public void setCurrentMusic(MusicData currentMusic) {
        this.currentMusic = currentMusic;
    }

    public YTDLP getYtdlp() {
        return ytdlp;
    }
}

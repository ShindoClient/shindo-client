package me.miki.shindo.utils;

import lombok.Getter;
import me.miki.shindo.management.mods.impl.InternalSettingsMod;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.net.URL;

public class Sound {

    private final Minecraft mc = Minecraft.getMinecraft();

    @Getter
    private Clip clip;

    public static void play(String location, boolean uiSound) {
        if (uiSound && !InternalSettingsMod.getInstance().getSoundsUISetting().isToggled()) return;
        URL diskPath = Sound.class.getClassLoader().getResource("assets/minecraft/" + location);
        if (diskPath != null) {
            try {
                Clip clip = AudioSystem.getClip();
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(diskPath);
                clip.open(audioInputStream);
                clip.start();

                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close();
                        try {
                            audioInputStream.close();
                        } catch (Exception ignored) {
                        }
                    }
                });
            } catch (Exception ignored) {
            }
        }
    }

    public void loadClip(ResourceLocation location) throws Exception {
        clip = AudioSystem.getClip();
        clip.open(AudioSystem.getAudioInputStream(new BufferedInputStream(mc.getResourceManager().getResource(location).getInputStream())));
    }

    public void loadClip(File file) throws Exception {
        clip = AudioSystem.getClip();
        clip.open(AudioSystem.getAudioInputStream(file));
    }

    public void play() {
        if (clip != null) {
            clip.stop();
            clip.setFramePosition(0);
            clip.start();
        }
    }

    public void setVolume(float volume) {

        if (clip == null) {
            return;
        }

        FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);

        float dB = (float) (Math.log(volume) / Math.log(10.0) * 20.0);
        gainControl.setValue(dB);
    }
}

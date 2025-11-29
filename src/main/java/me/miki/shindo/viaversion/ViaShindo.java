package me.miki.shindo.viaversion;

import lombok.Getter;
import me.miki.shindo.Shindo;
import me.miki.shindo.management.file.FileManager;
import me.miki.shindo.viaversion.gui.AsyncVersionSlider;

import java.io.File;

public class ViaShindo {

    public final static int NATIVE_VERSION = 47;

    @Getter
    private static ViaShindo instance;

    @Getter
    private AsyncVersionSlider asyncVersionSlider;

    public ViaShindo() {

        FileManager fileManager = Shindo.getInstance().getFileManager();

        ViaLoadingBase.ViaLoadingBaseBuilder.create().runDirectory(new File(fileManager.getShindoDir(), "ViaVersion")).nativeVersion(NATIVE_VERSION).onProtocolReload(comparableProtocolVersion -> {
            if (getAsyncVersionSlider() != null) {
                getAsyncVersionSlider().setVersion(comparableProtocolVersion.getVersion());
            }
        }).build();
    }

    public static void create() {
        instance = new ViaShindo();
    }

    public void initAsyncSlider() {
        this.initAsyncSlider(5, 5, 110, 20);
    }

    public void initAsyncSlider(int x, int y, int width, int height) {
        asyncVersionSlider = new AsyncVersionSlider(-1, x, y, Math.max(width, 110), height);
    }

}

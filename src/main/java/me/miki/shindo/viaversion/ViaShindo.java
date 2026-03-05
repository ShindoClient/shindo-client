package me.miki.shindo.viaversion;

import kotlin.Unit;
import me.miki.shindo.Shindo;
import me.miki.shindo.management.file.FileManager;
import me.miki.shindo.viaversion.gui.AsyncVersionSlider;

import java.io.File;

/**
 * Ponte client-side que conecta o Shindo ao viashindo (me.miki.viashindo).
 * Mantém o AsyncVersionSlider e delega a lógica de protocolo para viashindo.
 */
public class ViaShindo {

    private static ViaShindo instance;
    private AsyncVersionSlider asyncVersionSlider;

    public static void create() {
        instance = new ViaShindo();
        FileManager fm = Shindo.getInstance().getFileManager();
        File runDir = new File(fm.getShindoDir(), "ViaVersion");
        me.miki.viashindo.ViaShindo.create(runDir, cv -> {
            if (instance != null && instance.asyncVersionSlider != null) {
                instance.asyncVersionSlider.setVersion(cv.getVersion());
            }
            return Unit.INSTANCE;
        });
    }

    public static ViaShindo getInstance() {
        return instance;
    }

    public AsyncVersionSlider getAsyncVersionSlider() {
        return asyncVersionSlider;
    }

    public void initAsyncSlider() {
        this.initAsyncSlider(5, 5, 110, 20);
    }

    public void initAsyncSlider(int x, int y, int width, int height) {
        asyncVersionSlider = new AsyncVersionSlider(-1, x, y, Math.max(width, 110), height);
    }
}

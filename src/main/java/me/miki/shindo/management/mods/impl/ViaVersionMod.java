package me.miki.shindo.management.mods.impl;


import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import me.miki.shindo.utils.Multithreading;
import me.miki.shindo.viaversion.ViaLoadingBase;
import me.miki.shindo.viaversion.ViaShindo;
import me.miki.shindo.viaversion.protocolinfo.ProtocolInfo;

public class ViaVersionMod extends Mod {

    private static ViaVersionMod instance;

    private boolean loaded;

    public ViaVersionMod() {
        super(TranslateText.VIA_VERSION, TranslateText.VIA_VERSION_DESCRIPTION, ModCategory.OTHER);

        instance = this;
        loaded = false;
    }

    public static ViaVersionMod getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        if (!loaded) {
            loaded = true;
            Multithreading.runAsync(() -> {
                ViaShindo.create();
                ViaShindo.getInstance().initAsyncSlider();
            });
        }
    }

    @Override
    public void onDisable() {

        super.onDisable();

        if (loaded) {
            ViaShindo.getInstance().getAsyncVersionSlider().setVersion(ProtocolInfo.R1_8.getProtocolVersion().getVersion());
            ViaLoadingBase.getInstance().reload(ProtocolInfo.R1_8.getProtocolVersion());
        }
    }

    public boolean isLoaded() {
        return loaded;
    }
}

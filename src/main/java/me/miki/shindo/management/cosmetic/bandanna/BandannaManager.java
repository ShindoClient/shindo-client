package me.miki.shindo.management.cosmetic.bandanna;

import lombok.Getter;
import me.miki.shindo.Shindo;
import me.miki.shindo.api.roles.Role;
import me.miki.shindo.api.roles.RoleManager;
import me.miki.shindo.management.cosmetic.bandanna.impl.Bandanna;
import me.miki.shindo.management.cosmetic.bandanna.impl.NormalBandanna;
import me.miki.shindo.management.file.FileManager;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.impl.InternalSettingsMod;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

@Getter
public class BandannaManager {

    private final ArrayList<Bandanna> bandannas = new ArrayList<Bandanna>();
    private Bandanna currentBandanna;

    public BandannaManager() {

        Shindo instance = Shindo.getInstance();
        FileManager fileManager = instance.getFileManager();
        File cacheDir = fileManager.getBandannaCacheDir();

        bandannas.add(new NormalBandanna("None", null, null, BandannaCategory.ALL, Role.MEMBER));
        currentBandanna = getBandannaByName(InternalSettingsMod.getInstance().getBandannaConfigName());

        for (Bandanna c : bandannas) {

            if (c instanceof NormalBandanna) {

                NormalBandanna bandanna = (NormalBandanna) c;

                if (bandanna.getSample() != null) {
                    instance.getNanoVGManager().loadImage(bandanna.getSample());
                }
            }

            if (c.getBandanna() != null) {
                Minecraft mc = Minecraft.getMinecraft();
                mc.getTextureManager().bindTexture(c.getBandanna());
            }
        }
    }

    private void add(String name, String samplePath, String bandannaPath, BandannaCategory category, Role requiredRole) {

        String cosmeticPath = "shindo/cosmetics/bandanna/";

        bandannas.add(new NormalBandanna(name, new ResourceLocation(cosmeticPath + samplePath), new ResourceLocation(cosmeticPath + bandannaPath), category, requiredRole));
    }


    public ArrayList<Bandanna> getBandannas() {
        return bandannas;
    }

    public Bandanna getCurrentBandanna() {
        return currentBandanna;
    }

    public void setCurrentBandanna(Bandanna currentBandanna) {
        this.currentBandanna = currentBandanna;
        InternalSettingsMod.getInstance().setBandannaConfigName(currentBandanna.getName());
    }

    public Bandanna getBandannaByName(String name) {

        for (Bandanna c : bandannas) {
            if (c.getName().equals(name)) {
                return c;
            }
        }

        return getBandannaByName("None");
    }

    public boolean canUseBandanna(UUID uuid, Bandanna bandanna) {
        return RoleManager.hasAtLeast(uuid, bandanna.getRequiredRole());
    }

    public TranslateText getTranslateError(Role role) {
        switch (role) {
            case STAFF:
                return TranslateText.STAFF_ONLY;
            case DIAMOND:
                return TranslateText.DIAMOND_ONLY;
            case GOLD:
                return TranslateText.GOLD_ONLY;
            default:
                return TranslateText.NONE;

        }
    }

    public TranslateText getTranslateText(Role role) {
        switch (role) {
            case STAFF:
                return TranslateText.STAFF;
            case DIAMOND:
                return TranslateText.DIAMOND;
            case GOLD:
                return TranslateText.GOLD;
            case MEMBER:
                return TranslateText.MEMBER;
            default:
                return TranslateText.NONE;

        }
    }
}

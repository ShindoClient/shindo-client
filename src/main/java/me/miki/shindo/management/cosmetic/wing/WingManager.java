package me.miki.shindo.management.cosmetic.wing;

import lombok.Getter;
import me.miki.shindo.Shindo;
import me.miki.shindo.api.roles.Role;
import me.miki.shindo.api.roles.RoleManager;
import me.miki.shindo.management.cosmetic.wing.impl.NormalWing;
import me.miki.shindo.management.cosmetic.wing.impl.Wing;
import me.miki.shindo.management.file.FileManager;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.impl.InternalSettingsMod;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

@Getter
public class WingManager {

    private final ArrayList<Wing> wings = new ArrayList<Wing>();

    private Wing currentWing;

    public WingManager() {

        wings.add(new NormalWing("None", null, null, WingCategory.ALL, Role.MEMBER));


        //add("Blue Wing", "lunar/samples/blue-sample.webp", "lunar/blue-wing.webp", WingCategory.LUNAR, Role.MEMBER);

        Shindo instance = Shindo.getInstance();
        FileManager fileManager = instance.getFileManager();
        File cacheDir = fileManager.getWingCacheDir();

        currentWing = getWingByName(InternalSettingsMod.getInstance().getWingConfigName());

        for (Wing c : wings) {

            if (c instanceof NormalWing) {

                NormalWing wing = (NormalWing) c;

                if (wing.getSample() != null) {
                    instance.getNanoVGManager().loadImage(wing.getSample());
                }
            }
            if (c.getWing() != null) {
                Minecraft mc = Minecraft.getMinecraft();
                mc.getTextureManager().bindTexture(c.getWing());
            }
        }
    }

    private void add(String name, String samplePath, String wingPath, WingCategory category, Role requiredRole) {

        String cosmeticPath = "shindo/cosmetics/wings/";

        wings.add(new NormalWing(name, new ResourceLocation(cosmeticPath + samplePath), new ResourceLocation(cosmeticPath + wingPath), category, requiredRole));
    }

    public void setCurrentWing(Wing currentWing) {
        this.currentWing = currentWing;
        InternalSettingsMod.getInstance().setWingConfigName(currentWing.getName());
    }

    public Wing getWingByName(String name) {

        for (Wing c : wings) {
            if (c.getName().equals(name)) {
                return c;
            }
        }

        return getWingByName("None");
    }

    public boolean canUseWing(UUID uuid, Wing wing) {
        return RoleManager.hasAtLeast(uuid, wing.getRequiredRole());
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

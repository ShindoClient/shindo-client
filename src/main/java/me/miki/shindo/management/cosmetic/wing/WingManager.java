package me.miki.shindo.management.cosmetic.wing;

import lombok.Getter;
import me.miki.shindo.Shindo;
import me.miki.shindo.api.roles.Role;
import me.miki.shindo.api.roles.RoleManager;
import me.miki.shindo.management.cosmetic.wing.impl.NormalWing;
import me.miki.shindo.management.cosmetic.wing.impl.Wing;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.impl.InternalSettingsMod;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.UUID;

@Getter
public class WingManager {

    private static final String BASE_PATH = "shindo/cosmetics/wings/";

    private final ArrayList<Wing> wings = new ArrayList<Wing>();

    private Wing currentWing;

    public WingManager() {

        wings.add(new NormalWing("None", null, null, WingCategory.ALL, Role.MEMBER));

        add("Blue Wing", "lunar/samples/blue-sample.png", "lunar/blue-wing.png", WingCategory.LUNAR, Role.MEMBER);
        add("Green Wing", "lunar/samples/green-sample.png", "lunar/green-wing.png", WingCategory.LUNAR, Role.MEMBER);
        add("Maroon Wing", "lunar/samples/maroon-sample.png", "lunar/maroon-wing.png", WingCategory.LUNAR, Role.MEMBER);
        add("Red Wing", "lunar/samples/red-sample.png", "lunar/red-wing.png", WingCategory.LUNAR, Role.MEMBER);
        add("White Wing", "lunar/samples/white-sample.png", "lunar/white-wing.png", WingCategory.LUNAR, Role.MEMBER);
        add("Yellow Wing", "lunar/samples/yellow-sample.png", "lunar/yellow-wing.png", WingCategory.LUNAR, Role.MEMBER);

        currentWing = getWingByName(InternalSettingsMod.getInstance().getWingConfigName());
        if (currentWing == null) {
            currentWing = wings.get(0);
        }

        Shindo instance = Shindo.getInstance();
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
        ResourceLocation sample = samplePath != null ? new ResourceLocation(BASE_PATH + samplePath) : null;
        ResourceLocation wing = wingPath != null ? new ResourceLocation(BASE_PATH + wingPath) : null;
        wings.add(new NormalWing(name, sample, wing, category, requiredRole));
    }

    public void setCurrentWing(Wing currentWing) {
        if (currentWing == null) {
            return;
        }
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

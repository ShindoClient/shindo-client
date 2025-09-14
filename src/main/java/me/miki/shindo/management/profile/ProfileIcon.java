package me.miki.shindo.management.profile;

import lombok.Getter;
import me.miki.shindo.utils.animation.simple.SimpleAnimation;
import net.minecraft.util.ResourceLocation;

@Getter
public enum ProfileIcon {
    COMMAND(0, "command"), CRAFTING_TABLE(1, "crafting_table"), FURNACE(2, "furnace"), GRASS(3, "grass"),
    HAY(4, "hay"), PUMPKIN(5, "pumpkin"), TNT(6, "tnt");

    private final SimpleAnimation animation = new SimpleAnimation();

    private final int id;
    private final ResourceLocation icon;

    ProfileIcon(int id, String name) {
        this.id = id;
        this.icon = new ResourceLocation("shindo/icons/" + name + ".png");
    }

    public static ProfileIcon getIconById(int id) {

        for (ProfileIcon pi : ProfileIcon.values()) {
            if (pi.getId() == id) {
                return pi;
            }
        }

        return ProfileIcon.GRASS;
    }

}

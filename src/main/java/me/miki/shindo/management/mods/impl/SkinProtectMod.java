package me.miki.shindo.management.mods.impl;

import com.mojang.util.UUIDTypeAdapter;
import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventLocationSkin;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import net.minecraft.util.ResourceLocation;

public class SkinProtectMod extends Mod {

    public SkinProtectMod() {
        super(TranslateText.SKIN_PROTECT, TranslateText.SKIN_PROTECT_DESCRIPTION, ModCategory.PLAYER, "nickhider");
    }

    @EventTarget
    public void onLocationSkin(EventLocationSkin event) {

        String uuid = UUIDTypeAdapter.fromUUID(event.getPlayerInfo().getGameProfile().getId());
        String pUuid = UUIDTypeAdapter.fromUUID(mc.thePlayer.getGameProfile().getId());

        if (uuid.equals(pUuid)) {
            event.setCancelled(true);
            event.setSkin(new ResourceLocation("textures/entity/steve.png"));
        }
    }
}

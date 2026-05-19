package me.miki.shindo.management.mods.impl

import com.mojang.util.UUIDTypeAdapter
import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventLocationSkin
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.nanovg.font.Lucide
import me.miki.shindo.management.nanovg.font.Shinconic
import net.minecraft.util.ResourceLocation

class SkinProtectMod :
    Mod(
        TranslateText.SKIN_PROTECT,
        TranslateText.SKIN_PROTECT_DESCRIPTION,
        ModCategory.PLAYER,
        Shinconic.MOD_SKIN_PROTECT,
        "nickhider",
    ) {
    @EventTarget
    fun onLocationSkin(event: EventLocationSkin) {
        val uuid = UUIDTypeAdapter.fromUUID(event.getPlayerInfo().gameProfile.id)
        val pUuid = UUIDTypeAdapter.fromUUID(mc.thePlayer.gameProfile.id)

        if (uuid == pUuid) {
            event.setCancelled(true)
            event.setSkin(ResourceLocation("textures/entity/steve.png"))
        }
    }
}

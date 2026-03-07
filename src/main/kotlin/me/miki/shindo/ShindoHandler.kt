package me.miki.shindo

import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.gui.modmenu.GuiModMenu
import me.miki.shindo.management.event.impl.*
import me.miki.shindo.management.profile.Profile
import me.miki.shindo.management.skin.Skin
import me.miki.shindo.management.skin.SkinManager
import me.miki.shindo.utils.OptifineUtils
import me.miki.shindo.utils.TargetUtils
import net.minecraft.client.Minecraft
import net.minecraft.network.play.server.S2EPacketCloseWindow
import org.apache.commons.lang3.StringUtils

@Suppress("unused", "UNUSED_PARAMETER")
class ShindoHandler {

    private val mc: Minecraft = Minecraft.getMinecraft()
    private val instance: Shindo = Shindo.getInstance()
    @EventTarget
    fun onTick(event: EventTick) {
        OptifineUtils.disableFastRender()
    }

    @EventTarget
    fun onJoinServer(event: EventJoinServer) {
        for (p: Profile in  instance.profileManager.profiles) {
            val serverIp = p.serverIp ?: return
            if (serverIp.isNotEmpty() && StringUtils.containsIgnoreCase(event.getIp(), serverIp)) {
                instance.modManager.disableAll()
                p.jsonFile.let { instance.profileManager.load(it) }
                break
            }
        }
        instance.restrictedMod.joinServer(event.getIp())
    }

    @EventTarget
    fun onLoadWorld(event: EventLoadWorld) {
        instance.restrictedMod.joinWorld()
    }

    @EventTarget
    fun onUpdate(event: EventUpdate) {
        TargetUtils.onUpdate()
    }

    @EventTarget
    fun onClickMouse(event: EventClickMouse) {
        if (mc.gameSettings.keyBindTogglePerspective.isPressed) {
            mc.gameSettings.thirdPersonView = (mc.gameSettings.thirdPersonView + 1) % 3
            mc.renderGlobal.setDisplayListEntitiesDirty()
        }
    }

    @EventTarget
    fun onReceivePacket(event: EventReceivePacket) {
        if (event.getPacket() is S2EPacketCloseWindow && mc.currentScreen is GuiModMenu) {
            event.setCancelled(true)
        }
    }

    @EventTarget
    fun onCape(event: EventLocationCape) {
        val capeManager = instance.capeManager
        val playerInfo = event.getPlayerInfo() ?: return
        if (playerInfo.gameProfile.id == mc.thePlayer.gameProfile.id) {
            val currentCape = capeManager.getCurrentCape()
            if (currentCape != capeManager.getCapeByName("None")) {
                event.setCancelled(true)
                event.setCape(currentCape!!.getCape())
            }
        }
    }


    @EventTarget
    fun onSkin(event: EventLocationSkin) {
        val player = mc.thePlayer ?: return
        val info = event.getPlayerInfo() ?: return
        val profile = info.gameProfile ?: return
        if (profile.id != player.gameProfile.id) return

        val skinManager: SkinManager = instance.skinManager
        val skin: Skin? = skinManager.getCurrentSkin()
        if (skin?.texture == null) return

        event.setCancelled(true)
        event.setSkin(skin.texture!!)
    }
}

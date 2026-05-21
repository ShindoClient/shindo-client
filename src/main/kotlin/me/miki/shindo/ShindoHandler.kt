package me.miki.shindo

import me.miki.shindo.gui.modmenu.v2.GuiModMenu
import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventClickMouse
import me.miki.shindo.management.event.impl.EventJoinServer
import me.miki.shindo.management.event.impl.EventLoadWorld
import me.miki.shindo.management.event.impl.EventLocationCape
import me.miki.shindo.management.event.impl.EventLocationSkin
import me.miki.shindo.management.event.impl.EventReceivePacket
import me.miki.shindo.management.event.impl.EventTick
import me.miki.shindo.management.event.impl.EventUpdate
import me.miki.shindo.management.profile.Profile
import me.miki.shindo.management.skin.Skin
import me.miki.shindo.management.skin.SkinManager
import me.miki.shindo.utils.OptifineUtils
import me.miki.shindo.utils.TargetUtils
import net.minecraft.client.Minecraft
import net.minecraft.network.play.server.S2EPacketCloseWindow
import org.apache.commons.lang3.StringUtils

@Suppress("UNUSED")
class ShindoHandler {
    private val mc: Minecraft = Minecraft.getMinecraft()
    private val instance: Shindo = Shindo.getInstance()

    @EventTarget
    fun onTick(event: EventTick) {
        OptifineUtils.disableFastRender()
    }

    @EventTarget
    fun onJoinServer(event: EventJoinServer) {
        for (p: Profile in instance.getProfileManager().profiles) {
            val serverIp = p.serverIp ?: return
            if (serverIp.isNotEmpty() && StringUtils.containsIgnoreCase(event.getIp(), serverIp)) {
                instance.getModManager().disableAll()
                p.jsonFile.let { instance.getProfileManager().load(it) }
                break
            }
        }
        instance.getRestrictedMod().joinServer(event.getIp())
    }

    @EventTarget
    fun onLoadWorld(event: EventLoadWorld) {
        instance.getRestrictedMod().joinWorld()
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
        val capeManager = instance.getCapeManager()
        val playerInfo = event.getPlayerInfo()
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
        val info = event.getPlayerInfo()
        val profile = info.gameProfile ?: return
        if (profile.id != player.gameProfile.id) return

        val skinManager: SkinManager = instance.getSkinManager()
        val skin: Skin? = skinManager.getCurrentSkin()
        if (skin?.texture == null) return

        event.setCancelled(true)
        event.setSkin(skin.texture!!)
    }
}

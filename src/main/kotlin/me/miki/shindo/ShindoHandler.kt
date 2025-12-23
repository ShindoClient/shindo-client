package me.miki.shindo

import me.miki.shindo.gui.modmenu.GuiModMenu
import me.miki.shindo.management.cosmetic.cape.CapeManager
import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.*
import me.miki.shindo.management.profile.Profile
import me.miki.shindo.management.skin.Skin
import me.miki.shindo.management.skin.SkinManager
import me.miki.shindo.utils.OptifineUtils
import me.miki.shindo.utils.TargetUtils
import net.minecraft.client.Minecraft
import net.minecraft.network.play.server.S2EPacketCloseWindow
import net.minecraft.util.ResourceLocation
import org.apache.commons.lang3.StringUtils

class ShindoHandler {

    private val mc: Minecraft = Minecraft.getMinecraft()
    private val instance: Shindo = Shindo.getInstance()

    private var prevOfflineName: String? = null
    private var offlineSkin: ResourceLocation? = null

    @EventTarget
    fun onTick(event: EventTick) {
        OptifineUtils.disableFastRender()
    }

    @EventTarget
    fun onJoinServer(event: EventJoinServer) {
        for (p: Profile in profiles()) {
            val serverIp = serverIp(p)
            if (serverIp.isNotEmpty() && StringUtils.containsIgnoreCase(event.ip, serverIp)) {
                instance.modManager.disableAll()
                jsonFile(p)?.let { instance.profileManager.load(it) }
                break
            }
        }
        instance.restrictedMod.joinServer(event.ip)
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
        val pkt = packet(event)
        if (pkt is S2EPacketCloseWindow && mc.currentScreen is GuiModMenu) {
            event.isCancelled = true
        }
    }

    @EventTarget
    fun onCape(event: EventLocationCape) {
        val capeManager: CapeManager = instance.capeManager
        val playerInfo = event.playerInfo ?: return
        if (playerInfo.gameProfile.id == mc.thePlayer.gameProfile.id) {
            val currentCape = currentCape(capeManager)
            val capeTex = currentCape?.capeTexture()
            if (currentCape != null && currentCape != capeManager.getCapeByName("None")) {
                event.isCancelled = true
                if (capeTex != null) {
                    event.cape = capeTex
                }
            }
        }
    }

    @EventTarget
    fun onSkin(event: EventLocationSkin) {
        val player = mc.thePlayer ?: return
        val info = event.playerInfo ?: return
        val profile = info.gameProfile ?: return
        if (profile.id != player.gameProfile.id) return

        val skinManager: SkinManager = instance.skinManager
        val skin: Skin? = skinManager.currentSkin
        if (skin == null || skin.texture == null) return

        event.isCancelled = true
        event.skin = skin.texture
    }

    private fun profiles(): List<Profile> = instance.profileManager.profiles.toList()
    private fun serverIp(profile: Profile): String = profile.serverIp ?: ""
    private fun jsonFile(profile: Profile): java.io.File? = profile.jsonFile

    private fun packet(event: EventReceivePacket): Any? = try {
        val field = event.javaClass.getDeclaredField("packet")
        field.isAccessible = true
        field.get(event)
    } catch (_: Exception) {
        null
    }

    private fun currentCape(capeManager: CapeManager): Any? = try {
        val field = capeManager.javaClass.getDeclaredField("currentCape")
        field.isAccessible = true
        field.get(capeManager)
    } catch (_: Exception) {
        null
    }

    private fun Any.capeTexture(): ResourceLocation? = try {
        val field = this.javaClass.getDeclaredField("cape")
        field.isAccessible = true
        field.get(this) as? ResourceLocation
    } catch (_: Exception) {
        null
    }
}

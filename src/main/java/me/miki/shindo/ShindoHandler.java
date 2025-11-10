package me.miki.shindo;

import me.miki.shindo.gui.modmenu.GuiModMenu;
import me.miki.shindo.management.cosmetic.cape.CapeManager;
import me.miki.shindo.management.cosmetic.cape.impl.Cape;
import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.*;
import me.miki.shindo.management.profile.Profile;
import me.miki.shindo.utils.OptifineUtils;
import me.miki.shindo.utils.TargetUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.server.S2EPacketCloseWindow;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.StringUtils;

public class ShindoHandler {

    private final Minecraft mc = Minecraft.getMinecraft();

    private final Shindo instance;

    private String prevOfflineName;
    private ResourceLocation offlineSkin;

    public ShindoHandler() {
        instance = Shindo.getInstance();
    }

    @EventTarget
    public void onTick(EventTick event) {
        OptifineUtils.disableFastRender();
        instance.getProfileManager().handleAutoSave();
    }

    @EventTarget
    public void onJoinServer(EventJoinServer event) {
        for (Profile p : instance.getProfileManager().getProfiles()) {
            if (!p.getServerIp().isEmpty() && StringUtils.containsIgnoreCase(event.getIp(), p.getServerIp())) {
                instance.getModManager().disableAll();
                instance.getProfileManager().load(p.getJsonFile());
                break;
            }
        }

        instance.getRestrictedMod().joinServer(event.getIp());
    }

    @EventTarget
    public void onLoadWorld(EventLoadWorld event) {
        instance.getRestrictedMod().joinWorld();
    }

    @EventTarget
    public void onUpdate(EventUpdate event) {
        TargetUtils.onUpdate();
    }

    @EventTarget
    public void onClickMouse(EventClickMouse event) {
        if (mc.gameSettings.keyBindTogglePerspective.isPressed()) {
            mc.gameSettings.thirdPersonView = (mc.gameSettings.thirdPersonView + 1) % 3;
            mc.renderGlobal.setDisplayListEntitiesDirty();
        }
    }

    @EventTarget
    public void onReceivePacket(EventReceivePacket event) {
        if (event.getPacket() instanceof S2EPacketCloseWindow && mc.currentScreen instanceof GuiModMenu) {
            event.setCancelled(true);
        }
    }

    @EventTarget
    public void onCape(EventLocationCape event) {

        CapeManager capeManager = instance.getCapeManager();

        if (event.getPlayerInfo() != null && event.getPlayerInfo().getGameProfile().getId().equals(mc.thePlayer.getGameProfile().getId())) {

            Cape currentCape = capeManager.getCurrentCape();

            if (!currentCape.equals(capeManager.getCapeByName("None"))) {
                event.setCancelled(true);
                event.setCape(currentCape.getCape());
            }
        }
    }
}

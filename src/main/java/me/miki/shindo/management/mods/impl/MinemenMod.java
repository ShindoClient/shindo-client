package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventReceivePacket;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import me.miki.shindo.management.mods.settings.impl.BooleanSetting;
import net.minecraft.network.play.server.S02PacketChat;

public class MinemenMod extends Mod {

    private final BooleanSetting autoPlaySetting = new BooleanSetting(TranslateText.AUTO_PLAY, this, false);

    public MinemenMod() {
        super(TranslateText.MINEMEN, TranslateText.MINEMEN_DESCRIPTION, ModCategory.OTHER);
    }

    @EventTarget
    public void onReceivePacket(EventReceivePacket event) {

        if (autoPlaySetting.isToggled() && event.getPacket() instanceof S02PacketChat) {

            S02PacketChat chatPacket = (S02PacketChat) event.getPacket();
            String raw = chatPacket.getChatComponent().toString();

            if (raw.contains("clickEvent=ClickEvent{action=RUN_COMMAND, value='/requeue")) {
                mc.thePlayer.sendChatMessage("/requeue");
            }
        }
    }
}

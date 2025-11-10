package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventReceivePacket;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import net.minecraft.network.play.server.S02PacketChat;

import me.miki.shindo.management.settings.config.Property;
import me.miki.shindo.management.settings.config.PropertyType;
public class MinemenMod extends Mod {

    @Property(type = PropertyType.BOOLEAN, translate = TranslateText.AUTO_PLAY)
    private boolean autoPlaySetting = false;

    public MinemenMod() {
        super(TranslateText.MINEMEN, TranslateText.MINEMEN_DESCRIPTION, ModCategory.OTHER);
    }

    @EventTarget
    public void onReceivePacket(EventReceivePacket event) {

        if (autoPlaySetting && event.getPacket() instanceof S02PacketChat) {

            S02PacketChat chatPacket = (S02PacketChat) event.getPacket();
            String raw = chatPacket.getChatComponent().toString();

            if (raw.contains("clickEvent=ClickEvent{action=RUN_COMMAND, value='/requeue")) {
                mc.thePlayer.sendChatMessage("/requeue");
            }
        }
    }
}

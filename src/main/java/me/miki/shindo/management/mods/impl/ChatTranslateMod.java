package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.nanovg.font.LegacyIcon;
import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventReceivePacket;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import me.miki.shindo.management.settings.config.Property;
import me.miki.shindo.management.settings.config.PropertyEnum;
import me.miki.shindo.management.settings.config.PropertyType;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ChatTranslateMod extends Mod {

    private static ChatTranslateMod instance;

    @Property(type = PropertyType.COMBO, translate = TranslateText.LANGUAGE)
    private Language languageSetting = Language.JAPANESE;

    public ChatTranslateMod() {
        super(TranslateText.CHAT_TRANSLATE, TranslateText.CHAT_TRANSLATE_DESCRIPTION, ModCategory.OTHER, LegacyIcon.MOD_CHAT_TRANSLATE);

        instance = this;
    }

    @EventTarget
    public void onReceivePacket(EventReceivePacket event) {

        if (event.getPacket() instanceof S02PacketChat) {

            S02PacketChat chatPacket = (S02PacketChat) event.getPacket();
            IChatComponent translate = new ChatComponentText(" [" + '\u270E' + "]").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GREEN).setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, ".scmd translate " + chatPacket.getChatComponent().getUnformattedText()))
                    .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(TranslateText.CLICK_TO_TRANSLATE.getText()))));
            final String chatMessage = chatPacket.getChatComponent().getUnformattedText();

            if (chatMessage.replaceAll(" ", "").isEmpty() || chatPacket.getType() == 2) {
                return;
            }

            event.setCancelled(true);

            mc.ingameGUI.getChatGUI().printChatMessage(chatPacket.getChatComponent().appendSibling(translate));
        }
    }

    public static ChatTranslateMod getInstance() {
        return instance;
    }

    public Language getLanguage() {
        return languageSetting;
    }

    public enum Language implements PropertyEnum {
        JAPANESE(TranslateText.JAPANESE),
        ENGLISH(TranslateText.ENGLISH),
        CHINESE(TranslateText.CHINESE),
        POLISH(TranslateText.POLISH);

        private final TranslateText translate;

        Language(TranslateText translate) {
            this.translate = translate;
        }

        @NotNull
        @Override
        public TranslateText getTranslate() {
            return translate;
        }

    }
}





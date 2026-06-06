package com.shindoclient.shindo.management.mods.impl

import com.shindoclient.shindo.management.event.EventTarget
import com.shindoclient.shindo.management.event.impl.EventReceivePacket
import com.shindoclient.shindo.management.language.TranslateText
import com.shindoclient.shindo.management.mods.Mod
import com.shindoclient.shindo.management.mods.ModCategory
import com.shindoclient.shindo.management.nanovg.font.Shinconic
import com.shindoclient.shindo.management.settings.config.Property
import com.shindoclient.shindo.management.settings.config.PropertyEnum
import com.shindoclient.shindo.management.settings.config.PropertyType
import net.minecraft.event.ClickEvent
import net.minecraft.event.HoverEvent
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.util.ChatComponentText
import net.minecraft.util.ChatStyle
import net.minecraft.util.EnumChatFormatting

class ChatTranslateMod :
    Mod(
        TranslateText.CHAT_TRANSLATE,
        TranslateText.CHAT_TRANSLATE_DESCRIPTION,
        ModCategory.OTHER,
        Shinconic.MOD_CHAT_TRANSLATE,
    ) {
    @Property(type = PropertyType.COMBO, translate = TranslateText.LANGUAGE)
    val language: Language = Language.JAPANESE

    init {
        instance = this
    }

    @EventTarget
    fun onReceivePacket(event: EventReceivePacket) {
        if (event.getPacket() is S02PacketChat) {
            val chatPacket = event.getPacket() as S02PacketChat
            val translate =
                ChatComponentText(" [" + '\u270E' + "]").setChatStyle(
                    ChatStyle()
                        .setColor(EnumChatFormatting.GREEN)
                        .setChatClickEvent(
                            ClickEvent(
                                ClickEvent.Action.RUN_COMMAND,
                                ".scmd translate " + chatPacket.chatComponent.unformattedText,
                            ),
                        ).setChatHoverEvent(
                            HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                ChatComponentText(TranslateText.CLICK_TO_TRANSLATE.getText()),
                            ),
                        ),
                )
            val chatMessage = chatPacket.chatComponent.unformattedText

            if (chatMessage.replace(" ".toRegex(), "").isEmpty() || chatPacket.type.toInt() == 2) {
                return
            }

            event.setCancelled(true)

            mc.ingameGUI.chatGUI.printChatMessage(chatPacket.chatComponent.appendSibling(translate))
        }
    }

    enum class Language(
        private val translate: TranslateText,
    ) : PropertyEnum {
        JAPANESE(TranslateText.JAPANESE),
        ENGLISH(TranslateText.ENGLISH),
        CHINESE(TranslateText.CHINESE),
        POLISH(TranslateText.POLISH),
        ;

        override fun getTranslate(): TranslateText = translate
    }

    companion object {
        @JvmField
        var instance: ChatTranslateMod? = null
    }
}

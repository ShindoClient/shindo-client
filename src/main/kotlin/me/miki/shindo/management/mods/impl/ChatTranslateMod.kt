package me.miki.shindo.management.mods.impl

import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventReceivePacket
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyEnum
import me.miki.shindo.management.settings.config.PropertyType
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
        LegacyIcon.MOD_CHAT_TRANSLATE,
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

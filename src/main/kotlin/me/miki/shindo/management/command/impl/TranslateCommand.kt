package me.miki.shindo.management.command.impl

import me.miki.shindo.logger.ShindoLogger
import me.miki.shindo.management.command.Command
import me.miki.shindo.management.mods.impl.ChatTranslateMod
import me.miki.shindo.utils.concurrent.TaskExecutor
import me.miki.shindo.utils.concurrent.ThreadPoolType
import me.miki.shindo.utils.translate.Translator
import net.minecraft.util.ChatComponentText
import net.minecraft.util.EnumChatFormatting

class TranslateCommand : Command("translate") {

    private var to: String = Translator.JAPANESE

    override fun onCommand(message: String) {
        val language = ChatTranslateMod.instance?.language ?: return
        to = when (language) {
            ChatTranslateMod.Language.JAPANESE -> Translator.JAPANESE
            ChatTranslateMod.Language.ENGLISH -> Translator.ENGLISH
            ChatTranslateMod.Language.CHINESE -> Translator.CHINESE_SIMPLIFIED
            ChatTranslateMod.Language.POLISH -> Translator.POLISH
        }

        TaskExecutor.runAsync(ThreadPoolType.NETWORK) {
            try {
                mc.ingameGUI.chatGUI.printChatMessage(
                    ChatComponentText(
                        EnumChatFormatting.GREEN.toString() + "[Translate] " + EnumChatFormatting.WHITE + Translator.translate(
                            message,
                            Translator.AUTO_DETECT,
                            to
                        )
                    )
                )
            } catch (e: Exception) {
                ShindoLogger.error("Failed translate", e)
            }
        }
    }
}

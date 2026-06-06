package com.shindoclient.shindo.management.command.impl

import com.shindoclient.shindo.logger.ShindoLogger
import com.shindoclient.shindo.management.command.Command
import com.shindoclient.shindo.management.mods.impl.ChatTranslateMod
import com.shindoclient.shindo.utils.concurrent.TaskExecutor
import com.shindoclient.shindo.utils.concurrent.ThreadPoolType
import com.shindoclient.shindo.utils.translate.Translator
import net.minecraft.util.ChatComponentText
import net.minecraft.util.EnumChatFormatting

class TranslateCommand : Command("translate") {
    private var to: String = Translator.JAPANESE

    override fun onCommand(message: String) {
        val language = ChatTranslateMod.instance?.language ?: return
        to =
            when (language) {
                ChatTranslateMod.Language.JAPANESE -> Translator.JAPANESE
                ChatTranslateMod.Language.ENGLISH -> Translator.ENGLISH
                ChatTranslateMod.Language.CHINESE -> Translator.CHINESE_SIMPLIFIED
                ChatTranslateMod.Language.POLISH -> Translator.POLISH
            }

        TaskExecutor.runAsync(ThreadPoolType.NETWORK) {
            try {
                mc.ingameGUI.chatGUI.printChatMessage(
                    ChatComponentText(
                        EnumChatFormatting.GREEN.toString() + "[Translate] " + EnumChatFormatting.WHITE +
                            Translator.translate(
                                message,
                                Translator.AUTO_DETECT,
                                to,
                            ),
                    ),
                )
            } catch (e: Exception) {
                ShindoLogger.error("Failed translate", e)
            }
        }
    }
}

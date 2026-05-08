package me.miki.shindo.management.command.impl

import me.miki.shindo.Shindo
import me.miki.shindo.management.command.Command
import me.miki.shindo.utils.transferable.FileTransferable
import net.minecraft.util.ChatComponentText
import java.awt.Desktop
import java.awt.Toolkit
import java.io.File
import java.io.IOException

class ScreenshotCommand : Command("screenshot") {

    override fun onCommand(message: String) {
        val fileManager = Shindo.getInstance().getFileManager()
        val args = message.split(" ")
        if (args.size < 2) return
        val file = File(fileManager.screenshotDir, args[1])

        when (args[0]) {
            "open" -> {
                try {
                    Desktop.getDesktop().open(file)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            "copy" -> {
                val selection = FileTransferable(file)
                Toolkit.getDefaultToolkit().systemClipboard.setContents(selection, null)
            }

            "del" -> {
                file.delete()
                mc.ingameGUI.chatGUI.printChatMessage(ChatComponentText("${args[1]} has been deleted"))
            }
        }
    }
}

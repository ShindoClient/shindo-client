package me.miki.shindo.management.mods.impl.asyncscreenshot

import me.miki.shindo.Shindo.Companion.getInstance
import me.miki.shindo.management.mods.impl.AsyncScreenshotMod
import net.minecraft.client.Minecraft
import net.minecraft.event.ClickEvent
import net.minecraft.util.ChatComponentText
import net.minecraft.util.ChatStyle
import net.minecraft.util.EnumChatFormatting
import java.awt.image.BufferedImage
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.imageio.ImageIO

class AsyncScreenshots(private val width: Int, private val height: Int, private val pixelValues: IntArray) : Thread() {
    private val mc: Minecraft = Minecraft.getMinecraft()

    override fun run() {
        processPixelValues(pixelValues, width, height)
        screenshot = timestampedPNGFileForDirectory

        try {
            image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
            image!!.setRGB(0, 0, width, height, pixelValues, 0, width)
            ImageIO.write(image, "png", screenshot)

            val asyncMod = AsyncScreenshotMod.instance
            if (asyncMod != null && asyncMod.isMessageEnabled) {
                mc.ingameGUI.chatGUI.printChatMessage(
                    ChatComponentText(EnumChatFormatting.UNDERLINE.toString() + "Saved screenshot" + EnumChatFormatting.RESET + " ")
                        .appendSibling(
                            ChatComponentText("[Open] ").setChatStyle(
                                ChatStyle().setColor(EnumChatFormatting.GOLD).setChatClickEvent(
                                    ClickEvent(
                                        ClickEvent.Action.RUN_COMMAND,
                                        ".scmd screenshot open " + screenshot!!.getName()
                                    )
                                )
                            )
                                .appendSibling(
                                    ChatComponentText("[Copy] ").setChatStyle(
                                        ChatStyle().setColor(EnumChatFormatting.BLUE).setChatClickEvent(
                                            ClickEvent(
                                                ClickEvent.Action.RUN_COMMAND,
                                                ".scmd screenshot copy " + screenshot!!.getName()
                                            )
                                        )
                                    )
                                        .appendSibling(
                                            ChatComponentText("[Delete]").setChatStyle(
                                                ChatStyle().setColor(EnumChatFormatting.RED).setChatClickEvent(
                                                    ClickEvent(
                                                        ClickEvent.Action.RUN_COMMAND,
                                                        ".scmd screenshot del " + screenshot!!.getName()
                                                    )
                                                )
                                            )
                                        )
                                )
                        )
                )
            }

            if (asyncMod != null && asyncMod.isClipboardEnabled) {
                mc.thePlayer.sendChatMessage(".scmd screenshot copy " + screenshot!!.getName())
            }
        } catch (e: Exception) {
        }
    }

    private fun processPixelValues(pixels: IntArray, displayWidth: Int, displayHeight: Int) {
        val xValues = IntArray(displayWidth)
        val yValues = displayHeight shr 1
        var `val` = 0
        while (`val` < yValues) {
            System.arraycopy(pixels, `val` * displayWidth, xValues, 0, displayWidth)
            System.arraycopy(
                pixels,
                (displayHeight - 1 - `val`) * displayWidth,
                pixels,
                `val` * displayWidth,
                displayWidth
            )
            System.arraycopy(xValues, 0, pixels, (displayHeight - 1 - `val`) * displayWidth, displayWidth)
            ++`val`
        }
    }

    companion object {
        private var image: BufferedImage? = null
        private var screenshot: File? = null

        @JvmStatic
        val timestampedPNGFileForDirectory: File
            get() {
                val dateFormatting =
                    SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(Date())
                var screenshotCount = 1
                var screenshot: File

                while (true) {
                    screenshot = File(
                        getInstance().fileManager.screenshotDir,
                        dateFormatting + (if (screenshotCount == 1) "" else ("_" + screenshotCount)) + ".png"
                    )
                    if (!screenshot.exists()) {
                        break
                    }

                    ++screenshotCount
                }

                return screenshot
            }
    }
}

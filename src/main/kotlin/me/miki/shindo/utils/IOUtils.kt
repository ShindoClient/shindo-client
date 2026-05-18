package me.miki.shindo.utils

import me.miki.shindo.logger.ShindoLogger
import me.miki.shindo.utils.transferable.ImageTransferable
import net.minecraft.client.Minecraft
import net.minecraft.util.ResourceLocation
import java.awt.Image
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import java.io.File
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.file.Files

object IOUtils {
    private val mc: Minecraft = Minecraft.getMinecraft()

    @JvmStatic
    fun copyStringToClipboard(s: String) {
        val stringSelection = StringSelection(s)
        toolkit.systemClipboard.setContents(stringSelection, null)
    }

    @JvmStatic
    fun getStringFromClipboard(): String? =
        try {
            toolkit.systemClipboard
                .getContents(null)
                .getTransferData(DataFlavor.stringFlavor)
                .toString()
        } catch (_: Exception) {
            null
        }

    @JvmStatic
    fun copyImageToClipboard(image: Image) {
        val imageSelection = ImageTransferable(image)
        toolkit.systemClipboard.setContents(imageSelection, null)
    }

    @JvmStatic
    fun getImageFromClipboard(): Image? =
        try {
            toolkit.systemClipboard.getContents(null).getTransferData(DataFlavor.imageFlavor) as Image
        } catch (_: Exception) {
            null
        }

    private val toolkit: Toolkit
        get() = Toolkit.getDefaultToolkit()

    @JvmStatic
    fun resourceToByteBuffer(location: ResourceLocation): ByteBuffer? =
        try {
            val bytes =
                org.apache.commons.io.IOUtils
                    .toByteArray(mc.resourceManager.getResource(location).inputStream)
            val data = ByteBuffer.allocateDirect(bytes.size).order(ByteOrder.nativeOrder()).put(bytes)
            data.flip() as Buffer
            data
        } catch (e: Exception) {
            ShindoLogger.error("Failed to load resource", e)
            null
        }

    @JvmStatic
    fun resourceToByteBuffer(file: File): ByteBuffer? =
        try {
            val bytes =
                org.apache.commons.io.IOUtils
                    .toByteArray(Files.newInputStream(file.toPath()))
            val data = ByteBuffer.allocateDirect(bytes.size).order(ByteOrder.nativeOrder()).put(bytes)
            data.flip() as Buffer
            data
        } catch (e: Exception) {
            ShindoLogger.error("Failed to load resource", e)
            null
        }
}

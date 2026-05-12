package me.miki.shindo.utils

import org.lwjgl.BufferUtils
import org.lwjgl.opengl.Display
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.InputStream
import java.nio.ByteBuffer
import javax.imageio.ImageIO

object IconUtil {

    private val SIZES = intArrayOf(
        16, 32, 48, 64, 128, 256
    )

    @JvmStatic
    fun setDisplayIcon(path: String) {
        try {
            val icons = loadIcons(path)
            Display.setIcon(icons)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun loadIcons(path: String): Array<ByteBuffer> {

        val stream = getStream(path)
            ?: throw IllegalStateException("Icon not found: $path")

        val original = ImageIO.read(stream)

        return Array(SIZES.size) { i ->
            val resized = resize(original, SIZES[i])
            toBuffer(resized)
        }
    }

    private fun getStream(path: String): InputStream? {
        return IconUtil::class.java.getResourceAsStream(path)
            ?: ClassLoader.getSystemResourceAsStream(path)
    }

    private fun resize(src: BufferedImage, size: Int): BufferedImage {

        val img = BufferedImage(
            size,
            size,
            BufferedImage.TYPE_INT_ARGB
        )

        val g = img.createGraphics()

        g.setRenderingHint(
            RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_BICUBIC
        )

        g.setRenderingHint(
            RenderingHints.KEY_RENDERING,
            RenderingHints.VALUE_RENDER_QUALITY
        )

        g.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON
        )

        g.drawImage(src, 0, 0, size, size, null)
        g.dispose()

        return img
    }

    private fun toBuffer(image: BufferedImage): ByteBuffer {

        val pixels = IntArray(image.width * image.height)

        image.getRGB(
            0, 0,
            image.width,
            image.height,
            pixels,
            0,
            image.width
        )

        val buffer = BufferUtils.createByteBuffer(
            image.width * image.height * 4
        )

        for (y in 0 until image.height) {
            for (x in 0 until image.width) {

                val pixel = pixels[y * image.width + x]

                buffer.put(((pixel shr 16) and 0xFF).toByte()) // R
                buffer.put(((pixel shr 8) and 0xFF).toByte())  // G
                buffer.put((pixel and 0xFF).toByte())          // B
                buffer.put(((pixel shr 24) and 0xFF).toByte()) // A
            }
        }

        buffer.flip()

        return buffer
    }
}
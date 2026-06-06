package com.shindoclient.shindo.utils

import org.lwjgl.BufferUtils
import org.lwjgl.opengl.Display
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.InputStream
import java.nio.ByteBuffer
import javax.imageio.ImageIO

object IconUtils {
    private val SIZES = intArrayOf(16, 32, 48, 64, 128, 256)

    @JvmStatic
    fun setDisplayIcon(path: String) {
        try {
            Display.setIcon(loadIcons(path))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun loadIcons(path: String): Array<ByteBuffer> {
        val stream =
            getStream(path)
                ?: throw IllegalStateException("Icon resource not found: $path")

        val original =
            ImageIO.read(stream)
                ?: throw IllegalStateException("Failed to decode image at: $path")

        // Normalize to square before generating sizes — avoids distortion if source isn't 1:1
        val square = toSquare(original)

        return Array(SIZES.size) { i -> toBuffer(resize(square, SIZES[i])) }
    }

    private fun getStream(path: String): InputStream? =
        IconUtils::class.java.getResourceAsStream(path)
            ?: IconUtils::class.java.classLoader?.getResourceAsStream(path)
            ?: ClassLoader.getSystemResourceAsStream(path)

    /**
     * Crops the image to a centered square if it isn't already square.
     * Prevents aspect ratio distortion when resizing to icon sizes.
     */
    private fun toSquare(src: BufferedImage): BufferedImage {
        if (src.width == src.height) return src
        val size = minOf(src.width, src.height)
        val x = (src.width - size) / 2
        val y = (src.height - size) / 2
        return src.getSubimage(x, y, size, size)
    }

    /**
     * Multi-step downscaling for high quality results at small icon sizes.
     *
     * A single-step bicubic from 512px → 16px loses detail and produces pixelation.
     * Progressively halving the image until near the target, then doing a final
     * bicubic pass, preserves sharpness significantly better.
     */
    private fun resize(
        src: BufferedImage,
        targetSize: Int,
    ): BufferedImage {
        var current = src

        // Progressive halving while still more than 2x the target size
        while (current.width / 2 > targetSize) {
            current = resizeStep(current, current.width / 2)
        }

        // Final pass to exact target size
        return resizeStep(current, targetSize)
    }

    private fun resizeStep(
        src: BufferedImage,
        size: Int,
    ): BufferedImage {
        // TYPE_INT_ARGB_PRE: premultiplied alpha prevents color fringing on semi-transparent edges
        val out = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB_PRE)
        val g = out.createGraphics()

        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY)
        g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY)
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE)

        g.drawImage(src, 0, 0, size, size, null)
        g.dispose()

        return out
    }

    /**
     * Converts a BufferedImage to an RGBA ByteBuffer for LWJGL / Display.setIcon().
     *
     * LWJGL expects RGBA byte order (R, G, B, A), not the ARGB int packing Java uses.
     * Premultiplied alpha from TYPE_INT_ARGB_PRE is reversed here back to straight alpha
     * by using getRGB(), which always returns straight ARGB regardless of internal storage.
     */
    private fun toBuffer(image: BufferedImage): ByteBuffer {
        val w = image.width
        val h = image.height
        val pixels = IntArray(w * h)
        image.getRGB(0, 0, w, h, pixels, 0, w)

        val buffer = BufferUtils.createByteBuffer(w * h * 4)

        for (pixel in pixels) {
            buffer.put(((pixel shr 16) and 0xFF).toByte()) // R
            buffer.put(((pixel shr 8) and 0xFF).toByte()) // G
            buffer.put((pixel and 0xFF).toByte()) // B
            buffer.put(((pixel shr 24) and 0xFF).toByte()) // A
        }

        buffer.flip()
        return buffer
    }
}

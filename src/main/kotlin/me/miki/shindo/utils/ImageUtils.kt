package me.miki.shindo.utils

import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Image
import java.awt.image.BufferedImage

object ImageUtils {
    @JvmStatic
    fun combine(
        img1: BufferedImage,
        img2: BufferedImage,
    ): BufferedImage {
        val width = maxOf(img1.width, img2.width)
        val height = maxOf(img1.height, img2.height)
        val combinedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)

        val graphics: Graphics = combinedImage.createGraphics()
        graphics.drawImage(img1, 0, 0, null)
        graphics.drawImage(img2, 0, 0, null)
        graphics.dispose()

        return combinedImage
    }

    @JvmStatic
    fun resize(
        img: BufferedImage,
        newW: Int,
        newH: Int,
    ): BufferedImage {
        val tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH)
        val image = BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB)

        val g2d: Graphics2D = image.createGraphics()
        g2d.drawImage(tmp, 0, 0, null)
        g2d.dispose()

        return image
    }

    @JvmStatic
    fun scissor(
        img: BufferedImage,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
    ): BufferedImage {
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val g2d: Graphics2D = image.createGraphics()
        g2d.drawImage(img, 0, 0, width, height, x, y, x + width, y + height, null)
        g2d.dispose()
        return image
    }

    @JvmStatic
    fun flipHorizontal(img: BufferedImage): BufferedImage {
        val width = img.width
        val height = img.height
        val flippedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val srcPixel = img.getRGB(x, y)
                val destX = width - x - 1
                flippedImage.setRGB(destX, y, srcPixel)
            }
        }

        return flippedImage
    }

    @JvmStatic
    fun flipVertical(img: BufferedImage): BufferedImage {
        val width = img.width
        val height = img.height
        val flippedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val srcPixel = img.getRGB(x, y)
                val destY = height - y - 1
                flippedImage.setRGB(x, destY, srcPixel)
            }
        }

        return flippedImage
    }

    @JvmStatic
    fun cropCenterHorizontal(
        src: BufferedImage,
        size: Int,
    ): BufferedImage {
        val width = src.width
        val height = src.height

        val cropWidth = minOf(width, height)
        val x = (width - cropWidth) / 2

        val cropped = src.getSubimage(x, 0, cropWidth, height)
        return resize(cropped, size, size)
    }
}

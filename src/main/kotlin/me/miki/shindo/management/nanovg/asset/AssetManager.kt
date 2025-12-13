package me.miki.shindo.management.nanovg.asset

import me.miki.shindo.logger.ShindoLogger
import me.miki.shindo.utils.IOUtils
import net.minecraft.client.Minecraft
import net.minecraft.util.ResourceLocation
import org.lwjgl.nanovg.NSVGImage
import org.lwjgl.nanovg.NanoSVG
import org.lwjgl.nanovg.NanoVG
import org.lwjgl.nanovg.NanoVGGL2
import org.lwjgl.stb.STBImage
import org.lwjgl.system.MemoryUtil
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.util.HashMap

class AssetManager {

    private val mc: Minecraft = Minecraft.getMinecraft()

    private val imageCache: HashMap<String, NVGAsset> = HashMap()
    private val glTextureCache: HashMap<Int, Int> = HashMap()
    private val svgCache: HashMap<String, NVGAsset> = HashMap()

    fun loadImage(nvg: Long, texture: Int, width: Float, height: Float): Boolean {
        if (!glTextureCache.containsKey(texture)) {
            glTextureCache[texture] = NanoVGGL2.nvglCreateImageFromHandle(
                nvg,
                texture,
                width.toInt(),
                -height.toInt(),
                0
            )
        }
        return true
    }

    fun loadImage(nvg: Long, location: ResourceLocation): Boolean {
        val key = location.resourcePath
        if (!imageCache.containsKey(key)) {
            val width = intArrayOf(0)
            val height = intArrayOf(0)
            val channels = intArrayOf(0)

            val image: ByteBuffer = IOUtils.resourceToByteBuffer(location) ?: return false
            val buffer = STBImage.stbi_load_from_memory(image, width, height, channels, 4) ?: return false

            imageCache[key] = NVGAsset(
                NanoVG.nvgCreateImageRGBA(
                    nvg,
                    width[0],
                    height[0],
                    NanoVG.NVG_IMAGE_REPEATX or NanoVG.NVG_IMAGE_REPEATY or NanoVG.NVG_IMAGE_GENERATE_MIPMAPS,
                    buffer
                ),
                width[0],
                height[0]
            )
        }
        return true
    }

    fun loadImage(nvg: Long, file: File): Boolean {
        val key = file.name
        if (!imageCache.containsKey(key)) {
            val width = intArrayOf(0)
            val height = intArrayOf(0)
            val channels = intArrayOf(0)

            val image: ByteBuffer = IOUtils.resourceToByteBuffer(file) ?: return false
            val buffer = STBImage.stbi_load_from_memory(image, width, height, channels, 4) ?: return false

            imageCache[key] = NVGAsset(
                NanoVG.nvgCreateImageRGBA(
                    nvg,
                    width[0],
                    height[0],
                    NanoVG.NVG_IMAGE_REPEATX or NanoVG.NVG_IMAGE_REPEATY or NanoVG.NVG_IMAGE_GENERATE_MIPMAPS,
                    buffer
                ),
                width[0],
                height[0]
            )
        }
        return true
    }

    fun loadSvg(nvg: Long, location: ResourceLocation, width: Float, height: Float): Boolean {
        val name = "${location.resourcePath}-$width-$height"
        if (!svgCache.containsKey(name)) {
            try {
                val resource = mc.resourceManager.getResource(location)
                val inputStream = resource?.inputStream ?: return false

                val result = StringBuilder()
                BufferedReader(InputStreamReader(inputStream)).use { br ->
                    var line: String?
                    while (br.readLine().also { line = it } != null) {
                        result.append(line)
                    }
                }

                val svg: NSVGImage = NanoSVG.nsvgParse(result.toString(), "px", 96f) ?: return false
                val rasterizer = NanoSVG.nsvgCreateRasterizer()

                var w = svg.width().toInt()
                var h = svg.height().toInt()
                val scale = Math.max(width / w, height / h)
                w = (w * scale).toInt()
                h = (h * scale).toInt()

                val image = MemoryUtil.memAlloc(w * h * 4)
                NanoSVG.nsvgRasterize(rasterizer, svg, 0f, 0f, scale, image, w, h, w * 4)

                NanoSVG.nsvgDeleteRasterizer(rasterizer)
                NanoSVG.nsvgDelete(svg)

                svgCache[name] = NVGAsset(
                    NanoVG.nvgCreateImageRGBA(
                        nvg,
                        w,
                        h,
                        NanoVG.NVG_IMAGE_REPEATX or NanoVG.NVG_IMAGE_REPEATY or NanoVG.NVG_IMAGE_GENERATE_MIPMAPS,
                        image
                    ),
                    w,
                    h
                )
            } catch (e: Exception) {
                ShindoLogger.error("Failed to load svg", e)
                return false
            }
        }
        return true
    }

    fun getImage(location: ResourceLocation): Int {
        return imageCache[location.resourcePath]?.image ?: 0
    }

    fun getImageAsset(location: ResourceLocation): NVGAsset? {
        return imageCache[location.resourcePath]
    }

    fun getImage(file: File): Int {
        return imageCache[file.name]?.image ?: 0
    }

    fun getImageAsset(file: File): NVGAsset? {
        return imageCache[file.name]
    }

    fun getImage(texture: Int): Int {
        return glTextureCache[texture] ?: 0
    }

    fun removeImage(nvg: Long, location: ResourceLocation) {
        imageCache[location.resourcePath]?.let {
            NanoVG.nvgDeleteImage(nvg, it.image)
            imageCache.remove(location.resourcePath)
        }
    }

    fun removeImage(nvg: Long, file: File) {
        imageCache[file.name]?.let {
            NanoVG.nvgDeleteImage(nvg, it.image)
            imageCache.remove(file.name)
        }
    }

    fun getSvg(location: ResourceLocation, width: Float, height: Float): Int {
        val name = "${location.resourcePath}-$width-$height"
        return svgCache[name]?.image ?: 0
    }

    fun removeSvg(nvg: Long, path: String, width: Float, height: Float) {
        val name = "$path-$width-$height"
        svgCache[name]?.let {
            NanoVG.nvgDeleteImage(nvg, it.image)
            svgCache.remove(name)
        }
    }
}

package me.miki.shindo.management.nanovg.asset

import me.miki.shindo.logger.ShindoLogger
import me.miki.shindo.utils.IOUtils
import net.minecraft.client.Minecraft
import net.minecraft.util.ResourceLocation
import org.lwjgl.nanovg.NanoSVG
import org.lwjgl.nanovg.NanoVG
import org.lwjgl.nanovg.NanoVGGL2
import org.lwjgl.stb.STBImage
import org.lwjgl.system.MemoryUtil
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

class AssetManager {
    private val mc: Minecraft = Minecraft.getMinecraft()

    private val imageCache: HashMap<String, NVGAsset> = HashMap()
    private val glTextureCache: HashMap<Int, Int> = HashMap()
    private val svgCache: HashMap<String, NVGAsset> = HashMap()

    @JvmOverloads
    fun loadImage(
        nvg: Long,
        texture: Int,
        width: Float,
        height: Float,
        flags: Int = 0,
    ): Boolean {
        if (!glTextureCache.containsKey(texture)) {
            glTextureCache[texture] =
                NanoVGGL2.nvglCreateImageFromHandle(
                    nvg,
                    texture,
                    width.toInt(),
                    (-height).toInt(),
                    flags,
                )
            return true
        }
        return true
    }

    fun loadImage(
        nvg: Long,
        location: ResourceLocation,
    ): Boolean {
        if (!imageCache.containsKey(location.resourcePath)) {
            val width = intArrayOf(0)
            val height = intArrayOf(0)
            val channels = intArrayOf(0)
            val image = IOUtils.resourceToByteBuffer(location) ?: return false
            val buffer = STBImage.stbi_load_from_memory(image, width, height, channels, 4) ?: return false
            imageCache[location.resourcePath] =
                NVGAsset(
                    NanoVG.nvgCreateImageRGBA(
                        nvg,
                        width[0],
                        height[0],
                        NanoVG.NVG_IMAGE_REPEATX or NanoVG.NVG_IMAGE_REPEATY or NanoVG.NVG_IMAGE_GENERATE_MIPMAPS,
                        buffer,
                    ),
                    width[0],
                    height[0],
                )
            return true
        }
        return true
    }

    fun loadImage(
        nvg: Long,
        file: File,
    ): Boolean {
        if (!imageCache.containsKey(file.name)) {
            val width = intArrayOf(0)
            val height = intArrayOf(0)
            val channels = intArrayOf(0)
            val image = IOUtils.resourceToByteBuffer(file) ?: return false
            val buffer = STBImage.stbi_load_from_memory(image, width, height, channels, 4) ?: return false
            imageCache[file.name] =
                NVGAsset(
                    NanoVG.nvgCreateImageRGBA(
                        nvg,
                        width[0],
                        height[0],
                        NanoVG.NVG_IMAGE_REPEATX or NanoVG.NVG_IMAGE_REPEATY or NanoVG.NVG_IMAGE_GENERATE_MIPMAPS,
                        buffer,
                    ),
                    width[0],
                    height[0],
                )
            return true
        }
        return true
    }

    fun loadSvg(
        nvg: Long,
        location: ResourceLocation,
        width: Float,
        height: Float,
    ): Boolean {
        val name = location.resourcePath + "-" + width + "-" + height
        return if (!svgCache.containsKey(name)) {
            try {
                val inputStream = mc.resourceManager.getResource(location).inputStream ?: return false
                val resultStringBuilder = StringBuilder()
                BufferedReader(InputStreamReader(inputStream)).use { br ->
                    var line: String?
                    while (br.readLine().also { line = it } != null) {
                        resultStringBuilder.append(line)
                    }
                }
                val s: CharSequence = resultStringBuilder.toString()
                val svg = NanoSVG.nsvgParse(s, "px", 96f) ?: return false
                val rasterizer = NanoSVG.nsvgCreateRasterizer()
                var w = svg.width().toInt()
                var h = svg.height().toInt()
                val scale = (width / w).coerceAtLeast(height / h)
                w = (w * scale).toInt()
                h = (h * scale).toInt()
                val image = MemoryUtil.memAlloc(w * h * 4)
                NanoSVG.nsvgRasterize(rasterizer, svg, 0f, 0f, scale, image, w, h, w * 4)
                NanoSVG.nsvgDeleteRasterizer(rasterizer)
                NanoSVG.nsvgDelete(svg)
                svgCache[name] =
                    NVGAsset(
                        NanoVG.nvgCreateImageRGBA(
                            nvg,
                            w,
                            h,
                            NanoVG.NVG_IMAGE_REPEATX or NanoVG.NVG_IMAGE_REPEATY or NanoVG.NVG_IMAGE_GENERATE_MIPMAPS,
                            image,
                        ),
                        w,
                        h,
                    )
                true
            } catch (e: Exception) {
                ShindoLogger.error("Failed to load svg", e)
                false
            }
        } else {
            true
        }
    }

    fun getImage(location: ResourceLocation): Int = imageCache[location.resourcePath]!!.image

    fun getImageAsset(location: ResourceLocation): NVGAsset? = imageCache[location.resourcePath]

    fun getImage(file: File): Int = imageCache[file.name]!!.image

    fun getImageAsset(file: File): NVGAsset? = imageCache[file.name]

    fun getImage(texture: Int): Int = glTextureCache[texture]!!

    fun removeImage(
        nvg: Long,
        location: ResourceLocation,
    ) {
        NanoVG.nvgDeleteImage(nvg, imageCache[location.resourcePath]!!.image)
        imageCache.remove(location.resourcePath)
    }

    fun removeImage(
        nvg: Long,
        file: File,
    ) {
        NanoVG.nvgDeleteImage(nvg, imageCache[file.name]!!.image)
        imageCache.remove(file.name)
    }

    fun getSvg(
        location: ResourceLocation,
        width: Float,
        height: Float,
    ): Int {
        val name = location.resourcePath + "-" + width + "-" + height
        return svgCache[name]!!.image
    }

    fun removeSvg(
        nvg: Long,
        path: String,
        width: Float,
        height: Float,
    ) {
        val name = "$path-$width-$height"
        NanoVG.nvgDeleteImage(nvg, svgCache[name]!!.image)
        svgCache.remove(name)
    }
}

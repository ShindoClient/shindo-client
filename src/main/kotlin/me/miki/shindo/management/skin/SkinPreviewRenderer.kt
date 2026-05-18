package me.miki.shindo.management.skin

import me.miki.shindo.logger.ShindoLogger
import org.lwjgl.nanovg.NVGColor
import org.lwjgl.nanovg.NVGPaint
import org.lwjgl.nanovg.NanoVG
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import java.awt.Color
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class SkinPreviewRenderer {
    var baseWidth: Float = DEFAULT_BASE_WIDTH
        private set
    var baseHeight: Float = DEFAULT_BASE_HEIGHT
        private set

    private val previewCache = HashMap<String, Int>()

    fun renderRemoteSkinPreview(
        vg: Long,
        uuid: String?,
        x: Float,
        y: Float,
        scale: Float,
        background: Color?,
        border: Color?,
    ) {
        if (vg == 0L || scale <= 0f) return
        val normalizedUuid = normalizeUuid(uuid) ?: return
        val imageHandle =
            try {
                getOrCreatePreviewImage(vg, normalizedUuid)
            } catch (e: IOException) {
                ShindoLogger.error("SkinPreviewRenderer: Failed to fetch preview for $normalizedUuid (${e.message})")
                return
            }
        if (imageHandle <= 0) return
        val (imageWidth, imageHeight) =
            MemoryStack.stackPush().use { stack ->
                val w = stack.mallocInt(1)
                val h = stack.mallocInt(1)
                NanoVG.nvgImageSize(vg, imageHandle, w, h)
                Pair(w.get(0), h.get(0))
            }
        if (imageWidth <= 0 || imageHeight <= 0) return
        baseWidth = imageWidth.toFloat()
        baseHeight = imageHeight.toFloat()
        NanoVG.nvgSave(vg)
        NanoVG.nvgTranslate(vg, x, y)
        NanoVG.nvgScale(vg, scale, scale)
        if (background != null && background.alpha > 0) {
            drawBackground(
                vg,
                background,
                imageWidth.toFloat(),
                imageHeight.toFloat(),
            )
        }
        val paint = NVGPaint.calloc()
        try {
            NanoVG.nvgBeginPath(vg)
            NanoVG.nvgRect(vg, 0f, 0f, imageWidth.toFloat(), imageHeight.toFloat())
            NanoVG.nvgImagePattern(vg, 0f, 0f, imageWidth.toFloat(), imageHeight.toFloat(), 0f, imageHandle, 1f, paint)
            NanoVG.nvgFillPaint(vg, paint)
            NanoVG.nvgFill(vg)
        } finally {
            paint.free()
        }
        if (border != null && border.alpha > 0) drawBorder(vg, border, imageWidth.toFloat(), imageHeight.toFloat())
        NanoVG.nvgRestore(vg)
    }

    fun isPreviewCached(uuid: String?): Boolean {
        val normalized = normalizeUuid(uuid) ?: return false
        return previewCache.containsKey(normalized)
    }

    fun destroyCachedPreview(
        vg: Long,
        uuid: String?,
    ) {
        val normalized = normalizeUuid(uuid) ?: return
        if (vg == 0L) return
        val handle = previewCache.remove(normalized)
        if (handle != null && handle > 0) NanoVG.nvgDeleteImage(vg, handle)
    }

    fun clearCache(vg: Long) {
        if (vg == 0L && previewCache.isNotEmpty()) {
            previewCache.clear()
            baseWidth = DEFAULT_BASE_WIDTH
            baseHeight = DEFAULT_BASE_HEIGHT
            return
        }
        for (handle in previewCache.values) {
            if (handle > 0 && vg != 0L) NanoVG.nvgDeleteImage(vg, handle)
        }
        previewCache.clear()
        baseWidth = DEFAULT_BASE_WIDTH
        baseHeight = DEFAULT_BASE_HEIGHT
    }

    private fun getOrCreatePreviewImage(
        vg: Long,
        normalizedUuid: String,
    ): Int {
        previewCache[normalizedUuid]?.let { if (it > 0) return it }
        val payload = downloadPreviewBytes(normalizedUuid)
        val buffer = MemoryUtil.memAlloc(payload.size)
        try {
            buffer.put(payload)
            buffer.flip()
            val imageHandle = NanoVG.nvgCreateImageMem(vg, NanoVG.NVG_IMAGE_NEAREST, buffer)
            if (imageHandle <= 0) throw IOException("nvgCreateImageMem returned $imageHandle")
            previewCache[normalizedUuid] = imageHandle
            return imageHandle
        } finally {
            MemoryUtil.memFree(buffer)
        }
    }

    @Throws(IOException::class)
    private fun downloadPreviewBytes(normalizedUuid: String): ByteArray {
        val url = String.format(Locale.ROOT, PREVIEW_URL_TEMPLATE, normalizedUuid, REMOTE_PREVIEW_SCALE)
        var connection: HttpURLConnection? = null
        try {
            connection = URL(url).openConnection() as HttpURLConnection
            connection.connectTimeout = CONNECT_TIMEOUT_MS
            connection.readTimeout = READ_TIMEOUT_MS
            connection.setRequestProperty("User-Agent", "ShindoClient/RemoteSkinPreview")
            connection.useCaches = false
            if (connection.responseCode !=
                HttpURLConnection.HTTP_OK
            ) {
                throw IOException("HTTP ${connection.responseCode} while requesting $url")
            }
            connection.inputStream.use { inputStream ->
                ByteArrayOutputStream().use { outputStream ->
                    val buffer = ByteArray(8192)
                    var read: Int
                    while (inputStream.read(buffer).also { read = it } != -1) outputStream.write(buffer, 0, read)
                    return outputStream.toByteArray()
                }
            }
        } finally {
            connection?.disconnect()
        }
    }

    private fun drawBackground(
        vg: Long,
        color: Color,
        width: Float,
        height: Float,
    ) {
        val nvgColor = NVGColor.calloc()
        try {
            NanoVG.nvgRGBA(
                color.red.toByte(),
                color.green.toByte(),
                color.blue.toByte(),
                color.alpha.toByte(),
                nvgColor,
            )
            NanoVG.nvgBeginPath(vg)
            NanoVG.nvgRoundedRect(vg, 0f, 0f, width, height, 6f)
            NanoVG.nvgFillColor(vg, nvgColor)
            NanoVG.nvgFill(vg)
        } finally {
            nvgColor.free()
        }
    }

    private fun drawBorder(
        vg: Long,
        color: Color,
        width: Float,
        height: Float,
    ) {
        val nvgColor = NVGColor.calloc()
        try {
            NanoVG.nvgRGBA(
                color.red.toByte(),
                color.green.toByte(),
                color.blue.toByte(),
                color.alpha.toByte(),
                nvgColor,
            )
            NanoVG.nvgBeginPath(vg)
            NanoVG.nvgRoundedRect(vg, 0f, 0f, width, height, 6f)
            NanoVG.nvgStrokeColor(vg, nvgColor)
            NanoVG.nvgStrokeWidth(vg, 0.9f)
            NanoVG.nvgStroke(vg)
        } finally {
            nvgColor.free()
        }
    }

    companion object {
        private const val REMOTE_PREVIEW_SCALE = 8
        private const val CONNECT_TIMEOUT_MS = 6000
        private const val READ_TIMEOUT_MS = 7000
        private const val DEFAULT_BASE_WIDTH = 132f
        private const val DEFAULT_BASE_HEIGHT = 276f
        private const val PREVIEW_URL_TEMPLATE = "https://api.mineatar.io/body/full/%s?scale=%d&overlay=true"

        private fun normalizeUuid(uuid: String?): String? {
            if (uuid.isNullOrBlank()) return null
            val trimmed = uuid!!.trim()
            if (trimmed.isEmpty()) return null
            val cleaned = trimmed.replace("-", "")
            return if (cleaned.isEmpty()) null else cleaned.lowercase(Locale.ROOT)
        }
    }
}

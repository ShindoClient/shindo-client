package me.miki.shindo.management.nanovg

import me.miki.extensions.ui.nanovg.drawCenteredText
import me.miki.extensions.ui.nanovg.drawOutlineRoundedRect
import me.miki.extensions.ui.nanovg.drawRoundedRect
import me.miki.shindo.logger.ShindoLogger
import me.miki.shindo.management.color.AccentColor
import me.miki.shindo.management.color.palette.ColorPalette
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.nanovg.asset.AssetManager
import me.miki.shindo.management.nanovg.asset.NVGAsset
import me.miki.shindo.management.nanovg.font.Font
import me.miki.shindo.management.nanovg.font.FontManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.types.CircQueue
import me.miki.shindo.types.Rect
import me.miki.shindo.types.Size
import me.miki.shindo.utils.ColorUtils.applyAlpha
import me.miki.shindo.utils.MathUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.util.ResourceLocation
import org.lwjgl.nanovg.NVGColor
import org.lwjgl.nanovg.NVGPaint
import org.lwjgl.nanovg.NanoVG
import org.lwjgl.nanovg.NanoVGGL2
import org.lwjgl.opengl.GL11
import org.lwjgl.system.MemoryUtil
import java.awt.Color
import java.awt.Dimension
import java.io.File
import java.nio.FloatBuffer
import java.nio.IntBuffer
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

class NanoVGManager {
    private val mc: Minecraft = Minecraft.getMinecraft()

    private val f4Buff: FloatBuffer = MemoryUtil.memAllocFloat(4)
    private val i1buff1: IntBuffer = MemoryUtil.memAllocInt(1)
    private val i1buff2: IntBuffer = MemoryUtil.memAllocInt(1)
    private val f1Buff1: FloatBuffer = MemoryUtil.memAllocFloat(1)
    private val f1Buff2: FloatBuffer = MemoryUtil.memAllocFloat(1)

    private val colorQueue = CircQueue(NVGColor.calloc(), NVGColor.calloc(), NVGColor.calloc(), NVGColor.calloc())
    private val paintQueue = CircQueue(NVGPaint.calloc(), NVGPaint.calloc(), NVGPaint.calloc(), NVGPaint.calloc())

    private var nvg: Long = 0
    private val colorCache: HashMap<Int, NVGColor> = HashMap()

    private var fontManager: FontManager? = null
    private var assetManager: AssetManager? = null

    init {
        nvg = NanoVGGL2.nvgCreate(NanoVGGL2.NVG_ANTIALIAS or NanoVGGL2.NVG_STENCIL_STROKES)

        if (nvg == 0L) {
            ShindoLogger.error("Failed to create NanoVG context")
            mc.shutdown()
        }

        fontManager = FontManager()
        fontManager!!.init(nvg)

        assetManager = AssetManager()
    }

    fun destroy() {
        NanoVGGL2.nvgDelete(nvg)

        for (i in 0..3) {
            colorQueue.poll().free()
            paintQueue.poll().free()
        }

        MemoryUtil.memFree(f4Buff)
        MemoryUtil.memFree(i1buff1)
        MemoryUtil.memFree(i1buff2)
        MemoryUtil.memFree(f1Buff1)
        MemoryUtil.memFree(f1Buff2)
    }

    fun getColor(color: Color): NVGColor = getColor(color.rgb)

    fun getColor(
        r: Float,
        g: Float,
        b: Float,
        a: Float,
    ): NVGColor {
        val nvgColor = colorQueue.poll()
        nvgColor.r(r)
        nvgColor.g(g)
        nvgColor.b(b)
        nvgColor.a(a)
        return nvgColor
    }

    fun getColor(color: Int): NVGColor =
        getColor(
            (color shr 16 and 0xFF) / 255f,
            (color shr 8 and 0xFF) / 255f,
            (color and 0xFF) / 255f,
            (color shr 24 and 0xFF) / 255f,
        )

    private fun getAvailablePaint(): NVGPaint = paintQueue.poll()

    fun imageSize(
        imageId: Int,
        src: Size,
    ) {
        NanoVG.nvgImageSize(nvg, imageId, i1buff1, i1buff2)
        src[i1buff1[0].toFloat()] = i1buff2[0].toFloat()
    }

    fun setupAndDraw(
        task: Runnable,
        scale: Boolean = true,
    ) {
        val sr = ScaledResolution(mc)

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS)
        NanoVG.nvgBeginFrame(nvg, mc.displayWidth.toFloat(), mc.displayHeight.toFloat(), 1f)

        if (scale) {
            NanoVG.nvgScale(nvg, sr.scaleFactor.toFloat(), sr.scaleFactor.toFloat())
        }

        task.run()

        GL11.glDisable(GL11.GL_ALPHA_TEST)
        NanoVG.nvgEndFrame(nvg)
        GL11.glPopAttrib()
    }

    fun setupAndDraw(task: () -> Unit) {
        setupAndDraw(Runnable { task() })
    }

    fun setupAndDraw(task: Runnable?) {
        setupAndDraw(task!!, true)
    }

    fun drawAlphaBar(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        radius: Float,
        color: Color,
    ) {
        drawAlphaBar(x, y, width, height, radius, color.rgb)
    }

    fun drawAlphaBar(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        radius: Float,
        color: Int,
    ) {
        NanoVG.nvgBeginPath(nvg)
        NanoVG.nvgRoundedRect(nvg, x, y, width, height, radius)
        val nvgColor = getColor(color)
        val nvgColor2 = getColor(0)
        NanoVG.nvgFillPaint(
            nvg,
            NanoVG.nvgLinearGradient(nvg, x, y, x + width, y, nvgColor2, nvgColor, getAvailablePaint()),
        )
        NanoVG.nvgFill(nvg)
    }

    fun drawHSBBox(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        radius: Float,
        color: Color,
    ) {
        drawHSBBox(x, y, width, height, radius, color.rgb)
    }

    fun drawHSBBox(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        radius: Float,
        color: Int,
    ) {
        drawRoundedRect(x, y, width, height, radius, color)
        NanoVG.nvgBeginPath(nvg)
        NanoVG.nvgRoundedRect(nvg, x, y, width, height, radius)
        val nvgColor = getColor(-1)
        val nvgColor2 = getColor(0)
        NanoVG.nvgFillPaint(
            nvg,
            NanoVG.nvgLinearGradient(nvg, x + 8, y + 8, x + width, y, nvgColor, nvgColor2, getAvailablePaint()),
        )
        NanoVG.nvgFill(nvg)
        NanoVG.nvgBeginPath(nvg)
        NanoVG.nvgRoundedRect(nvg, x, y, width, height, radius)
        val nvgColor3 = getColor(0)
        val nvgColor4 = getColor(-0x1000000)
        NanoVG.nvgFillPaint(
            nvg,
            NanoVG.nvgLinearGradient(nvg, x + 8, y + 8, x, y + height, nvgColor3, nvgColor4, getAvailablePaint()),
        )
        NanoVG.nvgFill(nvg)
    }

    fun drawRect(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        color: Color,
    ) {
        drawRect(x, y, width, height, color.rgb)
    }

    fun drawRect(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        color: Int,
    ) {
        NanoVG.nvgBeginPath(nvg)
        NanoVG.nvgRect(nvg, x, y, width, height)
        val nvgColor = getColor(color)
        NanoVG.nvgFillColor(nvg, nvgColor)
        NanoVG.nvgFill(nvg)
    }

    fun drawRoundedRect(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        radius: Float,
        color: Color,
    ) {
        drawRoundedRect(x, y, width, height, radius, color.rgb)
    }

    fun drawRoundedRect(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        radius: Float,
        color: Int,
    ) {
        NanoVG.nvgBeginPath(nvg)
        NanoVG.nvgRoundedRect(nvg, x, y, width, height, radius)
        val nvgColor = getColor(color)
        NanoVG.nvgFillColor(nvg, nvgColor)
        NanoVG.nvgFill(nvg)
    }

    fun drawRoundedRect(
        rect: Rect,
        radius: Float,
        color: Int,
    ) {
        drawRoundedRect(rect.x, rect.y, rect.width, rect.height, radius, color)
    }

    fun drawRoundedRect(
        rect: Rect,
        radius: Float,
        color: Color,
    ) {
        drawRoundedRect(rect.x, rect.y, rect.width, rect.height, radius, color)
    }

    fun drawRoundedRectVarying(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        topLeftRadius: Float,
        topRightRadius: Float,
        bottomLeftRadius: Float,
        bottomRightRadius: Float,
        color: Color,
    ) {
        drawRoundedRectVarying(
            x,
            y,
            width,
            height,
            topLeftRadius,
            topRightRadius,
            bottomLeftRadius,
            bottomRightRadius,
            color.rgb,
        )
    }

    fun drawRoundedRectVarying(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        topLeftRadius: Float,
        topRightRadius: Float,
        bottomLeftRadius: Float,
        bottomRightRadius: Float,
        color: Int,
    ) {
        NanoVG.nvgBeginPath(nvg)
        NanoVG.nvgRoundedRectVarying(
            nvg,
            x,
            y,
            width,
            height,
            topLeftRadius,
            topRightRadius,
            bottomRightRadius,
            bottomLeftRadius,
        )
        val nvgColor = getColor(color)
        NanoVG.nvgFillColor(nvg, nvgColor)
        NanoVG.nvgFill(nvg)
    }

    fun drawVerticalGradientRect(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        color1: Color,
        color2: Color,
    ) {
        drawVerticalGradientRect(x, y, width, height, color1.rgb, color2.rgb)
    }

    fun drawVerticalGradientRect(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        color1: Int,
        color2: Int,
    ) {
        NanoVG.nvgBeginPath(nvg)
        NanoVG.nvgRect(nvg, x, y, width, height)
        val nvgColor1 = getColor(color1)
        val nvgColor2 = getColor(color2)
        NanoVG.nvgFillColor(nvg, nvgColor1)
        NanoVG.nvgFillColor(nvg, nvgColor2)
        NanoVG.nvgFillPaint(
            nvg,
            NanoVG.nvgLinearGradient(nvg, x, y, x, y + height, nvgColor1, nvgColor2, getAvailablePaint()),
        )
        NanoVG.nvgFill(nvg)
    }

    fun drawHorizontalGradientRect(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        color1: Color,
        color2: Color,
    ) {
        drawHorizontalGradientRect(x, y, width, height, color1.rgb, color2.rgb)
    }

    fun drawHorizontalGradientRect(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        color1: Int,
        color2: Int,
    ) {
        NanoVG.nvgBeginPath(nvg)
        NanoVG.nvgRect(nvg, x, y, width, height)
        val nvgColor1 = getColor(color1)
        val nvgColor2 = getColor(color2)
        NanoVG.nvgFillColor(nvg, nvgColor1)
        NanoVG.nvgFillColor(nvg, nvgColor2)
        NanoVG.nvgFillPaint(
            nvg,
            NanoVG.nvgLinearGradient(nvg, x, y, x + width, y, nvgColor1, nvgColor2, getAvailablePaint()),
        )
        NanoVG.nvgFill(nvg)
    }

    fun drawGradientRect(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        color1: Color,
        color2: Color,
    ) {
        drawGradientRect(x, y, width, height, color1.rgb, color2.rgb)
    }

    fun drawGradientRect(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        color1: Int,
        color2: Int,
    ) {
        val tick = System.currentTimeMillis() % 3600 / 570f
        val max = width.coerceAtLeast(height)
        NanoVG.nvgBeginPath(nvg)
        NanoVG.nvgRect(nvg, x, y, width, height)
        val nvgColor1 = getColor(color1)
        val nvgColor2 = getColor(color2)
        NanoVG.nvgFillColor(nvg, nvgColor1)
        NanoVG.nvgFillColor(nvg, nvgColor2)
        NanoVG.nvgFillPaint(
            nvg,
            NanoVG.nvgLinearGradient(
                nvg,
                x + width / 2 - max / 2 * MathUtils.cos(tick.toDouble()),
                y + height / 2 - max / 2 * MathUtils.sin(tick.toDouble()),
                x + width / 2 + max / 2 * MathUtils.cos(tick.toDouble()),
                y + height / 2 + (max + 2f) * MathUtils.sin(tick.toDouble()),
                nvgColor1,
                nvgColor2,
                getAvailablePaint(),
            ),
        )
        NanoVG.nvgFill(nvg)
    }

    fun drawGlassButton(
        text: String,
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        hover: Float,
        anim: Float,
        red: Boolean,
    ) {
        val scale = 1.0f + (hover * 0.03f)
        val dw = w * scale
        val dh = h * scale
        val dx = x - (dw / 2f)
        val dy = y + (h - dh) / 2f

        val bg: Color =
            if (red) {
                Color(180, 30, 30, (anim * (80 + hover * 100)).toInt())
            } else {
                Color(
                    15,
                    15,
                    20,
                    (anim * (140 + hover * 70)).toInt(),
                )
            }
        drawRoundedRect(dx, dy, dw, dh, 4.5f, bg)
        drawOutlineRoundedRect(dx, dy, dw, dh, 4.5f, 1.2f, Color(255, 255, 255, (anim * (35 + hover * 85)).toInt()))
        drawCenteredText(
            text,
            x,
            dy + dh / 2f - 4.5f,
            Color(255, 255, 255, (anim * (200 + hover * 55)).toInt()),
            9.5f,
            Fonts.REGULAR,
        )
    }

    fun drawGlassButtonWithIcon(
        icon: String,
        x: Float,
        y: Float,
        size: Float,
        hover: Float,
        lucide: Boolean,
        anim: Float,
    ) {
        drawRoundedRect(x, y, size, size, 5, Color(20, 20, 25, (anim * (180 + hover * 75)).toInt()))
        drawOutlineRoundedRect(x, y, size, size, 5, 1f, Color(255, 255, 255, (anim * (30 + hover * 50)).toInt()))
        val iconColor: Color = Color(255, 255, 255, (anim * (180 + hover * 75)).toInt())
        drawCenteredText(icon, x + size / 2f, y + size / 2f - 6, iconColor, 14, if (lucide) Fonts.LUCIDE else Fonts.SHINCONIC)
    }

    fun drawGradientRoundedRect(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        radius: Float,
        color1: Color,
        color2: Color,
    ) {
        drawGradientRoundedRect(x, y, width, height, radius, color1.rgb, color2.rgb)
    }

    fun drawGradientRoundedRect(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        radius: Float,
        color1: Int,
        color2: Int,
    ) {
        val tick = System.currentTimeMillis() % 3600 / 570f
        val max = width.coerceAtLeast(height)
        NanoVG.nvgBeginPath(nvg)
        NanoVG.nvgRoundedRect(nvg, x, y, width, height, radius)
        val nvgColor1 = getColor(color1)
        val nvgColor2 = getColor(color2)
        NanoVG.nvgFillColor(nvg, nvgColor1)
        NanoVG.nvgFillColor(nvg, nvgColor2)
        NanoVG.nvgFillPaint(
            nvg,
            NanoVG.nvgLinearGradient(
                nvg,
                x + width / 2 - max / 2 * MathUtils.cos(tick.toDouble()),
                y + height / 2 - max / 2 * MathUtils.sin(tick.toDouble()),
                x + width / 2 + max / 2 * MathUtils.cos(tick.toDouble()),
                y + height / 2 + (max + 2f) * MathUtils.sin(tick.toDouble()),
                nvgColor1,
                nvgColor2,
                getAvailablePaint(),
            ),
        )
        NanoVG.nvgFill(nvg)
    }

    fun drawOutlineRoundedRect(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        radius: Float,
        strokeWidth: Float,
        color: Color,
    ) {
        drawOutlineRoundedRect(x, y, width, height, radius, strokeWidth, color.rgb)
    }

    fun drawOutlineRoundedRect(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        radius: Float,
        strokeWidth: Float,
        color: Int,
    ) {
        var radius = radius
        if (radius < 0.5f) {
            radius = 0.5f
        }
        val nvgColor = getColor(color)
        NanoVG.nvgBeginPath(nvg)
        NanoVG.nvgRoundedRect(
            nvg,
            x - strokeWidth / 2f,
            y - strokeWidth / 2f,
            width + strokeWidth,
            height + strokeWidth,
            radius + strokeWidth / 2f,
        )
        NanoVG.nvgRoundedRect(nvg, x, y, width, height, radius)
        NanoVG.nvgPathWinding(nvg, NanoVG.NVG_HOLE)
        NanoVG.nvgFillColor(nvg, nvgColor)
        NanoVG.nvgFill(nvg)
    }

    fun drawGradientOutlineRoundedRect(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        radius: Float,
        strokeWidth: Float,
        color1: Color,
        color2: Color,
    ) {
        drawGradientOutlineRoundedRect(x, y, width, height, radius, strokeWidth, color1.rgb, color2.rgb)
    }

    fun drawGradientOutlineRoundedRect(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        radius: Float,
        strokeWidth: Float,
        color1: Int,
        color2: Int,
    ) {
        val tick = System.currentTimeMillis() % 3600 / 570f
        val max = width.coerceAtLeast(height)
        NanoVG.nvgBeginPath(nvg)
        NanoVG.nvgRoundedRect(nvg, x, y, width, height, radius)
        val nvgColor1 = getColor(color1)
        val nvgColor2 = getColor(color2)
        NanoVG.nvgFillColor(nvg, nvgColor1)
        NanoVG.nvgFillColor(nvg, nvgColor2)
        NanoVG.nvgStrokeWidth(nvg, strokeWidth)
        NanoVG.nvgStrokePaint(
            nvg,
            NanoVG.nvgLinearGradient(
                nvg,
                x + width / 2 - max / 2 * MathUtils.cos(tick.toDouble()),
                y + height / 2 - max / 2 * MathUtils.sin(tick.toDouble()),
                x + width / 2 + max / 2 * MathUtils.cos(tick.toDouble()),
                y + height / 2 + (max + 2f) * MathUtils.sin(tick.toDouble()),
                nvgColor1,
                nvgColor2,
                getAvailablePaint(),
            ),
        )
        NanoVG.nvgStroke(nvg)
    }

    fun drawArrow(
        x: Float,
        y: Float,
        size: Float,
        angle: Float,
        color: Color,
    ) {
        drawArrow(x, y, size, angle, color.rgb)
    }

    fun drawArrow(
        x: Float,
        y: Float,
        size: Float,
        angle: Float,
        color: Int,
    ) {
        save()
        NanoVG.nvgBeginPath(nvg)
        val offsetX = (size * cos(Math.toRadians(angle.toDouble()))).toFloat()
        val offsetY = (size * sin(Math.toRadians(angle.toDouble()))).toFloat()
        val diffX = x + offsetX / 2
        val diffY = y + offsetY / 2
        NanoVG.nvgTranslate(nvg, diffX, diffY)
        NanoVG.nvgRotate(nvg, Math.toRadians(angle.toDouble()).toFloat())
        NanoVG.nvgMoveTo(nvg, -size, -size / 2)
        NanoVG.nvgLineTo(nvg, 0f, 0f)
        NanoVG.nvgLineTo(nvg, -size, size / 2)
        NanoVG.nvgStrokeWidth(nvg, 0.8f)
        NanoVG.nvgStrokeColor(nvg, getColor(color))
        NanoVG.nvgStroke(nvg)
        restore()
    }

    fun drawShadow(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        radius: Float,
        strength: Int,
    ) {
        val bg = getAvailablePaint()
        NanoVG.nvgBoxGradient(
            nvg,
            x,
            y,
            width,
            height,
            radius,
            (strength * 2).toFloat(),
            getColor(0x32000000),
            getColor(0),
            bg,
        )
        NanoVG.nvgBeginPath(nvg)
        NanoVG.nvgRect(nvg, x - strength, y - strength, width + strength * 2, height + strength * 2)
        NanoVG.nvgRoundedRect(nvg, x, y, width, height, radius)
        NanoVG.nvgPathWinding(nvg, NanoVG.NVG_HOLE)
        NanoVG.nvgFillPaint(nvg, bg)
        NanoVG.nvgFill(nvg)
    }

    fun drawShadow(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        radius: Float,
    ) {
        drawShadow(x, y, width, height, radius, 7)
    }

    fun drawGradientShadow(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        radius: Float,
        color1: Color,
        color2: Color,
    ) {
        drawGradientShadow(x, y, width, height, radius, color1.rgb, color2.rgb)
    }

    fun drawGradientShadow(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        radius: Float,
        color1: Int,
        color2: Int,
    ) {
        var alpha = 1
        for (f in 10 downTo 1) {
            drawGradientOutlineRoundedRect(
                x - f / 2,
                y - f / 2,
                width + f,
                height + f,
                radius + 2,
                f.toFloat(),
                applyAlpha(color1, alpha),
                applyAlpha(color2, alpha),
            )
            alpha += 3
        }
    }

    fun drawRoundedGlow(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        radius: Float,
        color1: Color,
        strength: Int,
    ) {
        drawRoundedGlow(x, y, width, height, radius, color1.rgb, strength)
    }

    fun drawRoundedGlow(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        radius: Float,
        color1: Int,
        strength: Int,
    ) {
        var alpha = 1
        for (f in strength downTo 1) {
            drawGradientOutlineRoundedRect(
                x - f / 2,
                y - f / 2,
                width + f,
                height + f,
                radius + 2,
                f.toFloat(),
                applyAlpha(color1, alpha),
                applyAlpha(color1, alpha),
            )
            alpha += 2
        }
    }

    fun drawCircle(
        x: Float,
        y: Float,
        radius: Float,
        color: Color,
    ) {
        drawCircle(x, y, radius, color.rgb)
    }

    fun drawCircle(
        x: Float,
        y: Float,
        radius: Float,
        color: Int,
    ) {
        NanoVG.nvgBeginPath(nvg)
        NanoVG.nvgCircle(nvg, x, y, radius)
        val nvgColor = getColor(color)
        NanoVG.nvgFillColor(nvg, nvgColor)
        NanoVG.nvgFill(nvg)
    }

    fun drawArc(
        x: Float,
        y: Float,
        radius: Float,
        startAngle: Float,
        endAngle: Float,
        strokeWidth: Float,
        color: Color,
    ) {
        drawArc(x, y, radius, startAngle, endAngle, strokeWidth, color.rgb)
    }

    fun drawArc(
        x: Float,
        y: Float,
        radius: Float,
        startAngle: Float,
        endAngle: Float,
        strokeWidth: Float,
        color: Int,
    ) {
        val nvgColor = getColor(color)
        NanoVG.nvgBeginPath(nvg)
        NanoVG.nvgArc(
            nvg,
            x,
            y,
            radius,
            Math.toRadians(startAngle.toDouble()).toFloat(),
            Math.toRadians(endAngle.toDouble()).toFloat(),
            NanoVG.NVG_CW,
        )
        NanoVG.nvgStrokeWidth(nvg, strokeWidth)
        NanoVG.nvgStrokeColor(nvg, nvgColor)
        NanoVG.nvgStroke(nvg)
    }

    fun drawGradientCircle(
        x: Float,
        y: Float,
        radius: Float,
        color1: Color,
        color2: Color,
    ) {
        drawGradientCircle(x, y, radius, color1.rgb, color2.rgb)
    }

    fun drawGradientCircle(
        x: Float,
        y: Float,
        radius: Float,
        color1: Int,
        color2: Int,
    ) {
        NanoVG.nvgBeginPath(nvg)
        NanoVG.nvgCircle(nvg, x, y, radius)
        val nvgColor1 = getColor(color1)
        val nvgColor2 = getColor(color2)
        NanoVG.nvgFillColor(nvg, nvgColor1)
        NanoVG.nvgFillColor(nvg, nvgColor2)
        NanoVG.nvgFillPaint(
            nvg,
            NanoVG.nvgLinearGradient(nvg, x, y, radius, radius, nvgColor1, nvgColor2, getAvailablePaint()),
        )
        NanoVG.nvgFill(nvg)
    }

    fun fontBlur(blur: Float) {
        NanoVG.nvgFontBlur(nvg, blur)
    }

    fun drawText(
        text: String,
        x: Float,
        y: Float,
        color: Color,
        size: Float,
        font: Font,
    ) {
        drawText(text, x, y, color.rgb, size, font)
    }

    fun drawText(
        text: String,
        x: Float,
        y: Float,
        color: Int,
        size: Float,
        font: Font,
    ) {
        var y = y
        y += size / 2
        NanoVG.nvgBeginPath(nvg)
        NanoVG.nvgFontSize(nvg, size)
        NanoVG.nvgFontFace(nvg, font.name)
        NanoVG.nvgTextAlign(nvg, NanoVG.NVG_ALIGN_LEFT or NanoVG.NVG_ALIGN_MIDDLE)
        val nvgColor = getColor(color)
        NanoVG.nvgFillColor(nvg, nvgColor)
        NanoVG.nvgText(nvg, x, y, text)
    }

    fun drawBlurredText(
        text: String,
        x: Float,
        y: Float,
        color: Color,
        blurRadius: Float,
        size: Float,
        align: Int,
        font: Font,
    ) {
        drawBlurredText(text, x, y, color.rgb, blurRadius, size, align, font)
    }

    fun drawBlurredText(
        text: String,
        x: Float,
        y: Float,
        color: Int,
        blurRadius: Float,
        size: Float,
        align: Int,
        font: Font,
    ) {
//        y += size / 2;
        NanoVG.nvgBeginPath(nvg)
        NanoVG.nvgFontBlur(nvg, blurRadius)
        NanoVG.nvgFontSize(nvg, size)
        NanoVG.nvgFontFace(nvg, font.name)
        NanoVG.nvgTextAlign(nvg, align)
        val nvgColor = getColor(color)
        NanoVG.nvgFillColor(nvg, nvgColor)
        NanoVG.nvgText(nvg, x, y, text)
        NanoVG.nvgFontBlur(nvg, 0f)
    }

    fun drawTextGlowing(
        text: String,
        x: Float,
        y: Float,
        color: Color,
        blurRadius: Float,
        size: Float,
        font: Font,
    ) {
        drawTextGlowing(text, x, y, color.rgb, blurRadius, size, font)
    }

    fun drawTextGlowing(
        text: String,
        x: Float,
        y: Float,
        color: Int,
        blurRadius: Float,
        size: Float,
        font: Font,
    ) {
        drawTextGlowingBg(text, x, y, color, size, blurRadius, NanoVG.NVG_ALIGN_LEFT or NanoVG.NVG_ALIGN_MIDDLE, font)
        drawText(text, x, y, color, size, font)
    }

    fun drawCenteredTextGlowing(
        text: String,
        x: Float,
        y: Float,
        color: Color,
        blurRadius: Float,
        size: Float,
        font: Font,
    ) {
        drawCenteredTextGlowing(text, x, y, color.rgb, blurRadius, size, font)
    }

    fun drawCenteredTextGlowing(
        text: String,
        x: Float,
        y: Float,
        color: Int,
        blurRadius: Float,
        size: Float,
        font: Font,
    ) {
        drawTextGlowingBg(text, x, y, color, size, blurRadius, NanoVG.NVG_ALIGN_CENTER or NanoVG.NVG_ALIGN_MIDDLE, font)
        drawCenteredText(text, x, y, color, size, font)
    }

    private fun drawTextGlowingBg(
        text: String,
        x: Float,
        y: Float,
        color: Color,
        size: Float,
        blurRadius: Float,
        align: Int,
        font: Font,
    ) {
        drawTextGlowingBg(text, x, y, color.rgb, size, blurRadius, align, font)
    }

    private fun drawTextGlowingBg(
        text: String,
        x: Float,
        y: Float,
        color: Int,
        size: Float,
        blurRadius: Float,
        align: Int,
        font: Font,
    ) {
        var y = y
        y += size / 2
        NanoVG.nvgBeginPath(nvg)
        NanoVG.nvgFontSize(nvg, size)
        NanoVG.nvgFontFace(nvg, font.name)
        NanoVG.nvgTextAlign(nvg, align)
        val nvgColor = getColor(color)
        NanoVG.nvgFillColor(nvg, nvgColor)
        save()
        fontBlur(blurRadius)
        NanoVG.nvgText(nvg, x, y, text)
        restore()
    }

    fun drawTextBox(
        text: String,
        x: Float,
        y: Float,
        maxWidth: Float,
        color: Color,
        size: Float,
        font: Font,
    ) {
        drawTextBox(text, x, y, maxWidth, color.rgb, size, font)
    }

    fun drawTextBox(
        text: String,
        x: Float,
        y: Float,
        maxWidth: Float,
        color: Int,
        size: Float,
        font: Font,
    ) {
        var y = y
        y += size / 2
        NanoVG.nvgBeginPath(nvg)
        NanoVG.nvgFontSize(nvg, size)
        NanoVG.nvgFontFace(nvg, font.name)
        NanoVG.nvgTextAlign(nvg, NanoVG.NVG_ALIGN_LEFT or NanoVG.NVG_ALIGN_MIDDLE)
        val nvgColor = getColor(color)
        NanoVG.nvgFillColor(nvg, nvgColor)
        NanoVG.nvgTextBox(nvg, x, y, maxWidth, text)
    }

    fun drawCenteredText(
        text: String,
        x: Float,
        y: Float,
        color: Color,
        size: Float,
        font: Font,
    ) {
        drawCenteredText(text, x, y, color.rgb, size, font)
    }

    fun drawCenteredText(
        text: String,
        x: Float,
        y: Float,
        color: Int,
        size: Float,
        font: Font,
    ) {
        val textWidth = getTextWidth(text, size, font)
        drawText(text, x - (textWidth / 2f), y, color, size, font)
    }

    fun getTextWidth(
        text: String,
        size: Float,
        font: Font,
    ): Float {
        NanoVG.nvgFontSize(nvg, size)
        NanoVG.nvgFontFace(nvg, font.name)
        NanoVG.nvgTextBounds(nvg, 0f, 0f, text, f4Buff)
        NanoVG.nvgTextAlign(nvg, NanoVG.NVG_ALIGN_LEFT or NanoVG.NVG_ALIGN_MIDDLE)
        return f4Buff[2] - f4Buff[0]
    }

    fun getTextHeight(
        text: String,
        size: Float,
        font: Font,
    ): Float {
        NanoVG.nvgFontSize(nvg, size)
        NanoVG.nvgFontFace(nvg, font.name)
        NanoVG.nvgTextBounds(nvg, 0f, 0f, text, f4Buff)
        return f4Buff[3] - f4Buff[1]
    }

    fun getTextBoxHeight(
        text: String,
        size: Float,
        font: Font,
        maxWidth: Float,
    ): Float {
        NanoVG.nvgFontSize(nvg, size)
        NanoVG.nvgFontFace(nvg, font.name)
        NanoVG.nvgTextBoxBounds(nvg, 0f, 0f, maxWidth, text, f4Buff)
        return f4Buff[3] - f4Buff[1]
    }

    fun getLimitText(
        inputText: String,
        fontSize: Float,
        font: Font?,
        width: Float,
    ): String {
        var text = inputText
        var isInRange = false
        var isRemoved = false
        while (!isInRange) {
            if (getTextWidth(text, fontSize, font!!) > width) {
                text = text.substring(0, text.length - 1)
                isRemoved = true
            } else {
                isInRange = true
            }
        }
        return text + if (isRemoved) "..." else ""
    }

    fun getImageSize(location: ResourceLocation): Dimension? {
        if (!assetManager!!.loadImage(nvg, location)) {
            return null
        }
        val asset: NVGAsset = assetManager!!.getImageAsset(location) ?: return null
        return Dimension(asset.width, asset.height)
    }

    fun getImageSize(file: File): Dimension? {
        if (!assetManager!!.loadImage(nvg, file)) {
            return null
        }
        val asset: NVGAsset = assetManager!!.getImageAsset(file) ?: return null
        return Dimension(asset.width, asset.height)
    }

    fun scale(
        x: Float,
        y: Float,
        scaleX: Float,
        scaleY: Float,
    ) {
        NanoVG.nvgTranslate(nvg, x, y)
        NanoVG.nvgScale(nvg, scaleX, scaleY)
        NanoVG.nvgTranslate(nvg, -x, -y)
    }

    fun scale(
        x: Float,
        y: Float,
        scale: Float,
    ) {
        NanoVG.nvgTranslate(nvg, x, y)
        NanoVG.nvgScale(nvg, scale, scale)
        NanoVG.nvgTranslate(nvg, -x, -y)
    }

    fun scale(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        scale: Float,
    ) {
        NanoVG.nvgTranslate(nvg, (x + (x + width)) / 2, (y + (y + height)) / 2)
        NanoVG.nvgScale(nvg, scale, scale)
        NanoVG.nvgTranslate(nvg, -(x + (x + width)) / 2, -(y + (y + height)) / 2)
    }

    fun rotate(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        angle: Float,
    ) {
        NanoVG.nvgTranslate(nvg, (x + (x + width)) / 2, (y + (y + height)) / 2)
        NanoVG.nvgRotate(nvg, angle)
        NanoVG.nvgTranslate(nvg, -(x + (x + width)) / 2, -(y + (y + height)) / 2)
    }

    fun translate(
        x: Float,
        y: Float,
    ) {
        NanoVG.nvgTranslate(nvg, x, y)
    }

    fun setAlpha(alpha: Float) {
        NanoVG.nvgGlobalAlpha(nvg, alpha)
    }

    fun scissor(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
    ) {
        NanoVG.nvgScissor(nvg, x, y, width, height)
    }

    fun intersectScissor(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
    ) {
        NanoVG.nvgIntersectScissor(nvg, x, y, width, height)
    }

    fun resetScissor() {
        NanoVG.nnvgResetScissor(nvg)
    }

    fun save() {
        NanoVG.nvgSave(nvg)
    }

    fun restore() {
        NanoVG.nvgRestore(nvg)
    }

    fun rotateAt(
        x: Float,
        y: Float,
        angleRadians: Float,
    ) {
        NanoVG.nvgTranslate(nvg, x, y)
        NanoVG.nvgRotate(nvg, angleRadians)
        NanoVG.nvgTranslate(nvg, -x, -y)
    }

    fun rotateDegrees(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        angleDegrees: Float,
    ) {
        rotate(x, y, width, height, Math.toRadians(angleDegrees.toDouble()).toFloat())
    }

    fun rotateDegreesAt(
        x: Float,
        y: Float,
        angleDegrees: Float,
    ) {
        rotateAt(x, y, Math.toRadians(angleDegrees.toDouble()).toFloat())
    }

    inline fun withState(block: () -> Unit) {
        save()
        try {
            block()
        } finally {
            restore()
        }
    }

    fun drawSvg(
        location: ResourceLocation,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        color: Color,
    ) {
        drawSvg(location, x, y, width, height, color.rgb)
    }

    fun drawSvg(
        location: ResourceLocation,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        color: Int,
    ) {
        if (assetManager!!.loadSvg(nvg, location!!, width, height)) {
            val imagePaint = getAvailablePaint()
            val image = assetManager!!.getSvg(location, width, height)
            NanoVG.nvgBeginPath(nvg)
            NanoVG.nvgImagePattern(nvg, x, y, width, height, 0f, image, 1f, imagePaint)
            imagePaint.innerColor(getColor(color))
            imagePaint.outerColor(getColor(color))
            NanoVG.nvgRect(nvg, x, y, width, height)
            NanoVG.nvgFillPaint(nvg, imagePaint)
            NanoVG.nvgFill(nvg)
        }
    }

    fun drawImage(
        location: ResourceLocation,
        rect: Rect,
    ) {
        drawImage(location, rect.x, rect.y, rect.width, rect.height)
    }

    fun drawImage(
        location: ResourceLocation,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
    ) {
        if (assetManager!!.loadImage(nvg, location)) {
            val imagePaint = getAvailablePaint()
            val image = assetManager!!.getImage(location)
            NanoVG.nvgBeginPath(nvg)
            NanoVG.nvgImagePattern(nvg, x, y, width, height, 0f, image, 1f, imagePaint)
            NanoVG.nvgRect(nvg, x, y, width, height)
            NanoVG.nvgFillPaint(nvg, imagePaint)
            NanoVG.nvgFill(nvg)
        }
    }

    fun drawImage(
        location: ResourceLocation,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        alpha: Int,
    ) {
        if (assetManager!!.loadImage(nvg, location)) {
            val imagePaint = getAvailablePaint()
            val image = assetManager!!.getImage(location)
            NanoVG.nvgBeginPath(nvg)
            NanoVG.nvgImagePattern(nvg, x, y, width, height, 0f, image, alpha.toFloat(), imagePaint)
            NanoVG.nvgRect(nvg, x, y, width, height)
            NanoVG.nvgFillPaint(nvg, imagePaint)
            NanoVG.nvgFill(nvg)
        }
    }

    fun drawImage(
        file: File,
        rect: Rect,
    ) {
        drawImage(file, rect.x, rect.y, rect.width, rect.height)
    }

    fun drawImage(
        file: File,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
    ) {
        if (assetManager!!.loadImage(nvg, file)) {
            val imagePaint = getAvailablePaint()
            val image = assetManager!!.getImage(file)
            NanoVG.nvgBeginPath(nvg)
            NanoVG.nvgImagePattern(nvg, x, y, width, height, 0f, image, 1f, imagePaint)
            NanoVG.nvgRect(nvg, x, y, width, height)
            NanoVG.nvgFillPaint(nvg, imagePaint)
            NanoVG.nvgFill(nvg)
        }
    }

    fun drawImage(
        texture: Int,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        alpha: Float,
        flags: Int,
    ) {
        if (assetManager!!.loadImage(nvg, texture, width, height, flags)) {
            val image = assetManager!!.getImage(texture)
            NanoVG.nvgImageSize(nvg, image, intArrayOf(width.toInt()), intArrayOf((-height).toInt()))
            val p = getAvailablePaint()
            NanoVG.nvgImagePattern(nvg, x, y, width, height, 0f, image, alpha, p)
            NanoVG.nvgBeginPath(nvg)
            NanoVG.nvgRect(nvg, x, y, width, height)
            NanoVG.nvgFillPaint(nvg, p)
            NanoVG.nvgFill(nvg)
            NanoVG.nvgClosePath(nvg)
        }
    }

    fun drawImage(
        texture: Int,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        alpha: Float,
    ) {
        drawImage(texture, x, y, width, height, alpha, 0)
    }

    fun drawImage(
        texture: Int,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
    ) {
        drawImage(texture, x, y, width, height, 1.0f)
    }

    fun drawRoundedImage(
        texture: Int,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        radius: Float,
        alpha: Float,
    ) {
        if (assetManager!!.loadImage(nvg, texture, width, height)) {
            val image = assetManager!!.getImage(texture)
            NanoVG.nvgImageSize(nvg, image, intArrayOf(width.toInt()), intArrayOf((-height).toInt()))
            val p = getAvailablePaint()
            NanoVG.nvgImagePattern(nvg, x, y, width, height, 0f, image, alpha, p)
            NanoVG.nvgBeginPath(nvg)
            NanoVG.nvgRoundedRect(nvg, x, y, width, height, radius)
            NanoVG.nvgFillPaint(nvg, p)
            NanoVG.nvgFill(nvg)
            NanoVG.nvgClosePath(nvg)
        }
    }

    fun drawRoundedImage(
        texture: Int,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        radius: Float,
    ) {
        drawRoundedImage(texture, x, y, width, height, radius, 1.0f)
    }

    fun drawPlayerHead(
        location: ResourceLocation,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        radius: Float,
        alpha: Float,
    ) {
        if (mc.textureManager.getTexture(location) == null) {
            return
        }
        val texture = mc.textureManager.getTexture(location).glTextureId
        if (assetManager!!.loadImage(nvg, texture, width, height)) {
            val image = assetManager!!.getImage(texture)
            NanoVG.nvgImageSize(nvg, image, intArrayOf(width.toInt()), intArrayOf((-height).toInt()))
            val p = getAvailablePaint()
            val sizeMultiplier = 8f
            NanoVG.nvgImagePattern(
                nvg,
                x - width / 4 * sizeMultiplier / 2,
                y - height / 4 * sizeMultiplier / 2,
                width * sizeMultiplier,
                height * sizeMultiplier,
                0f,
                image,
                alpha,
                p,
            )
            NanoVG.nvgBeginPath(nvg)
            NanoVG.nvgRoundedRect(nvg, x, y, width, height, radius)
            NanoVG.nvgFillPaint(nvg, p)
            NanoVG.nvgFill(nvg)
            NanoVG.nvgClosePath(nvg)
            NanoVG.nvgImagePattern(
                nvg,
                x - width * 3.25f * sizeMultiplier / 2,
                y - height / 4 * sizeMultiplier / 2,
                width * sizeMultiplier,
                height * sizeMultiplier,
                0f,
                image,
                alpha,
                p,
            )
            NanoVG.nvgBeginPath(nvg)
            NanoVG.nvgRoundedRect(nvg, x, y, width, height, radius)
            NanoVG.nvgFillPaint(nvg, p)
            NanoVG.nvgFill(nvg)
            NanoVG.nvgClosePath(nvg)
        }
    }

    fun drawPlayerHead(
        location: ResourceLocation,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        radius: Float,
    ) {
        drawPlayerHead(location, x, y, width, height, radius, 1.0f)
    }

    fun drawRoundedImage(
        location: ResourceLocation,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        radius: Float,
        alpha: Float,
    ) {
        if (assetManager!!.loadImage(nvg, location)) {
            val imagePaint = getAvailablePaint()
            val image = assetManager!!.getImage(location)
            NanoVG.nvgBeginPath(nvg)
            NanoVG.nvgImagePattern(nvg, x, y, width, height, 0f, image, alpha, imagePaint)
            NanoVG.nvgRoundedRect(nvg, x, y, width, height, radius)
            NanoVG.nvgFillPaint(nvg, imagePaint)
            NanoVG.nvgFill(nvg)
        }
    }

    fun drawRoundedImage(
        location: ResourceLocation,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        radius: Float,
    ) {
        drawRoundedImage(location, x, y, width, height, radius, 1.0f)
    }

    fun drawRoundedImage(
        file: File,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        radius: Float,
        alpha: Float,
    ) {
        if (assetManager!!.loadImage(nvg, file)) {
            val imagePaint = getAvailablePaint()
            val image = assetManager!!.getImage(file)
            NanoVG.nvgBeginPath(nvg)
            NanoVG.nvgImagePattern(nvg, x, y, width, height, 0f, image, alpha, imagePaint)
            NanoVG.nvgRoundedRect(nvg, x, y, width, height, radius)
            NanoVG.nvgFillPaint(nvg, imagePaint)
            NanoVG.nvgFill(nvg)
        }
    }

    fun drawRoundedImage(
        file: File,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        radius: Float,
    ) {
        drawRoundedImage(file, x, y, width, height, radius, 1.0f)
    }

    fun loadImage(file: File?) {
        assetManager!!.loadImage(nvg, file!!)
    }

    fun loadImage(location: ResourceLocation?) {
        assetManager!!.loadImage(nvg, location!!)
    }

    fun getAssetManager(): AssetManager? = assetManager

    fun getContext(): Long = nvg

    fun drawScrollbar(
        baseX: Float,
        baseY: Float,
        baseWidth: Float,
        baseHeight: Float,
        contentHeight: Float,
        scrollValue: Float,
        palette: ColorPalette,
        accent: AccentColor,
        minHandleHeight: Float,
    ) {
        val viewportHeight = max(0f, baseHeight)
        val viewportWidth = max(0f, baseWidth)
        if (viewportHeight <= 0f || viewportWidth <= 0f) {
            return
        }

        val maxScroll = 0f.coerceAtLeast(contentHeight - viewportHeight)
        if (maxScroll <= 0f) {
            return
        }

        val trackX = baseX + viewportWidth - 10f
        val trackY = baseY + 10f
        val trackWidth = 4f
        val trackHeight = max(0f, viewportHeight - 20f)
        if (trackHeight <= 0f) {
            return
        }

        drawRoundedRect(
            trackX,
            trackY,
            trackWidth,
            trackHeight,
            2f,
            applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 130),
        )

        val visibleRatio = (viewportHeight / contentHeight.coerceAtLeast(1f)).coerceIn(0f, 1f)
        val handleHeight = min(trackHeight, max(minHandleHeight, trackHeight * visibleRatio))
        val scrollOffset = (-scrollValue).coerceIn(0f, maxScroll)
        val scrollProgress = if (maxScroll <= 0f) 0f else scrollOffset / maxScroll
        val handleY = trackY + (trackHeight - handleHeight) * scrollProgress

        drawGradientRoundedRect(
            trackX - 1f,
            handleY,
            trackWidth + 2f,
            handleHeight,
            3f,
            applyAlpha(accent.getColor1(), 190),
            applyAlpha(accent.getColor2(), 190),
        )
    }

    fun drawDivider(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        radius: Float,
        alpha: Float,
    ) {
        drawRoundedRect(x, y, width, height, radius, Color(255, 255, 255, alpha.toInt()))
    }

    fun drawLine(
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        strokeWidth: Float,
        color: Color,
    ) {
        val nvgColor = getColor(color)
        NanoVG.nvgBeginPath(nvg)
        NanoVG.nvgMoveTo(nvg, x1, y1)
        NanoVG.nvgLineTo(nvg, x2, y2)
        NanoVG.nvgStrokeWidth(nvg, strokeWidth)
        NanoVG.nvgStrokeColor(nvg, nvgColor)
        NanoVG.nvgStroke(nvg)
    }

    fun drawGradientLine(
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        strokeWidth: Float,
        color1: Color,
        color2: Color,
    ) {
        val bg = NVGPaint.create()
        val nvgColor1 = getColor(color1)
        val nvgColor2 = getColor(color2)

        NanoVG.nvgBeginPath(nvg)
        NanoVG.nvgMoveTo(nvg, x1, y1)
        NanoVG.nvgLineTo(nvg, x2, y2)
        NanoVG.nvgStrokeWidth(nvg, strokeWidth)
        NanoVG.nvgStrokePaint(nvg, NanoVG.nvgLinearGradient(nvg, x1, y1, x2, y2, nvgColor1, nvgColor2, bg))
        NanoVG.nvgStroke(nvg)
    }

    fun drawPolygon(
        centerX: Float,
        centerY: Float,
        radius: Float,
        sides: Int,
        rotation: Float,
        color: Color,
    ) {
        val nvgColor = getColor(color)
        val angleStep = 360f / sides

        NanoVG.nvgBeginPath(nvg)
        for (i in 0 until sides) {
            val angle = Math.toRadians((rotation + i * angleStep).toDouble()).toFloat()
            val x = centerX + radius * cos(angle)
            val y = centerY + radius * sin(angle)
            if (i == 0) {
                NanoVG.nvgMoveTo(nvg, x, y)
            } else {
                NanoVG.nvgLineTo(nvg, x, y)
            }
        }
        NanoVG.nvgClosePath(nvg)
        NanoVG.nvgFillColor(nvg, nvgColor)
        NanoVG.nvgFill(nvg)
    }

    fun drawPolygonOutline(
        centerX: Float,
        centerY: Float,
        radius: Float,
        sides: Int,
        rotation: Float,
        strokeWidth: Float,
        color: Color,
    ) {
        val nvgColor = getColor(color)
        val angleStep = 360f / sides

        NanoVG.nvgBeginPath(nvg)
        for (i in 0 until sides) {
            val angle = Math.toRadians((rotation + i * angleStep).toDouble()).toFloat()
            val x = centerX + radius * cos(angle)
            val y = centerY + radius * sin(angle)
            if (i == 0) {
                NanoVG.nvgMoveTo(nvg, x, y)
            } else {
                NanoVG.nvgLineTo(nvg, x, y)
            }
        }
        NanoVG.nvgClosePath(nvg)
        NanoVG.nvgStrokeWidth(nvg, strokeWidth)
        NanoVG.nvgStrokeColor(nvg, nvgColor)
        NanoVG.nvgStroke(nvg)
    }

    fun drawRoundedRectSelective(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        radius: Float,
        corners: Int,
        color: Color,
    ) {
        val nvgColor = getColor(color)
        val topLeft = (corners and 1) != 0
        val topRight = (corners and 2) != 0
        val bottomRight = (corners and 4) != 0
        val bottomLeft = (corners and 8) != 0

        NanoVG.nvgBeginPath(nvg)
        NanoVG.nvgRoundedRectVarying(
            nvg,
            x,
            y,
            width,
            height,
            if (topLeft) radius else 0f,
            if (topRight) radius else 0f,
            if (bottomRight) radius else 0f,
            if (bottomLeft) radius else 0f,
        )
        NanoVG.nvgFillColor(nvg, nvgColor)
        NanoVG.nvgFill(nvg)
    }

    fun drawInsetBorder(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        radius: Float,
        borderWidth: Float,
        color: Color,
    ) {
        if (borderWidth <= 0f || width <= 0f || height <= 0f) {
            return
        }
        val inset = borderWidth / 2f
        val innerWidth = (width - borderWidth).coerceAtLeast(0f)
        val innerHeight = (height - borderWidth).coerceAtLeast(0f)
        val innerRadius = (radius - inset).coerceAtLeast(0f)
        drawOutlineRoundedRect(
            x + inset,
            y + inset,
            innerWidth,
            innerHeight,
            innerRadius,
            borderWidth,
            color,
        )
    }

    fun drawGlowRect(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        radius: Float,
        color: Color,
        strength: Int = 5,
    ) {
        var alpha = 10
        var f = strength.toFloat()
        while (f > 0f) {
            drawRoundedRect(
                x - f / 2,
                y - f / 2,
                width + f,
                height + f,
                radius + f / 2,
                applyAlpha(color, alpha),
            )
            alpha += 5
            f -= 0.5f
        }
    }

    fun drawMultilineText(
        text: String,
        x: Float,
        y: Float,
        maxWidth: Float,
        lineHeight: Float,
        color: Color,
        size: Float,
        font: Font,
    ) {
        val lines = text.split("\n")
        var currentY = y
        for (line in lines) {
            drawTextBox(line, x, currentY, maxWidth, color, size, font)
            currentY += lineHeight
        }
    }

    fun drawCenteredIcon(
        icon: String,
        x: Float,
        y: Float,
        size: Float,
        color: Color,
    ) {
        val iconFont = Fonts.LUCIDE
        val iconWidth = getTextWidth(icon, size, iconFont)
        val iconHeight = getTextHeight(icon, size, iconFont)
        drawText(icon, x - iconWidth / 2, y - iconHeight / 2, color, size, iconFont)
    }
}

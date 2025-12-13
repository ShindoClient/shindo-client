package me.miki.shindo.management.nanovg

import me.miki.shindo.logger.ShindoLogger
import me.miki.shindo.management.color.AccentColor
import me.miki.shindo.management.color.palette.ColorPalette
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.nanovg.asset.AssetManager
import me.miki.shindo.management.nanovg.asset.NVGAsset
import me.miki.shindo.management.nanovg.font.Font
import me.miki.shindo.management.nanovg.font.FontManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.utils.ColorUtils
import me.miki.shindo.utils.MathUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.util.ResourceLocation
import org.lwjgl.nanovg.NVGColor
import org.lwjgl.nanovg.NVGPaint
import org.lwjgl.nanovg.NanoVG
import org.lwjgl.nanovg.NanoVGGL2
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.io.File
import java.util.HashMap

class NanoVGManager {

    private val mc: Minecraft = Minecraft.getMinecraft()
    private val colorCache: HashMap<Int, NVGColor> = HashMap()
    private val nvg: Long
    private val fontManager: FontManager
    val assetManager: AssetManager

    init {
        nvg = NanoVGGL2.nvgCreate(NanoVGGL2.NVG_ANTIALIAS)
        if (nvg == 0L) {
            ShindoLogger.error("Failed to create NanoVG context")
            mc.shutdown()
        }

        fontManager = FontManager()
        fontManager.init(nvg)
        assetManager = AssetManager()
    }

    @JvmOverloads
    fun setupAndDraw(task: Runnable, scale: Boolean = true) {
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

    fun beginFrame() {
        beginFrame(true)
    }

    @JvmOverloads
    fun beginFrame(scale: Boolean = true) {
        val sr = ScaledResolution(mc)
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS)
        NanoVG.nvgBeginFrame(nvg, mc.displayWidth.toFloat(), mc.displayHeight.toFloat(), 1f)
        if (scale) {
            NanoVG.nvgScale(nvg, sr.scaleFactor.toFloat(), sr.scaleFactor.toFloat())
        }
    }

    fun endFrame() {
        GL11.glDisable(GL11.GL_ALPHA_TEST)
        NanoVG.nvgEndFrame(nvg)
        GL11.glPopAttrib()
    }

    fun setupAndDraw(task: Runnable) {
        setupAndDraw(task, true)
    }

    fun drawAlphaBar(x: Float, y: Float, width: Float, height: Float, radius: Float, color: Color) {
        val bg = NVGPaint.create()

        NanoVG.nvgBeginPath(nvg)
        NanoVG.nvgRoundedRect(nvg, x, y, width, height, radius)
        val nvgColor = getColor(color)
        val nvgColor2 = getColor(Color(0, 0, 0, 0))
        NanoVG.nvgFillPaint(nvg, NanoVG.nvgLinearGradient(nvg, x, y, x + width, y, nvgColor2, nvgColor, bg))
        NanoVG.nvgFill(nvg)
    }

    fun drawHSBBox(x: Float, y: Float, width: Float, height: Float, radius: Float, color: Color) {
        drawRoundedRect(x, y, width, height, radius, color)

        val bg = NVGPaint.create()
        NanoVG.nvgBeginPath(nvg)
        NanoVG.nvgRoundedRect(nvg, x, y, width, height, radius)
        val nvgColor = getColor(Color.WHITE)
        val nvgColor2 = getColor(Color(0, 0, 0, 0))
        NanoVG.nvgFillPaint(nvg, NanoVG.nvgLinearGradient(nvg, x + 8, y + 8, x + width, y, nvgColor, nvgColor2, bg))
        NanoVG.nvgFill(nvg)

        val bg2 = NVGPaint.create()
        NanoVG.nvgBeginPath(nvg)
        NanoVG.nvgRoundedRect(nvg, x, y, width, height, radius)
        val nvgColor3 = getColor(Color(0, 0, 0, 0))
        val nvgColor4 = getColor(Color.BLACK)

        NanoVG.nvgFillPaint(nvg, NanoVG.nvgLinearGradient(nvg, x + 8, y + 8, x, y + height, nvgColor3, nvgColor4, bg2))
        NanoVG.nvgFill(nvg)
    }

    fun drawRect(x: Float, y: Float, width: Float, height: Float, color: Color) {
        NanoVG.nvgBeginPath(nvg)
        NanoVG.nvgRect(nvg, x, y, width, height)
        val nvgColor = getColor(color)
        NanoVG.nvgFillColor(nvg, nvgColor)
        NanoVG.nvgFill(nvg)
    }

    fun drawRoundedRect(x: Float, y: Float, width: Float, height: Float, radius: Float, color: Color) {
        NanoVG.nvgBeginPath(nvg)
        NanoVG.nvgRoundedRect(nvg, x, y, width, height, radius)
        val nvgColor = getColor(color)
        NanoVG.nvgFillColor(nvg, nvgColor)
        NanoVG.nvgFill(nvg)
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
        color: Color
    ) {
        NanoVG.nvgBeginPath(nvg)
        NanoVG.nvgRoundedRectVarying(nvg, x, y, width, height, topLeftRadius, topRightRadius, bottomRightRadius, bottomLeftRadius)
        val nvgColor = getColor(color)
        NanoVG.nvgFillColor(nvg, nvgColor)
        NanoVG.nvgFill(nvg)
    }

    fun drawVerticalGradientRect(x: Float, y: Float, width: Float, height: Float, color1: Color, color2: Color) {
        val bg = NVGPaint.create()
        NanoVG.nvgBeginPath(nvg)
        NanoVG.nvgRect(nvg, x, y, width, height)

        val nvgColor1 = getColor(color1)
        val nvgColor2 = getColor(color2)

        NanoVG.nvgFillColor(nvg, nvgColor1)
        NanoVG.nvgFillColor(nvg, nvgColor2)

        NanoVG.nvgFillPaint(nvg, NanoVG.nvgLinearGradient(nvg, x, y, x, y + height, nvgColor1, nvgColor2, bg))
        NanoVG.nvgFill(nvg)
    }

    fun drawHorizontalGradientRect(x: Float, y: Float, width: Float, height: Float, color1: Color, color2: Color) {
        val bg = NVGPaint.create()
        NanoVG.nvgBeginPath(nvg)
        NanoVG.nvgRect(nvg, x, y, width, height)

        val nvgColor1 = getColor(color1)
        val nvgColor2 = getColor(color2)

        NanoVG.nvgFillColor(nvg, nvgColor1)
        NanoVG.nvgFillColor(nvg, nvgColor2)

        NanoVG.nvgFillPaint(nvg, NanoVG.nvgLinearGradient(nvg, x, y, x + width, y, nvgColor1, nvgColor2, bg))
        NanoVG.nvgFill(nvg)
    }

    fun drawGradientRect(x: Float, y: Float, width: Float, height: Float, color1: Color, color2: Color) {
        val bg = NVGPaint.create()

        val tick: Double = (System.currentTimeMillis() % 3600 / 570f).toDouble()
        val max = Math.max(width, height)

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
                x + width / 2 - (max / 2) * MathUtils.cos(tick),
                y + height / 2 - (max / 2) * MathUtils.sin(tick),
                x + width / 2 + (max / 2) * MathUtils.cos(tick),
                y + height / 2 + (max + 2f) * MathUtils.sin(tick),
                nvgColor1,
                nvgColor2,
                bg
            )
        )
        NanoVG.nvgFill(nvg)
    }

    fun drawGradientRoundedRect(x: Float, y: Float, width: Float, height: Float, radius: Float, color1: Color, color2: Color) {
        val bg = NVGPaint.create()

        val tick: Double = (System.currentTimeMillis() % 3600 / 570f).toDouble()
        val max = Math.max(width, height)

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
                x + width / 2 - (max / 2) * MathUtils.cos(tick),
                y + height / 2 - (max / 2) * MathUtils.sin(tick),
                x + width / 2 + (max / 2) * MathUtils.cos(tick),
                y + height / 2 + (max + 2f) * MathUtils.sin(tick),
                nvgColor1,
                nvgColor2,
                bg
            )
        )
        NanoVG.nvgFill(nvg)
    }

    fun drawOutlineRoundedRect(x: Float, y: Float, width: Float, height: Float, radius: Float, strokeWidth: Float, color: Color) {
        val nvgColor = getColor(color)
        NanoVG.nvgBeginPath(nvg)
        NanoVG.nvgRoundedRect(nvg, x, y, width, height, radius)
        NanoVG.nvgStrokeWidth(nvg, strokeWidth)
        NanoVG.nvgStrokeColor(nvg, nvgColor)
        NanoVG.nvgStroke(nvg)
    }

    fun drawGradientOutlineRoundedRect(x: Float, y: Float, width: Float, height: Float, radius: Float, strokeWidth: Float, color1: Color, color2: Color) {
        val bg = NVGPaint.create()

        val tick: Double = (System.currentTimeMillis() % 3600 / 570f).toDouble()
        val max = Math.max(width, height)

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
                x + width / 2 - (max / 2) * MathUtils.cos(tick),
                y + height / 2 - (max / 2) * MathUtils.sin(tick),
                x + width / 2 + (max / 2) * MathUtils.cos(tick),
                y + height / 2 + (max + 2f) * MathUtils.sin(tick),
                nvgColor1,
                nvgColor2,
                bg
            )
        )
        NanoVG.nvgStroke(nvg)
    }

    fun drawRadialRoundedRect(x: Float, y: Float, width: Float, height: Float, radius: Float, centerColor: Color, edgeColor: Color) {
        val paint = NVGPaint.create()

        val cx = x + width / 2f
        val cy = y + height / 2f

        val inner = 4f
        val outer = Math.max(width, height)

        val c1 = getColor(centerColor)
        val c2 = getColor(edgeColor)

        NanoVG.nvgBeginPath(nvg)
        NanoVG.nvgRoundedRect(nvg, x, y, width, height, radius)

        NanoVG.nvgFillPaint(
            nvg,
            NanoVG.nvgRadialGradient(
                nvg,
                cx,
                cy,
                inner,
                outer,
                c1,
                c2,
                paint
            )
        )

        NanoVG.nvgFill(nvg)
    }

    fun drawArrow(x: Float, y: Float, size: Float, angle: Float, color: Color) {
        save()

        NanoVG.nvgBeginPath(nvg)

        val offsetX = (size * Math.cos(Math.toRadians(angle.toDouble()))).toFloat()
        val offsetY = (size * Math.sin(Math.toRadians(angle.toDouble()))).toFloat()

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

    fun drawShadow(x: Float, y: Float, width: Float, height: Float, radius: Float, strength: Int) {
        var alpha = 1
        var f = strength.toFloat()
        while (f > 0f) {
            drawOutlineRoundedRect(x - f / 2, y - f / 2, width + f, height + f, radius + 2, f, Color(0, 0, 0, alpha))
            alpha += 2
            f -= 1f
        }
    }

    fun drawShadow(x: Float, y: Float, width: Float, height: Float, radius: Float) {
        drawShadow(x, y, width, height, radius, 7)
    }

    fun drawGradientShadow(x: Float, y: Float, width: Float, height: Float, radius: Float, color1: Color, color2: Color) {
        var alpha = 1
        var f = 10f
        while (f > 0f) {
            drawGradientOutlineRoundedRect(
                x - f / 2,
                y - f / 2,
                width + f,
                height + f,
                radius + 2,
                f,
                ColorUtils.applyAlpha(color1, alpha),
                ColorUtils.applyAlpha(color2, alpha)
            )
            alpha += 3
            f -= 1f
        }
    }

    fun drawRoundedGlow(x: Float, y: Float, width: Float, height: Float, radius: Float, color1: Color, strength: Int) {
        var alpha = 1
        var f = strength.toFloat()
        while (f > 0f) {
            drawGradientOutlineRoundedRect(
                x - f / 2,
                y - f / 2,
                width + f,
                height + f,
                radius + 2,
                f,
                ColorUtils.applyAlpha(color1, alpha),
                ColorUtils.applyAlpha(color1, alpha)
            )
            alpha += 2
            f -= 1f
        }
    }

    fun drawCircle(x: Float, y: Float, radius: Float, color: Color) {
        NanoVG.nvgBeginPath(nvg)
        NanoVG.nvgCircle(nvg, x, y, radius)
        val nvgColor = getColor(color)
        NanoVG.nvgFillColor(nvg, nvgColor)
        NanoVG.nvgFill(nvg)
    }

    fun drawArc(x: Float, y: Float, radius: Float, startAngle: Float, endAngle: Float, strokeWidth: Float, color: Color) {
        val nvgColor = getColor(color)

        NanoVG.nvgBeginPath(nvg)
        NanoVG.nvgArc(nvg, x, y, radius, Math.toRadians(startAngle.toDouble()).toFloat(), Math.toRadians(endAngle.toDouble()).toFloat(), NanoVG.NVG_CW)
        NanoVG.nvgStrokeWidth(nvg, strokeWidth)
        NanoVG.nvgStrokeColor(nvg, nvgColor)
        NanoVG.nvgStroke(nvg)
    }

    fun drawGradientCircle(x: Float, y: Float, radius: Float, color1: Color, color2: Color) {
        val bg = NVGPaint.create()

        NanoVG.nvgBeginPath(nvg)
        NanoVG.nvgCircle(nvg, x, y, radius)

        val nvgColor1 = getColor(color1)
        val nvgColor2 = getColor(color2)

        NanoVG.nvgFillColor(nvg, nvgColor1)
        NanoVG.nvgFillColor(nvg, nvgColor2)

        NanoVG.nvgFillPaint(nvg, NanoVG.nvgLinearGradient(nvg, x, y, radius, radius, nvgColor1, nvgColor2, bg))
        NanoVG.nvgFill(nvg)
    }

    fun fontBlur(blur: Float) {
        NanoVG.nvgFontBlur(nvg, blur)
    }

    fun drawText(text: String, x: Float, y: Float, color: Color, size: Float, font: Font) {
        var textY = y + size / 2

        NanoVG.nvgBeginPath(nvg)
        NanoVG.nvgFontSize(nvg, size)
        NanoVG.nvgFontFace(nvg, font.name)
        NanoVG.nvgTextAlign(nvg, NanoVG.NVG_ALIGN_LEFT or NanoVG.NVG_ALIGN_MIDDLE)

        val nvgColor = getColor(color)

        NanoVG.nvgFillColor(nvg, nvgColor)
        NanoVG.nvgText(nvg, x, textY, text)
    }

    fun drawFormattedText(text: String, x: Float, y: Float, defaultColor: Color, size: Float, font: Font) {
        var cursorX = x
        var currentColor = defaultColor
        var bold = false
        var italic = false

        var i = 0
        while (i < text.length) {
            val c = text[i]
            if (c == 'õ' && i + 1 < text.length) {
                val code = Character.toLowerCase(text[++i])
                when {
                    code in '0'..'f' -> {
                        currentColor = getColorByCode(code)
                        bold = false
                        italic = false
                    }
                    code == 'l' -> bold = true
                    code == 'o' -> italic = true
                    code == 'r' -> {
                        currentColor = defaultColor
                        bold = false
                        italic = false
                    }
                }
                i++
                continue
            }

            val styledFont = getFontWithStyle(font, bold, italic)
            val s = c.toString()
            drawText(s, cursorX, y, currentColor, size, styledFont)
            cursorX += getTextWidth(s, size, styledFont)
            i++
        }
    }

    fun drawTextGlowing(text: String, x: Float, y: Float, color: Color, blurRadius: Float, size: Float, font: Font) {
        drawTextGlowingBg(text, x, y, color, size, blurRadius, font)
        drawText(text, x, y, color, size, font)
    }

    private fun drawTextGlowingBg(text: String, x: Float, y: Float, color: Color, size: Float, blurRadius: Float, font: Font) {
        var textY = y + size / 2

        NanoVG.nvgBeginPath(nvg)
        NanoVG.nvgFontSize(nvg, size)
        NanoVG.nvgFontFace(nvg, font.name)
        NanoVG.nvgTextAlign(nvg, NanoVG.NVG_ALIGN_LEFT or NanoVG.NVG_ALIGN_MIDDLE)
        val nvgColor = getColor(color)
        NanoVG.nvgFillColor(nvg, nvgColor)
        save()
        fontBlur(blurRadius)
        NanoVG.nvgText(nvg, x, textY, text)
        restore()
    }

    fun drawTextBox(text: String, x: Float, y: Float, maxWidth: Float, color: Color, size: Float, font: Font) {
        var textY = y + size / 2

        NanoVG.nvgBeginPath(nvg)
        NanoVG.nvgFontSize(nvg, size)
        NanoVG.nvgFontFace(nvg, font.name)
        NanoVG.nvgTextAlign(nvg, NanoVG.NVG_ALIGN_LEFT or NanoVG.NVG_ALIGN_MIDDLE)
        val nvgColor = getColor(color)

        NanoVG.nvgFillColor(nvg, nvgColor)
        NanoVG.nvgTextBox(nvg, x, textY, maxWidth, text)
    }

    fun drawCenteredText(text: String, x: Float, y: Float, color: Color, size: Float, font: Font) {
        val textWidth = getTextWidth(text, size, font).toInt()
        drawText(text, x - (textWidth shr 1), y, color, size, font)
    }

    fun getTextWidth(text: String, size: Float, font: Font): Float {
        val bounds = FloatArray(4)

        NanoVG.nvgFontSize(nvg, size)
        NanoVG.nvgFontFace(nvg, font.name)
        NanoVG.nvgTextBounds(nvg, 0f, 0f, text, bounds)
        NanoVG.nvgTextAlign(nvg, NanoVG.NVG_ALIGN_LEFT or NanoVG.NVG_ALIGN_MIDDLE)

        return bounds[2] - bounds[0]
    }

    fun getTextHeight(text: String, size: Float, font: Font): Float {
        val bounds = FloatArray(4)

        NanoVG.nvgFontSize(nvg, size)
        NanoVG.nvgFontFace(nvg, font.name)
        NanoVG.nvgTextBounds(nvg, 0f, 0f, text, bounds)

        return bounds[3] - bounds[1]
    }

    fun getTextBoxHeight(text: String, size: Float, font: Font, maxWidth: Float): Float {
        val bounds = FloatArray(4)

        NanoVG.nvgFontSize(nvg, size)
        NanoVG.nvgFontFace(nvg, font.name)
        NanoVG.nvgTextBoxBounds(nvg, 0f, 0f, maxWidth, text, bounds)

        return bounds[3] - bounds[1]
    }

    fun getLimitText(inputText: String, fontSize: Float, font: Font, width: Float): String {
        var text = inputText
        var isInRange = false
        var isRemoved = false

        while (!isInRange) {
            if (getTextWidth(text, fontSize, font) > width) {
                text = text.substring(0, text.length - 1)
                isRemoved = true
            } else {
                isInRange = true
            }
        }

        return text + if (isRemoved) "..." else ""
    }

    fun drawSvg(location: ResourceLocation, x: Float, y: Float, width: Float, height: Float, color: Color) {
        if (assetManager.loadSvg(nvg, location, width, height)) {
            val imagePaint = NVGPaint.calloc()
            val image = assetManager.getSvg(location, width, height)

            NanoVG.nvgBeginPath(nvg)
            NanoVG.nvgImagePattern(nvg, x, y, width, height, 0f, image, 1f, imagePaint)

            imagePaint.innerColor(getColor(color))
            imagePaint.outerColor(getColor(color))

            NanoVG.nvgRect(nvg, x, y, width, height)
            NanoVG.nvgFillPaint(nvg, imagePaint)
            NanoVG.nvgFill(nvg)

            imagePaint.free()
        }
    }

    fun drawImage(location: ResourceLocation, x: Float, y: Float, width: Float, height: Float) {
        if (assetManager.loadImage(nvg, location)) {
            val imagePaint = NVGPaint.calloc()
            val image = assetManager.getImage(location)

            NanoVG.nvgBeginPath(nvg)
            NanoVG.nvgImagePattern(nvg, x, y, width, height, 0f, image, 1f, imagePaint)

            NanoVG.nvgRect(nvg, x, y, width, height)
            NanoVG.nvgFillPaint(nvg, imagePaint)
            NanoVG.nvgFill(nvg)

            imagePaint.free()
        }
    }

    fun getImageSize(location: ResourceLocation): java.awt.Dimension? {
        if (!assetManager.loadImage(nvg, location)) {
            return null
        }
        val asset: NVGAsset = assetManager.getImageAsset(location) ?: return null
        return java.awt.Dimension(asset.width, asset.height)
    }

    fun getImageSize(file: File): java.awt.Dimension? {
        if (!assetManager.loadImage(nvg, file)) {
            return null
        }
        val asset: NVGAsset = assetManager.getImageAsset(file) ?: return null
        return java.awt.Dimension(asset.width, asset.height)
    }

    fun drawImage(location: ResourceLocation, x: Float, y: Float, width: Float, height: Float, alpha: Int) {
        if (assetManager.loadImage(nvg, location)) {
            val imagePaint = NVGPaint.calloc()
            val image = assetManager.getImage(location)

            NanoVG.nvgBeginPath(nvg)
            NanoVG.nvgImagePattern(nvg, x, y, width, height, 0f, image, alpha.toFloat(), imagePaint)

            NanoVG.nvgRect(nvg, x, y, width, height)
            NanoVG.nvgFillPaint(nvg, imagePaint)
            NanoVG.nvgFill(nvg)

            imagePaint.free()
        }
    }

    fun drawImage(file: File, x: Float, y: Float, width: Float, height: Float) {
        if (assetManager.loadImage(nvg, file)) {
            val imagePaint = NVGPaint.calloc()
            val image = assetManager.getImage(file)

            NanoVG.nvgBeginPath(nvg)
            NanoVG.nvgImagePattern(nvg, x, y, width, height, 0f, image, 1f, imagePaint)

            NanoVG.nvgRect(nvg, x, y, width, height)
            NanoVG.nvgFillPaint(nvg, imagePaint)
            NanoVG.nvgFill(nvg)

            imagePaint.free()
        }
    }

    fun drawImage(texture: Int, x: Float, y: Float, width: Float, height: Float, alpha: Float) {
        if (assetManager.loadImage(nvg, texture, width, height)) {
            val image = assetManager.getImage(texture)

            NanoVG.nvgImageSize(nvg, image, intArrayOf(width.toInt()), intArrayOf(-height.toInt()))
            val p = NVGPaint.calloc()

            NanoVG.nvgImagePattern(nvg, x, y, width, height, 0f, image, alpha, p)
            NanoVG.nvgBeginPath(nvg)
            NanoVG.nvgRect(nvg, x, y, width, height)
            NanoVG.nvgFillPaint(nvg, p)
            NanoVG.nvgFill(nvg)
            NanoVG.nvgClosePath(nvg)

            p.free()
        }
    }

    fun drawImage(texture: Int, x: Float, y: Float, width: Float, height: Float) {
        drawImage(texture, x, y, width, height, 1.0f)
    }

    fun drawRoundedImage(texture: Int, x: Float, y: Float, width: Float, height: Float, radius: Float, alpha: Float) {
        if (assetManager.loadImage(nvg, texture, width, height)) {
            val image = assetManager.getImage(texture)

            NanoVG.nvgImageSize(nvg, image, intArrayOf(width.toInt()), intArrayOf(-height.toInt()))
            val p = NVGPaint.calloc()

            NanoVG.nvgImagePattern(nvg, x, y, width, height, 0f, image, alpha, p)
            NanoVG.nvgBeginPath(nvg)
            NanoVG.nvgRoundedRect(nvg, x, y, width, height, radius)
            NanoVG.nvgFillPaint(nvg, p)
            NanoVG.nvgFill(nvg)
            NanoVG.nvgClosePath(nvg)

            p.free()
        }
    }

    fun drawRoundedImage(texture: Int, x: Float, y: Float, width: Float, height: Float, radius: Float) {
        drawRoundedImage(texture, x, y, width, height, radius, 1.0f)
    }

    fun drawPlayerHead(location: ResourceLocation?, x: Float, y: Float, width: Float, height: Float, radius: Float, alpha: Float) {
        if (location == null || mc.textureManager.getTexture(location) == null) {
            return
        }

        val texture = mc.textureManager.getTexture(location).glTextureId
        if (assetManager.loadImage(nvg, texture, width, height)) {
            val image = assetManager.getImage(texture)

            NanoVG.nvgImageSize(nvg, image, intArrayOf(width.toInt()), intArrayOf(-height.toInt()))
            val p = NVGPaint.calloc()

            val sizeMultiplier = 8f

            NanoVG.nvgImagePattern(nvg, x - width / 4 * sizeMultiplier / 2, y - height / 4 * sizeMultiplier / 2, width * sizeMultiplier, height * sizeMultiplier, 0f, image, alpha, p)
            NanoVG.nvgBeginPath(nvg)
            NanoVG.nvgRoundedRect(nvg, x, y, width, height, radius)
            NanoVG.nvgFillPaint(nvg, p)
            NanoVG.nvgFill(nvg)
            NanoVG.nvgClosePath(nvg)

            NanoVG.nvgImagePattern(nvg, x - width * 3.25f * sizeMultiplier / 2, y - height / 4 * sizeMultiplier / 2, width * sizeMultiplier, height * sizeMultiplier, 0f, image, alpha, p)
            NanoVG.nvgBeginPath(nvg)
            NanoVG.nvgRoundedRect(nvg, x, y, width, height, radius)
            NanoVG.nvgFillPaint(nvg, p)
            NanoVG.nvgFill(nvg)
            NanoVG.nvgClosePath(nvg)

            p.free()
        }
    }

    fun drawPlayerHead(location: ResourceLocation?, x: Float, y: Float, width: Float, height: Float, radius: Float) {
        drawPlayerHead(location, x, y, width, height, radius, 1.0f)
    }

    fun drawRoundedImage(location: ResourceLocation, x: Float, y: Float, width: Float, height: Float, radius: Float, alpha: Float) {
        if (assetManager.loadImage(nvg, location)) {
            val imagePaint = NVGPaint.calloc()
            val image = assetManager.getImage(location)

            NanoVG.nvgBeginPath(nvg)
            NanoVG.nvgImagePattern(nvg, x, y, width, height, 0f, image, alpha, imagePaint)

            NanoVG.nvgRoundedRect(nvg, x, y, width, height, radius)
            NanoVG.nvgFillPaint(nvg, imagePaint)
            NanoVG.nvgFill(nvg)

            imagePaint.free()
        }
    }

    fun drawRoundedImage(location: ResourceLocation, x: Float, y: Float, width: Float, height: Float, radius: Float) {
        drawRoundedImage(location, x, y, width, height, radius, 1.0f)
    }

    fun drawRoundedImage(file: File, x: Float, y: Float, width: Float, height: Float, radius: Float, alpha: Float) {
        if (assetManager.loadImage(nvg, file)) {
            val imagePaint = NVGPaint.calloc()
            val image = assetManager.getImage(file)

            NanoVG.nvgBeginPath(nvg)
            NanoVG.nvgImagePattern(nvg, x, y, width, height, 0f, image, alpha, imagePaint)

            NanoVG.nvgRoundedRect(nvg, x, y, width, height, radius)
            NanoVG.nvgFillPaint(nvg, imagePaint)
            NanoVG.nvgFill(nvg)

            imagePaint.free()
        }
    }

    fun drawRoundedImage(file: File, x: Float, y: Float, width: Float, height: Float, radius: Float) {
        drawRoundedImage(file, x, y, width, height, radius, 1.0f)
    }

    fun loadImage(file: File) {
        assetManager.loadImage(nvg, file)
    }

    fun loadImage(location: ResourceLocation) {
        assetManager.loadImage(nvg, location)
    }

    fun scale(x: Float, y: Float, scale: Float) {
        NanoVG.nvgTranslate(nvg, x, y)
        NanoVG.nvgScale(nvg, scale, scale)
        NanoVG.nvgTranslate(nvg, -x, -y)
    }

    fun scale(x: Float, y: Float, width: Float, height: Float, scale: Float) {
        NanoVG.nvgTranslate(nvg, (x + (x + width)) / 2, (y + (y + height)) / 2)
        NanoVG.nvgScale(nvg, scale, scale)
        NanoVG.nvgTranslate(nvg, -(x + (x + width)) / 2, -(y + (y + height)) / 2)
    }

    fun rotate(x: Float, y: Float, width: Float, height: Float, angle: Float) {
        NanoVG.nvgTranslate(nvg, (x + (x + width)) / 2, (y + (y + height)) / 2)
        NanoVG.nvgRotate(nvg, angle)
        NanoVG.nvgTranslate(nvg, -(x + (x + width)) / 2, -(y + (y + height)) / 2)
    }

    fun translate(x: Float, y: Float) {
        NanoVG.nvgTranslate(nvg, x, y)
    }

    fun setAlpha(alpha: Float) {
        NanoVG.nvgGlobalAlpha(nvg, alpha)
    }

    fun scissor(x: Float, y: Float, width: Float, height: Float) {
        NanoVG.nvgScissor(nvg, x, y, width, height)
    }

    fun intersectScissor(x: Float, y: Float, width: Float, height: Float) {
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

    fun drawScrollbar(baseX: Float, baseY: Float, baseWidth: Float, baseHeight: Float, contentHeight: Float, scrollValue: Float, palette: ColorPalette, accent: AccentColor, minHandleHeight: Float) {
        val maxScroll = Math.max(0f, contentHeight - baseHeight)
        if (maxScroll <= 0f) {
            return
        }

        val trackX = baseX + baseWidth - 10f
        val trackY = baseY + 10f
        val trackWidth = 4f
        val trackHeight = Math.max(0f, baseHeight - 20f)

        drawRoundedRect(trackX, trackY, trackWidth, trackHeight, 2f, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 130))

        val visibleRatio = Math.min(1f, baseHeight / Math.max(contentHeight, 1f))
        val handleHeight = Math.max(minHandleHeight, trackHeight * visibleRatio)
        val scrollOffset = -scrollValue
        val handleY = trackY + (trackHeight - handleHeight) * (scrollOffset / maxScroll)

        drawGradientRoundedRect(trackX - 1f, handleY, trackWidth + 2f, handleHeight, 3f, ColorUtils.applyAlpha(accent.color1, 190), ColorUtils.applyAlpha(accent.color2, 190))
    }

    fun drawContainer(x: Float, y: Float, width: Float, height: Float, radius: Float, palette: ColorPalette) {
        drawShadow(x, y, width, height, radius, 7)
        drawRoundedRect(x, y, width, height, radius, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 210))
        drawRoundedRect(x + 1f, y + 1f, width - 2f, height - 2f, radius - 1f, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 230))
    }

    fun getColor(color: Color?): NVGColor {
        var safeColor = color ?: Color.RED
        colorCache[safeColor.rgb]?.let { return it }

        val nvgColor = NVGColor.create()
        NanoVG.nvgRGBA(safeColor.red.toByte(), safeColor.green.toByte(), safeColor.blue.toByte(), safeColor.alpha.toByte(), nvgColor)
        colorCache[safeColor.rgb] = nvgColor
        return nvgColor
    }

    fun getContext(): Long {
        return nvg
    }

    fun getColorByCode(code: Char): Color {
        return when (Character.toLowerCase(code)) {
            '0' -> Color(0, 0, 0)
            '1' -> Color(0, 0, 170)
            '2' -> Color(0, 170, 0)
            '3' -> Color(0, 170, 170)
            '4' -> Color(170, 0, 0)
            '5' -> Color(170, 0, 170)
            '6' -> Color(255, 170, 0)
            '7' -> Color(170, 170, 170)
            '8' -> Color(85, 85, 85)
            '9' -> Color(85, 85, 255)
            'a' -> Color(85, 255, 85)
            'b' -> Color(85, 255, 255)
            'c' -> Color(255, 85, 85)
            'd' -> Color(255, 85, 255)
            'e' -> Color(255, 255, 85)
            'f' -> Color(255, 255, 255)
            else -> Color.WHITE
        }
    }

    fun getFontWithStyle(base: Font, bold: Boolean, italic: Boolean): Font {
        return if (bold && italic) {
            Fonts.SEMIBOLD
        } else if (bold) {
            Fonts.MEDIUM
        } else if (italic) {
            Fonts.REGULAR
        } else {
            base
        }
    }
}

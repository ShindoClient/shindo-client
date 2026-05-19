@file:JvmName("NanoVGExtensions")
@file:Suppress("UNUSED")

package me.miki.extensions.ui.nanovg

import me.miki.shindo.management.color.AccentColor
import me.miki.shindo.management.color.palette.ColorPalette
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Font
import me.miki.shindo.types.Rect
import me.miki.shindo.types.Size
import net.minecraft.util.ResourceLocation
import java.awt.Color
import java.io.File

fun NanoVGManager.getColor(
    r: Number,
    g: Number,
    b: Number,
    a: Number,
) = this.getColor(r.toFloat(), g.toFloat(), b.toFloat(), a.toFloat())

fun NanoVGManager.getColor(color: Number) = this.getColor(color.toInt())

fun NanoVGManager.imageSize(
    imageId: Number,
    src: Size,
) = this.imageSize(imageId.toInt(), src)

fun NanoVGManager.drawAlphaBar(
    x: Number,
    y: Number,
    width: Number,
    height: Number,
    radius: Number,
    color: Color,
) = this.drawAlphaBar(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat(), radius.toFloat(), color)

fun NanoVGManager.drawAlphaBar(
    x: Number,
    y: Number,
    width: Number,
    height: Number,
    radius: Number,
    color: Number,
) = this.drawAlphaBar(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat(), radius.toFloat(), color.toInt())

fun NanoVGManager.drawHSBBox(
    x: Number,
    y: Number,
    width: Number,
    height: Number,
    radius: Number,
    color: Color,
) = this.drawHSBBox(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat(), radius.toFloat(), color)

fun NanoVGManager.drawHSBBox(
    x: Number,
    y: Number,
    width: Number,
    height: Number,
    radius: Number,
    color: Number,
) = this.drawHSBBox(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat(), radius.toFloat(), color.toInt())

fun NanoVGManager.drawRect(
    x: Number,
    y: Number,
    width: Number,
    height: Number,
    color: Color,
) = this.drawRect(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat(), color)

fun NanoVGManager.drawRect(
    x: Number,
    y: Number,
    width: Number,
    height: Number,
    color: Number,
) = this.drawRect(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat(), color.toInt())

fun NanoVGManager.drawRoundedRect(
    x: Number,
    y: Number,
    width: Number,
    height: Number,
    radius: Number,
    color: Color,
) = this.drawRoundedRect(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat(), radius.toFloat(), color)

fun NanoVGManager.drawRoundedRect(
    x: Number,
    y: Number,
    width: Number,
    height: Number,
    radius: Number,
    color: Number,
) = this.drawRoundedRect(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat(), radius.toFloat(), color.toInt())

fun NanoVGManager.drawRoundedRect(
    rect: Rect,
    radius: Number,
    color: Number,
) = this.drawRoundedRect(rect, radius.toFloat(), color.toInt())

fun NanoVGManager.drawRoundedRect(
    rect: Rect,
    radius: Number,
    color: Color,
) = this.drawRoundedRect(rect, radius.toFloat(), color)

fun NanoVGManager.drawRoundedRectVarying(
    x: Number,
    y: Number,
    width: Number,
    height: Number,
    topLeftRadius: Number,
    topRightRadius: Number,
    bottomLeftRadius: Number,
    bottomRightRadius: Number,
    color: Color,
) = this.drawRoundedRectVarying(
    x.toFloat(),
    y.toFloat(),
    width.toFloat(),
    height.toFloat(),
    topLeftRadius.toFloat(),
    topRightRadius.toFloat(),
    bottomLeftRadius.toFloat(),
    bottomRightRadius.toFloat(),
    color,
)

fun NanoVGManager.drawRoundedRectVarying(
    x: Number,
    y: Number,
    width: Number,
    height: Number,
    topLeftRadius: Number,
    topRightRadius: Number,
    bottomLeftRadius: Number,
    bottomRightRadius: Number,
    color: Number,
) = this.drawRoundedRectVarying(
    x.toFloat(),
    y.toFloat(),
    width.toFloat(),
    height.toFloat(),
    topLeftRadius.toFloat(),
    topRightRadius.toFloat(),
    bottomLeftRadius.toFloat(),
    bottomRightRadius.toFloat(),
    color.toInt(),
)

fun NanoVGManager.drawVerticalGradientRect(
    x: Number,
    y: Number,
    width: Number,
    height: Number,
    color1: Color,
    color2: Color,
) = this.drawVerticalGradientRect(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat(), color1, color2)

fun NanoVGManager.drawVerticalGradientRect(
    x: Number,
    y: Number,
    width: Number,
    height: Number,
    color1: Number,
    color2: Number,
) = this.drawVerticalGradientRect(
    x.toFloat(),
    y.toFloat(),
    width.toFloat(),
    height.toFloat(),
    color1.toInt(),
    color2.toInt(),
)

fun NanoVGManager.drawHorizontalGradientRect(
    x: Number,
    y: Number,
    width: Number,
    height: Number,
    color1: Color,
    color2: Color,
) = this.drawHorizontalGradientRect(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat(), color1, color2)

fun NanoVGManager.drawHorizontalGradientRect(
    x: Number,
    y: Number,
    width: Number,
    height: Number,
    color1: Number,
    color2: Number,
) = this.drawHorizontalGradientRect(
    x.toFloat(),
    y.toFloat(),
    width.toFloat(),
    height.toFloat(),
    color1.toInt(),
    color2.toInt(),
)

fun NanoVGManager.drawGradientRect(
    x: Number,
    y: Number,
    width: Number,
    height: Number,
    color1: Color,
    color2: Color,
) = this.drawGradientRect(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat(), color1, color2)

fun NanoVGManager.drawGradientRect(
    x: Number,
    y: Number,
    width: Number,
    height: Number,
    color1: Number,
    color2: Number,
) = this.drawGradientRect(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat(), color1.toInt(), color2.toInt())

fun NanoVGManager.drawGradientRoundedRect(
    x: Number,
    y: Number,
    width: Number,
    height: Number,
    radius: Number,
    color1: Color,
    color2: Color,
) = this.drawGradientRoundedRect(
    x.toFloat(),
    y.toFloat(),
    width.toFloat(),
    height.toFloat(),
    radius.toFloat(),
    color1,
    color2,
)

fun NanoVGManager.drawGradientRoundedRect(
    x: Number,
    y: Number,
    width: Number,
    height: Number,
    radius: Number,
    color1: Number,
    color2: Number,
) = this.drawGradientRoundedRect(
    x.toFloat(),
    y.toFloat(),
    width.toFloat(),
    height.toFloat(),
    radius.toFloat(),
    color1.toInt(),
    color2.toInt(),
)

fun NanoVGManager.drawOutlineRoundedRect(
    x: Number,
    y: Number,
    width: Number,
    height: Number,
    radius: Number,
    strokeWidth: Number,
    color: Color,
) = this.drawOutlineRoundedRect(
    x.toFloat(),
    y.toFloat(),
    width.toFloat(),
    height.toFloat(),
    radius.toFloat(),
    strokeWidth.toFloat(),
    color,
)

fun NanoVGManager.drawOutlineRoundedRect(
    x: Number,
    y: Number,
    width: Number,
    height: Number,
    radius: Number,
    strokeWidth: Number,
    color: Number,
) = this.drawOutlineRoundedRect(
    x.toFloat(),
    y.toFloat(),
    width.toFloat(),
    height.toFloat(),
    radius.toFloat(),
    strokeWidth.toFloat(),
    color.toInt(),
)

fun NanoVGManager.drawGradientOutlineRoundedRect(
    x: Number,
    y: Number,
    width: Number,
    height: Number,
    radius: Number,
    strokeWidth: Number,
    color1: Color,
    color2: Color,
) = this.drawGradientOutlineRoundedRect(
    x.toFloat(),
    y.toFloat(),
    width.toFloat(),
    height.toFloat(),
    radius.toFloat(),
    strokeWidth.toFloat(),
    color1,
    color2,
)

fun NanoVGManager.drawGradientOutlineRoundedRect(
    x: Number,
    y: Number,
    width: Number,
    height: Number,
    radius: Number,
    strokeWidth: Number,
    color1: Number,
    color2: Number,
) = this.drawGradientOutlineRoundedRect(
    x.toFloat(),
    y.toFloat(),
    width.toFloat(),
    height.toFloat(),
    radius.toFloat(),
    strokeWidth.toFloat(),
    color1.toInt(),
    color2.toInt(),
)

fun NanoVGManager.drawArrow(
    x: Number,
    y: Number,
    size: Number,
    angle: Number,
    color: Color,
) = this.drawArrow(x.toFloat(), y.toFloat(), size.toFloat(), angle.toFloat(), color)

fun NanoVGManager.drawArrow(
    x: Number,
    y: Number,
    size: Number,
    angle: Number,
    color: Number,
) = this.drawArrow(x.toFloat(), y.toFloat(), size.toFloat(), angle.toFloat(), color.toInt())

fun NanoVGManager.drawShadow(
    x: Number,
    y: Number,
    width: Number,
    height: Number,
    radius: Number,
    strength: Number,
) = this.drawShadow(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat(), radius.toFloat(), strength.toInt())

fun NanoVGManager.drawShadow(
    x: Number,
    y: Number,
    width: Number,
    height: Number,
    radius: Number,
) = this.drawShadow(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat(), radius.toFloat())

fun NanoVGManager.drawGradientShadow(
    x: Number,
    y: Number,
    width: Number,
    height: Number,
    radius: Number,
    color1: Color,
    color2: Color,
) = this.drawGradientShadow(
    x.toFloat(),
    y.toFloat(),
    width.toFloat(),
    height.toFloat(),
    radius.toFloat(),
    color1,
    color2,
)

fun NanoVGManager.drawGradientShadow(
    x: Number,
    y: Number,
    width: Number,
    height: Number,
    radius: Number,
    color1: Number,
    color2: Number,
) = this.drawGradientShadow(
    x.toFloat(),
    y.toFloat(),
    width.toFloat(),
    height.toFloat(),
    radius.toFloat(),
    color1.toInt(),
    color2.toInt(),
)

fun NanoVGManager.drawRoundedGlow(
    x: Number,
    y: Number,
    width: Number,
    height: Number,
    radius: Number,
    color1: Color,
    strength: Number,
) = this.drawRoundedGlow(
    x.toFloat(),
    y.toFloat(),
    width.toFloat(),
    height.toFloat(),
    radius.toFloat(),
    color1,
    strength.toInt(),
)

fun NanoVGManager.drawRoundedGlow(
    x: Number,
    y: Number,
    width: Number,
    height: Number,
    radius: Number,
    color1: Number,
    strength: Number,
) = this.drawRoundedGlow(
    x.toFloat(),
    y.toFloat(),
    width.toFloat(),
    height.toFloat(),
    radius.toFloat(),
    color1.toInt(),
    strength.toInt(),
)

fun NanoVGManager.drawCircle(
    x: Number,
    y: Number,
    radius: Number,
    color: Color,
) = this.drawCircle(x.toFloat(), y.toFloat(), radius.toFloat(), color)

fun NanoVGManager.drawCircle(
    x: Number,
    y: Number,
    radius: Number,
    color: Number,
) = this.drawCircle(x.toFloat(), y.toFloat(), radius.toFloat(), color.toInt())

fun NanoVGManager.drawArc(
    x: Number,
    y: Number,
    radius: Number,
    startAngle: Number,
    endAngle: Number,
    strokeWidth: Number,
    color: Color,
) = this.drawArc(
    x.toFloat(),
    y.toFloat(),
    radius.toFloat(),
    startAngle.toFloat(),
    endAngle.toFloat(),
    strokeWidth.toFloat(),
    color,
)

fun NanoVGManager.drawArc(
    x: Number,
    y: Number,
    radius: Number,
    startAngle: Number,
    endAngle: Number,
    strokeWidth: Number,
    color: Number,
) = this.drawArc(
    x.toFloat(),
    y.toFloat(),
    radius.toFloat(),
    startAngle.toFloat(),
    endAngle.toFloat(),
    strokeWidth.toFloat(),
    color.toInt(),
)

fun NanoVGManager.drawGradientCircle(
    x: Number,
    y: Number,
    radius: Number,
    color1: Color,
    color2: Color,
) = this.drawGradientCircle(x.toFloat(), y.toFloat(), radius.toFloat(), color1, color2)

fun NanoVGManager.drawGradientCircle(
    x: Number,
    y: Number,
    radius: Number,
    color1: Number,
    color2: Number,
) = this.drawGradientCircle(x.toFloat(), y.toFloat(), radius.toFloat(), color1.toInt(), color2.toInt())

fun NanoVGManager.fontBlur(blur: Number) = this.fontBlur(blur.toFloat())

fun NanoVGManager.drawText(
    text: String,
    x: Number,
    y: Number,
    color: Color,
    size: Number,
    font: Font,
) = this.drawText(text, x.toFloat(), y.toFloat(), color, size.toFloat(), font)

fun NanoVGManager.drawText(
    text: String,
    x: Number,
    y: Number,
    color: Number,
    size: Number,
    font: Font,
) = this.drawText(text, x.toFloat(), y.toFloat(), color.toInt(), size.toFloat(), font)

fun NanoVGManager.drawBlurredText(
    text: String,
    x: Number,
    y: Number,
    color: Color,
    blurRadius: Number,
    size: Number,
    align: Number,
    font: Font,
) = this.drawBlurredText(
    text,
    x.toFloat(),
    y.toFloat(),
    color,
    blurRadius.toFloat(),
    size.toFloat(),
    align.toInt(),
    font,
)

fun NanoVGManager.drawBlurredText(
    text: String,
    x: Number,
    y: Number,
    color: Number,
    blurRadius: Number,
    size: Number,
    align: Number,
    font: Font,
) = this.drawBlurredText(
    text,
    x.toFloat(),
    y.toFloat(),
    color.toInt(),
    blurRadius.toFloat(),
    size.toFloat(),
    align.toInt(),
    font,
)

fun NanoVGManager.drawTextGlowing(
    text: String,
    x: Number,
    y: Number,
    color: Color,
    blurRadius: Number,
    size: Number,
    font: Font,
) = this.drawTextGlowing(text, x.toFloat(), y.toFloat(), color, blurRadius.toFloat(), size.toFloat(), font)

fun NanoVGManager.drawTextGlowing(
    text: String,
    x: Number,
    y: Number,
    color: Number,
    blurRadius: Number,
    size: Number,
    font: Font,
) = this.drawTextGlowing(text, x.toFloat(), y.toFloat(), color.toInt(), blurRadius.toFloat(), size.toFloat(), font)

fun NanoVGManager.drawCenteredTextGlowing(
    text: String,
    x: Number,
    y: Number,
    color: Color,
    blurRadius: Number,
    size: Number,
    font: Font,
) = this.drawCenteredTextGlowing(text, x.toFloat(), y.toFloat(), color, blurRadius.toFloat(), size.toFloat(), font)

fun NanoVGManager.drawCenteredTextGlowing(
    text: String,
    x: Number,
    y: Number,
    color: Number,
    blurRadius: Number,
    size: Number,
    font: Font,
) = this.drawCenteredTextGlowing(
    text,
    x.toFloat(),
    y.toFloat(),
    color.toInt(),
    blurRadius.toFloat(),
    size.toFloat(),
    font,
)

fun NanoVGManager.drawTextBox(
    text: String,
    x: Number,
    y: Number,
    maxWidth: Number,
    color: Color,
    size: Number,
    font: Font,
) = this.drawTextBox(text, x.toFloat(), y.toFloat(), maxWidth.toFloat(), color, size.toFloat(), font)

fun NanoVGManager.drawTextBox(
    text: String,
    x: Number,
    y: Number,
    maxWidth: Number,
    color: Number,
    size: Number,
    font: Font,
) = this.drawTextBox(text, x.toFloat(), y.toFloat(), maxWidth.toFloat(), color.toInt(), size.toFloat(), font)

fun NanoVGManager.drawCenteredText(
    text: String,
    x: Number,
    y: Number,
    color: Color,
    size: Number,
    font: Font,
) = this.drawCenteredText(text, x.toFloat(), y.toFloat(), color, size.toFloat(), font)

fun NanoVGManager.drawCenteredText(
    text: String,
    x: Number,
    y: Number,
    color: Number,
    size: Number,
    font: Font,
) = this.drawCenteredText(text, x.toFloat(), y.toFloat(), color.toInt(), size.toFloat(), font)

fun NanoVGManager.getTextWidth(
    text: String,
    size: Number,
    font: Font,
) = this.getTextWidth(text, size.toFloat(), font)

fun NanoVGManager.getTextHeight(
    text: String,
    size: Number,
    font: Font,
) = this.getTextHeight(text, size.toFloat(), font)

fun NanoVGManager.getTextBoxHeight(
    text: String,
    size: Number,
    font: Font,
    maxWidth: Number,
) = this.getTextBoxHeight(text, size.toFloat(), font, maxWidth.toFloat())

fun NanoVGManager.getLimitText(
    inputText: String,
    fontSize: Number,
    font: Font?,
    width: Number,
) = this.getLimitText(inputText, fontSize.toFloat(), font, width.toFloat())

fun NanoVGManager.scale(
    x: Number,
    y: Number,
    scaleX: Number,
    scaleY: Number,
) = this.scale(x.toFloat(), y.toFloat(), scaleX.toFloat(), scaleY.toFloat())

fun NanoVGManager.scale(
    x: Number,
    y: Number,
    scale: Number,
) = this.scale(x.toFloat(), y.toFloat(), scale.toFloat())

fun NanoVGManager.scale(
    x: Number,
    y: Number,
    width: Number,
    height: Number,
    scale: Number,
) = this.scale(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat(), scale.toFloat())

fun NanoVGManager.rotate(
    x: Number,
    y: Number,
    width: Number,
    height: Number,
    angle: Number,
) = this.rotate(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat(), angle.toFloat())

fun NanoVGManager.translate(
    x: Number,
    y: Number,
) = this.translate(x.toFloat(), y.toFloat())

fun NanoVGManager.setAlpha(alpha: Number) = this.setAlpha(alpha.toFloat())

fun NanoVGManager.scissor(
    x: Number,
    y: Number,
    width: Number,
    height: Number,
) = this.scissor(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat())

fun NanoVGManager.intersectScissor(
    x: Number,
    y: Number,
    width: Number,
    height: Number,
) = this.intersectScissor(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat())

fun NanoVGManager.rotateAt(
    x: Number,
    y: Number,
    angleRadians: Number,
) = this.rotateAt(x.toFloat(), y.toFloat(), angleRadians.toFloat())

fun NanoVGManager.rotateDegrees(
    x: Number,
    y: Number,
    width: Number,
    height: Number,
    angleDegrees: Number,
) = this.rotateDegrees(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat(), angleDegrees.toFloat())

fun NanoVGManager.rotateDegreesAt(
    x: Number,
    y: Number,
    angleDegrees: Number,
) = this.rotateDegreesAt(x.toFloat(), y.toFloat(), angleDegrees.toFloat())

fun NanoVGManager.drawSvg(
    location: ResourceLocation,
    x: Number,
    y: Number,
    width: Number,
    height: Number,
    color: Color,
) = this.drawSvg(location, x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat(), color)

fun NanoVGManager.drawSvg(
    location: ResourceLocation,
    x: Number,
    y: Number,
    width: Number,
    height: Number,
    color: Number,
) = this.drawSvg(location, x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat(), color.toInt())

fun NanoVGManager.drawImage(
    location: ResourceLocation,
    x: Number,
    y: Number,
    width: Number,
    height: Number,
) = this.drawImage(location, x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat())

fun NanoVGManager.drawImage(
    location: ResourceLocation,
    x: Number,
    y: Number,
    width: Number,
    height: Number,
    alpha: Number,
) = this.drawImage(location, x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat(), alpha.toInt())

fun NanoVGManager.drawImage(
    file: File,
    x: Number,
    y: Number,
    width: Number,
    height: Number,
) = this.drawImage(file, x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat())

fun NanoVGManager.drawImage(
    texture: Number,
    x: Number,
    y: Number,
    width: Number,
    height: Number,
    alpha: Number,
    flags: Number,
) = this.drawImage(
    texture.toInt(),
    x.toFloat(),
    y.toFloat(),
    width.toFloat(),
    height.toFloat(),
    alpha.toFloat(),
    flags.toInt(),
)

fun NanoVGManager.drawImage(
    texture: Number,
    x: Number,
    y: Number,
    width: Number,
    height: Number,
    alpha: Number,
) = this.drawImage(texture.toInt(), x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat(), alpha.toFloat())

fun NanoVGManager.drawImage(
    texture: Number,
    x: Number,
    y: Number,
    width: Number,
    height: Number,
) = this.drawImage(texture.toInt(), x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat())

fun NanoVGManager.drawRoundedImage(
    texture: Number,
    x: Number,
    y: Number,
    width: Number,
    height: Number,
    radius: Number,
    alpha: Number,
) = this.drawRoundedImage(
    texture.toInt(),
    x.toFloat(),
    y.toFloat(),
    width.toFloat(),
    height.toFloat(),
    radius.toFloat(),
    alpha.toFloat(),
)

fun NanoVGManager.drawRoundedImage(
    texture: Number,
    x: Number,
    y: Number,
    width: Number,
    height: Number,
    radius: Number,
) = this.drawRoundedImage(
    texture.toInt(),
    x.toFloat(),
    y.toFloat(),
    width.toFloat(),
    height.toFloat(),
    radius.toFloat(),
)

fun NanoVGManager.drawPlayerHead(
    location: ResourceLocation,
    x: Number,
    y: Number,
    width: Number,
    height: Number,
    radius: Number,
    alpha: Number,
) = this.drawPlayerHead(
    location,
    x.toFloat(),
    y.toFloat(),
    width.toFloat(),
    height.toFloat(),
    radius.toFloat(),
    alpha.toFloat(),
)

fun NanoVGManager.drawPlayerHead(
    location: ResourceLocation,
    x: Number,
    y: Number,
    width: Number,
    height: Number,
    radius: Number,
) = this.drawPlayerHead(location, x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat(), radius.toFloat())

fun NanoVGManager.drawRoundedImage(
    location: ResourceLocation,
    x: Number,
    y: Number,
    width: Number,
    height: Number,
    radius: Number,
    alpha: Number,
) = this.drawRoundedImage(
    location,
    x.toFloat(),
    y.toFloat(),
    width.toFloat(),
    height.toFloat(),
    radius.toFloat(),
    alpha.toFloat(),
)

fun NanoVGManager.drawRoundedImage(
    location: ResourceLocation,
    x: Number,
    y: Number,
    width: Number,
    height: Number,
    radius: Number,
) = this.drawRoundedImage(location, x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat(), radius.toFloat())

fun NanoVGManager.drawRoundedImage(
    file: File,
    x: Number,
    y: Number,
    width: Number,
    height: Number,
    radius: Number,
    alpha: Number,
) = this.drawRoundedImage(
    file,
    x.toFloat(),
    y.toFloat(),
    width.toFloat(),
    height.toFloat(),
    radius.toFloat(),
    alpha.toFloat(),
)

fun NanoVGManager.drawRoundedImage(
    file: File,
    x: Number,
    y: Number,
    width: Number,
    height: Number,
    radius: Number,
) = this.drawRoundedImage(file, x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat(), radius.toFloat())

fun NanoVGManager.drawScrollbar(
    baseX: Number,
    baseY: Number,
    baseWidth: Number,
    baseHeight: Number,
    contentHeight: Number,
    scrollValue: Number,
    palette: ColorPalette,
    accent: AccentColor,
    minHandleHeight: Number,
) = this.drawScrollbar(
    baseX.toFloat(),
    baseY.toFloat(),
    baseWidth.toFloat(),
    baseHeight.toFloat(),
    contentHeight.toFloat(),
    scrollValue.toFloat(),
    palette,
    accent,
    minHandleHeight.toFloat(),
)

fun NanoVGManager.drawDivider(
    x: Number,
    y: Number,
    width: Number,
    height: Number,
    radius: Number,
    alpha: Number,
) = this.drawDivider(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat(), radius.toFloat(), alpha.toFloat())

fun NanoVGManager.drawLine(
    x1: Number,
    y1: Number,
    x2: Number,
    y2: Number,
    strokeWidth: Number,
    color: Color,
) = this.drawLine(x1.toFloat(), y1.toFloat(), x2.toFloat(), y2.toFloat(), strokeWidth.toFloat(), color)

fun NanoVGManager.drawGradientLine(
    x1: Number,
    y1: Number,
    x2: Number,
    y2: Number,
    strokeWidth: Number,
    color1: Color,
    color2: Color,
) = this.drawGradientLine(x1.toFloat(), y1.toFloat(), x2.toFloat(), y2.toFloat(), strokeWidth.toFloat(), color1, color2)

fun NanoVGManager.drawPolygon(
    centerX: Number,
    centerY: Number,
    radius: Number,
    sides: Number,
    rotation: Number,
    color: Color,
) = this.drawPolygon(centerX.toFloat(), centerY.toFloat(), radius.toFloat(), sides.toInt(), rotation.toFloat(), color)

fun NanoVGManager.drawPolygonOutline(
    centerX: Number,
    centerY: Number,
    radius: Number,
    sides: Number,
    rotation: Number,
    strokeWidth: Number,
    color: Color,
) = this.drawPolygonOutline(
    centerX.toFloat(),
    centerY.toFloat(),
    radius.toFloat(),
    sides.toInt(),
    rotation.toFloat(),
    strokeWidth.toFloat(),
    color,
)

fun NanoVGManager.drawRoundedRectSelective(
    x: Number,
    y: Number,
    width: Number,
    height: Number,
    radius: Number,
    corners: Number,
    color: Color,
) = this.drawRoundedRectSelective(
    x.toFloat(),
    y.toFloat(),
    width.toFloat(),
    height.toFloat(),
    radius.toFloat(),
    corners.toInt(),
    color,
)

fun NanoVGManager.drawInsetBorder(
    x: Number,
    y: Number,
    width: Number,
    height: Number,
    radius: Number,
    borderWidth: Number,
    color: Color,
) = this.drawInsetBorder(
    x.toFloat(),
    y.toFloat(),
    width.toFloat(),
    height.toFloat(),
    radius.toFloat(),
    borderWidth.toFloat(),
    color,
)

fun NanoVGManager.drawGlowRect(
    x: Number,
    y: Number,
    width: Number,
    height: Number,
    radius: Number,
    color: Color,
    strength: Number,
) = this.drawGlowRect(
    x.toFloat(),
    y.toFloat(),
    width.toFloat(),
    height.toFloat(),
    radius.toFloat(),
    color,
    strength.toInt(),
)

fun NanoVGManager.drawMultilineText(
    text: String,
    x: Number,
    y: Number,
    maxWidth: Number,
    lineHeight: Number,
    color: Color,
    size: Number,
    font: Font,
) = this.drawMultilineText(
    text,
    x.toFloat(),
    y.toFloat(),
    maxWidth.toFloat(),
    lineHeight.toFloat(),
    color,
    size.toFloat(),
    font,
)

fun NanoVGManager.drawCenteredIcon(
    icon: String,
    x: Number,
    y: Number,
    size: Number,
    color: Color,
) = this.drawCenteredIcon(icon, x.toFloat(), y.toFloat(), size.toFloat(), color)

fun NanoVGManager.drawGlassButton(
    text: String,
    x: Number,
    y: Number,
    w: Number,
    h: Number,
    hover: Float,
    anim: Float,
    red: Boolean,
) = this.drawGlassButton(text, x.toFloat(), y.toFloat(), w.toFloat(), h.toFloat(), hover, anim, red)

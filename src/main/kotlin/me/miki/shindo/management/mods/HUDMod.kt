package me.miki.shindo.management.mods

import me.miki.shindo.Shindo
import me.miki.shindo.gui.GuiEditHUD
import me.miki.shindo.management.color.AccentColor
import me.miki.shindo.management.color.ColorManager
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.impl.InternalSettingsMod
import me.miki.shindo.management.nanovg.font.Font
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.utils.ColorUtils
import net.minecraft.util.ResourceLocation
import java.awt.Color
import java.io.File

@Suppress("UNUSED")
open class HUDMod : Mod {
    private var x = 100
    private var y = 100
    private var draggingX = 0
    private var draggingY = 0
    private var width = 100
    private var height = 100
    private var scale = 1.0f
    private var dragging = false
    private var draggable = true

    constructor(nameTranslate: TranslateText, descriptionText: TranslateText, icon: String) : super(
        nameTranslate,
        descriptionText,
        ModCategory.HUD,
        icon,
    )

    constructor(nameTranslate: TranslateText, descriptionText: TranslateText, icon: String, alias: String) : super(
        nameTranslate,
        descriptionText,
        ModCategory.HUD,
        icon,
        alias,
    )

    constructor(
        nameTranslate: TranslateText,
        descriptionText: TranslateText,
        icon: String,
        alias: String,
        restricted: Boolean,
    ) : super(
        nameTranslate,
        descriptionText,
        ModCategory.HUD,
        icon,
        alias,
        restricted,
    )

    fun getX(): Int = x

    fun setX(x: Int) {
        this.x = x
    }

    fun getY(): Int = y

    fun setY(y: Int) {
        this.y = y
    }

    fun getDraggingX(): Int = draggingX

    fun setDraggingX(draggingX: Int) {
        this.draggingX = draggingX
    }

    fun getDraggingY(): Int = draggingY

    fun setDraggingY(draggingY: Int) {
        this.draggingY = draggingY
    }

    fun setWidth(width: Int) {
        this.width = width
    }

    fun setHeight(height: Int) {
        this.height = height
    }

    fun getScale(): Float = scale

    fun isDragging(): Boolean = dragging

    fun setDragging(dragging: Boolean) {
        this.dragging = dragging
    }

    fun isDraggable(): Boolean = draggable

    fun setDraggable(draggable: Boolean) {
        this.draggable = draggable
    }

    fun save() {
        val nvg = Shindo.getInstance().nanoVGManager
        nvg.save()
    }

    fun restore() {
        val nvg = Shindo.getInstance().nanoVGManager
        nvg.restore()
    }

    fun scissor(
        addX: Float,
        addY: Float,
        width: Float,
        height: Float,
    ) {
        val nvg = Shindo.getInstance().nanoVGManager
        nvg.scissor(
            x + (addX * scale),
            y + (addY * scale),
            width * scale,
            height * scale,
        )
    }

    fun drawPlayerHead(
        location: ResourceLocation,
        addX: Float,
        addY: Float,
        width: Float,
        height: Float,
        radius: Float,
    ) {
        val nvg = Shindo.getInstance().nanoVGManager
        nvg.drawPlayerHead(
            location,
            x + (addX * scale),
            y + (addY * scale),
            width * scale,
            height * scale,
            radius * scale,
        )
    }

    fun drawImage(
        location: ResourceLocation,
        addX: Float,
        addY: Float,
        width: Float,
        height: Float,
    ) {
        val nvg = Shindo.getInstance().nanoVGManager
        nvg.drawImage(location, addX, addY, width, height)
    }

    fun drawRoundedImage(
        texture: Int,
        addX: Float,
        addY: Float,
        width: Float,
        height: Float,
        radius: Float,
    ) {
        val nvg = Shindo.getInstance().nanoVGManager
        nvg.drawRoundedImage(
            texture,
            x + (addX * scale),
            y + (addY * scale),
            width * scale,
            height * scale,
            radius * scale,
        )
    }

    fun drawRoundedImage(
        file: File,
        addX: Float,
        addY: Float,
        width: Float,
        height: Float,
        radius: Float,
        alpha: Float,
    ) {
        val nvg = Shindo.getInstance().nanoVGManager
        nvg.drawRoundedImage(
            file,
            x + (addX * scale),
            y + (addY * scale),
            width * scale,
            height * scale,
            radius * scale,
            alpha,
        )
    }

    fun drawRoundedImage(
        file: File,
        addX: Float,
        addY: Float,
        width: Float,
        height: Float,
        radius: Float,
    ) {
        drawRoundedImage(file, addX, addY, width, height, radius, 1.0f)
    }

    fun drawRoundedImage(
        location: ResourceLocation,
        addX: Float,
        addY: Float,
        width: Float,
        height: Float,
        radius: Float,
    ) {
        val nvg = Shindo.getInstance().nanoVGManager
        nvg.drawRoundedImage(
            location,
            x + (addX * scale),
            y + (addY * scale),
            width * scale,
            height * scale,
            radius * scale,
        )
    }

    fun drawArc(
        addX: Float,
        addY: Float,
        radius: Float,
        startAngle: Float,
        endAngle: Float,
        strokeWidth: Float,
        color: Color,
    ) {
        val nvg = Shindo.getInstance().nanoVGManager
        nvg.drawArc(
            x + (addX * scale),
            y + (addY * scale),
            radius * scale,
            startAngle,
            endAngle,
            strokeWidth * scale,
            color,
        )
    }

    fun drawArc(
        addX: Float,
        addY: Float,
        radius: Float,
        startAngle: Float,
        endAngle: Float,
        strokeWidth: Float,
    ) {
        drawArc(addX, addY, radius, startAngle, endAngle, strokeWidth, getFontColor())
    }

    fun drawShadow(
        addX: Float,
        addY: Float,
        width: Float,
        height: Float,
        radius: Float,
    ) {
        val nvg = Shindo.getInstance().nanoVGManager
        nvg.drawShadow(
            x + (addX * scale),
            y + (addY * scale),
            width * scale,
            height * scale,
            radius * scale,
        )
    }

    fun drawRect(
        addX: Float,
        addY: Float,
        width: Float,
        height: Float,
        color: Color,
    ) {
        val nvg = Shindo.getInstance().nanoVGManager
        nvg.drawRect(
            x + (addX * scale),
            y + (addY * scale),
            width * scale,
            height * scale,
            color,
        )
    }

    fun drawRect(
        addX: Float,
        addY: Float,
        width: Float,
        height: Float,
    ) {
        drawRect(addX, addY, width, height, getFontColor())
    }

    fun drawRoundedRect(
        addX: Float,
        addY: Float,
        width: Float,
        height: Float,
        radius: Float,
        color: Color,
    ) {
        val nvg = Shindo.getInstance().nanoVGManager
        if (width < 0 || height < 0) {
            return
        }
        nvg.drawRoundedRect(
            x + (addX * scale),
            y + (addY * scale),
            width * scale,
            height * scale,
            radius * scale,
            color,
        )
    }

    fun drawRoundedRect(
        addX: Float,
        addY: Float,
        width: Float,
        height: Float,
        radius: Float,
    ) {
        drawRoundedRect(addX, addY, width, height, radius, getFontColor())
    }

    fun drawBackground(
        addX: Float,
        addY: Float,
        width: Float,
        height: Float,
        radius: Float,
    ) {
        val instance = Shindo.getInstance()
        val nvg = instance.nanoVGManager
        val colorManager: ColorManager = instance.getColorManager()
        val currentColor: AccentColor = colorManager.getCurrentColor()
        val theme = InternalSettingsMod.instance.hudTheme

        val isNormal = theme == InternalSettingsMod.HudTheme.NORMAL
        val isVanilla = theme == InternalSettingsMod.HudTheme.VANILLA
        val isGlow = theme == InternalSettingsMod.HudTheme.GLOW
        val isVanillaGlow = theme == InternalSettingsMod.HudTheme.VANILLA_GLOW
        val isOutline = theme == InternalSettingsMod.HudTheme.OUTLINE
        val isOutlineGlow = theme == InternalSettingsMod.HudTheme.OUTLINE_GLOW
        val isShadow = theme == InternalSettingsMod.HudTheme.SHADOW
        val isDark = theme == InternalSettingsMod.HudTheme.DARK
        val isLight = theme == InternalSettingsMod.HudTheme.LIGHT
        val isRect = theme == InternalSettingsMod.HudTheme.RECT
        val isModern = theme == InternalSettingsMod.HudTheme.MODERN
        val isSimpGrad = theme == InternalSettingsMod.HudTheme.GRADIENT_SIMPLE
        val isBlur = InternalSettingsMod.instance.getBlurSetting()?.isToggled() == true

        val lastWidth = width * scale
        val lastHeight = height * scale
        val x = this.x + (addX * scale)
        val y = this.y + (addY * scale)

        if (isNormal || isVanilla || isShadow || isDark || isLight || isModern) {
            nvg.drawShadow(x, y, lastWidth, lastHeight, radius - 0.75f)
        } else if (isGlow || isVanillaGlow) {
            nvg.drawGradientShadow(
                x,
                y,
                lastWidth,
                lastHeight,
                radius,
                currentColor.getColor1(),
                currentColor.getColor2(),
            )
        } else if (isOutline || isOutlineGlow) {
            if (isOutline) {
                nvg.drawShadow(x - 2, y - 2, lastWidth + 4, lastHeight + 4, radius + 2)
            } else {
                nvg.drawGradientShadow(
                    x - 2,
                    y - 2,
                    lastWidth + 4,
                    lastHeight + 4,
                    radius + 2,
                    currentColor.getColor1(),
                    currentColor.getColor2(),
                )
            }
        }

        if (isOutline || isOutlineGlow) {
            nvg.drawGradientOutlineRoundedRect(
                x - 1,
                y - 1,
                lastWidth + 2,
                lastHeight + 2,
                radius + 1,
                1.5f,
                currentColor.getColor1(),
                currentColor.getColor2(),
            )
        }

        if (isVanilla || isVanillaGlow || isOutline || isOutlineGlow) {
            nvg.drawRoundedRect(x, y, lastWidth, lastHeight, radius, Color(0, 0, 0, 100))
        } else if (isNormal || isGlow) {
            nvg.drawGradientRoundedRect(
                x,
                y,
                lastWidth,
                lastHeight,
                radius,
                ColorUtils.applyAlpha(currentColor.getColor1(), 220),
                ColorUtils.applyAlpha(currentColor.getColor2(), 220),
            )
        } else if (isLight) {
            nvg.drawRoundedRect(x, y, lastWidth, lastHeight, radius, Color(240, 240, 240, 220))
        } else if (isDark) {
            nvg.drawRoundedRect(x, y, lastWidth, lastHeight, radius, Color(20, 20, 20, 220))
        }
        if (isRect || isSimpGrad) {
            nvg.drawRect(x, y, lastWidth, lastHeight, Color(20, 20, 20, 165))
        }
        if (isSimpGrad) {
            nvg.drawHorizontalGradientRect(
                x,
                y - (2 * scale),
                lastWidth,
                (2 * scale),
                ColorUtils.interpolateColors(8, 0, currentColor.getColor1(), currentColor.getColor2()),
                ColorUtils.interpolateColors(10, 20, currentColor.getColor1(), currentColor.getColor2()),
            )
        }
        if (isModern) {
            nvg.drawRoundedRect(x, y, lastWidth, lastHeight, radius, Color(0, 0, 0, 110))
            nvg.drawOutlineRoundedRect(x, y, lastWidth, lastHeight, radius, .5f, Color(255, 255, 255, 80))
        }
    }

    fun drawBackground(
        width: Float,
        height: Float,
    ) {
        drawBackground(0f, 0f, width, height, 6 * scale)
    }

    fun drawBackground(
        addX: Float,
        addY: Float,
        width: Float,
        height: Float,
    ) {
        drawBackground(addX, addY, width, height, 6 * scale)
    }

    fun drawBackground(
        width: Float,
        height: Float,
        radius: Float,
    ) {
        drawBackground(0f, 0f, width, height, radius)
    }

    fun drawText(
        text: String,
        addX: Float,
        addY: Float,
        size: Float,
        font: Font,
        color: Color,
    ) {
        var localX = addX
        var localY = addY
        if (font == Fonts.MOJANGLES) {
            localX -= 0.5f
            localY -= 1.3f
        }

        val nvg = Shindo.getInstance().nanoVGManager
        val lastSize = size * scale
        val theme = InternalSettingsMod.instance.hudTheme
        val isText = theme == InternalSettingsMod.HudTheme.TEXT

        if (isText) {
            nvg.save()
            nvg.fontBlur(20F)
            nvg.drawText(text, x + (localX * scale), y + (localY * scale), Color(0, 0, 0, 150), lastSize, font)
            nvg.restore()
        }

        nvg.drawText(
            text,
            x + (localX * scale),
            y + (localY * scale),
            Color(color.red, color.green, color.blue, 180),
            lastSize,
            font,
        )
    }

    fun scale(
        addX: Float,
        addY: Float,
        width: Float,
        height: Float,
        nvgScale: Float,
    ) {
        val nvg = Shindo.getInstance().nanoVGManager
        nvg.scale(x + (addX * scale), y + (addY * scale), width * scale, height * scale, nvgScale)
    }

    fun drawText(
        text: String,
        addX: Float,
        addY: Float,
        size: Float,
        font: Font,
    ) {
        drawText(text, addX, addY, size, font, getFontColor())
    }

    fun drawCenteredText(
        text: String,
        addX: Float,
        addY: Float,
        size: Float,
        font: Font,
        color: Color,
    ) {
        var addY2: Float = addY
        if (font == Fonts.MOJANGLES) {
            addY2 = addY - 1F
        }

        val nvg = Shindo.getInstance().nanoVGManager
        val lastSize = size * scale
        nvg.drawCenteredText(text, x + (addX * scale), y + (addY2 * scale), color, lastSize, font)
    }

    fun drawCenteredText(
        text: String,
        addX: Float,
        addY: Float,
        size: Float,
        font: Font,
    ) {
        drawCenteredText(text, addX, addY, size, font, getFontColor())
    }

    fun getTextWidth(
        text: String,
        size: Float,
        font: Font,
    ): Float {
        val nvg = Shindo.getInstance().nanoVGManager
        return nvg.getTextWidth(text, size, font)
    }

    fun getLimitText(
        text: String,
        size: Float,
        font: Font,
        maxWidth: Float,
    ): String {
        val nvg = Shindo.getInstance().nanoVGManager
        return nvg.getLimitText(text, size, font, maxWidth)
    }

    fun getFontColor(alpha: Int): Color {
        val theme = InternalSettingsMod.instance.hudTheme
        val isDark = theme == InternalSettingsMod.HudTheme.DARK
        val isLight = theme == InternalSettingsMod.HudTheme.LIGHT

        if (isDark || isLight) {
            return Shindo
                .getInstance()
                .getColorManager()
                .getCurrentColor()
                .getInterpolateColor(alpha)
        }
        return Color(255, 255, 255, alpha)
    }

    fun getFontColor(): Color = getFontColor(255)

    fun isEditing(): Boolean = mc.currentScreen is GuiEditHUD

    fun getWidth(): Int = (width * scale).toInt()

    fun getHeight(): Int = (height * scale).toInt()

    fun setScale(scale: Float) {
        if (scale !in 0.2..5.0) {
            if (scale > 5.0) {
                this.scale = 5.0f
            }
            if (scale < 0.2) {
                this.scale = 0.2f
            }
            return
        }
        this.scale = scale
    }

    fun getHudFont(`in`: Int): Font {
        if (InternalSettingsMod.instance.mCHUDFont?.isToggled() == true) {
            return Fonts.MOJANGLES
        }
        return when (`in`) {
            1 -> Fonts.REGULAR
            2 -> Fonts.MEDIUM
            3 -> Fonts.SEMIBOLD
            else -> Fonts.REGULAR
        }
    }
}

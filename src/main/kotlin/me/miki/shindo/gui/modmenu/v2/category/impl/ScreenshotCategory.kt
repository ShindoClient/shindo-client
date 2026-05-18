package me.miki.shindo.gui.modmenu.v2.category.impl

import me.miki.extensions.ui.animation.setAnimation
import me.miki.extensions.ui.nanovg.drawRect
import me.miki.extensions.ui.nanovg.drawRoundedRect
import me.miki.shindo.Shindo
import me.miki.shindo.gui.modmenu.v2.GuiModMenu
import me.miki.shindo.gui.modmenu.v2.category.Category
import me.miki.shindo.logger.ShindoLogger
import me.miki.shindo.management.color.ColorManager
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.screenshot.Screenshot
import me.miki.shindo.management.screenshot.ScreenshotManager
import me.miki.shindo.ui.animation.v2.value.SimpleAnimation
import me.miki.shindo.utils.ColorUtils.applyAlpha
import me.miki.shindo.utils.mouse.MouseUtils
import org.lwjgl.input.Keyboard
import java.awt.Desktop
import java.io.IOException

class ScreenshotCategory(
    parent: GuiModMenu?,
) : Category(parent!!, TranslateText.SCREENSHOT, LegacyIcon.CAMERA, false, true) {
    // todo: add delete confirm dialog
    private var currentScreenshot: Screenshot? = null

    private val leftAnimation = SimpleAnimation()
    private val rightAnimation = SimpleAnimation()
    private val trashAnimation = SimpleAnimation()

    override fun initCategory() {
        scroll.resetAll()
    }

    override fun initGui() {
        val screenshotManager: ScreenshotManager = Shindo.getInstance().getScreenshotManager()

        if (currentScreenshot == null && !screenshotManager.getScreenshots().isEmpty()) {
            currentScreenshot = screenshotManager.getScreenshots()[0]
        }
    }

    override fun drawScreen(
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float,
    ) {
        val instance: Shindo = Shindo.getInstance()
        val nvg: NanoVGManager = instance.nanoVGManager!!
        val screenshotManager: ScreenshotManager = instance.getScreenshotManager()
        val colorManager: ColorManager = instance.getColorManager()
        val palette = colorManager.getPalette()
        val accentColor = colorManager.getCurrentColor()

        var addX = 42
        val addY = 12
        var offsetX = 0
        var index = 1

        screenshotManager.loadScreenshots()

        if (currentScreenshot == null && !screenshotManager.getScreenshots().isEmpty()) {
            currentScreenshot = screenshotManager.getScreenshots()[0]
        }

        leftAnimation.setAnimation(
            if (MouseUtils.isInside(
                    mouseX,
                    mouseY,
                    this.getX(),
                    this.getY(),
                    42,
                    this.getHeight(),
                )
            ) {
                1.0f
            } else {
                0.0f
            },
            16,
        )
        rightAnimation.setAnimation(
            if (MouseUtils.isInside(
                    mouseX,
                    mouseY,
                    this.getX() + this.getWidth() - 42,
                    this.getY(),
                    42,
                    this.getHeight(),
                )
            ) {
                1.0f
            } else {
                0.0f
            },
            16,
        )

        if (currentScreenshot != null) {
            trashAnimation.setAnimation(
                if (MouseUtils.isInside(
                        mouseX,
                        mouseY,
                        this.getX() + addX,
                        this.getY() + addY,
                        this.getWidth() - (addX * 2),
                        this.getHeight() - (addY * 2) - 38,
                    )
                ) {
                    1.0f
                } else {
                    0.0f
                },
                16,
            )

            nvg.drawRoundedImage(
                currentScreenshot!!.getImage(),
                this.getX() + addX.toFloat(),
                this.getY() + addY.toFloat(),
                this.getWidth() - (addX * 2f),
                this.getHeight() - (addY * 2) - 38f,
                6f,
            )
            nvg.drawText(
                LegacyIcon.TRASH,
                this.getX() + this.getWidth() - 59f,
                this.getY() + addY + 6f,
                palette.getMaterialRed((trashAnimation.getValue() * 255).toInt()),
                12f,
                Fonts.LEGACYICON,
            )

            addX = 58

            nvg.drawRoundedRect(
                this.getX() + addX.toFloat(),
                this.getY() + this.getHeight() - 40f,
                this.getWidth() - (addX * 2f),
                30f,
                6f,
                palette.getBackgroundColor(ColorType.DARK),
            )

            nvg.save()
            nvg.translate(scroll.getValue(), 0f)

            for (s in screenshotManager.getScreenshots()) {
                val alpha = (s.getSelectAnimation().getValue() * 255).toInt()

                if (offsetX + scroll.getValue() + 30 > 0 && offsetX + scroll.getValue() < this.getWidth() - 100) {
                    nvg.drawShadow(
                        this.getX() + offsetX + 62f,
                        this.getY() + this.getHeight() - 36f,
                        50f,
                        23f,
                        5f,
                        7,
                    )
                    nvg.drawRoundedRect(
                        this.getX() + offsetX + 62f,
                        this.getY() + this.getHeight() - 36f,
                        50f,
                        23f,
                        5f,
                        applyAlpha(palette.getBackgroundColor(ColorType.MID), 220),
                    )
                    nvg.drawOutlineRoundedRect(
                        this.getX() + offsetX + 62f,
                        this.getY() + this.getHeight() - 36f,
                        50f,
                        23f,
                        5f,
                        1f,
                        applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 210),
                    )

                    // nvg.save()
                    // nvg.scale(this.getX() + offsetX + 62f, this.getY() + this.getHeight() - 31f, 0.07f)
                    nvg.drawRoundedImage(
                        s.getImage(),
                        this.getX() + offsetX + 63f,
                        this.getY() + this.getHeight() - 35f,
                        48f,
                        21f,
                        5f,
                    )
                    // nvg.restore()

                    s.getSelectAnimation().setAnimation(if (currentScreenshot == s) 1.0f else 0.0f, 16)

                    nvg.drawGradientOutlineRoundedRect(
                        this.getX() + offsetX + 62f,
                        this.getY() + this.getHeight() - 36f,
                        50f,
                        23f,
                        5f,
                        s.getSelectAnimation().getValue() * 1.2f,
                        applyAlpha(accentColor.getColor1(), alpha),
                        applyAlpha(accentColor.getColor2(), alpha),
                    )
                }

                offsetX += 54
                index++
            }

            nvg.restore()

            nvg.drawRect(
                this.getX(),
                this.getY() + this.getHeight() - 40f,
                addX,
                30f,
                palette.getBackgroundColor(ColorType.NORMAL),
            )
            nvg.drawRect(
                this.getX() + this.getWidth() - addX,
                this.getY() + this.getHeight() - 40f,
                addX - 14f,
                30f,
                palette.getBackgroundColor(ColorType.NORMAL),
            )

            val leftValue: Float = leftAnimation.getValue()
            val rightValue: Float = rightAnimation.getValue()

            nvg.save()
            nvg.translate(10 - (leftValue * 10), 0f)

            nvg.drawRoundedRect(
                this.getX() + 20f,
                this.getY() + (this.getHeight() / 2) - 30.5f,
                12f,
                24f,
                4f,
                palette.getBackgroundColor(ColorType.DARK, (leftValue * 255).toInt()),
            )
            nvg.drawText(
                "<",
                this.getX() + 23f,
                this.getY() + (this.getHeight() / 2) - 22f,
                palette.getFontColor(ColorType.DARK, (leftValue * 255).toInt()),
                9f,
                Fonts.SEMIBOLD,
            )

            nvg.restore()

            nvg.save()
            nvg.translate(-10 + (rightValue * 10), 0f)

            nvg.drawRoundedRect(
                this.getX() + this.getWidth() - 32f,
                this.getY() + (this.getHeight() / 2) - 30.5f,
                12f,
                24f,
                4f,
                palette.getBackgroundColor(ColorType.DARK, (rightValue * 255).toInt()),
            )
            nvg.drawText(
                ">",
                this.getX() + this.getWidth() - 29f,
                this.getY() + (this.getHeight() / 2) - 22f,
                palette.getFontColor(ColorType.DARK, (rightValue * 255).toInt()),
                9f,
                Fonts.SEMIBOLD,
            )

            nvg.restore()
        } else {
            nvg.drawRoundedRect(
                this.getX() + addX,
                this.getY() + addY,
                this.getWidth() - (addX * 2f),
                this.getHeight() - (addY * 2f) - 38f,
                6f,
                palette.getBackgroundColor(ColorType.DARK),
            )
            nvg.drawCenteredText(
                LegacyIcon.CAMERA,
                this.getX() + addX + ((this.getWidth() - (addX * 2f)) / 2f),
                this.getY() + 68f,
                palette.getFontColor(ColorType.NORMAL),
                64f,
                Fonts.LEGACYICON,
            )

            addX = 58

            nvg.drawRoundedRect(
                this.getX() + addX.toFloat(),
                this.getY() + this.getHeight() - 40f,
                this.getWidth() - (addX * 2f),
                30f,
                6f,
                palette.getBackgroundColor(ColorType.DARK),
            )
        }

        scroll.maxScroll = (if (index > 12f) (index - 12f) * 27f else 0f)
    }

    public override fun mouseClicked(
        mouseX: Int,
        mouseY: Int,
        mouseButton: Int,
    ) {
        val screenshotManager: ScreenshotManager = Shindo.getInstance().getScreenshotManager()

        var offsetX = scroll.getValue()
        var addX = 42
        val addY = 12

        val inside =
            MouseUtils.isInside(
                mouseX,
                mouseY,
                this.getX() + addX,
                this.getY() + addY,
                this.getWidth() - (addX * 2),
                this.getHeight() - (addY * 2) - 38,
            )
        val trash: Boolean =
            MouseUtils.isInside(
                mouseX,
                mouseY,
                this.getX() + this.getWidth() - 61f,
                this.getY() + addY + 4.5f,
                16f,
                16f,
            )

        if (trash && mouseButton == 0) {
            var index = screenshotManager.getScreenshots().indexOf(currentScreenshot) - 1

            screenshotManager.delete(currentScreenshot!!)

            if (index < 0) {
                index = 0
            }

            currentScreenshot =
                if (screenshotManager.getScreenshots().isEmpty()) {
                    null
                } else {
                    screenshotManager.getScreenshots()[index]
                }
        }

        addX = 58

        for (s in screenshotManager.getScreenshots()) {
            if (MouseUtils.isInside(
                    mouseX,
                    mouseY,
                    this.getX() + offsetX + 62f,
                    this.getY() + this.getHeight() - 36f,
                    50f,
                    23f,
                ) &&
                mouseButton == 0 &&
                MouseUtils.isInside(
                    mouseX,
                    mouseY,
                    this.getX() + addX,
                    this.getY() + this.getHeight() - 40,
                    this.getWidth() - (addX * 2),
                    30,
                )
            ) {
                currentScreenshot = s
            }

            offsetX += 54
        }

        if (inside && !trash && mouseButton == 0 && currentScreenshot != null) {
            try {
                Desktop.getDesktop().open(currentScreenshot!!.getImage())
            } catch (e: IOException) {
                ShindoLogger.error("Failed to open screenshot file!")
            }
        }

        if (currentScreenshot != null && mouseButton == 0) {
            if (MouseUtils.isInside(
                    mouseX,
                    mouseY,
                    this.getX() + 20f,
                    this.getY() + (this.getHeight() / 2) - 30.5f,
                    12f,
                    24f,
                )
            ) {
                currentScreenshot = screenshotManager.getBackScreenshot(currentScreenshot!!)
            }

            if (MouseUtils.isInside(
                    mouseX,
                    mouseY,
                    this.getX() + this.getWidth() - 32f,
                    this.getY() + (this.getHeight() / 2) - 30.5f,
                    12f,
                    24f,
                )
            ) {
                currentScreenshot = screenshotManager.getNextScreenshot(currentScreenshot!!)
            }
        }
    }

    override fun keyTyped(
        typedChar: Char,
        keyCode: Int,
    ) {
        val screenshotManager: ScreenshotManager = Shindo.getInstance().getScreenshotManager()
        if (currentScreenshot == null) return
        if (keyCode == Keyboard.KEY_LEFT) {
            currentScreenshot = screenshotManager.getBackScreenshot(currentScreenshot!!)
        }
        if (keyCode == Keyboard.KEY_RIGHT) {
            currentScreenshot = screenshotManager.getNextScreenshot(currentScreenshot!!)
        }
    }
}
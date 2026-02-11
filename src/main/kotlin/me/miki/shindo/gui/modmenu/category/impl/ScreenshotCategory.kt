package me.miki.shindo.gui.modmenu.category.impl

import me.miki.shindo.Shindo
import me.miki.shindo.gui.modmenu.GuiModMenu
import me.miki.shindo.gui.modmenu.category.Category
import me.miki.shindo.management.color.AccentColor
import me.miki.shindo.management.color.palette.ColorPalette
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.screenshot.Screenshot
import me.miki.shindo.management.screenshot.ScreenshotManager
import me.miki.shindo.utils.ColorUtils
import me.miki.shindo.ui.animation.value.SimpleAnimation
import me.miki.shindo.utils.mouse.MouseUtils
import me.miki.shindo.utils.mouse.Scroll
import org.lwjgl.input.Keyboard
import java.awt.Desktop
import java.io.IOException

class ScreenshotCategory(parent: GuiModMenu) :
    Category(parent, TranslateText.SCREENSHOT, LegacyIcon.CAMERA, false, true) {

    private val leftAnimation = SimpleAnimation()
    private val rightAnimation = SimpleAnimation()
    private val trashAnimation = SimpleAnimation()
    private val filmstripScroll = Scroll()

    private val previewBounds = Bounds()
    private val trashBounds = Bounds()
    private val leftButtonBounds = Bounds()
    private val rightButtonBounds = Bounds()
    private val filmstripBarBounds = Bounds()

    private var currentScreenshot: Screenshot? = null

    override fun initCategory() {
        scroll.resetAll()
        filmstripScroll.resetAll()
    }

    override fun initGui() {
        val screenshotManager = Shindo.getInstance().screenshotManager
        if (currentScreenshot == null && screenshotManager.getScreenshots().isNotEmpty()) {
            currentScreenshot = screenshotManager.getScreenshots()[0]
        }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val instance = Shindo.getInstance()
        val nvg = instance.nanoVGManager!!
        val screenshotManager = instance.screenshotManager
        val colorManager = instance.colorManager
        val palette = colorManager.getPalette()
        val accentColor = colorManager.getCurrentColor()
        screenshotManager.loadScreenshots()
        ensureSelection(screenshotManager)
        resetInteractiveBounds()

        if (screenshotManager.getScreenshots().isEmpty()) {
            hideNavigationButtons()
            drawEmptyState(nvg, palette)
            return
        }

        drawFilmstripMode(nvg, palette, accentColor, screenshotManager, mouseX, mouseY)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        val screenshotManager = Shindo.getInstance().screenshotManager
        if (screenshotManager.getScreenshots().isEmpty()) {
            return
        }

        if (currentScreenshot != null && trashBounds.contains(mouseX, mouseY) && mouseButton == 0) {
            deleteCurrentScreenshot(screenshotManager)
            return
        }

        val consumed = handleFilmstripClick(screenshotManager, mouseX, mouseY, mouseButton)

        if (consumed) {
            return
        }

        if (mouseButton == 0 && currentScreenshot != null && previewBounds.contains(
                mouseX,
                mouseY
            ) && !trashBounds.contains(mouseX, mouseY)
        ) {
            openCurrentScreenshot()
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        val screenshotManager = Shindo.getInstance().screenshotManager
        if (currentScreenshot == null || screenshotManager.getScreenshots().isEmpty()) {
            return
        }
        if (keyCode == Keyboard.KEY_LEFT) {
            currentScreenshot = screenshotManager.getBackScreenshot(currentScreenshot!!)
        }
        if (keyCode == Keyboard.KEY_RIGHT) {
            currentScreenshot = screenshotManager.getNextScreenshot(currentScreenshot!!)
        }
    }

    private fun drawFilmstripMode(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accentColor: AccentColor,
        screenshotManager: ScreenshotManager,
        mouseX: Int,
        mouseY: Int
    ) {
        val paddingX = 42f
        val paddingY = 12f
        val previewX = getX() + paddingX
        val previewY = getY() + paddingY
        val previewWidth = getWidth() - (paddingX * 2f)
        val previewHeight = getHeight() - (paddingY * 2f) - 38f

        drawScreenshotPreview(
            nvg,
            palette,
            currentScreenshot,
            previewX,
            previewY,
            previewWidth,
            previewHeight,
            mouseX,
            mouseY
        )

        val barPaddingX = 58f
        val barHeight = 30f
        val barX = getX() + barPaddingX
        val barY = getY() + getHeight() - 40f
        val barWidth = getWidth() - (barPaddingX * 2f)
        filmstripBarBounds.set(barX, barY, barWidth, barHeight)

        val barColor = palette.getBackgroundColor(ColorType.DARK)
        nvg.drawRoundedRect(barX, barY, barWidth, barHeight, 6f, barColor)

        val count = screenshotManager.getScreenshots().size

        val thumbWidth = 36f
        val thumbHeight = 22f
        val step = thumbWidth + 6f
        var offsetX = 0f
        val thumbY = barY + (barHeight - thumbHeight) / 2f
        val visibleWidth = kotlin.math.max(0f, barWidth - 8f)
        val contentWidth = count * step
        filmstripScroll.maxScroll = kotlin.math.max(0f, contentWidth - visibleWidth)

        if (MouseUtils.isInside(mouseX, mouseY, barX, barY, barWidth, barHeight)) {
            filmstripScroll.onScroll()
        }
        filmstripScroll.onAnimation()
        val scrollValue = filmstripScroll.getValue()

        nvg.save()
        nvg.scissor(barX, barY, barWidth, barHeight)

        for (screenshot in screenshotManager.getScreenshots()) {
            val x = barX + 4f + offsetX + scrollValue
            if (x + thumbWidth > barX - 4f && x < barX + barWidth + 4f) {
                nvg.drawRoundedRect(
                    x,
                    thumbY,
                    thumbWidth,
                    thumbHeight,
                    6f,
                    palette.getBackgroundColor(ColorType.NORMAL)
                )
                nvg.save()
                nvg.intersectScissor(x + 1f, thumbY + 1f, thumbWidth - 2f, thumbHeight - 2f)
                drawThumbnailImage(nvg, screenshot, x, thumbY, thumbWidth, thumbHeight)
                nvg.restore()

                screenshot.getSelectAnimation().setAnimation(if (currentScreenshot == screenshot) 1f else 0f, 16.0)
                val alpha = (screenshot.getSelectAnimation().value * 255).toInt()
                if (alpha > 0) {
                    nvg.drawGradientOutlineRoundedRect(
                        x,
                        thumbY,
                        thumbWidth,
                        thumbHeight,
                        6f,
                        screenshot.getSelectAnimation().value * 1.2f,
                        ColorUtils.applyAlpha(accentColor.getColor1(), alpha),
                        ColorUtils.applyAlpha(accentColor.getColor2(), alpha)
                    )
                }
            }
            offsetX += step
        }

        nvg.restore()

        drawNavigationButtons(nvg, palette, mouseX, mouseY, screenshotManager.getScreenshots().size > 1)
    }

    private fun drawScreenshotPreview(
        nvg: NanoVGManager,
        palette: ColorPalette,
        screenshot: Screenshot?,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        mouseX: Int,
        mouseY: Int
    ) {
        previewBounds.set(x, y, width, height)
        if (screenshot == null) {
            trashBounds.clear()
            nvg.drawRoundedRect(x, y, width, height, 8f, palette.getBackgroundColor(ColorType.DARK))
            return
        }

        trashAnimation.setAnimation(if (MouseUtils.isInside(mouseX, mouseY, x, y, width, height)) 1f else 0f, 16.0)
        nvg.drawRoundedImage(screenshot.getImage(), x, y, width, height, 8f)

        val trashSize = 16f
        val trashX = x + width - trashSize - 8f
        val trashY = y + 8f
        trashBounds.set(trashX - 2f, trashY - 2f, trashSize + 4f, trashSize + 4f)

        nvg.drawText(
            LegacyIcon.TRASH,
            trashX,
            trashY,
            palette.getMaterialRed((trashAnimation.value * 255).toInt()),
            12f,
            Fonts.LEGACYICON
        )
    }

    private fun drawNavigationButtons(
        nvg: NanoVGManager,
        palette: ColorPalette,
        mouseX: Int,
        mouseY: Int,
        visible: Boolean
    ) {
        if (!visible) {
            hideNavigationButtons()
            return
        }

        val buttonWidth = 12f
        val buttonHeight = 24f
        val baseY = getY() + (getHeight() / 2f) - 30.5f

        leftButtonBounds.set(getX() + 20f, baseY, buttonWidth, buttonHeight)
        rightButtonBounds.set(getX() + getWidth() - 32f, baseY, buttonWidth, buttonHeight)

        val leftHovered = leftButtonBounds.contains(mouseX, mouseY)
        val rightHovered = rightButtonBounds.contains(mouseX, mouseY)
        leftAnimation.setAnimation(if (leftHovered) 1f else 0f, 16.0)
        rightAnimation.setAnimation(if (rightHovered) 1f else 0f, 16.0)

        val leftValue = leftAnimation.value
        val rightValue = rightAnimation.value

        nvg.save()
        nvg.translate(10 - (leftValue * 10f), 0f)
        nvg.drawRoundedRect(
            leftButtonBounds.x,
            leftButtonBounds.y,
            buttonWidth,
            buttonHeight,
            4f,
            palette.getBackgroundColor(ColorType.DARK, (leftValue * 255).toInt())
        )
        nvg.drawText(
            LegacyIcon.CHEVRON_LEFT,
            leftButtonBounds.x + 2f,
            leftButtonBounds.y + 8f,
            palette.getFontColor(ColorType.DARK, (leftValue * 255).toInt()),
            10f,
            Fonts.LEGACYICON
        )
        nvg.restore()

        nvg.save()
        nvg.translate(-10 + (rightValue * 10f), 0f)
        nvg.drawRoundedRect(
            rightButtonBounds.x,
            rightButtonBounds.y,
            buttonWidth,
            buttonHeight,
            4f,
            palette.getBackgroundColor(ColorType.DARK, (rightValue * 255).toInt())
        )
        nvg.drawText(
            LegacyIcon.CHEVRON_RIGHT,
            rightButtonBounds.x + 2f,
            rightButtonBounds.y + 8f,
            palette.getFontColor(ColorType.DARK, (rightValue * 255).toInt()),
            10f,
            Fonts.LEGACYICON
        )
        nvg.restore()
    }

    private fun hideNavigationButtons() {
        leftButtonBounds.clear()
        rightButtonBounds.clear()
        leftAnimation.value = 0f
        rightAnimation.value = 0f
    }

    private fun handleFilmstripClick(
        screenshotManager: ScreenshotManager,
        mouseX: Int,
        mouseY: Int,
        mouseButton: Int
    ): Boolean {
        if (mouseButton != 0) {
            return false
        }

        if (handleNavigationButtonsClick(screenshotManager, mouseX, mouseY)) {
            return true
        }

        val target = findFilmstripTargetAt(mouseX, mouseY, screenshotManager)
        if (target != null) {
            currentScreenshot = target
            return true
        }
        return false
    }

    private fun findFilmstripTargetAt(mouseX: Int, mouseY: Int, screenshotManager: ScreenshotManager): Screenshot? {
        if (!filmstripBarBounds.contains(mouseX, mouseY)) {
            return null
        }

        val scrollValue = filmstripScroll.getValue()
        val thumbWidth = 36f
        val thumbHeight = 22f
        val step = thumbWidth + 6f
        val thumbY = filmstripBarBounds.y + (filmstripBarBounds.height - thumbHeight) / 2f
        var offsetX = 0f
        for (screenshot in screenshotManager.getScreenshots()) {
            val x = filmstripBarBounds.x + 4f + offsetX + scrollValue
            if (MouseUtils.isInside(mouseX, mouseY, x, thumbY, thumbWidth, thumbHeight)) {
                return screenshot
            }
            offsetX += step
        }
        return null
    }

    private fun handleNavigationButtonsClick(screenshotManager: ScreenshotManager, mouseX: Int, mouseY: Int): Boolean {
        if (currentScreenshot == null || screenshotManager.getScreenshots().size <= 1) {
            return false
        }
        if (leftButtonBounds.contains(mouseX, mouseY)) {
            currentScreenshot = screenshotManager.getBackScreenshot(currentScreenshot!!)
            return true
        }
        if (rightButtonBounds.contains(mouseX, mouseY)) {
            currentScreenshot = screenshotManager.getNextScreenshot(currentScreenshot!!)
            return true
        }
        return false
    }

    private fun drawEmptyState(nvg: NanoVGManager, palette: ColorPalette) {
        val paddingX = 42f
        val paddingY = 12f
        val width = getWidth() - (paddingX * 2f)
        val height = getHeight() - (paddingY * 2f) - 38f
        val x = getX() + paddingX
        val y = getY() + paddingY

        nvg.drawRoundedRect(x, y, width, height, 6f, palette.getBackgroundColor(ColorType.DARK))
        nvg.drawCenteredText(
            LegacyIcon.CAMERA,
            x + width / 2f,
            y + 56f,
            palette.getFontColor(ColorType.NORMAL),
            64f,
            Fonts.LEGACYICON
        )

        val barX = getX() + 58f
        val barWidth = getWidth() - (58f * 2f)
        nvg.drawRoundedRect(
            barX,
            getY() + getHeight() - 40f,
            barWidth,
            30f,
            6f,
            palette.getBackgroundColor(ColorType.DARK)
        )
    }

    private fun drawThumbnailImage(
        nvg: NanoVGManager,
        screenshot: Screenshot,
        x: Float,
        y: Float,
        width: Float,
        height: Float
    ) {
        val padding = 1f
        val availableWidth = width - (padding * 2f)
        val availableHeight = height - (padding * 2f)
        val aspect = 16f / 9f
        var drawWidth = availableWidth
        var drawHeight = drawWidth / aspect
        if (drawHeight < availableHeight) {
            drawHeight = availableHeight
            drawWidth = drawHeight * aspect
        }
        val drawX = x + (width - drawWidth) / 2f
        val drawY = y + (height - drawHeight) / 2f

        nvg.drawImage(screenshot.getImage(), drawX, drawY, drawWidth, drawHeight)
    }

    private fun ensureSelection(screenshotManager: ScreenshotManager) {
        if (currentScreenshot != null && !screenshotManager.getScreenshots().contains(currentScreenshot)) {
            currentScreenshot = null
        }
        if (currentScreenshot == null && screenshotManager.getScreenshots().isNotEmpty()) {
            currentScreenshot = screenshotManager.getScreenshots()[0]
        }
    }

    private fun deleteCurrentScreenshot(screenshotManager: ScreenshotManager) {
        if (currentScreenshot == null) {
            return
        }

        var index = screenshotManager.getScreenshots().indexOf(currentScreenshot)
        screenshotManager.delete(currentScreenshot!!)
        if (screenshotManager.getScreenshots().isEmpty()) {
            currentScreenshot = null
            return
        }
        index = kotlin.math.max(0, kotlin.math.min(index - 1, screenshotManager.getScreenshots().size - 1))
        currentScreenshot = screenshotManager.getScreenshots()[index]
    }

    private fun openCurrentScreenshot() {
        val screenshot = currentScreenshot ?: return
        try {
            Desktop.getDesktop().open(screenshot.getImage())
        } catch (ignored: IOException) {
        }
    }

    private fun resetInteractiveBounds() {
        previewBounds.clear()
        trashBounds.clear()
        filmstripBarBounds.clear()
        leftButtonBounds.clear()
        rightButtonBounds.clear()
    }

    private class Bounds {
        var x = 0f
        var y = 0f
        var width = 0f
        var height = 0f

        fun set(x: Float, y: Float, width: Float, height: Float) {
            this.x = x
            this.y = y
            this.width = width
            this.height = height
        }

        fun contains(mouseX: Int, mouseY: Int): Boolean {
            return width > 0f && height > 0f && MouseUtils.isInside(mouseX, mouseY, x, y, width, height)
        }

        fun clear() {
            set(0f, 0f, 0f, 0f)
        }
    }
}

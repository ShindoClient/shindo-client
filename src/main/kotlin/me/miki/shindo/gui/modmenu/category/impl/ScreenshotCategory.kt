package me.miki.shindo.gui.modmenu.category.impl

import me.miki.shindo.Shindo
import me.miki.shindo.gui.modmenu.GuiModMenu
import me.miki.shindo.gui.modmenu.category.Category
import me.miki.shindo.management.color.AccentColor
import me.miki.shindo.management.color.ColorManager
import me.miki.shindo.management.color.palette.ColorPalette
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.impl.InternalSettingsMod
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.screenshot.Screenshot
import me.miki.shindo.management.screenshot.ScreenshotDisplayMode
import me.miki.shindo.management.screenshot.ScreenshotManager
import me.miki.shindo.utils.ColorUtils
import me.miki.shindo.utils.animation.simple.SimpleAnimation
import me.miki.shindo.utils.mouse.MouseUtils
import me.miki.shindo.utils.mouse.Scroll
import org.lwjgl.input.Keyboard
import java.awt.Color
import java.awt.Desktop
import java.io.IOException

class ScreenshotCategory(parent: GuiModMenu) : Category(parent, TranslateText.SCREENSHOT, LegacyIcon.CAMERA, false, true) {

    private val leftAnimation = SimpleAnimation()
    private val rightAnimation = SimpleAnimation()
    private val trashAnimation = SimpleAnimation()
    private val backAnimation = SimpleAnimation()
    private val filmstripScroll = Scroll()
    private val gridScroll = Scroll()

    private val previewBounds = Bounds()
    private val trashBounds = Bounds()
    private val leftButtonBounds = Bounds()
    private val rightButtonBounds = Bounds()
    private val filmstripBarBounds = Bounds()
    private val gridBackBounds = Bounds()
    private val gridAreaBounds = Bounds()

    private var currentScreenshot: Screenshot? = null
    private var gridPreviewActive = false
    private var gridListVisible = false
    private var gridCardWidth = 0f
    private var gridCardHeight = 0f

    override fun initCategory() {
        scroll.resetAll()
        filmstripScroll.resetAll()
        gridScroll.resetAll()
        gridPreviewActive = false
    }

    override fun initGui() {
        val screenshotManager = Shindo.getInstance().screenshotManager
        if (currentScreenshot == null && screenshotManager.screenshots.isNotEmpty()) {
            currentScreenshot = screenshotManager.screenshots[0]
        }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val instance = Shindo.getInstance()
        val nvg = instance.nanoVGManager!!
        val screenshotManager = instance.screenshotManager
        val colorManager = instance.colorManager
        val palette = colorManager.palette
        val accentColor = colorManager.currentColor
        val displayMode = InternalSettingsMod.getInstance().screenshotDisplayMode

        screenshotManager.loadScreenshots()
        ensureSelection(screenshotManager)

        if (displayMode != ScreenshotDisplayMode.GRID) {
            gridPreviewActive = false
        }

        resetInteractiveBounds()

        if (screenshotManager.screenshots.isEmpty()) {
            hideNavigationButtons()
            drawEmptyState(nvg, palette)
            return
        }

        if (displayMode == ScreenshotDisplayMode.GRID) {
            drawGridMode(nvg, palette, accentColor, screenshotManager, mouseX, mouseY)
        } else {
            drawFilmstripMode(nvg, palette, accentColor, screenshotManager, mouseX, mouseY)
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        val screenshotManager = Shindo.getInstance().screenshotManager
        if (screenshotManager.screenshots.isEmpty()) {
            return
        }

        val displayMode = InternalSettingsMod.getInstance().screenshotDisplayMode

        if (currentScreenshot != null && trashBounds.contains(mouseX, mouseY) && mouseButton == 0) {
            deleteCurrentScreenshot(screenshotManager)
            return
        }

        val consumed = if (displayMode == ScreenshotDisplayMode.GRID) {
            handleGridClick(screenshotManager, mouseX, mouseY, mouseButton)
        } else {
            handleFilmstripClick(screenshotManager, mouseX, mouseY, mouseButton)
        }

        if (consumed) {
            return
        }

        if (mouseButton == 0 && currentScreenshot != null && previewBounds.contains(mouseX, mouseY) && !trashBounds.contains(mouseX, mouseY) && !gridBackBounds.contains(mouseX, mouseY)) {
            openCurrentScreenshot()
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        val screenshotManager = Shindo.getInstance().screenshotManager
        if (currentScreenshot == null || screenshotManager.screenshots.isEmpty()) {
            return
        }
        if (keyCode == Keyboard.KEY_LEFT) {
            currentScreenshot = screenshotManager.getBackScreenshot(currentScreenshot)
        }
        if (keyCode == Keyboard.KEY_RIGHT) {
            currentScreenshot = screenshotManager.getNextScreenshot(currentScreenshot)
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

        drawScreenshotPreview(nvg, palette, currentScreenshot, previewX, previewY, previewWidth, previewHeight, mouseX, mouseY)

        val barPaddingX = 58f
        val barHeight = 30f
        val barX = getX() + barPaddingX
        val barY = getY() + getHeight() - 40f
        val barWidth = getWidth() - (barPaddingX * 2f)
        filmstripBarBounds.set(barX, barY, barWidth, barHeight)

        val barColor = palette.getBackgroundColor(ColorType.DARK)
        nvg.drawRoundedRect(barX, barY, barWidth, barHeight, 6f, barColor)

        val count = screenshotManager.screenshots.size

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

        for (screenshot in screenshotManager.screenshots) {
            val x = barX + 4f + offsetX + scrollValue
            if (x + thumbWidth > barX - 4f && x < barX + barWidth + 4f) {
                nvg.drawRoundedRect(x, thumbY, thumbWidth, thumbHeight, 6f, palette.getBackgroundColor(ColorType.NORMAL))
                nvg.save()
                nvg.intersectScissor(x + 1f, thumbY + 1f, thumbWidth - 2f, thumbHeight - 2f)
                drawThumbnailImage(nvg, screenshot, x, thumbY, thumbWidth, thumbHeight)
                nvg.restore()

                screenshot.selectAnimation.setAnimation(if (currentScreenshot == screenshot) 1f else 0f, 16.0)
                val alpha = (screenshot.selectAnimation.value * 255).toInt()
                if (alpha > 0) {
                    nvg.drawGradientOutlineRoundedRect(
                        x,
                        thumbY,
                        thumbWidth,
                        thumbHeight,
                        6f,
                        screenshot.selectAnimation.value * 1.2f,
                        ColorUtils.applyAlpha(accentColor.color1, alpha),
                        ColorUtils.applyAlpha(accentColor.color2, alpha)
                    )
                }
            }
            offsetX += step
        }

        nvg.restore()

        drawNavigationButtons(nvg, palette, mouseX, mouseY, screenshotManager.screenshots.size > 1)
    }

    private fun drawGridMode(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accentColor: AccentColor,
        screenshotManager: ScreenshotManager,
        mouseX: Int,
        mouseY: Int
    ) {
        hideNavigationButtons()
        filmstripBarBounds.clear()
        gridListVisible = false

        if (gridPreviewActive && currentScreenshot != null) {
            drawGridPreview(nvg, palette, currentScreenshot, mouseX, mouseY)
            return
        }

        gridBackBounds.clear()
        val areaX = getX() + 20f
        val areaY = getY() + 20f
        val areaWidth = getWidth() - 40f
        val areaHeight = getHeight() - 60f
        gridAreaBounds.set(areaX, areaY, areaWidth, areaHeight)
        gridListVisible = true

        val frameColor = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 220)
        nvg.drawRoundedRect(areaX - 8f, areaY - 8f, areaWidth + 16f, areaHeight + 16f, 14f, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 150))
        nvg.drawRoundedRect(areaX, areaY, areaWidth, areaHeight, 12f, frameColor)

        val count = screenshotManager.screenshots.size
        gridCardWidth = ((areaWidth - GRID_SPACING * (GRID_COLUMNS - 1)) / GRID_COLUMNS) - 15f
        gridCardHeight = gridCardWidth * 9f / 16f
        val rows = kotlin.math.ceil(count / GRID_COLUMNS.toFloat()).toInt()
        val cellHeight = gridCardHeight + GRID_CAPTION_HEIGHT
        val contentHeight = rows * cellHeight + kotlin.math.max(0, rows - 1) * GRID_SPACING
        gridScroll.maxScroll = kotlin.math.max(0f, contentHeight - areaHeight)

        if (MouseUtils.isInside(mouseX, mouseY, areaX, areaY, areaWidth, areaHeight)) {
            gridScroll.onScroll()
        }
        gridScroll.onAnimation()
        val scrollValue = gridScroll.getValue()

        nvg.save()
        nvg.scissor(areaX, areaY, areaWidth, areaHeight)

        for ((index, screenshot) in screenshotManager.screenshots.withIndex()) {
            val column = index % GRID_COLUMNS
            val row = index / GRID_COLUMNS
            val cardX = areaX + column * (gridCardWidth + GRID_SPACING) + 20f
            val cardY = areaY + row * (cellHeight + GRID_SPACING) + scrollValue + 10f
            val label = nvg.getLimitText(screenshot.name, 8.5f, Fonts.REGULAR, gridCardWidth)

            nvg.drawRoundedRect(
                cardX,
                cardY,
                gridCardWidth,
                gridCardHeight + 8f + nvg.getTextHeight(label, 8.5f, Fonts.REGULAR),
                8f,
                ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 210)
            )
            nvg.drawRoundedImage(screenshot.image, cardX, cardY, gridCardWidth, gridCardHeight, 8f)

            screenshot.selectAnimation.setAnimation(if (currentScreenshot == screenshot) 1f else 0f, 18.0)
            val selected = screenshot.selectAnimation.value
            if (selected > 0f) {
                nvg.drawGradientOutlineRoundedRect(
                    cardX,
                    cardY,
                    gridCardWidth,
                    gridCardHeight,
                    8f,
                    selected * 1.4f,
                    ColorUtils.applyAlpha(accentColor.color1, (selected * 220).toInt()),
                    ColorUtils.applyAlpha(accentColor.color2, (selected * 220).toInt())
                )
            }

            nvg.drawCenteredText(label, cardX + (gridCardWidth / 2f), cardY + gridCardHeight + 8f, palette.getFontColor(ColorType.NORMAL), 8.5f, Fonts.REGULAR)

        }

        nvg.restore()
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
        nvg.drawRoundedImage(screenshot.image, x, y, width, height, 8f)

        val trashSize = 16f
        val trashX = x + width - trashSize - 8f
        val trashY = y + 8f
        trashBounds.set(trashX - 2f, trashY - 2f, trashSize + 4f, trashSize + 4f)

        nvg.drawText(LegacyIcon.TRASH, trashX, trashY, palette.getMaterialRed((trashAnimation.value * 255).toInt()), 12f, Fonts.LEGACYICON)
    }

    private fun drawGridPreview(nvg: NanoVGManager, palette: ColorPalette, screenshot: Screenshot?, mouseX: Int, mouseY: Int) {
        val previewX = getX() + 48f
        val previewY = getY() + 28f
        val previewWidth = getWidth() - 96f
        val previewHeight = getHeight() - 72f

        drawScreenshotPreview(nvg, palette, screenshot, previewX, previewY, previewWidth, previewHeight, mouseX, mouseY)

        backAnimation.setAnimation(if (MouseUtils.isInside(mouseX, mouseY, previewX, previewY, previewWidth, previewHeight)) 1f else 0f, 16.0)

        val backSize = 16f
        val backX = previewX + 8f
        val backY = previewY + 8f
        gridBackBounds.set(backX - 2f, backY - 2f, backSize + 4f, backSize + 4f)

        nvg.drawText(LegacyIcon.BACK, backX, backY, palette.getFontColor(ColorType.DARK, (backAnimation.value * 255).toInt()), 12f, Fonts.LEGACYICON)
    }

    private fun drawNavigationButtons(nvg: NanoVGManager, palette: ColorPalette, mouseX: Int, mouseY: Int, visible: Boolean) {
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
        nvg.drawRoundedRect(leftButtonBounds.x, leftButtonBounds.y, buttonWidth, buttonHeight, 4f, palette.getBackgroundColor(ColorType.DARK, (leftValue * 255).toInt()))
        nvg.drawText(LegacyIcon.CHEVRON_LEFT, leftButtonBounds.x + 2f, leftButtonBounds.y + 8f, palette.getFontColor(ColorType.DARK, (leftValue * 255).toInt()), 10f, Fonts.LEGACYICON)
        nvg.restore()

        nvg.save()
        nvg.translate(-10 + (rightValue * 10f), 0f)
        nvg.drawRoundedRect(rightButtonBounds.x, rightButtonBounds.y, buttonWidth, buttonHeight, 4f, palette.getBackgroundColor(ColorType.DARK, (rightValue * 255).toInt()))
        nvg.drawText(LegacyIcon.CHEVRON_RIGHT, rightButtonBounds.x + 2f, rightButtonBounds.y + 8f, palette.getFontColor(ColorType.DARK, (rightValue * 255).toInt()), 10f, Fonts.LEGACYICON)
        nvg.restore()
    }

    private fun hideNavigationButtons() {
        leftButtonBounds.clear()
        rightButtonBounds.clear()
        leftAnimation.value = 0f
        rightAnimation.value = 0f
    }
    private fun handleFilmstripClick(screenshotManager: ScreenshotManager, mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
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

    private fun handleGridClick(screenshotManager: ScreenshotManager, mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        if (mouseButton != 0) {
            return false
        }

        if (gridPreviewActive) {
            if (gridBackBounds.contains(mouseX, mouseY)) {
                gridPreviewActive = false
                return true
            }
            return handleNavigationButtonsClick(screenshotManager, mouseX, mouseY)
        }

        val target = findGridTargetAt(mouseX, mouseY, screenshotManager)
        if (target != null) {
            currentScreenshot = target
            gridPreviewActive = true
            return true
        }
        return false
    }

    private fun findGridTargetAt(mouseX: Int, mouseY: Int, screenshotManager: ScreenshotManager): Screenshot? {
        if (!gridListVisible || !gridAreaBounds.contains(mouseX, mouseY)) {
            return null
        }
        val scrollValue = gridScroll.getValue()
        val cellHeight = gridCardHeight + GRID_CAPTION_HEIGHT + GRID_SPACING
        for ((index, screenshot) in screenshotManager.screenshots.withIndex()) {
            val column = index % GRID_COLUMNS
            val row = index / GRID_COLUMNS
            val cardX = gridAreaBounds.x + column * (gridCardWidth + GRID_SPACING)
            val cardY = gridAreaBounds.y + row * cellHeight + scrollValue
            if (MouseUtils.isInside(mouseX, mouseY, cardX, cardY, gridCardWidth, gridCardHeight)) {
                return screenshot
            }
        }
        return null
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
        for (screenshot in screenshotManager.screenshots) {
            val x = filmstripBarBounds.x + 4f + offsetX + scrollValue
            if (MouseUtils.isInside(mouseX, mouseY, x, thumbY, thumbWidth, thumbHeight)) {
                return screenshot
            }
            offsetX += step
        }
        return null
    }

    private fun handleNavigationButtonsClick(screenshotManager: ScreenshotManager, mouseX: Int, mouseY: Int): Boolean {
        if (currentScreenshot == null || screenshotManager.screenshots.size <= 1) {
            return false
        }
        if (leftButtonBounds.contains(mouseX, mouseY)) {
            currentScreenshot = screenshotManager.getBackScreenshot(currentScreenshot)
            return true
        }
        if (rightButtonBounds.contains(mouseX, mouseY)) {
            currentScreenshot = screenshotManager.getNextScreenshot(currentScreenshot)
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
        nvg.drawCenteredText(LegacyIcon.CAMERA, x + width / 2f, y + 56f, palette.getFontColor(ColorType.NORMAL), 64f, Fonts.LEGACYICON)

        val barX = getX() + 58f
        val barWidth = getWidth() - (58f * 2f)
        nvg.drawRoundedRect(barX, getY() + getHeight() - 40f, barWidth, 30f, 6f, palette.getBackgroundColor(ColorType.DARK))
    }

    private fun drawThumbnailImage(nvg: NanoVGManager, screenshot: Screenshot, x: Float, y: Float, width: Float, height: Float) {
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

        nvg.drawImage(screenshot.image, drawX, drawY, drawWidth, drawHeight)
    }

    private fun ensureSelection(screenshotManager: ScreenshotManager) {
        if (currentScreenshot != null && !screenshotManager.screenshots.contains(currentScreenshot)) {
            currentScreenshot = null
        }
        if (currentScreenshot == null && screenshotManager.screenshots.isNotEmpty()) {
            currentScreenshot = screenshotManager.screenshots[0]
        }
        if (currentScreenshot == null) {
            gridPreviewActive = false
        }
    }

    private fun deleteCurrentScreenshot(screenshotManager: ScreenshotManager) {
        if (currentScreenshot == null) {
            return
        }

        var index = screenshotManager.screenshots.indexOf(currentScreenshot)
        screenshotManager.delete(currentScreenshot)
        if (screenshotManager.screenshots.isEmpty()) {
            currentScreenshot = null
            gridPreviewActive = false
            return
        }
        index = kotlin.math.max(0, kotlin.math.min(index - 1, screenshotManager.screenshots.size - 1))
        currentScreenshot = screenshotManager.screenshots[index]
    }

    private fun openCurrentScreenshot() {
        val screenshot = currentScreenshot ?: return
        try {
            Desktop.getDesktop().open(screenshot.image)
        } catch (ignored: IOException) {
        }
    }

    private fun resetInteractiveBounds() {
        previewBounds.clear()
        trashBounds.clear()
        filmstripBarBounds.clear()
        leftButtonBounds.clear()
        rightButtonBounds.clear()
        gridBackBounds.clear()
        gridAreaBounds.clear()
        gridListVisible = false
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

    private companion object {
        private const val GRID_COLUMNS = 3
        private const val GRID_SPACING = 18f
        private const val GRID_CAPTION_HEIGHT = 18f
    }
}

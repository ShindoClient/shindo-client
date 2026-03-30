package me.miki.shindo.gui.mainmenu.impl

import eu.shoroa.contrib.render.Blur
import me.miki.shindo.Shindo
import me.miki.shindo.gui.mainmenu.GuiShindoMainMenu
import me.miki.shindo.gui.mainmenu.MainMenuScene
import me.miki.shindo.logger.ShindoLogger
import me.miki.shindo.management.color.palette.ColorPalette
import me.miki.shindo.management.file.FileManager
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.profile.mainmenu.BackgroundManager
import me.miki.shindo.management.profile.mainmenu.impl.CustomBackground
import me.miki.shindo.management.profile.mainmenu.impl.DefaultBackground
import me.miki.shindo.management.profile.mainmenu.impl.ShaderBackground
import me.miki.shindo.management.shader.ShaderBackgroundRenderer
import me.miki.shindo.ui.animation.Animation
import me.miki.shindo.ui.animation.Direction
import me.miki.shindo.ui.animation.easing.EaseInOutCirc
import me.miki.shindo.ui.animation.screen.ScreenAnimation
import me.miki.shindo.utils.concurrent.TaskExecutor
import me.miki.shindo.utils.concurrent.ThreadPoolType
import me.miki.shindo.utils.file.FileUtils
import me.miki.shindo.utils.mouse.MouseUtils
import me.miki.shindo.utils.mouse.Scroll
import net.minecraft.client.gui.ScaledResolution
import org.lwjgl.input.Keyboard
import java.awt.Color
import java.io.File
import java.io.IOException

class BackgroundScene(parent: GuiShindoMainMenu) : MainMenuScene(parent) {

    private val screenAnimation = ScreenAnimation()
    private val scroll = Scroll()
    private lateinit var introAnimation: Animation

    override fun initScene() {
        introAnimation = EaseInOutCirc(250, 1.0)
        introAnimation.setDirection(Direction.FORWARDS)
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val sr = ScaledResolution(mc)
        val instance = Shindo.getInstance()
        val nvg = instance.nanoVGManager
        screenAnimation.wrap(
            Runnable { drawNanoVG(mouseX, mouseY, sr, instance, nvg) },
            0,
            0,
            sr.scaledWidth,
            sr.scaledHeight,
            2f - introAnimation.getValueFloat(),
            introAnimation.getValueFloat().coerceAtMost(1f),
            false
        )
        if (introAnimation.isDone(Direction.BACKWARDS)) {
            setCurrentScene(getSceneByClass(MainScene::class.java))
        }
    }

    private fun drawNanoVG(mouseX: Int, mouseY: Int, sr: ScaledResolution, instance: Shindo, nvg: NanoVGManager?) {
        val backgroundManager: BackgroundManager = instance.profileManager.backgroundManager
        val palette: ColorPalette = getMenuPalette()
        val panelColor = getPanelColor()
        val controlColor = getControlColor()

        val acWidth = 240
        val acHeight = 148
        val acX = sr.scaledWidth / 2 - (acWidth / 2)
        val acY = sr.scaledHeight / 2 - (acHeight / 2)
        var offsetX = 0
        var offsetY = 0
        var index = 1
        var prevIndex = 1

        scroll.onScroll()
        scroll.onAnimation()

        Blur.drawBlur(acX.toFloat(), acY.toFloat(), acWidth.toFloat(), acHeight.toFloat(), 8f)
        nvg!!.drawRoundedRect(acX.toFloat(), acY.toFloat(), acWidth.toFloat(), acHeight.toFloat(), 8f, panelColor)
        nvg.drawCenteredText(
            TranslateText.SELECT_BACKGROUND.getText(),
            acX + (acWidth / 2f),
            acY + 8f,
            Color.WHITE,
            14f,
            Fonts.SEMIBOLD
        )

        nvg.save()
        nvg.scissor(acX.toFloat(), acY + 25f, acWidth.toFloat(), acHeight - 25f)
        nvg.translate(0f, scroll.getValue())

        for (bg in backgroundManager.getBackgrounds()) {
            val isSelected = backgroundManager.getCurrentBackground() == bg
            val itemX = acX + 11f + offsetX
            val itemY = acY + 35f + offsetY
            val itemWidth = 102.5f
            val itemHeight = 57.5f

            if (isSelected) {
                nvg.drawGradientShadow(
                    itemX - 1,
                    itemY - 1,
                    itemWidth + 2,
                    itemHeight + 2,
                    7f,
                    Color(255, 255, 255, 180),
                    Color(255, 255, 255, 180)
                )
                nvg.drawRoundedRect(itemX - 1, itemY - 1, itemWidth + 2, itemHeight + 2, 7f, Color(255, 255, 255, 180))
            }

            if (MouseUtils.isInside(mouseX, mouseY, itemX, itemY + scroll.getValue(), itemWidth, itemHeight)) {
                nvg.drawRoundedRect(itemX - 1, itemY - 1, itemWidth + 2, itemHeight + 2, 7f, Color(255, 255, 255, 100))
            }

            if (bg is DefaultBackground) {
                if (bg.getId() == 999) {
                    nvg.drawRoundedRect(acX + 11f + offsetX, acY + 35f + offsetY, 102.5f, 57.5f, 6f, Color.BLACK)
                    nvg.drawCenteredText(
                        LegacyIcon.PLUS,
                        acX + 10f + offsetX + (102.5f / 2),
                        acY + 42.5f + offsetY,
                        Color.WHITE,
                        26f,
                        Fonts.LEGACYICON
                    )
                } else {
                    nvg.drawRoundedImage(bg.getImage()!!, acX + 11f + offsetX, acY + 35f + offsetY, 102.5f, 57.5f, 6f)
                }
            }

            if (bg is ShaderBackground) {
                ShaderBackgroundRenderer.renderShaderPreview(
                    nvg,
                    bg.getShaderFile(),
                    acX + 11f + offsetX,
                    acY + 35f + offsetY,
                    102.5f,
                    57.5f
                )
            }

            if (bg is CustomBackground) {
                bg.getTrashAnimation().setAnimation(
                    if (MouseUtils.isInside(
                            mouseX,
                            mouseY,
                            acX + 11f + offsetX,
                            acY + 35f + offsetY + scroll.getValue(),
                            102.5f,
                            57.5f
                        )
                    ) 1.0f else 0.0f,
                    16
                )

                nvg.drawRoundedImage(bg.getImage(), acX + 11f + offsetX, acY + 35f + offsetY, 102.5f, 57.5f, 6f)
                nvg.drawText(
                    LegacyIcon.TRASH,
                    acX + offsetX + 100f,
                    acY + 38f + offsetY,
                    palette.getMaterialRed((bg.getTrashAnimation().value * 255).toInt()),
                    10f,
                    Fonts.LEGACYICON
                )
            }

            nvg.drawRoundedRectVarying(
                acX + offsetX + 11f,
                acY + offsetY + 76.5f,
                102.5f,
                16f,
                0f,
                0f,
                6f,
                6f,
                controlColor
            )
            nvg.drawCenteredText(
                bg.getName()!!,
                acX + offsetX + 11f + (102.5f / 2),
                acY + offsetY + 80f,
                Color.WHITE,
                10f,
                Fonts.REGULAR
            )

            offsetX += 115

            if (index % 2 == 0) {
                offsetY += 70
                offsetX = 0
                prevIndex++
            }

            index++
        }

        nvg.restore()

        scroll.maxScroll = if (prevIndex == 1) 0f else offsetY - (70f / 1.56f) - if (index % 2 == 1) 70f else 0f
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        val sr = ScaledResolution(mc)

        val instance = Shindo.getInstance()
        val fileManager: FileManager = instance.fileManager
        val backgroundManager: BackgroundManager = instance.profileManager.backgroundManager

        val acWidth = 240
        val acHeight = 148
        val acX = sr.scaledWidth / 2 - (acWidth / 2)
        val acY = sr.scaledHeight / 2 - (acHeight / 2)
        var offsetX = 0
        var offsetY = (0 + scroll.getValue()).toInt()
        var index = 1

        if (!MouseUtils.isInside(mouseX, mouseY, acX.toFloat(), acY.toFloat(), acWidth.toFloat(), acHeight.toFloat())
            && !MouseUtils.isInside(mouseX, mouseY, sr.scaledWidth - 56f, 6f, 22f, 22f)
        ) {
            introAnimation.setDirection(Direction.BACKWARDS)
        }

        for (bg in backgroundManager.getBackgrounds()) {
            if (mouseButton == 0) {
                if (MouseUtils.isInside(mouseX, mouseY, acX + 11f + offsetX, acY + 35f + offsetY, 102.5f, 57.5f)) {
                    if (bg.getId() == 999) {
                        TaskExecutor.runAsync(ThreadPoolType.IO) {
                            val file = FileUtils.selectImageFile()
                            val bgCacheDir = File(fileManager.cacheDir, "background")

                            if (file != null && bgCacheDir.exists() && file.exists() && FileUtils.getExtension(file) == "png") {
                                val destFile = File(bgCacheDir, file.name)

                                try {
                                    FileUtils.copyFile(file, destFile)
                                    backgroundManager.addCustomBackground(destFile)
                                } catch (e: IOException) {
                                    ShindoLogger.error(
                                        "An error occurred while copying the background file: " + file.name,
                                        e
                                    )
                                }
                            }
                        }
                    } else {
                        backgroundManager.setCurrentBackground(bg)
                    }
                }

                if (bg is CustomBackground && MouseUtils.isInside(
                        mouseX,
                        mouseY,
                        acX + offsetX + 98f,
                        acY + 35.5f + offsetY,
                        14f,
                        14f
                    )
                ) {
                    if (backgroundManager.getCurrentBackground() == bg) {
                        backgroundManager.setCurrentBackground(backgroundManager.getBackgroundById(0))
                    }

                    backgroundManager.removeCustomBackground(bg)
                }
            }

            offsetX += 115

            if (index % 2 == 0) {
                offsetY += 70
                offsetX = 0
            }

            index++
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            introAnimation.setDirection(Direction.BACKWARDS)
        }
    }
}

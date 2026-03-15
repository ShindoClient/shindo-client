package me.miki.shindo.gui.gamemenus.views


import me.miki.shindo.Shindo
import me.miki.shindo.gui.gamemenus.MenuManager
import me.miki.shindo.gui.gamemenus.ShindoScreen
import me.miki.shindo.management.color.palette.ColorPalette
import me.miki.shindo.management.file.FileManager
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.profile.mainmenu.BackgroundManager
import me.miki.shindo.management.profile.mainmenu.impl.CustomBackground
import me.miki.shindo.management.profile.mainmenu.impl.DefaultBackground
import me.miki.shindo.ui.animation.Animation
import me.miki.shindo.utils.Multithreading
import me.miki.shindo.utils.file.FileUtils
import me.miki.shindo.utils.mouse.MouseUtils
import me.miki.shindo.utils.mouse.Scroll
import net.minecraft.client.gui.ScaledResolution
import java.awt.Color
import java.io.File
import java.io.IOException

class BackgroundSelector(parent: MenuManager) : ShindoScreen(parent, "Select Background") {
    private val introAnimation: Animation? = null
    private val scroll: Scroll = Scroll()
    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val sr = ScaledResolution(mc)
        val instance: Shindo = Shindo.getInstance()
        val nvg: NanoVGManager = instance.nanoVGManager!!
        nvg.setupAndDraw(Runnable { drawNanoVG(mouseX, mouseY, sr, instance, nvg) })
    }

    private fun drawNanoVG(mouseX: Int, mouseY: Int, sr: ScaledResolution, instance: Shindo, nvg: NanoVGManager) {
        val backgroundManager: BackgroundManager = instance.profileManager.backgroundManager
        val palette: ColorPalette = instance.colorManager.getPalette()
        val acWidth = 240
        val acHeight = 148
        val acX: Int = sr.scaledWidth / 2 - acWidth / 2
        val acY: Int = sr.scaledHeight / 2 - acHeight / 2
        var offsetX = 0
        var offsetY = 0
        var index = 1
        var prevIndex = 1
        scroll.onScroll()
        scroll.onAnimation()
        nvg.drawCenteredText(
            TranslateText.SELECT_BACKGROUND.getText(),
            acX + acWidth / 2f,
            acY + 8f,
            Color.WHITE,
            14f,
            Fonts.SEMIBOLD
        )
        nvg.save()
        //nvg.scissor(acX, acY + 25, acWidth, acHeight - 25);
        nvg.translate(0f, scroll.getValue())
        for (bg in backgroundManager.getBackgrounds()) {
            val isSelected: Boolean = backgroundManager.getCurrentBackground()!! == bg
            val itemX = (acX + 11 + offsetX).toFloat()
            val itemY = (acY + 35 + offsetY).toFloat()
            val itemWidth = 102.5f
            val itemHeight = 57.5f

            // Draw selection highlight and glow effect
            if (isSelected) {
                // Outer glow
                nvg.drawGradientShadow(
                    itemX - 1f,
                    itemY - 1f,
                    itemWidth + 2f,
                    itemHeight + 2f,
                    7f,
                    Color(255, 255, 255, 180),
                    Color(255, 255, 255, 180)
                )
                // Inner highlight
                nvg.drawRoundedRect(itemX - 1, itemY - 1, itemWidth + 2, itemHeight + 2, 7f, Color(255, 255, 255, 180))
            }

            // Hover effect
            if (MouseUtils.isInside(mouseX, mouseY, itemX, itemY + scroll.getValue(), itemWidth, itemHeight)) {
                nvg.drawRoundedRect(itemX - 1, itemY - 1, itemWidth + 2, itemHeight + 2, 7f, Color(255, 255, 255, 100))
            }
            if (bg is DefaultBackground) {
                val defBackground: DefaultBackground = bg as DefaultBackground
                if (bg.getId() == 999) {
                    nvg.drawRoundedRect(acX + 11f + offsetX, acY + 35f + offsetY, 102.5f, 57.5f, 6f, Color.BLACK)
                    nvg.drawCenteredText(
                        LegacyIcon.PLUS,
                        acX + 10 + offsetX + 102.5f / 2,
                        acY + 42.5f + offsetY,
                        Color.WHITE,
                        26f,
                        Fonts.LEGACYICON
                    )
                } else {
                    nvg.drawRoundedImage(
                        defBackground.getImage(),
                        acX + 11f + offsetX,
                        acY + 35f + offsetY,
                        102.5f,
                        57.5f,
                        6f
                    )
                }
            }
            if (bg is CustomBackground) {
                val cusBackground: CustomBackground = bg as CustomBackground
                cusBackground.getTrashAnimation().setAnimation(
                    if (MouseUtils.isInside(
                            mouseX,
                            mouseY,
                            acX + 11f + offsetX,
                            acY + 35f + offsetY + scroll.getValue(),
                            102.5f,
                            57.5f
                        )
                    ) 1.0f else 0.0f, 16
                )
                nvg.drawRoundedImage(
                    cusBackground.getImage(),
                    acX + 11f + offsetX,
                    acY + 35f + offsetY,
                    102.5f,
                    57.5f,
                    6f
                )
                nvg.drawText(
                    LegacyIcon.TRASH,
                    acX + offsetX + 100f,
                    acY + 38f + offsetY,
                    palette.getMaterialRed((cusBackground.getTrashAnimation().value * 255).toInt()),
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
                this.getBackgroundColor()
            )
            nvg.drawCenteredText(
                bg.getName()!!,
                acX + offsetX + 11f + 102.5f / 2f,
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
        scroll.maxScroll = if (prevIndex == 1) 0f else offsetY - 70 / 1.56f - if (index % 2 == 1) 70 else 0
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        val sr = ScaledResolution(mc)
        val instance: Shindo = Shindo.getInstance()
        val fileManager: FileManager = instance.fileManager
        val backgroundManager: BackgroundManager = instance.profileManager.backgroundManager
        val acWidth = 240
        val acHeight = 148
        val acX: Int = sr.scaledWidth / 2 - acWidth / 2
        val acY: Int = sr.scaledHeight / 2 - acHeight / 2
        var offsetX = 0
        var offsetY = (0 + scroll.getValue()).toInt()
        var index = 1
        for (bg in backgroundManager.getBackgrounds()) {
            if (mouseButton == 0) {
                if (MouseUtils.isInside(mouseX, mouseY, acX + 11f + offsetX, acY + 35f + offsetY, 102.5f, 57.5f)) {
                    if (bg.getId() == 999) {
                        Multithreading.runAsync {
                            val file: File = FileUtils.selectImageFile()!!
                            val bgCacheDir: File = File(fileManager.cacheDir, "background")
                            if (bgCacheDir.exists() && file.exists() && FileUtils.getExtension(file)
                                    .equals("png")
                            ) {
                                val destFile = File(bgCacheDir, file.name)
                                try {
                                    FileUtils.copyFile(file, destFile)
                                    backgroundManager.addCustomBackground(destFile)
                                } catch (e: IOException) {
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
                    val cusBackground: CustomBackground = bg as CustomBackground
                    if (backgroundManager.getCurrentBackground()!! == cusBackground) {
                        backgroundManager.setCurrentBackground(backgroundManager.getBackgroundById(0))
                    }
                    backgroundManager.removeCustomBackground(cusBackground)
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
}
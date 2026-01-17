package me.miki.shindo.gui.mainmenu.impl

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
import me.miki.shindo.ui.frame.adapter.MainMenuSceneFrameAdapter
import me.miki.shindo.ui.frame.template.ConfigFrameTemplate
import me.miki.shindo.utils.Multithreading
import me.miki.shindo.utils.animation.normal.Animation
import me.miki.shindo.utils.animation.normal.Direction
import me.miki.shindo.utils.animation.normal.easing.EaseInOutCirc
import me.miki.shindo.utils.buffer.ScreenAnimation
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
    
    // Frame adapter para usar o sistema de frames
    private val frameAdapter = MainMenuSceneFrameAdapter(
        this,
        ConfigFrameTemplate,
        TranslateText.SELECT_BACKGROUND.text
    )

    override fun initScene() {
        introAnimation = EaseInOutCirc(250, 1.0)
        introAnimation.setDirection(Direction.FORWARDS)
        
        // Ajusta o tamanho do frame para ser menor (como era originalmente)
        val frame = frameAdapter.getFrame()
        frame.setSize(240f, 148f)
        val sr = ScaledResolution(mc)
        frame.setPosition(
            (sr.scaledWidth - 240f) / 2f,
            (sr.scaledHeight - 148f) / 2f
        )
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val sr = ScaledResolution(mc)
        val instance = Shindo.getInstance()
        val nvg = instance.nanoVGManager
        
        // Mantém a animação de entrada/saída usando ScreenAnimation
        screenAnimation.wrap(
            Runnable { 
                // Desenha o frame dentro da animação
                frameAdapter.draw(mouseX, mouseY, partialTicks)
                
                // Desenha o conteúdo customizado (grid de backgrounds)
                drawBackgroundGrid(mouseX, mouseY, sr, instance, nvg)
            },
            0, 0, sr.scaledWidth, sr.scaledHeight,
            2f - introAnimation.getValueFloat(),
            introAnimation.getValueFloat().coerceAtMost(1f),
            false
        )
        
        if (introAnimation.isDone(Direction.BACKWARDS)) {
            setCurrentScene(getSceneByClass(MainScene::class.java))
        }
    }
    
    private fun drawBackgroundGrid(
        mouseX: Int,
        mouseY: Int,
        sr: ScaledResolution,
        instance: Shindo,
        nvg: NanoVGManager?
    ) {
        val backgroundManager: BackgroundManager = instance.profileManager.backgroundManager
        val palette: ColorPalette = getMenuPalette()
        val controlColor = getControlColor()
        
        val frame = frameAdapter.getFrame()
        val container = frameAdapter.getContainer()
        val contentArea = container.getContentArea()
        
        val acX = frame.getX()
        val acY = frame.getY() + frame.getHeaderHeight() // Começa após o header
        val acWidth = frame.getWidth()
        val acHeight = frame.getHeight() - frame.getHeaderHeight()
        
        var offsetX = 0
        var offsetY = 0
        var index = 1
        var prevIndex = 1

        scroll.onScroll()
        scroll.onAnimation()

        // Área de scroll para os backgrounds
        nvg!!.save()
        nvg.scissor(acX, acY + 25f, acWidth, acHeight - 25f)
        nvg.translate(0f, scroll.getValue())

        for (bg in backgroundManager.backgrounds) {
            val isSelected = backgroundManager.currentBackground == bg
            val itemX = acX + 11f + offsetX
            val itemY = acY + 35f + offsetY
            val itemWidth = 102.5f
            val itemHeight = 57.5f

            if (isSelected) {
                nvg.drawGradientShadow(
                    itemX - 1, itemY - 1, itemWidth + 2, itemHeight + 2,
                    7f, Color(255, 255, 255, 180), Color(255, 255, 255, 180)
                )
                nvg.drawRoundedRect(
                    itemX - 1, itemY - 1, itemWidth + 2, itemHeight + 2,
                    7f, Color(255, 255, 255, 180)
                )
            }

            // O translate já move o conteúdo, então não precisa adicionar scroll.getValue() aqui
            // Mas precisamos ajustar o mouseY para considerar o translate
            val adjustedMouseYForHover = mouseY + scroll.getValue().toInt()
            if (MouseUtils.isInside(mouseX, adjustedMouseYForHover, itemX, itemY, itemWidth, itemHeight)) {
                nvg.drawRoundedRect(
                    itemX - 1, itemY - 1, itemWidth + 2, itemHeight + 2,
                    7f, Color(255, 255, 255, 100)
                )
            }

            if (bg is DefaultBackground) {
                if (bg.id == 999) {
                    nvg.drawRoundedRect(
                        acX + 11f + offsetX, acY + 35f + offsetY,
                        102.5f, 57.5f, 6f, Color.BLACK
                    )
                    nvg.drawCenteredText(
                        LegacyIcon.PLUS,
                        acX + 10f + offsetX + (102.5f / 2),
                        acY + 42.5f + offsetY,
                        Color.WHITE, 26f, Fonts.LEGACYICON
                    )
                } else {
                    nvg.drawRoundedImage(
                        bg.image,
                        acX + 11f + offsetX, acY + 35f + offsetY,
                        102.5f, 57.5f, 6f
                    )
                }
            }

            if (bg is CustomBackground) {
                // O translate já move o conteúdo, então não precisa adicionar scroll.getValue() aqui
                // Mas precisamos ajustar o mouseY para considerar o translate
                val adjustedMouseYForHover = mouseY + scroll.getValue().toInt()
                bg.trashAnimation.setAnimation(
                    if (MouseUtils.isInside(
                            mouseX, adjustedMouseYForHover,
                            acX + 11f + offsetX, acY + 35f + offsetY,
                            102.5f, 57.5f
                        )
                    ) 1.0f else 0.0f,
                    16
                )

                nvg.drawRoundedImage(
                    bg.image,
                    acX + 11f + offsetX, acY + 35f + offsetY,
                    102.5f, 57.5f, 6f
                )
                nvg.drawText(
                    LegacyIcon.TRASH,
                    acX + offsetX + 100f, acY + 38f + offsetY,
                    palette.getMaterialRed((bg.trashAnimation.value * 255).toInt()),
                    10f, Fonts.LEGACYICON
                )
            }

            if (bg is ShaderBackground) {
                ShaderBackgroundRenderer.renderShaderPreview(
                    nvg,
                    bg.shaderFile,
                    acX + 11f + offsetX,
                    acY + 35f + offsetY,
                    102.5f,
                    57.5f
                )
            }

            nvg.drawRoundedRectVarying(
                acX + offsetX + 11f, acY + offsetY + 76.5f,
                102.5f, 16f,
                0f, 0f, 6f, 6f,
                controlColor
            )
            nvg.drawCenteredText(
                bg.name,
                acX + offsetX + 11f + (102.5f / 2),
                acY + offsetY + 80f,
                Color.WHITE, 10f, Fonts.REGULAR
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

        val frame = frameAdapter.getFrame()
        val acX = frame.getX()
        val acY = frame.getY() + frame.getHeaderHeight()
        val acWidth = frame.getWidth()
        val acHeight = frame.getHeight() - frame.getHeaderHeight()
        
        // Ajusta o mouseY para considerar o translate do scroll
        // O translate move o conteúdo para cima, então precisamos ajustar o mouseY para baixo
        val scrollValue = scroll.getValue()
        val adjustedMouseY = mouseY + scrollValue.toInt()
        
        var offsetX = 0
        var offsetY = 0
        var index = 1

        // Processa eventos do frame primeiro
        frameAdapter.mouseClicked(mouseX, mouseY, mouseButton)

        // Se clicou fora do frame (mas não no botão de background), fecha
        if (!MouseUtils.isInside(mouseX, mouseY, acX, acY, acWidth, acHeight)
            && !MouseUtils.isInside(mouseX, mouseY, sr.scaledWidth - 56f, 6f, 22f, 22f)) {
            introAnimation.setDirection(Direction.BACKWARDS)
        }

        for (bg in backgroundManager.backgrounds) {
            if (mouseButton == 0) {
                // Usa as mesmas coordenadas que o desenho (sem o translate, pois já ajustamos o mouseY)
                val itemX = acX + 11f + offsetX
                val itemY = acY + 35f + offsetY
                
                if (MouseUtils.isInside(
                        mouseX, adjustedMouseY,
                        itemX, itemY,
                        102.5f, 57.5f
                    )
                ) {
                    if (bg.id == 999) {
                        Multithreading.runAsync {
                            val file = FileUtils.selectImageFile()
                            val bgCacheDir = File(fileManager.cacheDir, "background")

                            if (file != null && bgCacheDir.exists() && file.exists() && FileUtils.getExtension(file) == "png") {
                                val destFile = File(bgCacheDir, file.name)

                                try {
                                    FileUtils.copyFile(file, destFile)
                                    backgroundManager.addCustomBackground(destFile)
                                } catch (e: IOException) {
                                    ShindoLogger.error("An error occurred while copying the background file: " + file.name, e)
                                }
                            }
                        }
                    } else {
                        backgroundManager.currentBackground = bg
                    }
                }

                if (bg is CustomBackground && MouseUtils.isInside(
                        mouseX, adjustedMouseY,
                        acX + offsetX + 98f, acY + 35.5f + offsetY,
                        14f, 14f
                    )
                ) {
                    if (backgroundManager.currentBackground == bg) {
                        backgroundManager.currentBackground = backgroundManager.getBackgroundById(0)
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
        frameAdapter.keyTyped(typedChar, keyCode)
        
        if (keyCode == Keyboard.KEY_ESCAPE) {
            introAnimation.setDirection(Direction.BACKWARDS)
        }
    }
}

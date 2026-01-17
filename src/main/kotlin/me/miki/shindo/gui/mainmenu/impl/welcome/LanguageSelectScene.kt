package me.miki.shindo.gui.mainmenu.impl.welcome

import me.miki.shindo.Shindo
import me.miki.shindo.gui.mainmenu.GuiShindoMainMenu
import me.miki.shindo.gui.mainmenu.MainMenuScene
import me.miki.shindo.management.color.AccentColor
import me.miki.shindo.management.language.Language
import me.miki.shindo.management.language.LanguageManager
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.ui.comp.frame.CompFrameButton
import me.miki.shindo.ui.frame.adapter.MainMenuSceneFrameAdapter
import me.miki.shindo.ui.frame.template.WelcomeFrameTemplate
import me.miki.shindo.utils.animation.normal.Animation
import me.miki.shindo.utils.animation.normal.Direction
import me.miki.shindo.utils.animation.normal.other.DecelerateAnimation
import me.miki.shindo.utils.buffer.ScreenAlpha
import me.miki.shindo.utils.mouse.MouseUtils
import me.miki.shindo.utils.mouse.Scroll
import me.miki.shindo.utils.render.BlurUtils
import net.minecraft.client.gui.ScaledResolution
import java.awt.Color

class LanguageSelectScene(parent: GuiShindoMainMenu) : MainMenuScene(parent) {

    private val screenAlpha = ScreenAlpha()
    private val scroll = Scroll()
    private val languageManager: LanguageManager = Shindo.getInstance().languageManager
    private var fadeAnimation: Animation? = null
    private var currentLanguage: Language = languageManager.currentLanguage
    private lateinit var nextButton: CompFrameButton
    
    // Frame adapter
    private val frameAdapter = MainMenuSceneFrameAdapter(
        this,
        WelcomeFrameTemplate,
        "Choose a Language"
    )

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val sr = ScaledResolution(mc)

        if (fadeAnimation == null) {
            fadeAnimation = DecelerateAnimation(800, 1.0)
            fadeAnimation!!.setDirection(Direction.FORWARDS)
            fadeAnimation!!.reset()
        }

        BlurUtils.drawBlurScreen(14F)

        screenAlpha.wrap(Runnable { drawNanoVG() }, fadeAnimation!!.getValueFloat())

        if (fadeAnimation!!.isDone(Direction.BACKWARDS)) {
            setCurrentScene(getSceneByClass(ThemeSelectScene::class.java))
        }
    }

    private fun drawNanoVG() {
        val instance = Shindo.getInstance()
        val nvg: NanoVGManager = instance.nanoVGManager!!
        val currentColor: AccentColor = instance.colorManager.currentColor
        val frame = frameAdapter.getFrame()
        
        // Ajusta tamanho do frame para o tamanho original
        frame.setSize(280f, 146f)
        val sr = ScaledResolution(mc)
        frame.setPosition(
            (sr.scaledWidth - 280f) / 2f,
            (sr.scaledHeight - 146f) / 2f
        )
        
        val x = frame.getX().toInt()
        val y = frame.getY().toInt()
        val width = frame.getWidth().toInt()
        val height = frame.getHeight().toInt()

        var offsetX = 0
        var index = 1

        val controlColor = getControlColor()
        
        // Desenha o frame (header e container)
        frameAdapter.draw(0, 0, 0f)

        scroll.onScroll()
        scroll.onAnimation()

        // Área de scroll para os idiomas usando o método auxiliar do frame
        // Isso garante que o translate e scissor funcionem corretamente juntos
        frame.drawInContainer(scroll.getValue(), 0f) { nvgInstance ->
            val container = frameAdapter.getContainer()
            val contentArea = container.getContentArea()
            
            // Desenha os idiomas dentro da área de conteúdo
            for (lang in Language.entries) {
                val flagX = contentArea.x + offsetX + 14f
                val flagY = contentArea.y + 27f
                
                nvgInstance.drawRoundedImage(
                    lang.flag,
                    flagX,
                    flagY,
                    90f,
                    56f,
                    4f
                )
                nvgInstance.drawCenteredText(
                    lang.name,
                    flagX + (90 / 2f),
                    flagY + 62f, // 27f + 35f (meio da flag) + offset para texto
                    Color.WHITE,
                    7f,
                    Fonts.REGULAR
                )
                if (lang == currentLanguage) {
                    nvgInstance.drawGradientOutlineRoundedRect(
                        flagX,
                        flagY,
                        90f,
                        56f,
                        6f,
                        2f,
                        currentColor.color1,
                        currentColor.color2
                    )
                }
                offsetX += 102
                index++
            }
        }

        scroll.maxScroll = (index - 3.58f) * 102

        // Botão Next
        if (!::nextButton.isInitialized) {
            nextButton = CompFrameButton(
                frame.getX() + frame.getWidth() - 86f,
                frame.getY() + frame.getHeight() - 26f,
                80f,
                20f,
                "Next"
            )
            nextButton.onClick = {
                Shindo.getInstance().languageManager.currentLanguage = currentLanguage
                fadeAnimation!!.setDirection(Direction.BACKWARDS)
            }
            frameAdapter.attachToFrame(nextButton)
        }
        
        nextButton.setBounds(
            frame.getX() + frame.getWidth() - 86f,
            frame.getY() + frame.getHeight() - 26f,
            80f,
            20f
        )
        nextButton.draw(0, 0, 0f)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        val frame = frameAdapter.getFrame()
        val container = frameAdapter.getContainer()
        val contentArea = container.getContentArea()
        
        // Processa eventos do frame primeiro
        frameAdapter.mouseClicked(mouseX, mouseY, mouseButton)

        // Ajusta as coordenadas do mouse para considerar o translate do scroll
        // O translate é aplicado no desenho, então precisamos desfazê-lo aqui
        val scrollValue = scroll.getValue()
        val adjustedMouseX = mouseX - scrollValue.toInt()
        
        var offsetX = 0
        for (lang in Language.entries) {
            // Usa as mesmas coordenadas que o desenho (contentArea + offsetX + padding)
            val flagX = contentArea.x + offsetX + 14f
            val flagY = contentArea.y + 27f
            
            if (MouseUtils.isInside(
                    adjustedMouseX, mouseY,
                    flagX, flagY,
                    90f, 56f
                ) && mouseButton == 0
            ) {
                currentLanguage = lang
            }

            offsetX += 102
        }
    }
}

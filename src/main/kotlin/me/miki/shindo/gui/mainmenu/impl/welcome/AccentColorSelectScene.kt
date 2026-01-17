package me.miki.shindo.gui.mainmenu.impl.welcome

import me.miki.shindo.Shindo
import me.miki.shindo.gui.mainmenu.GuiShindoMainMenu
import me.miki.shindo.gui.mainmenu.MainMenuScene
import me.miki.shindo.management.color.AccentColor
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.ui.comp.selectors.CompAccentColorSelectorWelcome
import me.miki.shindo.ui.comp.buttons.CompActionButton
import me.miki.shindo.ui.frame.adapter.MainMenuSceneFrameAdapter
import me.miki.shindo.ui.frame.template.WelcomeFrameTemplate
import me.miki.shindo.utils.ColorUtils
import me.miki.shindo.utils.animation.normal.Animation
import me.miki.shindo.utils.animation.normal.Direction
import me.miki.shindo.utils.animation.normal.other.DecelerateAnimation
import me.miki.shindo.utils.buffer.ScreenAlpha
import me.miki.shindo.utils.render.BlurUtils
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.util.ResourceLocation
import java.awt.Color

class AccentColorSelectScene(parent: GuiShindoMainMenu) : MainMenuScene(parent) {

    private val screenAlpha = ScreenAlpha()
    private var fadeAnimation: Animation? = null
    private lateinit var colorSelector: CompAccentColorSelectorWelcome
    private lateinit var nextButton: CompActionButton
    
    // Frame adapter
    private val frameAdapter = MainMenuSceneFrameAdapter(
        this,
        WelcomeFrameTemplate,
        "Choose a accent color"
    )

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val sr = ScaledResolution(mc)

        if (fadeAnimation == null) {
            fadeAnimation = DecelerateAnimation(800, 1.0)
            fadeAnimation!!.setDirection(Direction.FORWARDS)
            fadeAnimation!!.reset()
        }

        BlurUtils.drawBlurScreen(14F)

        screenAlpha.wrap(Runnable { 
            drawNanoVG(mouseX, mouseY, partialTicks)
        }, fadeAnimation!!.getValueFloat())

        if (fadeAnimation!!.isDone(Direction.BACKWARDS)) {
            setCurrentScene(getSceneByClass(CheckingDataScene::class.java))
        }
    }

    private fun drawNanoVG(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val instance = Shindo.getInstance()
        val nvg = instance.nanoVGManager!!
        val colorManager = instance.colorManager
        val currentColor = colorManager.currentColor
        val frame = frameAdapter.getFrame()
        
        // Ajusta tamanho do frame para acomodar o preview sem sobrepor o seletor
        // Aumentamos a largura para dar mais espaço e a altura para centralizar o preview
        frame.setSize(360f, 200f)
        val sr = ScaledResolution(mc)
        frame.setPosition(
            (sr.scaledWidth - 360f) / 2f,
            (sr.scaledHeight - 200f) / 2f
        )

        // Inicializa componentes se necessário
        if (!::colorSelector.isInitialized) {
            val contentArea = frameAdapter.getContainer().getContentArea()
            
            colorSelector = CompAccentColorSelectorWelcome(
                contentArea.x,
                contentArea.y + 13f,
                (frame.getWidth() - 130f), // Largura menos espaço para preview (96f + 20f margem + 14f padding)
                (frame.getHeight() - 82f), // Altura menos botão (sem header)
                colorManager.colors
            ).apply {
                setSelectedColor(currentColor)
                setOnColorSelected { accent ->
                    instance.colorManager.currentColor = accent
                }
            }
            frameAdapter.attachToFrame(colorSelector)
            
            nextButton = CompActionButton(
                "Next",
                frame.getX() + frame.getWidth() - 108f,
                frame.getY() + frame.getHeight() - 26f,
                96f,
                20f
            )
            nextButton.onClick = {
                fadeAnimation!!.setDirection(Direction.BACKWARDS)
            }
            frameAdapter.attachToFrame(nextButton)
        }

        // Atualiza posições
        val contentArea = frameAdapter.getContainer().getContentArea()
        colorSelector.setBounds(
            contentArea.x,
            contentArea.y + 13f,
            (frame.getWidth() - 130f), // Largura menos espaço para preview (96f + 20f margem + 14f padding)
            (frame.getHeight() - 82f) // Altura menos botão (sem header)
        )
        nextButton.setBounds(
            frame.getX() + frame.getWidth() - 108f,
            frame.getY() + frame.getHeight() - 26f,
            96f,
            20f
        )
        colorSelector.setSelectedColor(currentColor)

        // Desenha o frame (header e container)
        frameAdapter.draw(mouseX, mouseY, partialTicks)

        // Preview do HUD (customizado, desenha sobre o frame)
        // Posiciona o preview à direita, centralizado verticalmente com a altura do frame
        val previewSize = 96f
        val previewX = frame.getX() + frame.getWidth() - previewSize - 20f // 20f de margem da direita
        val previewY = frame.getY() + (frame.getHeight() - previewSize) / 2f // Centralizado verticalmente
        nvg.drawRoundedImage(
            ResourceLocation("shindo/backgrounds/example-vertical.png"),
            previewX,
            previewY,
            previewSize,
            previewSize,
            6f
        )
        drawExampleHud(
            frame.getX() + frame.getWidth() - previewSize - 8f, // Centralizado horizontalmente no preview
            previewY + 30.5f, // Mantém a posição relativa do HUD dentro do preview
            currentColor
        )
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        frameAdapter.mouseClicked(mouseX, mouseY, mouseButton)
    }

    private fun drawExampleHud(x: Float, y: Float, accentColor: AccentColor) {
        val nvg = Shindo.getInstance().nanoVGManager

        val width = 71f
        val height = 34f

        nvg!!.drawGradientRoundedRect(
            x, y, width, height, 5f,
            ColorUtils.applyAlpha(accentColor.color1, 220),
            ColorUtils.applyAlpha(accentColor.color2, 220)
        )

        nvg.drawText("X: 190", x + 3.9f, y + 3.9f, Color.WHITE, 6.42f, Fonts.REGULAR)
        nvg.drawText("Y: 60", x + 3.9f, y + 10.9f, Color.WHITE, 6.42f, Fonts.REGULAR)
        nvg.drawText("Z: 20", x + 3.9f, y + 17.9f, Color.WHITE, 6.42f, Fonts.REGULAR)
        nvg.drawText("Biome: Plains", x + 3.9f, y + 24.9f, Color.WHITE, 6.42f, Fonts.REGULAR)
    }
}

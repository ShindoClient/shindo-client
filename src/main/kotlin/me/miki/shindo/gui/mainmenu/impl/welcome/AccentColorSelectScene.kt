package me.miki.shindo.gui.mainmenu.impl.welcome

import me.miki.shindo.Shindo
import me.miki.shindo.gui.mainmenu.GuiShindoMainMenu
import me.miki.shindo.gui.mainmenu.MainMenuScene
import me.miki.shindo.management.color.AccentColor
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.ui.comp.selectors.CompAccentColorSelectorWelcome
import me.miki.shindo.ui.comp.buttons.CompActionButton
import me.miki.shindo.ui.comp.templates.CompPanel
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
    private var x = 0
    private var y = 0
    private var width = 0
    private var height = 0
    private var fadeAnimation: Animation? = null
    private lateinit var colorSelector: CompAccentColorSelectorWelcome
    private lateinit var nextButton: CompActionButton
    private lateinit var panel: CompPanel

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val sr = ScaledResolution(mc)

        width = 280
        height = 172
        x = sr.scaledWidth / 2 - (width / 2)
        y = sr.scaledHeight / 2 - (height / 2)

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

        // Inicializa componentes se necessário
        if (!::panel.isInitialized) {
            panel = CompPanel(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat())
                .setRadius(8f)
                .setBackgroundColor(getPanelColor())
                .setShadowEnabled(false)
            
            colorSelector = CompAccentColorSelectorWelcome(
                0f, 0f,
                (width - 118f).toFloat(), // Largura menos espaço para preview
                (height - 82f).toFloat(), // Altura menos header e botão
                colorManager.colors
            ).apply {
                setSelectedColor(currentColor)
                setOnColorSelected { accent ->
                    instance.colorManager.currentColor = accent
                }
            }
            
            nextButton = CompActionButton("Next", x + width - 108f, y + height - 26f, 96f, 20f)
            nextButton.onClick = {
                fadeAnimation!!.setDirection(Direction.BACKWARDS)
            }
        }

        // Atualiza posições
        panel.setBounds(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat())
        colorSelector.setBounds(x.toFloat() + 10f, y.toFloat() + 40f, (width - 118f).toFloat(), (height - 82f).toFloat())
        nextButton.setBounds(x + width - 108f, y + height - 26f, 96f, 20f)
        colorSelector.setSelectedColor(currentColor)

        // Desenha título
        nvg.drawCenteredText("Choose a accent color", x + (width / 2f), y + 10f, Color.WHITE, 16f, Fonts.MEDIUM)
        nvg.drawRect(x.toFloat(), y + 27f, width.toFloat(), 1f, Color.WHITE)

        // Preview do HUD
        nvg.drawRoundedImage(ResourceLocation("shindo/backgrounds/example-vertical.png"), x + width - 108f, y + 40f, 96f, 96f, 6f)
        drawExampleHud(x + width - 96f, y + 70.5f, currentColor)

        // Desenha componentes
        panel.draw(mouseX, mouseY, partialTicks)
        colorSelector.draw(mouseX, mouseY, partialTicks)
        nextButton.draw(mouseX, mouseY, partialTicks)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (::colorSelector.isInitialized) {
            colorSelector.mouseClicked(mouseX, mouseY, mouseButton)
        }
        if (::nextButton.isInitialized) {
            nextButton.mouseClicked(mouseX, mouseY, mouseButton)
        }
    }

    private fun drawExampleHud(x: Float, y: Float, accentColor: AccentColor) {
        val nvg = Shindo.getInstance().nanoVGManager

        val width = 71f
        val height = 34f

        nvg!!.drawGradientRoundedRect(x, y, width, height, 5f, ColorUtils.applyAlpha(accentColor.color1, 220), ColorUtils.applyAlpha(accentColor.color2, 220))

        nvg.drawText("X: 190", x + 3.9f, y + 3.9f, Color.WHITE, 6.42f, Fonts.REGULAR)
        nvg.drawText("Y: 60", x + 3.9f, y + 10.9f, Color.WHITE, 6.42f, Fonts.REGULAR)
        nvg.drawText("Z: 20", x + 3.9f, y + 17.9f, Color.WHITE, 6.42f, Fonts.REGULAR)
        nvg.drawText("Biome: Plains", x + 3.9f, y + 24.9f, Color.WHITE, 6.42f, Fonts.REGULAR)
    }
}

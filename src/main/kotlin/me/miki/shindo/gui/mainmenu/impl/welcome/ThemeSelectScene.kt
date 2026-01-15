package me.miki.shindo.gui.mainmenu.impl.welcome

import me.miki.shindo.Shindo
import me.miki.shindo.gui.mainmenu.GuiShindoMainMenu
import me.miki.shindo.gui.mainmenu.MainMenuScene
import me.miki.shindo.management.color.Theme
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.ui.comp.buttons.CompActionButton
import me.miki.shindo.ui.comp.selectors.CompThemeSelectorWelcome
import me.miki.shindo.ui.comp.templates.CompPanel
import me.miki.shindo.utils.animation.normal.Animation
import me.miki.shindo.utils.animation.normal.Direction
import me.miki.shindo.utils.animation.normal.other.DecelerateAnimation
import me.miki.shindo.utils.buffer.ScreenAlpha
import me.miki.shindo.utils.mouse.MouseUtils
import me.miki.shindo.utils.render.BlurUtils
import net.minecraft.client.gui.ScaledResolution
import java.awt.Color

class ThemeSelectScene(parent: GuiShindoMainMenu) : MainMenuScene(parent) {

    private val screenAlpha = ScreenAlpha()
    private var x = 0
    private var y = 0
    private var width = 0
    private var height = 0
    private var fadeAnimation: Animation? = null
    private lateinit var themeSelector: CompThemeSelectorWelcome
    private lateinit var nextButton: CompActionButton
    private lateinit var panel: CompPanel

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val sr = ScaledResolution(mc)

        width = 280
        height = 146
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
            setCurrentScene(getSceneByClass(AccentColorSelectScene::class.java))
        }
    }

    private fun drawNanoVG(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val instance = Shindo.getInstance()
        val nvg: NanoVGManager = instance.nanoVGManager!!
        val currentTheme = instance.colorManager.theme

        // Inicializa componentes se necessário
        if (!::panel.isInitialized) {
            panel = CompPanel(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat())
                .setRadius(8f)
                .setBackgroundColor(getPanelColor())
                .setShadowEnabled(false)
            
            themeSelector = CompThemeSelectorWelcome(0f, 0f, width.toFloat() - 36f, 88f).apply {
                setSelectedTheme(currentTheme)
                setOnThemeSelected { theme ->
                    instance.colorManager.theme = theme
                }
            }
            
            nextButton = CompActionButton("Next", x + width - 86f, y + height - 26f, 80f, 20f)
            nextButton.onClick = {
                fadeAnimation!!.setDirection(Direction.BACKWARDS)
            }
        }

        // Atualiza posições
        panel.setBounds(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat())
        themeSelector.setBounds(x.toFloat() + 18f, y.toFloat() + 42f, width.toFloat() - 36f, 88f)
        nextButton.setBounds(x + width - 86f, y + height - 26f, 80f, 20f)
        themeSelector.setSelectedTheme(instance.colorManager.theme)

        // Desenha título
        nvg.drawCenteredText("Choose a theme", x + (width / 2f), y + 10f, Color.WHITE, 16f, Fonts.MEDIUM)
        nvg.drawRect(x.toFloat(), y + 27f, width.toFloat(), 1f, Color.WHITE)

        // Desenha componentes
        if (::panel.isInitialized) {
            panel.draw(mouseX, mouseY, partialTicks)
        }
        if (::themeSelector.isInitialized) {
            themeSelector.draw(mouseX, mouseY, partialTicks)
        }
        if (::nextButton.isInitialized) {
            nextButton.draw(mouseX, mouseY, partialTicks)
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (::themeSelector.isInitialized) {
            themeSelector.mouseClicked(mouseX, mouseY, mouseButton)
        }
        if (::nextButton.isInitialized) {
            nextButton.mouseClicked(mouseX, mouseY, mouseButton)
        }
    }
}

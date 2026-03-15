package me.miki.shindo.gui.gamemenus


import me.miki.shindo.Shindo
import me.miki.shindo.gui.gamemenus.backgrounds.BackgroundsHandler
import me.miki.shindo.gui.gamemenus.views.BackgroundSelector
import me.miki.shindo.gui.gamemenus.views.MainMenuClassic
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.LegacyIcon
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import java.awt.Color
import java.io.IOException

class MenuManager : GuiScreen() {
    private var currentView: ShindoScreen
    var backgroundsHandler: BackgroundsHandler = BackgroundsHandler()
    private val views: ArrayList<ShindoScreen> = ArrayList<ShindoScreen>()

    override fun updateScreen() {
        backgroundsHandler.update(width.toFloat(), height.toFloat())
    }

    override fun initGui() {
        currentView.initGui()
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val sr = ScaledResolution(mc)
        val instance: Shindo = Shindo.getInstance()
        val nvg: NanoVGManager = instance.nanoVGManager!!
        instance.shindoAPI.isFirstLogin()
        backgroundsHandler.draw(sr, instance, nvg, partialTicks)
        nvg.setupAndDraw(Runnable { drawNanoVG(sr, instance, nvg, mouseX, mouseY) })
        if (currentView != null) {
            currentView.drawScreen(mouseX, mouseY, partialTicks)
        }

        // add splash stuff
        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    private fun drawNanoVG(sr: ScaledResolution, instance: Shindo, nvg: NanoVGManager, mouseX: Int, mouseY: Int) {
        drawMenuBar(mouseX, mouseY, sr, nvg)
    }

    private fun drawMenuBar(mouseX: Int, mouseY: Int, sr: ScaledResolution, nvg: NanoVGManager) {

        // draw logo
        nvg.drawText(LegacyIcon.SHINDO, 10f, 10f, Color.WHITE, 18f, Fonts.LEGACYICON)
        // menu title
        nvg.drawText(currentView.getMenuName(), 32f, 12f, Color.WHITE, 15f, Fonts.MEDIUM)
    }

    private fun drawMenuButtons(mouseX: Int, mouseY: Int, sr: ScaledResolution, nvg: NanoVGManager) {}
    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        ScaledResolution(mc)
        val instance: Shindo = Shindo.getInstance()
        instance.nanoVGManager!!
        val isFirstLogin: Boolean = instance.shindoAPI.isFirstLogin()
        if (mouseButton == 0 && !isFirstLogin) {

            // mouse inside logic for buttons
        }
        currentView.mouseClicked(mouseX, mouseY, mouseButton)
        try {
            super.mouseClicked(mouseX, mouseY, mouseButton)
        } catch (ignored: IOException) {
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, mouseButton: Int) {
        currentView.mouseReleased(mouseX, mouseY, mouseButton)
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        currentView.keyTyped(typedChar, keyCode)
    }

    @Throws(IOException::class)
    override fun handleInput() {
        super.handleInput()
    }

    override fun onGuiClosed() {
        currentView.onGuiClosed()
    }

    fun getCurrentView(): ShindoScreen {
        return currentView
    }

    fun setCurrentView(currentView: ShindoScreen) {
        if (this.currentView != null) {
            this.currentView.onSceneClosed()
        }
        this.currentView = currentView
        if (this.currentView != null) {
            this.currentView.initScene()
        }
    }

    fun addViews() {
        views.add(BackgroundSelector(this))
        views.add(MainMenuClassic(this))
    }

    fun getViewByClass(clazz: Class<out ShindoScreen>): ShindoScreen? {
        for (v in views) {
            if (v.javaClass == clazz) {
                return v
            }
        }
        return null
    }

    val backgroundColor: Color
        get() = Color(230, 230, 230, 120)

    init {
        val instance: Shindo = Shindo.getInstance()
        backgroundsHandler = BackgroundsHandler()
        addViews()

        // add curent scene setting
        currentView = getViewByClass(BackgroundSelector::class.java)!!
    }
}
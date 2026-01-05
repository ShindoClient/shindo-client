package me.miki.shindo.gui.mainmenu

import me.miki.shindo.Shindo
import me.miki.shindo.management.color.AccentColor
import me.miki.shindo.management.color.ColorManager
import me.miki.shindo.management.color.palette.ColorPalette
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.utils.animation.simple.SimpleAnimation
import net.minecraft.client.Minecraft
import java.awt.Color

open class MainMenuScene(private val parent: GuiShindoMainMenu) {

    val animation = SimpleAnimation()

    val mc: Minecraft = Minecraft.getMinecraft()

    open fun initScene() {
    }

    open fun initGui() {
    }

    open fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
    }

    open fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
    }

    open fun keyTyped(typedChar: Char, keyCode: Int) {
    }

    open fun mouseReleased(mouseX: Int, mouseY: Int, mouseButton: Int) {
    }

    open fun handleInput() {
    }

    open fun onGuiClosed() {
    }

    open fun onSceneClosed() {
    }

    fun setCurrentScene(scene: MainMenuScene?) {
        parent.setCurrentScene(scene)
    }

    fun getBackgroundColor(): Color {
        return getMenuPalette().getBackgroundColor(ColorType.DARK)
    }

    protected fun getPanelColor(): Color {
        return getMenuPalette().getBackgroundColor(ColorType.MID)
    }

    protected fun getControlColor(): Color {
        return getMenuPalette().getBackgroundColor(ColorType.NORMAL)
    }

    protected fun getMenuPalette(): ColorPalette {
        val manager = getColorManager()
        return manager?.palette ?: FALLBACK_PALETTE
    }

    protected fun getMenuAccent(): AccentColor {
        val manager = getColorManager()
        val accent = manager?.currentColor
        return accent ?: FALLBACK_ACCENT
    }

    private fun getColorManager(): ColorManager? {
        val instance = Shindo.getInstance()
        return instance?.colorManager
    }

    fun getSceneByClass(clazz: Class<out MainMenuScene>): MainMenuScene? {
        return parent.getSceneByClass(clazz)
    }

    fun getParent(): GuiShindoMainMenu {
        return parent
    }

    companion object {
        private val FALLBACK_PALETTE = ColorPalette()
        private val FALLBACK_ACCENT = AccentColor(
            "Default",
            Color(170, 255, 169),
            Color(17, 255, 189)
        )
    }
}

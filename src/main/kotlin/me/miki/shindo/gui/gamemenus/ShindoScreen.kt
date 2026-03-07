package me.miki.shindo.gui.gamemenus


import me.miki.shindo.ui.animation.value.SimpleAnimation
import net.minecraft.client.Minecraft
import java.awt.Color

open class ShindoScreen(manager: MenuManager, menuName: String) {
    var mc: Minecraft = Minecraft.getMinecraft()
    private val menuManager: MenuManager
    private val animation: SimpleAnimation = SimpleAnimation()
    private var menuName = ""


    open fun initScene() {}
    open fun initGui() {}
    open fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {}
    open fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {}
    open fun keyTyped(typedChar: Char, keyCode: Int) {}
    open fun mouseReleased(mouseX: Int, mouseY: Int, mouseButton: Int) {}
    open fun handleInput() {}
    open fun onGuiClosed() {}
    open fun onSceneClosed() {}


    fun getMenuManager(): MenuManager {
        return menuManager
    }

    fun setCurrentView(view: ShindoScreen) {
        menuManager.setCurrentView(view)
    }

    open fun getBackgroundColor(): Color {
        return menuManager.backgroundColor
    }

    fun getViewByClass(clazz: Class<out ShindoScreen>): ShindoScreen {
        return menuManager.getViewByClass(clazz)!!
    }

    open fun getMenuName(): String {
        return menuName
    }

    init {
        this.menuManager = manager;
        this.menuName = menuName;
    }
}
package me.miki.shindo.gui.modmenu.v2.category

import me.miki.shindo.gui.modmenu.v2.GuiModMenu
import me.miki.shindo.gui.modmenu.v2.category.section.CategorySectionCursor
import me.miki.shindo.gui.modmenu.v2.category.section.CategorySectionRenderer
import me.miki.shindo.gui.modmenu.v2.category.section.CategorySectionSpec
import me.miki.shindo.gui.modmenu.v2.category.section.CategorySectionStyle
import me.miki.shindo.management.color.palette.ColorPalette
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.ui.animation.v2.value.ColorAnimation
import me.miki.shindo.ui.animation.v2.value.SimpleAnimation
import me.miki.shindo.ui.components.v2.inputs.CompSearchBox
import me.miki.shindo.utils.mouse.Scroll
import net.minecraft.client.Minecraft

open class Category(
    @JvmField val parent: GuiModMenu,
    private val nameTranslate: TranslateText,
    private val icon: String,
    private val showSearchBox: Boolean,
    private val showTitle: Boolean
) {
    private val textAnimation = SimpleAnimation()
    private val textColorAnimation = ColorAnimation()
    private val categoryAnimation = SimpleAnimation()

    val mc: Minecraft = Minecraft.getMinecraft()

    @JvmField var scroll: Scroll = parent.getScroll()

    private var initialized = false

    open fun initGui() {}
    open fun initCategory() {}
    open fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {}
    open fun prepareFrame(mouseX: Int, mouseY: Int, partialTicks: Float) {}
    open fun renderFrame(mouseX: Int, mouseY: Int, partialTicks: Float) = drawScreen(mouseX, mouseY, partialTicks)
    open fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {}
    open fun handleMouseClick(mouseX: Int, mouseY: Int, mouseButton: Int) = mouseClicked(mouseX, mouseY, mouseButton)
    open fun mouseReleased(mouseX: Int, mouseY: Int, mouseButton: Int) {}
    open fun handleMouseRelease(mouseX: Int, mouseY: Int, mouseButton: Int) = mouseReleased(mouseX, mouseY, mouseButton)
    open fun keyTyped(typedChar: Char, keyCode: Int) {}
    open fun handleKeyInput(typedChar: Char, keyCode: Int) = keyTyped(typedChar, keyCode)
    open fun isAnySceneOpen(): Boolean = false

    fun getName(): String = nameTranslate.getText()
    fun getNameKey(): String = nameTranslate.getKey()
    fun getIcon(): String = icon
    fun isShowSearchBox(): Boolean = showSearchBox
    fun isShowTitle(): Boolean = showTitle
    fun isInitialized(): Boolean = initialized
    fun setInitialized(value: Boolean) { initialized = value }

    fun getX(): Int = parent.getX() + 32
    fun getY(): Int = parent.getY() + if (showTitle) 31 else 0
    fun getWidth(): Int = parent.getWidth() - 32
    fun getHeight(): Int = parent.getHeight() - if (showTitle) 31 else 0

    fun getTextAnimation(): SimpleAnimation = textAnimation
    fun getTextColorAnimation(): ColorAnimation = textColorAnimation
    fun getCategoryAnimation(): SimpleAnimation = categoryAnimation

    fun getSearchBox(): CompSearchBox = parent.getSearchBox()
    fun isCanClose(): Boolean = parent.isCanClose()
    fun setCanClose(canClose: Boolean) = parent.setCanClose(canClose)

    protected fun createSectionCursor(startY: Float, style: CategorySectionStyle = CategorySectionStyle()) =
        CategorySectionCursor(startY, style)

    protected fun drawSectionHeader(
        nvg: NanoVGManager,
        palette: ColorPalette,
        x: Float,
        cursor: CategorySectionCursor,
        section: CategorySectionSpec
    ): Float {
        val nextY = CategorySectionRenderer.drawHeader(nvg, palette, x, cursor.y, section, cursor.style)
        cursor.y = nextY
        return nextY
    }
}
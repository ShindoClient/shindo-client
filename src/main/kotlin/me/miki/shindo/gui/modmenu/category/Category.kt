package me.miki.shindo.gui.modmenu.category

import me.miki.shindo.gui.modmenu.GuiModMenu
import me.miki.shindo.gui.modmenu.category.section.CategorySectionCursor
import me.miki.shindo.gui.modmenu.category.section.CategorySectionRenderer
import me.miki.shindo.gui.modmenu.category.section.CategorySectionSpec
import me.miki.shindo.gui.modmenu.category.section.CategorySectionStyle
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.color.palette.ColorPalette
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.ui.comp.inputs.CompSearchBox
import me.miki.shindo.ui.animation.value.ColorAnimation
import me.miki.shindo.ui.animation.value.SimpleAnimation
import me.miki.shindo.utils.mouse.Scroll
import net.minecraft.client.Minecraft

open class Category(
    private val parent: GuiModMenu,
    private val nameTranslate: TranslateText,
    private val icon: String,
    private val showSearchBox: Boolean,
    private val showTitle: Boolean
) {

    private val textAnimation = SimpleAnimation()
    private val textColorAnimation = ColorAnimation()
    private val categoryAnimation = SimpleAnimation()

    val mc: Minecraft = Minecraft.getMinecraft()

    @JvmField
    var scroll: Scroll = parent.getScroll()

    private var initialized = false

    open fun initGui() {
    }

    open fun initCategory() {
    }

    open fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
    }

    open fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
    }

    open fun mouseReleased(mouseX: Int, mouseY: Int, mouseButton: Int) {
    }

    open fun keyTyped(typedChar: Char, keyCode: Int) {
    }

    fun getName(): String {
        return nameTranslate.getText()
    }

    fun getNameKey(): String {
        return nameTranslate.getKey()
    }

    fun getIcon(): String {
        return icon
    }

    fun getX(): Int {
        return parent.getX() + 32
    }

    fun getY(): Int {
        val yOff = if (showTitle) 31 else 0
        return parent.getY() + yOff
    }

    fun getWidth(): Int {
        return parent.getWidth() - 32
    }

    fun getHeight(): Int {
        val yOff = if (showTitle) 31 else 0
        return parent.getHeight() - yOff
    }

    fun getTextColorAnimation(): ColorAnimation {
        return textColorAnimation
    }

    fun getTextAnimation(): SimpleAnimation {
        return textAnimation
    }

    fun isInitialized(): Boolean {
        return initialized
    }

    fun setInitialized(initialized: Boolean) {
        this.initialized = initialized
    }

    fun getCategoryAnimation(): SimpleAnimation {
        return categoryAnimation
    }

    fun isShowSearchBox(): Boolean {
        return showSearchBox
    }

    fun isShowTitle(): Boolean {
        return showTitle
    }

    fun getSearchBox(): CompSearchBox {
        return parent.getSearchBox()
    }

    fun isCanClose(): Boolean {
        return parent.isCanClose()
    }

    fun setCanClose(canClose: Boolean) {
        parent.setCanClose(canClose)
    }

    protected fun createSectionCursor(
        startY: Float,
        style: CategorySectionStyle = CategorySectionStyle()
    ): CategorySectionCursor {
        return CategorySectionCursor(startY, style)
    }

    protected fun drawSectionHeader(
        nvg: NanoVGManager,
        palette: ColorPalette,
        x: Float,
        cursor: CategorySectionCursor,
        section: CategorySectionSpec
    ): Float {
        val nextY = CategorySectionRenderer.drawHeader(
            nvg,
            palette,
            x,
            cursor.y,
            section,
            cursor.style
        )
        cursor.y = nextY
        return nextY
    }
}

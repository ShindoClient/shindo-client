package me.miki.shindo.gui.modmenu.category.impl.setting

import me.miki.shindo.gui.modmenu.category.impl.SettingsCategory
import me.miki.shindo.management.language.TranslateText

open class SettingScene(
    private val parent: SettingsCategory,
    private val nameTranslate: TranslateText,
    private val descriptionTranslate: TranslateText,
    val icon: String
) {

    open fun initGui() {}

    open fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {}

    open fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {}

    open fun mouseReleased(mouseX: Int, mouseY: Int, mouseButton: Int) {}

    open fun keyTyped(typedChar: Char, keyCode: Int) {}

    val name: String
        get() = nameTranslate.getText()

    val description: String
        get() = descriptionTranslate.getText()

    val x: Int
        get() = parent.getSceneX()

    val y: Int
        get() = parent.getSceneY()

    val width: Int
        get() = parent.getSceneWidth()

    val height: Int
        get() = parent.getSceneHeight()

    val contentY: Int
        get() = parent.getSceneY() + HEADER_OFFSET

    val contentHeight: Int
        get() = kotlin.math.max(0, parent.getSceneHeight() - HEADER_OFFSET)

    val headerOffset: Int
        get() = HEADER_OFFSET

    companion object {
        private const val HEADER_OFFSET = 56
    }
}

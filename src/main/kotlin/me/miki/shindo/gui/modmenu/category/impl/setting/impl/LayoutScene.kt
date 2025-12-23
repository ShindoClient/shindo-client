package me.miki.shindo.gui.modmenu.category.impl.setting.impl

import me.miki.shindo.Shindo
import me.miki.shindo.gui.modmenu.category.impl.SettingsCategory
import me.miki.shindo.gui.modmenu.category.impl.setting.SettingScene
import me.miki.shindo.gui.modmenu.category.impl.setting.impl.layout.LayoutAreaScene
import me.miki.shindo.gui.modmenu.category.impl.setting.impl.layout.LayoutAddonsScene
import me.miki.shindo.gui.modmenu.category.impl.setting.impl.layout.LayoutModulesScene
import me.miki.shindo.gui.modmenu.category.impl.setting.impl.layout.LayoutNotificationsScene
import me.miki.shindo.gui.modmenu.category.impl.setting.impl.layout.LayoutScreenshotsScene
import me.miki.shindo.gui.modmenu.category.impl.setting.impl.layout.LayoutSettingsScene
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.utils.ColorUtils
import me.miki.shindo.utils.animation.normal.Animation
import me.miki.shindo.utils.animation.normal.Direction
import me.miki.shindo.utils.animation.normal.other.SmoothStepAnimation
import me.miki.shindo.utils.mouse.MouseUtils
import me.miki.shindo.utils.mouse.Scroll
import org.lwjgl.input.Keyboard
import java.awt.Color
import kotlin.math.max

class LayoutScene(private val parentCategory: SettingsCategory) :
    SettingScene(parentCategory, TranslateText.SETTINGS_LAYOUT_TITLE, TranslateText.SETTINGS_LAYOUT_DESCRIPTION, LegacyIcon.GRID) {

    private val listScroll = Scroll()
    private val scenes = arrayListOf<LayoutAreaScene>()
    private lateinit var sceneAnimation: Animation
    private var currentScene: LayoutAreaScene? = null

    init {
        addScene(LayoutSettingsScene(parentCategory))
        addScene(LayoutModulesScene(parentCategory))
        addScene(LayoutAddonsScene(parentCategory))
        addScene(LayoutScreenshotsScene(parentCategory))
        addScene(LayoutNotificationsScene(parentCategory))
    }

    private fun addScene(scene: LayoutAreaScene) {
        scenes.add(scene)
    }

    override fun initGui() {
        sceneAnimation = SmoothStepAnimation(260, 1.0)
        sceneAnimation.setValue(1.0)
        listScroll.resetAll()

        for (scene in scenes) {
            scene.initGui()
        }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val instance = Shindo.getInstance()
        val nvg = instance.nanoVGManager ?: return
        val palette = instance.colorManager.palette
        val accent = instance.colorManager.currentColor

        val baseX = x.toFloat()
        val baseY = contentY.toFloat()
        val baseWidth = width.toFloat()
        val baseHeight = contentHeight.toFloat()
        if (baseWidth <= 0f || baseHeight <= 0f) {
            return
        }

        val cardHeight = 52f
        val cardSpacing = 10f
        var offsetY = 15f
        val scrollValue = listScroll.getValue()

        if (sceneAnimation.isDone(Direction.FORWARDS)) {
            currentScene = null
        }

        if (currentScene == null && MouseUtils.isInside(mouseX, mouseY, baseX, baseY, baseWidth, baseHeight)) {
            listScroll.onScroll()
            listScroll.onAnimation()
        }

        nvg.save()
        val sceneSlide = (sceneAnimation.getValue() * baseWidth.toDouble()).toFloat()
        nvg.translate(-(baseWidth - sceneSlide), 0f)
        nvg.save()
        nvg.translate(0f, scrollValue)

        for (scene in scenes) {
            val cardX = baseX + 18f
            val cardY = baseY + offsetY
            val cardWidth = baseWidth - 36f
            val isActive = currentScene == scene && !sceneAnimation.isDone(Direction.FORWARDS)
            val hovered = currentScene == null && MouseUtils.isInside(mouseX, mouseY, cardX, cardY + scrollValue, cardWidth, cardHeight)

            val base = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), if (isActive) 220 else 180)
            val overlayStart = ColorUtils.applyAlpha(accent.color1, if (hovered || isActive) 70 else 35)
            val overlayEnd = ColorUtils.applyAlpha(accent.color2, if (hovered || isActive) 70 else 35)

            nvg.drawShadow(cardX, cardY, cardWidth, cardHeight, 10f, 5)
            nvg.drawRoundedRect(cardX, cardY, cardWidth, cardHeight, 10f, base)
            nvg.drawGradientRoundedRect(cardX, cardY, cardWidth, cardHeight, 10f, overlayStart, overlayEnd)

            val iconSize = 28f
            val iconX = cardX + 18f
            val iconY = cardY + (cardHeight - iconSize) / 2f

            nvg.drawGradientRoundedRect(
                iconX,
                iconY,
                iconSize,
                iconSize,
                8f,
                ColorUtils.applyAlpha(accent.color1, 160),
                ColorUtils.applyAlpha(accent.color2, 160)
            )

            nvg.drawCenteredText(scene.icon, iconX + (iconSize / 2f) - 1f, iconY + (iconSize / 2f) - 8f, Color.WHITE, 18f, Fonts.LEGACYICON)

            val textStartX = iconX + iconSize + 14f
            val textWidth = cardWidth - (textStartX - cardX) - 34f
            val displayName = nvg.getLimitText(scene.name, 11.5f, Fonts.MEDIUM, textWidth)
            val displayDescription = nvg.getLimitText(scene.description, 8.5f, Fonts.REGULAR, textWidth)

            nvg.drawText(displayName, textStartX, cardY + 16f, palette.getFontColor(ColorType.DARK), 11.5f, Fonts.MEDIUM)
            nvg.drawText(displayDescription, textStartX, cardY + 30f, palette.getFontColor(ColorType.NORMAL), 8.5f, Fonts.REGULAR)

            nvg.drawCenteredText(
                LegacyIcon.CHEVRON_RIGHT,
                cardX + cardWidth - 22f,
                cardY + (cardHeight / 2f) - (nvg.getTextHeight(LegacyIcon.CHEVRON_RIGHT, 12f, Fonts.LEGACYICON) / 2f),
                palette.getFontColor(ColorType.NORMAL),
                12f,
                Fonts.LEGACYICON
            )

            offsetY += cardHeight + cardSpacing
        }

        nvg.restore()
        nvg.restore()

        nvg.save()
        nvg.translate(sceneSlide, 0f)

        currentScene?.drawScreen(mouseX, mouseY, partialTicks)
        nvg.restore()

        if (currentScene == null) {
            val contentHeight = 15f + scenes.size * cardHeight + max(0, scenes.size - 1) * cardSpacing
            listScroll.maxScroll = max(0f, contentHeight - baseHeight)
        } else {
            listScroll.maxScroll = 0f
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        val cardHeight = 52f
        val cardSpacing = 10f
        var offsetY = 15f
        val scrollValue = listScroll.getValue()

        val baseX = x.toFloat()
        val baseY = contentY.toFloat()
        val baseWidth = width.toFloat()
        val baseHeight = contentHeight.toFloat()

        for (scene in scenes) {
            val cardX = baseX + 18f
            val cardY = baseY + offsetY + scrollValue
            val cardWidth = baseWidth - 36f
            if (MouseUtils.isInside(mouseX, mouseY, cardX, cardY, cardWidth, cardHeight) && mouseButton == 0 && currentScene == null) {
                currentScene = scene
                sceneAnimation.setDirection(Direction.BACKWARDS)
                break
            }

            offsetY += cardHeight + cardSpacing
        }

        if (currentScene != null && sceneAnimation.isDone(Direction.BACKWARDS)) {
            currentScene?.mouseClicked(mouseX, mouseY, mouseButton)
        }

        if (currentScene != null && !MouseUtils.isInside(mouseX, mouseY, baseX, baseY, baseWidth, baseHeight) && mouseButton == 0) {
            sceneAnimation.setDirection(Direction.FORWARDS)
        }

        if (currentScene != null && mouseButton == 3) {
            sceneAnimation.setDirection(Direction.FORWARDS)
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (currentScene != null && sceneAnimation.isDone(Direction.BACKWARDS)) {
            currentScene?.mouseReleased(mouseX, mouseY, mouseButton)
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (currentScene != null && keyCode == Keyboard.KEY_ESCAPE) {
            sceneAnimation.setDirection(Direction.FORWARDS)
        }
        if (currentScene != null && sceneAnimation.isDone(Direction.BACKWARDS)) {
            currentScene?.keyTyped(typedChar, keyCode)
        }
    }

    fun isSubSceneOpen(): Boolean {
        return currentScene != null
    }

    fun getActiveSubScene(): LayoutAreaScene? {
        return currentScene
    }
}

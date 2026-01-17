package me.miki.shindo.gui.modmenu.category.impl

import me.miki.shindo.Shindo
import me.miki.shindo.gui.modmenu.GuiModMenu
import me.miki.shindo.gui.modmenu.category.Category
import me.miki.shindo.gui.modmenu.category.impl.setting.SettingScene
import me.miki.shindo.gui.modmenu.category.impl.setting.impl.AppearanceScene
import me.miki.shindo.gui.modmenu.category.impl.setting.impl.GeneralScene
import me.miki.shindo.gui.modmenu.category.impl.setting.impl.LanguageScene
import me.miki.shindo.gui.modmenu.category.impl.setting.impl.LayoutScene
import me.miki.shindo.gui.modmenu.category.impl.setting.impl.PerformanceScene
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

class SettingsCategory(parent: GuiModMenu) : Category(parent, TranslateText.SETTINGS, LegacyIcon.SETTINGS, false, false) {

    private val scenes = arrayListOf<SettingScene>()
    private lateinit var sceneAnimation: Animation
    private var currentScene: SettingScene? = null

    init {
        scenes.add(AppearanceScene(this))
        scenes.add(LanguageScene(this))
        scenes.add(GeneralScene(this))
        scenes.add(LayoutScene(this))
        scenes.add(PerformanceScene(this))
    }

    override fun initGui() {
        sceneAnimation = SmoothStepAnimation(260, 1.0)
        sceneAnimation.setValue(1.0)

        for (scene in scenes) {
            scene.initGui()
        }
    }

    override fun initCategory() {
        scroll.resetAll()
        sceneAnimation = SmoothStepAnimation(260, 1.0)
        sceneAnimation.setValue(1.0)
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val instance = Shindo.getInstance()
        val nvg = instance.nanoVGManager ?: return
        val palette = instance.colorManager.palette
        val accent = instance.colorManager.currentColor

        val cardHeight = 52f
        val cardSpacing = 10f
        var offsetY = 15f
        val scrollValue = scroll.getValue()

        if (sceneAnimation.isDone(Direction.FORWARDS)) {
            setCanClose(true)
            currentScene = null
        }

        if (currentScene == null && MouseUtils.isInside(mouseX, mouseY, getX().toFloat(), getY().toFloat(), getWidth().toFloat(), getHeight().toFloat())) {
            scroll.onScroll()
            scroll.onAnimation()
        }

        nvg.save()
        val sceneSlide = (sceneAnimation.getValue() * 600.0).toFloat()
        nvg.translate(-(600f - sceneSlide), 0f)
        nvg.save()
        nvg.translate(0f, scrollValue)

        for (scene in scenes) {
            val cardX = getX() + 18f
            val cardY = getY() + offsetY
            val cardWidth = getWidth() - 36f
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

        currentScene?.let { scene ->
            scene.drawScreen(mouseX, mouseY, partialTicks)

            val headerScene = (scene as? LayoutScene)?.getActiveSubScene() ?: scene
            val headerX = getX() + 18f
            val headerY = getY() + 14f
            val iconSize = 34f
            val iconRadius = 10f
            val textX = headerX + iconSize + 16f
            val textWidth = getWidth() - (textX - getX()) - 24f

            val iconStart = ColorUtils.applyAlpha(accent.color1, 200)
            val iconEnd = ColorUtils.applyAlpha(accent.color2, 200)
            val titleColor = palette.getFontColor(ColorType.DARK)
            val subtitleColor = palette.getFontColor(ColorType.NORMAL)

            nvg.drawGradientRoundedRect(headerX, headerY, iconSize, iconSize, iconRadius, iconStart, iconEnd)
            nvg.drawCenteredText(headerScene.icon, headerX + (iconSize / 2f) - 1f, headerY + (iconSize / 2f) - 10f, Color.WHITE, 22f, Fonts.LEGACYICON)

            val title = nvg.getLimitText(headerScene.name, 13.5f, Fonts.MEDIUM, textWidth)
            nvg.drawText(title, textX, headerY + 7f, titleColor, 13.5f, Fonts.MEDIUM)

            val description = headerScene.description
            if (!description.equals("null", ignoreCase = true)) {
                val clippedDescription = nvg.getLimitText(description, 9f, Fonts.REGULAR, textWidth)
                nvg.drawText(clippedDescription, textX, headerY + 24f, subtitleColor, 9f, Fonts.REGULAR)
            }
        }

        nvg.restore()

        if (currentScene == null) {
            val contentHeight = 15f + scenes.size * cardHeight + max(0, scenes.size - 1) * cardSpacing
            val viewportHeight = getHeight() - 30f
            scroll.maxScroll = max(0f, contentHeight - viewportHeight)
        } else {
            scroll.maxScroll = 0f
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        val cardHeight = 52f
        val cardSpacing = 10f
        var offsetY = 15f
        val scrollValue = scroll.getValue()

        for (scene in scenes) {
            val cardX = getX() + 18f
            val cardY = getY() + offsetY + scrollValue
            val cardWidth = getWidth() - 36f
            if (MouseUtils.isInside(mouseX, mouseY, cardX, cardY, cardWidth, cardHeight) && mouseButton == 0 && currentScene == null) {
                currentScene = scene
                setCanClose(false)
                sceneAnimation.setDirection(Direction.BACKWARDS)
                break
            }

            offsetY += cardHeight + cardSpacing
        }

        if (currentScene != null && sceneAnimation.isDone(Direction.BACKWARDS)) {
            currentScene?.mouseClicked(mouseX, mouseY, mouseButton)
        }

        if (!MouseUtils.isInside(mouseX, mouseY, getX().toFloat(), getY().toFloat(), getWidth().toFloat(), getHeight().toFloat()) && mouseButton == 0) {
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
            val layoutScene = currentScene as? LayoutScene
            if (layoutScene == null || !layoutScene.isSubSceneOpen()) {
                sceneAnimation.setDirection(Direction.FORWARDS)
            }
        }
        if (currentScene != null && sceneAnimation.isDone(Direction.BACKWARDS)) {
            currentScene?.keyTyped(typedChar, keyCode)
        }
    }

    fun getSceneX(): Int = getX() + 15
    fun getSceneY(): Int = getY() + 15
    fun getSceneWidth(): Int = getWidth() - 30
    fun getSceneHeight(): Int = getHeight() - 30
}

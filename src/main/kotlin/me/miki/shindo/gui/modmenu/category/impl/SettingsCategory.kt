package me.miki.shindo.gui.modmenu.category.impl

import me.miki.shindo.Shindo
import me.miki.shindo.gui.modmenu.GuiModMenu
import me.miki.shindo.gui.modmenu.category.Category
import me.miki.shindo.gui.modmenu.category.impl.setting.SettingScene
import me.miki.shindo.gui.modmenu.category.impl.setting.impl.*
import me.miki.shindo.ui.comp.buttons.CompSceneButton
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.utils.ColorUtils
import me.miki.shindo.ui.animation.Animation
import me.miki.shindo.ui.animation.Direction
import me.miki.shindo.ui.animation.curve.SmoothStepAnimation
import me.miki.shindo.utils.mouse.MouseUtils
import org.lwjgl.input.Keyboard
import java.awt.Color
import kotlin.math.max

class SettingsCategory(parent: GuiModMenu) :
    Category(parent, TranslateText.SETTINGS, LegacyIcon.SETTINGS, false, false) {

    private val scenes = arrayListOf<SettingScene>()
    private val sceneButtons = arrayListOf<CompSceneButton>()
    private lateinit var sceneAnimation: Animation
    private var currentScene: SettingScene? = null

    init {
        registerScene(AppearanceScene(this))
        registerScene(LanguageScene(this))
        registerScene(GeneralScene(this))
        registerScene(LayoutScene(this))
        registerScene(PerformanceScene(this))
    }

    private fun registerScene(scene: SettingScene) {
        scenes.add(scene)
        sceneButtons.add(
            CompSceneButton({ scene.icon }, { scene.name }, { scene.description })
        )
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
        val palette = instance.colorManager.getPalette()
        val accent = instance.colorManager.getCurrentColor()

        val cardHeight = 52f
        val cardSpacing = 10f
        val scrollValue = scroll.getValue()

        if (sceneAnimation.isDone(Direction.FORWARDS)) {
            setCanClose(true)
            currentScene = null
        }

        if (currentScene == null && MouseUtils.isInside(
                mouseX,
                mouseY,
                getX().toFloat(),
                getY().toFloat(),
                getWidth().toFloat(),
                getHeight().toFloat()
            )
        ) {
            scroll.onScroll()
            scroll.onAnimation()
        }

        nvg.save()
        val sceneSlide = (sceneAnimation.getValue() * 600.0).toFloat()
        nvg.translate(-(600f - sceneSlide), 0f)

        forEachSceneEntry(scrollValue) { scene, button, cardX, cardY, cardW, cardH ->
            button.setBounds(cardX, cardY, cardW, cardH)
            button.setActive(currentScene == scene && !sceneAnimation.isDone(Direction.FORWARDS))
            button.setEnabled(currentScene == null)
            button.draw(mouseX, mouseY, partialTicks)
        }

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

            val iconStart = ColorUtils.applyAlpha(accent.getColor1(), 200)
            val iconEnd = ColorUtils.applyAlpha(accent.getColor2(), 200)
            val titleColor = palette.getFontColor(ColorType.DARK)
            val subtitleColor = palette.getFontColor(ColorType.NORMAL)

            nvg.drawGradientRoundedRect(headerX, headerY, iconSize, iconSize, iconRadius, iconStart, iconEnd)
            nvg.drawCenteredText(
                headerScene.icon,
                headerX + (iconSize / 2f) - 1f,
                headerY + (iconSize / 2f) - 10f,
                Color.WHITE,
                22f,
                Fonts.LEGACYICON
            )

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
        forEachSceneEntryUntil(scroll.getValue()) { scene, _, cardX, cardY, cardWidth, cardHeight ->
            if (MouseUtils.isInside(
                    mouseX,
                    mouseY,
                    cardX,
                    cardY,
                    cardWidth,
                    cardHeight
                ) && mouseButton == 0 && currentScene == null
            ) {
                currentScene = scene
                setCanClose(false)
                sceneAnimation.setDirection(Direction.BACKWARDS)
                return@forEachSceneEntryUntil true
            }
            false
        }

        if (currentScene != null && sceneAnimation.isDone(Direction.BACKWARDS)) {
            currentScene?.mouseClicked(mouseX, mouseY, mouseButton)
        }

        if (!MouseUtils.isInside(
                mouseX,
                mouseY,
                getX().toFloat(),
                getY().toFloat(),
                getWidth().toFloat(),
                getHeight().toFloat()
            ) && mouseButton == 0
        ) {
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

    private inline fun forEachSceneEntryUntil(
        scrollValue: Float,
        action: (scene: SettingScene, button: CompSceneButton, cardX: Float, cardY: Float, cardWidth: Float, cardHeight: Float) -> Boolean
    ): Boolean {
        var offsetY = 15f
        val baseX = getX().toFloat()
        val baseY = getY().toFloat()
        val entryWidth = getWidth() - 36f
        val entryHeight = 52f
        for (index in scenes.indices) {
            val scene = scenes[index]
            val button = sceneButtons[index]
            val cardX = baseX + 18f
            val cardY = baseY + offsetY + scrollValue
            if (action(scene, button, cardX, cardY, entryWidth, entryHeight)) {
                return true
            }
            offsetY += 52f + 10f
        }
        return false
    }

    private inline fun forEachSceneEntry(
        scrollValue: Float,
        action: (scene: SettingScene, button: CompSceneButton, cardX: Float, cardY: Float, cardWidth: Float, cardHeight: Float) -> Unit
    ) {
        forEachSceneEntryUntil(scrollValue) { scene, button, cardX, cardY, cardWidth, cardHeight ->
            action(scene, button, cardX, cardY, cardWidth, cardHeight)
            false
        }
    }

    fun getSceneX(): Int = getX() + 15
    fun getSceneY(): Int = getY() + 15
    fun getSceneWidth(): Int = getWidth() - 30
    fun getSceneHeight(): Int = getHeight() - 30
}

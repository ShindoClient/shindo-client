package me.miki.shindo.gui.modmenu.category.impl

import me.miki.shindo.Shindo
import me.miki.shindo.gui.modmenu.GuiModMenu
import me.miki.shindo.gui.modmenu.category.Category
import me.miki.shindo.gui.modmenu.category.impl.setting.SettingScene
import me.miki.shindo.gui.modmenu.category.impl.setting.SettingSceneTransitionCoordinator
import me.miki.shindo.gui.modmenu.category.impl.setting.impl.*
import me.miki.shindo.gui.modmenu.render.ModMenuClipCoordinator
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.ui.comp.buttons.CompSceneButton
import me.miki.shindo.utils.ColorUtils
import me.miki.shindo.utils.mouse.MouseUtils
import me.miki.shindo.utils.mouse.ScrollInputGuard
import org.lwjgl.input.Keyboard
import java.awt.Color
import kotlin.math.max

class SettingsCategory(parent: GuiModMenu) :
    Category(parent, TranslateText.SETTINGS, LegacyIcon.SETTINGS, false, false) {

    private val scenes = arrayListOf<SettingScene>()
    private val sceneButtons = arrayListOf<CompSceneButton>()
    private val transitionCoordinator = SettingSceneTransitionCoordinator()

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
        transitionCoordinator.reset()

        for (scene in scenes) {
            scene.initGui()
        }
    }

    override fun initCategory() {
        scroll.resetAll()
        transitionCoordinator.reset()
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val instance = Shindo.getInstance()
        val nvg = instance.nanoVGManager ?: return
        val palette = instance.colorManager.getPalette()
        val accent = instance.colorManager.getCurrentColor()
        val baseX = getX().toFloat()
        val baseY = getY().toFloat()
        val baseWidth = getWidth().toFloat()
        val baseHeight = getHeight().toFloat()
        if (baseWidth <= 0f || baseHeight <= 0f) {
            return
        }

        val cardHeight = 52f
        val cardSpacing = 10f
        val scrollValue = scroll.getValue()

        transitionCoordinator.update()
        val scene = transitionCoordinator.getActiveScene()
        if (transitionCoordinator.isListInteractive()) {
            setCanClose(true)
        }

        if (transitionCoordinator.isListInteractive() && MouseUtils.isInside(
                mouseX,
                mouseY,
                baseX,
                baseY,
                baseWidth,
                baseHeight
            )
        ) {
            scroll.onScroll()
            scroll.onAnimation()
        }

        val lockScrollInput = transitionCoordinator.isTransitioning()
        if (lockScrollInput) {
            ScrollInputGuard.lock()
        }

        try {
            val listTranslateX = transitionCoordinator.getListTranslateX(baseWidth)
            ModMenuClipCoordinator.withClipTranslate(
                nvg = nvg,
                x = baseX,
                y = baseY,
                width = baseWidth,
                height = baseHeight,
                translateX = listTranslateX,
                translateY = 0f,
                layer = ModMenuClipCoordinator.ClipLayer.SETTINGS_LIST,
                tag = "settings_scene_list"
            ) {
                //nvg.drawShadow(baseX + 14f, baseY + 10f, baseWidth - 28f, baseHeight - 20f, 12f, 6)
                //nvg.drawRoundedRect( baseX + 14f, baseY + 10f, baseWidth - 28f, baseHeight - 20f, 12f, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 176))
                forEachSceneEntry(scrollValue) { entryScene, button, cardX, cardY, cardW, cardH ->
                    button.setBounds(cardX, cardY, cardW, cardH)
                    button.setActive(scene == entryScene && transitionCoordinator.isSceneVisible())
                    button.setEnabled(transitionCoordinator.isListInteractive())
                    button.draw(mouseX, mouseY, partialTicks)
                }
            }

            if (scene != null) {
                val sceneTranslateX = transitionCoordinator.getSceneTranslateX(baseWidth)
                ModMenuClipCoordinator.withClipTranslate(
                    nvg = nvg,
                    x = baseX,
                    y = baseY,
                    width = baseWidth,
                    height = baseHeight,
                    translateX = sceneTranslateX,
                    translateY = 0f,
                    layer = ModMenuClipCoordinator.ClipLayer.SETTINGS_SCENE,
                    tag = "settings_scene_content"
                ) {
                    //nvg.drawShadow(baseX + 10f, baseY + 8f, baseWidth - 20f, baseHeight - 16f, 12f, 7)
                    //nvg.drawRoundedRect( baseX + 10f, baseY + 8f, baseWidth - 20f, baseHeight - 16f, 12f, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 190) )
                    //nvg.drawGradientRoundedRect( baseX + 10f, baseY + 8f, baseWidth - 20f, baseHeight - 16f, 12f, ColorUtils.applyAlpha(accent.getColor1(), 24), ColorUtils.applyAlpha(accent.getColor2(), 24))
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
            }
        } finally {
            if (lockScrollInput) {
                ScrollInputGuard.unlock()
            }
        }

        if (transitionCoordinator.isListInteractive()) {
            val contentHeight = 15f + scenes.size * cardHeight + max(0, scenes.size - 1) * cardSpacing
            val viewportHeight = getHeight() - 30f
            scroll.maxScroll = max(0f, contentHeight - viewportHeight)
        } else {
            scroll.maxScroll = 0f
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (transitionCoordinator.isListInteractive()) {
            forEachSceneEntryUntil(scroll.getValue()) { scene, _, cardX, cardY, cardWidth, cardHeight ->
                if (MouseUtils.isInside(
                        mouseX,
                        mouseY,
                        cardX,
                        cardY,
                        cardWidth,
                        cardHeight
                    ) && mouseButton == 0
                ) {
                    transitionCoordinator.open(scene)
                    setCanClose(false)
                    return@forEachSceneEntryUntil true
                }
                false
            }
        }

        if (transitionCoordinator.isSceneInteractive()) {
            transitionCoordinator.getActiveScene()?.mouseClicked(mouseX, mouseY, mouseButton)
        }

        if (transitionCoordinator.isSceneVisible() &&
            !MouseUtils.isInside(
                mouseX,
                mouseY,
                getX().toFloat(),
                getY().toFloat(),
                getWidth().toFloat(),
                getHeight().toFloat()
            ) && mouseButton == 0
        ) {
            transitionCoordinator.close()
        }

        if (transitionCoordinator.isSceneVisible() && mouseButton == 3) {
            transitionCoordinator.close()
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (transitionCoordinator.isSceneInteractive()) {
            transitionCoordinator.getActiveScene()?.mouseReleased(mouseX, mouseY, mouseButton)
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        val activeScene = transitionCoordinator.getActiveScene()
        if (activeScene != null && keyCode == Keyboard.KEY_ESCAPE) {
            val layoutScene = activeScene as? LayoutScene
            if (layoutScene == null || !layoutScene.isSubSceneOpen()) {
                transitionCoordinator.close()
            }
        }
        if (transitionCoordinator.isSceneInteractive()) {
            transitionCoordinator.getActiveScene()?.keyTyped(typedChar, keyCode)
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

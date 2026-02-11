package me.miki.shindo.gui.modmenu.category.impl.setting.impl

import me.miki.shindo.Shindo
import me.miki.shindo.gui.modmenu.category.impl.SettingsCategory
import me.miki.shindo.gui.modmenu.category.impl.setting.SettingScene
import me.miki.shindo.gui.modmenu.category.impl.setting.impl.layout.*
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.ui.comp.layout.CompScrollableContainer
import me.miki.shindo.ui.animation.Animation
import me.miki.shindo.ui.animation.Direction
import me.miki.shindo.ui.animation.curve.SmoothStepAnimation
import me.miki.shindo.utils.mouse.MouseUtils
import org.lwjgl.input.Keyboard
import kotlin.math.max

class LayoutScene(private val parentCategory: SettingsCategory) :
        SettingScene(
                parentCategory,
                TranslateText.SETTINGS_LAYOUT_TITLE,
                TranslateText.SETTINGS_LAYOUT_DESCRIPTION,
                LegacyIcon.GRID
        ) {

    private val controllers = arrayListOf<LayoutAreaController>()
    private lateinit var sceneAnimation: Animation
    private lateinit var container: CompScrollableContainer
    private var currentController: LayoutAreaController? = null

    init {
        addScene(LayoutSettingsScene(parentCategory))
        addScene(LayoutModulesScene(parentCategory))
        addScene(LayoutNotificationsScene(parentCategory))
        addScene(LayoutVisualScene(parentCategory))
    }

    private fun addScene(scene: LayoutAreaScene) {
        controllers.add(LayoutAreaController(scene))
    }

    override fun initGui() {
        sceneAnimation = SmoothStepAnimation(260, 1.0)
        sceneAnimation.setValue(1.0)
        container = CompScrollableContainer().setInnerPadding(0f)
        container.getScroll().resetAll()

        for (controller in controllers) {
            controller.initGui()
        }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val instance = Shindo.getInstance()
        val nvg = instance.nanoVGManager ?: return

        val baseX = x.toFloat()
        val baseY = contentY.toFloat()
        val baseWidth = width.toFloat()
        val baseHeight = contentHeight.toFloat()

        if (baseWidth <= 0f || baseHeight <= 0f) {
            return
        }

        val cardHeight = CARD_HEIGHT
        val cardSpacing = CARD_SPACING
        val contentHeight = (controllers.size * cardHeight + max(0, controllers.size - 1) * cardSpacing) + CARD_PADDING * 2
        val cardWidth = baseWidth - 36f

        if (sceneAnimation.isDone(Direction.FORWARDS)) {
            currentController = null
        }

        nvg.save()
        val sceneSlide = (sceneAnimation.getValue() * baseWidth.toDouble()).toFloat()
        nvg.translate(-(baseWidth - sceneSlide), 0f)

        if (currentController == null) {
            container.setBounds(baseX, baseY, baseWidth, baseHeight)
            container.render(mouseX, mouseY, partialTicks, contentHeight) { mouseXInner, mouseYInner, partialInner, scrollValue ->
                forEachSceneEntry(scrollValue, baseX, baseY, cardWidth) { controller, cardX, cardY, cardW, cardH ->
                    controller.drawCard(
                            mouseXInner,
                            mouseYInner,
                            partialInner,
                            cardX,
                            cardY,
                            cardW,
                            cardH,
                            currentController == controller && !sceneAnimation.isDone(Direction.FORWARDS),
                            currentController == null
                    )
                }
            }
        }

        nvg.restore()

        nvg.save()
        nvg.translate(sceneSlide, 0f)

        currentController?.scene?.drawScreen(mouseX, mouseY, partialTicks)
        nvg.restore()

    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        val baseX = x.toFloat()
        val baseY = contentY.toFloat()
        val baseWidth = width.toFloat()
        val baseHeight = contentHeight.toFloat()

        if (currentController == null) {
            val scrollValue = container.getScroll().getValue()
            val cardWidth = baseWidth - 36f
            var offsetY = CARD_PADDING
            for (controller in controllers) {
                val cardX = baseX + 18f
                val cardY = baseY + offsetY + scrollValue
                if (controller.hit(mouseX, mouseY, cardX, cardY, cardWidth, CARD_HEIGHT) && mouseButton == 0
                ) {
                    currentController = controller
                    sceneAnimation.setDirection(Direction.BACKWARDS)
                    break
                }
                offsetY += CARD_HEIGHT + CARD_SPACING
            }
        }

        if (currentController != null && sceneAnimation.isDone(Direction.BACKWARDS)) {
            currentController?.scene?.mouseClicked(mouseX, mouseY, mouseButton)
        }

        if (currentController != null && !MouseUtils.isInside(
                        mouseX,
                        mouseY,
                        baseX,
                        baseY,
                        baseWidth,
                        baseHeight
                ) && mouseButton == 0
        ) {
            sceneAnimation.setDirection(Direction.FORWARDS)
        }

        if (currentController != null && mouseButton == 3) {
            sceneAnimation.setDirection(Direction.FORWARDS)
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (currentController != null && sceneAnimation.isDone(Direction.BACKWARDS)) {
            currentController?.scene?.mouseReleased(mouseX, mouseY, mouseButton)
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (currentController != null && keyCode == Keyboard.KEY_ESCAPE) {
            sceneAnimation.setDirection(Direction.FORWARDS)
        }
        if (currentController != null && sceneAnimation.isDone(Direction.BACKWARDS)) {
            currentController?.scene?.keyTyped(typedChar, keyCode)
        }
    }

    private fun forEachSceneEntry(
            scrollValue: Float,
            baseX: Float,
            baseY: Float,
            cardWidth: Float,
            action: (controller: LayoutAreaController, cardX: Float, cardY: Float, cardWidth: Float, cardHeight: Float) -> Unit
    ) {
        var offsetY = CARD_PADDING
        for (controller in controllers) {
            val cardX = baseX + 18f
            val cardY = baseY + offsetY + scrollValue
            action(controller, cardX, cardY, cardWidth, CARD_HEIGHT)
            offsetY += CARD_HEIGHT + CARD_SPACING
        }
    }

    fun isSubSceneOpen(): Boolean {
        return currentController != null
    }

    fun getActiveSubScene(): LayoutAreaScene? {
        return currentController?.scene
    }

    companion object {
        private const val CARD_HEIGHT = 52f
        private const val CARD_SPACING = 10f
        private const val CARD_PADDING = 15f
    }
}

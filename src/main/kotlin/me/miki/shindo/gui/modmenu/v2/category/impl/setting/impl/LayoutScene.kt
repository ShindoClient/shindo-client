package me.miki.shindo.gui.modmenu.v2.category.impl.setting.impl

import me.miki.shindo.Shindo
import me.miki.shindo.gui.modmenu.v2.category.impl.SettingsCategory
import me.miki.shindo.gui.modmenu.v2.category.impl.setting.SettingScene
import me.miki.shindo.gui.modmenu.v2.category.impl.setting.impl.layout.*
import me.miki.shindo.gui.modmenu.v2.navigation.ModMenuSlideTransitionCoordinator
import me.miki.shindo.gui.modmenu.v2.render.ModMenuClipCoordinator
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.utils.ColorUtils

/**
 * Root scene for layout configuration.
 *
 * The scene has two layers:
 * - an index list with the 4 area scenes;
 * - a focused area scene rendered in the same viewport with animated transition.
 */
class LayoutScene(
    parentCategory: SettingsCategory,
) : SettingScene(
        parentCategory,
        TranslateText.SETTINGS_LAYOUT_TITLE,
        TranslateText.SETTINGS_LAYOUT_DESCRIPTION,
        LegacyIcon.GRID,
    ) {
    private val controllers = ArrayList<LayoutAreaController>()
    private val stateCoordinator = ModMenuSlideTransitionCoordinator()
    private val listController = LayoutSceneListController()
    private val inputController = LayoutSceneInputController()

    init {
        addScene(LayoutSettingsScene(parentCategory))
        addScene(LayoutModulesScene(parentCategory))
        addScene(LayoutNotificationsScene(parentCategory))
        addScene(LayoutVisualScene(parentCategory))
    }

    /**
     * Registers one child scene in the root index list.
     */
    private fun addScene(scene: LayoutAreaScene) {
        controllers.add(LayoutAreaController(scene))
    }

    override fun initGui() {
        stateCoordinator.init()
        listController.reset()

        var i = 0
        while (i < controllers.size) {
            controllers[i].scene.initGui()
            i++
        }
    }

    override fun drawScreen(
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float,
    ) {
        val instance = Shindo.getInstance()
        val nvg = instance.nanoVGManager ?: return
        val palette = instance.getColorManager().getPalette()

        val baseX = x.toFloat()
        val baseY = contentY.toFloat()
        val baseWidth = width.toFloat()
        val baseHeight = contentHeight.toFloat()
        if (baseWidth <= 0f || baseHeight <= 0f) {
            return
        }

        stateCoordinator.update()

        val slide = stateCoordinator.getSlideOffset(baseWidth)

        // Keep clip pinned to viewport while translated layers animate horizontally.
        ModMenuClipCoordinator.withClipTranslate(
            nvg = nvg,
            x = baseX,
            y = baseY,
            width = baseWidth,
            height = baseHeight,
            translateX = -(baseWidth - slide),
            translateY = 0f,
        ) {
            nvg.drawShadow(baseX + 14f, baseY + 10f, baseWidth - 28f, baseHeight - 20f, 12f, 6)
            nvg.drawRoundedRect(
                baseX + 14f,
                baseY + 10f,
                baseWidth - 28f,
                baseHeight - 20f,
                12f,
                ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 176),
            )
            listController.drawList(
                controllers = controllers,
                activeController = stateCoordinator.getActiveScene() as? LayoutAreaController?,
                mouseX = mouseX,
                mouseY = mouseY,
                partialTicks = partialTicks,
                baseX = baseX,
                baseY = baseY,
                baseWidth = baseWidth,
                baseHeight = baseHeight,
            )
        }

        ModMenuClipCoordinator.withClipTranslate(
            nvg = nvg,
            x = baseX,
            y = baseY,
            width = baseWidth,
            height = baseHeight,
            translateX = slide,
            translateY = 0f,
        ) {
            (stateCoordinator.getActiveScene() as? LayoutAreaController)?.scene?.drawScreen(
                mouseX,
                mouseY,
                partialTicks,
            )
        }
    }

    override fun mouseClicked(
        mouseX: Int,
        mouseY: Int,
        mouseButton: Int,
    ) {
        val baseX = x.toFloat()
        val baseY = contentY.toFloat()
        val baseWidth = width.toFloat()
        val baseHeight = contentHeight.toFloat()

        if (!stateCoordinator.isSceneVisible() && inputController.isPrimaryClick(mouseButton)) {
            val selected = listController.findClickedController(mouseX, mouseY)
            if (selected != null) {
                stateCoordinator.open(selected)
                return
            }
        }

        if (stateCoordinator.isSceneInteractive()) {
            (stateCoordinator.getActiveScene() as? LayoutAreaController)?.scene?.mouseClicked(
                mouseX,
                mouseY,
                mouseButton,
            )
        }

        if (stateCoordinator.isSceneVisible() &&
            inputController.isPrimaryClick(mouseButton) &&
            inputController.shouldCloseByOutsideClick(mouseX, mouseY, baseX, baseY, baseWidth, baseHeight)
        ) {
            stateCoordinator.close()
        }

        if (stateCoordinator.isSceneVisible() && inputController.isBackMouseButton(mouseButton)) {
            stateCoordinator.close()
        }
    }

    override fun mouseReleased(
        mouseX: Int,
        mouseY: Int,
        mouseButton: Int,
    ) {
        if (stateCoordinator.isSceneInteractive()) {
            (stateCoordinator.getActiveScene() as? LayoutAreaController)?.scene?.mouseReleased(
                mouseX,
                mouseY,
                mouseButton,
            )
        }
    }

    override fun keyTyped(
        typedChar: Char,
        keyCode: Int,
    ) {
        if (stateCoordinator.isSceneVisible() && inputController.shouldCloseByEscape(keyCode)) {
            stateCoordinator.close()
            return
        }
        if (stateCoordinator.isSceneInteractive()) {
            (stateCoordinator.getActiveScene() as? LayoutAreaController)?.scene?.keyTyped(typedChar, keyCode)
        }
    }

    /**
     * Compatibility helper used by [SettingsCategory] to control Escape behavior.
     */
    fun isSubSceneOpen(): Boolean = stateCoordinator.isSceneVisible()

    /**
     * Compatibility helper used by [SettingsCategory] to show active sub-scene metadata.
     */
    fun getActiveSubScene(): LayoutAreaScene? = (stateCoordinator.getActiveScene() as? LayoutAreaController)?.scene
}

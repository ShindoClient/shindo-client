package me.miki.shindo.gui.modmenu.v2

import eu.shoroa.contrib.render.Blur
import me.miki.shindo.Shindo
import me.miki.shindo.gui.GuiEditHUD
import me.miki.shindo.gui.modmenu.v2.category.Category
import me.miki.shindo.gui.modmenu.v2.category.impl.*
import me.miki.shindo.gui.modmenu.v2.category.list.ModMenuListPageContract
import me.miki.shindo.gui.modmenu.v2.navigation.ModMenuCategoryTransitionCoordinator
import me.miki.shindo.gui.modmenu.v2.navigation.ModMenuSidebarController
import me.miki.shindo.gui.modmenu.v2.render.ModMenuClipCoordinator
import me.miki.shindo.gui.modmenu.v2.render.ModMenuHeaderController
import me.miki.shindo.gui.modmenu.v2.render.ModMenuTelemetryOverlay
import me.miki.shindo.gui.modmenu.v2.style.ModMenuResponsiveLayout
import me.miki.shindo.gui.modmenu.v2.style.ModMenuStyle
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.event.impl.EventRenderNotification
import me.miki.shindo.management.mods.impl.InternalSettingsMod
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.Lucide
import me.miki.shindo.management.nanovg.font.Shinconic
import me.miki.shindo.ui.animation.v2.Animation
import me.miki.shindo.ui.animation.v2.Direction
import me.miki.shindo.ui.animation.v2.easing.EaseBackIn
import me.miki.shindo.ui.animation.v2.screen.ScreenAnimation
import me.miki.shindo.ui.components.v2.buttons.CompIconButton
import me.miki.shindo.ui.components.v2.inputs.CompSearchBox
import me.miki.shindo.utils.file.FileUtils
import me.miki.shindo.utils.mouse.MouseUtils
import me.miki.shindo.utils.mouse.Scroll
import me.miki.shindo.utils.mouse.ScrollInputGuard
import me.miki.shindo.utils.render.BlurUtils
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import org.lwjgl.input.Keyboard
import java.awt.Color
import java.io.IOException
import kotlin.math.min

class GuiModMenu(
    private val initialCategoryClass: Class<out Category>? = null,
    private val initialSearchText: String? = null,
) : GuiScreen() {
    private val categories = ArrayList<Category>()
    private val screenAnimation = ScreenAnimation()
    private val scroll = Scroll()
    private val searchBox = CompSearchBox()
    private val layoutButton = CompIconButton(21f) { Lucide.LAYOUT }
    private val folderButton = CompIconButton(18f) { Lucide.FOLDER }
    private val sidebarController = ModMenuSidebarController()
    private val categoryTransition = ModMenuCategoryTransitionCoordinator()
    private val headerController = ModMenuHeaderController()
    private val telemetryOverlay = ModMenuTelemetryOverlay()

    private lateinit var introAnimation: Animation

    private var x = 0
    private var y = 0
    private var menuWidth = 0
    private var menuHeight = 0

    private var currentCategory: Category
    private var canClose = false

    private var toEditHUD = false

    init {
        categories +=
            listOf(
                HomeCategory(this),
                ModuleCategory(this),
                AddonCategory(this),
                CosmeticsCategory(this),
                SpotifyCategory(this),
                ProfileCategory(this),
                ScreenshotCategory(this),
                SettingsCategory(this),
            )
        currentCategory = getCategoryByClass(HomeCategory::class.java)
    }

    override fun initGui() {
        val bounds = ModMenuResponsiveLayout.resolve(ScaledResolution(mc))
        x = bounds.x
        y = bounds.y
        menuWidth = bounds.width
        menuHeight = bounds.height

        introAnimation = EaseBackIn(320, 1.0, 2.0f).apply { setDirection(Direction.FORWARDS) }

        categories.forEach { it.initGui() }
        scroll.resetAll()
        toEditHUD = false
        canClose = true

        initialCategoryClass?.let { currentCategory = getCategoryByClass(it) }
        searchBox.setText(initialSearchText ?: "")
        categoryTransition.reset(currentCategory)

        layoutButton.apply {
            onClick {
                toEditHUD = true
                introAnimation.setDirection(Direction.BACKWARDS)
            }
            setFontSize(14f)
            setRadius(6f)
            enabledWhen { canClose }
        }
        folderButton.apply {
            setRadius(6f)
            setFontSize(9f)
            setVisible(false)
            onClick { FileUtils.openFolderAtPath(Shindo.getInstance().getFileManager().customCapeDir) }
        }
    }

    override fun drawScreen(
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float,
    ) {
        val nvg = Shindo.getInstance().nanoVGManager ?: return
        val introScale = introAnimation.getValueFloat()

        if (InternalSettingsMod.instance.getBlurSetting()?.isToggled() == true) {
            BlurUtils.drawBlurScreen(min(introAnimation.getValue(), 1.0).toFloat() * 20f + 1f)
        }

        val scale = 2f - introScale
        val alpha = min(introScale, 1f)

        screenAnimation.wrap(
            Runnable {
                nvg.drawShadow(x.toFloat(), y.toFloat(), menuWidth.toFloat(), menuHeight.toFloat(), 12f)
            },
            scale,
            alpha,
        )

        screenAnimation.wrap(
            Runnable { drawNanoVG(mouseX, mouseY, partialTicks) },
            x.toFloat(),
            y.toFloat(),
            menuWidth.toFloat(),
            menuHeight.toFloat(),
            scale,
            alpha,
            true,
        )

        EventRenderNotification().call()
        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    private fun drawNanoVG(
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float,
    ) {
        val instance = Shindo.getInstance()
        val nvg = instance.nanoVGManager ?: return
        val colorManager = instance.getColorManager()
        val palette = colorManager.getPalette()
        val accent = colorManager.getCurrentColor()
        val blurEnabled = InternalSettingsMod.instance.getBlurSetting()?.isToggled() == true

        telemetryOverlay.beginFrame()
        ModMenuClipCoordinator.beginFrame()

        if (introAnimation.isDone(Direction.BACKWARDS)) {
            mc.displayGuiScreen(if (toEditHUD) GuiEditHUD(true) else null)
        }

        drawLegacyFrame(
            nvg,
            palette,
            accent,
            x.toFloat(),
            y.toFloat(),
            menuWidth.toFloat(),
            menuHeight.toFloat(),
            blurEnabled,
        )

        rebuildSidebar()
        nvg.save()
        sidebarController.draw(
            nvg = nvg,
            palette = palette,
            accent = accent,
            currentCategory = currentCategory,
            mouseX = mouseX,
            mouseY = mouseY,
        )
        nvg.restore()

        layoutButton.setBounds(
            x + ModMenuStyle.HUD_BUTTON_X,
            y + menuHeight - ModMenuStyle.HUD_BUTTON_BOTTOM_MARGIN,
            ModMenuStyle.HUD_BUTTON_SIZE,
            ModMenuStyle.HUD_BUTTON_SIZE,
        )
        layoutButton.draw(mouseX, mouseY, partialTicks)

        val header =
            headerController.draw(
                nvg = nvg,
                palette = palette,
                currentCategory = currentCategory,
                menuX = x,
                menuY = y,
                menuWidth = menuWidth,
                mouseX = mouseX,
                mouseY = mouseY,
                partialTicks = partialTicks,
                searchBox = searchBox,
                folderButton = folderButton,
            )

        categoryTransition.update()
        currentCategory = categoryTransition.getActiveCategory(currentCategory)

        val contentX = (x + 32).toFloat()
        val contentY = (y + header.contentOffsetY).toFloat()
        val contentW = (menuWidth - 32).toFloat()
        val contentH = (menuHeight - header.contentOffsetY).toFloat()
        val transitioning = categoryTransition.isTransitioning()

        val drawLayers = {
            ModMenuClipCoordinator.withClip(
                nvg = nvg,
                x = contentX,
                y = contentY,
                width = contentW,
                height = contentH,
                layer = ModMenuClipCoordinator.ClipLayer.CONTENT_VIEWPORT,
                tag = "modmenu_content",
            ) {
                for (layer in categoryTransition.buildRenderLayers(contentW)) {
                    val cat = layer.category
                    ensureCategoryInitialized(cat)
                    nvg.withState {
                        if (layer.alpha < 0.999f) nvg.setAlpha(layer.alpha)
                        if (layer.offsetX != 0f) nvg.translate(layer.offsetX, 0f)
                        cat.prepareFrame(mouseX, mouseY, partialTicks)
                        cat.renderFrame(mouseX, mouseY, partialTicks)
                    }
                }
            }
        }

        if (transitioning) ScrollInputGuard.withLock { drawLayers() } else drawLayers()

        val visible = categoryTransition.collectVisibleCategories()
        categories.forEach { if (it.isInitialized() && it !in visible) it.setInitialized(false) }

        if (!transitioning &&
            MouseUtils.isInside(
                mouseX,
                mouseY,
                contentX,
                contentY,
                contentW,
                contentH,
            )
        ) {
            scroll.onScroll()
        }
        scroll.onAnimation()

        if (!transitioning &&
            !isOverlayInputLocked() &&
            currentCategory.isShowSearchBox() &&
            Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) &&
            Keyboard.isKeyDown(Keyboard.KEY_F)
        ) {
            currentCategory.getSearchBox().setFocused(true)
        }

        telemetryOverlay.endFrame()
        telemetryOverlay.draw(
            nvg = nvg,
            palette = palette,
            menuX = x.toFloat(),
            menuY = y.toFloat(),
            menuWidth = menuWidth.toFloat(),
        )
        ModMenuClipCoordinator.drawDebugOverlay(
            nvg = nvg,
            originX = x.toFloat(),
            originY = y.toFloat(),
            panelWidth = menuWidth.toFloat(),
        )
    }

    private fun drawLegacyFrame(
        nvg: NanoVGManager,
        palette: me.miki.shindo.management.color.palette.ColorPalette,
        accent: me.miki.shindo.management.color.AccentColor,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        blurEnabled: Boolean,
    ) {
        val radius = ModMenuStyle.ROOT_RADIUS
        val sidebarW = ModMenuStyle.SIDEBAR_WIDTH

        nvg.drawRoundedRect(x, y, width, height, radius, palette.getBackgroundColor(ColorType.NORMAL))

        val drawSidebar = {
            nvg.drawRoundedRectVarying(
                x,
                y,
                sidebarW,
                height,
                radius,
                0f,
                radius,
                0f,
                palette.getBackgroundColor(ColorType.DARK),
            )
        }

        if (blurEnabled) {
            Blur.drawBlur { drawSidebar() }
            val c = palette.getBackgroundColor(ColorType.DARK)
            nvg.drawRoundedRectVarying(
                x,
                y,
                sidebarW,
                height,
                radius,
                0f,
                radius,
                0f,
                Color(c.red, c.green, c.blue, 210),
            )
        } else {
            drawSidebar()
        }

        nvg.drawGradientRoundedRect(x + 5f, y + 7f, 22f, 22f, 11f, accent.getColor1(), accent.getColor2())
        nvg.drawText(Shinconic.SHINDO, x + 8f, y + 10f, Color.WHITE, 16f, Fonts.SHINCONIC)
    }

    override fun mouseClicked(
        mouseX: Int,
        mouseY: Int,
        mouseButton: Int,
    ) {
        val overlayLocked = isOverlayInputLocked()

        if (mouseButton == 0 &&
            canClose &&
            !overlayLocked &&
            !MouseUtils.isInside(
                mouseX,
                mouseY,
                x - 5f,
                y - 5f,
                (menuWidth + 10).toFloat(),
                (menuHeight + 10).toFloat(),
            )
        ) {
            introAnimation.setDirection(Direction.BACKWARDS)
        }

        rebuildSidebar()

        if (!overlayLocked) {
            sidebarController.resolveClickedCategory(mouseX, mouseY, mouseButton)?.let { requestCategorySwitch(it) }
        }

        if (!categoryTransition.isTransitioning()) {
            currentCategory.handleMouseClick(mouseX, mouseY, mouseButton)
            if (!overlayLocked) searchBox.mouseClicked(mouseX, mouseY, mouseButton)
        }

        if (!overlayLocked) {
            layoutButton.mouseClicked(mouseX, mouseY, mouseButton)
            folderButton.mouseClicked(mouseX, mouseY, mouseButton)
        }

        try {
            super.mouseClicked(mouseX, mouseY, mouseButton)
        } catch (_: IOException) {
        }
    }

    override fun mouseReleased(
        mouseX: Int,
        mouseY: Int,
        mouseButton: Int,
    ) {
        if (!categoryTransition.isTransitioning()) currentCategory.handleMouseRelease(mouseX, mouseY, mouseButton)
        if (!isOverlayInputLocked()) {
            layoutButton.mouseReleased(mouseX, mouseY, mouseButton)
            folderButton.mouseReleased(mouseX, mouseY, mouseButton)
        }
    }

    override fun keyTyped(
        typedChar: Char,
        keyCode: Int,
    ) {
        if (keyCode == Keyboard.KEY_F9 && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
            telemetryOverlay.toggle()
            return
        }
        if (keyCode == Keyboard.KEY_F10 && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
            ModMenuClipCoordinator.toggleDebugOverlay()
            return
        }

        val overlayLocked = isOverlayInputLocked()
        if (overlayLocked) {
            if (!categoryTransition.isTransitioning()) currentCategory.handleKeyInput(typedChar, keyCode)
            return
        }

        val shift = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)
        if (keyCode == Keyboard.KEY_TAB && canClose) {
            switchCategory(if (shift) -1 else 1)
            return
        }
        if ((keyCode == Keyboard.KEY_LEFT || keyCode == Keyboard.KEY_RIGHT) &&
            Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)
        ) {
            switchCategory(if (keyCode == Keyboard.KEY_LEFT) -1 else 1)
            return
        }

        if (categoryTransition.isTransitioning()) {
            if (keyCode == Keyboard.KEY_ESCAPE && canClose) introAnimation.setDirection(Direction.BACKWARDS)
            return
        }

        currentCategory.handleKeyInput(typedChar, keyCode)
        searchBox.keyTyped(typedChar, keyCode)

        if (keyCode == Keyboard.KEY_ESCAPE && canClose) {
            if (currentCategory.isShowSearchBox()) {
                when {
                    searchBox.getText().isNotEmpty() -> {
                        searchBox.setText("")
                        searchBox.setFocused(false)
                        return
                    }

                    searchBox.isFocused() -> {
                        searchBox.setFocused(false)
                        return
                    }
                }
            }
            introAnimation.setDirection(Direction.BACKWARDS)
        }
    }

    override fun doesGuiPauseGame() = false

    override fun onGuiClosed() = Shindo.getInstance().getProfileManager().save()

    fun getX() = x

    fun getY() = y

    fun getWidth() = menuWidth

    fun getHeight() = menuHeight

    fun getCategories() = categories

    fun getScroll() = scroll

    fun getSearchBox() = searchBox

    fun isCanClose() = canClose

    fun setCanClose(value: Boolean) {
        canClose = value
    }

    fun getCategoryByClass(clazz: Class<*>): Category =
        categories.firstOrNull { it.javaClass == clazz }
            ?: throw IllegalStateException("Category not found: ${clazz.name}")

    private fun rebuildSidebar() =
        sidebarController.rebuildSlots(
            categories = categories,
            currentCategory = currentCategory,
            startX = x + ModMenuStyle.SIDEBAR_ITEM_X,
            startY = y + ModMenuStyle.SIDEBAR_TOP_Y,
            slotSize = ModMenuStyle.SIDEBAR_ITEM_SIZE,
            gap = ModMenuStyle.SIDEBAR_ITEM_GAP,
            maxBottomY = y + menuHeight - ModMenuStyle.HUD_BUTTON_BOTTOM_MARGIN - ModMenuStyle.SIDEBAR_BOTTOM_PADDING,
        )

    private fun switchCategory(direction: Int) {
        if (categories.isEmpty()) return
        val idx = categories.indexOf(currentCategory).coerceAtLeast(0)
        requestCategorySwitch(categories[(idx + direction + categories.size) % categories.size], direction)
    }

    private fun requestCategorySwitch(
        target: Category,
        directionHint: Int? = null,
    ) {
        val active = categoryTransition.getActiveCategory(currentCategory)
        if (target == active) return
        val currentIdx = categories.indexOf(active).coerceAtLeast(0)
        val targetIdx = categories.indexOf(target).takeIf { it >= 0 } ?: return
        categoryTransition.requestSwitch(
            active,
            target,
            directionHint ?: resolveDirectionHint(currentIdx, targetIdx, categories.size),
        )
        currentCategory = target
    }

    private fun resolveDirectionHint(
        currentIdx: Int,
        targetIdx: Int,
        size: Int,
    ): Int {
        if (size <= 1 || currentIdx == targetIdx) return 1
        val fwd = (targetIdx - currentIdx + size) % size
        val bwd = (currentIdx - targetIdx + size) % size
        return if (fwd <= bwd) 1 else -1
    }

    private fun ensureCategoryInitialized(category: Category) {
        if (category.isInitialized()) return
        category.setInitialized(true)
        category.initCategory()
        searchBox.setText("")
        category.setCanClose(true)
    }

    private fun isOverlayInputLocked(): Boolean {
        if ((currentCategory as? ModMenuListPageContract)?.isDetailsLayerOpen() == true) return true
        return currentCategory.isAnySceneOpen()
    }
}

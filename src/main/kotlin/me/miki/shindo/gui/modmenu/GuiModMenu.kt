package me.miki.shindo.gui.modmenu

import eu.shoroa.contrib.render.Blur
import me.miki.shindo.Shindo
import me.miki.shindo.gui.GuiEditHUD
import me.miki.shindo.gui.IShindoScreen
import me.miki.shindo.gui.modmenu.category.Category
import me.miki.shindo.gui.modmenu.category.impl.*
import me.miki.shindo.gui.modmenu.category.list.ModMenuListPageContract
import me.miki.shindo.gui.modmenu.navigation.ModMenuCategoryTransitionCoordinator
import me.miki.shindo.gui.modmenu.navigation.ModMenuSidebarController
import me.miki.shindo.gui.modmenu.render.ModMenuClipCoordinator
import me.miki.shindo.gui.modmenu.render.ModMenuHeaderController
import me.miki.shindo.gui.modmenu.render.ModMenuTelemetryOverlay
import me.miki.shindo.gui.modmenu.style.ModMenuResponsiveLayout
import me.miki.shindo.gui.modmenu.style.ModMenuStyle
import me.miki.shindo.management.color.AccentColor
import me.miki.shindo.management.color.ColorManager
import me.miki.shindo.management.color.palette.ColorPalette
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.event.impl.EventRenderNotification
import me.miki.shindo.management.mods.impl.InternalSettingsMod
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.ui.animation.Animation
import me.miki.shindo.ui.animation.Direction
import me.miki.shindo.ui.animation.easing.EaseBackIn
import me.miki.shindo.ui.animation.screen.ScreenAnimation
import me.miki.shindo.ui.comp.buttons.CompIconButton
import me.miki.shindo.ui.comp.inputs.CompSearchBox
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

/**
 * Root ModMenu screen.
 *
 * This version keeps legacy category behavior but moves shell painting to
 * dedicated style/renderer classes for easier migration of each category UI.
 */
class GuiModMenu(
    private val initialCategoryClass: Class<out Category>? = null,
    private val initialSearchText: String? = null
) : GuiScreen(), IShindoScreen {

    private val categories = ArrayList<Category>()
    private val screenAnimation = ScreenAnimation()
    private val scroll = Scroll()
    private val searchBox = CompSearchBox()
    private val layoutButton = CompIconButton(21f) { LegacyIcon.LAYOUT }
    private val folderButton = CompIconButton(18f) { LegacyIcon.FOLDER }
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
    private var toEditHUD = false
    private var canClose = false

    init {
        categories.add(HomeCategory(this))
        categories.add(ModuleCategory(this))
        categories.add(AddonCategory(this))
        categories.add(CosmeticsCategory(this))
        categories.add(SpotifyCategory(this))
        categories.add(ProfileCategory(this))

        categories.add(ScreenshotCategory(this))
        //categories.add(NetworkCategory(this))
        categories.add(SettingsCategory(this))

        currentCategory = getCategoryByClass(HomeCategory::class.java)
    }

    override fun initGui() {
        val sr = ScaledResolution(mc)
        val bounds = ModMenuResponsiveLayout.resolve(sr)
        x = bounds.x
        y = bounds.y
        menuWidth = bounds.width
        menuHeight = bounds.height

        introAnimation = EaseBackIn(320, 1.0, 2.0f)
        introAnimation.setDirection(Direction.FORWARDS)

        for (c in categories) {
            c.initGui()
        }

        scroll.resetAll()
        toEditHUD = false
        canClose = true
        initialCategoryClass?.let { currentCategory = getCategoryByClass(it) }
        searchBox.setText(initialSearchText ?: "")
        categoryTransition.reset(currentCategory)

        layoutButton.onClick {
            toEditHUD = true
            introAnimation.setDirection(Direction.BACKWARDS)
        }
        layoutButton.setFontSize(14f)
        layoutButton.setRadius(6f)
        layoutButton.enabledWhen { canClose }

        folderButton.setRadius(6f)
        folderButton.setFontSize(9f)
        folderButton.setVisible(false)
        folderButton.onClick {
            FileUtils.openFolderAtPath(Shindo.getInstance().fileManager.customCapeDir)
        }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val instance = Shindo.getInstance()
        val nvg: NanoVGManager = instance.nanoVGManager ?: return

        if (InternalSettingsMod.instance.getBlurSetting()?.isToggled() == true) {
            BlurUtils.drawBlurScreen(min(introAnimation.getValue(), 1.0).toFloat() * 20f + 1f)
        }
        screenAnimation.wrap(Runnable {
            nvg.drawShadow(x.toFloat(), y.toFloat(), menuWidth.toFloat(), menuHeight.toFloat(), 12f)
        }, (2f - introAnimation.getValueFloat()), min(introAnimation.getValueFloat(), 1f))

        screenAnimation.wrap(
            Runnable { drawNanoVG(mouseX, mouseY, partialTicks) },
            x.toFloat(),
            y.toFloat(),
            menuWidth.toFloat(),
            menuHeight.toFloat(),
            (2f - introAnimation.getValueFloat()),
            min(introAnimation.getValueFloat(), 1f),
            true
        )

        EventRenderNotification().call()

        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    private fun drawNanoVG(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val instance = Shindo.getInstance()
        val nvg: NanoVGManager = instance.nanoVGManager ?: return
        val colorManager: ColorManager = instance.colorManager
        val palette: ColorPalette = colorManager.getPalette()
        val currentColor: AccentColor = colorManager.getCurrentColor()
        val blurEnabled = InternalSettingsMod.instance.getBlurSetting()?.isToggled() == true
        telemetryOverlay.beginFrame()
        ModMenuClipCoordinator.beginFrame()

        if (introAnimation.isDone(Direction.BACKWARDS)) {
            mc.displayGuiScreen(if (toEditHUD) GuiEditHUD(true) else null)
        }

        drawLegacyFrame(
            nvg = nvg,
            palette = palette,
            accent = currentColor,
            x = x.toFloat(),
            y = y.toFloat(),
            width = menuWidth.toFloat(),
            height = menuHeight.toFloat(),
            radius = ModMenuStyle.ROOT_RADIUS,
            sidebarWidth = ModMenuStyle.SIDEBAR_WIDTH,
            blurEnabled = blurEnabled
        )

        sidebarController.rebuildSlots(
            categories = categories,
            currentCategory = currentCategory,
            startX = x + ModMenuStyle.SIDEBAR_ITEM_X,
            startY = y + ModMenuStyle.SIDEBAR_TOP_Y,
            slotSize = ModMenuStyle.SIDEBAR_ITEM_SIZE,
            gap = ModMenuStyle.SIDEBAR_ITEM_GAP,
            maxBottomY = y + menuHeight - ModMenuStyle.HUD_BUTTON_BOTTOM_MARGIN - ModMenuStyle.SIDEBAR_BOTTOM_PADDING
        )
        nvg.save()
        sidebarController.draw(
            nvg = nvg,
            palette = palette,
            accent = currentColor,
            currentCategory = currentCategory,
            mouseX = mouseX,
            mouseY = mouseY
        )
        nvg.restore()

        layoutButton.setBounds(
            x + ModMenuStyle.HUD_BUTTON_X,
            y + menuHeight - ModMenuStyle.HUD_BUTTON_BOTTOM_MARGIN,
            ModMenuStyle.HUD_BUTTON_SIZE,
            ModMenuStyle.HUD_BUTTON_SIZE
        )
        layoutButton.draw(mouseX, mouseY, partialTicks)
        val header = headerController.draw(
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
            folderButton = folderButton
        )
        val contentOffsetY = header.contentOffsetY

        categoryTransition.update()
        currentCategory = categoryTransition.getActiveCategory(currentCategory)

        val contentX = (x + 32).toFloat()
        val contentY = (y + contentOffsetY).toFloat()
        val contentWidth = (menuWidth - 32).toFloat()
        val contentHeight = (menuHeight - contentOffsetY).toFloat()
        val transitionRunning = categoryTransition.isTransitioning()

        val drawCategoryLayers = {
            ModMenuClipCoordinator.withClip(
                nvg = nvg,
                x = contentX,
                y = contentY,
                width = contentWidth,
                height = contentHeight,
                layer = ModMenuClipCoordinator.ClipLayer.CONTENT_VIEWPORT,
                tag = "modmenu_content"
            ) {
                for (layer in categoryTransition.buildRenderLayers(contentWidth)) {
                    val category = layer.category
                    ensureCategoryInitialized(category)

                    nvg.withState {
                        if (layer.alpha < 0.999f) {
                            nvg.setAlpha(layer.alpha)
                        }
                        if (layer.offsetX != 0f) {
                            nvg.translate(layer.offsetX, 0f)
                        }

                        category.prepareFrame(mouseX, mouseY, partialTicks)
                        category.renderFrame(mouseX, mouseY, partialTicks)
                    }
                }
            }
        }

        if (transitionRunning) {
            ScrollInputGuard.withLock {
                drawCategoryLayers()
            }
        } else {
            drawCategoryLayers()
        }

        val visibleCategories = categoryTransition.collectVisibleCategories()
        for (category in categories) {
            if (category.isInitialized() && !visibleCategories.contains(category)) {
                category.setInitialized(false)
            }
        }

        if (!transitionRunning && MouseUtils.isInside(
                mouseX,
                mouseY,
                contentX,
                contentY,
                contentWidth,
                contentHeight
            )
        ) {
            scroll.onScroll()
        }

        scroll.onAnimation()

        if (!transitionRunning && !isOverlayInputLocked() && currentCategory.isShowSearchBox() && Keyboard.isKeyDown(
                Keyboard.KEY_LCONTROL
            ) && Keyboard.isKeyDown(
                Keyboard.KEY_F
            )
        ) {
            currentCategory.getSearchBox().setFocused(true)
        }

        telemetryOverlay.endFrame()
        telemetryOverlay.draw(
            nvg = nvg,
            palette = palette,
            menuX = x.toFloat(),
            menuY = y.toFloat(),
            menuWidth = menuWidth.toFloat()
        )

        ModMenuClipCoordinator.drawDebugOverlay(
            nvg = nvg,
            originX = x.toFloat(),
            originY = y.toFloat(),
            panelWidth = menuWidth.toFloat()
        )
    }

    private fun drawLegacyFrame(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accent: AccentColor,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        radius: Float,
        sidebarWidth: Float,
        blurEnabled: Boolean
    ) {
        nvg.drawRoundedRect(
            x,
            y,
            width,
            height,
            radius,
            palette.getBackgroundColor(ColorType.NORMAL)
        )

        if (blurEnabled) {
            Blur.drawBlur {
                nvg.drawRoundedRectVarying(
                    x,
                    y,
                    sidebarWidth,
                    height,
                    radius,
                    0f,
                    radius,
                    0f,
                    palette.getBackgroundColor(ColorType.DARK)
                )
            }
            val sidebarColor = palette.getBackgroundColor(ColorType.DARK)
            nvg.drawRoundedRectVarying(
                x,
                y,
                sidebarWidth,
                height,
                radius,
                0f,
                radius,
                0f,
                Color(sidebarColor.red, sidebarColor.green, sidebarColor.blue, 210)
            )
        } else {
            nvg.drawRoundedRectVarying(
                x,
                y,
                sidebarWidth,
                height,
                radius,
                0f,
                radius,
                0f,
                palette.getBackgroundColor(ColorType.DARK)
            )
        }

        nvg.drawGradientRoundedRect(x + 5f, y + 7f, 22f, 22f, 11f, accent.getColor1(), accent.getColor2())
        nvg.drawText(LegacyIcon.SHINDO, x + 8f, y + 10f, Color.WHITE, 16f, Fonts.LEGACYICON)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        val overlayInputLocked = isOverlayInputLocked()

        if (!MouseUtils.isInside(
                mouseX,
                mouseY,
                x - 5f,
                y - 5f,
                (menuWidth + 10).toFloat(),
                (menuHeight + 10).toFloat()
            ) && mouseButton == 0 && canClose && !overlayInputLocked
        ) {
            introAnimation.setDirection(Direction.BACKWARDS)
        }

        sidebarController.rebuildSlots(
            categories = categories,
            currentCategory = currentCategory,
            startX = x + ModMenuStyle.SIDEBAR_ITEM_X,
            startY = y + ModMenuStyle.SIDEBAR_TOP_Y,
            slotSize = ModMenuStyle.SIDEBAR_ITEM_SIZE,
            gap = ModMenuStyle.SIDEBAR_ITEM_GAP,
            maxBottomY = y + menuHeight - ModMenuStyle.HUD_BUTTON_BOTTOM_MARGIN - ModMenuStyle.SIDEBAR_BOTTOM_PADDING
        )
        if (!overlayInputLocked) {
            sidebarController.resolveClickedCategory(mouseX, mouseY, mouseButton)?.let { clicked ->
                requestCategorySwitch(clicked)
            }
        }

        if (!categoryTransition.isTransitioning()) {
            currentCategory.handleMouseClick(mouseX, mouseY, mouseButton)
            if (!overlayInputLocked) {
                searchBox.mouseClicked(mouseX, mouseY, mouseButton)
            }
        }

        if (!overlayInputLocked) {
            layoutButton.mouseClicked(mouseX, mouseY, mouseButton)
            folderButton.mouseClicked(mouseX, mouseY, mouseButton)
        }
        try {
            super.mouseClicked(mouseX, mouseY, mouseButton)
        } catch (_: IOException) {
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, mouseButton: Int) {
        val overlayInputLocked = isOverlayInputLocked()
        if (!categoryTransition.isTransitioning()) {
            currentCategory.handleMouseRelease(mouseX, mouseY, mouseButton)
        }
        if (!overlayInputLocked) {
            layoutButton.mouseReleased(mouseX, mouseY, mouseButton)
            folderButton.mouseReleased(mouseX, mouseY, mouseButton)
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (keyCode == Keyboard.KEY_F9 && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
            telemetryOverlay.toggle()
            return
        }
        if (keyCode == Keyboard.KEY_F10 && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
            ModMenuClipCoordinator.toggleDebugOverlay()
            return
        }

        val overlayInputLocked = isOverlayInputLocked()
        if (overlayInputLocked) {
            if (!categoryTransition.isTransitioning()) {
                currentCategory.handleKeyInput(typedChar, keyCode)
            }
            return
        }

        if (keyCode == Keyboard.KEY_TAB && canClose) {
            val backwards = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)
            switchCategory(if (backwards) -1 else 1)
            return
        }

        if ((keyCode == Keyboard.KEY_LEFT || keyCode == Keyboard.KEY_RIGHT) && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
            switchCategory(if (keyCode == Keyboard.KEY_LEFT) -1 else 1)
            return
        }

        if (categoryTransition.isTransitioning()) {
            if (keyCode == Keyboard.KEY_ESCAPE && canClose) {
                introAnimation.setDirection(Direction.BACKWARDS)
            }
            return
        }

        currentCategory.handleKeyInput(typedChar, keyCode)
        searchBox.keyTyped(typedChar, keyCode)

        if (currentCategory.isShowSearchBox() && canClose) {
            if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
                if (searchBox.getText().isNotEmpty()) {
                    searchBox.setText("")
                    searchBox.setFocused(false)
                    return
                }

                if (searchBox.isFocused()) {
                    searchBox.setFocused(false)
                    return
                }
            }
        }

        if (keyCode == Keyboard.KEY_ESCAPE && canClose) {
            introAnimation.setDirection(Direction.BACKWARDS)
        }
    }

    override fun doesGuiPauseGame(): Boolean {
        return false
    }

    override fun onGuiClosed() {
        Shindo.getInstance().profileManager.save()
    }

    fun getX(): Int = x
    fun getY(): Int = y
    fun getWidth(): Int = menuWidth
    fun getHeight(): Int = menuHeight
    fun getCategories(): ArrayList<Category> = categories

    fun getCategoryByClass(clazz: Class<*>): Category {
        for (c in categories) {
            if (c.javaClass == clazz) {
                return c
            }
        }
        throw IllegalStateException("Category not found: " + clazz.name)
    }

    fun getScroll(): Scroll = scroll
    fun getSearchBox(): CompSearchBox = searchBox

    fun isCanClose(): Boolean = canClose
    fun setCanClose(canClose: Boolean) {
        this.canClose = canClose
    }

    private fun switchCategory(direction: Int) {
        if (categories.isEmpty()) {
            return
        }

        val currentIndex = categories.indexOf(currentCategory).coerceAtLeast(0)
        val size = categories.size
        val targetIndex = (currentIndex + direction + size) % size
        requestCategorySwitch(categories[targetIndex], direction)
    }

    private fun requestCategorySwitch(targetCategory: Category, directionHint: Int? = null) {
        val activeCategory = categoryTransition.getActiveCategory(currentCategory)
        if (targetCategory == activeCategory) {
            return
        }

        val currentIndex = categories.indexOf(activeCategory).coerceAtLeast(0)
        val targetIndex = categories.indexOf(targetCategory)
        if (targetIndex < 0) {
            return
        }
        val resolvedDirection = directionHint ?: resolveDirectionHint(currentIndex, targetIndex, categories.size)

        categoryTransition.requestSwitch(activeCategory, targetCategory, resolvedDirection)
        currentCategory = targetCategory
    }

    private fun resolveDirectionHint(currentIndex: Int, targetIndex: Int, size: Int): Int {
        if (size <= 1 || currentIndex == targetIndex) {
            return 1
        }

        val forwardDistance = (targetIndex - currentIndex + size) % size
        val backwardDistance = (currentIndex - targetIndex + size) % size
        return if (forwardDistance <= backwardDistance) 1 else -1
    }

    private fun ensureCategoryInitialized(category: Category) {
        if (category.isInitialized()) {
            return
        }

        category.setInitialized(true)
        category.initCategory()
        searchBox.setText("")
        category.setCanClose(true)
    }

    private fun isOverlayInputLocked(): Boolean {
        val listCategory = currentCategory as? ModMenuListPageContract ?: return false
        return listCategory.isDetailsLayerOpen()
    }
}

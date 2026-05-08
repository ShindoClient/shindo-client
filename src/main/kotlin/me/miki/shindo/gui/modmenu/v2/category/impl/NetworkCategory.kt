package me.miki.shindo.gui.modmenu.v2.category.impl

import me.miki.shindo.Shindo
import me.miki.shindo.gui.modmenu.v2.GuiModMenu
import me.miki.shindo.gui.modmenu.v2.category.Category
import me.miki.shindo.gui.modmenu.v2.render.ModMenuClipCoordinator
import me.miki.shindo.gui.modmenu.v2.style.ModMenuMotion
import me.miki.shindo.management.color.AccentColor
import me.miki.shindo.management.color.palette.ColorPalette
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.network.NetworkManager
import me.miki.shindo.management.network.proxy.CustomProxy
import me.miki.shindo.ui.animation.v2.Animation
import me.miki.shindo.ui.animation.v2.Direction
import me.miki.shindo.ui.animation.v2.curve.SmoothStepAnimation
import me.miki.shindo.ui.components.v2.chips.CategoryChipRenderer
import me.miki.shindo.ui.components.v2.chips.FilterChip
import me.miki.shindo.ui.components.v2.inputs.CompTextBox
import me.miki.shindo.ui.components.v2.layout.CompAddProxyCard
import me.miki.shindo.ui.components.v2.layout.CompProxyCard
import me.miki.shindo.utils.ColorUtils
import me.miki.shindo.utils.mouse.MouseUtils
import me.miki.shindo.utils.mouse.Scroll
import org.lwjgl.input.Keyboard
import kotlin.math.max

private enum class NetworkPage(val label: String) {
    GENERAL("General"),
    PROXY("Proxy")
}

private enum class ProxySectionFilter(val label: String) {
    ALL("All"),
    PRESET("Preset"),
    CUSTOM("Custom")
}

class NetworkCategory(parent: GuiModMenu) :
    Category(parent, TranslateText.NETWORK, LegacyIcon.GLOBE, false, true) {

    private val pageChips = ArrayList<FilterChip>()
    private val sectionChips = ArrayList<FilterChip>()

    private val proxyScroll = Scroll()
    private val cloudflareCard = CompProxyCard()
    private val addProxyCard = CompAddProxyCard()
    private val proxyCardPool = LinkedHashMap<String, CompProxyCard>()
    private val visibleProxyCards = ArrayList<CompProxyCard>()

    private val nameBox = CompTextBox()
    private val primaryDNSBox = CompTextBox()
    private val secondaryDNSBox = CompTextBox()

    private var currentPage = NetworkPage.GENERAL
    private var sectionFilter = ProxySectionFilter.ALL

    private var showProxyForm = false
    private var editingProxyId: String? = null
    private var formAnimation: Animation = SmoothStepAnimation(ModMenuMotion.DETAILS_PANEL_ANIMATION_MS, 1.0)

    private var contentOffsetX = 0f
    private var panelOffsetX = ModMenuMotion.DETAILS_PANEL_SLIDE_DISTANCE
    private var proxyScrollY = 0f

    private var panelX = 0f
    private var panelY = 0f
    private var panelWidth = 0f
    private var panelHeight = 0f
    private var proxyListViewportX = 0f
    private var proxyListViewportY = 0f
    private var proxyListViewportWidth = 0f
    private var proxyListViewportHeight = 0f

    init {
        addProxyCard.label = "Add Proxy"
        addProxyCard.onClick = { openCreateForm() }
        cloudflareCard.onToggleClick = { toggleCloudflare(Shindo.getInstance().getNetworkManager()) }
    }

    override fun initGui() {
        resetState()
    }

    override fun initCategory() {
        resetState()
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val instance = Shindo.getInstance()
        val nvg = instance.nanoVGManager ?: return
        val palette = instance.getColorManager().getPalette()
        val accent = instance.getColorManager().getCurrentColor()
        val networkManager = instance.getNetworkManager()

        updateAnimationState()

        val contentMouseX = (mouseX - contentOffsetX).toInt()
        ModMenuClipCoordinator.withClip(
            nvg = nvg,
            x = getX().toFloat(),
            y = getY().toFloat(),
            width = getWidth().toFloat(),
            height = getHeight().toFloat(),
            layer = ModMenuClipCoordinator.ClipLayer.CATEGORY_CONTENT
        ) {
            nvg.save()
            nvg.translate(contentOffsetX, 0f)
            val pageChipBottom = drawPageChips(nvg, palette, accent, contentMouseX, mouseY)

            when (currentPage) {
                NetworkPage.GENERAL -> drawGeneralPage(
                    nvg,
                    palette,
                    accent,
                    networkManager,
                    pageChipBottom + SECTION_SPACING
                )

                NetworkPage.PROXY -> drawProxyPage(
                    nvg,
                    palette,
                    accent,
                    networkManager,
                    contentMouseX,
                    mouseY,
                    pageChipBottom + SECTION_SPACING
                )
            }
            nvg.restore()
        }

        drawProxyForm(nvg, palette, accent, mouseX, mouseY, partialTicks)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (mouseButton != 0) return

        if (isFormInteractionLocked()) {
            if (showProxyForm) {
                handleFormClick(mouseX, mouseY)
            }
            return
        }

        val contentMouseX = (mouseX - contentOffsetX).toInt()

        for (chip in pageChips) {
            if (chip.contains(contentMouseX, mouseY)) {
                chip.click()
                return
            }
        }

        if (currentPage != NetworkPage.PROXY) return

        for (chip in sectionChips) {
            if (chip.contains(contentMouseX, mouseY)) {
                chip.click()
                return
            }
        }

        if (sectionFilter != ProxySectionFilter.CUSTOM) {
            cloudflareCard.mouseClicked(contentMouseX, mouseY, mouseButton)
        }

        if (sectionFilter != ProxySectionFilter.PRESET) {
            if (MouseUtils.isInside(
                    contentMouseX,
                    mouseY,
                    proxyListViewportX,
                    proxyListViewportY,
                    proxyListViewportWidth,
                    proxyListViewportHeight
                )
            ) {
                val listMouseY = (mouseY - proxyScrollY).toInt()
                for (card in visibleProxyCards) {
                    card.mouseClicked(contentMouseX, listMouseY, mouseButton)
                }
                addProxyCard.mouseClicked(contentMouseX, listMouseY, mouseButton)
            }
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (!showProxyForm) return

        nameBox.keyTyped(typedChar, keyCode)
        primaryDNSBox.keyTyped(typedChar, keyCode)
        secondaryDNSBox.keyTyped(typedChar, keyCode)

        if (keyCode == Keyboard.KEY_ESCAPE) {
            closeForm()
        }
    }

    private fun drawPageChips(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accent: AccentColor,
        mouseX: Int,
        mouseY: Int
    ): Float {
        pageChips.clear()
        val startX = getX() + CONTENT_PADDING
        var xCursor = startX
        val y = getY() + CHIP_TOP_PADDING

        for (page in NetworkPage.values()) {
            val chipWidth = CategoryChipRenderer.computeWidth(nvg, page.label, null)
            val active = currentPage == page
            val hovered = !isFormInteractionLocked() && MouseUtils.isInside(
                mouseX, mouseY, xCursor, y, chipWidth, CategoryChipRenderer.CHIP_HEIGHT
            )
            CategoryChipRenderer.drawChip(
                nvg, palette, accent, xCursor, y, chipWidth, page.label, null, active, hovered
            )

            val chip = FilterChip(Runnable {
                if (currentPage != page) {
                    currentPage = page
                    sectionFilter = ProxySectionFilter.ALL
                    proxyScroll.resetAll()
                }
            })
            chip.setBounds(xCursor, y, chipWidth, CategoryChipRenderer.CHIP_HEIGHT)
            pageChips.add(chip)
            xCursor += chipWidth + CHIP_GAP
        }

        return y + CategoryChipRenderer.CHIP_HEIGHT
    }

    private fun drawProxySectionChips(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accent: AccentColor,
        mouseX: Int,
        mouseY: Int,
        y: Float
    ): Float {
        sectionChips.clear()
        val startX = getX() + CONTENT_PADDING
        var xCursor = startX

        for (section in ProxySectionFilter.values()) {
            val chipWidth = CategoryChipRenderer.computeWidth(nvg, section.label, null)
            val active = sectionFilter == section
            val hovered = !isFormInteractionLocked() && MouseUtils.isInside(
                mouseX, mouseY, xCursor, y, chipWidth, CategoryChipRenderer.CHIP_HEIGHT
            )
            CategoryChipRenderer.drawChip(
                nvg, palette, accent, xCursor, y, chipWidth, section.label, null, active, hovered
            )

            val chip = FilterChip(Runnable { sectionFilter = section })
            chip.setBounds(xCursor, y, chipWidth, CategoryChipRenderer.CHIP_HEIGHT)
            sectionChips.add(chip)
            xCursor += chipWidth + CHIP_GAP
        }

        return y + CategoryChipRenderer.CHIP_HEIGHT
    }

    private fun drawGeneralPage(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accent: AccentColor,
        networkManager: NetworkManager,
        startY: Float
    ) {
        val x = getX() + CONTENT_PADDING
        val width = getWidth() - CONTENT_PADDING * 2f
        val cardHeight = 76f

        drawInfoCard(
            nvg,
            palette,
            accent,
            x,
            startY,
            width,
            cardHeight,
            "Current DNS",
            networkManager.getCurrentDNSInfo()
        )
        drawInfoCard(
            nvg,
            palette,
            accent,
            x,
            startY + cardHeight + CARD_GAP,
            width,
            cardHeight,
            "Proxy Mode",
            networkManager.getActiveProxyType().name
        )
    }

    private fun drawInfoCard(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accent: AccentColor,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        title: String,
        value: String
    ) {
        nvg.drawShadow(x, y, width, height, 12f, 6)
        nvg.drawRoundedRect(
            x,
            y,
            width,
            height,
            12f,
            ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 212)
        )
        nvg.drawOutlineRoundedRect(
            x,
            y,
            width,
            height,
            12f,
            1f,
            ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 210)
        )
        nvg.drawRoundedRect(
            x + 10f,
            y + 13f,
            3f,
            height - 26f,
            1.5f,
            ColorUtils.applyAlpha(accent.getInterpolateColor(), 178)
        )
        nvg.drawText(title, x + 16f, y + 18f, palette.getFontColor(ColorType.NORMAL), 9f, Fonts.MEDIUM)
        nvg.drawText(
            nvg.getLimitText(value, 11f, Fonts.SEMIBOLD, width - 32f),
            x + 16f,
            y + 38f,
            palette.getFontColor(ColorType.DARK),
            11f,
            Fonts.SEMIBOLD
        )
    }

    private fun drawProxyPage(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accent: AccentColor,
        networkManager: NetworkManager,
        mouseX: Int,
        mouseY: Int,
        startY: Float
    ) {
        var yCursor = drawProxySectionChips(nvg, palette, accent, mouseX, mouseY, startY) + SECTION_SPACING
        val x = getX() + CONTENT_PADDING
        val width = getWidth() - CONTENT_PADDING * 2f

        if (sectionFilter != ProxySectionFilter.CUSTOM) {
            syncCloudflareCard(networkManager)
            cloudflareCard.setBounds(x, yCursor, width, PROXY_CARD_HEIGHT)
            cloudflareCard.draw(mouseX, mouseY, 0f)
            yCursor += PROXY_CARD_HEIGHT + CARD_GAP
        }

        if (sectionFilter == ProxySectionFilter.PRESET) {
            visibleProxyCards.clear()
            proxyListViewportX = 0f
            proxyListViewportY = 0f
            proxyListViewportWidth = 0f
            proxyListViewportHeight = 0f
            return
        }

        drawCustomProxyList(nvg, networkManager, mouseX, mouseY, x, yCursor, width)
    }

    private fun drawCustomProxyList(
        nvg: NanoVGManager,
        networkManager: NetworkManager,
        mouseX: Int,
        mouseY: Int,
        x: Float,
        y: Float,
        width: Float
    ) {
        visibleProxyCards.clear()

        val proxies = networkManager.proxyManager.getCustomProxies()
        val viewportHeight = max(0f, getHeight() - (y - getY()) - CONTENT_BOTTOM_PADDING)
        val totalHeight = (proxies.size + 1) * (PROXY_CARD_HEIGHT + PROXY_CARD_GAP) - PROXY_CARD_GAP
        proxyScroll.maxScroll = max(0f, totalHeight - viewportHeight)
        proxyListViewportX = x
        proxyListViewportY = y
        proxyListViewportWidth = width
        proxyListViewportHeight = viewportHeight

        if (!isFormInteractionLocked() && MouseUtils.isInside(mouseX, mouseY, x, y, width, viewportHeight)) {
            proxyScroll.onScroll()
        }
        proxyScroll.onAnimation()
        proxyScrollY = proxyScroll.getValue()
        val listMouseY = (mouseY - proxyScrollY).toInt()

        ModMenuClipCoordinator.withClipTranslate(
            nvg = nvg,
            x = x,
            y = y,
            width = width,
            height = viewportHeight,
            translateX = 0f,
            translateY = proxyScrollY,
            intersect = true,
            layer = ModMenuClipCoordinator.ClipLayer.CATEGORY_CONTENT
        ) {
            var cardY = y
            for (proxy in proxies) {
                val card = proxyCardPool.getOrPut(proxy.id) { CompProxyCard() }
                syncProxyCard(card, proxy, networkManager.getActiveCustomProxyId() == proxy.id)
                card.setBounds(x, cardY, width, PROXY_CARD_HEIGHT)

                val displayY = cardY + proxyScrollY
                if (displayY + PROXY_CARD_HEIGHT >= y && displayY <= y + viewportHeight) {
                    card.draw(mouseX, listMouseY, 0f)
                    visibleProxyCards.add(card)
                }

                cardY += PROXY_CARD_HEIGHT + PROXY_CARD_GAP
            }

            addProxyCard.setBounds(x, cardY, width, PROXY_CARD_HEIGHT)
            val addDisplayY = cardY + proxyScrollY
            if (addDisplayY + PROXY_CARD_HEIGHT >= y && addDisplayY <= y + viewportHeight) {
                addProxyCard.draw(mouseX, listMouseY, 0f)
            }
        }
    }

    private fun drawProxyForm(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accent: AccentColor,
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float
    ) {
        if (!showProxyForm && formAnimation.isDone(Direction.FORWARDS)) {
            return
        }

        panelX = getX() + CONTENT_PADDING
        panelY = getY() + 15f
        panelWidth = getWidth() - CONTENT_PADDING * 2f
        panelHeight = getHeight() - 30f

        val panelMouseX = (mouseX - panelOffsetX).toInt()
        ModMenuClipCoordinator.withClip(
            nvg = nvg,
            x = getX().toFloat(),
            y = getY().toFloat(),
            width = getWidth().toFloat(),
            height = getHeight().toFloat(),
            layer = ModMenuClipCoordinator.ClipLayer.OVERLAY
        ) {
            nvg.save()
            nvg.translate(panelOffsetX, 0f)
            nvg.drawShadow(panelX, panelY, panelWidth, panelHeight, 12f, 7)
            nvg.drawRoundedRect(
                panelX,
                panelY,
                panelWidth,
                panelHeight,
                12f,
                palette.getBackgroundColor(ColorType.DARK)
            )
            nvg.drawOutlineRoundedRect(
                panelX,
                panelY,
                panelWidth,
                panelHeight,
                12f,
                1.1f,
                ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 220)
            )

            nvg.drawText(
                if (editingProxyId == null) "Add Custom Proxy" else "Edit Proxy",
                panelX + 24f,
                panelY + 22f,
                palette.getFontColor(ColorType.DARK),
                14f,
                Fonts.SEMIBOLD
            )

            val fieldWidth = (panelWidth - 48f) / 2f - 15f
            val fieldStartY = panelY + 62f

            nvg.drawText("Name", panelX + 24f, fieldStartY, palette.getFontColor(ColorType.DARK), 11f, Fonts.MEDIUM)
            nameBox.setPosition(panelX + 24f, fieldStartY + 20f, fieldWidth, 20f)
            nameBox.setDefaultText("Proxy name")
            nameBox.draw(panelMouseX, mouseY, partialTicks)

            nvg.drawText(
                "Primary DNS",
                panelX + 24f,
                fieldStartY + 42f,
                palette.getFontColor(ColorType.DARK),
                11f,
                Fonts.MEDIUM
            )
            primaryDNSBox.setPosition(panelX + 24f, fieldStartY + 62f, fieldWidth, 20f)
            primaryDNSBox.setDefaultText("1.1.1.1")
            primaryDNSBox.draw(panelMouseX, mouseY, partialTicks)

            val secondColumnX = panelX + 24f + fieldWidth + 30f
            nvg.drawText(
                "Secondary DNS",
                secondColumnX,
                fieldStartY + 42f,
                palette.getFontColor(ColorType.DARK),
                11f,
                Fonts.MEDIUM
            )
            secondaryDNSBox.setPosition(secondColumnX, fieldStartY + 62f, fieldWidth, 20f)
            secondaryDNSBox.setDefaultText("Optional")
            secondaryDNSBox.draw(panelMouseX, mouseY, partialTicks)

            val buttonY = panelY + panelHeight - FORM_BUTTON_HEIGHT - 20f
            val cancelX = panelX + panelWidth - FORM_BUTTON_WIDTH * 2f - 30f
            val saveX = panelX + panelWidth - FORM_BUTTON_WIDTH - 20f

            val cancelHovered =
                MouseUtils.isInside(panelMouseX, mouseY, cancelX, buttonY, FORM_BUTTON_WIDTH, FORM_BUTTON_HEIGHT)
            val saveHovered =
                MouseUtils.isInside(panelMouseX, mouseY, saveX, buttonY, FORM_BUTTON_WIDTH, FORM_BUTTON_HEIGHT)
            nvg.drawRoundedRect(
                cancelX, buttonY, FORM_BUTTON_WIDTH, FORM_BUTTON_HEIGHT, 6f,
                if (cancelHovered) palette.getBackgroundColor(ColorType.MID) else palette.getBackgroundColor(ColorType.NORMAL)
            )
            if (cancelHovered) {
                nvg.drawOutlineRoundedRect(
                    cancelX,
                    buttonY,
                    FORM_BUTTON_WIDTH,
                    FORM_BUTTON_HEIGHT,
                    6f,
                    1f,
                    ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 110)
                )
            }
            nvg.drawCenteredText(
                "Cancel",
                cancelX + FORM_BUTTON_WIDTH / 2f,
                buttonY + FORM_BUTTON_HEIGHT / 2f,
                palette.getFontColor(ColorType.NORMAL),
                10f,
                Fonts.MEDIUM
            )

            nvg.drawRoundedRect(
                saveX,
                buttonY,
                FORM_BUTTON_WIDTH,
                FORM_BUTTON_HEIGHT,
                6f,
                if (saveHovered) palette.getBackgroundColor(ColorType.MID) else palette.getBackgroundColor(ColorType.NORMAL)
            )

            if (saveHovered) {
                nvg.drawOutlineRoundedRect(
                    saveX,
                    buttonY,
                    FORM_BUTTON_WIDTH,
                    FORM_BUTTON_HEIGHT,
                    6f,
                    1f,
                    ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 110)
                )
            }
            nvg.drawCenteredText(
                "Save",
                saveX + FORM_BUTTON_WIDTH / 2f,
                buttonY + FORM_BUTTON_HEIGHT / 2f,
                palette.getFontColor(ColorType.NORMAL),
                10f,
                Fonts.MEDIUM
            )

            nvg.restore()
        }
    }

    private fun handleFormClick(mouseX: Int, mouseY: Int) {
        val panelMouseX = (mouseX - panelOffsetX).toInt()
        val insidePanel = MouseUtils.isInside(panelMouseX, mouseY, panelX, panelY, panelWidth, panelHeight)
        if (!insidePanel) {
            closeForm()
            return
        }

        nameBox.mouseClicked(panelMouseX, mouseY, 0)
        primaryDNSBox.mouseClicked(panelMouseX, mouseY, 0)
        secondaryDNSBox.mouseClicked(panelMouseX, mouseY, 0)

        val buttonY = panelY + panelHeight - FORM_BUTTON_HEIGHT - 20f
        val cancelX = panelX + panelWidth - FORM_BUTTON_WIDTH * 2f - 30f
        val saveX = panelX + panelWidth - FORM_BUTTON_WIDTH - 20f

        if (MouseUtils.isInside(panelMouseX, mouseY, cancelX, buttonY, FORM_BUTTON_WIDTH, FORM_BUTTON_HEIGHT)) {
            closeForm()
            return
        }
        if (MouseUtils.isInside(panelMouseX, mouseY, saveX, buttonY, FORM_BUTTON_WIDTH, FORM_BUTTON_HEIGHT)) {
            saveProxy(Shindo.getInstance().getNetworkManager())
        }
    }

    private fun saveProxy(networkManager: NetworkManager) {
        val name = nameBox.getText().trim()
        val primary = primaryDNSBox.getText().trim()
        val secondary = secondaryDNSBox.getText().trim().takeIf { it.isNotEmpty() }

        if (name.isEmpty() || primary.isEmpty()) {
            return
        }

        val success = editingProxyId?.let { id ->
            networkManager.proxyManager.updateProxy(id, name, primary, secondary)
        } ?: networkManager.proxyManager.addProxy(
            CustomProxy(name = name, primaryDNS = primary, secondaryDNS = secondary)
        )

        if (success) {
            closeForm()
            saveProfileState()
        }
    }

    private fun syncCloudflareCard(networkManager: NetworkManager) {
        cloudflareCard.title = "Cloudflare DNS"
        cloudflareCard.subtitle = "1.1.1.1 / 1.0.0.1"
        cloudflareCard.active = networkManager.getActiveProxyType() == NetworkManager.ProxyType.CLOUDFLARE
        cloudflareCard.onCardClick = null
    }

    private fun syncProxyCard(card: CompProxyCard, proxy: CustomProxy, active: Boolean) {
        card.title = proxy.name
        card.subtitle = proxy.primaryDNS + (proxy.secondaryDNS?.let { " / $it" } ?: "")
        card.active = active
        card.onCardClick = { openEditForm(proxy) }
        card.onToggleClick = { toggleCustomProxy(Shindo.getInstance().getNetworkManager(), proxy.id) }
    }

    private fun toggleCloudflare(networkManager: NetworkManager) {
        if (networkManager.getActiveProxyType() == NetworkManager.ProxyType.CLOUDFLARE) {
            networkManager.disableAllProxies()
        } else {
            networkManager.proxyManager.setActiveProxy(null)
            networkManager.enableCloudflareProxy()
        }
        saveProfileState()
    }

    private fun toggleCustomProxy(networkManager: NetworkManager, proxyId: String) {
        if (networkManager.getActiveCustomProxyId() == proxyId) {
            networkManager.disableAllProxies()
        } else {
            networkManager.disableCloudflareProxy()
            networkManager.enableCustomProxy(proxyId)
        }
        saveProfileState()
    }

    private fun openCreateForm() {
        editingProxyId = null
        resetForm()
        showProxyForm = true
        setCanClose(false)
        formAnimation.setDirection(Direction.BACKWARDS)
    }

    private fun openEditForm(proxy: CustomProxy) {
        editingProxyId = proxy.id
        nameBox.setText(proxy.name)
        primaryDNSBox.setText(proxy.primaryDNS)
        secondaryDNSBox.setText(proxy.secondaryDNS ?: "")
        showProxyForm = true
        setCanClose(false)
        formAnimation.setDirection(Direction.BACKWARDS)
    }

    private fun closeForm() {
        showProxyForm = false
        editingProxyId = null
        formAnimation.setDirection(Direction.FORWARDS)
    }

    private fun updateAnimationState() {
        formAnimation.setDirection(if (showProxyForm) Direction.BACKWARDS else Direction.FORWARDS)

        val slide = (formAnimation.getValue() * ModMenuMotion.DETAILS_PANEL_SLIDE_DISTANCE).toFloat()
        panelOffsetX = slide
        contentOffsetX = -(ModMenuMotion.DETAILS_PANEL_SLIDE_DISTANCE - slide)

        if (!showProxyForm && formAnimation.isDone(Direction.FORWARDS)) {
            resetForm()
            setCanClose(true)
        }
    }

    private fun isFormInteractionLocked(): Boolean {
        return showProxyForm || !formAnimation.isDone(Direction.FORWARDS)
    }

    private fun resetForm() {
        nameBox.setText("")
        primaryDNSBox.setText("")
        secondaryDNSBox.setText("")
    }

    private fun saveProfileState() {
        Shindo.getInstance().getProfileManager().save()
    }

    private fun resetState() {
        currentPage = NetworkPage.GENERAL
        sectionFilter = ProxySectionFilter.ALL
        proxyScroll.resetAll()
        proxyScrollY = 0f
        showProxyForm = false
        editingProxyId = null
        resetForm()

        formAnimation = SmoothStepAnimation(ModMenuMotion.DETAILS_PANEL_ANIMATION_MS, 1.0)
        formAnimation.setValue(1.0)
        formAnimation.setDirection(Direction.FORWARDS)

        contentOffsetX = 0f
        panelOffsetX = ModMenuMotion.DETAILS_PANEL_SLIDE_DISTANCE
        proxyListViewportX = 0f
        proxyListViewportY = 0f
        proxyListViewportWidth = 0f
        proxyListViewportHeight = 0f
        setCanClose(true)
    }

    private companion object {
        private const val CONTENT_PADDING = 18f
        private const val CONTENT_BOTTOM_PADDING = 20f
        private const val CHIP_TOP_PADDING = 16f
        private const val CHIP_GAP = 8f
        private const val SECTION_SPACING = 12f

        private const val CARD_GAP = 12f
        private const val PROXY_CARD_HEIGHT = 70f
        private const val PROXY_CARD_GAP = 12f

        private const val FORM_BUTTON_WIDTH = 84f
        private const val FORM_BUTTON_HEIGHT = 22f

    }
}

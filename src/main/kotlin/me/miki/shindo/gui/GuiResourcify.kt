package me.miki.shindo.gui

import me.miki.shindo.Shindo
import me.miki.shindo.management.addons.resourcify.cache.ResourcifyIconCache
import me.miki.shindo.management.addons.resourcify.core.ResourcifyAddon
import me.miki.shindo.management.addons.resourcify.core.ResourcifyManager
import me.miki.shindo.management.addons.resourcify.model.ResourcifyCategory
import me.miki.shindo.management.addons.resourcify.model.ResourcifyDownloadResult
import me.miki.shindo.management.addons.resourcify.model.ResourcifyFilters
import me.miki.shindo.management.addons.resourcify.model.ResourcifyProject
import me.miki.shindo.management.addons.resourcify.model.ResourcifyResourceType
import me.miki.shindo.management.addons.resourcify.model.ResourcifyServiceType
import me.miki.shindo.management.addons.resourcify.model.ResourcifyUpdate
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.notification.NotificationType
import me.miki.shindo.ui.comp.inputs.CompSearchBox
import me.miki.shindo.utils.ColorUtils
import me.miki.shindo.utils.Multithreading
import me.miki.shindo.utils.mouse.MouseUtils
import me.miki.shindo.utils.mouse.Scroll
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import org.lwjgl.input.Keyboard
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.max
import kotlin.math.min

class GuiResourcify(
    private val parent: GuiScreen,
    private val startType: ResourcifyResourceType
) : GuiScreen(), IShindoScreen {

    private val searchBox = CompSearchBox()
    private val scroll = Scroll()
    private val categoryScroll = Scroll()
    private val versionScroll = Scroll()
    private val actionHitboxes = ArrayList<Hitbox>()
    private val categoryPanel = PanelBounds()
    private val versionPanel = PanelBounds()

    private val manager: ResourcifyManager = ResourcifyAddon.getInstance()?.manager
        ?: ResourcifyManager(java.io.File(Shindo.getInstance().fileManager.addonConfigDir, "resourcify.json"))
    private val iconCache = ResourcifyIconCache(java.io.File(Shindo.getInstance().fileManager.cacheDir, "resourcify/icons"))

    private var currentService = ResourcifyServiceType.MODRINTH
    private var currentType = startType
    private var currentTab = Tab.BROWSE
    private var categories: List<ResourcifyCategory> = emptyList()
    private var versions: List<String> = emptyList()
    private var selectedCategory: ResourcifyCategory? = null
    private var selectedVersion: String = ResourcifyManager.DEFAULT_MC_VERSION
    private var searchOffset = 0
    private var totalHits = 0
    private var loading = AtomicBoolean(false)
    private var filtersLoading = AtomicBoolean(false)
    private var errorMessage: String? = null
    private var searchResults: List<ResourcifyProject> = emptyList()
    private var updateResults: List<ResourcifyUpdate> = emptyList()

    override fun initGui() {
        Keyboard.enableRepeatEvents(true)
        searchBox.setFocused(false)
        searchBox.setText("")
        selectedCategory = null
        selectedVersion = ResourcifyManager.DEFAULT_MC_VERSION
        searchOffset = 0
        totalHits = 0
        scroll.resetAll()
        categoryScroll.resetAll()
        versionScroll.resetAll()
        errorMessage = null
        refreshFilters(true, true)
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawDefaultBackground()
        val instance = Shindo.getInstance()
        val nvg = instance.nanoVGManager ?: return
        val sr = ScaledResolution(mc)

        nvg.setupAndDraw(Runnable {
            drawNanoVG(nvg, sr, mouseX, mouseY, partialTicks)
        })
    }

    private fun drawNanoVG(nvg: NanoVGManager, sr: ScaledResolution, mouseX: Int, mouseY: Int, partialTicks: Float) {
        val palette = Shindo.getInstance().colorManager.palette

        actionHitboxes.clear()

        val maxWidth = sr.scaledWidth.toFloat() - 40f
        val maxHeight = sr.scaledHeight.toFloat() - 40f
        val width = min(600f, maxWidth)
        val height = min(384f, maxHeight)
        val x = (sr.scaledWidth - width) / 2f
        val y = (sr.scaledHeight - height) / 2f

        nvg.drawRoundedRect(x, y, width, height, 16f, palette.getBackgroundColor(ColorType.DARK))
        nvg.drawRoundedRect(x + 1f, y + 1f, width - 2f, height - 2f, 15f, palette.getBackgroundColor(ColorType.MID))

        // Header
        nvg.drawText("Resourcify", x + 16f, y + 18f, palette.getFontColor(ColorType.DARK), 14f, Fonts.SEMIBOLD)
        val closeSize = 18f
        val closeX = x + width - closeSize - 14f
        val closeY = y + 12f
        nvg.drawRoundedRect(closeX, closeY, closeSize, closeSize, 6f, palette.getBackgroundColor(ColorType.DARK))
        nvg.drawCenteredText(LegacyIcon.X, closeX + closeSize / 2f, closeY + closeSize / 2f - 6f, palette.getFontColor(ColorType.DARK), 12f, Fonts.LEGACYICON)
        actionHitboxes.add(Hitbox(closeX, closeY, closeSize, closeSize) { mc.displayGuiScreen(parent) })

        val headerHeight = 40f
        val contentTop = y + headerHeight + 6f
        val innerPadding = 12f
        val sideGap = 12f
        val sideWidth = min(190f, width * 0.34f)
        val sideX = x + innerPadding
        val sideY = contentTop
        val sideHeight = height - (sideY - y) - innerPadding
        val rightX = sideX + sideWidth + sideGap
        val rightY = sideY
        val rightWidth = x + width - innerPadding - rightX
        val rightHeight = sideHeight

        drawFilterSidebar(nvg, sideX, sideY, sideWidth, sideHeight, mouseX, mouseY)

        // Tabs
        val tabY = rightY
        val tabX = rightX
        drawTab(nvg, tabX, tabY, "Browse", currentTab == Tab.BROWSE) {
            if (currentTab != Tab.BROWSE) {
                currentTab = Tab.BROWSE
                scroll.resetAll()
                triggerSearch(true)
            }
        }
        drawTab(nvg, tabX + 86f, tabY, "Updates", currentTab == Tab.UPDATES) {
            if (currentTab != Tab.UPDATES) {
                currentTab = Tab.UPDATES
                scroll.resetAll()
                refreshUpdates()
            }
        }

        // Search + filters
        var searchWidth = min(170f, rightWidth - 130f)
        var searchY = tabY
        var searchX = rightX + rightWidth - searchWidth
        if (searchWidth < 120f) {
            searchWidth = rightWidth
            searchX = rightX
            searchY = tabY + 26f
        }
        searchBox.setPosition(searchX, searchY, searchWidth, 22f)
        searchBox.draw(mouseX, mouseY, partialTicks)

        val typeLabel = currentType.displayName
        val typeChipWidth = max(80f, nvg.getTextWidth(typeLabel, 8f, Fonts.MEDIUM) + 18f)
        val filterY = if (searchY == tabY) tabY + 28f else searchY + 30f
        val chipHeight = 22f
        val chipGap = 8f
        var chipX = rightX
        var chipY = filterY
        var chipBottom = chipY + chipHeight

        fun placeChip(width: Float, draw: (Float, Float) -> Unit) {
            if (chipX + width > rightX + rightWidth) {
                chipX = rightX
                chipY += chipHeight + 6f
            }
            draw(chipX, chipY)
            chipX += width + chipGap
            chipBottom = max(chipBottom, chipY + chipHeight)
        }

        placeChip(typeChipWidth) { drawX, drawY ->
            drawChip(nvg, drawX, drawY, typeChipWidth, typeLabel, true)
            actionHitboxes.add(Hitbox(drawX, drawY, typeChipWidth, chipHeight) {
                currentType = if (currentType == ResourcifyResourceType.RESOURCE_PACK) {
                    ResourcifyResourceType.SHADER_PACK
                } else {
                    ResourcifyResourceType.RESOURCE_PACK
                }
                scroll.resetAll()
                refreshFilters(true, currentTab == Tab.BROWSE)
            })
        }

        for (service in ResourcifyServiceType.values()) {
            val enabled = manager.getService(service)?.isEnabled(manager.config) == true
            val label = service.displayName
            val chipWidth = max(80f, nvg.getTextWidth(label, 8f, Fonts.MEDIUM) + 18f)
            placeChip(chipWidth) { drawX, drawY ->
                drawChip(nvg, drawX, drawY, chipWidth, label, currentService == service, !enabled)
                if (enabled) {
                    actionHitboxes.add(Hitbox(drawX, drawY, chipWidth, chipHeight) {
                        if (currentService != service) {
                            currentService = service
                            scroll.resetAll()
                            refreshFilters(true, currentTab == Tab.BROWSE)
                        }
                    })
                }
            }
        }

        var listY = chipBottom + 12f
        if (filtersLoading.get()) {
            nvg.drawText("Loading filters...", rightX, chipBottom + 8f, palette.getFontColor(ColorType.NORMAL), 8f, Fonts.REGULAR)
            listY += 12f
        }

        val listX = rightX
        val listW = rightWidth
        val listH = max(0f, rightY + rightHeight - listY)

        val blockListScroll = categoryPanel.contains(mouseX, mouseY) || versionPanel.contains(mouseX, mouseY)
        if (!blockListScroll && MouseUtils.isInside(mouseX, mouseY, listX, listY, listW, listH)) {
            scroll.onScroll()
        }
        scroll.onAnimation()

        nvg.save()
        nvg.scissor(listX, listY, listW, listH)
        nvg.translate(0f, scroll.getValue())

        if (currentTab == Tab.BROWSE) {
            drawBrowseList(nvg, listX, listY, listW, mouseX, mouseY)
        } else {
            drawUpdatesList(nvg, listX, listY, listW, mouseX, mouseY)
        }

        nvg.restore()

        val maxScroll = computeScrollMax(listH)
        scroll.maxScroll = maxScroll

        if (loading.get()) {
            nvg.drawCenteredText("Loading...", rightX + rightWidth / 2f, listY + listH / 2f, palette.getFontColor(ColorType.DARK), 11f, Fonts.MEDIUM)
        } else if (errorMessage != null) {
            nvg.drawCenteredText(errorMessage ?: "", rightX + rightWidth / 2f, listY + listH / 2f, palette.getFontColor(ColorType.DARK), 10f, Fonts.REGULAR)
        }
    }

    private fun drawBrowseList(nvg: NanoVGManager, x: Float, y: Float, width: Float, mouseX: Int, mouseY: Int) {
        val palette = Shindo.getInstance().colorManager.palette
        val accent = Shindo.getInstance().colorManager.currentColor
        val cardHeight = 64f
        val gap = 10f
        val list = searchResults
        var offsetY = y

        for (project in list) {
            val hovered = MouseUtils.isInside(mouseX, mouseY, x, offsetY + scroll.getValue(), width, cardHeight)
            val base = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), if (hovered) 230 else 200)
            nvg.drawRoundedRect(x, offsetY, width, cardHeight, 10f, base)
            val iconSize = 40f
            val iconX = x + 12f
            val iconY = offsetY + (cardHeight - iconSize) / 2f
            val iconFile = iconCache.getIconFile(project.iconUrl)
            if (iconFile != null && iconFile.exists()) {
                nvg.drawRoundedImage(iconFile, iconX, iconY, iconSize, iconSize, 8f)
            } else {
                nvg.drawRoundedRect(iconX, iconY, iconSize, iconSize, 8f, palette.getBackgroundColor(ColorType.MID))
                nvg.drawCenteredText(LegacyIcon.IMAGE, iconX + iconSize / 2f, iconY + iconSize / 2f - 6f, palette.getFontColor(ColorType.DARK), 12f, Fonts.LEGACYICON)
            }

            val textX = iconX + iconSize + 12f
            nvg.drawText(project.title, textX, offsetY + 20f, palette.getFontColor(ColorType.DARK), 11f, Fonts.MEDIUM)
            val desc = nvg.getLimitText(project.description, 8f, Fonts.REGULAR, width - textX - 110f)
            nvg.drawText(desc, textX, offsetY + 38f, palette.getFontColor(ColorType.NORMAL), 8f, Fonts.REGULAR)

            val actionWidth = 84f
            val actionHeight = 24f
            val actionX = x + width - actionWidth - 12f
            val actionY = offsetY + (cardHeight - actionHeight) / 2f
            nvg.drawRoundedRect(actionX, actionY, actionWidth, actionHeight, 8f, palette.getBackgroundColor(ColorType.MID))
            nvg.drawCenteredText("Download", actionX + actionWidth / 2f, actionY + 8f, palette.getFontColor(ColorType.DARK), 8f, Fonts.MEDIUM)
            if (MouseUtils.isInside(mouseX, mouseY, actionX, actionY + scroll.getValue(), actionWidth, actionHeight)) {
                nvg.drawGradientOutlineRoundedRect(actionX, actionY, actionWidth, actionHeight, 8f, 1f, accent.color1, accent.color2)
            }
            actionHitboxes.add(Hitbox(actionX, actionY + scroll.getValue(), actionWidth, actionHeight) {
                startDownload(project)
            })

            offsetY += cardHeight + gap
        }

        drawPagingControls(nvg, x, offsetY, width)
    }

    private fun drawUpdatesList(nvg: NanoVGManager, x: Float, y: Float, width: Float, mouseX: Int, mouseY: Int) {
        val palette = Shindo.getInstance().colorManager.palette
        val accent = Shindo.getInstance().colorManager.currentColor
        val cardHeight = 64f
        val gap = 10f
        var offsetY = y

        for (update in updateResults) {
            val hovered = MouseUtils.isInside(mouseX, mouseY, x, offsetY + scroll.getValue(), width, cardHeight)
            val base = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), if (hovered) 230 else 200)
            nvg.drawRoundedRect(x, offsetY, width, cardHeight, 10f, base)
            nvg.drawText(update.entry.fileName, x + 12f, offsetY + 20f, palette.getFontColor(ColorType.DARK), 11f, Fonts.MEDIUM)
            val status = if (update.version == null) "Up to date" else "Update available"
            nvg.drawText(status, x + 12f, offsetY + 38f, palette.getFontColor(ColorType.NORMAL), 8f, Fonts.REGULAR)

            if (update.version != null) {
                val actionWidth = 84f
                val actionHeight = 24f
                val actionX = x + width - actionWidth - 12f
                val actionY = offsetY + (cardHeight - actionHeight) / 2f
                nvg.drawRoundedRect(actionX, actionY, actionWidth, actionHeight, 8f, palette.getBackgroundColor(ColorType.MID))
                nvg.drawCenteredText("Update", actionX + actionWidth / 2f, actionY + 8f, palette.getFontColor(ColorType.DARK), 8f, Fonts.MEDIUM)
                if (MouseUtils.isInside(mouseX, mouseY, actionX, actionY + scroll.getValue(), actionWidth, actionHeight)) {
                    nvg.drawGradientOutlineRoundedRect(actionX, actionY, actionWidth, actionHeight, 8f, 1f, accent.color1, accent.color2)
                }
                actionHitboxes.add(Hitbox(actionX, actionY + scroll.getValue(), actionWidth, actionHeight) {
                    startUpdate(update)
                })
            }
            offsetY += cardHeight + gap
        }
    }

    private fun drawPagingControls(nvg: NanoVGManager, x: Float, y: Float, width: Float) {
        val palette = Shindo.getInstance().colorManager.palette
        if (totalHits <= PAGE_SIZE) return
        val hasPrev = searchOffset > 0
        val hasNext = searchOffset + PAGE_SIZE < totalHits
        val buttonWidth = 70f
        val buttonHeight = 22f
        val leftX = x + width - buttonWidth * 2 - 12f
        val rightX = x + width - buttonWidth - 6f
        val buttonY = y + 4f

        drawPagingButton(nvg, leftX, buttonY, buttonWidth, buttonHeight, "Prev", hasPrev) {
            if (hasPrev) {
                searchOffset = max(0, searchOffset - PAGE_SIZE)
                scroll.resetAll()
                triggerSearch(false)
            }
        }
        drawPagingButton(nvg, rightX, buttonY, buttonWidth, buttonHeight, "Next", hasNext) {
            if (hasNext) {
                searchOffset += PAGE_SIZE
                scroll.resetAll()
                triggerSearch(false)
            }
        }
    }

    private fun drawPagingButton(
        nvg: NanoVGManager,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        label: String,
        enabled: Boolean,
        onClick: () -> Unit
    ) {
        val palette = Shindo.getInstance().colorManager.palette
        val base = if (enabled) palette.getBackgroundColor(ColorType.MID) else ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 120)
        nvg.drawRoundedRect(x, y, width, height, 7f, base)
        nvg.drawCenteredText(label, x + width / 2f, y + 7f, palette.getFontColor(ColorType.DARK), 8f, Fonts.MEDIUM)
        if (enabled) {
            actionHitboxes.add(Hitbox(x, y + scroll.getValue(), width, height) { onClick() })
        }
    }

    private fun drawTab(nvg: NanoVGManager, x: Float, y: Float, label: String, active: Boolean, onClick: () -> Unit) {
        val palette = Shindo.getInstance().colorManager.palette
        val textColor = palette.getFontColor(ColorType.DARK)
        val width = max(60f, nvg.getTextWidth(label, 9f, Fonts.MEDIUM) + 18f)
        val height = 22f
        val bg = if (active) palette.getBackgroundColor(ColorType.DARK) else palette.getBackgroundColor(ColorType.MID)
        nvg.drawRoundedRect(x, y, width, height, 8f, bg)
        nvg.drawCenteredText(label, x + width / 2f, y + 7f, textColor, 9f, Fonts.MEDIUM)
        actionHitboxes.add(Hitbox(x, y, width, height) { onClick() })
    }

    private fun drawChip(nvg: NanoVGManager, x: Float, y: Float, width: Float, label: String, active: Boolean, disabled: Boolean = false) {
        val palette = Shindo.getInstance().colorManager.palette
        val bg = if (active) palette.getBackgroundColor(ColorType.DARK) else palette.getBackgroundColor(ColorType.MID)
        val alpha = if (disabled) 110 else 220
        nvg.drawRoundedRect(x, y, width, 22f, 8f, ColorUtils.applyAlpha(bg, alpha))
        nvg.drawCenteredText(label, x + width / 2f, y + 7f, palette.getFontColor(ColorType.DARK, alpha), 8f, Fonts.MEDIUM)
    }

    private fun drawFilterSidebar(
        nvg: NanoVGManager,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        mouseX: Int,
        mouseY: Int
    ) {
        if (width <= 0f || height <= 0f) {
            categoryPanel.reset()
            versionPanel.reset()
            return
        }
        val palette = Shindo.getInstance().colorManager.palette
        nvg.drawRoundedRect(x, y, width, height, 10f, palette.getBackgroundColor(ColorType.DARK))

        val inset = 10f
        val labelHeight = 12f
        val listGap = 6f
        val sectionGap = 12f
        val contentHeight = max(0f, height - inset * 2 - labelHeight * 2 - listGap * 2 - sectionGap)
        val listHeight = contentHeight / 2f
        val contentX = x + inset
        val contentWidth = width - inset * 2
        var cursorY = y + inset

        nvg.drawText("Category", contentX, cursorY + 8f, palette.getFontColor(ColorType.NORMAL), 8f, Fonts.SEMIBOLD)
        cursorY += labelHeight + listGap
        drawCategoryMenu(nvg, contentX, cursorY, contentWidth, listHeight, mouseX, mouseY)
        cursorY += listHeight + sectionGap

        nvg.drawText("Version", contentX, cursorY + 8f, palette.getFontColor(ColorType.NORMAL), 8f, Fonts.SEMIBOLD)
        cursorY += labelHeight + listGap
        drawVersionMenu(nvg, contentX, cursorY, contentWidth, contentHeight - listHeight, mouseX, mouseY)
    }

    private fun drawCategoryMenu(nvg: NanoVGManager, x: Float, y: Float, width: Float, height: Float, mouseX: Int, mouseY: Int) {
        if (height <= 0f) {
            categoryPanel.reset()
            return
        }
        val palette = Shindo.getInstance().colorManager.palette
        val items = ArrayList<ResourcifyCategory>()
        items.add(ResourcifyCategory("all", "All"))
        items.addAll(categories)

        val itemHeight = 22f
        val contentHeight = items.size * itemHeight
        categoryPanel.set(x, y, width, height)

        if (MouseUtils.isInside(mouseX, mouseY, x, y, width, height)) {
            categoryScroll.onScroll()
        }
        categoryScroll.onAnimation()
        categoryScroll.maxScroll = max(0f, contentHeight - height + 6f)

        nvg.drawRoundedRect(x, y, width, height, 8f, palette.getBackgroundColor(ColorType.DARK))
        nvg.save()
        nvg.scissor(x + 2f, y + 2f, width - 4f, height - 4f)
        nvg.translate(0f, categoryScroll.getValue())

        var offsetY = y + 3f
        for (category in items) {
            val isAll = category.id == "all"
            val selected = if (isAll) selectedCategory == null else selectedCategory?.id == category.id
            val hovered = MouseUtils.isInside(mouseX, mouseY, x, offsetY + categoryScroll.getValue(), width, itemHeight)
            val bg = if (hovered) palette.getBackgroundColor(ColorType.MID) else palette.getBackgroundColor(ColorType.DARK)
            nvg.drawRoundedRect(x + 3f, offsetY, width - 6f, itemHeight - 2f, 6f, ColorUtils.applyAlpha(bg, 230))
            nvg.drawText(category.name, x + 10f, offsetY + 8f, palette.getFontColor(ColorType.DARK), 8f, Fonts.MEDIUM)
            if (selected) {
                nvg.drawText(LegacyIcon.CHECK, x + width - 18f, offsetY + 7f, palette.getFontColor(ColorType.DARK), 9f, Fonts.LEGACYICON)
            }
            actionHitboxes.add(Hitbox(x + 3f, offsetY + categoryScroll.getValue(), width - 6f, itemHeight - 2f) {
                selectedCategory = if (isAll) null else category
                categoryScroll.resetAll()
                if (currentTab == Tab.BROWSE) {
                    triggerSearch(true)
                } else {
                    refreshUpdates()
                }
            })
            offsetY += itemHeight
        }
        nvg.restore()
    }

    private fun drawVersionMenu(nvg: NanoVGManager, x: Float, y: Float, width: Float, height: Float, mouseX: Int, mouseY: Int) {
        if (height <= 0f) {
            versionPanel.reset()
            return
        }
        val palette = Shindo.getInstance().colorManager.palette
        val items = if (versions.isEmpty()) listOf(ResourcifyManager.DEFAULT_MC_VERSION) else versions

        val itemHeight = 22f
        val contentHeight = items.size * itemHeight
        versionPanel.set(x, y, width, height)

        if (MouseUtils.isInside(mouseX, mouseY, x, y, width, height)) {
            versionScroll.onScroll()
        }
        versionScroll.onAnimation()
        versionScroll.maxScroll = max(0f, contentHeight - height + 6f)

        nvg.drawRoundedRect(x, y, width, height, 8f, palette.getBackgroundColor(ColorType.DARK))
        nvg.save()
        nvg.scissor(x + 2f, y + 2f, width - 4f, height - 4f)
        nvg.translate(0f, versionScroll.getValue())

        var offsetY = y + 3f
        for (version in items) {
            val selected = version == selectedVersion
            val hovered = MouseUtils.isInside(mouseX, mouseY, x, offsetY + versionScroll.getValue(), width, itemHeight)
            val bg = if (hovered) palette.getBackgroundColor(ColorType.MID) else palette.getBackgroundColor(ColorType.DARK)
            nvg.drawRoundedRect(x + 3f, offsetY, width - 6f, itemHeight - 2f, 6f, ColorUtils.applyAlpha(bg, 230))
            nvg.drawText(version, x + 10f, offsetY + 8f, palette.getFontColor(ColorType.DARK), 8f, Fonts.MEDIUM)
            if (selected) {
                nvg.drawText(LegacyIcon.CHECK, x + width - 18f, offsetY + 7f, palette.getFontColor(ColorType.DARK), 9f, Fonts.LEGACYICON)
            }
            actionHitboxes.add(Hitbox(x + 3f, offsetY + versionScroll.getValue(), width - 6f, itemHeight - 2f) {
                selectedVersion = version
                versionScroll.resetAll()
                if (currentTab == Tab.BROWSE) {
                    triggerSearch(true)
                } else {
                    refreshUpdates()
                }
            })
            offsetY += itemHeight
        }
        nvg.restore()
    }

    private fun computeScrollMax(viewportHeight: Float): Float {
        val itemCount = if (currentTab == Tab.BROWSE) searchResults.size else updateResults.size
        val rows = if (itemCount == 0) 0 else itemCount
        val contentHeight = rows * 64f + max(0, rows - 1) * 10f + if (currentTab == Tab.BROWSE && totalHits > PAGE_SIZE) 30f else 0f
        return max(0f, contentHeight - viewportHeight)
    }

    private fun refreshFilters(resetSelection: Boolean, refreshResults: Boolean) {
        if (filtersLoading.get()) return
        filtersLoading.set(true)
        Multithreading.runAsync(Runnable {
            val nextCategories = manager.getCategories(currentService, currentType)
            val nextVersions = manager.getVersions(currentService)
            Minecraft.getMinecraft().addScheduledTask {
                filtersLoading.set(false)
                categories = nextCategories
                versions = nextVersions
                categoryScroll.resetAll()
                versionScroll.resetAll()
                if (resetSelection || (selectedCategory != null && nextCategories.none { it.id == selectedCategory?.id })) {
                    selectedCategory = null
                }
                if (resetSelection || !nextVersions.contains(selectedVersion)) {
                    selectedVersion = ResourcifyManager.DEFAULT_MC_VERSION
                }
                if (refreshResults) {
                    if (currentTab == Tab.BROWSE) {
                        triggerSearch(true)
                    } else if (ResourcifyAddon.getInstance()?.shouldAutoCheckUpdates() == true) {
                        refreshUpdates()
                    }
                }
            }
        })
    }

    private fun triggerSearch(resetOffset: Boolean) {
        if (loading.get()) return
        if (resetOffset) {
            searchOffset = 0
        }
        errorMessage = null
        loading.set(true)
        val query = searchBox.getText()
        val filters = ResourcifyFilters(selectedCategory?.id, selectedVersion)
        Multithreading.runAsync(Runnable {
            val result = manager.search(currentService, query, currentType, searchOffset, filters)
            Minecraft.getMinecraft().addScheduledTask {
                loading.set(false)
                if (result == null) {
                    searchResults = emptyList()
                    totalHits = 0
                    errorMessage = "Search failed"
                } else {
                    searchResults = result.results
                    totalHits = result.total
                }
            }
        })
    }

    private fun refreshUpdates() {
        if (loading.get()) return
        errorMessage = null
        loading.set(true)
        Multithreading.runAsync(Runnable {
            val result = manager.checkUpdates(currentType, selectedVersion, currentService)
            Minecraft.getMinecraft().addScheduledTask {
                loading.set(false)
                updateResults = result
            }
        })
    }

    private fun startDownload(project: ResourcifyProject) {
        if (loading.get()) return
        loading.set(true)
        Multithreading.runAsync(Runnable {
            val version = manager.getLatestVersion(project.service, project.projectId, currentType, selectedVersion)
            val result = if (version == null) ResourcifyDownloadResult(null, "No compatible version") else manager.download(version, currentType)
            Minecraft.getMinecraft().addScheduledTask {
                loading.set(false)
                if (result.file != null) {
                    Shindo.getInstance().notificationManager.post(
                        "Resourcify",
                        "Downloaded ${result.file.name}",
                        NotificationType.SUCCESS
                    )
                    refreshUpdates()
                } else {
                    Shindo.getInstance().notificationManager.post(
                        "Resourcify",
                        result.error ?: "Download failed",
                        NotificationType.ERROR
                    )
                }
            }
        })
    }

    private fun startUpdate(update: ResourcifyUpdate) {
        val version = update.version ?: return
        if (loading.get()) return
        loading.set(true)
        Multithreading.runAsync(Runnable {
            val result = manager.download(version, currentType)
            Minecraft.getMinecraft().addScheduledTask {
                loading.set(false)
                if (result.file != null) {
                    Shindo.getInstance().notificationManager.post(
                        "Resourcify",
                        "Updated ${result.file.name}",
                        NotificationType.SUCCESS
                    )
                    refreshUpdates()
                } else {
                    Shindo.getInstance().notificationManager.post(
                        "Resourcify",
                        result.error ?: "Update failed",
                        NotificationType.ERROR
                    )
                }
            }
        })
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (mouseButton == 0) {
            for (hitbox in actionHitboxes) {
                if (hitbox.contains(mouseX, mouseY)) {
                    hitbox.onClick()
                    return
                }
            }
        }
        searchBox.mouseClicked(mouseX, mouseY, mouseButton)
        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(parent)
            return
        }
        if (keyCode == Keyboard.KEY_RETURN) {
            triggerSearch(true)
            return
        }
        searchBox.keyTyped(typedChar, keyCode)
        super.keyTyped(typedChar, keyCode)
    }

    override fun handleMouseInput() {
        try {
            super.handleMouseInput()
        } catch (_: IOException) {
        }
    }

    override fun onGuiClosed() {
        Keyboard.enableRepeatEvents(false)
    }

    override fun doesGuiPauseGame(): Boolean {
        return false
    }

    private class Hitbox(
        private val x: Float,
        private val y: Float,
        private val width: Float,
        private val height: Float,
        val onClick: () -> Unit
    ) {
        fun contains(mouseX: Int, mouseY: Int): Boolean {
            return MouseUtils.isInside(mouseX, mouseY, x, y, width, height)
        }
    }

    private class PanelBounds {
        private var x = 0f
        private var y = 0f
        private var width = 0f
        private var height = 0f

        fun set(x: Float, y: Float, width: Float, height: Float) {
            this.x = x
            this.y = y
            this.width = width
            this.height = height
        }

        fun reset() {
            x = 0f
            y = 0f
            width = 0f
            height = 0f
        }

        fun contains(mouseX: Int, mouseY: Int): Boolean {
            return MouseUtils.isInside(mouseX, mouseY, x, y, width, height)
        }
    }

    private enum class Tab {
        BROWSE,
        UPDATES
    }

    companion object {
        private const val PAGE_SIZE = 20
    }
}

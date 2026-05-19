package me.miki.shindo.gui.modmenu.v2.category.impl

import me.miki.shindo.Shindo
import me.miki.shindo.api.roles.Role
import me.miki.shindo.gui.modmenu.v2.GuiModMenu
import me.miki.shindo.gui.modmenu.v2.category.Category
import me.miki.shindo.gui.modmenu.v2.render.ModMenuClipCoordinator
import me.miki.shindo.management.color.AccentColor
import me.miki.shindo.management.color.palette.ColorPalette
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.cosmetic.bandana.BandanaCategory
import me.miki.shindo.management.cosmetic.bandana.impl.Bandana
import me.miki.shindo.management.cosmetic.cape.CapeCategory
import me.miki.shindo.management.cosmetic.cape.impl.Cape
import me.miki.shindo.management.cosmetic.cape.impl.CustomCape
import me.miki.shindo.management.cosmetic.cape.impl.NormalCape
import me.miki.shindo.management.cosmetic.wing.WingCategory
import me.miki.shindo.management.cosmetic.wing.impl.Wing
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.Lucide
import me.miki.shindo.management.notification.NotificationType
import me.miki.shindo.ui.components.v2.chips.CategoryChipRenderer
import me.miki.shindo.ui.components.v2.chips.FilterChip
import me.miki.shindo.utils.ColorUtils
import me.miki.shindo.utils.SearchUtils
import me.miki.shindo.utils.mouse.MouseUtils
import net.minecraft.util.ResourceLocation
import java.awt.Color
import java.io.File
import java.util.*
import kotlin.math.max
import kotlin.math.min

private typealias PreviewRenderer = (NanoVGManager, Float, Float, Float, Float) -> Unit

class CosmeticsCategory(
    parent: GuiModMenu,
) : Category(parent, TranslateText.COSMETICS, Lucide.SHIRT, true, true) {
    private val sectionChips = ArrayList<FilterChip>()
    private val categoryChips = ArrayList<FilterChip>()
    private val capeCardBounds = LinkedHashMap<Cape, CardBounds>()
    private val wingCardBounds = LinkedHashMap<Wing, CardBounds>()
    private val bandanaCardBounds = LinkedHashMap<Bandana, CardBounds>()

    private var activeSection = CosmeticSection.CAPES
    private var activeCapeCategory = CapeCategory.ALL
    private var activeWingCategory = WingCategory.ALL
    private var activeBandanaCategory = BandanaCategory.ALL

    override fun initGui() {
        activeSection = CosmeticSection.CAPES
        activeCapeCategory = CapeCategory.ALL
        activeWingCategory = WingCategory.ALL
        activeBandanaCategory = BandanaCategory.ALL
        scroll.resetAll()
    }

    override fun initCategory() {
        scroll.resetAll()
    }

    fun shouldShowCustomCapeFolder(): Boolean = activeSection == CosmeticSection.CAPES && activeCapeCategory == CapeCategory.CUSTOM

    override fun drawScreen(
        mouseX: Int,
        mouseY: Int,
        partialTicks: Float,
    ) {
        val instance = Shindo.getInstance()
        val nvg = instance.nanoVGManager ?: return
        val palette = instance.getColorManager().getPalette()
        val accent = instance.getColorManager().getCurrentColor()

        val viewportX = getX().toFloat()
        val viewportY = getY().toFloat()
        val viewportWidth = getWidth().toFloat()
        val viewportHeight = getHeight().toFloat()

        if (!activeSection.visible) {
            activeSection = CosmeticSection.CAPES
        }

        sectionChips.clear()
        categoryChips.clear()
        capeCardBounds.clear()
        wingCardBounds.clear()
        bandanaCardBounds.clear()

        if (MouseUtils.isInside(mouseX, mouseY, viewportX, viewportY, viewportWidth, viewportHeight)) {
            scroll.onScroll()
        }
        scroll.onAnimation()
        val scrollOffset = scroll.getValue()

        val searchQuery = getSearchBox().getText().trim()
        val contentX = viewportX + CONTENT_PADDING
        val contentWidth = viewportWidth - CONTENT_PADDING * 2f
        val startY = viewportY + CONTENT_PADDING
        var y = startY

        ModMenuClipCoordinator.withClipTranslate(
            nvg = nvg,
            x = viewportX,
            y = viewportY,
            width = viewportWidth,
            height = viewportHeight,
            translateX = 0f,
            translateY = scrollOffset,
        ) {
            y = drawSectionChips(nvg, palette, accent, contentX, contentWidth, y, scrollOffset, mouseX, mouseY)
            y += SECTION_BLOCK_GAP
            y = drawCategoryChips(nvg, palette, accent, contentX, contentWidth, y, scrollOffset, mouseX, mouseY)
            y += CATEGORY_BLOCK_GAP
            y =
                when (activeSection) {
                    CosmeticSection.CAPES -> {
                        drawCapeGrid(
                            nvg,
                            palette,
                            accent,
                            contentX,
                            contentWidth,
                            y,
                            scrollOffset,
                            searchQuery,
                            mouseX,
                            mouseY,
                        )
                    }

                    CosmeticSection.WINGS -> {
                        drawWingGrid(
                            nvg,
                            palette,
                            accent,
                            contentX,
                            contentWidth,
                            y,
                            scrollOffset,
                            searchQuery,
                            mouseX,
                            mouseY,
                        )
                    }

                    CosmeticSection.BANDANAS -> {
                        drawBandanaGrid(
                            nvg,
                            palette,
                            accent,
                            contentX,
                            contentWidth,
                            y,
                            scrollOffset,
                            searchQuery,
                            mouseX,
                            mouseY,
                        )
                    }
                }
        }

        val logicalHeight = max(0f, (y - startY) + CONTENT_PADDING)
        scroll.maxScroll = max(0f, logicalHeight - viewportHeight)
    }

    override fun mouseClicked(
        mouseX: Int,
        mouseY: Int,
        mouseButton: Int,
    ) {
        if (mouseButton != 0) {
            return
        }
        if (handleChipClick(sectionChips, mouseX, mouseY) || handleChipClick(categoryChips, mouseX, mouseY)) {
            return
        }
        when (activeSection) {
            CosmeticSection.CAPES -> handleCapeClick(mouseX, mouseY)
            CosmeticSection.WINGS -> handleWingClick(mouseX, mouseY)
            CosmeticSection.BANDANAS -> handleBandanaClick(mouseX, mouseY)
        }
    }

    override fun mouseReleased(
        mouseX: Int,
        mouseY: Int,
        mouseButton: Int,
    ) {
    }

    override fun keyTyped(
        typedChar: Char,
        keyCode: Int,
    ) {
        scroll.onKey(keyCode)
    }

    private fun drawSectionChips(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accent: AccentColor,
        x: Float,
        width: Float,
        y: Float,
        scrollOffset: Float,
        mouseX: Int,
        mouseY: Int,
    ): Float {
        var currentX = x
        var currentY = y

        for (section in CosmeticSection.values().filter { it.visible }) {
            val chipWidth = CategoryChipRenderer.computeWidth(nvg, section.label, section.icon)
            if (currentX + chipWidth > x + width) {
                currentX = x
                currentY += CategoryChipRenderer.CHIP_HEIGHT + SECTION_CHIP_GAP
            }

            val hovered =
                MouseUtils.isInside(
                    mouseX,
                    mouseY,
                    currentX,
                    currentY + scrollOffset,
                    chipWidth,
                    CategoryChipRenderer.CHIP_HEIGHT,
                )
            CategoryChipRenderer.drawChip(
                nvg,
                palette,
                accent,
                currentX,
                currentY,
                chipWidth,
                section.label,
                section.icon,
                section == activeSection,
                hovered,
            )

            val chip =
                FilterChip(
                    Runnable {
                        if (activeSection != section) {
                            activeSection = section
                            scroll.resetAll()
                        }
                    },
                )
            chip.setBounds(currentX, currentY + scrollOffset, chipWidth, CategoryChipRenderer.CHIP_HEIGHT)
            sectionChips.add(chip)
            currentX += chipWidth + SECTION_CHIP_GAP
        }

        return currentY + CategoryChipRenderer.CHIP_HEIGHT
    }

    private fun drawCategoryChips(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accent: AccentColor,
        x: Float,
        width: Float,
        y: Float,
        scrollOffset: Float,
        mouseX: Int,
        mouseY: Int,
    ): Float {
        var currentX = x
        var currentY = y

        for (option in getActiveCategoryOptions()) {
            val chipWidth = CategoryChipRenderer.computeWidth(nvg, option.label, null)
            if (currentX + chipWidth > x + width) {
                currentX = x
                currentY += CategoryChipRenderer.CHIP_HEIGHT + CATEGORY_CHIP_GAP
            }

            val hovered =
                MouseUtils.isInside(
                    mouseX,
                    mouseY,
                    currentX,
                    currentY + scrollOffset,
                    chipWidth,
                    CategoryChipRenderer.CHIP_HEIGHT,
                )
            CategoryChipRenderer.drawChip(
                nvg,
                palette,
                accent,
                currentX,
                currentY,
                chipWidth,
                option.label,
                null,
                option.active,
                hovered,
            )

            val chip =
                FilterChip(
                    Runnable {
                        option.onClick.run()
                        scroll.resetAll()
                    },
                )
            chip.setBounds(currentX, currentY + scrollOffset, chipWidth, CategoryChipRenderer.CHIP_HEIGHT)
            categoryChips.add(chip)
            currentX += chipWidth + CATEGORY_CHIP_GAP
        }

        return currentY + CategoryChipRenderer.CHIP_HEIGHT
    }

    private fun drawCapeGrid(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accent: AccentColor,
        x: Float,
        width: Float,
        startY: Float,
        scrollOffset: Float,
        searchQuery: String,
        mouseX: Int,
        mouseY: Int,
    ): Float {
        val manager = Shindo.getInstance().getCapeManager()
        val filtered = manager.getCapes().filter { isCapeVisible(it, searchQuery) }
        val current = manager.getCurrentCape() ?: manager.getCapes().firstOrNull()

        return drawCardGrid(
            nvg,
            palette,
            accent,
            x,
            width,
            startY,
            scrollOffset,
            mouseX,
            mouseY,
            filtered,
            current,
            { item -> item.getName() },
            { item -> getRequirementText(item.getRequiredRole(), manager::getTranslateText) },
            { item -> manager.canUseCape(getClientUuid(), item) },
            { item -> createCapePreview(item) },
            { item, bounds -> capeCardBounds[item] = bounds },
        )
    }

    private fun drawWingGrid(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accent: AccentColor,
        x: Float,
        width: Float,
        startY: Float,
        scrollOffset: Float,
        searchQuery: String,
        mouseX: Int,
        mouseY: Int,
    ): Float {
        val manager = Shindo.getInstance().getWingManager()
        val filtered = manager.getWings().filter { isWingVisible(it, searchQuery) }
        val current = manager.getCurrentWing() ?: manager.getWings().firstOrNull()

        return drawCardGrid(
            nvg,
            palette,
            accent,
            x,
            width,
            startY,
            scrollOffset,
            mouseX,
            mouseY,
            filtered,
            current,
            { item -> item.getName() },
            { item -> getRequirementText(item.getRequiredRole(), manager::getTranslateText) },
            { item -> manager.canUseWing(getClientUuid(), item) },
            { item -> createWingPreview(item) },
            { item, bounds -> wingCardBounds[item] = bounds },
        )
    }

    private fun drawBandanaGrid(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accent: AccentColor,
        x: Float,
        width: Float,
        startY: Float,
        scrollOffset: Float,
        searchQuery: String,
        mouseX: Int,
        mouseY: Int,
    ): Float {
        val manager = Shindo.getInstance().getBandanaManager()
        val filtered = manager.getBandanas().filter { isBandanaVisible(it, searchQuery) }
        val current = manager.getCurrentBandana() ?: manager.getBandanas().firstOrNull()

        return drawCardGrid(
            nvg,
            palette,
            accent,
            x,
            width,
            startY,
            scrollOffset,
            mouseX,
            mouseY,
            filtered,
            current,
            { item -> item.getName() },
            { item -> getRequirementText(item.getRequiredRole(), manager::getTranslateText) },
            { item -> manager.canUseBandana(getClientUuid(), item) },
            { item -> createBandanaPreview(item) },
            { item, bounds -> bandanaCardBounds[item] = bounds },
        )
    }

    private fun <T> drawCardGrid(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accent: AccentColor,
        x: Float,
        width: Float,
        startY: Float,
        scrollOffset: Float,
        mouseX: Int,
        mouseY: Int,
        items: List<T>,
        current: T?,
        nameMapper: (T) -> String,
        requirementMapper: (T) -> String,
        unlockedMapper: (T) -> Boolean,
        previewFactory: (T) -> PreviewRenderer,
        boundsCollector: (T, CardBounds) -> Unit,
    ): Float {
        if (items.isEmpty()) {
            nvg.drawText(
                TranslateText.COSMETICS_EMPTY.getText(),
                x,
                startY + 4f,
                ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 210),
                10f,
                Fonts.REGULAR,
            )
            return startY + 28f
        }

        val columns = 4
        val cardWidth = computeCardWidth(width, columns)
        val rows = (items.size + columns - 1) / columns

        for (index in items.indices) {
            val item = items[index]
            val column = index % columns
            val row = index / columns
            val cardX = x + column * (cardWidth + CARD_GAP)
            val cardY = startY + row * (CARD_HEIGHT + CARD_GAP)

            drawCosmeticCard(
                nvg,
                palette,
                accent,
                cardX,
                cardY,
                cardWidth,
                CARD_HEIGHT,
                nameMapper(item),
                requirementMapper(item),
                previewFactory(item),
                current == item,
                unlockedMapper(item),
                mouseX,
                mouseY,
                scrollOffset,
            )

            boundsCollector(item, CardBounds(cardX, cardY + scrollOffset, cardWidth, CARD_HEIGHT))
        }

        val gridHeight = rows * CARD_HEIGHT + max(0, rows - 1) * CARD_GAP
        return startY + gridHeight
    }

    private fun drawCosmeticCard(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accent: AccentColor,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        title: String,
        requirement: String,
        preview: PreviewRenderer,
        selected: Boolean,
        unlocked: Boolean,
        mouseX: Int,
        mouseY: Int,
        scrollOffset: Float,
    ) {
        val hovered = MouseUtils.isInside(mouseX, mouseY, x, y + scrollOffset, width, height)
        val overlayAlpha =
            if (selected) {
                74
            } else if (hovered) {
                48
            } else {
                30
            }

        nvg.drawShadow(x, y, width, height, 12f, 7)
        nvg.drawRoundedRect(
            x,
            y,
            width,
            height,
            12f,
            ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 220),
        )
        nvg.drawOutlineRoundedRect(
            x,
            y,
            width,
            height,
            12f,
            1f,
            ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 210),
        )

        val previewX = x + 6f
        val previewY = y + 7f
        val previewWidth = width - 12f
        val previewHeight = height - 50f

        nvg.drawRoundedRect(
            previewX,
            previewY,
            previewWidth,
            previewHeight,
            9f,
            ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 220),
        )

        preview(nvg, previewX + 1.5f, previewY + 1.5f, previewWidth - 3f, previewHeight - 3f)

        nvg.drawText(title, x + 8f, y + height - 24f, palette.getFontColor(ColorType.DARK), 8.8f, Fonts.MEDIUM)

        if (requirement.isNotEmpty()) {
            nvg.drawText(
                requirement,
                x + 8f,
                y + height - 11f,
                ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 215),
                7.6f,
                Fonts.REGULAR,
            )
        }

        if (selected) {
            val badgeSize = 16f
            val badgeX = x + width - badgeSize - 6f
            val badgeY = y + 6f

            nvg.drawShadow(badgeX, badgeY, badgeSize, badgeSize, 5.5f, 7)
            nvg.drawRoundedRect(
                badgeX,
                badgeY,
                badgeSize,
                badgeSize,
                5.5f,
                ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 220),
            )
            nvg.drawOutlineRoundedRect(
                badgeX,
                badgeY,
                badgeSize,
                badgeSize,
                5.5f,
                1f,
                ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 210),
            )
            nvg.drawCenteredText(
                Lucide.CHECK,
                badgeX + badgeSize / 2f,
                badgeY + badgeSize / 2f - 4f,
                Color.WHITE,
                10.5f,
                Fonts.LUCIDE,
            )
        }

        if (!unlocked) {
            nvg.drawRoundedRect(
                x,
                y,
                width,
                height,
                12f,
                ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 205),
            )
            nvg.drawCenteredText(
                Lucide.LOCK,
                x + width / 2f,
                y + height / 2f - 8f,
                Color(227, 116, 116),
                16f,
                Fonts.LUCIDE,
            )
        }
    }

    private fun handleCapeClick(
        mouseX: Int,
        mouseY: Int,
    ) {
        val manager = Shindo.getInstance().getCapeManager()
        for ((cape, bounds) in capeCardBounds) {
            if (!bounds.contains(mouseX, mouseY)) continue
            if (!manager.canUseCape(getClientUuid(), cape)) {
                Shindo.getInstance().getNotificationManager().post(
                    TranslateText.ERROR,
                    manager.getTranslateError(cape.getRequiredRole()),
                    NotificationType.ERROR,
                )
                return
            }
            manager.setCurrentCape(cape)
            return
        }
    }

    private fun handleWingClick(
        mouseX: Int,
        mouseY: Int,
    ) {
        val manager = Shindo.getInstance().getWingManager()
        for ((wing, bounds) in wingCardBounds) {
            if (!bounds.contains(mouseX, mouseY)) continue
            if (!manager.canUseWing(getClientUuid(), wing)) {
                Shindo.getInstance().getNotificationManager().post(
                    TranslateText.ERROR,
                    manager.getTranslateError(wing.getRequiredRole()),
                    NotificationType.ERROR,
                )
                return
            }
            manager.setCurrentWing(wing)
            return
        }
    }

    private fun handleBandanaClick(
        mouseX: Int,
        mouseY: Int,
    ) {
        val manager = Shindo.getInstance().getBandanaManager()
        for ((bandana, bounds) in bandanaCardBounds) {
            if (!bounds.contains(mouseX, mouseY)) continue
            if (!manager.canUseBandana(getClientUuid(), bandana)) {
                Shindo.getInstance().getNotificationManager().post(
                    TranslateText.ERROR,
                    manager.getTranslateError(bandana.getRequiredRole()),
                    NotificationType.ERROR,
                )
                return
            }
            manager.setCurrentBandana(bandana)
            return
        }
    }

    private fun createCapePreview(cape: Cape): PreviewRenderer {
        if (cape is NormalCape) {
            val sample = cape.getSample()
            if (sample != null) {
                return { nvg, px, py, width, height ->
                    if (!drawImagePreview(nvg, sample, null, px, py, width, height, 6f)) {
                        defaultPreview()(nvg, px, py, width, height)
                    }
                }
            }
        } else if (cape is CustomCape) {
            val sample = cape.getSample()
            return { nvg, px, py, width, height ->
                if (!drawImagePreview(nvg, null, sample, px, py, width, height, 6f)) {
                    defaultPreview()(nvg, px, py, width, height)
                }
            }
        }
        return defaultPreview()
    }

    private fun createWingPreview(wing: Wing): PreviewRenderer {
        val sample = wing.getSample()
        if (sample != null) {
            return { nvg, px, py, width, height ->
                if (!drawImagePreview(nvg, sample, null, px, py, width, height, 6f)) {
                    drawWingConceptPreview(nvg, px, py, width, height, wing.getName())
                }
            }
        }
        return { nvg, px, py, width, height ->
            drawWingConceptPreview(nvg, px, py, width, height, wing.getName())
        }
    }

    private fun createBandanaPreview(bandana: Bandana): PreviewRenderer {
        val sample = bandana.getSample()
        if (sample != null) {
            return { nvg, px, py, width, height ->
                if (!drawImagePreview(nvg, sample, null, px, py, width, height, 6f)) {
                    drawBandanaConceptPreview(nvg, px, py, width, height, bandana.getName())
                }
            }
        }
        return { nvg, px, py, width, height ->
            drawBandanaConceptPreview(nvg, px, py, width, height, bandana.getName())
        }
    }

    private fun drawWingConceptPreview(
        nvg: NanoVGManager,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        label: String,
    ) {
        val palette = Shindo.getInstance().getColorManager().getPalette()
        val accent = Shindo.getInstance().getColorManager().getCurrentColor()

        val tone = if (label.hashCode() % 2 == 0) accent.getColor1() else accent.getColor2()
        nvg.drawRoundedRect(
            x,
            y,
            width,
            height,
            6f,
            ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 215),
        )

        val centerX = x + width / 2f
        val bodyWidth = width * 0.12f
        val wingWidth = width * 0.34f
        val wingHeight = height * 0.22f

        nvg.drawRoundedRect(
            centerX - bodyWidth / 2f,
            y + height * 0.19f,
            bodyWidth,
            height * 0.62f,
            4f,
            ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 240),
        )
        nvg.drawRoundedRect(
            centerX - bodyWidth / 2f - wingWidth,
            y + height * 0.24f,
            wingWidth,
            wingHeight,
            5f,
            ColorUtils.applyAlpha(tone, 185),
        )
        nvg.drawRoundedRect(
            centerX + bodyWidth / 2f,
            y + height * 0.24f,
            wingWidth,
            wingHeight,
            5f,
            ColorUtils.applyAlpha(tone, 185),
        )
        nvg.drawRoundedRect(
            centerX - bodyWidth / 2f - wingWidth * 0.85f,
            y + height * 0.56f,
            wingWidth * 0.85f,
            wingHeight * 0.78f,
            5f,
            ColorUtils.applyAlpha(accent.getColor2(), 170),
        )
        nvg.drawRoundedRect(
            centerX + bodyWidth / 2f,
            y + height * 0.56f,
            wingWidth * 0.85f,
            wingHeight * 0.78f,
            5f,
            ColorUtils.applyAlpha(accent.getColor2(), 170),
        )
    }

    private fun drawBandanaConceptPreview(
        nvg: NanoVGManager,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        label: String,
    ) {
        val palette = Shindo.getInstance().getColorManager().getPalette()
        val accent = Shindo.getInstance().getColorManager().getCurrentColor()

        val tint = if (label.hashCode() % 2 == 0) accent.getColor2() else accent.getColor1()
        nvg.drawRoundedRect(
            x,
            y,
            width,
            height,
            6f,
            ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 215),
        )

        val centerX = x + width / 2f
        val headY = y + height * 0.34f
        val headRadius = min(width, height) * 0.16f
        nvg.drawCircle(
            centerX,
            headY,
            headRadius,
            ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 240),
        )

        val stripY = headY - headRadius * 0.25f
        val stripHeight = headRadius * 0.55f
        nvg.drawGradientRoundedRect(
            centerX - width * 0.30f,
            stripY,
            width * 0.60f,
            stripHeight,
            4f,
            ColorUtils.applyAlpha(tint, 210),
            ColorUtils.applyAlpha(accent.getColor2(), 190),
        )
        nvg.drawRoundedRect(
            centerX + width * 0.21f,
            stripY + stripHeight * 0.12f,
            width * 0.13f,
            height * 0.20f,
            3f,
            ColorUtils.applyAlpha(tint, 175),
        )
        nvg.drawRoundedRect(
            centerX + width * 0.31f,
            stripY + stripHeight * 0.34f,
            width * 0.11f,
            height * 0.16f,
            3f,
            ColorUtils.applyAlpha(accent.getColor1(), 165),
        )
    }

    private fun defaultPreview(): PreviewRenderer =
        { nvg, px, py, width, height ->
            val palette = Shindo.getInstance().getColorManager().getPalette()
            nvg.drawRoundedRect(
                px,
                py,
                width,
                height,
                6f,
                ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 190),
            )
            nvg.drawCenteredText(
                "-",
                px + width / 2f,
                py + height / 2f - 5f,
                palette.getFontColor(ColorType.NORMAL),
                11f,
                Fonts.SEMIBOLD,
            )
        }

    private fun drawImagePreview(
        nvg: NanoVGManager,
        location: ResourceLocation?,
        file: File?,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        radius: Float,
    ): Boolean {
        val size = if (location != null) nvg.getImageSize(location) else nvg.getImageSize(file ?: return false)
        if (size == null || size.width <= 0 || size.height <= 0) {
            return false
        }

        val scaled = scaleToFit(size.width.toFloat(), size.height.toFloat(), width, height)
        val drawX = x + (width - scaled[0]) / 2f
        val drawY = y + (height - scaled[1]) / 2f

        when {
            location != null -> nvg.drawRoundedImage(location, drawX, drawY, scaled[0], scaled[1], radius)
            file != null -> nvg.drawRoundedImage(file, drawX, drawY, scaled[0], scaled[1], radius)
            else -> return false
        }
        return true
    }

    private fun scaleToFit(
        originalWidth: Float,
        originalHeight: Float,
        maxWidth: Float,
        maxHeight: Float,
    ): FloatArray {
        if (originalWidth <= 0f || originalHeight <= 0f) {
            return floatArrayOf(maxWidth, maxHeight)
        }
        var ratio = min(maxWidth / originalWidth, maxHeight / originalHeight)
        ratio = max(0.01f, ratio)
        return floatArrayOf(originalWidth * ratio, originalHeight * ratio)
    }

    private fun isCapeVisible(
        cape: Cape,
        searchQuery: String,
    ): Boolean =
        (activeCapeCategory == CapeCategory.ALL || cape.getCategory() == activeCapeCategory) &&
            matchesSearch(
                cape.getName(),
                searchQuery,
            )

    private fun isWingVisible(
        wing: Wing,
        searchQuery: String,
    ): Boolean =
        (activeWingCategory == WingCategory.ALL || wing.getCategory() == activeWingCategory) &&
            matchesSearch(
                wing.getName(),
                searchQuery,
            )

    private fun isBandanaVisible(
        bandana: Bandana,
        searchQuery: String,
    ): Boolean =
        (activeBandanaCategory == BandanaCategory.ALL || bandana.getCategory() == activeBandanaCategory) &&
            matchesSearch(
                bandana.getName(),
                searchQuery,
            )

    private fun matchesSearch(
        value: String,
        query: String,
    ): Boolean = query.isEmpty() || SearchUtils.isSimilar(value, query)

    private fun getRequirementText(
        role: Role,
        mapper: (Role) -> TranslateText?,
    ): String {
        if (role == Role.MEMBER) {
            return ""
        }
        val translate = mapper(role)
        return if (translate == null || translate == TranslateText.NONE) "" else translate.getText()
    }

    private fun computeCardWidth(
        contentWidth: Float,
        columns: Int,
    ): Float {
        val safeColumns = max(1, columns)
        val available = max(0f, contentWidth - (safeColumns - 1) * CARD_GAP)
        val target = if (available <= 0f) CARD_WIDTH else available / safeColumns
        return max(1f, min(CARD_WIDTH, target))
    }

    private fun getClientUuid(): UUID = Shindo.getInstance().getShindoAPI().getEffectiveUuid()

    private fun handleChipClick(
        chips: List<FilterChip>,
        mouseX: Int,
        mouseY: Int,
    ): Boolean {
        for (chip in chips) {
            if (chip.contains(mouseX, mouseY)) {
                chip.click()
                return true
            }
        }
        return false
    }

    private fun getActiveCategoryOptions(): List<ChipOption> =
        when (activeSection) {
            CosmeticSection.CAPES -> {
                CapeCategory
                    .values()
                    .map { ChipOption(it.getName(), it == activeCapeCategory, Runnable { activeCapeCategory = it }) }
            }

            CosmeticSection.WINGS -> {
                WingCategory
                    .values()
                    .map { ChipOption(it.getName(), it == activeWingCategory, Runnable { activeWingCategory = it }) }
            }

            CosmeticSection.BANDANAS -> {
                BandanaCategory
                    .values()
                    .map {
                        ChipOption(it.getName(), it == activeBandanaCategory, Runnable { activeBandanaCategory = it })
                    }
            }
        }

    private data class CardBounds(
        val x: Float,
        val y: Float,
        val width: Float,
        val height: Float,
    ) {
        fun contains(
            mx: Int,
            my: Int,
        ): Boolean = MouseUtils.isInside(mx, my, x, y, width, height)
    }

    private data class ChipOption(
        val label: String,
        val active: Boolean,
        val onClick: Runnable,
    )

    private enum class CosmeticSection(
        val label: String,
        val icon: String,
        val visible: Boolean,
    ) {
        CAPES("Capes", Lucide.STAR, true),
        WINGS("Wings", Lucide.SHIELD, false),
        BANDANAS("Bandanas", Lucide.USER, false),
    }

    private companion object {
        private const val CONTENT_PADDING = 18f
        private const val SECTION_BLOCK_GAP = 8f
        private const val CATEGORY_BLOCK_GAP = 10f
        private const val SECTION_CHIP_GAP = 7f
        private const val CATEGORY_CHIP_GAP = 6f

        private const val CARD_WIDTH = 122f
        private const val CARD_HEIGHT = 152f
        private const val CARD_GAP = 11f
    }
}

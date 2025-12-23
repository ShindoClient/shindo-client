package me.miki.shindo.gui.modmenu.category.impl

import me.miki.shindo.Shindo
import me.miki.shindo.api.roles.Role
import me.miki.shindo.gui.modmenu.GuiModMenu
import me.miki.shindo.gui.modmenu.category.Category
import me.miki.shindo.gui.modmenu.category.impl.shared.CategoryChipRenderer
import me.miki.shindo.gui.modmenu.category.impl.shared.FilterChip
import me.miki.shindo.management.color.AccentColor
import me.miki.shindo.management.color.palette.ColorPalette
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.cosmetic.cape.CapeCategory
import me.miki.shindo.management.cosmetic.cape.CapeManager
import me.miki.shindo.management.cosmetic.cape.impl.Cape
import me.miki.shindo.management.cosmetic.cape.impl.CustomCape
import me.miki.shindo.management.cosmetic.cape.impl.NormalCape
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.notification.NotificationType
import me.miki.shindo.utils.ColorUtils
import me.miki.shindo.utils.SearchUtils
import me.miki.shindo.utils.mouse.MouseUtils
import net.minecraft.client.Minecraft
import net.minecraft.util.ResourceLocation
import java.awt.Dimension
import java.io.File
import java.util.ArrayList
import java.util.IdentityHashMap
import java.util.UUID

class CosmeticsCategory(parent: GuiModMenu) : Category(parent, TranslateText.COSMETICS, LegacyIcon.SHOPPING, true, true) {

    private val capeCardBounds = IdentityHashMap<Cape, CardBounds>()
    private val categoryChips = ArrayList<FilterChip>()
    private var activeCategory = CapeCategory.ALL

    override fun initGui() {
        activeCategory = CapeCategory.ALL
        scroll.resetAll()
    }

    override fun initCategory() {
        scroll.resetAll()
    }

    fun shouldShowCustomCapeFolder(): Boolean {
        return activeCategory == CapeCategory.CUSTOM
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val instance = Shindo.getInstance()
        val nvg = instance.nanoVGManager!!
        val palette = instance.colorManager.palette
        val accent = instance.colorManager.currentColor

        val viewportX = getX().toFloat()
        val viewportY = getY().toFloat()
        val viewportWidth = getWidth().toFloat()
        val viewportHeight = getHeight().toFloat()

        categoryChips.clear()
        capeCardBounds.clear()

        if (MouseUtils.isInside(mouseX, mouseY, viewportX, viewportY, viewportWidth, viewportHeight)) {
            scroll.onScroll()
        }
        scroll.onAnimation()
        val scrollOffset = scroll.getValue()

        val searchQuery = getSearchBox().getText().trim() ?: ""

        val contentX = viewportX + CONTENT_PADDING
        val contentWidth = viewportWidth - (CONTENT_PADDING * 2f)
        val startY = viewportY + CONTENT_PADDING
        var y = startY

        nvg.save()
        nvg.scissor(viewportX, viewportY, viewportWidth, viewportHeight)
        nvg.translate(0f, scrollOffset)

        y = drawCategoryChips(nvg, palette, accent, contentX, contentWidth, y, scrollOffset, mouseX, mouseY)
        y += 20f
        y = drawCapeGrid(nvg, palette, accent, contentX, contentWidth, y, scrollOffset, searchQuery, mouseX, mouseY)

        nvg.restore()

        val logicalHeight = kotlin.math.max(0f, (y - startY) + CONTENT_PADDING)
        scroll.maxScroll = kotlin.math.max(0f, logicalHeight - viewportHeight)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (handleCategoryChipClick(mouseX, mouseY, mouseButton)) {
            return
        }
        handleCapeClick(mouseX, mouseY, mouseButton)
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, mouseButton: Int) {
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        scroll.onKey(keyCode)
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
        mouseY: Int
    ): Float {
        categoryChips.clear()

        var currentX = x
        var currentY = y

        for (category in CapeCategory.values()) {
            val label = category.name
            val chipWidth = CategoryChipRenderer.computeWidth(nvg, label, null)

            if (currentX + chipWidth > x + width) {
                currentX = x
                currentY += CategoryChipRenderer.CHIP_HEIGHT + CHIP_GAP
            }

            val active = category == activeCategory
            val hovered = MouseUtils.isInside(mouseX, mouseY, currentX, currentY + scrollOffset, chipWidth, CategoryChipRenderer.CHIP_HEIGHT)

            CategoryChipRenderer.drawChip(nvg, palette, accent, currentX, currentY, chipWidth, label, null, active, hovered)

            val chip = FilterChip(Runnable {
                if (activeCategory != category) {
                    activeCategory = category
                    scroll.resetAll()
                }
            })
            chip.setBounds(currentX, currentY + scrollOffset, chipWidth, CategoryChipRenderer.CHIP_HEIGHT)
            categoryChips.add(chip)

            currentX += chipWidth + CHIP_GAP
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
        mouseY: Int
    ): Float {
        val capeManager = Shindo.getInstance().capeManager
        val filtered = ArrayList<Cape>()

        for (cape in capeManager.getCapes()) {
            if (!isCapeVisible(cape, searchQuery)) {
                continue
            }
            filtered.add(cape)
        }

        var y = startY

        if (filtered.isEmpty()) {
            nvg.drawText(
                TranslateText.COSMETICS_EMPTY.text,
                x,
                y + 4f,
                ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 210),
                10f,
                Fonts.REGULAR
            )
            return y + 28f
        }

        val columns = 3
        val cardWidth = computeCardWidth(width, columns)
        val rows = (filtered.size + columns - 1) / columns

        val current = capeManager.getCurrentCape() ?: capeManager.getCapes().firstOrNull()

        for (index in filtered.indices) {
            val cape = filtered[index]
            val column = index % columns
            val row = index / columns

            val cardX = x + column * (cardWidth + CARD_GAP)
            val cardY = y + row * (CARD_HEIGHT + CARD_GAP)

            val selected = cape == current
            val unlocked = capeManager.canUseCape(getClientUuid(), cape)
            val state = SimpleCardState.of(selected, unlocked)

            val preview = createCapePreview(cape)
            drawCapeCard(
                nvg,
                palette,
                accent,
                cardX,
                cardY,
                cardWidth,
                CARD_HEIGHT,
                preview,
                cape,
                state,
                formatRequirement(cape.getRequiredRole(), capeManager::getTranslateText),
                mouseX,
                mouseY,
                scrollOffset
            )

            capeCardBounds.computeIfAbsent(cape) { CardBounds() }
                .set(cardX, cardY + scrollOffset, cardWidth, CARD_HEIGHT)
        }

        val gridHeight = rows * CARD_HEIGHT + kotlin.math.max(0, rows - 1) * CARD_GAP
        return y + gridHeight
    }

    private fun drawCapeCard(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accent: AccentColor,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        preview: PreviewRenderer,
        cape: Cape,
        state: SimpleCardState,
        requirement: String,
        mouseX: Int,
        mouseY: Int,
        scrollOffset: Float
    ) {
        val hovered = MouseUtils.isInside(mouseX, mouseY, x, y + scrollOffset, width, height)

        val base = palette.getBackgroundColor(ColorType.DARK)
        nvg.drawRoundedRect(x, y, width, height, 14f, base)
        nvg.drawRoundedRect(
            x + 1f,
            y + 1f,
            width - 2f,
            height - 2f,
            13f,
            ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), if (hovered) 235 else 215)
        )

        if (state.isSelected) {
            nvg.drawGradientRoundedRect(
                x,
                y,
                width,
                height,
                14f,
                ColorUtils.applyAlpha(accent.color1, 140),
                ColorUtils.applyAlpha(accent.color2, 140)
            )
        }

        val previewX = x + 12f
        val previewY = y + 14f
        val previewWidth = width - 20f
        val previewHeight = height - 78f

        nvg.drawRoundedRect(previewX, previewY, previewWidth, previewHeight, 10f, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 210))
        preview(nvg, previewX + 4f, previewY + 4f, previewWidth - 8f, previewHeight - 8f)

        nvg.drawText(cape.getName(), x + 12f, y + height - 34f, palette.getFontColor(ColorType.DARK), 9.8f, Fonts.MEDIUM)

        if (requirement.isNotEmpty()) {
            nvg.drawText(
                requirement,
                x + 12f,
                y + height - 20f,
                ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 220),
                8.6f,
                Fonts.REGULAR
            )
        }

        if (!state.isUnlocked) {
            val mask = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 210)
            nvg.drawRoundedRect(x, y, width, height, 14f, mask)
            nvg.drawCenteredText(LegacyIcon.LOCK, x + width / 2f, y + height / 2f - 8f, java.awt.Color(227, 116, 116), 18f, Fonts.LEGACYICON)
        }
    }

    private fun handleCategoryChipClick(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        if (mouseButton != 0) {
            return false
        }
        for (chip in categoryChips) {
            if (chip.contains(mouseX, mouseY)) {
                chip.click()
                return true
            }
        }
        return false
    }

    private fun handleCapeClick(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (mouseButton != 0) {
            return
        }

        val capeManager = Shindo.getInstance().capeManager

        for ((cape, bounds) in capeCardBounds.entries) {
            if (!bounds.contains(mouseX, mouseY)) {
                continue
            }
            if (!capeManager.canUseCape(getClientUuid(), cape)) {
                Shindo.getInstance().notificationManager
                .post(TranslateText.ERROR, capeManager.getTranslateError(cape.getRequiredRole()), NotificationType.ERROR)
                return
            }
            capeManager.setCurrentCape(cape)
            return
        }
    }
    private fun createCapePreview(cape: Cape): PreviewRenderer {
        if (cape is NormalCape) {
            val sample = cape.getSample()
            if (sample != null) {
                return { nvg, px, py, width, height ->
                    if (!drawImagePreview(nvg, sample, null, px, py, width, height, 8f)) {
                        defaultPreview()(nvg, px, py, width, height)
                    }
                }
            }
        } else if (cape is CustomCape) {
            val sample = cape.getSample()
            if (sample != null) {
                return { nvg, px, py, width, height ->
                    if (!drawImagePreview(nvg, null, sample, px, py, width, height, 8f)) {
                        defaultPreview()(nvg, px, py, width, height)
                    }
                }
            }
        }
        return defaultPreview()
    }

    private fun defaultPreview(): PreviewRenderer {
        return { nvg, px, py, width, height ->
            val palette = Shindo.getInstance().colorManager.palette
            nvg.drawRoundedRect(px, py, width, height, 8f, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 190))
            nvg.drawCenteredText("-", px + width / 2f, py + height / 2f - 6f, palette.getFontColor(ColorType.NORMAL), 12f, Fonts.SEMIBOLD)
        }
    }

    private fun drawImagePreview(
        nvg: NanoVGManager,
        location: ResourceLocation?,
        file: File?,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        radius: Float
    ): Boolean {
        val size = if (location != null) nvg.getImageSize(location) else nvg.getImageSize(file!!)
        if (size == null || size.width <= 0 || size.height <= 0) {
            return false
        }

        val scaled = scaleToFit(size.width.toFloat(), size.height.toFloat(), width, height)
        val drawX = x + (width - scaled[0]) / 2f
        val drawY = y + (height - scaled[1]) / 2f

        if (location != null) {
            nvg.drawRoundedImage(location, drawX, drawY, scaled[0], scaled[1], radius)
        } else if (file != null) {
            nvg.drawRoundedImage(file, drawX, drawY, scaled[0], scaled[1], radius)
        } else {
            return false
        }
        return true
    }

    private fun scaleToFit(originalWidth: Float, originalHeight: Float, maxWidth: Float, maxHeight: Float): FloatArray {
        if (originalWidth <= 0f || originalHeight <= 0f) {
            return floatArrayOf(maxWidth, maxHeight)
        }
        var ratio = kotlin.math.min(maxWidth / originalWidth, maxHeight / originalHeight)
        ratio = kotlin.math.max(0.01f, ratio)
        return floatArrayOf(originalWidth * ratio, originalHeight * ratio)
    }

    private fun isCapeVisible(cape: Cape, searchQuery: String): Boolean {
        if (activeCategory != CapeCategory.ALL && cape.getCategory() != activeCategory) {
            return false
        }
        return matchesSearch(cape.getName(), searchQuery)
    }

    private fun matchesSearch(value: String, query: String): Boolean {
        if (query.isEmpty()) {
            return true
        }
        return SearchUtils.isSimilar(value, query)
    }

    private fun formatRequirement(role: Role?, mapper: (Role) -> TranslateText?): String {
        if (role == null || role == Role.MEMBER) {
            return ""
        }
        val translate = mapper(role)
        if (translate == null || translate == TranslateText.NONE) {
            return ""
        }
        return translate.text
    }

    private fun computeCardWidth(contentWidth: Float, columns: Int): Float {
        val safeColumns = kotlin.math.max(1, columns)
        val available = kotlin.math.max(0f, contentWidth - (safeColumns - 1) * CARD_GAP)
        val target = if (available <= 0f) CARD_WIDTH else available / safeColumns
        return kotlin.math.max(1f, kotlin.math.min(CARD_WIDTH, target))
    }

    private fun getClientUuid(): UUID {
        return Shindo.getInstance().shindoAPI.getEffectiveUuid()
    }

    private class CardBounds {
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

        fun contains(mx: Int, my: Int): Boolean {
            return MouseUtils.isInside(mx, my, x, y, width, height)
        }
    }

    private data class SimpleCardState(val isSelected: Boolean, val isUnlocked: Boolean) {
        companion object {
            fun of(selected: Boolean, unlocked: Boolean): SimpleCardState {
                return SimpleCardState(selected, unlocked)
            }
        }
    }

    private companion object {
        private const val CONTENT_PADDING = 26f
        private const val CHIP_GAP = 12f
        private const val CARD_WIDTH = 140f
        private const val CARD_HEIGHT = 190f
        private const val CARD_GAP = 22f
    }
}

typealias PreviewRenderer = (NanoVGManager, Float, Float, Float, Float) -> Unit

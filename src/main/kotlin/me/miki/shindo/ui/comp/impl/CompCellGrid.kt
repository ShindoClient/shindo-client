package me.miki.shindo.ui.comp.impl

import me.miki.shindo.management.color.AccentColor
import me.miki.shindo.management.color.palette.ColorPalette
import me.miki.shindo.management.color.palette.ColorType
import me.miki.shindo.management.mods.impl.CrosshairMod
import me.miki.shindo.management.mods.impl.crosshair.LayoutManager
import me.miki.shindo.management.mods.impl.crosshair.LayoutManager.CellGridPreset
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.nanovg.NanoVGManager
import me.miki.shindo.management.nanovg.font.Fonts
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.impl.CellGridSetting
import me.miki.shindo.ui.comp.Comp
import me.miki.shindo.utils.ColorUtils
import me.miki.shindo.utils.mouse.MouseUtils
import net.minecraft.util.ResourceLocation
import java.awt.Color
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

class CompCellGrid : Comp {
    private val setting: CellGridSetting
    private val presetCards: MutableList<PresetCard> = mutableListOf()
    private val swatchColors: MutableList<Color> = mutableListOf()
    private val swatchBounds: MutableList<Bounds> = mutableListOf()

    private var saveButtonBounds: Bounds? = null
    private var clearButtonBounds: Bounds? = null
    private var colorToggleBounds: Bounds? = null
    private var hueBounds: Bounds? = null
    private var sbBounds: Bounds? = null
    private var alphaBounds: Bounds? = null

    private var editingPreset: CellGridPreset? = null
    private var activePresetId: String? = null
    private var paintColor: Color = Color.WHITE
    private var hue: Float = 0f
    private var saturation: Float = 0f
    private var brightness: Float = 1f
    private var alpha: Int = 255

    private var pickerOpen = false
    private var hueDragging = false
    private var sbDragging = false
    private var alphaDragging = false

    constructor(x: Float, y: Float, width: Int, height: Int, setting: CellGridSetting) : super(x, y) {
        this.setting = setting
        setWidth(width.toFloat())
        setHeight(height.toFloat())
        syncHSB(Color.RED)
    }

    constructor(width: Float, height: Float, setting: CellGridSetting) : super(0f, 0f) {
        this.setting = setting
        setWidth(width)
        setHeight(height)
        syncHSB(Color.RED)
    }

    override fun draw(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val nvgInstance = nvg
        val palette = palette
        val accent = accent

        val metrics = computeGridMetrics()

        drawGrid(nvgInstance, palette, mouseX, mouseY, metrics.gridX, metrics.gridY, metrics.gridBoxSize, metrics.cellSize)
        val colorBottom = drawColorControls(nvgInstance, palette, accent, mouseX, mouseY, metrics.rightX, metrics.gridY, metrics.rightWidth)
        val buttonsBottom = drawButtons(nvgInstance, palette, accent, mouseX, mouseY, metrics.rightX, colorBottom + 8f, metrics.rightWidth)

        val topBottom = max(metrics.gridY + metrics.gridBoxSize, buttonsBottom)
        val presetsStartY = topBottom + 14f
        val minPresetHeight = PRESET_CARD_HEIGHT * 2f + PRESET_GAP
        val availableHeight = max(minPresetHeight, getHeight() - (presetsStartY - getY()) - metrics.padding)
        drawPresets(nvgInstance, palette, accent, mouseX, mouseY, getX() + metrics.padding, presetsStartY, metrics.contentWidth, availableHeight)

        updatePickerDrag(mouseX, mouseY)
        super.draw(mouseX, mouseY, partialTicks)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (mouseButton != 0) {
            super.mouseClicked(mouseX, mouseY, mouseButton)
            return
        }

        if (handlePickerClick(mouseX, mouseY)) {
            super.mouseClicked(mouseX, mouseY, mouseButton)
            return
        }

        if (colorToggleBounds?.contains(mouseX, mouseY) == true) {
            pickerOpen = !pickerOpen
            super.mouseClicked(mouseX, mouseY, mouseButton)
            return
        }

        if (processSwatchClick(mouseX, mouseY)) {
            super.mouseClicked(mouseX, mouseY, mouseButton)
            return
        }

        if (saveButtonBounds?.contains(mouseX, mouseY) == true) {
            savePreset()
            super.mouseClicked(mouseX, mouseY, mouseButton)
            return
        }

        if (clearButtonBounds?.contains(mouseX, mouseY) == true) {
            clearGrid()
            super.mouseClicked(mouseX, mouseY, mouseButton)
            return
        }

        if (processPresetClick(mouseX, mouseY)) {
            super.mouseClicked(mouseX, mouseY, mouseButton)
            return
        }

        processGridClick(mouseX, mouseY)
        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, mouseButton: Int) {
        hueDragging = false
        sbDragging = false
        alphaDragging = false
        super.mouseReleased(mouseX, mouseY, mouseButton)
    }

    private fun drawGrid(
        nvg: NanoVGManager,
        palette: ColorPalette,
        mouseX: Int,
        mouseY: Int,
        x: Float,
        y: Float,
        boxSize: Float,
        cellSize: Float) {
        val outer = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 200)
        val inner = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 210)
        nvg.drawRoundedRect(x, y, boxSize, boxSize, 7f, outer)
        nvg.drawRoundedRect(x + 1f, y + 1f, boxSize - 2f, boxSize - 2f, 6f, inner)

        val cells = setting.getCells()!!
        val gridLight = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 150)
        val gridDark = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 170)

        for (row in 0 until GRID_SIZE) {
            for (col in 0 until GRID_SIZE) {
                val cx = x + GRID_PADDING + col * cellSize
                val cy = y + GRID_PADDING + row * cellSize
                val active = row < cells.size && cells[row] != null && col < cells[row].size && cells[row][col]
                val cellBg = if ((row + col) % 2 == 0) gridLight else gridDark
                nvg.drawRect(cx, cy, cellSize, cellSize, cellBg)
                val baseColor = if (active) {
                    setting.getCellColorOrDefault(row, col, paintColor)
                } else {
                    palette.getBackgroundColor(ColorType.MID)
                }

                val hovered = MouseUtils.isInside(mouseX, mouseY, cx, cy, cellSize, cellSize)
                val fill = if (active) {
                    ColorUtils.applyAlpha(baseColor, if (hovered) 255 else max(200, baseColor.alpha))
                } else {
                    ColorUtils.applyAlpha(baseColor, if (hovered) 140 else 90)
                }

                if (active) {
                    nvg.drawRect(cx, cy, cellSize, cellSize, fill)
                }
                if (hovered) {
                    nvg.drawOutlineRoundedRect(cx, cy, cellSize, cellSize, 0f, 1.1f, ColorUtils.applyAlpha(baseColor, 190))
                }
            }
        }
    }

    private fun drawColorControls(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accent: AccentColor,
        mouseX: Int,
        mouseY: Int,
        x: Float,
        y: Float,
        width: Float): Float {
        swatchColors.clear()
        swatchBounds.clear()
        swatchColors.addAll(
            listOf(
                accent.color1,
                accent.color2,
                Color(255, 255, 255),
                Color(0, 0, 0),
                Color(255, 96, 112),
                Color(93, 126, 255))
        )

        val labelY = y
        nvg.drawText(TranslateText.COLOR.text, x, labelY, palette.getFontColor(ColorType.DARK), 9.5f, Fonts.MEDIUM)

        val swatchY = labelY + 12f
        val perRow = 3
        val rowSpacing = 6f
        for (i in 0 until min(MAX_SWATCHES, swatchColors.size)) {
            val col = i % perRow
            val row = i / perRow
            val swatchX = x + col * (SWATCH_SIZE + 8f)
            val swatchRowY = swatchY + row * (SWATCH_SIZE + rowSpacing)

            val swatch = swatchColors[i]
            val hovered = MouseUtils.isInside(mouseX, mouseY, swatchX, swatchRowY, SWATCH_SIZE, SWATCH_SIZE)
            nvg.drawRoundedRect(swatchX, swatchRowY, SWATCH_SIZE, SWATCH_SIZE, 3f, ColorUtils.applyAlpha(swatch, if (hovered) 255 else 230))
            nvg.drawOutlineRoundedRect(
                swatchX,
                swatchRowY,
                SWATCH_SIZE,
                SWATCH_SIZE,
                3f,
                1f,
                ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), if (hovered) 150 else 100)
            )
            if (swatch == paintColor) {
                nvg.drawOutlineRoundedRect(
                    swatchX - 1f,
                    swatchRowY - 1f,
                    SWATCH_SIZE + 2f,
                    SWATCH_SIZE + 2f,
                    4f,
                    1.1f,
                    ColorUtils.applyAlpha(palette.getFontColor(ColorType.DARK), 200)
                )
            }

            swatchBounds.add(Bounds(swatchX, swatchRowY, SWATCH_SIZE, SWATCH_SIZE))
        }

        val swatchBlockHeight =
            ceil(min(MAX_SWATCHES, swatchColors.size).toFloat() / perRow.toFloat()) * (SWATCH_SIZE + rowSpacing) - rowSpacing
        val toggleY = swatchY + swatchBlockHeight + 6f
        val toggleWidth = max(90f, width * 0.55f)
        val toggleBg = if (pickerOpen) ColorUtils.applyAlpha(accent.color1, 210) else palette.getBackgroundColor(ColorType.NORMAL)
        colorToggleBounds = Bounds(x, toggleY, toggleWidth, 18f)
        val toggleLabel =
            if (pickerOpen) TranslateText.CROSSHAIR_COLOR_PICKER_CLOSE.text else TranslateText.CROSSHAIR_COLOR_PICKER_OPEN.text
        colorToggleBounds?.let {
            nvg.drawRoundedRect(it.x, it.y, it.width, it.height, 5f, toggleBg)
            nvg.drawText(toggleLabel, it.x + 6f, it.y + 6f, palette.getFontColor(ColorType.DARK), 8.5f, Fonts.MEDIUM)
        }

        val previewX = (colorToggleBounds?.x ?: 0f) + (colorToggleBounds?.width ?: 0f) + 8f
        val previewSize = 18f
        if (previewX + previewSize <= x + width) {
            nvg.drawRoundedRect(previewX, toggleY, previewSize, previewSize, 4f, paintColor)
            nvg.drawOutlineRoundedRect(
                previewX,
                toggleY,
                previewSize,
                previewSize,
                4f,
                1f,
                ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 140)
            )
        }

        if (pickerOpen) {
            val pickerY = (colorToggleBounds?.y ?: 0f) + (colorToggleBounds?.height ?: 0f) + 6f
            val maxPickerWidth = max(80f, min(width, x + getWidth() - x - 6f))
            drawColorPicker(nvg, palette, accent, mouseX, mouseY, x, pickerY, maxPickerWidth)
        } else {
            hueBounds = null
            sbBounds = null
            alphaBounds = null
        }
        val bottom = if (pickerOpen && alphaBounds != null) {
            alphaBounds!!.y + alphaBounds!!.height
        } else {
            (colorToggleBounds?.y ?: 0f) + (colorToggleBounds?.height ?: 0f)
        }
        return bottom
    }

    private fun drawColorPicker(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accent: AccentColor,
        mouseX: Int,
        mouseY: Int,
        x: Float,
        y: Float,
        size: Float) {
        val maxWidth = max(80f, getX() + getWidth() - x - 6f)
        var sbSize = min(size, maxWidth)
        val hueWidth = 12f
        val padding = 6f

        val maxHeight = (getY() + getHeight()) - y - 6f
        val requiredHeight = sbSize + padding + 12f
        if (requiredHeight > maxHeight) {
            sbSize = max(60f, maxHeight - padding - 12f)
        }

        val hueColor = Color.getHSBColor(hue, 1f, 1f)

        sbBounds = Bounds(x, y, sbSize, sbSize)
        hueBounds = Bounds(x + sbSize + padding, y, hueWidth, sbSize)
        alphaBounds = Bounds(x, y + sbSize + padding, sbSize + hueWidth + padding, 12f)

        nvg.drawHSBBox(sbBounds!!.x, sbBounds!!.y, sbBounds!!.width, sbBounds!!.height, 6f, hueColor)
        val satX = sbBounds!!.x + saturation * sbBounds!!.width
        val brightY = sbBounds!!.y + sbBounds!!.height - brightness * sbBounds!!.height
        nvg.drawArc(satX, brightY, 3.2f, 0f, 360f, 1.2f, Color.WHITE)

        nvg.drawRoundedImage(HUE_TEXTURE, hueBounds!!.x, hueBounds!!.y, hueWidth, sbSize, 3f)
        val hueY = hueBounds!!.y + sbSize * hue
        nvg.drawArc(hueBounds!!.x + hueWidth / 2f, hueY, 3.2f, 0f, 360f, 1.1f, Color.WHITE)

        val alphaColor = Color(paintColor.red, paintColor.green, paintColor.blue, 255)
        nvg.drawRoundedImage(ALPHA_TEXTURE, alphaBounds!!.x, alphaBounds!!.y, alphaBounds!!.width, alphaBounds!!.height, 3f)
        nvg.drawAlphaBar(alphaBounds!!.x, alphaBounds!!.y, alphaBounds!!.width, alphaBounds!!.height, 3f, alphaColor)
        val alphaX = alphaBounds!!.x + alphaBounds!!.width * (alpha / 255f)
        nvg.drawArc(alphaX, alphaBounds!!.y + alphaBounds!!.height / 2f, 3.2f, 0f, 360f, 1.1f, Color.WHITE)
    }

    private fun drawButtons(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accent: AccentColor,
        mouseX: Int,
        mouseY: Int,
        x: Float,
        y: Float,
        width: Float): Float {
        val spacing = 8f
        var primaryWidth = max(90f, min(width * 0.6f, width - spacing - 70f))
        var secondaryWidth = width - primaryWidth - spacing
        if (secondaryWidth < 64f) {
            secondaryWidth = 64f
            primaryWidth = max(70f, width - spacing - secondaryWidth)
        }

        saveButtonBounds = drawButton(
            nvg,
            palette,
            accent,
            x,
            y,
            primaryWidth,
            BUTTON_HEIGHT,
            TranslateText.CROSSHAIR_SAVE_PRESET.text,
            true,
            MouseUtils.isInside(mouseX, mouseY, x, y, primaryWidth, BUTTON_HEIGHT))
        clearButtonBounds = drawButton(
            nvg,
            palette,
            accent,
            x + primaryWidth + 10f,
            y,
            secondaryWidth,
            BUTTON_HEIGHT,
            TranslateText.CROSSHAIR_CLEAR_GRID.text,
            false,
            MouseUtils.isInside(mouseX, mouseY, x + primaryWidth + 10f, y, secondaryWidth, BUTTON_HEIGHT))
        return y + BUTTON_HEIGHT
    }

    private fun drawPresets(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accent: AccentColor,
        mouseX: Int,
        mouseY: Int,
        x: Float,
        y: Float,
        width: Float,
        availableHeight: Float) {
        presetCards.clear()
        val presets = CrosshairMod.layoutManager.customPresets
        val maxSlots = LayoutManager.MAX_CUSTOM_PRESETS

        val cardWidth = (width - PRESET_GAP * (PRESET_COLUMNS - 1)) / PRESET_COLUMNS
        var cardHeight = min(PRESET_CARD_HEIGHT, (availableHeight - PRESET_GAP) / 2f)
        cardHeight = max(48f, cardHeight)

        var addCardPlaced = false

        for (index in 0 until maxSlots) {
            val row = index / PRESET_COLUMNS
            val col = index % PRESET_COLUMNS
            val cardX = x + col * (cardWidth + PRESET_GAP)
            val cardY = y + row * (cardHeight + PRESET_GAP)
            val cardBounds = Bounds(cardX, cardY, cardWidth, cardHeight)

            val preset = if (index < presets.size) presets[index] else null
            val hasPreset = preset != null
            val isAddCard = !hasPreset && !addCardPlaced

            if (preset != null) {
                val hovered = cardBounds.contains(mouseX, mouseY)
                val active = activePresetId != null && activePresetId == preset.id
                val editing = isEditing(preset)

                var bg = if (hovered) {
                    ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 200)
                } else {
                    palette.getBackgroundColor(ColorType.NORMAL)
                }
                if (active) {
                    bg = ColorUtils.applyAlpha(accent.color1, if (hovered) 200 else 170)
                }

                nvg.drawRoundedRect(cardBounds.x, cardBounds.y, cardBounds.width, cardBounds.height, 6f, bg)
                if (active || hovered || editing) {
                    nvg.drawOutlineRoundedRect(
                        cardBounds.x,
                        cardBounds.y,
                        cardBounds.width,
                        cardBounds.height,
                        6f,
                        1.3f,
                        ColorUtils.applyAlpha(accent.color2, if (active) 200 else 150)
                    )
                }

                val previewSize = min(cardBounds.width - 16f, cardBounds.height - 16f)
                val previewX = cardBounds.x + (cardBounds.width - previewSize) / 2f
                val previewY = cardBounds.y + (cardBounds.height - previewSize) / 2f
                drawPresetPreview(
                    nvg,
                    preset,
                    previewX,
                    previewY,
                    previewSize,
                    ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 100),
                    ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 120))

                var deleteBounds: Bounds? = null
                var editBounds: Bounds? = null
                if (hovered) {
                    val iconSize = 12f
                    val iconY = cardBounds.y + 6f
                    editBounds = Bounds(cardBounds.x + cardBounds.width - iconSize * 2f - 8f, iconY, iconSize, iconSize)
                    deleteBounds = Bounds(cardBounds.x + cardBounds.width - iconSize - 4f, iconY, iconSize, iconSize)

                    nvg.drawText(LegacyIcon.PENCIL, editBounds.x, editBounds.y, palette.getFontColor(ColorType.DARK), 11f, Fonts.LEGACYICON)
                    nvg.drawText(LegacyIcon.TRASH, deleteBounds.x, deleteBounds.y, ColorUtils.applyAlpha(palette.getFontColor(ColorType.DARK), 220), 11f, Fonts.LEGACYICON)
                }

                presetCards.add(PresetCard(preset, cardBounds, deleteBounds, editBounds, false))
            } else if (isAddCard) {
                val hovered = cardBounds.contains(mouseX, mouseY)
                val bg = if (hovered) ColorUtils.applyAlpha(accent.color1, 200) else palette.getBackgroundColor(ColorType.NORMAL)
                nvg.drawRoundedRect(cardBounds.x, cardBounds.y, cardBounds.width, cardBounds.height, 6f, bg)
                nvg.drawOutlineRoundedRect(
                    cardBounds.x,
                    cardBounds.y,
                    cardBounds.width,
                    cardBounds.height,
                    6f,
                    1.2f,
                    ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 140)
                )

                nvg.drawText(
                    LegacyIcon.PLUS,
                    cardBounds.x + cardBounds.width / 2f - 6f,
                    cardBounds.y + cardBounds.height / 2f - 7f,
                    palette.getFontColor(ColorType.DARK),
                    14f,
                    Fonts.LEGACYICON
                )
                presetCards.add(PresetCard(null, cardBounds, null, null, true))
                addCardPlaced = true
            }
        }
    }

    private fun drawPresetPreview(
        nvg: NanoVGManager,
        preset: CellGridPreset,
        x: Float,
        y: Float,
        size: Float,
        evenBg: Color,
        oddBg: Color) {
        val layout = normalizeLayout(preset.layoutCopy) ?: return
        val colors = normalizeColors(preset.colorCopy, layout)
        val cell = size / GRID_SIZE
        for (row in 0 until GRID_SIZE) {
            for (col in 0 until GRID_SIZE) {
                val cx = x + col * cell
                val cy = y + row * cell
                val bg = if ((row + col) % 2 == 0) evenBg else oddBg
                nvg.drawRect(cx, cy, cell, cell, bg)

                val layoutRow = layout[row]
                val enabled = col < layoutRow.size && layoutRow[col]
                if (!enabled) continue
                val rgb = if (colors != null) {
                    val colorRow = if (row < colors.size) colors[row] else null
                    if (colorRow != null && col < colorRow.size) colorRow[col] else Color.WHITE.rgb
                } else {
                    Color.WHITE.rgb
                }
                nvg.drawRect(cx, cy, cell, cell, Color(rgb, true))
            }
        }
    }

    private fun drawButton(
        nvg: NanoVGManager,
        palette: ColorPalette,
        accent: AccentColor,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        label: String,
        primary: Boolean,
        hovered: Boolean): Bounds {
        val bg = if (primary) {
            ColorUtils.applyAlpha(accent.color1, if (hovered) 220 else 190)
        } else {
            palette.getBackgroundColor(ColorType.NORMAL)
        }
        val textColor = if (primary) palette.getFontColor(ColorType.DARK) else palette.getFontColor(ColorType.NORMAL)
        nvg.drawRoundedRect(x, y, width, height, 6f, bg)
        nvg.drawText(label, x + 8f, y + height / 2f - 3f, textColor, 9f, Fonts.MEDIUM)
        return Bounds(x, y, width, height)
    }

    private fun processGridClick(mouseX: Int, mouseY: Int) {
        val metrics = computeGridMetrics()
        val cellSize = metrics.cellSize
        val gridX = metrics.gridX
        val gridY = metrics.gridY
        val cells = setting.getCells() ?: return

        for (row in 0 until GRID_SIZE) {
            for (col in 0 until GRID_SIZE) {
                val cx = gridX + GRID_PADDING + col * cellSize
                val cy = gridY + GRID_PADDING + row * cellSize
                if (MouseUtils.isInside(mouseX, mouseY, cx, cy, cellSize, cellSize)) {
                    if (row >= cells.size || cells[row] == null || col >= cells[row].size) return
                    val current = cells[row][col]
                    toggleCell(row, col, !current)
                    return
                }
            }
        }
    }

    private fun processPresetClick(mouseX: Int, mouseY: Int): Boolean {
        for (card in presetCards) {
            if (card.deleteBounds?.contains(mouseX, mouseY) == true && card.preset != null) {
                if (isEditing(card.preset)) {
                    editingPreset = null
                }
                if (activePresetId != null && activePresetId == card.preset.id) {
                    activePresetId = null
                }
                CrosshairMod.layoutManager.removePreset(card.preset)
                return true
            }
            if (card.editBounds?.contains(mouseX, mouseY) == true && card.preset != null) {
                beginEditing(card.preset)
                return true
            }
            if (card.bounds.contains(mouseX, mouseY)) {
                if (card.addCard) {
                    editingPreset = null
                    savePreset()
                    return true
                }
                if (card.preset != null) {
                    applyPreset(card.preset)
                    editingPreset = null
                    return true
                }
            }
        }
        return false
    }

    private fun processSwatchClick(mouseX: Int, mouseY: Int): Boolean {
        for (i in swatchBounds.indices) {
            if (i >= swatchColors.size) break
            val b = swatchBounds[i]
            if (b.contains(mouseX, mouseY)) {
                val swatch = swatchColors[i]
                syncHSB(swatch)
                paintColor = swatch
                return true
            }
        }
        return false
    }

    private fun handlePickerClick(mouseX: Int, mouseY: Int): Boolean {
        if (!pickerOpen) return false
        if (sbBounds?.contains(mouseX, mouseY) == true) {
            sbDragging = true
            updateSBFromMouse(mouseX, mouseY)
            return true
        }
        if (hueBounds?.contains(mouseX, mouseY) == true) {
            hueDragging = true
            updateHueFromMouse(mouseY)
            return true
        }
        if (alphaBounds?.contains(mouseX, mouseY) == true) {
            alphaDragging = true
            updateAlphaFromMouse(mouseX)
            return true
        }
        return false
    }

    private fun updatePickerDrag(mouseX: Int, mouseY: Int) {
        if (sbDragging) updateSBFromMouse(mouseX, mouseY)
        if (hueDragging) updateHueFromMouse(mouseY)
        if (alphaDragging) updateAlphaFromMouse(mouseX)
    }

    private fun updateSBFromMouse(mouseX: Int, mouseY: Int) {
        val bounds = sbBounds ?: return
        val sx = max(0f, min(bounds.width, mouseX - bounds.x))
        val sy = max(0f, min(bounds.height, mouseY - bounds.y))
        saturation = sx / bounds.width
        brightness = 1f - sy / bounds.height
        syncColorFromHSB()
    }

    private fun updateHueFromMouse(mouseY: Int) {
        val bounds = hueBounds ?: return
        val offset = max(0f, min(bounds.height, mouseY - bounds.y))
        hue = offset / bounds.height
        syncColorFromHSB()
    }

    private fun updateAlphaFromMouse(mouseX: Int) {
        val bounds = alphaBounds ?: return
        val offset = max(0f, min(bounds.width, mouseX - bounds.x))
        alpha = max(0, min(255, ((offset / bounds.width) * 255).toInt()))
        syncColorFromHSB()
    }

    private fun beginEditing(preset: CellGridPreset?) {
        if (preset == null) return
        editingPreset = preset
        applyPreset(preset)
    }

    private fun isEditing(preset: CellGridPreset?): Boolean =
        editingPreset != null && preset != null && editingPreset?.id == preset.id

    private fun toggleCell(row: Int, col: Int, enabled: Boolean) {
        setting.setCell(row, col, enabled, if (enabled) paintColor else null)
    }

    private fun applyPreset(preset: CellGridPreset) {
        val layout = normalizeLayout(preset.layoutCopy)
        val colors = normalizeColors(preset.colorCopy, layout)
        setting.setCells(layout)
        setting.setColorGrid(colors)
        activePresetId = preset.id
    }

    private fun savePreset() {
        val cellsCopy = setting.getCells()
        val colorsCopy = setting.getColorGrid()
        val nullableCells = toNullableLayout(cellsCopy)
        val nullableColors = toNullableColors(colorsCopy)

        val presets = CrosshairMod.layoutManager.customPresets
        if (editingPreset == null && presets.size >= LayoutManager.MAX_CUSTOM_PRESETS) {
            val removed = presets[0]
            CrosshairMod.layoutManager.removePreset(removed)
            if (removed?.id != null && removed.id == activePresetId) {
                activePresetId = null
            }
        }

        val saved =
            if (editingPreset != null) {
                CrosshairMod.layoutManager.addOrUpdatePreset(editingPreset!!.id, nullableCells, nullableColors, editingPreset!!.name)
            } else {
                CrosshairMod.layoutManager.addCustomPreset(null, nullableCells, nullableColors)
            }

        activePresetId = saved?.id ?: activePresetId
        editingPreset = null
    }

    private fun normalizeLayout(layout: Array<BooleanArray?>?): Array<BooleanArray>? {
        if (layout == null) {
            return null
        }
        val result = Array(GRID_SIZE) { BooleanArray(GRID_SIZE) }
        val rows = min(GRID_SIZE, layout.size)
        for (row in 0 until rows) {
            val rowData = layout[row] ?: continue
            val cols = min(GRID_SIZE, rowData.size)
            for (col in 0 until cols) {
                result[row][col] = rowData[col]
            }
        }
        return result
    }

    private fun normalizeColors(colors: Array<IntArray?>?, layout: Array<BooleanArray>?): Array<IntArray>? {
        if (layout == null) {
            return null
        }
        val result = Array(layout.size) { IntArray(layout[it].size) { Color.RED.rgb } }
        if (colors == null) {
            return result
        }
        val rows = min(layout.size, colors.size)
        for (row in 0 until rows) {
            val colorRow = colors[row] ?: continue
            val cols = min(layout[row].size, colorRow.size)
            for (col in 0 until cols) {
                result[row][col] = colorRow[col]
            }
        }
        return result
    }

    private fun toNullableLayout(layout: Array<BooleanArray>?): Array<BooleanArray?>? {
        if (layout == null) {
            return null
        }
        val result = arrayOfNulls<BooleanArray>(layout.size)
        for (i in layout.indices) {
            result[i] = layout[i]
        }
        return result
    }

    private fun toNullableColors(colors: Array<IntArray>?): Array<IntArray?>? {
        if (colors == null) {
            return null
        }
        val result = arrayOfNulls<IntArray>(colors.size)
        for (i in colors.indices) {
            result[i] = colors[i]
        }
        return result
    }

    private fun clearGrid() {
        setting.setCells(Array(GRID_SIZE) { BooleanArray(GRID_SIZE) })
        setting.fillColors(paintColor)
    }

    private fun syncHSB(color: Color?) {
        if (color == null) return
        val hsb = Color.RGBtoHSB(color.red, color.green, color.blue, null)
        hue = hsb[0]
        saturation = hsb[1]
        brightness = hsb[2]
        alpha = color.alpha
        paintColor = color
    }

    private fun syncColorFromHSB() {
        val rgb = Color.getHSBColor(hue, saturation, brightness)
        paintColor = Color(rgb.red, rgb.green, rgb.blue, alpha)
    }

    private fun computeGridMetrics(): GridMetrics {
        val padding = 8f
        val contentWidth = getWidth() - padding * 2f
        val gridBoxSize = min(190f, contentWidth * 0.52f)
        val cellSize = (gridBoxSize - GRID_PADDING * 2f) / GRID_SIZE
        val gridX = getX() + padding
        val gridY = getY() + padding
        val rightX = gridX + gridBoxSize + 12f
        val rightWidth = max(150f, contentWidth - gridBoxSize - 12f)
        return GridMetrics(padding, contentWidth, gridBoxSize, cellSize, gridX, gridY, rightX, rightWidth)
    }

    data class Bounds(
        val x: Float,
        val y: Float,
        val width: Float,
        val height: Float) {
        fun contains(mx: Int, my: Int): Boolean = mx >= x && mx <= x + width && my >= y && my <= y + height
    }

    data class PresetCard(
        val preset: CellGridPreset?,
        val bounds: Bounds,
        val deleteBounds: Bounds?,
        val editBounds: Bounds?,
        val addCard: Boolean)

    data class GridMetrics(
        val padding: Float,
        val contentWidth: Float,
        val gridBoxSize: Float,
        val cellSize: Float,
        val gridX: Float,
        val gridY: Float,
        val rightX: Float,
        val rightWidth: Float)

    companion object {
        private const val GRID_SIZE = 11
        private const val GRID_PADDING = 8f
        private const val PRESET_CARD_HEIGHT = 60f
        private const val BUTTON_HEIGHT = 22f
        private const val SWATCH_SIZE = 16f
        private const val PRESET_COLUMNS = 4
        private const val PRESET_GAP = 8f
        private const val MAX_SWATCHES = 6
        private val HUE_TEXTURE = ResourceLocation("shindo/hue.png")
        private val ALPHA_TEXTURE = ResourceLocation("shindo/alpha.png")
    }
}

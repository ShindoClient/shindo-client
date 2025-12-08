package me.miki.shindo.ui.comp.impl;

import lombok.Data;
import me.miki.shindo.management.color.AccentColor;
import me.miki.shindo.management.color.palette.ColorPalette;
import me.miki.shindo.management.color.palette.ColorType;
import me.miki.shindo.management.mods.impl.CrosshairMod;
import me.miki.shindo.management.mods.impl.crosshair.LayoutManager;
import me.miki.shindo.management.mods.impl.crosshair.LayoutManager.CellGridPreset;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.management.nanovg.font.Fonts;
import me.miki.shindo.management.nanovg.font.Icons;
import me.miki.shindo.management.settings.impl.CellGridSetting;
import me.miki.shindo.ui.comp.Comp;
import me.miki.shindo.ui.framework.UIContext;
import me.miki.shindo.utils.ColorUtils;
import me.miki.shindo.utils.mouse.MouseUtils;
import net.minecraft.util.ResourceLocation;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CompCellGrid extends Comp {

    private static final int GRID_SIZE = 11;
    private static final float GRID_PADDING = 8F;
    private static final float PRESET_CARD_HEIGHT = 60F;
    private static final float BUTTON_HEIGHT = 22F;
    private static final float SWATCH_SIZE = 16F;
    private static final int PRESET_COLUMNS = 4;
    private static final float PRESET_GAP = 8F;
    private static final int MAX_SWATCHES = 6;
    private static final ResourceLocation HUE_TEXTURE = new ResourceLocation("shindo/hue.png");
    private static final ResourceLocation ALPHA_TEXTURE = new ResourceLocation("shindo/alpha.png");

    private final CellGridSetting setting;
    private final List<PresetCard> presetCards = new ArrayList<>();
    private final List<Color> swatchColors = new ArrayList<>();
    private final List<Bounds> swatchBounds = new ArrayList<>();

    private Bounds saveButtonBounds;
    private Bounds clearButtonBounds;
    private Bounds colorToggleBounds;
    private Bounds hueBounds;
    private Bounds sbBounds;
    private Bounds alphaBounds;

    private CellGridPreset editingPreset;
    private String activePresetId;
    private Color paintColor = Color.WHITE;
    private float hue;
    private float saturation;
    private float brightness = 1F;
    private int alpha = 255;

    private boolean pickerOpen;
    private boolean hueDragging;
    private boolean sbDragging;
    private boolean alphaDragging;

    public CompCellGrid(float x, float y, int width, int height, CellGridSetting setting) {
        super(x, y);
        this.setting = setting;
        setWidth(width);
        setHeight(height);
        syncHSB(Color.RED);
    }

    public CompCellGrid(float width, float height, CellGridSetting setting) {
        super(0, 0);
        this.setting = setting;
        setWidth(width);
        setHeight(height);
        syncHSB(Color.RED);
    }

    @Override
    public void draw(int mouseX, int mouseY, float partialTicks) {
        UIContext ctx = ctx();
        NanoVGManager nvg = ctx.nvg();
        ColorPalette palette = ctx.palette();
        AccentColor accent = ctx.accent();

        GridMetrics metrics = computeGridMetrics();

        drawGrid(nvg, palette, mouseX, mouseY, metrics.gridX, metrics.gridY, metrics.gridBoxSize, metrics.cellSize);
        float colorBottom = drawColorControls(nvg, palette, accent, mouseX, mouseY, metrics.rightX, metrics.gridY, metrics.rightWidth);
        float buttonsBottom = drawButtons(nvg, palette, accent, mouseX, mouseY, metrics.rightX, colorBottom + 8F, metrics.rightWidth);

        float topBottom = Math.max(metrics.gridY + metrics.gridBoxSize, buttonsBottom);
        float presetsStartY = topBottom + 14F;
        float minPresetHeight = (PRESET_CARD_HEIGHT * 2F) + PRESET_GAP;
        float availableHeight = Math.max(minPresetHeight, getHeight() - (presetsStartY - getY()) - metrics.padding);
        drawPresets(nvg, palette, accent, mouseX, mouseY, getX() + metrics.padding, presetsStartY, metrics.contentWidth, availableHeight);

        updatePickerDrag(mouseX, mouseY);
        super.draw(mouseX, mouseY, partialTicks);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton != 0) {
            super.mouseClicked(mouseX, mouseY, mouseButton);
            return;
        }

        if (handlePickerClick(mouseX, mouseY)) {
            super.mouseClicked(mouseX, mouseY, mouseButton);
            return;
        }

        if (colorToggleBounds != null && colorToggleBounds.contains(mouseX, mouseY)) {
            pickerOpen = !pickerOpen;
            super.mouseClicked(mouseX, mouseY, mouseButton);
            return;
        }

        if (processSwatchClick(mouseX, mouseY)) {
            super.mouseClicked(mouseX, mouseY, mouseButton);
            return;
        }

        if (saveButtonBounds != null && saveButtonBounds.contains(mouseX, mouseY)) {
            savePreset();
            super.mouseClicked(mouseX, mouseY, mouseButton);
            return;
        }

        if (clearButtonBounds != null && clearButtonBounds.contains(mouseX, mouseY)) {
            clearGrid();
            super.mouseClicked(mouseX, mouseY, mouseButton);
            return;
        }

        if (processPresetClick(mouseX, mouseY)) {
            super.mouseClicked(mouseX, mouseY, mouseButton);
            return;
        }

        processGridClick(mouseX, mouseY);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        hueDragging = false;
        sbDragging = false;
        alphaDragging = false;
        super.mouseReleased(mouseX, mouseY, mouseButton);
    }

    private void drawGrid(NanoVGManager nvg, ColorPalette palette, int mouseX, int mouseY,
                          float x, float y, float boxSize, float cellSize) {
        Color outer = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 200);
        Color inner = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 210);
        nvg.drawRoundedRect(x, y, boxSize, boxSize, 7F, outer);
        nvg.drawRoundedRect(x + 1F, y + 1F, boxSize - 2F, boxSize - 2F, 6F, inner);

        boolean[][] cells = setting.getCells();
        Color gridLight = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 150);
        Color gridDark = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 170);

        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                float cx = x + GRID_PADDING + col * cellSize;
                float cy = y + GRID_PADDING + row * cellSize;
                boolean active = row < cells.length && cells[row] != null && col < cells[row].length && cells[row][col];
                Color cellBg = ((row + col) % 2 == 0) ? gridLight : gridDark;
                nvg.drawRect(cx, cy, cellSize, cellSize, cellBg);
                Color baseColor = active
                        ? setting.getCellColorOrDefault(row, col, paintColor)
                        : palette.getBackgroundColor(ColorType.MID);

                boolean hovered = MouseUtils.isInside(mouseX, mouseY, cx, cy, cellSize, cellSize);
                Color fill = active
                        ? ColorUtils.applyAlpha(baseColor, hovered ? 255 : Math.max(200, baseColor.getAlpha()))
                        : ColorUtils.applyAlpha(baseColor, hovered ? 140 : 90);

                if (active) {
                    nvg.drawRect(cx, cy, cellSize, cellSize, fill);
                }
                if (hovered) {
                    nvg.drawOutlineRoundedRect(cx, cy, cellSize, cellSize, 0F, 1.1F,
                            ColorUtils.applyAlpha(baseColor, 190));
                }
            }
        }
    }

    private float drawColorControls(NanoVGManager nvg, ColorPalette palette, AccentColor accent,
                                   int mouseX, int mouseY, float x, float y, float width) {
        swatchColors.clear();
        swatchBounds.clear();
        swatchColors.addAll(Arrays.asList(
                accent.getColor1(),
                accent.getColor2(),
                new Color(255, 255, 255),
                new Color(0, 0, 0),
                new Color(255, 96, 112),
                new Color(93, 126, 255)
        ));

        float labelY = y;
        nvg.drawText(TranslateText.COLOR.getText(), x, labelY, palette.getFontColor(ColorType.DARK), 9.5F, Fonts.MEDIUM);

        float swatchY = labelY + 12F;
        int perRow = 3;
        float rowSpacing = 6F;
        for (int i = 0; i < Math.min(MAX_SWATCHES, swatchColors.size()); i++) {
            int col = i % perRow;
            int row = i / perRow;
            float swatchX = x + col * (SWATCH_SIZE + 8F);
            float swatchRowY = swatchY + row * (SWATCH_SIZE + rowSpacing);

            Color swatch = swatchColors.get(i);
            boolean hovered = MouseUtils.isInside(mouseX, mouseY, swatchX, swatchRowY, SWATCH_SIZE, SWATCH_SIZE);
            nvg.drawRoundedRect(swatchX, swatchRowY, SWATCH_SIZE, SWATCH_SIZE, 3F,
                    ColorUtils.applyAlpha(swatch, hovered ? 255 : 230));
            nvg.drawOutlineRoundedRect(swatchX, swatchRowY, SWATCH_SIZE, SWATCH_SIZE, 3F, 1F,
                    ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), hovered ? 150 : 100));
            if (swatch.equals(paintColor)) {
                nvg.drawOutlineRoundedRect(swatchX - 1F, swatchRowY - 1F, SWATCH_SIZE + 2F, SWATCH_SIZE + 2F, 4F,
                        1.1F, ColorUtils.applyAlpha(palette.getFontColor(ColorType.DARK), 200));
            }

            swatchBounds.add(new Bounds(swatchX, swatchRowY, SWATCH_SIZE, SWATCH_SIZE));
        }

        float swatchBlockHeight = ((float) Math.ceil(Math.min(MAX_SWATCHES, swatchColors.size()) / (float) perRow) * (SWATCH_SIZE + rowSpacing)) - rowSpacing;
        float toggleY = swatchY + swatchBlockHeight + 6F;
        float toggleWidth = Math.max(90F, width * 0.55F);
        Color toggleBg = pickerOpen
                ? ColorUtils.applyAlpha(accent.getColor1(), 210)
                : palette.getBackgroundColor(ColorType.NORMAL);
        colorToggleBounds = new Bounds(x, toggleY, toggleWidth, 18F);
        String toggleLabel = pickerOpen
                ? TranslateText.CROSSHAIR_COLOR_PICKER_CLOSE.getText()
                : TranslateText.CROSSHAIR_COLOR_PICKER_OPEN.getText();
        nvg.drawRoundedRect(colorToggleBounds.x, colorToggleBounds.y, colorToggleBounds.width, colorToggleBounds.height, 5F, toggleBg);
        nvg.drawText(toggleLabel, colorToggleBounds.x + 6F,
                colorToggleBounds.y + 6F, palette.getFontColor(ColorType.DARK), 8.5F, Fonts.MEDIUM);

        // Current color chip
        float previewX = colorToggleBounds.x + colorToggleBounds.width + 8F;
        float previewSize = 18F;
        if (previewX + previewSize <= x + width) {
            nvg.drawRoundedRect(previewX, colorToggleBounds.y, previewSize, previewSize, 4F, paintColor);
            nvg.drawOutlineRoundedRect(previewX, colorToggleBounds.y, previewSize, previewSize, 4F, 1F,
                    ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 140));
        }

        if (pickerOpen) {
            float pickerY = colorToggleBounds.y + colorToggleBounds.height + 6F;
            float maxPickerWidth = Math.max(80F, Math.min(width, getX() + getWidth() - x - 6F));
            drawColorPicker(nvg, palette, accent, mouseX, mouseY, x, pickerY, maxPickerWidth);
        } else {
            hueBounds = null;
            sbBounds = null;
            alphaBounds = null;
        }
        float bottom = pickerOpen && alphaBounds != null
                ? alphaBounds.y + alphaBounds.height
                : colorToggleBounds.y + colorToggleBounds.height;
        return bottom;
    }

    private void drawColorPicker(NanoVGManager nvg, ColorPalette palette, AccentColor accent,
                                 int mouseX, int mouseY, float x, float y, float size) {
        float maxWidth = Math.max(80F, getX() + getWidth() - x - 6F);
        float sbSize = Math.min(size, maxWidth);
        float hueWidth = 12F;
        float padding = 6F;

        float maxHeight = (getY() + getHeight()) - y - 6F;
        float requiredHeight = sbSize + padding + 12F;
        if (requiredHeight > maxHeight) {
            sbSize = Math.max(60F, maxHeight - padding - 12F);
        }

        Color hueColor = Color.getHSBColor(hue, 1F, 1F);

        sbBounds = new Bounds(x, y, sbSize, sbSize);
        hueBounds = new Bounds(x + sbSize + padding, y, hueWidth, sbSize);
        alphaBounds = new Bounds(x, y + sbSize + padding, sbSize + hueWidth + padding, 12F);

        nvg.drawHSBBox(sbBounds.x, sbBounds.y, sbBounds.width, sbBounds.height, 6F, hueColor);
        float satX = sbBounds.x + saturation * sbBounds.width;
        float brightY = sbBounds.y + sbBounds.height - (brightness * sbBounds.height);
        nvg.drawArc(satX, brightY, 3.2F, 0, 360, 1.2F, Color.WHITE);

        nvg.drawRoundedImage(HUE_TEXTURE, hueBounds.x, hueBounds.y, hueWidth, sbSize, 3F);
        float hueY = hueBounds.y + (sbSize * hue);
        nvg.drawArc(hueBounds.x + hueWidth / 2F, hueY, 3.2F, 0, 360, 1.1F, Color.WHITE);

        Color alphaColor = new Color(paintColor.getRed(), paintColor.getGreen(), paintColor.getBlue(), 255);
        nvg.drawRoundedImage(ALPHA_TEXTURE, alphaBounds.x, alphaBounds.y, alphaBounds.width, alphaBounds.height, 3F);
        nvg.drawAlphaBar(alphaBounds.x, alphaBounds.y, alphaBounds.width, alphaBounds.height, 3F, alphaColor);
        float alphaX = alphaBounds.x + (alphaBounds.width * (alpha / 255F));
        nvg.drawArc(alphaX, alphaBounds.y + (alphaBounds.height / 2F), 3.2F, 0, 360, 1.1F, Color.WHITE);
    }

    private float drawButtons(NanoVGManager nvg, ColorPalette palette, AccentColor accent,
                             int mouseX, int mouseY, float x, float y, float width) {
        float spacing = 8F;
        float primaryWidth = Math.max(90F, Math.min(width * 0.6F, width - spacing - 70F));
        float secondaryWidth = width - primaryWidth - spacing;
        if (secondaryWidth < 64F) {
            secondaryWidth = 64F;
            primaryWidth = Math.max(70F, width - spacing - secondaryWidth);
        }

        saveButtonBounds = drawButton(nvg, palette, accent, x, y, primaryWidth, BUTTON_HEIGHT,
                TranslateText.CROSSHAIR_SAVE_PRESET.getText(), true,
                MouseUtils.isInside(mouseX, mouseY, x, y, primaryWidth, BUTTON_HEIGHT));
        clearButtonBounds = drawButton(nvg, palette, accent, x + primaryWidth + 10F, y, secondaryWidth, BUTTON_HEIGHT,
                TranslateText.CROSSHAIR_CLEAR_GRID.getText(), false,
                MouseUtils.isInside(mouseX, mouseY, x + primaryWidth + 10F, y, secondaryWidth, BUTTON_HEIGHT));
        return y + BUTTON_HEIGHT;
    }

    private void drawPresets(NanoVGManager nvg, ColorPalette palette, AccentColor accent,
                             int mouseX, int mouseY, float x, float y, float width, float availableHeight) {
        presetCards.clear();
        List<CellGridPreset> presets = CrosshairMod.layoutManager.getCustomPresets();
        int maxSlots = LayoutManager.MAX_CUSTOM_PRESETS;

        float cardWidth = (width - (PRESET_GAP * (PRESET_COLUMNS - 1))) / PRESET_COLUMNS;
        float cardHeight = Math.min(PRESET_CARD_HEIGHT, (availableHeight - PRESET_GAP) / 2F);
        cardHeight = Math.max(48F, cardHeight);

        boolean addCardPlaced = false;

        for (int index = 0; index < maxSlots; index++) {
            int row = index / PRESET_COLUMNS;
            int col = index % PRESET_COLUMNS;
            float cardX = x + col * (cardWidth + PRESET_GAP);
            float cardY = y + row * (cardHeight + PRESET_GAP);
            Bounds cardBounds = new Bounds(cardX, cardY, cardWidth, cardHeight);

            boolean hasPreset = index < presets.size();
            boolean isAddCard = !hasPreset && !addCardPlaced;

            if (hasPreset) {
                CellGridPreset preset = presets.get(index);
                boolean hovered = cardBounds.contains(mouseX, mouseY);
                boolean active = activePresetId != null && activePresetId.equals(preset.getId());
                boolean editing = isEditing(preset);

                Color bg = hovered
                        ? ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 200)
                        : palette.getBackgroundColor(ColorType.NORMAL);
                if (active) {
                    bg = ColorUtils.applyAlpha(accent.getColor1(), hovered ? 200 : 170);
                }

                nvg.drawRoundedRect(cardBounds.x, cardBounds.y, cardBounds.width, cardBounds.height, 6F, bg);
                if (active || hovered || editing) {
                    nvg.drawOutlineRoundedRect(cardBounds.x, cardBounds.y, cardBounds.width, cardBounds.height, 6F, 1.3F,
                            ColorUtils.applyAlpha(accent.getColor2(), active ? 200 : 150));
                }

                float previewSize = Math.min(cardBounds.width - 16F, cardBounds.height - 16F);
                float previewX = cardBounds.x + (cardBounds.width - previewSize) / 2F;
                float previewY = cardBounds.y + (cardBounds.height - previewSize) / 2F;
                drawPresetPreview(nvg, preset, previewX, previewY, previewSize,
                        ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 100),
                        ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 120));

                Bounds deleteBounds = null;
                Bounds editBounds = null;
                if (hovered) {
                    float iconSize = 12F;
                    float iconY = cardBounds.y + 6F;
                    editBounds = new Bounds(cardBounds.x + cardBounds.width - (iconSize * 2F) - 8F, iconY, iconSize, iconSize);
                    deleteBounds = new Bounds(cardBounds.x + cardBounds.width - iconSize - 4F, iconY, iconSize, iconSize);

                    nvg.drawText(Icons.EDIT_20, editBounds.x, editBounds.y,
                            palette.getFontColor(ColorType.DARK), 11F, Fonts.ICON_OUTLINE);
                    nvg.drawText(Icons.DELETE_20, deleteBounds.x, deleteBounds.y,
                            ColorUtils.applyAlpha(palette.getFontColor(ColorType.DARK), 220), 11F, Fonts.ICON_OUTLINE);
                }

                presetCards.add(new PresetCard(preset, cardBounds, deleteBounds, editBounds, false));
            } else if (isAddCard) {
                boolean hovered = cardBounds.contains(mouseX, mouseY);
                Color bg = hovered
                        ? ColorUtils.applyAlpha(accent.getColor1(), 200)
                        : palette.getBackgroundColor(ColorType.NORMAL);
                nvg.drawRoundedRect(cardBounds.x, cardBounds.y, cardBounds.width, cardBounds.height, 6F, bg);
                nvg.drawOutlineRoundedRect(cardBounds.x, cardBounds.y, cardBounds.width, cardBounds.height, 6F, 1.2F,
                        ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 140));

                nvg.drawText(Icons.ADD_CIRCLE_24,
                        cardBounds.x + (cardBounds.width / 2F) - 6F,
                        cardBounds.y + (cardBounds.height / 2F) - 7F,
                        palette.getFontColor(ColorType.DARK), 14F, Fonts.ICON_OUTLINE);
                presetCards.add(new PresetCard(null, cardBounds, null, null, true));
                addCardPlaced = true;
            }
        }
    }

    private void drawPresetPreview(NanoVGManager nvg, CellGridPreset preset, float x, float y, float size,
                                   Color evenBg, Color oddBg) {
        boolean[][] layout = preset.getLayoutCopy();
        int[][] colors = preset.getColorCopy();
        float cell = size / GRID_SIZE;
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                float cx = x + col * cell;
                float cy = y + row * cell;
                Color bg = ((row + col) % 2 == 0) ? evenBg : oddBg;
                nvg.drawRect(cx, cy, cell, cell, bg);

                boolean enabled = row < layout.length && layout[row] != null && col < layout[row].length && layout[row][col];
                if (!enabled) {
                    continue;
                }
                int rgb = colors != null && row < colors.length && colors[row] != null && col < colors[row].length
                        ? colors[row][col]
                        : Color.WHITE.getRGB();
                nvg.drawRect(cx, cy, cell, cell, new Color(rgb, true));
            }
        }
    }

    private Bounds drawButton(NanoVGManager nvg, ColorPalette palette, AccentColor accent,
                              float x, float y, float width, float height, String label,
                              boolean primary, boolean hovered) {
        Color bg = primary
                ? ColorUtils.applyAlpha(accent.getColor1(), hovered ? 220 : 190)
                : palette.getBackgroundColor(ColorType.NORMAL);
        Color textColor = primary ? palette.getFontColor(ColorType.DARK) : palette.getFontColor(ColorType.NORMAL);
        nvg.drawRoundedRect(x, y, width, height, 6F, bg);
        nvg.drawText(label, x + 8F, y + height / 2F - 3F, textColor, 9F, Fonts.MEDIUM);
        return new Bounds(x, y, width, height);
    }

    private void processGridClick(int mouseX, int mouseY) {
        GridMetrics metrics = computeGridMetrics();
        float gridBoxSize = metrics.gridBoxSize;
        float cellSize = metrics.cellSize;
        float gridX = metrics.gridX;
        float gridY = metrics.gridY;
        boolean[][] cells = setting.getCells();

        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                float cx = gridX + GRID_PADDING + col * cellSize;
                float cy = gridY + GRID_PADDING + row * cellSize;
                if (MouseUtils.isInside(mouseX, mouseY, cx, cy, cellSize, cellSize)) {
                    if (row >= cells.length || cells[row] == null || col >= cells[row].length) {
                        return;
                    }
                    boolean current = cells[row][col];
                    toggleCell(row, col, !current);
                    return;
                }
            }
        }
    }

    private boolean processPresetClick(int mouseX, int mouseY) {
        for (PresetCard card : presetCards) {
            if (card == null) {
                continue;
            }
            if (card.deleteBounds != null && card.deleteBounds.contains(mouseX, mouseY) && card.preset != null) {
                if (isEditing(card.preset)) {
                    editingPreset = null;
                }
                if (activePresetId != null && activePresetId.equals(card.preset.getId())) {
                    activePresetId = null;
                }
                CrosshairMod.layoutManager.removePreset(card.preset);
                return true;
            }
            if (card.editBounds != null && card.editBounds.contains(mouseX, mouseY) && card.preset != null) {
                beginEditing(card.preset);
                return true;
            }
            if (card.bounds.contains(mouseX, mouseY)) {
                if (card.addCard) {
                    editingPreset = null;
                    savePreset();
                    return true;
                }
                if (card.preset != null) {
                    applyPreset(card.preset);
                    editingPreset = null;
                    return true;
                }
            }
        }
        return false;
    }

    private boolean processSwatchClick(int mouseX, int mouseY) {
        for (int i = 0; i < swatchBounds.size() && i < swatchColors.size(); i++) {
            Bounds b = swatchBounds.get(i);
            if (b.contains(mouseX, mouseY)) {
                Color swatch = swatchColors.get(i);
                syncHSB(swatch);
                paintColor = swatch;
                return true;
            }
        }
        return false;
    }

    private boolean handlePickerClick(int mouseX, int mouseY) {
        if (!pickerOpen) {
            return false;
        }
        if (sbBounds != null && sbBounds.contains(mouseX, mouseY)) {
            sbDragging = true;
            updateSBFromMouse(mouseX, mouseY);
            return true;
        }
        if (hueBounds != null && hueBounds.contains(mouseX, mouseY)) {
            hueDragging = true;
            updateHueFromMouse(mouseY);
            return true;
        }
        if (alphaBounds != null && alphaBounds.contains(mouseX, mouseY)) {
            alphaDragging = true;
            updateAlphaFromMouse(mouseX);
            return true;
        }
        return false;
    }

    private void updatePickerDrag(int mouseX, int mouseY) {
        if (sbDragging) {
            updateSBFromMouse(mouseX, mouseY);
        }
        if (hueDragging) {
            updateHueFromMouse(mouseY);
        }
        if (alphaDragging) {
            updateAlphaFromMouse(mouseX);
        }
    }

    private void updateSBFromMouse(int mouseX, int mouseY) {
        if (sbBounds == null) {
            return;
        }
        float sx = Math.max(0F, Math.min(sbBounds.width, mouseX - sbBounds.x));
        float sy = Math.max(0F, Math.min(sbBounds.height, mouseY - sbBounds.y));
        saturation = sx / sbBounds.width;
        brightness = 1F - (sy / sbBounds.height);
        syncColorFromHSB();
    }

    private void updateHueFromMouse(int mouseY) {
        if (hueBounds == null) {
            return;
        }
        float offset = Math.max(0F, Math.min(hueBounds.height, mouseY - hueBounds.y));
        hue = offset / hueBounds.height;
        syncColorFromHSB();
    }

    private void updateAlphaFromMouse(int mouseX) {
        if (alphaBounds == null) {
            return;
        }
        float offset = Math.max(0F, Math.min(alphaBounds.width, mouseX - alphaBounds.x));
        alpha = Math.max(0, Math.min(255, Math.round((offset / alphaBounds.width) * 255)));
        syncColorFromHSB();
    }

    private void beginEditing(CellGridPreset preset) {
        if (preset == null) {
            return;
        }
        editingPreset = preset;
        applyPreset(preset);
    }

    private boolean isEditing(CellGridPreset preset) {
        return editingPreset != null && preset != null && editingPreset.getId().equals(preset.getId());
    }

    private void toggleCell(int row, int col, boolean enabled) {
        setting.setCell(row, col, enabled, enabled ? paintColor : null);
    }

    private void applyPreset(CellGridPreset preset) {
        setting.setCells(preset.getLayoutCopy());
        setting.setColorGrid(preset.getColorCopy());
        if (preset != null) {
            activePresetId = preset.getId();
        }
    }

    private void savePreset() {
        boolean[][] cellsCopy = setting.getCells();
        int[][] colorsCopy = setting.getColorGrid();

        List<CellGridPreset> presets = CrosshairMod.layoutManager.getCustomPresets();
        if (editingPreset == null && presets.size() >= LayoutManager.MAX_CUSTOM_PRESETS) {
            CellGridPreset removed = presets.get(0);
            CrosshairMod.layoutManager.removePreset(removed);
            if (removed != null && removed.getId() != null && removed.getId().equals(activePresetId)) {
                activePresetId = null;
            }
        }

        CellGridPreset saved = editingPreset != null
                ? CrosshairMod.layoutManager.addOrUpdatePreset(editingPreset.getId(), cellsCopy, colorsCopy, editingPreset.getName())
                : CrosshairMod.layoutManager.addCustomPreset(null, cellsCopy, colorsCopy);

        activePresetId = saved != null ? saved.getId() : activePresetId;
        editingPreset = null;
    }

    private void clearGrid() {
        setting.setCells(new boolean[GRID_SIZE][GRID_SIZE]);
        setting.fillColors(paintColor);
    }

    private void syncHSB(Color color) {
        if (color == null) {
            return;
        }
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        this.hue = hsb[0];
        this.saturation = hsb[1];
        this.brightness = hsb[2];
        this.alpha = color.getAlpha();
        this.paintColor = color;
    }

    private void syncColorFromHSB() {
        Color rgb = Color.getHSBColor(hue, saturation, brightness);
        paintColor = new Color(rgb.getRed(), rgb.getGreen(), rgb.getBlue(), alpha);
    }

    private boolean gridsEqual(boolean[][] a, boolean[][] b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null || a.length != b.length) {
            return false;
        }
        for (int i = 0; i < a.length; i++) {
            boolean[] ar = a[i];
            boolean[] br = b[i];
            if (ar == null || br == null || ar.length != br.length) {
                return false;
            }
            for (int j = 0; j < ar.length; j++) {
                if (ar[j] != br[j]) {
                    return false;
                }
            }
        }
        return true;
    }

    @Data
    private static class Bounds {
        final float x;
        final float y;
        final float width;
        final float height;

        boolean contains(int mx, int my) {
            return mx >= x && mx <= x + width && my >= y && my <= y + height;
        }
    }

    @Data
    private static class PresetCard {
        final CellGridPreset preset;
        final Bounds bounds;
        final Bounds deleteBounds;
        final Bounds editBounds;
        final boolean addCard;
    }

    private GridMetrics computeGridMetrics() {
        float padding = 8F;
        float contentWidth = getWidth() - (padding * 2F);
        float gridBoxSize = Math.min(190F, contentWidth * 0.52F);
        float cellSize = (gridBoxSize - (GRID_PADDING * 2F)) / GRID_SIZE;
        float gridX = getX() + padding;
        float gridY = getY() + padding;
        float rightX = gridX + gridBoxSize + 12F;
        float rightWidth = Math.max(150F, contentWidth - gridBoxSize - 12F);
        return new GridMetrics(padding, contentWidth, gridBoxSize, cellSize, gridX, gridY, rightX, rightWidth);
    }

    @Data
    private static class GridMetrics {
        final float padding;
        final float contentWidth;
        final float gridBoxSize;
        final float cellSize;
        final float gridX;
        final float gridY;
        final float rightX;
        final float rightWidth;
    }
}

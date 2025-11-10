package me.miki.shindo.gui.modmenu.category.impl;

import eu.shoroa.contrib.cosmetic.Cosmetic;
import eu.shoroa.contrib.cosmetic.CosmeticManager;
import eu.shoroa.contrib.gui.CompCosmetic;
import me.miki.shindo.Shindo;
import me.miki.shindo.api.roles.Role;
import me.miki.shindo.gui.modmenu.GuiModMenu;
import me.miki.shindo.gui.modmenu.category.Category;
import me.miki.shindo.management.color.AccentColor;
import me.miki.shindo.management.color.palette.ColorPalette;
import me.miki.shindo.management.color.palette.ColorType;
import me.miki.shindo.management.cosmetic.bandanna.BandannaCategory;
import me.miki.shindo.management.cosmetic.bandanna.BandannaManager;
import me.miki.shindo.management.cosmetic.bandanna.impl.Bandanna;
import me.miki.shindo.management.cosmetic.bandanna.impl.NormalBandanna;
import me.miki.shindo.management.cosmetic.cape.CapeCategory;
import me.miki.shindo.management.cosmetic.cape.CapeManager;
import me.miki.shindo.management.cosmetic.cape.impl.Cape;
import me.miki.shindo.management.cosmetic.cape.impl.CustomCape;
import me.miki.shindo.management.cosmetic.cape.impl.NormalCape;
import me.miki.shindo.management.cosmetic.wing.WingCategory;
import me.miki.shindo.management.cosmetic.wing.WingManager;
import me.miki.shindo.management.cosmetic.wing.impl.NormalWing;
import me.miki.shindo.management.cosmetic.wing.impl.Wing;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.management.nanovg.font.Fonts;
import me.miki.shindo.management.nanovg.font.LegacyIcon;
import me.miki.shindo.management.notification.NotificationType;
import me.miki.shindo.utils.ColorUtils;
import me.miki.shindo.utils.SearchUtils;
import me.miki.shindo.utils.mouse.MouseUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public class CosmeticsCategory extends Category {

    private static final float CONTENT_PADDING = 18F;
    private static final float SECTION_SPACING = 32F;
    private static final float TITLE_HEIGHT = 20F;
    private static final float DESCRIPTION_GAP = 8F;
    private static final float CARD_WIDTH = 96F;
    private static final float CARD_HEIGHT = 136F;
    private static final float CARD_GAP = 14F;
    private static final float CHIP_HEIGHT = 18F;
    private static final float CHIP_HORIZONTAL_PADDING = 12F;
    private static final float CHIP_GAP = 8F;
    private static final float TAB_HEIGHT = 22F;

    private enum CosmeticSection {
        CAPES(TranslateText.CAPES, TranslateText.CAPES_DESCRIPTION, LegacyIcon.STAR),
        WINGS(TranslateText.COSMETICS_WINGS, TranslateText.COSMETICS_WINGS_DESCRIPTION, LegacyIcon.HOVER),
        BANDANNAS(TranslateText.COSMETICS_BANDANNAS, TranslateText.COSMETICS_BANDANNAS_DESCRIPTION, LegacyIcon.SHIELD),
        OTHERS(TranslateText.COSMETICS_OTHER, TranslateText.COSMETICS_OTHER_DESCRIPTION, LegacyIcon.CAT);

        private final TranslateText title;
        private final TranslateText description;
        private final String icon;

        CosmeticSection(TranslateText title, TranslateText description, String icon) {
            this.title = title;
            this.description = description;
            this.icon = icon;
        }

        public String getTitle() {
            return title.getText();
        }

        public String getDescription() {
            return description.getText();
        }

        public String getIcon() {
            return icon;
        }
    }

    private final Minecraft mc = Minecraft.getMinecraft();

    private final Map<Cape, CardBounds> capeCardBounds = new IdentityHashMap<>();
    private final Map<Wing, CardBounds> wingCardBounds = new IdentityHashMap<>();
    private final Map<Bandanna, CardBounds> bandannaCardBounds = new IdentityHashMap<>();
    private final Map<Cosmetic, CardBounds> miscCardBounds = new IdentityHashMap<>();
    private final Map<Cosmetic, CompCosmetic> miscComponents = new IdentityHashMap<>();
    private final List<FilterChip> activeChips = new ArrayList<>();
    private final List<FilterChip> sectionChips = new ArrayList<>();

    private CosmeticSection activeSection = CosmeticSection.CAPES;
    private CapeCategory activeCapeCategory = CapeCategory.ALL;
    private WingCategory activeWingCategory = WingCategory.ALL;
    private BandannaCategory activeBandannaCategory = BandannaCategory.ALL;

    public boolean shouldShowCustomCapeFolder() {
        return activeSection == CosmeticSection.CAPES && activeCapeCategory == CapeCategory.CUSTOM;
    }

    public CosmeticsCategory(GuiModMenu parent) {
        super(parent, TranslateText.COSMETICS, LegacyIcon.SHOPPING, true, true);
    }

    @Override
    public void initGui() {
        rebuildCosmeticComponents();
        scroll.resetAll();
        activeSection = CosmeticSection.CAPES;
    }

    @Override
    public void initCategory() {
        rebuildCosmeticComponents();
        scroll.resetAll();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        Shindo instance = Shindo.getInstance();
        NanoVGManager nvg = instance.getNanoVGManager();
        ColorPalette palette = instance.getColorManager().getPalette();
        AccentColor accent = instance.getColorManager().getCurrentColor();

        float viewportX = getX();
        float viewportY = getY();
        float viewportWidth = getWidth();
        float viewportHeight = getHeight();

        sectionChips.clear();
        activeChips.clear();

        float tabHeight = drawSectionTabs(nvg, palette, accent, viewportX, viewportY, viewportWidth);
        float contentTop = viewportY + tabHeight + 12F;
        float contentHeight = Math.max(0F, viewportHeight - (contentTop - viewportY));
        if (contentHeight <= 0F) {
            scroll.setMaxScroll(0F);
            return;
        }

        String searchQuery = getSearchBox().getText() == null ? "" : getSearchBox().getText().trim();

        if (MouseUtils.isInside(mouseX, mouseY, viewportX, contentTop, viewportWidth, contentHeight)) {
            scroll.onScroll();
        }
        scroll.onAnimation();
        float scrollOffset = scroll.getValue();

        float contentX = viewportX + CONTENT_PADDING;
        float contentWidth = viewportWidth - (CONTENT_PADDING * 2F);
        float startY = contentTop + CONTENT_PADDING;
        float endY = startY;

        nvg.save();
        nvg.scissor(viewportX, contentTop, viewportWidth, contentHeight);
        nvg.translate(0, scrollOffset);

        switch (activeSection) {
            case CAPES:
                capeCardBounds.clear();
                endY = drawCapeSection(nvg, palette, accent, contentX, contentWidth, startY, scrollOffset, searchQuery, mouseX, mouseY);
                break;
            case WINGS:
                wingCardBounds.clear();
                endY = drawWingSection(nvg, palette, accent, contentX, contentWidth, startY, scrollOffset, searchQuery, mouseX, mouseY);
                break;
            case BANDANNAS:
                bandannaCardBounds.clear();
                endY = drawBandannaSection(nvg, palette, accent, contentX, contentWidth, startY, scrollOffset, searchQuery, mouseX, mouseY);
                break;
            case OTHERS:
                miscCardBounds.clear();
                endY = drawMiscSection(nvg, palette, accent, contentX, contentWidth, startY, scrollOffset, searchQuery, mouseX, mouseY, partialTicks);
                break;
        }

        nvg.restore();

        float logicalHeight = Math.max(0F, endY - startY + CONTENT_PADDING);
        scroll.setMaxScroll(Math.max(0F, logicalHeight - contentHeight));
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {

        if (handleSectionChipClick(mouseX, mouseY, mouseButton)) {
            return;
        }

        switch (activeSection) {
            case CAPES:
                if (handleChipClick(mouseX, mouseY, mouseButton)) {
                    return;
                }
                if (handleCapeClick(mouseX, mouseY, mouseButton)) {
                    return;
                }
                break;
            case WINGS:
                if (handleChipClick(mouseX, mouseY, mouseButton)) {
                    return;
                }
                if (handleWingClick(mouseX, mouseY, mouseButton)) {
                    return;
                }
                break;
            case BANDANNAS:
                if (handleChipClick(mouseX, mouseY, mouseButton)) {
                    return;
                }
                if (handleBandannaClick(mouseX, mouseY, mouseButton)) {
                    return;
                }
                break;
            case OTHERS:
                handleMiscClick(mouseX, mouseY, mouseButton);
                break;
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        scroll.onKey(keyCode);
    }

    private float drawSectionTabs(NanoVGManager nvg,
                                  ColorPalette palette,
                                  AccentColor accent,
                                  float viewportX,
                                  float viewportY,
                                  float viewportWidth) {

        float maxX = viewportX + viewportWidth - CONTENT_PADDING;
        float currentX = viewportX + CONTENT_PADDING;
        float currentY = viewportY + 6F;

        for (CosmeticSection section : CosmeticSection.values()) {

            String label = section.getTitle();
            float textWidth = nvg.getTextWidth(label, 9.5F, Fonts.MEDIUM);
            float iconWidth = 0F;
            String icon = section.getIcon();
            if (icon != null && !icon.isEmpty()) {
                iconWidth = nvg.getTextWidth(icon, 12F, Fonts.LEGACYICON) + 6F;
            }

            float chipWidth = CHIP_HORIZONTAL_PADDING * 2F + iconWidth + textWidth;
            if (currentX + chipWidth > maxX) {
                currentX = viewportX + CONTENT_PADDING;
                currentY += TAB_HEIGHT + CHIP_GAP;
            }

            boolean active = section == activeSection;
            Color base = palette.getBackgroundColor(ColorType.DARK);

            if (active) {
                Color start = ColorUtils.applyAlpha(accent.getColor1(), 200);
                Color end = ColorUtils.applyAlpha(accent.getColor2(), 200);
                nvg.drawGradientRoundedRect(currentX, currentY, chipWidth, TAB_HEIGHT, TAB_HEIGHT / 2F, start, end);
            } else {
                nvg.drawRoundedRect(currentX, currentY, chipWidth, TAB_HEIGHT, TAB_HEIGHT / 2F, base);
            }

            float textX = currentX + CHIP_HORIZONTAL_PADDING;
            float textY = currentY + TAB_HEIGHT / 2F - 1F;
            Color textColor = active ? Color.WHITE : palette.getFontColor(ColorType.NORMAL);

            if (iconWidth > 0F) {
                nvg.drawText(icon, textX, textY - 6F, textColor, 12F, Fonts.LEGACYICON);
                textX += iconWidth;
            }

            nvg.drawText(label, textX, textY, textColor, 9.5F, Fonts.MEDIUM);

            sectionChips.add(new FilterChip(currentX, currentY, chipWidth, TAB_HEIGHT, () -> {
                if (activeSection != section) {
                    activeSection = section;
                    scroll.resetAll();
                }
            }));

            currentX += chipWidth + CHIP_GAP;
        }

        return (currentY + TAB_HEIGHT) - viewportY;
    }

    private boolean handleSectionChipClick(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton != 0) {
            return false;
        }
        for (FilterChip chip : sectionChips) {
            if (chip.contains(mouseX, mouseY)) {
                chip.onClick();
                return true;
            }
        }
        return false;
    }

    private void rebuildCosmeticComponents() {
        miscComponents.clear();
        miscCardBounds.clear();
        CosmeticManager.getInstance().getCosmetics().forEach(cosmetic -> miscComponents.put(cosmetic, new CompCosmetic(cosmetic)));
    }

    private float drawCapeSection(NanoVGManager nvg, ColorPalette palette, AccentColor accent, float x, float width, float startY, float scrollOffset, String searchQuery, int mouseX, int mouseY) {

        CapeManager capeManager = Shindo.getInstance().getCapeManager();

        float y = startY;
        y += drawSectionHeader(nvg, palette, CosmeticSection.CAPES.getTitle(), CosmeticSection.CAPES.getDescription(), x, y);
        y += drawCategoryChips(nvg, palette, accent, x, width, y, CapeCategory.values(), activeCapeCategory,
                category -> setActiveCapeCategory((CapeCategory) category), scrollOffset);

        List<Cape> filtered = new ArrayList<>();
        for (Cape cape : capeManager.getCapes()) {
            if (!isCapeVisible(cape, searchQuery)) {
                continue;
            }
            filtered.add(cape);
        }

        if (filtered.isEmpty()) {
            y += drawEmptyMessage(nvg, palette, x, y);
            return y;
        }

        y += CARD_GAP;
        int columns = Math.max(1, (int) ((width + CARD_GAP) / (CARD_WIDTH + CARD_GAP)));
        int rows = (filtered.size() + columns - 1) / columns;

        for (int index = 0; index < filtered.size(); index++) {
            Cape cape = filtered.get(index);
            int column = index % columns;
            int row = index / columns;

            float cardX = x + column * (CARD_WIDTH + CARD_GAP);
            float cardY = y + row * (CARD_HEIGHT + CARD_GAP);

            boolean selected = cape.equals(capeManager.getCurrentCape());
            boolean unlocked = capeManager.canUseCape(getClientUuid(), cape);
            SimpleCardState state = SimpleCardState.of(selected, unlocked);

            PreviewRenderer preview = createCapePreview(cape);
            drawCard(nvg, palette, accent, cardX, cardY, state, preview, cape.getName(), formatRequirement(cape.getRequiredRole(), capeManager::getTranslateText), mouseX, mouseY, scrollOffset);
            capeCardBounds.computeIfAbsent(cape, key -> new CardBounds()).set(cardX, cardY + scrollOffset, CARD_WIDTH, CARD_HEIGHT);
        }

        float gridHeight = rows * CARD_HEIGHT + Math.max(0, rows - 1) * CARD_GAP;
        return y + gridHeight + CARD_GAP;
    }

    private float drawWingSection(NanoVGManager nvg, ColorPalette palette, AccentColor accent, float x, float width, float startY, float scrollOffset, String searchQuery, int mouseX, int mouseY) {

        WingManager wingManager = Shindo.getInstance().getWingManager();
        float y = startY;
        y += drawSectionHeader(nvg, palette, CosmeticSection.WINGS.getTitle(), CosmeticSection.WINGS.getDescription(), x, y);
        y += drawCategoryChips(nvg, palette, accent, x, width, y, WingCategory.values(), activeWingCategory,
                category -> setActiveWingCategory((WingCategory) category), scrollOffset);

        List<Wing> filtered = new ArrayList<>();
        for (Wing wing : wingManager.getWings()) {
            if (!isWingVisible(wing, searchQuery)) {
                continue;
            }
            filtered.add(wing);
        }

        if (filtered.isEmpty()) {
            y += drawEmptyMessage(nvg, palette, x, y);
            return y;
        }

        y += CARD_GAP;
        int columns = Math.max(1, (int) ((width + CARD_GAP) / (CARD_WIDTH + CARD_GAP)));
        int rows = (filtered.size() + columns - 1) / columns;

        for (int index = 0; index < filtered.size(); index++) {
            Wing wing = filtered.get(index);
            int column = index % columns;
            int row = index / columns;
            float cardX = x + column * (CARD_WIDTH + CARD_GAP);
            float cardY = y + row * (CARD_HEIGHT + CARD_GAP);

            boolean selected = wing.equals(wingManager.getCurrentWing());
            boolean unlocked = wingManager.canUseWing(getClientUuid(), wing);
            SimpleCardState state = SimpleCardState.of(selected, unlocked);

            PreviewRenderer preview = createWingPreview(wing);
            drawCard(nvg, palette, accent, cardX, cardY, state, preview, wing.getName(), formatRequirement(wing.getRequiredRole(), wingManager::getTranslateText), mouseX, mouseY, scrollOffset);
            wingCardBounds.computeIfAbsent(wing, key -> new CardBounds()).set(cardX, cardY + scrollOffset, CARD_WIDTH, CARD_HEIGHT);
        }

        float gridHeight = rows * CARD_HEIGHT + Math.max(0, rows - 1) * CARD_GAP;
        return y + gridHeight + CARD_GAP;
    }

    private float drawBandannaSection(NanoVGManager nvg, ColorPalette palette, AccentColor accent, float x, float width, float startY, float scrollOffset, String searchQuery, int mouseX, int mouseY) {

        BandannaManager bandannaManager = Shindo.getInstance().getBandannaManager();
        float y = startY;
        y += drawSectionHeader(nvg, palette, CosmeticSection.BANDANNAS.getTitle(), CosmeticSection.BANDANNAS.getDescription(), x, y);
        y += drawCategoryChips(nvg, palette, accent, x, width, y, BandannaCategory.values(), activeBandannaCategory,
                category -> setActiveBandannaCategory((BandannaCategory) category), scrollOffset);

        List<Bandanna> filtered = new ArrayList<>();
        for (Bandanna bandanna : bandannaManager.getBandannas()) {
            if (!isBandannaVisible(bandanna, searchQuery)) {
                continue;
            }
            filtered.add(bandanna);
        }

        if (filtered.isEmpty()) {
            y += drawEmptyMessage(nvg, palette, x, y);
            return y;
        }

        y += CARD_GAP;
        int columns = Math.max(1, (int) ((width + CARD_GAP) / (CARD_WIDTH + CARD_GAP)));
        int rows = (filtered.size() + columns - 1) / columns;

        for (int index = 0; index < filtered.size(); index++) {
            Bandanna bandanna = filtered.get(index);
            int column = index % columns;
            int row = index / columns;
            float cardX = x + column * (CARD_WIDTH + CARD_GAP);
            float cardY = y + row * (CARD_HEIGHT + CARD_GAP);

            boolean selected = bandanna.equals(bandannaManager.getCurrentBandanna());
            boolean unlocked = bandannaManager.canUseBandanna(getClientUuid(), bandanna);
            SimpleCardState state = SimpleCardState.of(selected, unlocked);

            PreviewRenderer preview = createBandannaPreview(bandanna);
            drawCard(nvg, palette, accent, cardX, cardY, state, preview, bandanna.getName(), formatRequirement(bandanna.getRequiredRole(), bandannaManager::getTranslateText), mouseX, mouseY, scrollOffset);
            bandannaCardBounds.computeIfAbsent(bandanna, key -> new CardBounds()).set(cardX, cardY + scrollOffset, CARD_WIDTH, CARD_HEIGHT);
        }

        float gridHeight = rows * CARD_HEIGHT + Math.max(0, rows - 1) * CARD_GAP;
        return y + gridHeight + CARD_GAP;
    }

    private float drawMiscSection(NanoVGManager nvg,
                                  ColorPalette palette,
                                  AccentColor accent,
                                  float x,
                                  float width,
                                  float startY,
                                  float scrollOffset,
                                  String searchQuery,
                                  int mouseX,
                                  int mouseY,
                                  float partialTicks) {

        List<Cosmetic> cosmetics = CosmeticManager.getInstance().getCosmetics();
        float y = startY;
        y += drawSectionHeader(nvg, palette, CosmeticSection.OTHERS.getTitle(), CosmeticSection.OTHERS.getDescription(), x, y);

        List<Cosmetic> filtered = new ArrayList<>();
        for (Cosmetic cosmetic : cosmetics) {
            if (!matchesSearch(cosmetic.getName(), searchQuery)) {
                continue;
            }
            filtered.add(cosmetic);
        }

        if (filtered.isEmpty()) {
            y += drawEmptyMessage(nvg, palette, x, y);
            return y;
        }

        y += CARD_GAP;
        int columns = Math.max(1, (int) ((width + CARD_GAP) / (CARD_WIDTH + CARD_GAP)));
        int rows = (filtered.size() + columns - 1) / columns;

        for (int index = 0; index < filtered.size(); index++) {
            Cosmetic cosmetic = filtered.get(index);
            CompCosmetic component = miscComponents.computeIfAbsent(cosmetic, CompCosmetic::new);

            int column = index % columns;
            int row = index / columns;
            float cardX = x + column * (CARD_WIDTH + CARD_GAP);
            float cardY = y + row * (CARD_HEIGHT + CARD_GAP);

            component.translate(cardX, cardY);
            component.draw(mouseX, mouseY, partialTicks);
            miscCardBounds.computeIfAbsent(cosmetic, key -> new CardBounds()).set(cardX, cardY + scrollOffset, component.getWidth(), component.getHeight());
        }

        float gridHeight = rows * CARD_HEIGHT + Math.max(0, rows - 1) * CARD_GAP;
        return y + gridHeight + CARD_GAP;
    }

    private float drawSectionHeader(NanoVGManager nvg, ColorPalette palette, String title, String description, float x, float y) {
        nvg.drawText(title, x, y, palette.getFontColor(ColorType.DARK), 15F, Fonts.SEMIBOLD);
        if (description != null && !description.isEmpty()) {
            nvg.drawText(description, x, y + TITLE_HEIGHT - 4F, ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 220), 9F, Fonts.REGULAR);
            return TITLE_HEIGHT + DESCRIPTION_GAP;
        }
        return TITLE_HEIGHT;
    }

    private float drawCategoryChips(NanoVGManager nvg,
                                    ColorPalette palette,
                                    AccentColor accent,
                                    float x,
                                    float width,
                                    float y,
                                    Enum<?>[] categories,
                                    Enum<?> activeCategory,
                                    java.util.function.Consumer<Enum<?>> onSelect,
                                    float scrollOffset) {

        if (categories.length <= 1) {
            return 0F;
        }

        float offsetX = x;
        for (Enum<?> category : categories) {
            String label = category.name();
            if (category instanceof CapeCategory) {
                label = ((CapeCategory) category).getName();
            } else if (category instanceof WingCategory) {
                label = ((WingCategory) category).getName();
            } else if (category instanceof BandannaCategory) {
                label = ((BandannaCategory) category).getName();
            }

            float textWidth = nvg.getTextWidth(label, 9F, Fonts.MEDIUM);
            float chipWidth = textWidth + CHIP_HORIZONTAL_PADDING * 2F;
            if (offsetX + chipWidth > x + width) {
                offsetX = x;
                y += CHIP_HEIGHT + CHIP_GAP;
            }

            boolean active = category.equals(activeCategory);
            Color background = palette.getBackgroundColor(ColorType.DARK);
            if (active) {
                Color start = ColorUtils.applyAlpha(accent.getColor1(), 180);
                Color end = ColorUtils.applyAlpha(accent.getColor2(), 180);
                nvg.drawGradientRoundedRect(offsetX, y, chipWidth, CHIP_HEIGHT, CHIP_HEIGHT / 2F, start, end);
            } else {
                nvg.drawRoundedRect(offsetX, y, chipWidth, CHIP_HEIGHT, CHIP_HEIGHT / 2F, background);
            }

            Color textColor = active ? Color.WHITE : palette.getFontColor(ColorType.NORMAL);
            nvg.drawText(label, offsetX + CHIP_HORIZONTAL_PADDING, y + CHIP_HEIGHT / 2F - 1F, textColor, 9F, Fonts.MEDIUM);

            Enum<?> chipCategory = category;
            activeChips.add(new FilterChip(offsetX, y + scrollOffset, chipWidth, CHIP_HEIGHT, () -> onSelect.accept(chipCategory)));
            offsetX += chipWidth + CHIP_GAP;
        }

        return CHIP_HEIGHT + CHIP_GAP;
    }

    private float drawEmptyMessage(NanoVGManager nvg, ColorPalette palette, float x, float y) {
        nvg.drawText(TranslateText.COSMETICS_EMPTY.getText(), x, y + 6F, ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 200), 10F, Fonts.REGULAR);
        return TITLE_HEIGHT;
    }

    private PreviewRenderer createCapePreview(Cape cape) {
        if (cape instanceof NormalCape) {
            ResourceLocation sample = ((NormalCape) cape).getSample();
            if (sample != null) {
                return (nvg, px, py, width, height) -> {
                    if (!drawImagePreview(nvg, sample, null, px, py, width, height, 6F)) {
                        defaultPreview().render(nvg, px, py, width, height);
                    }
                };
            }
        } else if (cape instanceof CustomCape) {
            File sample = ((CustomCape) cape).getSample();
            if (sample != null) {
                return (nvg, px, py, width, height) -> {
                    if (!drawImagePreview(nvg, null, sample, px, py, width, height, 6F)) {
                        defaultPreview().render(nvg, px, py, width, height);
                    }
                };
            }
        }
        return defaultPreview();
    }

    private PreviewRenderer createWingPreview(Wing wing) {
        if (wing instanceof NormalWing) {
            NormalWing normalWing = (NormalWing) wing;
            ResourceLocation sample = normalWing.getSample();
            if (sample != null) {
                return (nvg, px, py, width, height) -> {
                    if (!drawImagePreview(nvg, sample, null, px, py, width, height, 6F)) {
                        defaultPreview().render(nvg, px, py, width, height);
                    }
                };
            }
        }
        return defaultPreview();
    }

    private PreviewRenderer createBandannaPreview(Bandanna bandanna) {
        if (bandanna instanceof NormalBandanna) {
            ResourceLocation sample = ((NormalBandanna) bandanna).getSample();
            if (sample != null) {
                return (nvg, px, py, width, height) -> {
                    if (!drawImagePreview(nvg, sample, null, px, py, width, height, 6F)) {
                        defaultPreview().render(nvg, px, py, width, height);
                    }
                };
            }
        }
        return defaultPreview();
    }

    private PreviewRenderer defaultPreview() {
        return (nvg, px, py, width, height) -> {
            ColorPalette palette = Shindo.getInstance().getColorManager().getPalette();
            nvg.drawRoundedRect(px, py, width, height, 6F, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 200));
            nvg.drawCenteredText("-", px + width / 2F, py + height / 2F - 6F, palette.getFontColor(ColorType.NORMAL), 12F, Fonts.SEMIBOLD);
        };
    }

    private void drawCard(NanoVGManager nvg,
                          ColorPalette palette,
                          AccentColor accent,
                          float x,
                          float y,
                          SimpleCardState state,
                          PreviewRenderer preview,
                          String title,
                          String requirementText,
                          int mouseX,
                          int mouseY,
                          float scrollOffset) {

        Color base = palette.getBackgroundColor(ColorType.DARK);
        boolean hovered = MouseUtils.isInside(mouseX, mouseY, x, y + scrollOffset, CARD_WIDTH, CARD_HEIGHT);

        nvg.drawRoundedRect(x, y, CARD_WIDTH, CARD_HEIGHT, 9F, base);

        if (state.isSelected()) {
            nvg.drawGradientRoundedRect(x, y, CARD_WIDTH, CARD_HEIGHT, 9F,
                    ColorUtils.applyAlpha(accent.getColor1(), 90),
                    ColorUtils.applyAlpha(accent.getColor2(), 90));
        } else if (hovered) {
            nvg.drawRoundedRect(x + 1F, y + 1F, CARD_WIDTH - 2F, CARD_HEIGHT - 2F, 8F,
                    ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 230));
        } else {
            nvg.drawRoundedRect(x + 1F, y + 1F, CARD_WIDTH - 2F, CARD_HEIGHT - 2F, 8F,
                    ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 220));
        }

        float previewX = x + 8F;
        float previewY = y + 9F;
        float previewWidth = CARD_WIDTH - 16F;
        float previewHeight = CARD_HEIGHT - 50F;

        nvg.drawRoundedRect(previewX, previewY, previewWidth, previewHeight, 6F,
                ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 200));
        preview.render(nvg, previewX, previewY, previewWidth, previewHeight);

        nvg.drawText(title, x + 8F, y + CARD_HEIGHT - 24F, palette.getFontColor(ColorType.DARK), 10F, Fonts.SEMIBOLD);

        if (requirementText != null && !requirementText.isEmpty()) {
            nvg.drawText(requirementText, x + 8F, y + CARD_HEIGHT - 12F, ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 210), 8F, Fonts.REGULAR);
        }

        if (!state.isUnlocked()) {
            nvg.drawRoundedRect(x, y, CARD_WIDTH, CARD_HEIGHT, 9F, ColorUtils.applyAlpha(base, 180));
            nvg.drawCenteredText(LegacyIcon.PROHIBITED, x + CARD_WIDTH / 2F, y + CARD_HEIGHT / 2F - 8F, new Color(230, 92, 102), 16F, Fonts.LEGACYICON);
        }
    }

    private boolean drawImagePreview(NanoVGManager nvg, ResourceLocation location, File file, float x, float y, float width, float height, float radius) {
        Dimension size = location != null ? nvg.getImageSize(location) : nvg.getImageSize(file);
        if (size == null || size.width <= 0 || size.height <= 0) {
            return false;
        }

        float[] scaled = scaleToFit(size.width, size.height, width, height);
        float drawX = x + (width - scaled[0]) / 2F;
        float drawY = y + (height - scaled[1]) / 2F;

        if (location != null) {
            nvg.drawRoundedImage(location, drawX, drawY, scaled[0], scaled[1], radius);
        } else if (file != null) {
            nvg.drawRoundedImage(file, drawX, drawY, scaled[0], scaled[1], radius);
        } else {
            return false;
        }
        return true;
    }

    private float[] scaleToFit(float originalWidth, float originalHeight, float maxWidth, float maxHeight) {
        if (originalWidth <= 0 || originalHeight <= 0) {
            return new float[]{maxWidth, maxHeight};
        }
        float ratio = Math.min(maxWidth / originalWidth, maxHeight / originalHeight);
        ratio = Math.max(0.01F, ratio);
        return new float[]{originalWidth * ratio, originalHeight * ratio};
    }

    private boolean handleChipClick(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton != 0) {
            return false;
        }
        for (FilterChip chip : activeChips) {
            if (chip.contains(mouseX, mouseY)) {
                chip.onClick();
                return true;
            }
        }
        return false;
    }

    private boolean handleCapeClick(int mouseX, int mouseY, int mouseButton) {

        if (mouseButton != 0) {
            return false;
        }

        CapeManager capeManager = Shindo.getInstance().getCapeManager();

        for (Map.Entry<Cape, CardBounds> entry : capeCardBounds.entrySet()) {
            if (!entry.getValue().contains(mouseX, mouseY)) {
                continue;
            }
            Cape cape = entry.getKey();
            if (!capeManager.canUseCape(getClientUuid(), cape)) {
                Shindo.getInstance().getNotificationManager().post(TranslateText.ERROR, capeManager.getTranslateError(cape.getRequiredRole()), NotificationType.ERROR);
                return true;
            }
            capeManager.setCurrentCape(cape);
            return true;
        }
        return false;
    }

    private boolean handleWingClick(int mouseX, int mouseY, int mouseButton) {

        if (mouseButton != 0) {
            return false;
        }

        WingManager wingManager = Shindo.getInstance().getWingManager();

        for (Map.Entry<Wing, CardBounds> entry : wingCardBounds.entrySet()) {
            if (!entry.getValue().contains(mouseX, mouseY)) {
                continue;
            }
            Wing wing = entry.getKey();
            if (!wingManager.canUseWing(getClientUuid(), wing)) {
                Shindo.getInstance().getNotificationManager().post(TranslateText.ERROR, wingManager.getTranslateError(wing.getRequiredRole()), NotificationType.ERROR);
                return true;
            }
            wingManager.setCurrentWing(wing);
            return true;
        }
        return false;
    }

    private boolean handleBandannaClick(int mouseX, int mouseY, int mouseButton) {

        if (mouseButton != 0) {
            return false;
        }

        BandannaManager bandannaManager = Shindo.getInstance().getBandannaManager();

        for (Map.Entry<Bandanna, CardBounds> entry : bandannaCardBounds.entrySet()) {
            if (!entry.getValue().contains(mouseX, mouseY)) {
                continue;
            }
            Bandanna bandanna = entry.getKey();
            if (!bandannaManager.canUseBandanna(getClientUuid(), bandanna)) {
                Shindo.getInstance().getNotificationManager().post(TranslateText.ERROR, bandannaManager.getTranslateError(bandanna.getRequiredRole()), NotificationType.ERROR);
                return true;
            }
            bandannaManager.setCurrentBandanna(bandanna);
            return true;
        }
        return false;
    }

    private void handleMiscClick(int mouseX, int mouseY, int mouseButton) {
        for (Map.Entry<Cosmetic, CardBounds> entry : miscCardBounds.entrySet()) {
            if (!entry.getValue().contains(mouseX, mouseY)) {
                continue;
            }
            Cosmetic cosmetic = entry.getKey();
            if (mouseButton == 0) {
                cosmetic.toggle();
            } else {
                CompCosmetic component = miscComponents.get(cosmetic);
                if (component != null) {
                    component.mouseClicked(mouseX, (int) (mouseY - scroll.getValue()), mouseButton);
                }
            }
            return;
        }
    }

    private boolean isCapeVisible(Cape cape, String searchQuery) {
        if (activeCapeCategory != CapeCategory.ALL && cape.getCategory() != activeCapeCategory) {
            return false;
        }
        return matchesSearch(cape.getName(), searchQuery);
    }

    private boolean isWingVisible(Wing wing, String searchQuery) {
        if (activeWingCategory != WingCategory.ALL && wing.getCategory() != activeWingCategory) {
            return false;
        }
        return matchesSearch(wing.getName(), searchQuery);
    }

    private boolean isBandannaVisible(Bandanna bandanna, String searchQuery) {
        if (activeBandannaCategory != BandannaCategory.ALL && bandanna.getCategory() != activeBandannaCategory) {
            return false;
        }
        return matchesSearch(bandanna.getName(), searchQuery);
    }

    private void setActiveCapeCategory(CapeCategory category) {
        if (category == null || category == activeCapeCategory) {
            return;
        }
        activeCapeCategory = category;
        scroll.resetAll();
    }

    private void setActiveWingCategory(WingCategory category) {
        if (category == null || category == activeWingCategory) {
            return;
        }
        activeWingCategory = category;
        scroll.resetAll();
    }

    private void setActiveBandannaCategory(BandannaCategory category) {
        if (category == null || category == activeBandannaCategory) {
            return;
        }
        activeBandannaCategory = category;
        scroll.resetAll();
    }

    private boolean matchesSearch(String value, String query) {
        if (query == null || query.isEmpty()) {
            return true;
        }
        return SearchUtils.isSimilar(value, query);
    }

    private String formatRequirement(Role role, Function<Role, TranslateText> mapper) {
        if (role == null || role == Role.MEMBER) {
            return "";
        }
        TranslateText translate = mapper.apply(role);
        if (translate == null || translate == TranslateText.NONE) {
            return "";
        }
        return translate.getText();
    }

    private UUID getClientUuid() {
        return mc.getSession().getProfile().getId();
    }

    private static class CardBounds {
        private float x;
        private float y;
        private float width;
        private float height;

        void set(float x, float y, float width, float height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        boolean contains(int mx, int my) {
            return MouseUtils.isInside(mx, my, x, y, width, height);
        }
    }

    private static class FilterChip {
        private final float x;
        private final float y;
        private final float width;
        private final float height;
        private final Runnable onClick;

        FilterChip(float x, float y, float width, float height, Runnable onClick) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.onClick = onClick;
        }

        boolean contains(int mx, int my) {
            return MouseUtils.isInside(mx, my, x, y, width, height);
        }

        void onClick() {
            onClick.run();
        }
    }

    private interface PreviewRenderer {
        void render(NanoVGManager nvg, float x, float y, float width, float height);
    }

    private static class SimpleCardState {
        private final boolean selected;
        private final boolean unlocked;

        private SimpleCardState(boolean selected, boolean unlocked) {
            this.selected = selected;
            this.unlocked = unlocked;
        }

        static SimpleCardState of(boolean selected, boolean unlocked) {
            return new SimpleCardState(selected, unlocked);
        }

        boolean isSelected() {
            return selected;
        }

        boolean isUnlocked() {
            return unlocked;
        }
    }
}

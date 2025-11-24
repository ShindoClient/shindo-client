package me.miki.shindo.gui.modmenu.category.impl;

import me.miki.shindo.Shindo;
import me.miki.shindo.api.roles.Role;
import me.miki.shindo.gui.modmenu.GuiModMenu;
import me.miki.shindo.gui.modmenu.category.Category;
import me.miki.shindo.gui.modmenu.category.impl.shared.CategoryChipRenderer;
import me.miki.shindo.gui.modmenu.category.impl.shared.FilterChip;
import me.miki.shindo.management.color.AccentColor;
import me.miki.shindo.management.color.palette.ColorPalette;
import me.miki.shindo.management.color.palette.ColorType;
import me.miki.shindo.management.cosmetic.cape.CapeCategory;
import me.miki.shindo.management.cosmetic.cape.CapeManager;
import me.miki.shindo.management.cosmetic.cape.impl.Cape;
import me.miki.shindo.management.cosmetic.cape.impl.CustomCape;
import me.miki.shindo.management.cosmetic.cape.impl.NormalCape;
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

    private static final float CONTENT_PADDING = 22F;
    private static final float CHIP_GAP = 10F;
    private static final float CARD_WIDTH = 108F;
    private static final float CARD_HEIGHT = 152F;
    private static final float CARD_GAP = 14F;

    private final Minecraft mc = Minecraft.getMinecraft();
    private final Map<Cape, CardBounds> capeCardBounds = new IdentityHashMap<>();
    private final List<FilterChip> categoryChips = new ArrayList<>();
    private CapeCategory activeCategory = CapeCategory.ALL;

    public CosmeticsCategory(GuiModMenu parent) {
        super(parent, TranslateText.COSMETICS, LegacyIcon.SHOPPING, true, true);
    }

    @Override
    public void initGui() {
        activeCategory = CapeCategory.ALL;
        scroll.resetAll();
    }

    @Override
    public void initCategory() {
        scroll.resetAll();
    }

    public boolean shouldShowCustomCapeFolder() {
        return activeCategory == CapeCategory.CUSTOM;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        Shindo instance = Shindo.getInstance();
        NanoVGManager nvg = instance.getNanoVGManager();
        ColorPalette palette = instance.getColorManager().getPalette();
        AccentColor accent = instance.getColorManager().getCurrentColor();

        final float viewportX = getX();
        final float viewportY = getY();
        final float viewportWidth = getWidth();
        final float viewportHeight = getHeight();

        categoryChips.clear();
        capeCardBounds.clear();

        if (MouseUtils.isInside(mouseX, mouseY, viewportX, viewportY, viewportWidth, viewportHeight)) {
            scroll.onScroll();
        }
        scroll.onAnimation();
        float scrollOffset = scroll.getValue();

        String searchQuery = getSearchBox().getText() == null ? "" : getSearchBox().getText().trim();

        float contentX = viewportX + CONTENT_PADDING;
        float contentWidth = viewportWidth - (CONTENT_PADDING * 2F);
        float startY = viewportY + CONTENT_PADDING;
        float y = startY;

        nvg.save();
        nvg.scissor(viewportX, viewportY, viewportWidth, viewportHeight);
        nvg.translate(0, scrollOffset);

        y = drawCategoryChips(nvg, palette, accent, contentX, contentWidth, y, scrollOffset, mouseX, mouseY);
        y += 20F;
        y = drawCapeGrid(nvg, palette, accent, contentX, contentWidth, y, scrollOffset, searchQuery, mouseX, mouseY);

        nvg.restore();

        float logicalHeight = Math.max(0F, (y - startY) + CONTENT_PADDING);
        scroll.setMaxScroll(Math.max(0F, logicalHeight - viewportHeight));
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (handleCategoryChipClick(mouseX, mouseY, mouseButton)) {
            return;
        }
        handleCapeClick(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        scroll.onKey(keyCode);
    }

    private float drawCategoryChips(NanoVGManager nvg, ColorPalette palette, AccentColor accent, float x, float width, float y, float scrollOffset, int mouseX, int mouseY) {

        categoryChips.clear();

        float currentX = x;
        float currentY = y;

        for (CapeCategory category : CapeCategory.values()) {
            String label = category.getName();
            float chipWidth = CategoryChipRenderer.computeWidth(nvg, label, null);

            if (currentX + chipWidth > x + width) {
                currentX = x;
                currentY += CategoryChipRenderer.CHIP_HEIGHT + CHIP_GAP;
            }

            boolean active = category == activeCategory;
            boolean hovered = MouseUtils.isInside(mouseX, mouseY, currentX, currentY + scrollOffset, chipWidth, CategoryChipRenderer.CHIP_HEIGHT);

            CategoryChipRenderer.drawChip(nvg, palette, accent, currentX, currentY, chipWidth, label, null, active, hovered);

            FilterChip chip = new FilterChip(() -> {
                if (activeCategory != category) {
                    activeCategory = category;
                    scroll.resetAll();
                }
            });
            chip.setBounds(currentX, currentY + scrollOffset, chipWidth, CategoryChipRenderer.CHIP_HEIGHT);
            categoryChips.add(chip);

            currentX += chipWidth + CHIP_GAP;
        }

        return currentY + CategoryChipRenderer.CHIP_HEIGHT;
    }

    private float drawCapeGrid(NanoVGManager nvg, ColorPalette palette, AccentColor accent, float x, float width, float startY, float scrollOffset, String searchQuery, int mouseX, int mouseY) {

        CapeManager capeManager = Shindo.getInstance().getCapeManager();
        List<Cape> filtered = new ArrayList<>();

        for (Cape cape : capeManager.getCapes()) {
            if (!isCapeVisible(cape, searchQuery)) {
                continue;
            }
            filtered.add(cape);
        }

        float y = startY;

        if (filtered.isEmpty()) {
            nvg.drawText(TranslateText.COSMETICS_EMPTY.getText(), x, y + 4F,
                    ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 210), 10F, Fonts.REGULAR);
            return y + 28F;
        }

        int maxColumns = Math.max(1, (int) ((width + CARD_GAP) / (CARD_WIDTH + CARD_GAP)));
        int columns = Math.min(3, maxColumns);
        int rows = (filtered.size() + columns - 1) / columns;

        Cape current = capeManager.getCurrentCape();

        for (int index = 0; index < filtered.size(); index++) {
            Cape cape = filtered.get(index);
            int column = index % columns;
            int row = index / columns;

            float cardX = x + column * (CARD_WIDTH + CARD_GAP);
            float cardY = y + row * (CARD_HEIGHT + CARD_GAP);

            boolean selected = cape.equals(current);
            boolean unlocked = capeManager.canUseCape(getClientUuid(), cape);
            SimpleCardState state = SimpleCardState.of(selected, unlocked);

            PreviewRenderer preview = createCapePreview(cape);
            drawCapeCard(nvg, palette, accent, cardX, cardY, preview, cape, state,
                    formatRequirement(cape.getRequiredRole(), capeManager::getTranslateText),
                    mouseX, mouseY, scrollOffset);

            capeCardBounds.computeIfAbsent(cape, key -> new CardBounds())
                    .set(cardX, cardY + scrollOffset, CARD_WIDTH, CARD_HEIGHT);
        }

        float gridHeight = rows * CARD_HEIGHT + Math.max(0, rows - 1) * CARD_GAP;
        return y + gridHeight;
    }

    private void drawCapeCard(NanoVGManager nvg, ColorPalette palette, AccentColor accent, float x, float y, PreviewRenderer preview, Cape cape, SimpleCardState state, String requirement, int mouseX, int mouseY, float scrollOffset) {

        boolean hovered = MouseUtils.isInside(mouseX, mouseY, x, y + scrollOffset, CARD_WIDTH, CARD_HEIGHT);

        Color base = palette.getBackgroundColor(ColorType.DARK);
        nvg.drawRoundedRect(x, y, CARD_WIDTH, CARD_HEIGHT, 14F, base);
        nvg.drawRoundedRect(x + 1F, y + 1F, CARD_WIDTH - 2F, CARD_HEIGHT - 2F, 13F,
                ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), hovered ? 235 : 215));

        if (state.isSelected()) {
            nvg.drawGradientRoundedRect(x, y, CARD_WIDTH, CARD_HEIGHT, 14F,
                    ColorUtils.applyAlpha(accent.getColor1(), 140),
                    ColorUtils.applyAlpha(accent.getColor2(), 140));
        }

        float previewX = x + 10F;
        float previewY = y + 12F;
        float previewWidth = CARD_WIDTH - 20F;
        float previewHeight = CARD_HEIGHT - 68F;

        nvg.drawRoundedRect(previewX, previewY, previewWidth, previewHeight, 10F, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 210));
        preview.render(nvg, previewX + 4F, previewY + 4F, previewWidth - 8F, previewHeight - 8F);

        nvg.drawText(cape.getName(), x + 12F, y + CARD_HEIGHT - 34F, palette.getFontColor(ColorType.DARK), 9.8F, Fonts.MEDIUM);

        if (requirement != null && !requirement.isEmpty()) {
            nvg.drawText(requirement, x + 12F, y + CARD_HEIGHT - 20F,
                    ColorUtils.applyAlpha(palette.getFontColor(ColorType.NORMAL), 220), 8.6F, Fonts.REGULAR);
        }

        if (!state.isUnlocked()) {
            Color mask = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 210);
            nvg.drawRoundedRect(x, y, CARD_WIDTH, CARD_HEIGHT, 14F, mask);
            nvg.drawCenteredText(LegacyIcon.LOCK, x + CARD_WIDTH / 2F, y + CARD_HEIGHT / 2F - 8F,
                    new Color(227, 116, 116), 18F, Fonts.LEGACYICON);
        }
    }

    private boolean handleCategoryChipClick(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton != 0) {
            return false;
        }
        for (FilterChip chip : categoryChips) {
            if (chip.contains(mouseX, mouseY)) {
                chip.click();
                return true;
            }
        }
        return false;
    }

    private void handleCapeClick(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton != 0) {
            return;
        }

        CapeManager capeManager = Shindo.getInstance().getCapeManager();

        for (Map.Entry<Cape, CardBounds> entry : capeCardBounds.entrySet()) {
            if (!entry.getValue().contains(mouseX, mouseY)) {
                continue;
            }
            Cape cape = entry.getKey();
            if (!capeManager.canUseCape(getClientUuid(), cape)) {
                Shindo.getInstance().getNotificationManager()
                        .post(TranslateText.ERROR, capeManager.getTranslateError(cape.getRequiredRole()), NotificationType.ERROR);
                return;
            }
            capeManager.setCurrentCape(cape);
            return;
        }
    }

    private PreviewRenderer createCapePreview(Cape cape) {
        if (cape instanceof NormalCape) {
            ResourceLocation sample = ((NormalCape) cape).getSample();
            if (sample != null) {
                return (nvg, px, py, width, height) -> {
                    if (!drawImagePreview(nvg, sample, null, px, py, width, height, 8F)) {
                        defaultPreview().render(nvg, px, py, width, height);
                    }
                };
            }
        } else if (cape instanceof CustomCape) {
            File sample = ((CustomCape) cape).getSample();
            if (sample != null) {
                return (nvg, px, py, width, height) -> {
                    if (!drawImagePreview(nvg, null, sample, px, py, width, height, 8F)) {
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
            nvg.drawRoundedRect(px, py, width, height, 8F, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.NORMAL), 190));
            nvg.drawCenteredText("-", px + width / 2F, py + height / 2F - 6F, palette.getFontColor(ColorType.NORMAL), 12F, Fonts.SEMIBOLD);
        };
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

    private boolean isCapeVisible(Cape cape, String searchQuery) {
        if (activeCategory != CapeCategory.ALL && cape.getCategory() != activeCategory) {
            return false;
        }
        return matchesSearch(cape.getName(), searchQuery);
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

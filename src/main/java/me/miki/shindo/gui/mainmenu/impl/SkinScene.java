package me.miki.shindo.gui.mainmenu.impl;

import me.miki.shindo.Shindo;
import me.miki.shindo.gui.mainmenu.GuiShindoMainMenu;
import me.miki.shindo.gui.mainmenu.MainMenuScene;
import me.miki.shindo.gui.modmenu.category.impl.shared.CategoryChipRenderer;
import me.miki.shindo.logger.ShindoLogger;
import me.miki.shindo.management.color.AccentColor;
import me.miki.shindo.management.color.palette.ColorPalette;
import me.miki.shindo.management.color.palette.ColorType;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.management.nanovg.font.Fonts;
import me.miki.shindo.management.nanovg.font.LegacyIcon;
import me.miki.shindo.management.notification.NotificationType;
import me.miki.shindo.management.skin.Skin;
import me.miki.shindo.management.skin.SkinManager;
import me.miki.shindo.management.skin.SkinManager.DownloadedSkin;
import me.miki.shindo.management.skin.SkinPreviewRenderer;
import me.miki.shindo.management.skin.SkinType;
import me.miki.shindo.ui.comp.impl.field.CompMainMenuTextBox;
import me.miki.shindo.utils.ColorUtils;
import me.miki.shindo.utils.Multithreading;
import me.miki.shindo.utils.animation.normal.Animation;
import me.miki.shindo.utils.animation.normal.Direction;
import me.miki.shindo.utils.animation.normal.easing.EaseInOutCirc;
import me.miki.shindo.utils.animation.simple.SimpleAnimation;
import me.miki.shindo.utils.buffer.ScreenAnimation;
import me.miki.shindo.utils.mouse.MouseUtils;
import me.miki.shindo.utils.mouse.Scroll;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;

public class SkinScene extends MainMenuScene {

    private static final int CARDS_PER_ROW = 4;
    private static final float CARD_GAP = 12F;
    private static final float CARD_HEIGHT = 152F;
    private static final float FORM_PANEL_WIDTH = 360F;
    private static final float FORM_PANEL_HEIGHT = 340F;
    private static final float CONTENT_SLIDE_EXTRA = 48F;

    private final ScreenAnimation screenAnimation = new ScreenAnimation();
    private final Scroll scroll = new Scroll();
    private Animation introAnimation;
    private final SimpleAnimation formTransition = new SimpleAnimation();

    private final SkinPreviewRenderer previewRenderer = new SkinPreviewRenderer();
    private final List<CardSlot> cardSlots = new ArrayList<>();
    private final EnumMap<FilterType, Hitbox> filterChipBounds = new EnumMap<>(FilterType.class);
    private final EnumMap<SkinSource, Hitbox> sourceChipBounds = new EnumMap<>(SkinSource.class);
    private final EnumMap<SkinType, Hitbox> typeChipBounds = new EnumMap<>(SkinType.class);

    private final SkinFormState formState = new SkinFormState();

    private FilterType currentFilter = FilterType.ALL;
    private FormMode formMode = FormMode.HIDDEN;
    private Skin editingSkin;

    private Hitbox resetSelectionButton;
    private Hitbox saveButton;
    private Hitbox cancelButton;
    private Hitbox formBounds;

    public SkinScene(GuiShindoMainMenu parent) {
        super(parent);
    }

    @Override
    public void initScene() {
        introAnimation = new EaseInOutCirc(250, 1.0F);
        introAnimation.setDirection(Direction.FORWARDS);
        formTransition.setValue(0F);
        formMode = FormMode.HIDDEN;
        editingSkin = null;
        formState.resetForAdd();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        ScaledResolution sr = new ScaledResolution(mc);
        Shindo instance = Shindo.getInstance();
        NanoVGManager nvg = instance.getNanoVGManager();
        screenAnimation.wrap(() -> drawNanoVG(mouseX, mouseY, partialTicks, sr, instance, nvg), 0, 0, sr.getScaledWidth(), sr.getScaledHeight(), 2 - introAnimation.getValueFloat(), Math.min(introAnimation.getValueFloat(), 1), false);
        if (introAnimation.isDone(Direction.BACKWARDS)) {
            this.setCurrentScene(this.getSceneByClass(MainScene.class));
        }
    }

    private void drawNanoVG(int mouseX, int mouseY, float partialTicks, ScaledResolution sr, Shindo instance, NanoVGManager nvg) {
        ColorPalette palette = getMenuPalette();
        AccentColor accent = getMenuAccent();
        SkinManager skinManager = instance.getSkinManager();

        int acWidth = 640;
        int acHeight = 370;
        int acX = sr.getScaledWidth() / 2 - (acWidth / 2);
        int acY = sr.getScaledHeight() / 2 - (acHeight / 2);

        float slideDistance = acWidth + CONTENT_SLIDE_EXTRA;
        formTransition.setAnimation(formMode == FormMode.HIDDEN ? 0F : 1F, 20);
        float transition = Math.max(0F, Math.min(1F, formTransition.getValue()));
        float contentTranslate = -transition * slideDistance;
        float formTranslate = (1F - transition) * slideDistance;
        int logicalMouseX = Math.round(mouseX - acX - contentTranslate);
        int logicalMouseY = mouseY - acY;
        int formMouseX = Math.round(mouseX - acX - formTranslate);
        int formMouseY = mouseY - acY;
        boolean formVisible = isFormTransitionActive(transition);

        if (!formVisible) {
            scroll.onScroll();
        }
        scroll.onAnimation();

        nvg.save();
        nvg.translate(acX, acY);
        nvg.drawRoundedRect(0, 0, acWidth, acHeight, 10, getPanelColor());

        nvg.save();
        nvg.scissor(0, 0, acWidth, acHeight);
        nvg.translate(contentTranslate, 0F);
        nvg.drawCenteredText(tx(TranslateText.SKIN_LIBRARY_TITLE), (acWidth / 2F), 10, Color.WHITE, 16, Fonts.SEMIBOLD);
        nvg.drawCenteredText(tx(TranslateText.SKIN_LIBRARY_SUBTITLE), (acWidth / 2F), 26, palette.getFontColor(ColorType.DARK), 9.5F, Fonts.REGULAR);
        drawFilterChips(logicalMouseX, logicalMouseY, nvg, palette, accent, 18, 50);
        drawResetButton(logicalMouseX, logicalMouseY, nvg, palette, accent, acWidth - 132, 50);

        float gridX = 18;
        float gridY = 90;
        float gridWidth = acWidth - 36;
        float gridHeight = acHeight - 110;
        drawSkinGrid(logicalMouseX, logicalMouseY, partialTicks, nvg, palette, accent, skinManager, gridX, gridY, gridWidth, gridHeight);
        nvg.restore();

        if (formVisible) {
            nvg.save();
            nvg.intersectScissor(0, 0, acWidth, acHeight);
            nvg.translate(formTranslate, 0F);
            float formX = (acWidth - FORM_PANEL_WIDTH) / 2F;
            float formY = (acHeight - FORM_PANEL_HEIGHT) / 2F;
            drawFormPanel(formMouseX, formMouseY, nvg, palette, accent, formX, formY);
            nvg.restore();
        } else {
            formBounds = null;
        }

        nvg.restore();
    }

    private void drawFilterChips(int mouseX, int mouseY, NanoVGManager nvg, ColorPalette palette, AccentColor accent, float startX, float y) {
        filterChipBounds.clear();
        float x = startX;
        for (FilterType filterType : FilterType.values()) {
            String label = filterType == FilterType.ALL ? tx(TranslateText.SKIN_FILTER_ALL) : tx(TranslateText.SKIN_FILTER_FAVORITES);
            String icon = filterType == FilterType.ALL ? LegacyIcon.LIST : LegacyIcon.STAR;
            float width = CategoryChipRenderer.computeWidth(nvg, label, icon);
            boolean hovered = MouseUtils.isInside(mouseX, mouseY, x, y, width, CategoryChipRenderer.CHIP_HEIGHT);
            boolean active = currentFilter == filterType;
            CategoryChipRenderer.drawChip(nvg, palette, accent, x, y, width, label, icon, active, hovered);
            filterChipBounds.put(filterType, new Hitbox(x, y, width, CategoryChipRenderer.CHIP_HEIGHT));
            x += width + 8;
        }
    }

    private void drawResetButton(int mouseX, int mouseY, NanoVGManager nvg, ColorPalette palette, AccentColor accent, float x, float y) {
        float width = 120;
        float height = 22;
        boolean hovered = MouseUtils.isInside(mouseX, mouseY, x, y, width, height);
        Color background = palette.getBackgroundColor(ColorType.DARK);
        if (hovered) {
            background = ColorUtils.applyAlpha(background, 220);
        }
        nvg.drawRoundedRect(x, y, width, height, 6, background);
        nvg.drawCenteredText(tx(TranslateText.SKIN_RESET_BUTTON), x + (width / 2F), y + 7, Color.WHITE, 9.5F, Fonts.MEDIUM);
        nvg.drawText(LegacyIcon.REFRESH, x + 7, y + 6, Color.WHITE, 10, Fonts.LEGACYICON);
        resetSelectionButton = new Hitbox(x, y, width, height);
    }

    private void drawSkinGrid(int mouseX, int mouseY, float partialTicks, NanoVGManager nvg, ColorPalette palette, AccentColor accent, SkinManager skinManager, float gridX, float gridY, float gridWidth, float gridHeight) {
        cardSlots.clear();
        List<Skin> entries = new ArrayList<>(skinManager.getSkins());
        if (currentFilter == FilterType.FAVORITES) {
            entries.removeIf(skin -> !skin.isFavorite());
        }

        List<Object> cards = new ArrayList<>();
        cards.add(null); // add card
        cards.addAll(entries);

        float cardWidth = (gridWidth - (CARD_GAP * (CARDS_PER_ROW - 1))) / CARDS_PER_ROW;
        float scrollValue = scroll.getValue();

        nvg.save();
        nvg.intersectScissor(gridX, gridY - 8, gridWidth, gridHeight + 16);
        nvg.translate(0, scrollValue);

        int column = 0;
        int row = 0;

        for (Object entry : cards) {
            float cardX = gridX + column * (cardWidth + CARD_GAP);
            float cardY = gridY + row * (CARD_HEIGHT + CARD_GAP);
            float renderY = cardY + scrollValue;
                if (entry != null) {
                    Skin skin = (Skin) entry;
                    CardSlot slot = drawSkinCard(mouseX, mouseY, partialTicks, scrollValue, nvg, palette, accent, skinManager, skin, cardX, cardY, cardWidth);
                    slot.area = new Hitbox(cardX, renderY, cardWidth, CARD_HEIGHT);
                    cardSlots.add(slot);
                } else {
                    drawAddCard(mouseX, mouseY, scrollValue, nvg, palette, accent, cardX, cardY, cardWidth);
                    cardSlots.add(CardSlot.createAdd(new Hitbox(cardX, renderY, cardWidth, CARD_HEIGHT)));
                }

            column++;
            if (column >= CARDS_PER_ROW) {
                column = 0;
                row++;
            }
        }

        nvg.restore();

        int totalRows = (int) Math.ceil(cards.size() / (float) CARDS_PER_ROW);
        float contentHeight = Math.max(0, totalRows * (CARD_HEIGHT + CARD_GAP) - CARD_GAP);
        float maxScroll = Math.max(0, contentHeight - gridHeight);
        scroll.setMaxScroll(maxScroll);

        if (entries.isEmpty()) {
            nvg.drawCenteredText(tx(TranslateText.SKIN_EMPTY_PRIMARY), gridX + (gridWidth / 2F), gridY + (gridHeight / 2F) - 18, palette.getFontColor(ColorType.DARK), 11, Fonts.MEDIUM);
            nvg.drawCenteredText(tx(TranslateText.SKIN_EMPTY_SECONDARY), gridX + (gridWidth / 2F), gridY + (gridHeight / 2F) - 2, palette.getFontColor(ColorType.DARK), 9, Fonts.REGULAR);
        }
    }

    private void drawAddCard(int mouseX, int mouseY, float scrollValue, NanoVGManager nvg, ColorPalette palette, AccentColor accent, float cardX, float cardY, float cardWidth) {
        boolean hovered = MouseUtils.isInside(mouseX, mouseY, cardX, cardY + scrollValue, cardWidth, CARD_HEIGHT);
        Color base = palette.getBackgroundColor(ColorType.DARK);
        Color background = hovered ? ColorUtils.applyAlpha(base, 230) : base;
        nvg.drawRoundedRect(cardX, cardY, cardWidth, CARD_HEIGHT, 8, getControlColor());
        nvg.drawRoundedRect(cardX + 8, cardY + 8, cardWidth - 16, CARD_HEIGHT - 16, 8, getPanelColor());
        nvg.drawCenteredText(LegacyIcon.PLUS, cardX + (cardWidth / 2F), cardY + 36, Color.WHITE, 26, Fonts.LEGACYICON);
        nvg.drawCenteredText(tx(TranslateText.SKIN_ADD_CARD_TITLE), cardX + (cardWidth / 2F), cardY + CARD_HEIGHT - 45, Color.WHITE, 11, Fonts.MEDIUM);
        nvg.drawCenteredText(tx(TranslateText.SKIN_ADD_CARD_SUBTITLE), cardX + (cardWidth / 2F), cardY + CARD_HEIGHT - 30, palette.getFontColor(ColorType.DARK), 8.5F, Fonts.REGULAR);
    }

    private CardSlot drawSkinCard(int mouseX, int mouseY, float partialTicks, float scrollValue, NanoVGManager nvg, ColorPalette palette, AccentColor accent, SkinManager skinManager, Skin skin, float cardX, float cardY, float cardWidth) {
        boolean hovered = MouseUtils.isInside(mouseX, mouseY, cardX, cardY + scrollValue, cardWidth, CARD_HEIGHT);
        Skin current = skinManager.getCurrentSkin();
        boolean selected = current != null && current.equals(skin);

        Color base = palette.getBackgroundColor(ColorType.DARK);
        Color background = hovered ? ColorUtils.applyAlpha(base, 225) : base;
        nvg.drawRoundedRect(cardX, cardY, cardWidth, CARD_HEIGHT, 8, background);

        if (selected) {
            nvg.drawGradientRoundedRect(cardX - 1, cardY - 1, cardWidth + 2, CARD_HEIGHT + 2, 9, ColorUtils.applyAlpha(accent.getColor1(), 120), ColorUtils.applyAlpha(accent.getColor2(), 120));
        }

        String limitedName = nvg.getLimitText(skin.getName(), 12F, Fonts.MEDIUM, cardWidth - 70);
        float nameWidth = nvg.getTextWidth(limitedName, 12F, Fonts.MEDIUM) + 12F;
        nvg.drawRoundedRect(cardX + 8, cardY + 8, nameWidth, 18F, 6F, base);
        nvg.drawText(limitedName, cardX + 12, cardY + 12, Color.WHITE, 12, Fonts.MEDIUM);

        if (selected) {
            String badge = tx(TranslateText.SKIN_BADGE_IN_USE);
            float badgeWidth = nvg.getTextWidth(badge, 8, Fonts.REGULAR) + 12;
            nvg.drawRoundedRect(cardX + cardWidth - badgeWidth - 12, cardY + 28, badgeWidth, 14, 6, ColorUtils.applyAlpha(accent.getColor1(), 200));
            nvg.drawText(badge, cardX + cardWidth - badgeWidth - 6, cardY + 32, Color.WHITE, 8, Fonts.REGULAR);
        }

        float iconSize = 16;
        float iconY = cardY + 8;
        float starX = cardX + cardWidth - iconSize - 6;
        float editX = starX - iconSize - 4;
        float deleteX = editX - iconSize - 4;

        CardSlot slot = new CardSlot(skin, false);
        slot.favoriteButton = drawIconButton(nvg, palette, accent, starX, iconY, iconSize, skin.isFavorite() ? LegacyIcon.STAR_FILL : LegacyIcon.STAR, skin.isFavorite(), scrollValue);
        slot.editButton = drawIconButton(nvg, palette, accent, editX, iconY, iconSize, LegacyIcon.EDIT, false, scrollValue);
        slot.deleteButton = drawIconButton(nvg, palette, accent, deleteX, iconY, iconSize, LegacyIcon.TRASH, false, scrollValue);

        float previewBottom = cardY + CARD_HEIGHT - 40;
        float previewMaxWidth = Math.max(20F, cardWidth - 32F);
        float previewMaxHeight = Math.max(20F, CARD_HEIGHT - 86F);
        float scaleByWidth = previewMaxWidth / previewRenderer.getBaseWidth();
        float scaleByHeight = previewMaxHeight / previewRenderer.getBaseHeight();
        float previewScale = Math.min(Math.min(scaleByWidth, scaleByHeight), 4.0F);
        boolean rendered = renderPreview(skin, cardX + (cardWidth / 2F), previewBottom, previewScale, nvg);
        if (!rendered) {
            nvg.drawCenteredText(tx(TranslateText.SKIN_PREVIEW_UNAVAILABLE), cardX + (cardWidth / 2F), previewBottom - 10, palette.getFontColor(ColorType.NORMAL), 9, Fonts.REGULAR);
        }

        String typeLabel = skin.getType() == SkinType.SLIM ? tx(TranslateText.SKIN_TYPE_SLIM) : tx(TranslateText.SKIN_TYPE_DEFAULT);
        float typeWidth = nvg.getTextWidth(typeLabel, 9F, Fonts.REGULAR) + 10F;
        nvg.drawRoundedRect(cardX + 12, cardY + CARD_HEIGHT - 43F, typeWidth, 14F, 4F, palette.getBackgroundColor(ColorType.NORMAL));
        nvg.drawText(typeLabel, cardX + 16, cardY + CARD_HEIGHT - 40F, Color.WHITE, 9F, Fonts.REGULAR);

        float buttonWidth = cardWidth - 24;
        float buttonHeight = 20;
        float buttonX = cardX + 12;
        float buttonY = cardY + CARD_HEIGHT - buttonHeight - 8;
        Color buttonColor = selected ? ColorUtils.applyAlpha(accent.getColor1(), 220) : palette.getBackgroundColor(ColorType.NORMAL);
        nvg.drawRoundedRect(buttonX, buttonY, buttonWidth, buttonHeight, 6, buttonColor);
        nvg.drawCenteredText(selected ? tx(TranslateText.SKIN_BUTTON_SELECTED) : tx(TranslateText.SKIN_BUTTON_USE), buttonX + (buttonWidth / 2F), buttonY + 6, Color.WHITE, 9.5F, Fonts.MEDIUM);

        slot.selectButton = new Hitbox(buttonX, buttonY + scrollValue, buttonWidth, buttonHeight);
        return slot;
    }

    private Hitbox drawIconButton(NanoVGManager nvg, ColorPalette palette, AccentColor accent, float x, float y, float size, String icon, boolean active, float scrollValue) {
        Color background = palette.getBackgroundColor(ColorType.NORMAL);
        if (active) {
            background = ColorUtils.applyAlpha(accent.getColor1(), 200);
        }
        nvg.drawRoundedRect(x, y, size, size, 4, background);
        nvg.drawCenteredText(icon, x + (size / 2F), y + 3, Color.WHITE, 11, Fonts.LEGACYICON);
        return new Hitbox(x, y + scrollValue, size, size);
    }

    private boolean renderPreview(Skin skin, float centerX, float bottomY, float pixelScale, NanoVGManager nvg) {
        if (skin == null || nvg == null) {
            return false;
        }

        String uuid = skin.getProfileUuid();
        if (uuid == null || uuid.trim().isEmpty()) {
            return false;
        }

        float width = previewRenderer.getBaseWidth() * pixelScale;
        float height = previewRenderer.getBaseHeight() * pixelScale;
        float drawX = centerX - (width / 2F);
        float drawY = bottomY - height;

        previewRenderer.renderRemoteSkinPreview(
                nvg.getContext(),
                uuid,
                drawX,
                drawY,
                pixelScale,
                new Color(0, 0, 0, 35),
                null);
        return previewRenderer.isPreviewCached(uuid);
    }

    private void drawFormPanel(int mouseX, int mouseY, NanoVGManager nvg, ColorPalette palette, AccentColor accent, float formX, float formY) {
        float formWidth = FORM_PANEL_WIDTH;
        float formHeight = FORM_PANEL_HEIGHT;
        float drawX = formX;
        formBounds = new Hitbox(formX, formY, formWidth, formHeight);

        nvg.drawRoundedRect(drawX, formY, formWidth, formHeight, 10, getPanelColor());
        nvg.drawCenteredText(formMode == FormMode.ADD ? tx(TranslateText.SKIN_FORM_ADD_TITLE) : tx(TranslateText.SKIN_FORM_EDIT_TITLE), drawX + (formWidth / 2F), formY + 12, Color.WHITE, 14, Fonts.SEMIBOLD);

        float inset = 16F;
        float currentY = formY + 38F;

        formState.nameField.setPosition(drawX + inset, currentY, formWidth - (inset * 2F), 22);
        formState.nameField.setBackgroundColor(palette.getBackgroundColor(ColorType.DARK));
        formState.nameField.setFontColor(Color.WHITE);
        formState.nameField.draw(mouseX, mouseY, 0);
        currentY += 32F;

        nvg.drawText(tx(TranslateText.SKIN_FORM_SOURCE_LABEL), drawX + inset, currentY - 6, palette.getFontColor(ColorType.NORMAL), 9, Fonts.REGULAR);
        drawSourceChips(mouseX, mouseY, nvg, palette, accent, drawX + inset, currentY + 4F);
        currentY += CategoryChipRenderer.CHIP_HEIGHT + 18F;

        if (formState.source == SkinSource.USERNAME) {
            formState.usernameField.setPosition(drawX + inset, currentY, formWidth - (inset * 2F), 22);
            formState.usernameField.setBackgroundColor(palette.getBackgroundColor(ColorType.DARK));
            formState.usernameField.setFontColor(Color.WHITE);
            formState.usernameField.draw(mouseX, mouseY, 0);
        } else {
            formState.uuidField.setPosition(drawX + inset, currentY, formWidth - (inset * 2F), 22);
            formState.uuidField.setBackgroundColor(palette.getBackgroundColor(ColorType.DARK));
            formState.uuidField.setFontColor(Color.WHITE);
            formState.uuidField.draw(mouseX, mouseY, 0);
        }
        currentY += 32F;

        nvg.drawText(tx(TranslateText.SKIN_FORM_MODEL_LABEL), drawX + inset, currentY - 6, palette.getFontColor(ColorType.NORMAL), 9, Fonts.REGULAR);
        drawTypeChips(mouseX, mouseY, nvg, palette, accent, drawX + inset, currentY + 4F);

        if (formState.statusMessage != null) {
            Color statusColor = formState.statusError ? palette.getMaterialRed(220) : palette.getFontColor(ColorType.NORMAL);
            nvg.drawText(formState.statusMessage, drawX + inset, formY + formHeight - 74, statusColor, 8.5F, Fonts.REGULAR);
        }

        float buttonWidth = (formWidth - (inset * 2F) - 8F) / 2F;
        float buttonHeight = 22F;
        float buttonY = formY + formHeight - 46F;

        cancelButton = new Hitbox(formX + inset, buttonY, buttonWidth, buttonHeight);
        saveButton = new Hitbox(formX + inset + buttonWidth + 8F, buttonY, buttonWidth, buttonHeight);

        nvg.drawRoundedRect(drawX + inset, buttonY, buttonWidth, buttonHeight, 6, palette.getBackgroundColor(ColorType.DARK));
        nvg.drawCenteredText(tx(TranslateText.SKIN_FORM_CANCEL), drawX + inset + (buttonWidth / 2F), buttonY + 6, Color.WHITE, 9.5F, Fonts.MEDIUM);

        Color saveColor = formState.processing ? palette.getFontColor(ColorType.DARK) : ColorUtils.applyAlpha(accent.getColor1(), 220);
        nvg.drawRoundedRect(drawX + inset + buttonWidth + 8F, buttonY, buttonWidth, buttonHeight, 6, saveColor);
        nvg.drawCenteredText(formMode == FormMode.ADD ? tx(TranslateText.SKIN_FORM_ADD_ACTION) : tx(TranslateText.SKIN_FORM_SAVE_ACTION), drawX + inset + buttonWidth + 8F + (buttonWidth / 2F), buttonY + 6, Color.WHITE, 9.5F, Fonts.MEDIUM);
    }

    private void drawSourceChips(int mouseX, int mouseY, NanoVGManager nvg, ColorPalette palette, AccentColor accent, float startX, float y) {
        sourceChipBounds.clear();
        float x = startX;
        for (SkinSource source : SkinSource.values()) {
            String label;
            String icon;
            if (Objects.requireNonNull(source) == SkinSource.USERNAME) {
                label = tx(TranslateText.SKIN_SOURCE_USERNAME);
                icon = LegacyIcon.USER;
            } else {
                label = tx(TranslateText.SKIN_SOURCE_UUID);
                icon = LegacyIcon.KEY;
            }
            float width = CategoryChipRenderer.computeWidth(nvg, label, icon);
            boolean hovered = MouseUtils.isInside(mouseX, mouseY, x, y, width, CategoryChipRenderer.CHIP_HEIGHT);
            boolean active = formState.source == source;
            CategoryChipRenderer.drawChip(nvg, palette, accent, x, y, width, label, icon, active, hovered);
            sourceChipBounds.put(source, new Hitbox(x, y, width, CategoryChipRenderer.CHIP_HEIGHT));
            x += width + 8;
        }
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        disposePreviews();
    }

    @Override
    public void onSceneClosed() {
        super.onSceneClosed();
        disposePreviews();
    }

    private void disposePreviews() {
        NanoVGManager nvg = Shindo.getInstance().getNanoVGManager();
        long vg = nvg != null ? nvg.getContext() : 0;
        previewRenderer.clearCache(vg);
    }

    private void drawTypeChips(int mouseX, int mouseY, NanoVGManager nvg, ColorPalette palette, AccentColor accent, float startX, float y) {
        typeChipBounds.clear();
        float x = startX;
        for (SkinType type : SkinType.values()) {
            String label = type == SkinType.SLIM ? tx(TranslateText.SKIN_TYPE_SLIM) : tx(TranslateText.SKIN_TYPE_DEFAULT);
            float width = CategoryChipRenderer.computeWidth(nvg, label, LegacyIcon.USER);
            boolean hovered = MouseUtils.isInside(mouseX, mouseY, x, y, width, CategoryChipRenderer.CHIP_HEIGHT);
            boolean active = formState.selectedType == type;
            CategoryChipRenderer.drawChip(nvg, palette, accent, x, y, width, label, LegacyIcon.USER, active, hovered);
            typeChipBounds.put(type, new Hitbox(x, y, width, CategoryChipRenderer.CHIP_HEIGHT));
            x += width + 8;
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        ScaledResolution sr = new ScaledResolution(mc);
        int acWidth = 640;
        int acHeight = 370;
        int acX = sr.getScaledWidth() / 2 - (acWidth / 2);
        int acY = sr.getScaledHeight() / 2 - (acHeight / 2);
        float slideDistance = acWidth + CONTENT_SLIDE_EXTRA;
        float transition = Math.max(0F, Math.min(1F, formTransition.getValue()));
        float contentTranslate = -transition * slideDistance;
        float formTranslate = (1F - transition) * slideDistance;
        int logicalMouseX = Math.round(mouseX - acX - contentTranslate);
        int logicalMouseY = mouseY - acY;
        int formMouseX = Math.round(mouseX - acX - formTranslate);
        int formMouseY = mouseY - acY;
        boolean formVisible = isFormTransitionActive(transition);

        if (formVisible) {
            if (formMode != FormMode.HIDDEN) {
                if (mouseButton == 0 && formBounds != null && !formBounds.contains(formMouseX, formMouseY)) {
                    closeForm();
                    return;
                }
                if (handleFormClick(formMouseX, formMouseY, mouseButton)) {
                    return;
                }
            }
            return;
        }

        if (!MouseUtils.isInside(mouseX, mouseY, acX, acY, acWidth, acHeight)
                && !MouseUtils.isInside(mouseX, mouseY, sr.getScaledWidth() - (28 * 4), 6, 22, 22)) {
            introAnimation.setDirection(Direction.BACKWARDS);
            return;
        }

        if (mouseButton == 0) {
            for (Map.Entry<FilterType, Hitbox> entry : filterChipBounds.entrySet()) {
                if (entry.getValue().contains(logicalMouseX, logicalMouseY)) {
                    if (currentFilter != entry.getKey()) {
                        currentFilter = entry.getKey();
                        scroll.resetAll();
                    }
                    return;
                }
            }
            if (resetSelectionButton != null && resetSelectionButton.contains(logicalMouseX, logicalMouseY)) {
                resetSelection();
                return;
            }
            for (CardSlot slot : cardSlots) {
                if (slot.area != null && slot.area.contains(logicalMouseX, logicalMouseY)) {
                    if (slot.addCard) {
                        openAddForm();
                        return;
                    }
                    if (slot.favoriteButton != null && slot.favoriteButton.contains(logicalMouseX, logicalMouseY)) {
                        toggleFavorite(slot.skin);
                        return;
                    }
                    if (slot.editButton != null && slot.editButton.contains(logicalMouseX, logicalMouseY)) {
                        openEditForm(slot.skin);
                        return;
                    }
                    if (slot.deleteButton != null && slot.deleteButton.contains(logicalMouseX, logicalMouseY)) {
                        deleteSkin(slot.skin);
                        return;
                    }
                    if (slot.selectButton != null && slot.selectButton.contains(logicalMouseX, logicalMouseY)) {
                        selectSkin(slot.skin);
                        return;
                    }
                }
            }
        }
    }

    private boolean handleFormClick(int mouseX, int mouseY, int mouseButton) {
        formState.nameField.mouseClicked(mouseX, mouseY, mouseButton);
        formState.usernameField.mouseClicked(mouseX, mouseY, mouseButton);
        formState.uuidField.mouseClicked(mouseX, mouseY, mouseButton);

        if (mouseButton == 0) {
            for (Map.Entry<SkinSource, Hitbox> entry : sourceChipBounds.entrySet()) {
                if (entry.getValue().contains(mouseX, mouseY)) {
                    formState.source = entry.getKey();
                    return true;
                }
            }
            for (Map.Entry<SkinType, Hitbox> entry : typeChipBounds.entrySet()) {
                if (entry.getValue().contains(mouseX, mouseY)) {
                    formState.selectedType = entry.getKey();
                    return true;
                }
            }
            if (cancelButton != null && cancelButton.contains(mouseX, mouseY)) {
                closeForm();
                return true;
            }
            if (!formState.processing && saveButton != null && saveButton.contains(mouseX, mouseY)) {
                handleSubmitForm();
                return true;
            }
        }
        return false;
    }

    private void handleSubmitForm() {
        if (formState.processing) {
            return;
        }
        if (formMode == FormMode.EDIT && formState.getDisplayName().isEmpty()) {
            updateFormStatus(tx(TranslateText.SKIN_STATUS_NAME_REQUIRED), true);
            return;
        }

        SkinManager manager = Shindo.getInstance().getSkinManager();
        if (manager == null) {
            updateFormStatus(tx(TranslateText.SKIN_STATUS_MANAGER_UNAVAILABLE), true);
            return;
        }

        formState.processing = true;
        updateFormStatus(tx(TranslateText.SKIN_STATUS_PROCESSING), false);

        String providedName = formState.getDisplayName();
        SkinSource source = formState.source;
        SkinType selectedType = formState.selectedType;
        String username = formState.getUsername();
        String uuid = formState.getUuid();
        Skin editing = editingSkin;
        FormMode currentMode = formMode;

        Multithreading.runAsync(() -> {
            try {
                if (currentMode == FormMode.ADD) {
                    processAdd(manager, providedName, source, selectedType, username, uuid);
                } else if (editing != null) {
                    processEdit(manager, editing, providedName, source, selectedType, username, uuid);
                }
                mc.addScheduledTask(() -> {
                    formState.processing = false;
                    updateFormStatus(tx(TranslateText.SKIN_STATUS_SAVED), false);
                    closeForm();
                });
            } catch (Exception e) {
                ShindoLogger.error("Skin form error", e);
                mc.addScheduledTask(() -> {
                    formState.processing = false;
                    updateFormStatus(e.getMessage() == null ? tx(TranslateText.SKIN_STATUS_GENERIC_ERROR) : e.getMessage(), true);
                });
            }
        });
    }

    private void processAdd(SkinManager manager, String providedName, SkinSource source, SkinType selectedType, String username, String uuid) throws IOException {
        SkinType type = selectedType == null ? SkinType.DEFAULT : selectedType;
        switch (source) {
            case UUID:
                if (uuid.isEmpty()) {
                    throw new IOException(tx(TranslateText.SKIN_STATUS_UUID_INVALID));
                }
                DownloadedSkin remote = manager.downloadSkinByUuid(uuid);
                String nameFromUuid = providedName.isEmpty() ? uuid.substring(0, Math.min(12, uuid.length())) : providedName;
                manager.addSkin(nameFromUuid, type, false, remote.getImage(), remote.getUuid());
                break;
            case USERNAME:
            default:
                if (username.isEmpty()) {
                    throw new IOException(tx(TranslateText.SKIN_STATUS_USERNAME_INVALID));
                }
                DownloadedSkin downloaded = manager.downloadSkinByUsername(username);
                String nameFromUser = providedName.isEmpty() ? username : providedName;
                manager.addSkin(nameFromUser, type, false, downloaded.getImage(), downloaded.getUuid());
                break;
        }
    }

    private void processEdit(SkinManager manager, Skin skin, String newName, SkinSource source, SkinType selectedType, String username, String uuid) throws IOException {
        BufferedImage replacement = null;
        String profileUuid = null;
        switch (source) {
            case UUID:
                if (!uuid.isEmpty()) {
                    DownloadedSkin remote = manager.downloadSkinByUuid(uuid);
                    replacement = remote.getImage();
                    profileUuid = remote.getUuid();
                }
                break;
            case USERNAME:
                if (!username.isEmpty()) {
                    DownloadedSkin downloaded = manager.downloadSkinByUsername(username);
                    replacement = downloaded.getImage();
                    profileUuid = downloaded.getUuid();
                }
                break;
        }
        String finalName = newName.isEmpty() ? skin.getName() : newName;
        SkinType type = selectedType == null ? skin.getType() : selectedType;
        manager.updateSkin(skin, finalName, type, replacement, profileUuid);
    }

    private void updateFormStatus(String message, boolean error) {
        formState.statusMessage = message;
        formState.statusError = error;
    }

    private void openAddForm() {
        formState.resetForAdd();
        editingSkin = null;
        formMode = FormMode.ADD;
    }

    private void openEditForm(Skin skin) {
        if (skin == null) {
            return;
        }
        formState.resetForEdit(skin);
        editingSkin = skin;
        formMode = FormMode.EDIT;
    }

    private void closeForm() {
        formMode = FormMode.HIDDEN;
        editingSkin = null;
        formState.resetForAdd();
    }

    private void selectSkin(Skin skin) {
        Shindo.getInstance().getSkinManager().setCurrentSkin(skin);
        Shindo.getInstance().getNotificationManager().post(tx(TranslateText.SKIN_NOTIFICATION_TITLE), String.format(Locale.ROOT, tx(TranslateText.SKIN_NOTIFICATION_SELECTED), skin.getName()), NotificationType.SUCCESS);
    }

    private void toggleFavorite(Skin skin) {
        Shindo.getInstance().getSkinManager().setFavorite(skin, !skin.isFavorite());
    }

    private void deleteSkin(Skin skin) {
        Shindo.getInstance().getSkinManager().deleteSkin(skin);
        Shindo.getInstance().getNotificationManager().post(tx(TranslateText.SKIN_NOTIFICATION_TITLE), tx(TranslateText.SKIN_NOTIFICATION_REMOVED), NotificationType.WARNING);
    }

    private void resetSelection() {
        Shindo.getInstance().getSkinManager().clearCurrentSkin();
        Shindo.getInstance().getNotificationManager().post(tx(TranslateText.SKIN_NOTIFICATION_TITLE), tx(TranslateText.SKIN_NOTIFICATION_RESET), NotificationType.INFO);
    }

    private boolean isFormTransitionActive(float transition) {
        return formMode != FormMode.HIDDEN || transition > 0.01F;
    }

    private boolean isFormTransitionActive() {
        return formMode != FormMode.HIDDEN || formTransition.getValue() > 0.01F;
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (isFormTransitionActive()) {
            if (formMode != FormMode.HIDDEN) {
                formState.nameField.keyTyped(typedChar, keyCode);
                formState.usernameField.keyTyped(typedChar, keyCode);
                formState.uuidField.keyTyped(typedChar, keyCode);
                if (keyCode == Keyboard.KEY_ESCAPE) {
                    closeForm();
                }
            }
            return;
        }
        if (keyCode == Keyboard.KEY_ESCAPE) {
            introAnimation.setDirection(Direction.BACKWARDS);
        }
        scroll.onKey(keyCode);
    }

    private static String tx(TranslateText text) {
        return text.getText();
    }

    private enum FilterType {
        ALL,
        FAVORITES
    }

    private enum FormMode {
        HIDDEN,
        ADD,
        EDIT
    }

    private enum SkinSource {
        USERNAME,
        UUID
    }

    private static class Hitbox {
        final float x;
        final float y;
        final float width;
        final float height;

        Hitbox(float x, float y, float width, float height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        boolean contains(int mouseX, int mouseY) {
            return MouseUtils.isInside(mouseX, mouseY, x, y, width, height);
        }
    }

    private static class CardSlot {
        final Skin skin;
        final boolean addCard;
        Hitbox area;
        Hitbox selectButton;
        Hitbox favoriteButton;
        Hitbox editButton;
        Hitbox deleteButton;

        CardSlot(Skin skin, boolean addCard) {
            this.skin = skin;
            this.addCard = addCard;
        }

        static CardSlot createAdd(Hitbox hitbox) {
            CardSlot slot = new CardSlot(null, true);
            slot.area = hitbox;
            return slot;
        }
    }

    private static class SkinFormState {
        private final CompMainMenuTextBox nameField = new CompMainMenuTextBox();
        private final CompMainMenuTextBox usernameField = new CompMainMenuTextBox();
        private final CompMainMenuTextBox uuidField = new CompMainMenuTextBox();
        private SkinSource source = SkinSource.USERNAME;
        private SkinType selectedType = SkinType.DEFAULT;
        private boolean processing;
        private String statusMessage;
        private boolean statusError;

        SkinFormState() {
            applyPlaceholders();
        }

        void resetForAdd() {
            applyPlaceholders();
            nameField.setText("");
            usernameField.setText("");
            uuidField.setText("");
            source = SkinSource.USERNAME;
            selectedType = SkinType.DEFAULT;
            processing = false;
            statusMessage = null;
            statusError = false;
        }

        void resetForEdit(Skin skin) {
            applyPlaceholders();
            nameField.setText(skin.getName());
            usernameField.setText("");
            uuidField.setText(skin.getProfileUuid() == null ? "" : skin.getProfileUuid());
            selectedType = skin.getType();
            source = skin.getProfileUuid() == null ? SkinSource.USERNAME : SkinSource.UUID;
            processing = false;
            statusMessage = null;
            statusError = false;
        }

        String getDisplayName() {
            return nameField.getText().trim();
        }

        String getUsername() {
            return usernameField.getText().trim();
        }

        String getUuid() {
            return uuidField.getText().trim();
        }

        private void applyPlaceholders() {
            nameField.setEmptyText(LegacyIcon.PENCIL, TranslateText.SKIN_FIELD_NAME_PLACEHOLDER.getText());
            usernameField.setEmptyText(LegacyIcon.USER, TranslateText.SKIN_FIELD_USERNAME_PLACEHOLDER.getText());
            uuidField.setEmptyText(LegacyIcon.KEY, TranslateText.SKIN_FIELD_UUID_PLACEHOLDER.getText());
        }
    }
}

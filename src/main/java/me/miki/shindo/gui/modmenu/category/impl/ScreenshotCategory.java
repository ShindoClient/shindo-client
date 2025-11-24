package me.miki.shindo.gui.modmenu.category.impl;

import me.miki.shindo.Shindo;
import me.miki.shindo.gui.modmenu.GuiModMenu;
import me.miki.shindo.gui.modmenu.category.Category;
import me.miki.shindo.management.color.AccentColor;
import me.miki.shindo.management.color.ColorManager;
import me.miki.shindo.management.color.palette.ColorPalette;
import me.miki.shindo.management.color.palette.ColorType;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.impl.InternalSettingsMod;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.management.nanovg.font.Fonts;
import me.miki.shindo.management.nanovg.font.LegacyIcon;
import me.miki.shindo.management.screenshot.Screenshot;
import me.miki.shindo.management.screenshot.ScreenshotDisplayMode;
import me.miki.shindo.management.screenshot.ScreenshotManager;
import me.miki.shindo.utils.ColorUtils;
import me.miki.shindo.utils.animation.simple.SimpleAnimation;
import me.miki.shindo.utils.mouse.MouseUtils;
import me.miki.shindo.utils.mouse.Scroll;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.io.IOException;

public class ScreenshotCategory extends Category {

    private static final int GRID_COLUMNS = 3;
    private static final float GRID_SPACING = 18F;
    private static final float GRID_CAPTION_HEIGHT = 18F;

    private final SimpleAnimation leftAnimation = new SimpleAnimation();
    private final SimpleAnimation rightAnimation = new SimpleAnimation();
    private final SimpleAnimation trashAnimation = new SimpleAnimation();
    private final SimpleAnimation backAnimation = new SimpleAnimation();
    private final Scroll filmstripScroll = new Scroll();
    private final Scroll gridScroll = new Scroll();

    private final Bounds previewBounds = new Bounds();
    private final Bounds trashBounds = new Bounds();
    private final Bounds leftButtonBounds = new Bounds();
    private final Bounds rightButtonBounds = new Bounds();
    private final Bounds filmstripBarBounds = new Bounds();
    private final Bounds gridBackBounds = new Bounds();
    private final Bounds gridAreaBounds = new Bounds();

    private Screenshot currentScreenshot;
    private boolean gridPreviewActive;
    private boolean gridListVisible;
    private float gridCardWidth;
    private float gridCardHeight;

    public ScreenshotCategory(GuiModMenu parent) {
        super(parent, TranslateText.SCREENSHOT, LegacyIcon.CAMERA, false, true);
    }

    @Override
    public void initCategory() {
        scroll.resetAll();
        filmstripScroll.resetAll();
        gridScroll.resetAll();
        gridPreviewActive = false;
    }

    @Override
    public void initGui() {
        ScreenshotManager screenshotManager = Shindo.getInstance().getScreenshotManager();
        if (currentScreenshot == null && !screenshotManager.getScreenshots().isEmpty()) {
            currentScreenshot = screenshotManager.getScreenshots().get(0);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        Shindo instance = Shindo.getInstance();
        NanoVGManager nvg = instance.getNanoVGManager();
        ScreenshotManager screenshotManager = instance.getScreenshotManager();
        ColorManager colorManager = instance.getColorManager();
        ColorPalette palette = colorManager.getPalette();
        AccentColor accentColor = colorManager.getCurrentColor();
        ScreenshotDisplayMode displayMode = InternalSettingsMod.getInstance().getScreenshotDisplayMode();

        screenshotManager.loadScreenshots();
        ensureSelection(screenshotManager);

        if (displayMode != ScreenshotDisplayMode.GRID) {
            gridPreviewActive = false;
        }

        resetInteractiveBounds();

        if (screenshotManager.getScreenshots().isEmpty()) {
            hideNavigationButtons();
            drawEmptyState(nvg, palette);
            return;
        }

        if (displayMode == ScreenshotDisplayMode.GRID) {
            drawGridMode(nvg, palette, accentColor, screenshotManager, mouseX, mouseY);
        } else {
            drawFilmstripMode(nvg, palette, accentColor, screenshotManager, mouseX, mouseY);
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        ScreenshotManager screenshotManager = Shindo.getInstance().getScreenshotManager();
        if (screenshotManager.getScreenshots().isEmpty()) {
            return;
        }

        ScreenshotDisplayMode displayMode = InternalSettingsMod.getInstance().getScreenshotDisplayMode();

        if (currentScreenshot != null && trashBounds.contains(mouseX, mouseY) && mouseButton == 0) {
            deleteCurrentScreenshot(screenshotManager);
            return;
        }

        boolean consumed = displayMode == ScreenshotDisplayMode.GRID
                ? handleGridClick(screenshotManager, mouseX, mouseY, mouseButton)
                : handleFilmstripClick(screenshotManager, mouseX, mouseY, mouseButton);

        if (consumed) {
            return;
        }

        if (mouseButton == 0 && currentScreenshot != null && previewBounds.contains(mouseX, mouseY) && !trashBounds.contains(mouseX, mouseY) && !gridBackBounds.contains(mouseX, mouseY)) {
            openCurrentScreenshot();
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        ScreenshotManager screenshotManager = Shindo.getInstance().getScreenshotManager();
        if (currentScreenshot == null || screenshotManager.getScreenshots().isEmpty()) {
            return;
        }
        if (keyCode == Keyboard.KEY_LEFT) {
            currentScreenshot = screenshotManager.getBackScreenshot(currentScreenshot);
        }
        if (keyCode == Keyboard.KEY_RIGHT) {
            currentScreenshot = screenshotManager.getNextScreenshot(currentScreenshot);
        }
    }

    private void drawFilmstripMode(NanoVGManager nvg, ColorPalette palette, AccentColor accentColor, ScreenshotManager screenshotManager, int mouseX, int mouseY) {

        float paddingX = 42F;
        float paddingY = 12F;
        float previewX = this.getX() + paddingX;
        float previewY = this.getY() + paddingY;
        float previewWidth = this.getWidth() - (paddingX * 2F);
        float previewHeight = this.getHeight() - (paddingY * 2F) - 38F;

        drawScreenshotPreview(nvg, palette, currentScreenshot, previewX, previewY, previewWidth, previewHeight, mouseX, mouseY);

        float barPaddingX = 58F;
        float barHeight = 30F;
        float barX = this.getX() + barPaddingX;
        float barY = this.getY() + this.getHeight() - 40F;
        float barWidth = this.getWidth() - (barPaddingX * 2F);
        filmstripBarBounds.set(barX, barY, barWidth, barHeight);

        Color barColor = palette.getBackgroundColor(ColorType.DARK);
        nvg.drawRoundedRect(barX, barY, barWidth, barHeight, 6F, barColor);

        int count = screenshotManager.getScreenshots().size();

        float thumbWidth = 36F;
        float thumbHeight = 22F;
        float step = thumbWidth + 6F;
        float offsetX = 0F;
        float thumbY = barY + (barHeight - thumbHeight) / 2F;
        float visibleWidth = Math.max(0F, barWidth - 8F);
        float contentWidth = count * step;
        filmstripScroll.setMaxScroll(Math.max(0F, contentWidth - visibleWidth));

        if (MouseUtils.isInside(mouseX, mouseY, barX, barY, barWidth, barHeight)) {
            filmstripScroll.onScroll();
        }
        filmstripScroll.onAnimation();
        float scrollValue = filmstripScroll.getValue();

        nvg.save();
        nvg.scissor(barX, barY, barWidth, barHeight);


        for (Screenshot screenshot : screenshotManager.getScreenshots()) {
            float x = barX + 4F + offsetX + scrollValue;
            if (x + thumbWidth > barX - 4F && x < barX + barWidth + 4F) {
                nvg.drawRoundedRect(x, thumbY, thumbWidth, thumbHeight, 6F, palette.getBackgroundColor(ColorType.NORMAL));
                nvg.save();
                nvg.intersectScissor(x + 1F, thumbY + 1F, thumbWidth - 2F, thumbHeight - 2F);
                drawThumbnailImage(nvg, screenshot, x, thumbY, thumbWidth, thumbHeight);
                nvg.restore();

                screenshot.getSelectAnimation().setAnimation(currentScreenshot.equals(screenshot) ? 1F : 0F, 16);
                int alpha = (int) (screenshot.getSelectAnimation().getValue() * 255);
                if (alpha > 0) {
                    nvg.drawGradientOutlineRoundedRect(x, thumbY, thumbWidth, thumbHeight, 6F, screenshot.getSelectAnimation().getValue() * 1.2F, ColorUtils.applyAlpha(accentColor.getColor1(), alpha), ColorUtils.applyAlpha(accentColor.getColor2(), alpha));
                }
            }
            offsetX += step;
        }

        nvg.restore();

        drawNavigationButtons(nvg, palette, mouseX, mouseY, screenshotManager.getScreenshots().size() > 1);
    }

    private void drawGridMode(NanoVGManager nvg, ColorPalette palette, AccentColor accentColor, ScreenshotManager screenshotManager, int mouseX, int mouseY) {
        hideNavigationButtons();
        filmstripBarBounds.clear();
        gridListVisible = false;

        if (gridPreviewActive && currentScreenshot != null) {
            drawGridPreview(nvg, palette, currentScreenshot, mouseX, mouseY);
            return;
        }

        gridBackBounds.clear();
        float areaX = this.getX() + 20F;
        float areaY = this.getY() + 20F;
        float areaWidth = this.getWidth() - 40F;
        float areaHeight = this.getHeight() - 60F;
        gridAreaBounds.set(areaX, areaY, areaWidth, areaHeight);
        gridListVisible = true;

        Color frameColor = ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 220);
        nvg.drawRoundedRect(areaX - 8F, areaY - 8F, areaWidth + 16F, areaHeight + 16F, 14F, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 150));
        nvg.drawRoundedRect(areaX, areaY, areaWidth, areaHeight, 12F, frameColor);

        int count = screenshotManager.getScreenshots().size();
        gridCardWidth = ((areaWidth - GRID_SPACING * (GRID_COLUMNS - 1)) / GRID_COLUMNS) - 15F;
        gridCardHeight = gridCardWidth * 9F / 16F;
        int rows = (int) Math.ceil(count / (float) GRID_COLUMNS);
        float cellHeight = gridCardHeight + GRID_CAPTION_HEIGHT;
        float contentHeight = rows * cellHeight + Math.max(0, rows - 1) * GRID_SPACING;
        gridScroll.setMaxScroll(Math.max(0F, contentHeight - areaHeight));

        if (MouseUtils.isInside(mouseX, mouseY, areaX, areaY, areaWidth, areaHeight)) {
            gridScroll.onScroll();
        }
        gridScroll.onAnimation();
        float scrollValue = gridScroll.getValue();

        nvg.save();
        nvg.scissor(areaX, areaY, areaWidth, areaHeight);

        int index = 0;
        for (Screenshot screenshot : screenshotManager.getScreenshots()) {
            int column = index % GRID_COLUMNS;
            int row = index / GRID_COLUMNS;
            float cardX = areaX + column * (gridCardWidth + GRID_SPACING) + 20F;
            float cardY = areaY + row * (cellHeight + GRID_SPACING) + scrollValue + 10F;
            String label = nvg.getLimitText(screenshot.getName(), 8.5F, Fonts.REGULAR, gridCardWidth);

            nvg.drawRoundedRect(cardX, cardY, gridCardWidth, gridCardHeight + 8F + nvg.getTextHeight(label, 8.5F, Fonts.REGULAR), 8F, ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.MID), 210));
            nvg.drawRoundedImage(screenshot.getImage(), cardX, cardY, gridCardWidth, gridCardHeight, 8F);

            screenshot.getSelectAnimation().setAnimation(currentScreenshot.equals(screenshot) ? 1F : 0F, 18);
            float selected = screenshot.getSelectAnimation().getValue();
            if (selected > 0F) {
                nvg.drawGradientOutlineRoundedRect(cardX, cardY, gridCardWidth, gridCardHeight, 8F, selected * 1.4F, ColorUtils.applyAlpha(accentColor.getColor1(), (int) (selected * 220)), ColorUtils.applyAlpha(accentColor.getColor2(), (int) (selected * 220)));
            }


            nvg.drawCenteredText(label, cardX + (gridCardWidth / 2F), cardY + gridCardHeight + 8F, palette.getFontColor(ColorType.NORMAL), 8.5F, Fonts.REGULAR);

            index++;
        }

        nvg.restore();
    }

    private void drawScreenshotPreview(NanoVGManager nvg, ColorPalette palette, Screenshot screenshot, float x, float y, float width, float height, int mouseX, int mouseY) {
        previewBounds.set(x, y, width, height);
        if (screenshot == null) {
            trashBounds.clear();
            nvg.drawRoundedRect(x, y, width, height, 8F, palette.getBackgroundColor(ColorType.DARK));
            return;
        }

        trashAnimation.setAnimation(MouseUtils.isInside(mouseX, mouseY, x, y, width, height) ? 1F : 0F, 16);
        nvg.drawRoundedImage(screenshot.getImage(), x, y, width, height, 8F);

        float trashSize = 16F;
        float trashX = x + width - trashSize - 8F;
        float trashY = y + 8F;
        trashBounds.set(trashX - 2F, trashY - 2F, trashSize + 4F, trashSize + 4F);

        nvg.drawText(LegacyIcon.TRASH, trashX, trashY, palette.getMaterialRed((int) (trashAnimation.getValue() * 255)), 12F, Fonts.LEGACYICON);
    }

    private void drawGridPreview(NanoVGManager nvg, ColorPalette palette, Screenshot screenshot, int mouseX, int mouseY) {
        float previewX = this.getX() + 48F;
        float previewY = this.getY() + 28F;
        float previewWidth = this.getWidth() - 96F;
        float previewHeight = this.getHeight() - 72F;

        drawScreenshotPreview(nvg, palette, screenshot, previewX, previewY, previewWidth, previewHeight, mouseX, mouseY);

        backAnimation.setAnimation(MouseUtils.isInside(mouseX, mouseY, previewX, previewY, previewWidth, previewHeight) ? 1F : 0F, 16);

        float backSize = 16F;
        float backX = previewX + 8F;
        float backY = previewY + 8F;
        gridBackBounds.set(backX - 2F, backY - 2F, backSize + 4F, backSize + 4F);

        //nvg.drawRoundedRect(backX, backY, backWidth, backHeight, 8F, palette.getBackgroundColor(ColorType.NORMAL));
        nvg.drawText(LegacyIcon.BACK, backX, backY, palette.getFontColor(ColorType.DARK, (int) (backAnimation.getValue() * 255)), 12F, Fonts.LEGACYICON);
    }

    private void drawNavigationButtons(NanoVGManager nvg, ColorPalette palette, int mouseX, int mouseY, boolean visible) {
        if (!visible) {
            hideNavigationButtons();
            return;
        }

        float buttonWidth = 12F;
        float buttonHeight = 24F;
        float baseY = this.getY() + (this.getHeight() / 2F) - 30.5F;

        leftButtonBounds.set(this.getX() + 20F, baseY, buttonWidth, buttonHeight);
        rightButtonBounds.set(this.getX() + this.getWidth() - 32F, baseY, buttonWidth, buttonHeight);

        boolean leftHovered = leftButtonBounds.contains(mouseX, mouseY);
        boolean rightHovered = rightButtonBounds.contains(mouseX, mouseY);
        leftAnimation.setAnimation(leftHovered ? 1F : 0F, 16);
        rightAnimation.setAnimation(rightHovered ? 1F : 0F, 16);

        float leftValue = leftAnimation.getValue();
        float rightValue = rightAnimation.getValue();

        nvg.save();
        nvg.translate(10 - (leftValue * 10F), 0);
        nvg.drawRoundedRect(leftButtonBounds.x, leftButtonBounds.y, buttonWidth, buttonHeight, 4F, palette.getBackgroundColor(ColorType.DARK, (int) (leftValue * 255)));
        nvg.drawText(LegacyIcon.CHEVRON_LEFT, leftButtonBounds.x + 2F, leftButtonBounds.y + 8F, palette.getFontColor(ColorType.DARK, (int) (leftValue * 255)), 10F, Fonts.LEGACYICON);
        nvg.restore();

        nvg.save();
        nvg.translate(-10 + (rightValue * 10F), 0);
        nvg.drawRoundedRect(rightButtonBounds.x, rightButtonBounds.y, buttonWidth, buttonHeight, 4F, palette.getBackgroundColor(ColorType.DARK, (int) (rightValue * 255)));
        nvg.drawText(LegacyIcon.CHEVRON_RIGHT, rightButtonBounds.x + 2F, rightButtonBounds.y + 8F, palette.getFontColor(ColorType.DARK, (int) (rightValue * 255)), 10F, Fonts.LEGACYICON);
        nvg.restore();
    }

    private void hideNavigationButtons() {
        leftButtonBounds.clear();
        rightButtonBounds.clear();
        leftAnimation.setValue(0F);
        rightAnimation.setValue(0F);
    }

    private boolean handleFilmstripClick(ScreenshotManager screenshotManager, int mouseX, int mouseY, int mouseButton) {
        if (mouseButton != 0) {
            return false;
        }

        if (handleNavigationButtonsClick(screenshotManager, mouseX, mouseY)) {
            return true;
        }

        Screenshot target = findFilmstripTargetAt(mouseX, mouseY, screenshotManager);
        if (target != null) {
            currentScreenshot = target;
            return true;
        }
        return false;
    }

    private boolean handleGridClick(ScreenshotManager screenshotManager, int mouseX, int mouseY, int mouseButton) {
        if (mouseButton != 0) {
            return false;
        }

        if (gridPreviewActive) {
            if (gridBackBounds.contains(mouseX, mouseY)) {
                gridPreviewActive = false;
                return true;
            }
            return handleNavigationButtonsClick(screenshotManager, mouseX, mouseY);
        }

        Screenshot target = findGridTargetAt(mouseX, mouseY, screenshotManager);
        if (target != null) {
            currentScreenshot = target;
            gridPreviewActive = true;
            return true;
        }
        return false;
    }

    private Screenshot findGridTargetAt(int mouseX, int mouseY, ScreenshotManager screenshotManager) {
        if (!gridListVisible || !gridAreaBounds.contains(mouseX, mouseY)) {
            return null;
        }
        float scrollValue = gridScroll.getValue();
        float cellHeight = gridCardHeight + GRID_CAPTION_HEIGHT + GRID_SPACING;
        int index = 0;
        for (Screenshot screenshot : screenshotManager.getScreenshots()) {
            int column = index % GRID_COLUMNS;
            int row = index / GRID_COLUMNS;
            float cardX = gridAreaBounds.x + column * (gridCardWidth + GRID_SPACING);
            float cardY = gridAreaBounds.y + row * cellHeight + scrollValue;
            if (MouseUtils.isInside(mouseX, mouseY, cardX, cardY, gridCardWidth, gridCardHeight)) {
                return screenshot;
            }
            index++;
        }
        return null;
    }

    private Screenshot findFilmstripTargetAt(int mouseX, int mouseY, ScreenshotManager screenshotManager) {
        if (!filmstripBarBounds.contains(mouseX, mouseY)) {
            return null;
        }

        float scrollValue = filmstripScroll.getValue();
        float thumbWidth = 36F;
        float thumbHeight = 22F;
        float step = thumbWidth + 6F;
        float thumbY = filmstripBarBounds.y + (filmstripBarBounds.height - thumbHeight) / 2F;
        float offsetX = 0F;
        for (Screenshot screenshot : screenshotManager.getScreenshots()) {
            float x = filmstripBarBounds.x + 4F + offsetX + scrollValue;
            if (MouseUtils.isInside(mouseX, mouseY, x, thumbY, thumbWidth, thumbHeight)) {
                return screenshot;
            }
            offsetX += step;
        }
        return null;
    }

    private boolean handleNavigationButtonsClick(ScreenshotManager screenshotManager, int mouseX, int mouseY) {
        if (currentScreenshot == null || screenshotManager.getScreenshots().size() <= 1) {
            return false;
        }
        if (leftButtonBounds.contains(mouseX, mouseY)) {
            currentScreenshot = screenshotManager.getBackScreenshot(currentScreenshot);
            return true;
        }
        if (rightButtonBounds.contains(mouseX, mouseY)) {
            currentScreenshot = screenshotManager.getNextScreenshot(currentScreenshot);
            return true;
        }
        return false;
    }

    private void drawEmptyState(NanoVGManager nvg, ColorPalette palette) {
        float paddingX = 42F;
        float paddingY = 12F;
        float width = this.getWidth() - (paddingX * 2F);
        float height = this.getHeight() - (paddingY * 2F) - 38F;
        float x = this.getX() + paddingX;
        float y = this.getY() + paddingY;

        nvg.drawRoundedRect(x, y, width, height, 6F, palette.getBackgroundColor(ColorType.DARK));
        nvg.drawCenteredText(LegacyIcon.CAMERA, x + width / 2F, y + 56F, palette.getFontColor(ColorType.NORMAL), 64F, Fonts.LEGACYICON);

        float barX = this.getX() + 58F;
        float barWidth = this.getWidth() - (58F * 2F);
        nvg.drawRoundedRect(barX, this.getY() + this.getHeight() - 40F, barWidth, 30F, 6F, palette.getBackgroundColor(ColorType.DARK));
    }

    private void drawThumbnailImage(NanoVGManager nvg, Screenshot screenshot, float x, float y, float width, float height) {
        float padding = 1F;
        float availableWidth = width - (padding * 2F);
        float availableHeight = height - (padding * 2F);
        float aspect = 16F / 9F;
        float drawWidth = availableWidth;
        float drawHeight = drawWidth / aspect;
        if (drawHeight < availableHeight) {
            drawHeight = availableHeight;
            drawWidth = drawHeight * aspect;
        }
        float drawX = x + (width - drawWidth) / 2F;
        float drawY = y + (height - drawHeight) / 2F;

        nvg.drawImage(screenshot.getImage(), drawX, drawY, drawWidth, drawHeight);
    }

    private void ensureSelection(ScreenshotManager screenshotManager) {
        if (currentScreenshot != null && !screenshotManager.getScreenshots().contains(currentScreenshot)) {
            currentScreenshot = null;
        }
        if (currentScreenshot == null && !screenshotManager.getScreenshots().isEmpty()) {
            currentScreenshot = screenshotManager.getScreenshots().get(0);
        }
        if (currentScreenshot == null) {
            gridPreviewActive = false;
        }
    }

    private void deleteCurrentScreenshot(ScreenshotManager screenshotManager) {
        if (currentScreenshot == null) {
            return;
        }

        int index = screenshotManager.getScreenshots().indexOf(currentScreenshot);
        screenshotManager.delete(currentScreenshot);
        if (screenshotManager.getScreenshots().isEmpty()) {
            currentScreenshot = null;
            gridPreviewActive = false;
            return;
        }
        index = Math.max(0, Math.min(index - 1, screenshotManager.getScreenshots().size() - 1));
        currentScreenshot = screenshotManager.getScreenshots().get(index);
    }

    private void openCurrentScreenshot() {
        if (currentScreenshot == null) {
            return;
        }
        try {
            Desktop.getDesktop().open(currentScreenshot.getImage());
        } catch (IOException ignored) {
        }
    }

    private void resetInteractiveBounds() {
        previewBounds.clear();
        trashBounds.clear();
        filmstripBarBounds.clear();
        leftButtonBounds.clear();
        rightButtonBounds.clear();
        gridBackBounds.clear();
        gridAreaBounds.clear();
        gridListVisible = false;
    }

    private static final class Bounds {
        float x;
        float y;
        float width;
        float height;

        void set(float x, float y, float width, float height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        boolean contains(int mouseX, int mouseY) {
            return width > 0F && height > 0F && MouseUtils.isInside(mouseX, mouseY, x, y, width, height);
        }

        void clear() {
            set(0F, 0F, 0F, 0F);
        }
    }
}

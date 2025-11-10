package me.miki.shindo.gui.modmenu;

import eu.shoroa.contrib.cosmetic.CosmeticManager;
import eu.shoroa.contrib.render.ShBlur;
import me.miki.shindo.Shindo;
import me.miki.shindo.gui.GuiEditHUD;
import me.miki.shindo.gui.modmenu.category.Category;
import me.miki.shindo.gui.modmenu.category.impl.*;
import me.miki.shindo.management.color.AccentColor;
import me.miki.shindo.management.color.ColorManager;
import me.miki.shindo.management.color.palette.ColorPalette;
import me.miki.shindo.management.color.palette.ColorType;
import me.miki.shindo.management.event.impl.EventRenderNotification;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.impl.InternalSettingsMod;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.management.nanovg.font.Fonts;
import me.miki.shindo.management.nanovg.font.LegacyIcon;
import me.miki.shindo.ui.comp.impl.CompIconButton;
import me.miki.shindo.ui.comp.impl.field.CompSearchBox;
import me.miki.shindo.utils.MathUtils;
import me.miki.shindo.utils.animation.normal.Animation;
import me.miki.shindo.utils.animation.normal.Direction;
import me.miki.shindo.utils.animation.normal.easing.EaseBackIn;
import me.miki.shindo.utils.animation.simple.SimpleAnimation;
import me.miki.shindo.utils.buffer.ScreenAnimation;
import me.miki.shindo.utils.file.FileUtils;
import me.miki.shindo.utils.mouse.MouseUtils;
import me.miki.shindo.utils.mouse.Scroll;
import me.miki.shindo.utils.render.BlurUtils;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class GuiModMenu extends GuiScreen {

    private final ArrayList<Category> categories = new ArrayList<Category>();
    private final SimpleAnimation moveAnimation = new SimpleAnimation();
    private final ScreenAnimation screenAnimation = new ScreenAnimation();
    private final Scroll scroll = new Scroll();
    private final CompSearchBox searchBox = new CompSearchBox();
    private final CompIconButton layoutButton = new CompIconButton(21F, () -> LegacyIcon.LAYOUT);
    private final CompIconButton folderButton = new CompIconButton(18F, () -> LegacyIcon.FOLDER);
    private Animation introAnimation;
    private int x, y, width, height;
    private Category currentCategory;
    private boolean toEditHUD, canClose;

    public GuiModMenu() {

        categories.add(new HomeCategory(this));
        categories.add(new ModuleCategory(this));
        categories.add(new AddonCategory(this));
        categories.add(new CosmeticsCategory(this));
        categories.add(new SpotifyCategory(this));
        //categories.add(new GamesCategory(this));
        categories.add(new ProfileCategory(this));
        categories.add(new ScreenshotCategory(this));
        categories.add(new TweakerCategory(this));
        categories.add(new SettingCategory(this));

        currentCategory = getCategoryByClass(HomeCategory.class);
    }

    @Override
    public void initGui() {

        ScaledResolution sr = new ScaledResolution(mc);

        int addX = 225;
        int addY = 140;

        x = (sr.getScaledWidth() / 2) - addX;
        y = (sr.getScaledHeight() / 2) - addY;
        width = addX * 2;
        height = addY * 2;

        introAnimation = new EaseBackIn(320, 1.0F, 2.0F);
        introAnimation.setDirection(Direction.FORWARDS);

        for (Category c : categories) {
            c.initGui();
        }

        scroll.resetAll();
        toEditHUD = false;
        canClose = true;

        layoutButton.onClick(() -> {
            toEditHUD = true;
            introAnimation.setDirection(Direction.BACKWARDS);
        });
        layoutButton.setFontSize(14F);
        layoutButton.setRadius(6F);
        layoutButton.enabledWhen(() -> canClose);

        folderButton.setRadius(6F);
        folderButton.setFontSize(9F);
        folderButton.setVisible(false);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        CosmeticManager.getInstance().renderFBO();
        Shindo instance = Shindo.getInstance();
        NanoVGManager nvg = instance.getNanoVGManager();

        if (InternalSettingsMod.getInstance().getBlurSetting().isToggled())
            BlurUtils.drawBlurScreen((float) (Math.min(introAnimation.getValue(), 1) * 20) + 1F);
        screenAnimation.wrap(() -> {
            nvg.drawShadow(x, y, width, height, 12);
        }, 2 - introAnimation.getValueFloat(), Math.min(introAnimation.getValueFloat(), 1));

        screenAnimation.wrap(() -> drawNanoVG(mouseX, mouseY, partialTicks), this.getX(), this.getY(), this.getWidth(), this.getHeight(), 2 - introAnimation.getValueFloat(), Math.min(introAnimation.getValueFloat(), 1), true);

        new EventRenderNotification().call();

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawNanoVG(int mouseX, int mouseY, float partialTicks) {

        Shindo instance = Shindo.getInstance();
        NanoVGManager nvg = instance.getNanoVGManager();
        ColorManager colorManager = instance.getColorManager();
        ColorPalette palette = colorManager.getPalette();
        AccentColor currentColor = colorManager.getCurrentColor();

        if (introAnimation.isDone(Direction.BACKWARDS)) {
            mc.displayGuiScreen(toEditHUD ? new GuiEditHUD(true) : null);
        }
        nvg.drawRoundedRect(x, y, width, height, 12, palette.getBackgroundColor(ColorType.NORMAL));

        if (InternalSettingsMod.getInstance().getBlurSetting().isToggled()) {
            ShBlur.getInstance().drawBlur(() -> nvg.drawRoundedRectVarying(x, y, 32, height, 12, 0, 12, 0, palette.getBackgroundColor(ColorType.DARK)));
            Color colsidebar = palette.getBackgroundColor(ColorType.DARK);
            nvg.drawRoundedRectVarying(x, y, 32, height, 12, 0, 12, 0, new Color(colsidebar.getRed(), colsidebar.getGreen(), colsidebar.getBlue(), 210));
        } else {
            nvg.drawRoundedRectVarying(x, y, 32, height, 12, 0, 12, 0, palette.getBackgroundColor(ColorType.DARK));
        }

        nvg.drawGradientRoundedRect(x + 5, y + 7, 22, 22, 11, currentColor.getColor1(), currentColor.getColor2());
        nvg.drawText(LegacyIcon.SHINDO, x + 8, y + 10, Color.WHITE, 16, Fonts.LEGACYICON);
        if (currentCategory.isShowTitle()) {
            nvg.save();
            nvg.translate(currentCategory.getTextAnimation().getValue() * 15, 0);
            nvg.drawText(currentCategory.getName(), x + 32, y + 10, palette.getFontColor(ColorType.DARK, (int) (currentCategory.getTextAnimation().getValue() * 255)), 15, Fonts.SEMIBOLD);
            nvg.restore();
        }

        int offsetY = 0;

        moveAnimation.setAnimation(categories.indexOf(currentCategory) * 22, 18);

        nvg.save();

        nvg.drawGradientRoundedRect(x + 5.5F, y + 34.5F + moveAnimation.getValue(), 21, 21, 5, currentColor.getColor1(), currentColor.getColor2());

        for (Category c : categories) {

            Color textColor = c.getTextColorAnimation().getColor(MathUtils.isInRange(moveAnimation.getValue(), offsetY - 8, offsetY + 8) ? Color.WHITE : palette.getFontColor(ColorType.NORMAL), 18);

            c.getTextAnimation().setAnimation(c.equals(currentCategory) ? 1.0F : 0.0F, 14);

            nvg.drawText(c.getIcon(), x + 9F, y + 38 + offsetY, textColor, 14, Fonts.LEGACYICON);

            offsetY += 22;
        }

        nvg.restore();

        layoutButton.setBounds(x + 5.5F, y + height - 30, 21, 21);
        layoutButton.draw(mouseX, mouseY, partialTicks);

        for (Category c : categories) {

            c.getCategoryAnimation().setAnimation(c.equals(currentCategory) ? 1.0F : 0.0F, 16);

            if (c.equals(currentCategory)) {

                nvg.save();

                if (!c.isInitialized()) {
                    c.setInitialized(true);
                    c.initCategory();
                    searchBox.setText("");
                    c.setCanClose(true);
                }

                if (c.isShowSearchBox()) {
                    searchBox.setPosition(x + width - 175, y + 6.5F, 160, 18);
                    searchBox.draw(mouseX, mouseY, partialTicks);
                }
                int yOff = (currentCategory.isShowTitle()) ? 31 : 0;
                folderButton.setVisible(false);

                if (currentCategory instanceof me.miki.shindo.gui.modmenu.category.impl.CosmeticsCategory) {
                    me.miki.shindo.gui.modmenu.category.impl.CosmeticsCategory cosmeticsCategory = (me.miki.shindo.gui.modmenu.category.impl.CosmeticsCategory) currentCategory;
                    if (cosmeticsCategory.shouldShowCustomCapeFolder()) {
                        float folderButtonX = x + width - 198;
                        float folderButtonY = y + 6.5F;
                        folderButton.setVisible(true);
                        folderButton.setBounds(folderButtonX, folderButtonY, 18F, 18F);
                        folderButton.setIconColorSupplier(() -> palette.getFontColor(ColorType.NORMAL));
                        folderButton.onClick(() -> FileUtils.openFolderAtPath(Shindo.getInstance().getFileManager().getCustomCapeDir()));
                        folderButton.draw(mouseX, mouseY, partialTicks);
                    }
                } else if (Objects.equals(currentCategory.getNameKey(), TranslateText.MUSIC.getKey())) {
                    float folderButtonX = x + width - 198;
                    float folderButtonY = y + 6.5F;
                    folderButton.setVisible(true);
                    folderButton.setBounds(folderButtonX, folderButtonY, 18F, 18F);
                    folderButton.setIconColorSupplier(() -> palette.getFontColor(ColorType.NORMAL));
                    folderButton.onClick(() -> FileUtils.openFolderAtPath(Shindo.getInstance().getFileManager().getMusicDir()));
                    folderButton.draw(mouseX, mouseY, partialTicks);
                }

                nvg.scissor(x + 32, y + yOff, width - 32, height - yOff);
                nvg.translate(0, 50 - (c.getCategoryAnimation().getValue() * 50));

                c.drawScreen(mouseX, mouseY, partialTicks);

                nvg.restore();

            } else if (c.isInitialized()) {
                c.setInitialized(false);
            }
        }

        if (MouseUtils.isInside(mouseX, mouseY, x + 32, y + 31, width - 32, height - 31)) {
            scroll.onScroll();
        }

        scroll.onAnimation();

        if (currentCategory.isShowSearchBox() && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) && Keyboard.isKeyDown(Keyboard.KEY_F)) {
            currentCategory.getSearchBox().setFocused(true);
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        int offsetY = 0;

        // exit gui if not clicked in the gui area
        if (!MouseUtils.isInside(mouseX, mouseY, x - 5, y - 5, width + 10, height + 10) && mouseButton == 0 && canClose) {
            introAnimation.setDirection(Direction.BACKWARDS);
        }

        for (Category c : categories) {

            if (MouseUtils.isInside(mouseX, mouseY, x + 5.5F, y + 34.5F + offsetY, 21, 21) && mouseButton == 0) {
                currentCategory = c;
            }

            offsetY += 22;
        }

        currentCategory.mouseClicked(mouseX, mouseY, mouseButton);
        searchBox.mouseClicked(mouseX, mouseY, mouseButton);

        layoutButton.mouseClicked(mouseX, mouseY, mouseButton);
        folderButton.mouseClicked(mouseX, mouseY, mouseButton);
        try {
            super.mouseClicked(mouseX, mouseY, mouseButton);
        } catch (IOException e) {
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        currentCategory.mouseReleased(mouseX, mouseY, mouseButton);
        layoutButton.mouseReleased(mouseX, mouseY, mouseButton);
        folderButton.mouseReleased(mouseX, mouseY, mouseButton);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {

        currentCategory.keyTyped(typedChar, keyCode);
        searchBox.keyTyped(typedChar, keyCode);

        if (currentCategory.isShowSearchBox() && canClose) {

            if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {

                if (!searchBox.getText().isEmpty()) {
                    searchBox.setText("");
                    searchBox.setFocused(false);
                    return;
                }

                if (searchBox.isFocused()) {
                    searchBox.setFocused(false);
                    return;
                }
            }
        }

        if (keyCode == Keyboard.KEY_ESCAPE && canClose) {
            introAnimation.setDirection(Direction.BACKWARDS);
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void onGuiClosed() {
        Shindo.getInstance().getProfileManager().save();
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public ArrayList<Category> getCategories() {
        return categories;
    }

    public Category getCategoryByClass(Class<?> clazz) {

        for (Category c : categories) {
            if (c.getClass().equals(clazz)) {
                return c;
            }
        }

        return null;
    }

    public Scroll getScroll() {
        return scroll;
    }

    public CompSearchBox getSearchBox() {
        return searchBox;
    }

    public boolean isCanClose() {
        return canClose;
    }

    public void setCanClose(boolean canClose) {
        this.canClose = canClose;
    }
}

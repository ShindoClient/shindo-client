package me.miki.shindo.gui.modmenu.category.impl;

import me.miki.shindo.Shindo;
import me.miki.shindo.gui.modmenu.GuiModMenu;
import me.miki.shindo.gui.modmenu.category.Category;
import me.miki.shindo.management.addons.Addon;
import me.miki.shindo.management.addons.AddonManager;
import me.miki.shindo.management.addons.AddonType;
import me.miki.shindo.management.addons.settings.AddonSetting;
import me.miki.shindo.management.addons.settings.impl.*;
import me.miki.shindo.management.color.AccentColor;
import me.miki.shindo.management.color.ColorManager;
import me.miki.shindo.management.color.palette.ColorPalette;
import me.miki.shindo.management.color.palette.ColorType;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.management.nanovg.font.Fonts;
import me.miki.shindo.management.nanovg.font.LegacyIcon;
import me.miki.shindo.ui.comp.Comp;
import me.miki.shindo.ui.comp.addons.*;
import me.miki.shindo.utils.ColorUtils;
import me.miki.shindo.utils.MathUtils;
import me.miki.shindo.utils.SearchUtils;
import me.miki.shindo.utils.animation.normal.Animation;
import me.miki.shindo.utils.animation.normal.Direction;
import me.miki.shindo.utils.animation.normal.other.SmoothStepAnimation;
import me.miki.shindo.utils.animation.simple.SimpleAnimation;
import me.miki.shindo.utils.mouse.MouseUtils;
import me.miki.shindo.utils.mouse.Scroll;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.ArrayList;

public class AddonCategory extends Category {

    private final Scroll settingScroll = new Scroll();
    private final ArrayList<AddonsSetting> comps = new ArrayList<AddonsSetting>();
    Color noColour = new Color(0, 0, 0, 0);
    private AddonType currentType;
    private boolean openSetting;
    private Animation settingAnimation;
    private Addon currentAddon;


    public AddonCategory(GuiModMenu parent) {
        super(parent, TranslateText.ADDONS, LegacyIcon.PIECE, true, true);

    }

    @Override
    public void initGui() {
        currentType = AddonType.ALL;
        openSetting = false;
        settingAnimation = new SmoothStepAnimation(260, 1.0);
        settingAnimation.setValue(1.0);

    }

    @Override
    public void initCategory() {
        scroll.resetAll();
        openSetting = false;
        settingAnimation = new SmoothStepAnimation(260, 1.0);
        settingAnimation.setValue(1.0);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        Shindo instance = Shindo.getInstance();
        NanoVGManager nvg = instance.getNanoVGManager();
        AddonManager addonManager = instance.getAddonManager();
        ColorManager colorManager = instance.getColorManager();
        ColorPalette palette = colorManager.getPalette();
        AccentColor accentColor = colorManager.getCurrentColor();

        int offsetX = 0;
        float offsetY = 13;
        int index = 1;
        float scrollValue = scroll.getValue();

        settingAnimation.setDirection(openSetting ? Direction.BACKWARDS : Direction.FORWARDS);

        if (settingAnimation.isDone(Direction.FORWARDS)) {
            this.setCanClose(true);
            currentAddon = null;
        }

        nvg.save();
        nvg.translate((float) -(600 - (settingAnimation.getValue() * 600)), 0);

        //Draw addon scene

        nvg.save();
        nvg.translate(0, scrollValue);

        for (AddonType t : AddonType.values()) {

            float textWidth = nvg.getTextWidth(t.getName(), 9, Fonts.MEDIUM);
            boolean isCurrentType = t.equals(currentType);

            t.getBackgroundAnimation().setAnimation(isCurrentType ? 1.0F : 0.0F, 16);

            Color defaultColor = palette.getBackgroundColor(ColorType.DARK);
            Color color1 = ColorUtils.applyAlpha(accentColor.getColor1(), (int) (t.getBackgroundAnimation().getValue() * 255));
            Color color2 = ColorUtils.applyAlpha(accentColor.getColor2(), (int) (t.getBackgroundAnimation().getValue() * 255));
            Color textColor = t.getTextColorAnimation().getColor(isCurrentType ? Color.WHITE : palette.getFontColor(ColorType.DARK), 20);

            nvg.drawRoundedRect(this.getX() + 15 + offsetX, this.getY() + offsetY - 3, textWidth + 20, 16, 6, defaultColor);
            nvg.drawGradientRoundedRect(this.getX() + 15 + offsetX, this.getY() + offsetY - 3, textWidth + 20, 16, 6, color1, color2);

            nvg.drawText(t.getName(), this.getX() + 15 + offsetX + ((textWidth + 20) - textWidth) / 2, this.getY() + offsetY + 1.5F, textColor, 9, Fonts.MEDIUM);

            offsetX += (int) (textWidth + 28);
        }

        offsetX = 0;
        offsetY = offsetY + 23;

        for (Addon a : addonManager.getAddons()) {


            if (filterAddon(a)) {
                continue;
            }

            if (offsetY + scrollValue + 100 > 0 && offsetY + scrollValue < this.getHeight()) {

                nvg.drawRoundedRect(this.getX() + offsetX + 15, this.getY() + offsetY, 105, 120, 4, palette.getBackgroundColor(ColorType.DARK));
                nvg.drawRoundedImage(new ResourceLocation("shindo/logo.png"), this.getX() + offsetX + 20F, this.getY() + offsetY + 10, 95, 95, 4);

                nvg.drawRoundedRect(this.getX() + offsetX + 20, this.getY() + offsetY + 5, 95, 15, 4, palette.getBackgroundColor(ColorType.NORMAL));
                nvg.drawCenteredText(a.getName(), this.getX() + offsetX + 15 + 52.5F, this.getY() + offsetY + 9.5F, palette.getFontColor(ColorType.DARK), 7, Fonts.MEDIUM);

                nvg.drawRoundedRect(this.getX() + offsetX + 20, this.getY() + offsetY + 100, 75F, 15, 4, palette.getBackgroundColor(ColorType.NORMAL));

                a.getAnimation().setAnimation(a.isToggled() ? 1.0F : 0.0F, 16);

                nvg.save();
                nvg.scale(this.getX() + offsetX + 20, this.getY() + offsetY + 100, 75F, 15, a.getAnimation().getValue());
                nvg.drawGradientRoundedRect(this.getX() + offsetX + 20, this.getY() + offsetY + 100, 75F, 15, 4, ColorUtils.applyAlpha(accentColor.getColor1(), (int) (a.getAnimation().getValue() * 255)), ColorUtils.applyAlpha(accentColor.getColor2(), (int) (a.getAnimation().getValue() * 255)));
                nvg.drawCenteredText("Enabled", this.getX() + offsetX + 20 + 37.5F, this.getY() + offsetY + 105, palette.getFontColor(ColorType.NORMAL), 7, Fonts.MEDIUM);
                nvg.restore();
                nvg.drawCenteredText(a.isToggled() ? "" : "Disabled", this.getX() + offsetX + 20 + 37.5F, this.getY() + offsetY + 105, palette.getFontColor(ColorType.NORMAL), 7, Fonts.MEDIUM);

                nvg.drawRoundedRect(this.getX() + offsetX + 100, this.getY() + offsetY + 100, 15, 15, 4, palette.getBackgroundColor(ColorType.NORMAL));

                if (addonManager.getSettingByAddon(a) != null) {
                    nvg.drawCenteredText(LegacyIcon.SETTINGS, this.getX() + offsetX + 107F, this.getY() + offsetY + 101, palette.getFontColor(ColorType.NORMAL), 13, Fonts.LEGACYICON);
                }

            }

            offsetX += 130;

            if (index % 3 == 0) {
                offsetX = 0;
                offsetY += 115;
            }

            index++;
        }

        nvg.restore();
        nvg.drawVerticalGradientRect(getX() + 15, this.getY(), getWidth() - 30, 12, palette.getBackgroundColor(ColorType.NORMAL), noColour); //top
        nvg.drawVerticalGradientRect(getX() + 15, this.getY() + this.getHeight() - 12, getWidth() - 30, 12, noColour, palette.getBackgroundColor(ColorType.NORMAL)); // bottom
        nvg.restore();


        nvg.save();
        nvg.translate((float) (settingAnimation.getValue() * 600), 0);

        if (currentAddon != null) {

            int setIndex = 0;

            if (MouseUtils.isInside(mouseX, mouseY, this.getX(), this.getY(), this.getWidth(), this.getHeight())) {
                settingScroll.onScroll();
                settingScroll.onAnimation();
            }

            offsetY = 15;
            offsetX = 0;

            nvg.save();

            nvg.drawRoundedRect(this.getX() + 15, this.getY() + offsetY, this.getWidth() - 30, this.getHeight() - 30, 10, palette.getBackgroundColor(ColorType.DARK));
            nvg.drawText(LegacyIcon.CHEVRON_LEFT, this.getX() + 25, this.getY() + offsetY + 8, palette.getFontColor(ColorType.DARK), 13, Fonts.LEGACYICON);
            nvg.drawText(currentAddon.getName(), this.getX() + 42, this.getY() + offsetY + 9, palette.getFontColor(ColorType.DARK), 13, Fonts.MEDIUM);
            nvg.drawText(LegacyIcon.REFRESH, this.getX() + this.getWidth() - 39, this.getY() + offsetY + 7.5F, palette.getFontColor(ColorType.DARK), 13, Fonts.LEGACYICON);

            offsetY = 44;

            nvg.scissor(this.getX() + 15, this.getY() + offsetY, this.getWidth() - 30, this.getHeight() - 59);
            nvg.translate(0, settingScroll.getValue());

            for (AddonsSetting s : comps) {

                s.openAnimation.setAnimation(s.openY, 16);

                nvg.drawText(s.setting.getName(), this.getX() + offsetX + 26, this.getY() + offsetY + 15F + s.openAnimation.getValue(), palette.getFontColor(ColorType.DARK), 10, Fonts.MEDIUM);

                if (s.comp instanceof CompToggleButton) {

                    CompToggleButton toggleButton = (CompToggleButton) s.comp;

                    toggleButton.setX(this.getX() + 368);
                    toggleButton.setY(this.getY() + offsetY + 12 + s.openAnimation.getValue());
                    toggleButton.setScale(0.85F);
                }

                if (s.comp instanceof CompSlider) {

                    CompSlider slider = (CompSlider) s.comp;

                    slider.setX(this.getX() + 322);
                    slider.setY(this.getY() + offsetY + 17 + s.openAnimation.getValue());
                    slider.setWidth(75);
                }

                if (s.comp instanceof CompComboBox) {

                    CompComboBox comboBox = (CompComboBox) s.comp;

                    comboBox.setX(this.getX() + 322);
                    comboBox.setY(this.getY() + offsetY + 11 + s.openAnimation.getValue());
                }

                if (s.comp instanceof CompKeybind) {

                    CompKeybind keybind = (CompKeybind) s.comp;

                    keybind.setX(this.getX() + 322);
                    keybind.setY(this.getY() + offsetY + 11 + s.openAnimation.getValue());
                }

                if (s.comp instanceof CompAddonTextBox) {

                    CompAddonTextBox textBox = (CompAddonTextBox) s.comp;

                    textBox.setX(this.getX() + 322);
                    textBox.setY(this.getY() + offsetY + 11 + s.openAnimation.getValue());
                    textBox.setWidth(75);
                    textBox.setHeight(16);
                }

                if (s.comp instanceof CompColorPicker) {

                    CompColorPicker picker = (CompColorPicker) s.comp;

                    picker.setX(this.getX() + 298);
                    picker.setY(this.getY() + offsetY + 12.5F + s.openAnimation.getValue());
                    picker.setScale(0.8F);
                }

                if (s.comp instanceof CompImageSelect) {

                    CompImageSelect imageSelect = (CompImageSelect) s.comp;

                    imageSelect.setX(this.getX() + 381);
                    imageSelect.setY(this.getY() + offsetY + 11 + s.openAnimation.getValue());
                }

                if (s.comp instanceof CompSoundSelect) {

                    CompSoundSelect soundSelect = (CompSoundSelect) s.comp;

                    soundSelect.setX(this.getX() + 381);
                    soundSelect.setY(this.getY() + offsetY + 11 + s.openAnimation.getValue());
                }

                if(s.comp instanceof CompCategory) {
                    CompCategory categorySetting = (CompCategory) s.comp;

                    categorySetting.setX(this.getX() + 20);
                    categorySetting.setY(this.getY() + offsetY + 25 + s.openAnimation.getValue());

                }


                s.comp.draw(mouseX, (int) (mouseY - settingScroll.getValue()), partialTicks);


                offsetY += 29;
            }

            nvg.restore();

            settingScroll.setMaxScroll(this.getAddonSettingHeight());
        }

        nvg.restore();

        int scrollMax = 0;

        if (index > 3) {
            scrollMax += (int) Math.ceil((index - 3) / 3.0);
        }

        scroll.setMaxScroll(scrollMax * 56);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        Shindo instance = Shindo.getInstance();
        NanoVGManager nvg = instance.getNanoVGManager();
        AddonManager addonManager = instance.getAddonManager();

        int index = 1;
        int offsetX = 0;
        float offsetY = 13 + scroll.getValue();

        if (!openSetting) {
        for (AddonType t : AddonType.values()) {

            float textWidth = nvg.getTextWidth(t.getName(), 9, Fonts.MEDIUM);

            if (MouseUtils.isInside(mouseX, mouseY, this.getX() + 15 + offsetX, this.getY() + offsetY - 3, textWidth + 20, 16) && mouseButton == 0) {
                currentType = t;
                scroll.reset();
            }

            offsetX += (int) (textWidth + 28);
        }
            offsetX = 0;
            offsetY = offsetY + 23;

            for (Addon a : addonManager.getAddons()) {

                if (filterAddon(a)) {
                    continue;
                }

                if (MouseUtils.isInside(mouseX, mouseY, this.getX(), this.getY(), this.getWidth(), this.getHeight()) && mouseButton == 0) {
                    if (MouseUtils.isInside(mouseX, mouseY, this.getX() + offsetX + 20 , this.getY() + offsetY + 100, 75F, 15F)) {
                        a.toggle();
                    }


                    if (MouseUtils.isInside(mouseX, mouseY, this.getX() + offsetX + 100, this.getY() + offsetY + 100, 15, 15) && !openSetting) {

                        ArrayList<AddonSetting> settings = addonManager.getSettingByAddon(a);
                        int setIndex = 0;

                        offsetX = 0;
                        offsetY = 44;

                        if (settings != null) {

                            comps.clear();

                            for (AddonSetting s : settings) {

                                if (s instanceof BooleanSetting) {

                                    BooleanSetting bSetting = (BooleanSetting) s;

                                    CompToggleButton toggleButton = new CompToggleButton(bSetting);

                                    toggleButton.setX(this.getX() + 368);
                                    toggleButton.setY(this.getY() + offsetY + 8);
                                    toggleButton.setScale(0.85F);

                                    comps.add(new AddonsSetting(s, toggleButton));
                                }

                                if (s instanceof NumberSetting) {

                                    NumberSetting nSetting = (NumberSetting) s;

                                    CompSlider slider = new CompSlider(nSetting);

                                    slider.setX(this.getX() + 322);
                                    slider.setY(this.getY() + offsetY + 13);
                                    slider.setWidth(75);

                                    comps.add(new AddonsSetting(s, slider));
                                }

                                if (s instanceof ComboSetting) {

                                    ComboSetting cSetting = (ComboSetting) s;

                                    CompComboBox comboBox = new CompComboBox(75, cSetting);

                                    comboBox.setX(this.getX() + 322);
                                    comboBox.setY(this.getY() + offsetY + 11);

                                    comps.add(new AddonsSetting(s, comboBox));
                                }

                                if (s instanceof ImageSetting) {

                                    ImageSetting iSetting = (ImageSetting) s;
                                    CompImageSelect imageSelect = new CompImageSelect(iSetting);

                                    imageSelect.setX(this.getX() + 381);
                                    imageSelect.setY(this.getY() + offsetY + 11);

                                    comps.add(new AddonsSetting(s, imageSelect));
                                }

                                if (s instanceof SoundSetting) {

                                    SoundSetting sSetting = (SoundSetting) s;
                                    CompSoundSelect soundSelect = new CompSoundSelect(sSetting);

                                    soundSelect.setX(this.getX() + 381);
                                    soundSelect.setY(this.getY() + offsetY + 11);

                                    comps.add(new AddonsSetting(s, soundSelect));
                                }

                                if (s instanceof KeybindSetting) {

                                    KeybindSetting kSetting = (KeybindSetting) s;
                                    CompKeybind keybind = new CompKeybind(75, kSetting);

                                    keybind.setX(this.getX() + 322);
                                    keybind.setY(this.getY() + offsetY + 7);

                                    comps.add(new AddonsSetting(s, keybind));
                                }

                                if (s instanceof TextSetting) {

                                    TextSetting tSetting = (TextSetting) s;

                                    CompAddonTextBox textBox = new CompAddonTextBox(tSetting);

                                    textBox.setX(this.getX() + 322);
                                    textBox.setY(this.getY() + offsetY + 7);
                                    textBox.setWidth(75);
                                    textBox.setHeight(16);

                                    comps.add(new AddonsSetting(s, textBox));
                                }

                                if (s instanceof ColorSetting) {

                                    ColorSetting cSetting = (ColorSetting) s;
                                    CompColorPicker picker = new CompColorPicker(cSetting);

                                    picker.setX(this.getX()  + 298);
                                    picker.setY(this.getY() + offsetY + 8.5F);
                                    picker.setScale(0.8F);

                                    comps.add(new AddonsSetting(s, picker));
                                }

                                if (s instanceof CategorySetting) {
                                    CategorySetting cSetting = (CategorySetting) s;

                                    CompCategory category = new CompCategory(this.getWidth() - 45, cSetting);

                                    category.setX(this.getX() + 20);
                                    category.setY(this.getY() + offsetY + 25);

                                    comps.add(new AddonsSetting(s, category));
                                }


                                offsetY += 29;
                            }

                            settingScroll.resetAll();
                            currentAddon = a;
                            openSetting = true;
                            this.setCanClose(false);
                        }
                    }
                }

                offsetX += 130;

                if (index % 3 == 0) {
                    offsetX = 0;
                    offsetY += 115;
                }

                index++;
            }
        }

        if (openSetting && settingAnimation.isDone(Direction.BACKWARDS)) {
            if (MouseUtils.isInside(mouseX, mouseY, this.getX() + 22, this.getY() + 20, 18, 18) && mouseButton == 0) {
                openSetting = false;
            }
            int x = getX() - 32, y = getY() - 31, width = getWidth() + 32, height = getHeight() + 31;
            if (!MouseUtils.isInside(mouseX, mouseY, x - 5, y - 5, width + 10, height + 10) && mouseButton == 0) {
                openSetting = false;
            }

            for (AddonsSetting s : comps) {

                if (MouseUtils.isInside(mouseX, mouseY, this.getX(), this.getY(), this.getWidth(), this.getHeight()) && mouseButton == 0) {

                    s.comp.mouseClicked(mouseX, (int) (mouseY - settingScroll.getValue()), mouseButton);

                    if (s.comp instanceof CompColorPicker) {

                        CompColorPicker picker = (CompColorPicker) s.comp;
                        int openIndex = 1;

                        if (!picker.isInsideOpen(mouseX, (int) (mouseY - settingScroll.getValue()))) {
                            continue;
                        }

                        for (int i = 0; i < comps.size(); i++) {

                            if ((openIndex * 2) + (comps.indexOf(s)) < comps.size()) {

                                AddonsSetting s2 = comps.get((openIndex * 2) + (comps.indexOf(s)));
                                int add = picker.isShowAlpha() ? 100 : 85;

                                s2.openY += picker.isOpen() ? add : -add;
                            }

                            openIndex++;
                        }
                    }
                }
            }

            if (MouseUtils.isInside(mouseX, mouseY, this.getX() + this.getWidth() - 41, this.getY() + 15 + 6F, 16, 16) && mouseButton == 0) {

                for (AddonsSetting s : comps) {
                    s.setting.reset();
                }
            }
        }

        if (openSetting && mouseButton == 3) {
            openSetting = false;
        }


    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {

        for (AddonsSetting s : comps) {

            if (mouseButton == 0) {
                s.comp.mouseReleased(mouseX, mouseY, mouseButton);
            }
        }
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {

        boolean binding = false;

        for (AddonsSetting s : comps) {

            if (s.comp instanceof CompKeybind) {

                CompKeybind keybind = (CompKeybind) s.comp;

                if (keybind.isBinding()) {
                    binding = true;
                }
            }

            s.comp.keyTyped(typedChar, keyCode);
        }

        if (binding) {
            return;
        }

        if (openSetting && keyCode == Keyboard.KEY_ESCAPE) {
            openSetting = false;
        }
        if (!openSetting) {
            scroll.onKey(keyCode);
            if (keyCode != 0xD0 && keyCode != 0xC8 && keyCode != Keyboard.KEY_ESCAPE)
                this.getSearchBox().setFocused(true);
        }
    }

    private boolean filterAddon(Addon a) {

        if (!currentType.equals(AddonType.ALL) && !a.getType().equals(currentType)) {
            return true;
        }

        return !this.getSearchBox().getText().isEmpty() && !SearchUtils.isSimilar(Shindo.getInstance().getAddonManager().getWords(a), this.getSearchBox().getText());
    }

    private int getAddonSettingHeight() {

        int oddOutput = 0;
        int evenOutput = 0;
        int oddTotal = 0;
        int evenTotal = 0;

        for (int i = 0; i < comps.size(); i++) {

            oddOutput += 29;

            AddonsSetting s = comps.get(i);
            if (s.comp instanceof CompColorPicker) {
                CompColorPicker picker = (CompColorPicker) s.comp;
                if (picker.isOpen()) {
                    int add = picker.isShowAlpha() ? 100 : 85;
                    oddTotal += add;
                }
            }
        }

        int output = Math.max(oddOutput, evenOutput) + Math.max(oddTotal, evenTotal);

        return Math.max(0, output - (this.getHeight() - 72));
    }

    private static class AddonsSetting {

        private final SimpleAnimation openAnimation = new SimpleAnimation();

        private final AddonSetting setting;
        private final Comp comp;
        private float openY;

        public AddonsSetting(AddonSetting setting, Comp comp) {
            this.setting = setting;
            this.comp = comp;
            this.openY = 0;
        }
    }
}

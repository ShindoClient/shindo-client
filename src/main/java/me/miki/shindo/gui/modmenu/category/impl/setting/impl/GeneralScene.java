package me.miki.shindo.gui.modmenu.category.impl.setting.impl;

import me.miki.shindo.Shindo;
import me.miki.shindo.gui.modmenu.category.impl.SettingCategory;
import me.miki.shindo.gui.modmenu.category.impl.setting.SettingScene;
import me.miki.shindo.management.color.ColorManager;
import me.miki.shindo.management.color.palette.ColorPalette;
import me.miki.shindo.management.color.palette.ColorType;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.impl.InternalSettingsMod;
import me.miki.shindo.management.nanovg.NanoVGManager;
import me.miki.shindo.management.nanovg.font.LegacyIcon;
import me.miki.shindo.ui.comp.impl.CompKeybind;
import me.miki.shindo.ui.comp.impl.CompSettingButton;
import me.miki.shindo.ui.comp.impl.CompToggleButton;
import me.miki.shindo.utils.ColorUtils;
import me.miki.shindo.utils.mouse.MouseUtils;
import me.miki.shindo.utils.mouse.Scroll;

import java.util.ArrayList;
import java.util.List;

public class GeneralScene extends SettingScene {

    private final Scroll contentScroll = new Scroll();
    private CompKeybind modMenuKeybind;
    private CompToggleButton clickEffectSetting;
    private CompToggleButton soundsUISetting;
    private CompToggleButton mcFontSetting;
    private final List<CompSettingButton> settingCards = new ArrayList<CompSettingButton>();

    public GeneralScene(SettingCategory parent) {
        super(parent, TranslateText.GENERAL, TranslateText.GENERAL_DESCRIPTION, LegacyIcon.LIST);
    }

    @Override
    public void initGui() {
        modMenuKeybind = new CompKeybind(75, InternalSettingsMod.getInstance().getModMenuKeybindSetting());
        clickEffectSetting = new CompToggleButton(InternalSettingsMod.getInstance().getClickEffectsSetting());
        soundsUISetting = new CompToggleButton(InternalSettingsMod.getInstance().getSoundsUISetting());
        mcFontSetting = new CompToggleButton(InternalSettingsMod.getInstance().getMCHUDFont());
        contentScroll.resetAll();

        settingCards.clear();
        settingCards.add(new CompSettingButton(0F, TranslateText.OPEN_MOD_MENU::getText, TranslateText.OPEN_MOD_MENU_DESCRIPTION::getText)
                .trailing(modMenuKeybind));
        settingCards.add(new CompSettingButton(0F, TranslateText.CLICK_EFFECT::getText, TranslateText.CLICK_EFFECT_DESCRIPTION::getText)
                .trailing(clickEffectSetting)
                .onClick(() -> clickEffectSetting.getSetting().setToggled(!clickEffectSetting.getSetting().isToggled())));
        settingCards.add(new CompSettingButton(0F, TranslateText.UI_SOUNDS::getText, TranslateText.UI_SOUNDS_DESCRIPTION::getText)
                .trailing(soundsUISetting)
                .onClick(() -> soundsUISetting.getSetting().setToggled(!soundsUISetting.getSetting().isToggled())));
        settingCards.add(new CompSettingButton(0F, TranslateText.MC_FONT::getText, TranslateText.MC_FONT_DESCRIPTION::getText)
                .trailing(mcFontSetting)
                .onClick(() -> mcFontSetting.getSetting().setToggled(!mcFontSetting.getSetting().isToggled())));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        Shindo instance = Shindo.getInstance();
        NanoVGManager nvg = instance.getNanoVGManager();
        ColorManager colorManager = instance.getColorManager();
        ColorPalette palette = colorManager.getPalette();

        float baseX = getX();
        float baseY = getContentY();
        float baseWidth = getWidth();
        float baseHeight = getContentHeight();

        if (baseHeight <= 0F || baseWidth <= 0F) {
            return;
        }

        float radius = 12F;
        nvg.drawShadow(baseX, baseY, baseWidth, baseHeight, radius, 6);
        nvg.drawRoundedRect(baseX, baseY, baseWidth, baseHeight, radius,
                ColorUtils.applyAlpha(palette.getBackgroundColor(ColorType.DARK), 210));

        float cardHeight = 52F;
        float cardSpacing = 14F;
        float padding = 18F;
        int cardCount = settingCards.size();

        float contentHeight = padding * 2F + cardCount * cardHeight + Math.max(0, cardCount - 1) * cardSpacing;
        contentScroll.setMaxScroll(Math.max(0F, contentHeight - baseHeight));

        if (MouseUtils.isInside(mouseX, mouseY, baseX, baseY, baseWidth, baseHeight)) {
            contentScroll.onScroll();
        }
        contentScroll.onAnimation();
        float scrollValue = contentScroll.getValue();

        nvg.save();
        nvg.scissor(baseX, baseY, baseWidth, baseHeight);

        float currentY = baseY + padding + scrollValue;
        float cardWidth = baseWidth - 28F;

        for (CompSettingButton card : settingCards) {
            card.setBounds(baseX + 14F, currentY, cardWidth, cardHeight);
            card.draw(mouseX, mouseY, partialTicks);
            currentY += cardHeight + cardSpacing;
        }

        nvg.restore();
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        float baseX = getX();
        float baseY = getContentY();
        float baseWidth = getWidth();
        float baseHeight = getContentHeight();

        if (!MouseUtils.isInside(mouseX, mouseY, baseX, baseY, baseWidth, baseHeight)) {
            return;
        }

        for (CompSettingButton card : settingCards) {
            card.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        for (CompSettingButton card : settingCards) {
            card.mouseReleased(mouseX, mouseY, mouseButton);
        }
    }

    public void keyTyped(char typedChar, int keyCode) {
        if (modMenuKeybind.isBinding()) {
            modMenuKeybind.keyTyped(typedChar, keyCode);
        }
        for (CompSettingButton card : settingCards) {
            card.keyTyped(typedChar, keyCode);
        }
        contentScroll.onKey(keyCode);
    }
}

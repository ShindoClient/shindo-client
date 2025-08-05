package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventKey;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import me.miki.shindo.management.mods.settings.impl.KeybindSetting;
import me.miki.shindo.utils.PlayerUtils;
import net.minecraft.init.Items;
import org.lwjgl.input.Keyboard;

public class QuickSwitchMod extends Mod {

    private final KeybindSetting swordSetting = new KeybindSetting(TranslateText.SWORD, this, Keyboard.KEY_NONE);
    private final KeybindSetting blockSetting = new KeybindSetting(TranslateText.BLOCK, this, Keyboard.KEY_NONE);
    private final KeybindSetting rodSetting = new KeybindSetting(TranslateText.ROD, this, Keyboard.KEY_NONE);
    private final KeybindSetting axeSetting = new KeybindSetting(TranslateText.AXE, this, Keyboard.KEY_NONE);
    private final KeybindSetting pickaxeSetting = new KeybindSetting(TranslateText.PICKAXE, this, Keyboard.KEY_NONE);
    private final KeybindSetting bowSetting = new KeybindSetting(TranslateText.BOW, this, Keyboard.KEY_NONE);

    public QuickSwitchMod() {
        super(TranslateText.QUICK_SWITCH, TranslateText.QUICK_SWITCH_DESCRIPTION, ModCategory.PLAYER, "itemhotkey", true);
    }

    @EventTarget
    public void onKey(EventKey event) {

        if (event.getKeyCode() == swordSetting.getKeyCode()) {
            setCurrentItem(PlayerUtils.getBestSword(mc.thePlayer));
        }

        if (event.getKeyCode() == blockSetting.getKeyCode()) {
            setCurrentItem(PlayerUtils.getBestBlock(mc.thePlayer));
        }

        if (event.getKeyCode() == rodSetting.getKeyCode()) {
            setCurrentItem(PlayerUtils.getItemSlot(Items.fishing_rod));
        }

        if (event.getKeyCode() == axeSetting.getKeyCode()) {
            setCurrentItem(PlayerUtils.getBestAxe(mc.thePlayer));
        }

        if (event.getKeyCode() == pickaxeSetting.getKeyCode()) {
            setCurrentItem(PlayerUtils.getBestPickaxe(mc.thePlayer));
        }

        if (event.getKeyCode() == bowSetting.getKeyCode()) {
            setCurrentItem(PlayerUtils.getBestBow(mc.thePlayer));
        }
    }

    private void setCurrentItem(int slot) {
        mc.thePlayer.inventory.currentItem = slot;
    }
}

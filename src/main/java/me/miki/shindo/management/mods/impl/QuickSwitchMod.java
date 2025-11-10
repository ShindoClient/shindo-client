package me.miki.shindo.management.mods.impl;

import me.miki.shindo.management.settings.config.Property;
import me.miki.shindo.management.settings.config.PropertyType;
import me.miki.shindo.management.event.EventTarget;
import me.miki.shindo.management.event.impl.EventKey;
import me.miki.shindo.management.language.TranslateText;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.ModCategory;
import me.miki.shindo.utils.PlayerUtils;
import net.minecraft.init.Items;
import org.lwjgl.input.Keyboard;

public class QuickSwitchMod extends Mod {

    @Property(type = PropertyType.KEYBIND, translate = TranslateText.SWORD)
    private int swordKey = Keyboard.KEY_NONE;

    @Property(type = PropertyType.KEYBIND, translate = TranslateText.BLOCK)
    private int blockKey = Keyboard.KEY_NONE;

    @Property(type = PropertyType.KEYBIND, translate = TranslateText.ROD)
    private int rodKey = Keyboard.KEY_NONE;

    @Property(type = PropertyType.KEYBIND, translate = TranslateText.AXE)
    private int axeKey = Keyboard.KEY_NONE;

    @Property(type = PropertyType.KEYBIND, translate = TranslateText.PICKAXE)
    private int pickaxeKey = Keyboard.KEY_NONE;

    @Property(type = PropertyType.KEYBIND, translate = TranslateText.BOW)
    private int bowKey = Keyboard.KEY_NONE;

    public QuickSwitchMod() {
        super(TranslateText.QUICK_SWITCH, TranslateText.QUICK_SWITCH_DESCRIPTION, ModCategory.PLAYER, "itemhotkey", true);
    }

    @EventTarget
    public void onKey(EventKey event) {

        if (event.getKeyCode() == swordKey) {
            setCurrentItem(PlayerUtils.getBestSword(mc.thePlayer));
        }

        if (event.getKeyCode() == blockKey) {
            setCurrentItem(PlayerUtils.getBestBlock(mc.thePlayer));
        }

        if (event.getKeyCode() == rodKey) {
            setCurrentItem(PlayerUtils.getItemSlot(Items.fishing_rod));
        }

        if (event.getKeyCode() == axeKey) {
            setCurrentItem(PlayerUtils.getBestAxe(mc.thePlayer));
        }

        if (event.getKeyCode() == pickaxeKey) {
            setCurrentItem(PlayerUtils.getBestPickaxe(mc.thePlayer));
        }

        if (event.getKeyCode() == bowKey) {
            setCurrentItem(PlayerUtils.getBestBow(mc.thePlayer));
        }
    }

    private void setCurrentItem(int slot) {
        mc.thePlayer.inventory.currentItem = slot;
    }
}

package me.miki.shindo.management.mods.impl

import me.miki.shindo.management.event.EventTarget
import me.miki.shindo.management.event.impl.EventKey
import me.miki.shindo.management.language.TranslateText
import me.miki.shindo.management.mods.Mod
import me.miki.shindo.management.mods.ModCategory
import me.miki.shindo.management.nanovg.font.LegacyIcon
import me.miki.shindo.management.settings.config.Property
import me.miki.shindo.management.settings.config.PropertyType
import me.miki.shindo.utils.PlayerUtils.getBestAxe
import me.miki.shindo.utils.PlayerUtils.getBestBlock
import me.miki.shindo.utils.PlayerUtils.getBestBow
import me.miki.shindo.utils.PlayerUtils.getBestPickaxe
import me.miki.shindo.utils.PlayerUtils.getBestSword
import me.miki.shindo.utils.PlayerUtils.getItemSlot
import net.minecraft.init.Items
import org.lwjgl.input.Keyboard

class QuickSwitchMod : Mod(
    TranslateText.QUICK_SWITCH,
    TranslateText.QUICK_SWITCH_DESCRIPTION,
    ModCategory.PLAYER,
    LegacyIcon.MOD_QUICK_SWITCH,
    "itemhotkey",
    true
) {
    @Property(type = PropertyType.KEYBIND, translate = TranslateText.SWORD)
    private val swordKey = Keyboard.KEY_NONE

    @Property(type = PropertyType.KEYBIND, translate = TranslateText.BLOCK)
    private val blockKey = Keyboard.KEY_NONE

    @Property(type = PropertyType.KEYBIND, translate = TranslateText.ROD)
    private val rodKey = Keyboard.KEY_NONE

    @Property(type = PropertyType.KEYBIND, translate = TranslateText.AXE)
    private val axeKey = Keyboard.KEY_NONE

    @Property(type = PropertyType.KEYBIND, translate = TranslateText.PICKAXE)
    private val pickaxeKey = Keyboard.KEY_NONE

    @Property(type = PropertyType.KEYBIND, translate = TranslateText.BOW)
    private val bowKey = Keyboard.KEY_NONE

    @EventTarget
    fun onKey(event: EventKey) {
        if (event.getKeyCode() == swordKey) {
            setCurrentItem(getBestSword(mc.thePlayer))
        }

        if (event.getKeyCode() == blockKey) {
            setCurrentItem(getBestBlock(mc.thePlayer))
        }

        if (event.getKeyCode() == rodKey) {
            setCurrentItem(getItemSlot(Items.fishing_rod))
        }

        if (event.getKeyCode() == axeKey) {
            setCurrentItem(getBestAxe(mc.thePlayer))
        }

        if (event.getKeyCode() == pickaxeKey) {
            setCurrentItem(getBestPickaxe(mc.thePlayer))
        }

        if (event.getKeyCode() == bowKey) {
            setCurrentItem(getBestBow(mc.thePlayer))
        }
    }

    private fun setCurrentItem(slot: Int) {
        mc.thePlayer.inventory.currentItem = slot
    }
}





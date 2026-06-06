package com.shindoclient.shindo.management.mods.impl

import com.shindoclient.shindo.management.event.EventTarget
import com.shindoclient.shindo.management.event.impl.EventKey
import com.shindoclient.shindo.management.language.TranslateText
import com.shindoclient.shindo.management.mods.Mod
import com.shindoclient.shindo.management.mods.ModCategory
import com.shindoclient.shindo.management.nanovg.font.Shinconic
import com.shindoclient.shindo.management.settings.config.Property
import com.shindoclient.shindo.management.settings.config.PropertyType
import com.shindoclient.shindo.utils.PlayerUtils.getBestAxe
import com.shindoclient.shindo.utils.PlayerUtils.getBestBlock
import com.shindoclient.shindo.utils.PlayerUtils.getBestBow
import com.shindoclient.shindo.utils.PlayerUtils.getBestPickaxe
import com.shindoclient.shindo.utils.PlayerUtils.getBestSword
import com.shindoclient.shindo.utils.PlayerUtils.getItemSlot
import net.minecraft.init.Items
import org.lwjgl.input.Keyboard

class QuickSwitchMod :
    Mod(
        TranslateText.QUICK_SWITCH,
        TranslateText.QUICK_SWITCH_DESCRIPTION,
        ModCategory.PLAYER,
        Shinconic.MOD_QUICK_SWITCH,
        "itemhotkey",
        true,
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

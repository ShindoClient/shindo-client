package me.miki.shindo.injection.mixin.minecraft.client.renderer;

import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.impl.InventoryMod;
import me.miki.shindo.management.settings.impl.BooleanSetting;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.InventoryEffectRenderer;
import net.minecraft.inventory.Container;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(InventoryEffectRenderer.class)
public abstract class MixinInventoryEffectRenderer extends GuiContainer {

    public MixinInventoryEffectRenderer(Container inventorySlotsIn) {
        super(inventorySlotsIn);
    }

    @Redirect(method = "updateActivePotionEffects", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/InventoryEffectRenderer;guiLeft:I", ordinal = 0))
    public void preventPotionShift(InventoryEffectRenderer instance, int value) {

        InventoryMod mod = InventoryMod.instance;
        BooleanSetting preventPotionShiftSetting = mod.getPreventPotionShiftSetting();
        if (mod.isToggled() && preventPotionShiftSetting != null && preventPotionShiftSetting.isToggled()) {
            guiLeft = (width - xSize) / 2;
            return;
        }

        guiLeft = value;
    }
}


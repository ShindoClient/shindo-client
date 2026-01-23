package me.miki.shindo.injection.mixin.minecraft.enchantments;

import me.miki.shindo.utils.RomanNumeralUtil;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.util.StatCollector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enchantment.class)
public abstract class MixinEnchantment {
    @Shadow
    public abstract String getName();

    @Inject(method = "getTranslatedName", at = @At("HEAD"), cancellable = true)
    private void patcher$modifyRomanNumerals(int level, CallbackInfoReturnable<String> cir) {
        // PatcherAddon removed - using default enchantment names
    }
}

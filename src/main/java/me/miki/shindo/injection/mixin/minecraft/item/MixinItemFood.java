package me.miki.shindo.injection.mixin.minecraft.item;

import me.miki.shindo.injection.mixin.interfaces.item.IMixinItemFood;
import net.minecraft.item.ItemFood;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ItemFood.class)
public class MixinItemFood implements IMixinItemFood {

    @Shadow
    private int potionId;

    @Override
    public int client$getPotionID() {
        return potionId;
    }
}



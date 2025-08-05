package me.miki.shindo.injection.mixin.mixins.block;

import net.minecraft.block.BlockRedstoneTorch;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

@Mixin(BlockRedstoneTorch.class)
public class MixinBlockRedstoneTorch {

    @Shadow
    private static Map<World, List<?>> toggles;

    @Inject(method = "<clinit>", at = @At(value = "TAIL"))
    private static void fixMemorry(CallbackInfo ci) {
        toggles = new WeakHashMap<World, List<?>>();
    }
}

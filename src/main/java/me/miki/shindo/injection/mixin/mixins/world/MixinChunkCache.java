package me.miki.shindo.injection.mixin.mixins.world;

import me.miki.shindo.utils.EnumFacings;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.ChunkCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ChunkCache.class)
public class MixinChunkCache {

    @Redirect(method = "getLightForExt", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/EnumFacing;values()[Lnet/minecraft/util/EnumFacing;"))
    private EnumFacing[] getCachedArray() {
        return EnumFacings.FACINGS;
    }
}

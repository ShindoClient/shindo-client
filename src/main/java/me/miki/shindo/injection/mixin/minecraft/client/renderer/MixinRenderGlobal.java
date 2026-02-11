package me.miki.shindo.injection.mixin.minecraft.client.renderer;

import me.miki.shindo.injection.mixin.interfaces.client.renderer.IMixinRenderGlobal;
import me.miki.shindo.utils.EnumFacings;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RenderGlobal.class)
public abstract class MixinRenderGlobal implements IMixinRenderGlobal {

    @Shadow
    private WorldClient theWorld;

    @Redirect(method = "setupTerrain", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/EnumFacing;values()[Lnet/minecraft/util/EnumFacing;"))
    private EnumFacing[] setupTerrain$getCachedArray() {
        return EnumFacings.FACINGS;
    }

    @Override
    public WorldClient getWorldClient() {
        return theWorld;
    }
}



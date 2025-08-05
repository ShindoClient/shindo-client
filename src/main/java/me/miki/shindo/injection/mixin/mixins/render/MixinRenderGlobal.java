package me.miki.shindo.injection.mixin.mixins.render;

import me.miki.shindo.injection.interfaces.IMixinRenderGlobal;
import me.miki.shindo.injection.interfaces.IMixinVisGraph;
import me.miki.shindo.utils.EnumFacings;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RenderGlobal.class)
public class MixinRenderGlobal implements IMixinRenderGlobal {

    @Shadow
    private WorldClient theWorld;

    @Redirect(method = "setupTerrain", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/EnumFacing;values()[Lnet/minecraft/util/EnumFacing;"))
    private EnumFacing[] setupTerrain$getCachedArray() {
        return EnumFacings.FACINGS;
    }

    @ModifyVariable(method = "getVisibleFacings", at = @At("STORE"), ordinal = 0)
    private VisGraph onVisGraphCreated(VisGraph visgraph) {
        ((IMixinVisGraph) visgraph).setLimitScan(true);
        return visgraph;
    }

    @Override
    public WorldClient getWorldClient() {
        return theWorld;
    }
}

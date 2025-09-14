package me.miki.shindo.injection.mixin.mixins.client.renderer;

import me.miki.shindo.injection.interfaces.IMixinRenderGlobal;
import me.miki.shindo.injection.interfaces.IMixinVisGraph;
import me.miki.shindo.utils.EnumFacings;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.util.EnumFacing;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

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

    @Dynamic("OptiFine")
    @Redirect(
            method = "renderSky(Lnet/minecraft/client/renderer/WorldRenderer;FZ)V",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/renderer/RenderGlobal;renderDistance:I",
                    opcode = Opcodes.GETFIELD,
                    remap = false
            )
    )
    private int distanceOverride(RenderGlobal instance) {
        return 256;
    }

    @Dynamic("OptiFine")
    @Redirect(
            method = "renderSky(FI)V",
            slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/client/settings/GameSettings;renderDistanceChunks:I")),
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/client/renderer/RenderGlobal;vboEnabled:Z",
                    ordinal = 0
            )
    )
    private boolean fixVBO(RenderGlobal instance) {
        return false;
    }
}

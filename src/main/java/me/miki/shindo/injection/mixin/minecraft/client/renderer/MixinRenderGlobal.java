package me.miki.shindo.injection.mixin.minecraft.client.renderer;

import me.miki.shindo.injection.mixin.interfaces.client.renderer.IMixinRenderGlobal;
import me.miki.shindo.injection.mixin.interfaces.client.renderer.chunk.IMixinVisGraph;
import me.miki.shindo.management.addons.patcher.PatcherAddon;
import me.miki.shindo.utils.EnumFacings;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.util.EnumFacing;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

import java.lang.reflect.Field;

@Mixin(RenderGlobal.class)
public abstract class MixinRenderGlobal implements IMixinRenderGlobal {

    @Shadow
    private WorldClient theWorld;

    @Shadow
    private int renderDistanceChunks;

    @Redirect(method = "setupTerrain", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/EnumFacing;values()[Lnet/minecraft/util/EnumFacing;"))
    private EnumFacing[] setupTerrain$getCachedArray() {
        return EnumFacings.FACINGS;
    }

    @ModifyVariable(method = "getVisibleFacings", at = @At("STORE"), ordinal = 0)
    private VisGraph onVisGraphCreated(VisGraph visgraph) {
        PatcherAddon addon = PatcherAddon.getInstance();
        boolean enableFix = addon != null && addon.isToggled() && addon.getCullingFixSetting().isToggled();
        ((IMixinVisGraph) visgraph).setLimitScan(enableFix);
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
        return patcher$customSkyFixEnabled() ? 256 : patcher$getOptifineRenderDistance(instance);
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
        return patcher$customSkyFixEnabled() ? false : patcher$isVboEnabled();
    }

    @Unique
    private boolean patcher$customSkyFixEnabled() {
        PatcherAddon addon = PatcherAddon.getInstance();
        return addon != null && addon.isToggled() && addon.getCustomSkyFixSetting().isToggled();
    }

    @Unique
    private int patcher$getOptifineRenderDistance(RenderGlobal instance) {
        if (!patcher$renderDistanceFieldChecked) {
            try {
                patcher$renderDistanceField = RenderGlobal.class.getDeclaredField("renderDistance");
                patcher$renderDistanceField.setAccessible(true);
            } catch (NoSuchFieldException ignored) {
                patcher$renderDistanceField = null;
            }
            patcher$renderDistanceFieldChecked = true;
        }
        if (patcher$renderDistanceField != null) {
            try {
                return patcher$renderDistanceField.getInt(instance);
            } catch (IllegalAccessException ignored) {
                // fall through
            }
        }
        return renderDistanceChunks;
    }

    @Accessor("vboEnabled")
    protected abstract boolean patcher$isVboEnabled();

    @Unique
    private static Field patcher$renderDistanceField;
    @Unique
    private static boolean patcher$renderDistanceFieldChecked;
}



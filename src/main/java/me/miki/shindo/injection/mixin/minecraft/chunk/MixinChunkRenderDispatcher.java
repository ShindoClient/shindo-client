package me.miki.shindo.injection.mixin.minecraft.chunk;

import me.miki.shindo.logger.ShindoLogger;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.impl.FPSBoostMod;
import me.miki.shindo.management.settings.impl.BooleanSetting;
import me.miki.shindo.management.settings.impl.NumberSetting;
import net.minecraft.client.renderer.chunk.ChunkCompileTaskGenerator;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkRenderDispatcher.class)
public class MixinChunkRenderDispatcher {

    @Inject(method = "getNextChunkUpdate", at = @At("HEAD"))
    private void limitChunkUpdates(final CallbackInfoReturnable<ChunkCompileTaskGenerator> cir) {

        FPSBoostMod mod = FPSBoostMod.getInstance();

        if (mod != null && mod.isToggled() && mod.getChunkDelaySetting().isToggled()) {
            try {
                Thread.sleep(mod.getDelaySetting().getValueLong() * 15);
            } catch (InterruptedException e) {
                ShindoLogger.error("Failed to limit chunk updates", e);
            }
        }
    }
}

package me.miki.shindo.injection.mixin.mixins.chunk;

import me.miki.shindo.management.event.impl.EventRenderChunkPosition;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderChunk.class)
public class MixinRenderChunk {

    @Inject(method = "setPosition", at = @At("RETURN"))
    public void setPosition(BlockPos pos, CallbackInfo ci) {
        new EventRenderChunkPosition((RenderChunk) (Object) this, pos).call();
    }
}

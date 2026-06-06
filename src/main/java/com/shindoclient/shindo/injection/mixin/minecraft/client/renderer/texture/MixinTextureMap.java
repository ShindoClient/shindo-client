package com.shindoclient.shindo.injection.mixin.minecraft.client.renderer.texture;

import com.shindoclient.shindo.Shindo;
import com.shindoclient.shindo.management.event.impl.EventSwitchTexture;
import net.minecraft.client.renderer.texture.TextureMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TextureMap.class)
public class MixinTextureMap {

    @Inject(method = "loadTextureAtlas", at = @At("RETURN"))
    public void preLoadTextureAtlas(CallbackInfo ci) {
        if (Shindo.getInstance().hasStarted()) {
            new EventSwitchTexture().call();
        }
    }
}


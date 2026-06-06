package com.shindoclient.shindo.injection.mixin.minecraft.client.renderer.texture;

import com.shindoclient.shindo.management.mods.impl.InternalSettingsMod;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(TextureManager.class)
public abstract class MixinTextureManager {

    @Unique
    private final Map<ResourceLocation, Boolean> loadingTextures = new ConcurrentHashMap<>();

    @Final
    @Shadow
    private Map<ResourceLocation, ITextureObject> mapTextureObjects;

    @Inject(method = "loadTexture", at = @At("HEAD"))
    private void onTextureLoadStart(ResourceLocation p_110579_0_, ITextureObject textureLocation, CallbackInfoReturnable<Boolean> cir) {
        if (InternalSettingsMod.instance == null) return;
        if (!InternalSettingsMod.instance.textureOptimizationSetting) return;

        loadingTextures.put(p_110579_0_, Boolean.TRUE);
    }

    @Inject(method = "deleteTexture", at = @At("TAIL"))
    private void onTextureDeleted(ResourceLocation textureLocation, CallbackInfo ci) {
        if (textureLocation == null) return;
        if (InternalSettingsMod.instance == null) return;
        if (!InternalSettingsMod.instance.textureOptimizationSetting) return;
        loadingTextures.remove(textureLocation);
    }

}

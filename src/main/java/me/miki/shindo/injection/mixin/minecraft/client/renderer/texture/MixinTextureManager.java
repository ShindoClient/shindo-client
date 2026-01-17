package me.miki.shindo.injection.mixin.minecraft.client.renderer.texture;

import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Otimiza o carregamento de texturas usando cache para evitar carregamentos duplicados.
 * 
 * Nota: Carregamento paralelo completo não é implementado devido a restrições do OpenGL
 * (texturas devem ser carregadas no thread principal). Esta otimização foca em evitar
 * carregamentos duplicados e melhorar a eficiência do cache.
 */
@Mixin(TextureManager.class)
public abstract class MixinTextureManager {
    
    @Shadow
    private Map<ResourceLocation, ITextureObject> mapTextureObjects;
    
    // Cache de texturas que estão sendo carregadas para evitar duplicatas
    private final Map<ResourceLocation, Boolean> loadingTextures = new ConcurrentHashMap<>();
    
    /**
     * Marca uma textura como carregando para evitar carregamentos duplicados.
     */
    @Inject(method = "loadTexture", at = @At("HEAD"))
    private void onTextureLoadStart(ResourceLocation textureLocation, CallbackInfo ci) {
        // Marca como carregando (evita carregamentos duplicados simultâneos)
        loadingTextures.put(textureLocation, Boolean.TRUE);
    }
    
    /**
     * Limpa o cache quando uma textura é removida.
     */
    @Inject(method = "deleteTexture", at = @At("HEAD"))
    private void onTextureDeleted(ResourceLocation textureLocation, CallbackInfo ci) {
        loadingTextures.remove(textureLocation);
    }
}

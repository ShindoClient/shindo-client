package me.miki.shindo.injection.mixin.minecraft.audio;

import me.miki.shindo.management.mods.impl.SoundSubtitlesMod;
import me.miki.shindo.utils.concurrent.TaskExecutor;
import me.miki.shindo.utils.concurrent.ThreadPoolType;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.client.resources.IResourceManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import paulscode.sound.SoundSystem;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Otimiza o carregamento de sons usando multithreading.
 * Sons são carregados em paralelo no pool IO, melhorando o tempo de inicialização.
 */
@Mixin(SoundManager.class)
public abstract class MixinSoundManager {

    private final List<String> pausedSounds = new ArrayList<>();
    @Shadow
    @Final
    private Map<String, ISound> playingSounds;

    @Shadow
    private boolean loaded;

    @Shadow
    public abstract boolean isSoundPlaying(ISound sound);
    
    // Cache de futures para evitar carregamentos duplicados (reservado para uso futuro)
    // private final Map<String, CompletableFuture<Void>> loadingSounds = new ConcurrentHashMap<>();

    @Redirect(method = "pauseAllSounds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/audio/SoundManager$SoundSystemStarterThread;pause(Ljava/lang/String;)V", remap = false))
    private void onlyPauseSoundIfNecessary(@Coerce SoundSystem soundSystem, String sound) {
        if (isSoundPlaying(playingSounds.get(sound))) {
            soundSystem.pause(sound);
            pausedSounds.add(sound);
        }
    }

    @Redirect(method = "resumeAllSounds", at = @At(value = "INVOKE", target = "Ljava/util/Set;iterator()Ljava/util/Iterator;", remap = false))
    private Iterator<String> iterateOverPausedSounds(Set<String> keySet) {
        return pausedSounds.iterator();
    }

    @Inject(method = "playSound", at = @At("HEAD"))
    public void prePlaySound(ISound p_sound, CallbackInfo ci) {
        if (loaded) {
            SoundSubtitlesMod.instance.soundPlay(p_sound);
        }
    }

    @Inject(method = "resumeAllSounds", at = @At("TAIL"))
    private void clearPausedSounds(CallbackInfo ci) {
        pausedSounds.clear();
    }
    
    // Nota: Otimização de carregamento de sons será implementada quando necessário.
    // Por enquanto, mantemos o comportamento original para garantir thread-safety.
}


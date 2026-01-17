package me.miki.shindo.injection.mixin.minecraft.chunk;

import me.miki.shindo.utils.concurrent.TaskExecutor;
import me.miki.shindo.utils.concurrent.TaskPriority;
import me.miki.shindo.utils.concurrent.ThreadPoolType;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Otimiza o carregamento e salvamento de chunks usando multithreading.
 * A leitura/escrita de arquivos é feita em paralelo no pool IO, enquanto
 * o processamento final (descompressão, parsing NBT) acontece no thread principal.
 */
@Mixin(AnvilChunkLoader.class)
public class MixinAnvilChunkLoader {

    @Shadow
    private File chunkSaveLocation;
    
    // Cache de chunks sendo carregados para evitar carregamentos duplicados
    private final ConcurrentHashMap<ChunkCoordIntPair, CompletableFuture<NBTTagCompound>> loadingChunks = new ConcurrentHashMap<>();
    
    // Cache de chunks sendo salvos para evitar salvamentos duplicados
    private final ConcurrentHashMap<ChunkCoordIntPair, CompletableFuture<Void>> savingChunks = new ConcurrentHashMap<>();

    @Inject(method = "loadChunk", at = {@At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompressedStreamTools;read(Ljava/io/DataInputStream;)Lnet/minecraft/nbt/NBTTagCompound;", shift = At.Shift.AFTER)}, locals = LocalCapture.CAPTURE_FAILHARD)
    private void closeInputstream(final World worldIn, final int x, final int z, final CallbackInfoReturnable<Chunk> cir, final ChunkCoordIntPair pair, final NBTTagCompound nbt, final DataInputStream inputStream) throws IOException {
        inputStream.close();
    }
    
    /**
     * Otimiza a leitura de NBT de chunks fazendo a leitura do arquivo em paralelo.
     * O processamento do NBT ainda acontece no thread principal para garantir thread-safety.
     */
    @Redirect(
        method = "loadChunk",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/nbt/CompressedStreamTools;read(Ljava/io/DataInputStream;)Lnet/minecraft/nbt/NBTTagCompound;"
        )
    )
    private NBTTagCompound optimizeChunkLoading(DataInputStream inputStream, World worldIn, int x, int z) throws IOException {
        ChunkCoordIntPair pair = new ChunkCoordIntPair(x, z);
        
        // Verifica se já está carregando
        CompletableFuture<NBTTagCompound> existing = loadingChunks.get(pair);
        if (existing != null && !existing.isDone()) {
            try {
                return existing.get();
            } catch (Exception e) {
                // Se falhar, remove do cache e carrega normalmente
                loadingChunks.remove(pair);
            }
        }
        
        // Lê o arquivo em paralelo
        CompletableFuture<NBTTagCompound> future = TaskExecutor.runAsync(ThreadPoolType.IO, TaskPriority.NORMAL, (Supplier<NBTTagCompound>) () -> {
            try {
                return CompressedStreamTools.read(inputStream);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        
        loadingChunks.put(pair, future);
        
        try {
            NBTTagCompound result = future.get();
            loadingChunks.remove(pair);
            return result;
        } catch (Exception e) {
            loadingChunks.remove(pair);
            Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                throw (IOException) cause;
            }
            if (cause instanceof RuntimeException && cause.getCause() instanceof IOException) {
                throw (IOException) cause.getCause();
            }
            throw new IOException("Failed to load chunk", cause);
        }
    }
    
    /**
     * Otimiza o salvamento de chunks fazendo a escrita em paralelo.
     */
    @Redirect(
        method = "saveChunk",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/nbt/CompressedStreamTools;write(Lnet/minecraft/nbt/NBTTagCompound;Ljava/io/DataOutputStream;)V"
        )
    )
    private void optimizeChunkSaving(NBTTagCompound compound, DataOutputStream outputStream, World worldIn, Chunk chunk) throws IOException {
        ChunkCoordIntPair pair = new ChunkCoordIntPair(chunk.xPosition, chunk.zPosition);
        
        // Verifica se já está salvando
        CompletableFuture<Void> existing = savingChunks.get(pair);
        if (existing != null && !existing.isDone()) {
            try {
                existing.get();
                return; // Já está sendo salvo
            } catch (Exception e) {
                // Se falhar, remove do cache e salva normalmente
                savingChunks.remove(pair);
            }
        }
        
        // Salva em paralelo
        CompletableFuture<Void> future = TaskExecutor.runAsync(ThreadPoolType.IO, TaskPriority.NORMAL, (Runnable) () -> {
            try {
                CompressedStreamTools.write(compound, outputStream);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        
        savingChunks.put(pair, future);
        
        try {
            future.get();
            savingChunks.remove(pair);
        } catch (Exception e) {
            savingChunks.remove(pair);
            Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                throw (IOException) cause;
            }
            if (cause instanceof RuntimeException && cause.getCause() instanceof IOException) {
                throw (IOException) cause.getCause();
            }
            throw new IOException("Failed to save chunk", cause);
        }
    }
}


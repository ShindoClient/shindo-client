package me.miki.shindo.injection.mixin.minecraft.network;

import me.miki.shindo.management.network.optimization.NetworkOptimizationManager;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Mixin para otimização de processamento de pacotes de rede.
 * 
 * Delega o processamento para o NetworkOptimizationManager que gerencia
 * a classificação e processamento otimizado de pacotes.
 */
@Mixin(targets = "net.minecraft.network.PacketThreadUtil$1")
public class MixinPacketThreadUtil {

    @Redirect(method = "run", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Packet;processPacket(Lnet/minecraft/network/INetHandler;)V"))
    private void ignorePacketsFromClosedConnections(Packet<INetHandler> packet, INetHandler handler) {
        // Delega o processamento para o NetworkOptimizationManager
        // que gerencia toda a lógica de otimização
        me.miki.shindo.management.network.optimization.NetworkOptimizationManager.INSTANCE.processPacket(packet, handler);
    }
}

package me.miki.shindo.injection.mixin.mixins.network;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import me.miki.shindo.Shindo;
import me.miki.shindo.management.event.impl.EventReceivePacket;
import me.miki.shindo.management.event.impl.EventSendPacket;
import me.miki.shindo.management.network.NetworkManager;
import me.miki.shindo.viaversion.netty.event.CompressionReorderEvent;
import net.minecraft.network.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.network.NetworkManager.class)
public class MixinNetworkManager {

    @Shadow
    private Channel channel;

    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;)V", at = @At("HEAD"), cancellable = true)
    public void preSendPacket(Packet<?> packet, CallbackInfo ci) {

        EventSendPacket event = new EventSendPacket(packet);
        event.call();

        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;)V", at = @At("TAIL"))
    public void postSendPacket(Packet<?> packet, CallbackInfo ci) {
        NetworkManager manager = Shindo.getInstance().getConnectionTweakerManager();
        if (manager != null) {
            manager.onSendPacket(channel, packet);
        }
    }

    @Inject(method = "channelRead0", at = @At("HEAD"), cancellable = true)
    public void preChannelRead0(ChannelHandlerContext context, Packet<?> packet, CallbackInfo ci) {

        EventReceivePacket event = new EventReceivePacket(packet);
        event.call();

        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "setCompressionTreshold", at = @At("TAIL"))
    public void setUserEvent(int treshold, CallbackInfo ci) {
        this.channel.pipeline().fireUserEventTriggered(new CompressionReorderEvent());
    }

    @Inject(method = "channelActive", at = @At("TAIL"))
    private void onChannelActive(ChannelHandlerContext context, CallbackInfo ci) {
        NetworkManager manager = Shindo.getInstance().getConnectionTweakerManager();
        if (manager != null) {
            manager.applyChannel(channel);
        }
    }

}

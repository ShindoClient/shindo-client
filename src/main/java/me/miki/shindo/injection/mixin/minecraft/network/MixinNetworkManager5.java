package me.miki.shindo.injection.mixin.minecraft.network;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.connection.UserConnectionImpl;
import com.viaversion.viaversion.protocol.ProtocolPipelineImpl;
import io.netty.channel.Channel;
import io.netty.channel.socket.SocketChannel;
import me.miki.shindo.management.mods.Mod;
import me.miki.shindo.management.mods.impl.ViaVersionMod;
import me.miki.shindo.viaversion.MCPVLBPipeline;
import me.miki.shindo.viaversion.ViaLoadingBase;
import me.miki.shindo.viaversion.ViaShindo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(targets = "net.minecraft.network.NetworkManager$5")
public class MixinNetworkManager5 {

    @Inject(method = "initChannel", at = @At(value = "TAIL"), remap = false)
    private void onInitChannel(Channel channel, CallbackInfo ci) {

        ViaVersionMod viaMod = ViaVersionMod.instance;
        if (viaMod.isToggled() && viaMod.isLoaded() && channel instanceof SocketChannel &&
                ViaLoadingBase.getInstance().getTargetVersion().getVersion() != ViaShindo.NATIVE_VERSION) {
            final UserConnection user = new UserConnectionImpl(channel, true);
            new ProtocolPipelineImpl(user);

            channel.pipeline().addLast(new MCPVLBPipeline(user));
        }
    }
}


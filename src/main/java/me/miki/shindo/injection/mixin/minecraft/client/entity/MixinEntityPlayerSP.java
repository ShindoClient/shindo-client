package me.miki.shindo.injection.mixin.minecraft.client.entity;

import com.mojang.authlib.GameProfile;
import me.miki.shindo.management.event.impl.EventMotionUpdate;
import me.miki.shindo.management.event.impl.EventSendChat;
import me.miki.shindo.management.event.impl.EventUpdate;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPlayerSP.class)
public class MixinEntityPlayerSP extends AbstractClientPlayer {


    public MixinEntityPlayerSP(World worldIn, GameProfile playerProfile) {
        super(worldIn, playerProfile);
    }

    @Inject(method = "onUpdate", at = @At("HEAD"))
    public void preOnUpdate(CallbackInfo ci) {
        new EventUpdate().call();
    }

    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    public void preSendChatMessage(String message, CallbackInfo ci) {

        EventSendChat event = new EventSendChat(message);
        event.call();

        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "onUpdateWalkingPlayer", at = @At("HEAD"))
    private void preOnUpdateWalkingPlayer(CallbackInfo ci) {
        new EventMotionUpdate().call();
    }
}

